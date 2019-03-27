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
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.AbstractVcs;
import com.intellij.openapi.vcs.ProjectLevelVcsManager;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.Producer;
import com.intellij.util.containers.hash.HashMap;

import info.dong4j.idea.plugin.chain.ActionManager;
import info.dong4j.idea.plugin.chain.ImageCompressHandler;
import info.dong4j.idea.plugin.chain.ImageLabelChangeHandler;
import info.dong4j.idea.plugin.chain.ImageLabelInsertHandler;
import info.dong4j.idea.plugin.chain.paste.ImageStorageHandler;
import info.dong4j.idea.plugin.chain.paste.ImageUploadHandler;
import info.dong4j.idea.plugin.entity.EventData;
import info.dong4j.idea.plugin.enums.InsertEnum;
import info.dong4j.idea.plugin.exception.UploadException;
import info.dong4j.idea.plugin.notify.UploadNotification;
import info.dong4j.idea.plugin.settings.ImageManagerPersistenComponent;
import info.dong4j.idea.plugin.settings.ImageManagerState;
import info.dong4j.idea.plugin.task.ChainBackgroupTask;
import info.dong4j.idea.plugin.util.CharacterUtils;
import info.dong4j.idea.plugin.util.ImageUtils;
import info.dong4j.idea.plugin.util.MarkdownUtils;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.Image;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.image.BufferedImage;
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

                    Map<String, File> imageMap = resolveClipboardData(state, entry, editor.getProject());

                    if (imageMap.size() == 0) {
                        defaultAction(editor, caret, dataContext);
                        return;
                    }

                    // todo-dong4j : (2019年03月25日 12:26) [通知一次]
                    EventData data = new EventData()
                        .setProject(editor.getProject())
                        .setEditor(editor)
                        .setImageMap(imageMap)
                        .setInsertType(InsertEnum.DOCUMENT);

                    ActionManager manager = new ActionManager(data)
                        .addHandler(new ImageCompressHandler())
                        .addHandler(new ImageStorageHandler())
                        .addHandler(new ImageUploadHandler())
                        .addHandler(new ImageLabelChangeHandler())
                        .addHandler(new ImageLabelInsertHandler());

                    new ChainBackgroupTask(editor.getProject(), "Paste Task", manager).queue();

                    return;
                }
            }
        }
        defaultAction(editor, caret, dataContext);
    }

    /**
     * 处理 clipboard 数据
     *
     * @param state   the state
     * @param entry   the entry     List<File> 或者 Image 类型
     * @param project the project
     * @return the map              文件名-->File, File 有本地文件(resolveFromFile)和临时文件(resolveFromImage)
     */
    private Map<String, File> resolveClipboardData(ImageManagerState state,
                                                   @NotNull Map.Entry<DataFlavor, Object> entry,
                                                   Project project) {
        Map<String, File> imageMap = new HashMap<>(10);
        if (entry.getKey().equals(DataFlavor.javaFileListFlavor)) {
            resolveFromFile(state, entry, imageMap);
        } else {
            resolveFromImage(state, entry, project, imageMap);
        }
        return imageMap;
    }

    /**
     * 处理 clipboard 中为 List<File> 类型的数据
     *
     * @param state    the state
     * @param entry    the entry
     * @param imageMap the image map
     */
    private void resolveFromFile(@NotNull ImageManagerState state,
                                 @NotNull Map.Entry<DataFlavor, Object> entry,
                                 Map<String, File> imageMap) {
        @SuppressWarnings("unchecked") List<File> fileList = (List<File>) entry.getValue();
        for (File file : fileList) {
            // 第一步先初步排除非图片类型, 避免复制大量文件导致 OOM
            if (StringUtils.isBlank(ImageUtils.getImageType(file.getName()))) {
                break;
            }
            // 创建临时文件, 用于保存压缩后的图片
            File temp = ImageUtils.buildTempFile(file.getName());
            try {
                // todo-dong4j : (2019年03月26日 12:02) [gif 不压缩, 需要特殊处理]
                if (file.isFile() && !file.getName().endsWith("gif") && state.isCompress()) {
                    ImageUtils.compress(file, temp, state.getCompressBeforeUploadOfPercent());
                } else {
                    FileUtils.copyFile(file, temp);
                }
                // 读到缓冲区, 如果抛异常, 则不是图片
                ImageIO.read(temp);
            } catch (IOException ignored) {
                // 如果抛异常, 则不是图片, 清除所有数据, 使用默认处理程序处理 clipboard 数据
                imageMap.clear();
                break;
            }
            imageMap.put(file.getName(), temp);
        }
    }

    /**
     * 处理 clipboard 中为 Image 类型的数据
     *
     * @param state    the state
     * @param entry    the entry
     * @param project  the project
     * @param imageMap the image map
     */
    private void resolveFromImage(@NotNull ImageManagerState state,
                                  @NotNull Map.Entry<DataFlavor, Object> entry,
                                  Project project,
                                  Map<String, File> imageMap) {
        // image 类型统一重命名, 后缀为 png, 因为获取不到文件名
        String fileName = CharacterUtils.getRandomString(6) + ".png";
        // 如果是 image 类型, 则需要转换成 File
        Image image = (Image) entry.getValue();
        BufferedImage bufferedImage = ImageUtils.toBufferedImage(image);
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        assert bufferedImage != null;
        File temp = ImageUtils.buildTempFile(fileName);
        try {
            ImageIO.write(bufferedImage, "png", os);
            InputStream is = new ByteArrayInputStream(os.toByteArray());
            // 压缩写入临时文件
            if (state.isCompress()) {
                ImageUtils.compress(is, temp, state.getCompressBeforeUploadOfPercent());
            } else {
                FileUtils.copyToFile(is, temp);
            }
        } catch (IOException e) {
            UploadNotification.notifyUploadFailure(new UploadException("文件转换失败"), project);
        }
        imageMap.put(fileName, temp);
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
