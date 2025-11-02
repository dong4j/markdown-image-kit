package info.dong4j.idea.plugin.action.paste;

import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.editor.Caret;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.actionSystem.EditorActionHandler;
import com.intellij.openapi.editor.actionSystem.EditorTextInsertHandler;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vcs.AbstractVcs;
import com.intellij.openapi.vcs.ProjectLevelVcsManager;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.Producer;

import info.dong4j.idea.plugin.MikBundle;
import info.dong4j.idea.plugin.chain.ActionManager;
import info.dong4j.idea.plugin.chain.DownloadImageHandler;
import info.dong4j.idea.plugin.chain.FinalChainHandler;
import info.dong4j.idea.plugin.chain.ImageCompressionHandler;
import info.dong4j.idea.plugin.chain.ImageLabelChangeHandler;
import info.dong4j.idea.plugin.chain.ImageRenameHandler;
import info.dong4j.idea.plugin.chain.ImageStorageHandler;
import info.dong4j.idea.plugin.chain.ImageUploadHandler;
import info.dong4j.idea.plugin.chain.InsertToDocumentHandler;
import info.dong4j.idea.plugin.chain.OptionClientHandler;
import info.dong4j.idea.plugin.chain.RefreshFileSystemHandler;
import info.dong4j.idea.plugin.client.OssClient;
import info.dong4j.idea.plugin.content.ImageContents;
import info.dong4j.idea.plugin.entity.EventData;
import info.dong4j.idea.plugin.entity.MarkdownImage;
import info.dong4j.idea.plugin.enums.CloudEnum;
import info.dong4j.idea.plugin.enums.ImageLocationEnum;
import info.dong4j.idea.plugin.enums.ImageMarkEnum;
import info.dong4j.idea.plugin.enums.InsertImageActionEnum;
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
 * 复制一张图片(剪切板第一个是图片), 然后在 markdown 文档中执行复制操作, 会触发 {@link PasteImageAction#doExecute(Editor, Caret, DataContext)} 逻辑
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
@SuppressWarnings("D")
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
        MikState state = MikPersistenComponent.getInstance().getState();
        InsertImageActionEnum insertImageAction = state.getInsertImageAction();

        // 如果 caret 为 null，尝试获取当前的 caret
        Caret currentCaret = caret;
        if (currentCaret == null) {
            currentCaret = editor.getCaretModel().getCurrentCaret();
        }

        if (virtualFile != null
            && MarkdownUtils.isMardownFile(virtualFile)
            && insertImageAction != null
            && insertImageAction != InsertImageActionEnum.NONE) {
            try {
                Map<DataFlavor, Object> clipboardData = ImageUtils.getDataFromClipboard();
                if (clipboardData != null) {
                    Iterator<Map.Entry<DataFlavor, Object>> iterator = clipboardData.entrySet().iterator();
                    Map.Entry<DataFlavor, Object> entry = iterator.next();

                    Map<Document, List<MarkdownImage>> waitingProcessMap = this.buildWaitingProcessMap(entry, editor, virtualFile, state);

                    if (waitingProcessMap.isEmpty()) {
                        this.defaultAction(editor, caret, dataContext);
                        return;
                    }

                    // 检查是否包含网络图片
                    boolean hasNetworkImage = this.hasNetworkImage(waitingProcessMap);
                    final boolean needStoraged = prepareImageStoragePath(editor, insertImageAction, state, virtualFile);

                    final ActionManager manager = createManager(editor, state, waitingProcessMap);

                    // 如果包含网络图片，先添加下载处理器
                    manager.addHandler(hasNetworkImage, new DownloadImageHandler())
                        // 图片压缩
                        .addHandler(new ImageCompressionHandler())
                        // 图片重命名
                        .addHandler(new ImageRenameHandler())
                        .addHandler(needStoraged, new ImageStorageHandler())
                        // 刷新文件系统
                        .addHandler(needStoraged, new RefreshFileSystemHandler())
                        .addHandler(insertImageAction == InsertImageActionEnum.UPLOAD, new OptionClientHandler())
                        .addHandler(insertImageAction == InsertImageActionEnum.UPLOAD, new ImageUploadHandler())
                        .addHandler(new ImageLabelChangeHandler())
                        // 写入标签
                        .addHandler(new InsertToDocumentHandler())
                        .addHandler(new FinalChainHandler())
                        .addHandler(new RefreshFileSystemHandler());

                    new ActionTask(editor.getProject(), MikBundle.message("mik.action.paste.task"), manager).queue();
                    return;
                }
            } catch (Exception ignored) {
            }
        }
        // 兜底, 回退到默认逻辑
        this.defaultAction(editor, caret, dataContext);
    }

    private static ActionManager createManager(@NotNull Editor editor,
                                               MikState state,
                                               Map<Document, List<MarkdownImage>> waitingProcessMap) {
        // 使用默认 client
        CloudEnum cloudEnum = OssState.getCloudType(state.getDefaultCloudType());
        OssClient client = ClientUtils.getClient(cloudEnum);

        EventData data = new EventData()
            .setProject(editor.getProject())
            .setEditor(editor)
            .setClient(client)
            .setClientName(cloudEnum.title)
            .setWaitingProcessMap(waitingProcessMap);

        return new ActionManager(data);
    }

    /**
     * 根据插入图片的操作类型处理图片的保存路径，并设置到状态对象中。
     * <p>
     * 如果操作类型为复制到指定路径，则获取当前路径，处理占位符，并设置保存路径。
     *
     * @param editor            编辑器实例
     * @param insertImageAction 插入图片的操作类型
     * @param state             状态对象，用于存储和获取图片保存路径等信息
     * @param virtualFile       虚拟文件对象
     * @return 处理成功返回 true，否则返回 false
     */
    private boolean prepareImageStoragePath(@NotNull Editor editor,
                                            InsertImageActionEnum insertImageAction,
                                            MikState state,
                                            VirtualFile virtualFile) {
        // 处理复制到指定路径的逻辑（包括4个复制选项）
        if (insertImageAction == InsertImageActionEnum.COPY_TO_CURRENT
            || insertImageAction == InsertImageActionEnum.COPY_TO_ASSETS
            || insertImageAction == InsertImageActionEnum.COPY_TO_FILENAME_ASSETS
            || insertImageAction == InsertImageActionEnum.COPY_TO_CUSTOM) {

            // 获取当前路径，如果为空则根据枚举类型设置默认路径
            String currentPath = state.getCurrentInsertPath();
            if (currentPath == null || currentPath.isEmpty()) {
                String customPath = insertImageAction == InsertImageActionEnum.COPY_TO_CUSTOM
                                    ? state.getSavedCustomInsertPath()
                                    : null;
                currentPath = InsertImageActionEnum.getPathByAction(insertImageAction, customPath);
            }

            // 处理占位符并设置保存路径
            String processedPath = this.processPathPlaceholders(
                currentPath,
                virtualFile,
                editor.getProject()
                                                               );
            // 设置处理后的路径, 在 ImageStorageHandler 会使用到
            state.setImageSavePath(processedPath);
            return true;
        }

        return false;
    }

    /**
     * 构建等待处理的图片映射关系
     * <p>
     * 根据传入的条目、编辑器状态和状态信息，解析剪贴板数据并构建包含Markdown图片信息的映射关系。
     *
     * @param entry       条目信息，用于解析剪贴板数据
     * @param editor      编辑器对象，用于获取文档信息
     * @param virtualFile 当前编辑的虚拟文件
     * @param state       状态信息，用于控制解析过程
     * @return 包含文档与Markdown图片列表的映射关系
     * @since 0.0.1
     */
    @Contract(pure = true)
    private Map<Document, List<MarkdownImage>> buildWaitingProcessMap(@NotNull Map.Entry<DataFlavor, Object> entry,
                                                                      Editor editor,
                                                                      @Nullable VirtualFile virtualFile,
                                                                      MikState state) {
        // 如果 caret 为 null，尝试获取当前的 caret
        Caret caret = null;
        try {
            caret = editor.getCaretModel().getCurrentCaret();
        } catch (Exception e) {
            log.trace("无法获取当前 caret", e);
        }

        // 获取当前文档名
        String filename = virtualFile != null ? virtualFile.getName() : "";

        Map<Document, List<MarkdownImage>> waitingProcessMap = new HashMap<>(8);
        List<MarkdownImage> markdownImages = new ArrayList<>(8);
        for (Map.Entry<String, InputStream> inputStreamMap : this.resolveClipboardData(entry, editor, caret, state).entrySet()) {
            final MarkdownImage markdownImage = getMarkdownImage(inputStreamMap, filename);

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
     * @param inputStreamMap 输入流映射条目，包含图片的键值对（key可能是文件名或包含元数据的字符串）
     * @param filename       当前 markdown 文档的文件名
     * @return 初始化后的 MarkdownImage 实例
     */
    @NotNull
    private static MarkdownImage getMarkdownImage(Map.Entry<String, InputStream> inputStreamMap, String filename) {
        String key = inputStreamMap.getKey();
        MarkdownImage markdownImage = new MarkdownImage();

        // 检查是否是网络图片（key以"network:"开头表示是网络图片）
        if (key.startsWith("network:")) {
            // 解析网络图片的元数据
            // 格式：network:originalLineText|lineNumber|pathStartOffset|pathEndOffset|imageUrl|originalMark
            String[] parts = key.substring(8).split("\\|", -1);

            markdownImage.setFilename(filename);
            markdownImage.setImageName(""); // 图片名称由 DownloadImageHandler 解析
            markdownImage.setExtension(""); // 扩展名由 DownloadImageHandler 解析
            markdownImage.setOriginalLineText(parts.length > 0 ? parts[0] : "");
            markdownImage.setLineNumber(parts.length > 1 && !parts[1].isEmpty() ? Integer.parseInt(parts[1]) : 0);
            markdownImage.setLineStartOffset(parts.length > 2 && !parts[2].isEmpty() ? Integer.parseInt(parts[2]) : 0);
            markdownImage.setLineEndOffset(parts.length > 3 && !parts[3].isEmpty() ? Integer.parseInt(parts[3]) : 0);
            markdownImage.setTitle("");
            markdownImage.setPath(parts.length > 4 ? parts[4] : ""); // URL
            markdownImage.setLocation(ImageLocationEnum.NETWORK);
            markdownImage.setImageMarkType(ImageMarkEnum.ORIGINAL);
            markdownImage.setOriginalMark(parts.length > 5 ? parts[5] : "");
            markdownImage.setFinalMark("");
            markdownImage.setImageStream(true); // 网络图片视为图片流
        } else if (key.startsWith("file:")) {
            // 粘贴的文件
            // 格式：file:原始文件绝对路径|文件名
            String[] parts = key.substring(5).split("\\|", 2);
            String sourceFilePath = parts.length > 0 ? parts[0] : "";
            String imageName = parts.length > 1 ? parts[1] : "";

            markdownImage.setFilename(filename);
            markdownImage.setImageName(imageName);
            markdownImage.setExtension("");
            markdownImage.setOriginalLineText("");
            markdownImage.setLineNumber(0);
            markdownImage.setLineStartOffset(0);
            markdownImage.setLineEndOffset(0);
            markdownImage.setTitle("");
            markdownImage.setPath("");
            markdownImage.setLocation(ImageLocationEnum.LOCAL);
            markdownImage.setImageMarkType(ImageMarkEnum.ORIGINAL);
            markdownImage.setFinalMark("");
            markdownImage.setSourceFilePath(sourceFilePath);
            markdownImage.setImageStream(false); // 标记为文件
        } else {
            // 图片流（从剪贴板直接粘贴的图片）
            markdownImage.setFilename(filename);
            markdownImage.setImageName(key);
            markdownImage.setExtension("");
            markdownImage.setOriginalLineText("");
            markdownImage.setLineNumber(0);
            markdownImage.setLineStartOffset(0);
            markdownImage.setLineEndOffset(0);
            markdownImage.setTitle("");
            markdownImage.setPath("");
            markdownImage.setLocation(ImageLocationEnum.LOCAL);
            markdownImage.setImageMarkType(ImageMarkEnum.ORIGINAL);
            markdownImage.setFinalMark("");
            markdownImage.setImageStream(true); // 标记为图片流
        }

        markdownImage.setInputStream(inputStreamMap.getValue());
        return markdownImage;
    }

    /**
     * 处理剪贴板数据，根据不同的数据类型填充对应的输入流映射
     * <p>
     * 该方法接收一个剪贴板条目和状态对象，根据条目的数据类型（文件列表、图像或网络URL）分别调用相应的处理方法，将结果存入输入流映射中返回。
     *
     * @param entry  剪贴板条目，包含数据类型和数据对象
     * @param editor 编辑器对象
     * @param caret  光标对象
     * @param state  状态对象，用于传递处理过程中需要的上下文信息
     * @return 文件名到输入流的映射，其中文件名对应的是本地文件或临时文件的输入流
     */
    private Map<String, InputStream> resolveClipboardData(@NotNull Map.Entry<DataFlavor, Object> entry,
                                                          Editor editor,
                                                          Caret caret,
                                                          MikState state) {
        Map<String, InputStream> imageMap = new HashMap<>(8);

        // 检查是否是网络图片URL
        if (state.isApplyToNetworkImages() && entry.getKey().equals(DataFlavor.stringFlavor)) {
            String text = (String) entry.getValue();
            if (text != null && (text.trim().startsWith("http://") || text.trim().startsWith("https://"))) {
                // 检查光标是否在图片路径中 ![](光标必须在这里, 复制才能生效)
                if (this.isCaretInImagePath(editor, caret)) {
                    this.resolveFromNetworkUrl(text.trim(), editor, caret, imageMap);
                    if (!imageMap.isEmpty()) {
                        return imageMap;
                    }
                }
            }
        }

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
     * <p>
     * key 格式：file:原始文件绝对路径|文件名
     * 这样可以在后续处理中区分是文件还是图片流，并保留原始文件路径信息
     *
     * @param entry    包含数据类型的条目，用于获取文件列表
     * @param imageMap 用于存储图片文件的映射表，键为文件元数据，值为文件输入流
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
                    // 构建元数据 key，格式：file:原始文件绝对路径|文件名
                    String metadataKey = String.format("file:%s|%s", file.getAbsolutePath(), file.getName());
                    imageMap.put(metadataKey, new FileInputStream(finalFile));
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
        String filename = CharacterUtils.getRandomString(6) + ".png";
        // 如果是 image 类型, 转换成 inputstream
        Image image = (Image) entry.getValue();

        InputStream is = null;

        if (state.isWatermark()) {
            File watermarkFile = ImageUtils.watermarkFromText(image, filename, state.getWatermarkText());
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
            imageMap.put(filename, is);
        }
    }

    /**
     * 处理网络图片 URL
     * <p>
     * 该方法用于处理剪贴板中的网络图片 URL，解析光标所在的图片标签信息，并构建相应的元数据。
     * 元数据将以特殊格式存储在 imageMap 的 key 中。图片名称和扩展名的解析由 DownloadImageHandler 处理。
     *
     * @param imageUrl 网络图片 URL
     * @param editor   编辑器对象
     * @param caret    光标对象
     * @param imageMap 存储图像文件名与输入流的映射表
     */
    private void resolveFromNetworkUrl(@NotNull String imageUrl,
                                       Editor editor,
                                       Caret caret,
                                       Map<String, InputStream> imageMap) {
        Document doc = editor.getDocument();
        int caretOffset = caret.getOffset();
        int lineNumber = doc.getLineNumber(caretOffset);
        int lineStartOffset = doc.getLineStartOffset(lineNumber);
        int lineEndOffset = doc.getLineEndOffset(lineNumber);
        String originalLineText = doc.getText(new TextRange(lineStartOffset, lineEndOffset));

        // 解析图片标签信息
        int prefixIndex = originalLineText.indexOf(ImageContents.IMAGE_MARK_PREFIX);
        if (prefixIndex == -1) {
            return;
        }

        int middleIndex = originalLineText.indexOf(ImageContents.IMAGE_MARK_MIDDLE, prefixIndex);
        if (middleIndex == -1) {
            return;
        }

        int suffixIndex = originalLineText.indexOf(ImageContents.IMAGE_MARK_SUFFIX, middleIndex);
        if (suffixIndex == -1) {
            return;
        }

        // 计算路径部分的偏移量（相对于文档）
        int pathStartOffset = lineStartOffset + middleIndex + ImageContents.IMAGE_MARK_MIDDLE.length();
        int pathEndOffset = lineStartOffset + suffixIndex;
        String originalMark = originalLineText.substring(prefixIndex, suffixIndex + 1);

        // 构建元数据 key
        // 格式：network:originalLineText|lineNumber|pathStartOffset|pathEndOffset|imageUrl|originalMark
        // 图片名称和扩展名将在 DownloadImageHandler 中根据 URL 和响应头解析
        String metadataKey = String.format("network:%s|%d|%d|%d|%s|%s",
                                           originalLineText,
                                           lineNumber,
                                           pathStartOffset,
                                           pathEndOffset,
                                           imageUrl,
                                           originalMark);

        // 使用空的 ByteArrayInputStream 作为占位符，实际下载会在 DownloadImageHandler 中进行
        imageMap.put(metadataKey, new ByteArrayInputStream(new byte[0]));
    }

    /**
     * 检查 waitingProcessMap 中是否包含网络图片
     *
     * @param waitingProcessMap 待处理的图片映射
     * @return 如果包含网络图片返回 true，否则返回 false
     */
    private boolean hasNetworkImage(Map<Document, List<MarkdownImage>> waitingProcessMap) {
        for (List<MarkdownImage> images : waitingProcessMap.values()) {
            for (MarkdownImage image : images) {
                if (image.getLocation() == ImageLocationEnum.NETWORK) {
                    return true;
                }
            }
        }
        return false;
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
     * 处理路径中的占位符
     * <p>
     * 替换路径中的占位符：
     * - ${filename}: 当前文件名（不含扩展名）
     * - ${project}: 当前项目路径
     * <p>
     * 处理跨平台路径差异：
     * - Windows 使用反斜杠 `\`，Unix/Linux/macOS 使用正斜杠 `/`
     * - 对于相对路径（以 `./` 或 `../` 开头），统一使用正斜杠 `/`（Markdown 标准格式）
     * - 对于绝对路径，保持系统原生格式，但 File 类也能接受正斜杠
     * - 最终路径统一规范化为正斜杠，确保跨平台兼容性
     *
     * @param path        原始路径
     * @param virtualFile 当前文件
     * @param project     项目对象
     * @return 处理后的路径
     */
    @NotNull
    private String processPathPlaceholders(@NotNull String path, @NotNull VirtualFile virtualFile, @Nullable Project project) {
        if (path.isEmpty()) {
            return path;
        }

        String result = path;

        // 替换 ${filename} 为当前文件名（不含扩展名）
        String filename = virtualFile.getNameWithoutExtension();
        result = result.replace("${filename}", filename);

        // 替换 ${project} 为项目根路径
        if (project != null && project.getBasePath() != null) {
            String projectPath = project.getBasePath();
            result = result.replace("${project}", projectPath);
        } else {
            // 如果项目路径不可用，移除 ${project} 占位符
            result = result.replace("${project}", "");
        }

        // 规范化路径分隔符，统一使用正斜杠（跨平台兼容）
        // File 类可以接受正斜杠，即使在 Windows 上也能正确处理
        // 同时确保相对路径和绝对路径都能正确工作
        result = normalizePathSeparator(result);

        return result;
    }

    /**
     * 规范化路径分隔符
     * <p>
     * 将路径中的反斜杠统一转换为正斜杠，确保跨平台兼容性。
     * Java 的 File 类可以接受正斜杠，即使在 Windows 上也能正确处理。
     * 这对于相对路径和绝对路径都适用。
     *
     * @param path 原始路径
     * @return 规范化后的路径（统一使用正斜杠）
     */
    @NotNull
    private static String normalizePathSeparator(@NotNull String path) {
        // 统一将反斜杠替换为正斜杠
        // 这样无论在哪个操作系统上，都能正确工作
        // File 类会自动处理正斜杠，即使在 Windows 上也是如此
        return path.replace('\\', '/');
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

    /**
     * 检测光标是否在 markdown 图片标签的 path 中
     * <p>
     * 该方法用于检测当前光标位置是否在 markdown 图片标签的路径部分，即 `![xxx](光标在这里)` 的情况。
     * 通过解析光标所在行的文本，查找图片标签，并判断光标是否在路径的括号内。
     * 如果 caret 为 null，会尝试从编辑器获取当前的 caret。
     *
     * @param editor 编辑器实例
     * @param caret  光标位置，可能为 null
     * @return 如果光标在图片路径中返回 true，否则返回 false
     * @since 1.0.0
     */
    private boolean isCaretInImagePath(@NotNull Editor editor, @Nullable Caret caret) {
        // 如果 caret 为 null，尝试获取当前的 caret
        Caret currentCaret = caret;
        if (currentCaret == null) {
            try {
                currentCaret = editor.getCaretModel().getCurrentCaret();
            } catch (Exception e) {
                log.trace("无法获取当前 caret", e);
                return false;
            }
        }

        Document document = editor.getDocument();
        int caretOffset = currentCaret.getOffset();
        int documentLine = document.getLineNumber(caretOffset);
        int lineStartOffset = document.getLineStartOffset(documentLine);
        int lineEndOffset = document.getLineEndOffset(documentLine);
        String lineText = document.getText(new TextRange(lineStartOffset, lineEndOffset));

        // 查找图片标签的位置
        int prefixIndex = lineText.indexOf(ImageContents.IMAGE_MARK_PREFIX);
        if (prefixIndex == -1) {
            return false;
        }

        int middleIndex = lineText.indexOf(ImageContents.IMAGE_MARK_MIDDLE, prefixIndex);
        if (middleIndex == -1) {
            return false;
        }

        int suffixIndex = lineText.indexOf(ImageContents.IMAGE_MARK_SUFFIX, middleIndex);
        if (suffixIndex == -1) {
            return false;
        }

        // 计算路径部分的偏移量（相对于行）
        int pathStartOffset = middleIndex + ImageContents.IMAGE_MARK_MIDDLE.length();

        // 计算光标在行内的相对位置
        int caretOffsetInLine = caretOffset - lineStartOffset;

        // 判断光标是否在路径部分
        return caretOffsetInLine >= pathStartOffset && caretOffsetInLine <= suffixIndex;
    }

}
