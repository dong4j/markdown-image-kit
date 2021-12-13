/*
 * MIT License
 *
 * Copyright (c) 2021 dong4j <dong4j@gmail.com>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package info.dong4j.idea.plugin.chain;

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.VirtualFile;

import info.dong4j.idea.plugin.MikBundle;
import info.dong4j.idea.plugin.entity.EventData;
import info.dong4j.idea.plugin.entity.MarkdownImage;
import info.dong4j.idea.plugin.enums.ImageLocationEnum;
import info.dong4j.idea.plugin.enums.ImageMarkEnum;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;

/**
 * <p>Company: no company</p>
 * <p>Description: 保存图片操作 </p>
 *
 * @author dong4j
 * @version 0.0.1
 * @email "mailto:dong4j@gmail.com"
 * @date 2021.02.14 18:40
 * @since 0.0.1
 */
@Slf4j
public class ImageStorageHandler extends ActionHandlerAdapter {

    /**
     * Gets name *
     *
     * @return the name
     * @since 0.0.1
     */
    @Override
    public String getName() {
        return MikBundle.message("mik.action.store.title");
    }

    /**
     * Is enabled
     *
     * @param data data
     * @return the boolean
     * @since 0.0.1
     */
    @Override
    public boolean isEnabled(EventData data) {
        return STATE.isCopyToDir();
    }

    /**
     * Execute
     *
     * @param data data
     * @return the boolean
     * @since 0.0.1
     */
    @Override
    public boolean execute(EventData data) {
        ProgressIndicator indicator = data.getIndicator();
        int size = data.getSize();
        int totalProcessed = 0;

        for (Map.Entry<Document, List<MarkdownImage>> imageEntry : data.getWaitingProcessMap().entrySet()) {
            int totalCount = imageEntry.getValue().size();
            Document document = imageEntry.getKey();
            VirtualFile currentFile = FileDocumentManager.getInstance().getFile(document);
            if (currentFile == null) {
                continue;
            }

            File curDocument = new File(currentFile.getPath());
            String savepath = STATE.getImageSavePath();

            for (MarkdownImage markdownImage : imageEntry.getValue()) {
                String imageName = markdownImage.getImageName();
                indicator.setText2("Processing " + imageName);
                indicator.setFraction(((++totalProcessed * 1.0) + data.getIndex() * size) / totalCount * size);

                // 将 inputstream 转成 file
                File saveFile = null;
                File imageDir = new File(curDocument.getParent(), savepath);
                boolean checkDir = imageDir.exists() && imageDir.isDirectory();
                if (checkDir || imageDir.mkdirs()) {
                    // 保存的文件路径
                    saveFile = new File(imageDir, imageName);
                }
                if (saveFile == null) {
                    markdownImage.setFinalMark("copy error");
                    markdownImage.setImageMarkType(ImageMarkEnum.ORIGINAL);
                    markdownImage.setLocation(ImageLocationEnum.LOCAL);
                    continue;
                }

                try {
                    // todo-dong4j : (2019年03月29日 16:00) [如果覆盖 inputstream 所属文件将导致拷贝的文件错误]
                    FileUtil.copy(markdownImage.getInputStream(), new FileOutputStream(saveFile));
                } catch (IOException e) {
                    log.trace("", e);
                    markdownImage.setFinalMark("copy error");
                    markdownImage.setImageMarkType(ImageMarkEnum.ORIGINAL);
                    markdownImage.setLocation(ImageLocationEnum.LOCAL);
                    continue;
                }

                // 保存标签
                File imageFileRelativizePath = curDocument.getParentFile().toPath().relativize(saveFile.toPath()).toFile();
                String relImagePath = imageFileRelativizePath.toString().replace('\\', '/');
                markdownImage.setTitle("");
                markdownImage.setPath(relImagePath);
                try {
                    markdownImage.setInputStream(new FileInputStream(saveFile));
                } catch (FileNotFoundException e) {
                    log.trace("", e);
                }
                String mark = "![](" + relImagePath + ")";
                markdownImage.setOriginalLineText(mark);
                markdownImage.setOriginalMark(mark);
                markdownImage.setImageMarkType(ImageMarkEnum.ORIGINAL);
                markdownImage.setLocation(ImageLocationEnum.LOCAL);
                markdownImage.setFinalMark(mark);
            }
        }
        return true;
    }
}
