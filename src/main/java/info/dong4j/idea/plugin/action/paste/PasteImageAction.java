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

package info.dong4j.idea.plugin.action.paste;

import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Caret;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.actionSystem.EditorActionHandler;
import com.intellij.openapi.editor.actionSystem.EditorTextInsertHandler;
import com.intellij.openapi.externalSystem.task.TaskCallbackAdapter;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.vcs.AbstractVcs;
import com.intellij.openapi.vcs.ProjectLevelVcsManager;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.util.Producer;

import info.dong4j.idea.plugin.chain.ActionManager;
import info.dong4j.idea.plugin.chain.FinalChainHandler;
import info.dong4j.idea.plugin.chain.ImageCompressionHandler;
import info.dong4j.idea.plugin.chain.ImageLabelChangeHandler;
import info.dong4j.idea.plugin.chain.ImageRenameHandler;
import info.dong4j.idea.plugin.chain.ImageStorageHandler;
import info.dong4j.idea.plugin.chain.ImageUploadHandler;
import info.dong4j.idea.plugin.chain.InsertToDocumentHandler;
import info.dong4j.idea.plugin.chain.OptionClientHandler;
import info.dong4j.idea.plugin.client.OssClient;
import info.dong4j.idea.plugin.entity.EventData;
import info.dong4j.idea.plugin.entity.MarkdownImage;
import info.dong4j.idea.plugin.enums.CloudEnum;
import info.dong4j.idea.plugin.enums.ImageLocationEnum;
import info.dong4j.idea.plugin.enums.ImageMarkEnum;
import info.dong4j.idea.plugin.settings.MikPersistenComponent;
import info.dong4j.idea.plugin.settings.MikState;
import info.dong4j.idea.plugin.settings.OssState;
import info.dong4j.idea.plugin.task.ActionTask;
import info.dong4j.idea.plugin.util.CharacterUtils;
import info.dong4j.idea.plugin.util.ClientUtils;
import info.dong4j.idea.plugin.util.ImageUtils;
import info.dong4j.idea.plugin.util.MarkdownUtils;
import info.dong4j.idea.plugin.util.StringUtils;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.Image;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.imageio.ImageIO;

import lombok.extern.slf4j.Slf4j;

/**
 * <p>Company: no company</p>
 * <p>Description: 读取 Clipboard 图片, 上传到 OSS, 最后插入到光标位置</p>
 * todo-dong4j : (2019年03月17日 19:37) [图片压缩处理]
 *
 * @author dong4j
 * @version 0.0.1
 * @email "mailto:dong4j@gmail.com"
 * @date 2019.03.16 12:15
 * @since 0.0.1
 */
@Slf4j
public class PasteImageAction extends EditorActionHandler implements EditorTextInsertHandler {
    /** Editor action handler */
    private final EditorActionHandler editorActionHandler;
    /** STATE */
    private static final MikState STATE = MikPersistenComponent.getInstance().getState();

