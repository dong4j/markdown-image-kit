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

import info.dong4j.idea.plugin.MikBundle;
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
import java.io.FileNotFoundException;
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
 * 处理从剪贴板粘贴图片并上传到 OSS 的操作
 * <p>
 * 该类实现了从剪贴板读取图片数据，根据配置上传到指定的 OSS 服务，并将图片插入到当前光标位置的功能。支持从剪贴板获取图片或文件，并根据设置进行水印处理、压缩、重命名、存储和上传等操作。
 * <p>
 * 该类继承自 EditorActionHandler 并实现了 EditorTextInsertHandler 接口，用于在编辑器中执行图片粘贴和插入操作。
 *
 * @author dong4j
 * @version 0.0.1
 * @date 2019.03.16
 * @since 0.0.1
 */
@Slf4j
public class PasteImageAction extends EditorActionHandler implements EditorTextInsertHandler {
    /** 编辑器操作处理器，用于处理编辑器相关的用户操作事件 */
    private final EditorActionHandler editorActionHandler;

    /**
     * 初始化 PasteImageAction 对象
     * <p>
     * 通过传入的 EditorActionHandler 实例进行初始化
     *
     * @param originalAction 原始的 EditorActionHandler 实例
     * @since 0.0.1
     */
    public PasteImageAction(EditorActionHandler originalAction) {
        this.editorActionHandler = originalAction;
    }

    /**
     * 执行 paste 功能，处理从剪贴板获取的图片或文件
     * <p>
     * 该方法作为 paste 功能的入口，根据当前设置判断是否需要处理剪贴板中的图片或文件。
     * 如果是图片类型，根据配置进行压缩、重命名、上传等操作；如果是文件类型，则获取文件路径并进行处理。
     * 处理完成后，将结果写入文档并刷新文件系统视图。
     *
     * @param editor      编辑器实例
     * @param caret       光标实例，可能为 null
     * @param dataContext 数据上下文
     * @since 0.0.1
     */
    @SuppressWarnings("D")
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

                    Map<Document, List<MarkdownImage>> waitingProcessMap = this.buildWaitingProcessMap(entry, editor, state);

                    if (waitingProcessMap.isEmpty()) {
                        this.defaultAction(editor, caret, dataContext);
                        return;
                    }

