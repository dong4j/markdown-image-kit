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

package info.dong4j.idea.plugin.handler;

import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.editor.Caret;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.actionSystem.EditorActionHandler;
import com.intellij.openapi.editor.actionSystem.EditorTextInsertHandler;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.vcs.AbstractVcs;
import com.intellij.openapi.vcs.ProjectLevelVcsManager;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.Producer;
import com.intellij.util.containers.hash.HashMap;

import info.dong4j.idea.plugin.settings.ImageManagerPersistenComponent;
import info.dong4j.idea.plugin.settings.ImageManagerState;
import info.dong4j.idea.plugin.util.CharacterUtils;
import info.dong4j.idea.plugin.util.ImageUtils;
import info.dong4j.idea.plugin.util.MarkdownUtils;
import info.dong4j.idea.plugin.watch.ActionManager;
import info.dong4j.idea.plugin.watch.FinalActionHandler;
import info.dong4j.idea.plugin.watch.SaveAndInsertHandler;
import info.dong4j.idea.plugin.watch.UploadAndInsertHandler;

import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.Image;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.io.*;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.imageio.ImageIO;

import lombok.extern.slf4j.Slf4j;

/**
 * <p>Company: 科大讯飞股份有限公司-四川分公司</p>
 * <p>Description: 读取 Clipboard 图片, 上传到 OSS, 最后插入到光标位置</p>
 * todo-dong4j : (2019年03月17日 19:37) [图片压缩处理]
 *
 * @author dong4j
 * @date 2019 -03-16 12:15
 * @email sjdong3 @iflytek.com
 */
@Slf4j
public class PasteImageHandler extends EditorActionHandler implements EditorTextInsertHandler {
    private final EditorActionHandler editorActionHandler;

    /**
     * Instantiates a new Paste image handler.
     *
     * @param originalAction the original action
     */
    public PasteImageHandler(EditorActionHandler originalAction) {
        editorActionHandler = originalAction;
    }

    /**
     * 使用 paste 功能入口
     * 从 clipboard 操作图片文件
     * 1. 如果是 image 类型, 根据设置进行处理
     * 2. 如果是 file 类型, 则获取文件路径然后根据设置处理
     *
     * @param editor      the editor
     * @param caret       the caret
     * @param dataContext the data context
     */
    @Override
    protected void doExecute(@NotNull Editor editor, @Nullable Caret caret, DataContext dataContext) {

        Document document = editor.getDocument();
        VirtualFile virtualFile = FileDocumentManager.getInstance().getFile(document);

        if (virtualFile != null && MarkdownUtils.isMardownFile(virtualFile)) {
            ImageManagerState state = ImageManagerPersistenComponent.getInstance().getState();
            boolean isClipboardControl = state.isClipboardControl();

            if (isClipboardControl) {
                Map<DataFlavor, Object> clipboardData = ImageUtils.getDataFromClipboard();
                if (clipboardData != null) {
                    Iterator<Map.Entry<DataFlavor, Object>> iterator = clipboardData.entrySet().iterator();
                    Map.Entry<DataFlavor, Object> entry = iterator.next();

                    Map<String, Image> imageMap = resolveClipboardData(state, entry);

                    if (imageMap.size() == 0) {
                        defaultAction(editor, caret, dataContext);
                        return;
                    }

                    // todo-dong4j : (2019年03月25日 12:26) [通知一次]
                    new ActionManager()
                        .addHandler(new SaveAndInsertHandler(editor, imageMap))
                        .addHandler(new UploadAndInsertHandler(editor, imageMap))
                        .addHandler(new FinalActionHandler())
                        .invoke();
                    return;
                }
            }
        }
        defaultAction(editor, caret, dataContext);
    }

    private Map<String, Image> resolveClipboardData(ImageManagerState state, @NotNull Map.Entry<DataFlavor, Object> entry) {
        Map<String, Image> imageMap = new HashMap<>(10);
        if (entry.getKey().equals(DataFlavor.javaFileListFlavor)) {
            // 肯定是 List<File> 类型
            @SuppressWarnings("unchecked") List<File> fileList = (List<File>) entry.getValue();

            for (File file : fileList) {
                // 第一步先初步排除非图片类型, 避免复制大量文件导致 OOM
                if (StringUtils.isBlank(ImageUtils.getImageType(file.getName()))) {
                    return imageMap;
                }
                // 先检查是否为图片类型
                Image image;
                try {
                    File compressedFile = new File(System.getProperty("java.io.tmpdir") + file.getName());
                    // todo-dong4j : (2019年03月20日 04:29) [判断是否启动图片压缩]
                    if (file.isFile() && file.getName().endsWith("jpg")) {
                        ImageUtils.compress(file, compressedFile, state.getCompressBeforeUploadOfPercent() - 20);
                    } else {
                        compressedFile = file;
                    }
                    // 读到缓冲区
                    image = ImageIO.read(compressedFile);
                } catch (IOException ignored) {
                    // 如果抛异常, 则不是图片, 直接返回, 避免 OOM
                    imageMap.clear();
                    return imageMap;
                }
                String fileName = file.getName();
                // 只要有一个文件不是 image, 就执行默认操作然后退出
                if (image != null) {
                    imageMap.put(fileName, image);
                }
            }
        } else {
            // image 类型统一重命名, 因为获取不到文件名
            String fileName = CharacterUtils.getRandomString(6) + ".png";
            imageMap.put(fileName, (Image) entry.getValue());
        }
        return imageMap;
    }

    /**
     * 默认 paste 操作, 如果是文件的话应该粘贴文件名
     *
     * @param editor      the editor
     * @param caret       the caret
     * @param dataContext the data context
     */
    private void defaultAction(@NotNull Editor editor, @Nullable Caret caret, DataContext dataContext) {
        // 执行默认的 paste 操作
        if (editorActionHandler != null) {
            editorActionHandler.execute(editor, caret, dataContext);
        }
    }

    /**
     * Create virtual file.
     * https://intellij-support.jetbrains.com/hc/en-us/community/posts/206144389-Create-virtual-file-from-file-path
     *
     * @param ed        the ed
     * @param imageFile the image file
     */
    private void createVirtualFile(Editor ed, File imageFile) {
        VirtualFile fileByPath = LocalFileSystem.getInstance().refreshAndFindFileByIoFile(imageFile);
        assert fileByPath != null;
        AbstractVcs usedVcs = ProjectLevelVcsManager.getInstance(Objects.requireNonNull(ed.getProject())).getVcsFor(fileByPath);
        if (usedVcs != null && usedVcs.getCheckinEnvironment() != null) {
            usedVcs.getCheckinEnvironment().scheduleUnversionedFilesForAddition(Collections.singletonList(fileByPath));
        }
    }

    @Override
    public void execute(Editor editor, DataContext dataContext, Producer<Transferable> producer) {

    }
}