    /**
     * Instantiates a new Paste image handler.
     *
     * @param originalAction the original action
     * @since 0.0.1
     */
    public PasteImageAction(EditorActionHandler originalAction) {
        this.editorActionHandler = originalAction;
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
     * @since 0.0.1
     */
    @Override
    protected void doExecute(@NotNull Editor editor, @Nullable Caret caret, DataContext dataContext) {

        Document document = editor.getDocument();
        VirtualFile virtualFile = FileDocumentManager.getInstance().getFile(document);

        if (virtualFile != null && MarkdownUtils.isMardownFile(virtualFile)) {
            MikState state = MikPersistenComponent.getInstance().getState();

            if (state.isUploadAndReplace() || state.isCopyToDir()) {
                Map<DataFlavor, Object> clipboardData = ImageUtils.getDataFromClipboard();
                if (clipboardData != null) {
                    Iterator<Map.Entry<DataFlavor, Object>> iterator = clipboardData.entrySet().iterator();
                    Map.Entry<DataFlavor, Object> entry = iterator.next();

                    Map<Document, List<MarkdownImage>> waitingProcessMap = this.buildWaitingProcessMap(entry, editor);

                    if (waitingProcessMap.size() == 0) {
                        this.defaultAction(editor, caret, dataContext);
                        return;
                    }

                    // 使用默认 client
                    CloudEnum cloudEnum = OssState.getCloudType(STATE.getCloudType());
                    OssClient client = ClientUtils.getClient(cloudEnum);

                    EventData data = new EventData()
                        .setProject(editor.getProject())
                        .setEditor(editor)
                        .setClient(client)
                        .setClientName(cloudEnum.title)
                        .setWaitingProcessMap(waitingProcessMap);

                    ActionManager manager = new ActionManager(data)
                        // 图片压缩
                        .addHandler(new ImageCompressionHandler())
                        // 图片重命名
                        .addHandler(new ImageRenameHandler());
                    if (STATE.isCopyToDir()) {
                        // 图片保存
                        manager.addHandler(new ImageStorageHandler());
                    }
                    if (STATE.isUploadAndReplace()) {
                        // 处理 client
                        manager.addHandler(new OptionClientHandler())
                            .addHandler(new ImageUploadHandler());
                    }

                    // 标签转换
                    manager.addHandler(new ImageLabelChangeHandler())
                        // 写入标签
                        .addHandler(new InsertToDocumentHandler())
                        .addHandler(new FinalChainHandler())
                        .addCallback(new TaskCallbackAdapter() {
                            @Override
                            public void onSuccess() {
                                log.trace("Success callback");
                                // 刷新 VFS, 避免新增的图片很久才显示出来
                                ApplicationManager.getApplication().runWriteAction(() -> {
                                    VirtualFileManager.getInstance().syncRefresh();
                                });
                            }
                        });

                    new ActionTask(editor.getProject(), "Paste Task: ", manager).queue();
                    return;
                }
            }
        }
        this.defaultAction(editor, caret, dataContext);
    }

    /**
     * Build waiting process map
     *
     * @param entry  entry
     * @param editor editor
     * @return the map
     * @since 0.0.1
     */
    @Contract(pure = true)
    private Map<Document, List<MarkdownImage>> buildWaitingProcessMap(@NotNull Map.Entry<DataFlavor, Object> entry,
                                                                      Editor editor) {
        Map<Document, List<MarkdownImage>> waitingProcessMap = new HashMap<>(16);
        List<MarkdownImage> markdownImages = new ArrayList<>(16);
        for (Map.Entry<String, InputStream> inputStreamMap : this.resolveClipboardData(entry).entrySet()) {
            MarkdownImage markdownImage = new MarkdownImage();
            markdownImage.setFileName("");
            markdownImage.setImageName(inputStreamMap.getKey());
            markdownImage.setExtension("");
            markdownImage.setOriginalLineText("");
            markdownImage.setLineNumber(0);
            markdownImage.setLineStartOffset(0);
            markdownImage.setLineEndOffset(0);
            markdownImage.setTitle("");
            markdownImage.setPath("");
            markdownImage.setLocation(ImageLocationEnum.LOCAL);
            markdownImage.setImageMarkType(ImageMarkEnum.ORIGINAL);
            markdownImage.setInputStream(inputStreamMap.getValue());
            markdownImage.setFinalMark("");

            markdownImages.add(markdownImage);
        }
        if(markdownImages.size() > 0){
            waitingProcessMap.put(editor.getDocument(), markdownImages);
        }
        return waitingProcessMap;
    }

    /**
     * 处理 clipboard 数据
     *
     * @param entry the entry     List<File> 或者 Image 类型
     * @return the map              文件名-->File, File 有本地文件(resolveFromFile)和临时文件(resolveFromImage)
     * @since 0.0.1
     */
    private Map<String, InputStream> resolveClipboardData(@NotNull Map.Entry<DataFlavor, Object> entry) {
        Map<String, InputStream> imageMap = new HashMap<>(10);
        if (entry.getKey().equals(DataFlavor.javaFileListFlavor)) {
            this.resolveFromFile(entry, imageMap);
        } else {
            this.resolveFromImage(entry, imageMap);
        }
        return imageMap;
    }

    /**
     * 处理 clipboard 中为 List<File> 类型的数据
     *
     * @param entry    the entry
     * @param imageMap the image map
     * @since 0.0.1
     */
    private void resolveFromFile(@NotNull Map.Entry<DataFlavor, Object> entry,
                                 Map<String, InputStream> imageMap) {
        @SuppressWarnings("unchecked") List<File> fileList = (List<File>) entry.getValue();
        for (File file : fileList) {
            // 第一步先初步排除非图片类型, 避免复制大量文件导致 OOM
            if (file.isDirectory() || StringUtils.isBlank(ImageUtils.getImageType(file.getName()))) {
                break;
            }
            try {
                // 读到缓冲区, 如果抛异常, 则不是图片
                ImageIO.read(file);
                imageMap.put(file.getName(), new FileInputStream(file));
            } catch (IOException ignored) {
                // 如果抛异常, 则不是图片, 清除所有数据, 使用默认处理程序处理 clipboard 数据
                imageMap.clear();
                break;
            }
        }
    }

    /**
     * 处理 clipboard 中为 Image 类型的数据
     *
     * @param entry    the entry
     * @param imageMap the image map
     * @since 0.0.1
     */
    private void resolveFromImage(@NotNull Map.Entry<DataFlavor, Object> entry,
                                  Map<String, InputStream> imageMap) {
        // image 类型统一重命名, 后缀为 png, 因为获取不到文件名
        String fileName = CharacterUtils.getRandomString(6) + ".png";
        // 如果是 image 类型, 转换成 inputstream
        Image image = (Image) entry.getValue();
        BufferedImage bufferedImage = ImageUtils.toBufferedImage(image);
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        assert bufferedImage != null;
        try {
            ImageIO.write(bufferedImage, "png", os);
            InputStream is = new ByteArrayInputStream(os.toByteArray());
            imageMap.put(fileName, is);
        } catch (IOException ignored) {
        }
    }

    /**
     * 默认 paste 操作, 如果是文件的话应该粘贴文件名
     *
     * @param editor      the editor
     * @param caret       the caret
     * @param dataContext the data context
     * @since 0.0.1
     */
    private void defaultAction(@NotNull Editor editor, @Nullable Caret caret, DataContext dataContext) {
        // 执行默认的 paste 操作
        if (this.editorActionHandler != null) {
            this.editorActionHandler.execute(editor, caret, dataContext);
        }
    }

    /**
     * Create virtual file.
     * https://intellij-support.jetbrains.com/hc/en-us/community/posts/206144389-Create-virtual-file-from-file-path
     *
     * @param ed        the ed
     * @param imageFile the image file
     * @since 0.0.1
     */
    private void createVirtualFile(Editor ed, File imageFile) {
        VirtualFile fileByPath = LocalFileSystem.getInstance().refreshAndFindFileByIoFile(imageFile);
        assert fileByPath != null;
        AbstractVcs usedVcs = ProjectLevelVcsManager.getInstance(Objects.requireNonNull(ed.getProject())).getVcsFor(fileByPath);
        if (usedVcs != null && usedVcs.getCheckinEnvironment() != null) {
            usedVcs.getCheckinEnvironment().scheduleUnversionedFilesForAddition(Collections.singletonList(fileByPath));
        }
    }

    /**
     * Execute
     *
     * @param editor      editor
     * @param dataContext data context
     * @param producer    producer
     * @since 0.0.1
     */
    @Override
    public void execute(Editor editor, DataContext dataContext, @Nullable Producer<? extends Transferable> producer) {

    }
}