                    // 使用默认 client
                    CloudEnum cloudEnum = OssState.getCloudType(state.getCloudType());
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
                    if (state.isCopyToDir()) {
                        // 图片保存
                        manager.addHandler(new ImageStorageHandler());
                    }
                    if (state.isUploadAndReplace()) {
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
                            /**
                             * 处理成功回调逻辑
                             * <p>
                             * 在操作成功时执行回调，记录日志并刷新虚拟文件系统（VFS），确保新增的图片能够及时显示
                             *
                             * @since 1.0
                             */
                            @Override
                            public void onSuccess() {
                                log.trace("Success callback");
                                // 刷新 VFS, 避免新增的图片很久才显示出来
                                ApplicationManager.getApplication().runWriteAction(() -> {
                                    VirtualFileManager.getInstance().syncRefresh();
                                });
                            }
                        });

                    new ActionTask(editor.getProject(), MikBundle.message("mik.action.paste.task"), manager).queue();
                    return;
                }
            }
        }
        this.defaultAction(editor, caret, dataContext);
    }

    /**
     * 构建等待处理的图片映射关系
     * <p>
     * 根据传入的条目、编辑器状态和状态信息，解析剪贴板数据并构建包含Markdown图片信息的映射关系。
     *
     * @param entry  条目信息，用于解析剪贴板数据
     * @param editor 编辑器对象，用于获取文档信息
     * @param state  状态信息，用于控制解析过程
     * @return 包含文档与Markdown图片列表的映射关系
     * @since 0.0.1
     */
    @Contract(pure = true)
    private Map<Document, List<MarkdownImage>> buildWaitingProcessMap(@NotNull Map.Entry<DataFlavor, Object> entry,
                                                                      Editor editor,
                                                                      MikState state) {
        Map<Document, List<MarkdownImage>> waitingProcessMap = new HashMap<>(8);
        List<MarkdownImage> markdownImages = new ArrayList<>(8);
        for (Map.Entry<String, InputStream> inputStreamMap : this.resolveClipboardData(entry, state).entrySet()) {
            final MarkdownImage markdownImage = getMarkdownImage(inputStreamMap);

            markdownImages.add(markdownImage);
        }
        if (!markdownImages.isEmpty()) {
            waitingProcessMap.put(editor.getDocument(), markdownImages);
        }
        return waitingProcessMap;
    }

    /**
     * 创建一个 MarkdownImage 实例并初始化其基本属性
     * <p>
     * 该方法根据传入的输入流映射条目，初始化一个 MarkdownImage 对象，设置其文件名、图片名、扩展名、原始行文本、行号等信息。
     *
     * @param inputStreamMap 输入流映射条目，包含图片的键值对
     * @return 初始化后的 MarkdownImage 实例
     */
    @NotNull
    private static MarkdownImage getMarkdownImage(Map.Entry<String, InputStream> inputStreamMap) {
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
        return markdownImage;
    }

    /**
     * 处理剪贴板数据，根据不同的数据类型填充对应的输入流映射
     * <p>
     * 该方法接收一个剪贴板条目和状态对象，根据条目的数据类型（文件列表或图像）分别调用相应的处理方法，将结果存入输入流映射中返回。
     *
     * @param entry 剪贴板条目，包含数据类型和数据对象
     * @param state 状态对象，用于传递处理过程中需要的上下文信息
     * @return 文件名到输入流的映射，其中文件名对应的是本地文件或临时文件的输入流
     */
    private Map<String, InputStream> resolveClipboardData(@NotNull Map.Entry<DataFlavor, Object> entry,
                                                          MikState state) {
        Map<String, InputStream> imageMap = new HashMap<>(8);
        if (entry.getKey().equals(DataFlavor.javaFileListFlavor)) {
            this.resolveFromFile(entry, imageMap, state);
        } else {
            this.resolveFromImage(entry, imageMap, state);
        }
        return imageMap;
    }

    /**
     * 处理 clipboard 中为 List<File> 类型的数据
     * <p>
     * 该方法用于处理剪贴板中包含的文件列表数据，首先过滤非图片文件，然后将符合条件的图片文件
     * 加载到 imageMap 中，供后续使用。若处理过程中出现异常或不符合条件的文件，将提前终止处理。
     *
     * @param entry    包含数据类型的条目，用于获取文件列表
     * @param imageMap 用于存储图片文件的映射表，键为文件名，值为文件输入流
     * @param state    状态对象，用于控制是否添加水印等操作
     */
    private void resolveFromFile(@NotNull Map.Entry<DataFlavor, Object> entry,
                                 Map<String, InputStream> imageMap, MikState state) {
        @SuppressWarnings("unchecked") List<File> fileList = (List<File>) entry.getValue();
        for (File file : fileList) {
            // 第一步先初步排除非图片类型, 避免复制大量文件导致 OOM
            if (file.isDirectory() || StringUtils.isBlank(ImageUtils.getImageType(file.getName()))) {
                break;
            }

            if (ImageUtils.isImageFile(file)) {
                File finalFile = file;
                if (state.isWatermark()) {
                    finalFile = ImageUtils.watermarkFromText(file, state.getWatermarkText());
                }
                try {
                    imageMap.put(file.getName(), new FileInputStream(finalFile));
                } catch (FileNotFoundException e) {
                    break;
                }
            } else {
                imageMap.clear();
                break;
            }
        }
    }

    /**
     * 处理剪贴板中为图像类型的数据
     * <p>
     * 该方法用于处理剪贴板中类型为Image的数据，将其转换为InputStream并存入imageMap中。
     * 若启用了水印功能，则使用文本生成水印图片；否则将图像转换为PNG格式的输入流。
     *
     * @param entry    剪贴板条目，包含数据类型和对应的数据对象
     * @param imageMap 存储图像文件名与输入流的映射表
     * @param state    状态对象，用于控制是否启用水印功能及水印文本
     */
    private void resolveFromImage(@NotNull Map.Entry<DataFlavor, Object> entry,
                                  Map<String, InputStream> imageMap, MikState state) {
        // image 类型统一重命名, 后缀为 png, 因为获取不到文件名
        String fileName = CharacterUtils.getRandomString(6) + ".png";
        // 如果是 image 类型, 转换成 inputstream
        Image image = (Image) entry.getValue();

        InputStream is = null;

        if (state.isWatermark()) {
            File watermarkFile = ImageUtils.watermarkFromText(image, fileName, state.getWatermarkText());
            try {
                is = new FileInputStream(watermarkFile);
            } catch (FileNotFoundException ignored) {
            }
        } else {
            BufferedImage bufferedImage = ImageUtils.toBufferedImage(image);
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            assert bufferedImage != null;
            try {
                ImageIO.write(bufferedImage, "png", os);
                is = new ByteArrayInputStream(os.toByteArray());
            } catch (IOException ignored) {
            }
        }

        if (is != null) {
            imageMap.put(fileName, is);
        }
    }

    /**
     * 执行默认的粘贴操作，如果是文件则应粘贴文件名
     * <p>
     * 该方法用于处理粘贴操作，若存在已配置的编辑器操作处理器，则调用其执行方法
     *
     * @param editor      编辑器实例
     * @param caret       光标位置信息
     * @param dataContext 数据上下文，包含操作所需的数据
     * @since 0.0.1
     */
    private void defaultAction(@NotNull Editor editor, @Nullable Caret caret, DataContext dataContext) {
        // 执行默认的 paste 操作
        if (this.editorActionHandler != null) {
            this.editorActionHandler.execute(editor, caret, dataContext);
        }
    }

    /**
     * 创建虚拟文件
     * <p>
     * 根据指定的编辑器和图像文件路径创建虚拟文件，并根据版本控制系统（VCS）状态决定是否将其标记为待添加文件。
     *
     * @param ed        编辑器实例
     * @param imageFile 图像文件对象
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
     * 执行指定操作
     * <p>
     * 该方法用于执行特定的操作，接收编辑器、数据上下文和生产者作为参数，用于处理数据传输相关逻辑。
     *
     * @param editor      编辑器对象，用于操作界面元素
     * @param dataContext 数据上下文，包含当前操作所需的数据和状态
     * @param producer    生产者，用于生成可传输的数据对象，可为 null
     * @since 0.0.1
     */
    @Override
    public void execute(Editor editor, DataContext dataContext, @Nullable Producer<? extends Transferable> producer) {

    }
}
