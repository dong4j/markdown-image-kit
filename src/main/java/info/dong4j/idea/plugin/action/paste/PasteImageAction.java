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
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
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

        try {

            if (virtualFile != null
                && MarkdownUtils.isMardownFile(virtualFile)
                && insertImageAction != null
                && insertImageAction != InsertImageActionEnum.NONE) {

                // 检测是否是网络图片且光标在图片路径中
                if (state.isApplyToNetworkImages() && this.isCaretInImagePath(editor, currentCaret)) {
                    String imageUrl = this.getNetworkImageUrlFromClipboard();
                    if (imageUrl != null && !imageUrl.isEmpty()) {
                        this.handleNetworkImageDownload(editor, currentCaret, state, imageUrl);
                        return;
                    }
                }

                Map<DataFlavor, Object> clipboardData = ImageUtils.getDataFromClipboard();
                if (clipboardData != null) {
                    Iterator<Map.Entry<DataFlavor, Object>> iterator = clipboardData.entrySet().iterator();
                    Map.Entry<DataFlavor, Object> entry = iterator.next();

                    Map<Document, List<MarkdownImage>> waitingProcessMap = this.buildWaitingProcessMap(entry, editor, state);

                    if (waitingProcessMap.isEmpty()) {
                        this.defaultAction(editor, caret, dataContext);
                        return;
                    }

                    final ActionManager manager = createManager(editor, state, waitingProcessMap);

                    addImageStorageHandler(editor, insertImageAction, state, virtualFile, manager);

                    addPostHanler(insertImageAction, manager);

                    new ActionTask(editor.getProject(), MikBundle.message("mik.action.paste.task"), manager).queue();
                    return;
                }
            }
        } catch (Exception e) {
            // 兜底, 回退到默认逻辑
            this.defaultAction(editor, caret, dataContext);
        }
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

        return new ActionManager(data)
            // 图片压缩
            .addHandler(new ImageCompressionHandler())
            // 图片重命名
            .addHandler(new ImageRenameHandler());
    }

    /**
     * 添加后置处理器
     * <p>
     * 根据插入图片的操作类型，向 ActionManager 添加相应的处理器链。
     * 如果操作类型为 UPLOAD，则添加处理客户端和图片上传的处理器；
     * 否则添加标签转换、写入标签、最终处理和刷新文件系统等处理器。
     *
     * @param insertImageAction 插入图片的操作类型
     * @param manager           ActionManager 实例，用于添加处理器
     */
    private static void addPostHanler(InsertImageActionEnum insertImageAction, ActionManager manager) {
        // 处理上传图片的逻辑
        if (insertImageAction == InsertImageActionEnum.UPLOAD) {
            // 处理 client
            manager.addHandler(new OptionClientHandler())
                .addHandler(new ImageUploadHandler());
        }

        // 标签转换
        manager.addHandler(new ImageLabelChangeHandler())
            // 写入标签
            .addHandler(new InsertToDocumentHandler())
            .addHandler(new FinalChainHandler())
            .addHandler(new RefreshFileSystemHandler());
    }

    /**
     * 添加图片存储处理逻辑
     * <p>
     * 根据插入图片的操作类型，处理图片的保存路径，并设置到状态对象中。如果操作类型为复制到指定路径，则获取当前路径，处理占位符，并设置保存路径。
     *
     * @param editor            编辑器实例
     * @param insertImageAction 插入图片的操作类型
     * @param state             状态对象，用于存储和获取图片保存路径等信息
     * @param virtualFile       虚拟文件对象
     * @param manager           操作管理器，用于添加处理逻辑
     */
    private void addImageStorageHandler(@NotNull Editor editor,
                                        InsertImageActionEnum insertImageAction,
                                        MikState state,
                                        VirtualFile virtualFile,
                                        ActionManager manager) {
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
            // 图片保存
            manager.addHandler(new ImageStorageHandler())
                // 刷新文件系统
                .addHandler(new RefreshFileSystemHandler());
        }
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
        String fileName = virtualFile.getNameWithoutExtension();
        result = result.replace("${filename}", fileName);

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

    /**
     * 从剪贴板获取网络图片 URL
     * <p>
     * 该方法用于从系统剪贴板中获取字符串类型的网络图片 URL。
     *
     * @return 网络图片 URL，如果获取失败或不是 URL 则返回 null
     * @since 1.0.0
     */
    @Nullable
    private String getNetworkImageUrlFromClipboard() {
        try {
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            Transferable transferable = clipboard.getContents(null);
            if (transferable == null || !transferable.isDataFlavorSupported(DataFlavor.stringFlavor)) {
                return null;
            }
            Object data = transferable.getTransferData(DataFlavor.stringFlavor);
            if (data instanceof String text) {
                String trimmed = text.trim();
                if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) {
                    return trimmed;
                }
            }
        } catch (UnsupportedFlavorException | IOException e) {
            log.trace("从剪贴板获取 URL 失败", e);
        }
        return null;
    }

    /**
     * 处理网络图片下载
     * <p>
     * 该方法用于处理网络图片的下载流程，将从剪贴板获取的网络图片 URL 下载到本地，
     * 然后执行后续的图片处理流程（压缩、重命名、存储、上传等）。
     *
     * @param editor   编辑器实例
     * @param caret    光标位置，可能为 null
     * @param state    状态信息
     * @param imageUrl 网络图片 URL
     * @since 1.0.0
     */
    private void handleNetworkImageDownload(@NotNull Editor editor, @Nullable Caret caret, @NotNull MikState state,
                                            @NotNull String imageUrl) {
        Document document = editor.getDocument();
        VirtualFile virtualFile = FileDocumentManager.getInstance().getFile(document);

        // 如果光标在图片路径中，解析现有的图片标签信息
        Caret currentCaret = caret;
        if (currentCaret == null) {
            currentCaret = editor.getCaretModel().getCurrentCaret();
        }

        boolean shouldReplacePath = false;
        int pathStartOffset = -1;
        int pathEndOffset = -1;
        String originalLineText = "";
        String originalMark = "";
        int lineNumber = 0;

        if (this.isCaretInImagePath(editor, currentCaret)) {
            // 解析图片标签信息
            Document doc = editor.getDocument();
            int caretOffset = currentCaret.getOffset();
            lineNumber = doc.getLineNumber(caretOffset);
            int lineStartOffset = doc.getLineStartOffset(lineNumber);
            int lineEndOffset = doc.getLineEndOffset(lineNumber);
            originalLineText = doc.getText(new TextRange(lineStartOffset, lineEndOffset));

            // 查找图片标签的位置
            int prefixIndex = originalLineText.indexOf(ImageContents.IMAGE_MARK_PREFIX);
            if (prefixIndex != -1) {
                int middleIndex = originalLineText.indexOf(ImageContents.IMAGE_MARK_MIDDLE, prefixIndex);
                if (middleIndex != -1) {
                    int suffixIndex = originalLineText.indexOf(ImageContents.IMAGE_MARK_SUFFIX, middleIndex);
                    if (suffixIndex != -1) {
                        // 计算路径部分的偏移量（相对于文档）
                        pathStartOffset = lineStartOffset + middleIndex + ImageContents.IMAGE_MARK_MIDDLE.length();
                        pathEndOffset = lineStartOffset + suffixIndex;
                        originalMark = originalLineText.substring(prefixIndex, suffixIndex + 1);
                        shouldReplacePath = true;
                    }
                }
            }
        }

        // 解析图片名称
        // 移除查询参数（如 ?x-oss-process=...）
        String urlWithoutQuery = imageUrl.split("\\?")[0];
        String imageName = urlWithoutQuery.substring(urlWithoutQuery.lastIndexOf("/") + 1);

        // 如果没有扩展名，使用随机名称（扩展名会在下载时根据 Content-Type 或文件头确定）
        if (!imageName.contains(".") || imageName.endsWith(".")) {
            imageName = CharacterUtils.getRandomString(6);
        }

        // 创建 MarkdownImage 对象
        MarkdownImage markdownImage = new MarkdownImage();
        markdownImage.setFileName(virtualFile != null ? virtualFile.getName() : "");
        markdownImage.setImageName(imageName);
        // 扩展名会在下载时根据 Content-Type 或文件头确定，这里先设置为空
        // 如果 URL 中包含扩展名，则尝试提取
        String extension = "";
        int lastDot = imageName.lastIndexOf('.');
        if (lastDot > 0 && lastDot < imageName.length() - 1) {
            extension = imageName.substring(lastDot + 1);
        }
        markdownImage.setExtension(extension);
        markdownImage.setOriginalLineText(shouldReplacePath ? originalLineText : "");
        markdownImage.setLineNumber(lineNumber);
        markdownImage.setLineStartOffset(shouldReplacePath ? pathStartOffset : 0);
        markdownImage.setLineEndOffset(shouldReplacePath ? pathEndOffset : 0);
        markdownImage.setTitle("");
        markdownImage.setPath(imageUrl);
        markdownImage.setLocation(ImageLocationEnum.NETWORK);
        markdownImage.setImageMarkType(ImageMarkEnum.ORIGINAL);
        markdownImage.setOriginalMark(shouldReplacePath ? originalMark : "");
        markdownImage.setFinalMark("");

        Map<Document, List<MarkdownImage>> waitingProcessMap = new HashMap<>(8);
        waitingProcessMap.put(document, Collections.singletonList(markdownImage));

        // 使用默认 client
        CloudEnum cloudEnum = OssState.getCloudType(state.getDefaultCloudType());
        OssClient client = ClientUtils.getClient(cloudEnum);

        EventData data = new EventData()
            .setProject(editor.getProject())
            .setEditor(editor)
            .setClient(client)
            .setClientName(cloudEnum.title)
            .setWaitingProcessMap(waitingProcessMap);

        ActionManager manager = new ActionManager(data)
            // 下载图片
            .addHandler(new DownloadImageHandler())
            // 图片压缩
            .addHandler(new ImageCompressionHandler())
            // 图片重命名
            .addHandler(new ImageRenameHandler());

        InsertImageActionEnum insertImageAction = state.getInsertImageAction();

        // 处理复制到指定路径的逻辑
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
            // 设置处理后的路径
            state.setImageSavePath(processedPath);
            // 图片保存
            manager.addHandler(new ImageStorageHandler());
        }

        // 处理上传图片的逻辑
        addPostHanler(insertImageAction, manager);

        new ActionTask(editor.getProject(), MikBundle.message("mik.action.paste.task"), manager).queue();
    }
}
