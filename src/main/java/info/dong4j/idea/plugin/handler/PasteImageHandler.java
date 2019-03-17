package info.dong4j.idea.plugin.handler;

import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Caret;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorModificationUtil;
import com.intellij.openapi.editor.actionSystem.EditorActionHandler;
import com.intellij.openapi.editor.actionSystem.EditorTextInsertHandler;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.util.Producer;

import info.dong4j.idea.plugin.content.MarkdownContents;
import info.dong4j.idea.plugin.enums.CloudEnum;
import info.dong4j.idea.plugin.settings.OssPersistenConfig;
import info.dong4j.idea.plugin.settings.OssState;
import info.dong4j.idea.plugin.util.CharacterUtils;
import info.dong4j.idea.plugin.util.EnumsUtils;
import info.dong4j.idea.plugin.util.ImageUtils;
import info.dong4j.idea.plugin.util.UploadUtils;

import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.Image;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.image.BufferedImage;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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
        if (virtualFile != null) {
            if (virtualFile.getFileType().getName().equals(MarkdownContents.MARKDOWN_FILE_TYPE)
                || virtualFile.getName().endsWith(MarkdownContents.MARKDOWN_FILE_SUFIX)) {
                // 根据配置操作. 是否开启 clioboard 监听; 是否将文件拷贝到目录; 是否开启上传到图床
                OssState state = OssPersistenConfig.getInstance().getState();

                boolean isClipboardControl = state.isClipboardControl();

                if (isClipboardControl) {
                    Map<DataFlavor, Object> clipboardData = ImageUtils.getDataFromClipboard();
                    for (Map.Entry entry : clipboardData.entrySet()) {
                        if (entry.getKey().equals(DataFlavor.javaFileListFlavor)) {
                            // 肯定是 List<File> 类型
                            @SuppressWarnings("unchecked") List<File> fileList = (List<File>) entry.getValue();
                            for (File file : fileList) {
                                // 先检查是否为图片类型
                                Image image = null;
                                try {
                                    image = ImageIO.read(file);
                                } catch (IOException ignored) {
                                }
                                String fileName = file.getName();
                                if (image != null) {
                                    invoke(editor, document, image, fileName);
                                    // 提前退出, 防止执行默认 paste 操作
                                    return;
                                }
                            }
                        } else {
                            // image 类型统一重命名, 因为获取不到文件名
                            String fileName = CharacterUtils.getRandomString(12) + ".png";
                            invoke(editor, document, (Image) entry.getValue(), fileName);
                            // 提前退出, 防止执行默认 paste 操作
                            return;
                        }
                    }
                }
            }
        }

        // 执行默认的 paste 操作
        if (editorActionHandler != null) {
            editorActionHandler.execute(editor, caret, dataContext);
        }
    }

    /**
     * 执行上传或拷贝处理
     *
     * @param editor   the editor
     * @param document the document
     * @param image    the image
     * @param fileName the file name
     */
    private void invoke(@NotNull Editor editor, Document document, Image image, String fileName) {
        OssState state = OssPersistenConfig.getInstance().getState();
        boolean isCopyToDir = state.isCopyToDir();
        boolean isUploadAndReplace = state.isUploadAndReplace();

        BufferedImage bufferedImage = ImageUtils.toBufferedImage(image);
        if (bufferedImage != null) {
            if (isUploadAndReplace) {
                uploadAndReplace(editor, bufferedImage, fileName);
            }
            if (isCopyToDir) {
                copyToDirAndReplace(editor, document, state, bufferedImage, fileName);
            }
        }
    }

    /**
     * Copy to dir.
     *
     * @param editor        the editor
     * @param document      the document
     * @param state         the state
     * @param bufferedImage the buffered image
     * @param imageName     the image name
     */
    private void copyToDirAndReplace(@NotNull Editor editor,
                                     Document document,
                                     OssState state,
                                     BufferedImage bufferedImage,
                                     String imageName) {
        // 保存图片
        VirtualFile currentFile = FileDocumentManager.getInstance().getFile(document);
        assert currentFile != null;
        File curDocument = new File(currentFile.getPath());
        String savepath = state.getImageSavePath();
        File imageDir = new File(curDocument.getParent(), savepath);
        boolean checkDir = imageDir.exists() && imageDir.isDirectory();
        if (checkDir || imageDir.mkdirs()) {
            File imageFile = new File(imageDir, imageName);
            Runnable r = () -> {
                try {
                    ImageIO.write(bufferedImage, "png", imageFile);
                    // 保存到文件后异步刷新缓存, 让图片显示到文件树中
                    VirtualFileManager.getInstance().asyncRefresh(null);
                    File imageFileRelativizePath = curDocument.getParentFile().toPath().relativize(imageFile.toPath()).toFile();
                    String relImagePath = imageFileRelativizePath.toString().replace('\\', '/');
                    EditorModificationUtil.insertStringAtCaret(editor, "![](" + relImagePath + ")");
                } catch (IOException e) {
                    // todo-dong4j : (2019年03月17日 15:11) [消息通知]
                    log.trace("", e);
                }
            };
            WriteCommandAction.runWriteCommandAction(editor.getProject(), r);
        }
    }

    /**
     * Upload and replace.
     * todo-dong4j : (2019年03月17日 19:53) [处理是否替换标签]
     *
     * @param editor        the editor
     * @param bufferedImage the buffered image
     * @param imageName     the image name
     */
    private void uploadAndReplace(@NotNull Editor editor, BufferedImage bufferedImage, String imageName) {
        try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            ImageIO.write(bufferedImage, "png", os);
            InputStream is = new ByteArrayInputStream(os.toByteArray());
            // 上传到默认图床
            int defaultCloudType = OssPersistenConfig.getInstance().getState().getCloudType();
            CloudEnum cloudEnum = getCloudEnum(defaultCloudType);
            // 此处进行异步处理, 不然上传大图时会卡死
            Runnable r = () -> {
                String imageUrl = upload(cloudEnum, is, imageName);
                if (StringUtils.isNotBlank(imageUrl)) {
                    // 在光标位置插入指定字符串
                    String newLineText = UploadUtils.getFinalImageMark("", imageUrl, imageUrl);
                    EditorModificationUtil.insertStringAtCaret(editor, newLineText);
                }
            };
            WriteCommandAction.runWriteCommandAction(editor.getProject(), r);
        } catch (IOException e) {
            // todo-dong4j : (2019年03月17日 03:20) [添加通知]
            log.trace("", e);
        }
    }

    /**
     * 通过反射调用, 避免条件判断, 便于扩展
     * todo-dong4j : (2019年03月17日 14:13) [考虑将上传到具体的 OSS 使用 properties]
     * 通过反射调用 upload 单个文件上传{@link info.dong4j.idea.plugin.strategy.UploadStrategy#upload}
     *
     * @param cloudEnum   the cloud enum
     * @param inputStream the input stream
     * @return the string
     */
    private String upload(@NotNull CloudEnum cloudEnum, InputStream inputStream, String fileName) {
        try {
            Class<?> cls = Class.forName(cloudEnum.getClassName());
            Object obj = cls.newInstance();
            Method setFunc = cls.getMethod("upload", InputStream.class, String.class);
            return (String) setFunc.invoke(obj, inputStream, fileName);
        } catch (ClassNotFoundException | IllegalAccessException | InvocationTargetException | InstantiationException | NoSuchMethodException e) {
            // todo-dong4j : (2019年03月17日 03:20) [添加通知]
            log.trace("", e);
        }
        return "";
    }

    @Override
    public void execute(Editor editor, DataContext dataContext, Producer<Transferable> producer) {

    }

    /**
     * Gets cloud enum.
     *
     * @param index the index
     * @return the cloud enum
     */
    @NotNull
    private CloudEnum getCloudEnum(int index) {
        CloudEnum defaultCloud = CloudEnum.WEIBO_CLOUD;
        Optional<CloudEnum> defaultCloudType = EnumsUtils.getEnumObject(CloudEnum.class, e -> e.getIndex() == index);
        if (defaultCloudType.isPresent()) {
            defaultCloud = defaultCloudType.get();
        }
        return defaultCloud;
    }
}
