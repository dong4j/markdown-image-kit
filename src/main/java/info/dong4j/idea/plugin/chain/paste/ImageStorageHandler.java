/*
 * MIT License
 *
 * Copyright (c) 2019 dong4j <dong4j@gmail.com>
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
 *
 */

package info.dong4j.idea.plugin.chain.paste;

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;

import info.dong4j.idea.plugin.MikBundle;
import info.dong4j.idea.plugin.chain.PasteActionHandler;
import info.dong4j.idea.plugin.content.ImageContents;
import info.dong4j.idea.plugin.entity.EventData;
import info.dong4j.idea.plugin.settings.ImageManagerPersistenComponent;
import info.dong4j.idea.plugin.settings.ImageManagerState;

import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;

/**
 * <p>Company: 科大讯飞股份有限公司-四川分公司</p>
 * <p>Description: 保存图片操作 </p>
 *
 * @author dong4j
 * @email sjdong3 @iflytek.com
 * @since 2019 -03-26 12:35
 */
@Slf4j
public class ImageStorageHandler extends PasteActionHandler {

    @Override
    public boolean isEnabled(EventData data) {
        return STATE.isCopyToDir() && STATE.isClipboardControl();
    }

    /**
     * 将临时文件 copy 到用户设置的目录下, 返回处理后的 markdown image mark list
     *
     * @return the boolean
     */
    @Override
    public boolean execute(EventData data) {
        log.trace("save image to dir");

        Editor editor = data.getEditor();

        // 后台任务保存图片到指定目录
        Task.Backgroundable task = new Task.Backgroundable(editor.getProject(),
                                                           MikBundle.message("mik.paste.save.progress"),
                                                           true) {
            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                startBackgroupTask(indicator);

                Map<String, File> imageMap = data.getImageMap();
                List<String> markList = new ArrayList<>(imageMap.size());

                int totalCount = imageMap.size();
                int totalProcessed = 0;
                try {
                    for (Map.Entry<String, File> imageEntry : imageMap.entrySet()) {
                        String fileName = imageEntry.getKey();
                        File willProcessedFile = imageEntry.getValue();
                        indicator.setText("Processing " + fileName);

                        ImageManagerState state = ImageManagerPersistenComponent.getInstance().getState();
                        Document document = editor.getDocument();
                        VirtualFile currentFile = FileDocumentManager.getInstance().getFile(document);
                        assert currentFile != null;
                        File curDocument = new File(currentFile.getPath());
                        String savepath = state.getImageSavePath();
                        // 被保存图片的路径
                        File imageDir = new File(curDocument.getParent(), savepath);

                        boolean checkDir = imageDir.exists() && imageDir.isDirectory();

                        if (checkDir || imageDir.mkdirs()) {
                            // 保存的文件路径
                            File finalFile = new File(imageDir, fileName);
                            indicator.setText2("Save Image");
                            // 写入到文件
                            try {
                                FileUtils.copyFile(willProcessedFile, finalFile);
                            } catch (IOException e) {
                                log.trace("", e);
                                continue;
                            }

                            // 保存后 imageMap 对应的 File 修改为保存后的图片
                            imageMap.put(fileName, finalFile);
                            // 保存标签
                            File imageFileRelativizePath = curDocument.getParentFile().toPath().relativize(finalFile.toPath()).toFile();
                            String relImagePath = imageFileRelativizePath.toString().replace('\\', '/');
                            String mark = "![](" + relImagePath + ")" + ImageContents.LINE_BREAK;
                            indicator.setText2("Save Mark: " + mark);
                            markList.add(mark);
                            indicator.setFraction(++totalProcessed * 1.0 / totalCount);
                        }
                    }
                    data.setSaveMarkList(markList);
                } finally {
                    endBackgroupTask(indicator);
                    this.onSuccess();
                }
            }

            @Override
            public void onCancel() {
                log.trace("cancel callback");
            }

            @Override
            public void onSuccess() {
                log.trace("success callback");
            }

            @Override
            public void onFinished() {
                log.trace("finished callback");
                // 保存到文件后同步刷新缓存, 让图片显示到文件树中
                VirtualFileManager.getInstance().syncRefresh();
                // todo-dong4j : (2019年03月20日 17:42) [使用 VirtualFile.createChildData() 创建虚拟文件]
                //  以解决还未刷新前使用右键上传图片时找不到文件的问题.
                data.setSaveImageFinished(true);
            }
        };

        task.queue();

        return false;
    }
}
