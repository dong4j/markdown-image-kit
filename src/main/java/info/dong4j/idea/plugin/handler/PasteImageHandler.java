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
import com.intellij.util.Producer;

import info.dong4j.idea.plugin.content.MarkdownContents;
import info.dong4j.idea.plugin.enums.CloudEnum;
import info.dong4j.idea.plugin.settings.OssPersistenConfig;
import info.dong4j.idea.plugin.settings.OssState;
import info.dong4j.idea.plugin.util.CharacterUtils;
import info.dong4j.idea.plugin.util.EnumsUtils;
import info.dong4j.idea.plugin.util.ImageUtils;

import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.Image;
import java.awt.datatransfer.Transferable;
import java.awt.image.BufferedImage;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Optional;

import javax.imageio.ImageIO;

import lombok.extern.slf4j.Slf4j;

/**
 * <p>Company: 科大讯飞股份有限公司-四川分公司</p>
 * <p>Description: 读取 Clipboard 图片, 上传到 OSS, 最后插入到光标位置</p>
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
                boolean isCopyToDir = state.isCopyToDir();
                boolean isUploadAndReplace = state.isUploadAndReplace();
                if (isClipboardControl) {
                    Image imageFromClipboard = ImageUtils.getImageFromClipboard();
                    if (imageFromClipboard != null) {
                        BufferedImage bufferedImage = ImageUtils.toBufferedImage(imageFromClipboard);
                        ByteArrayOutputStream os = new ByteArrayOutputStream();
                        if (bufferedImage != null) {
                            String imageName = CharacterUtils.getRandomString(12) + ".png";
                            // 上传并替换
                            if (isUploadAndReplace) {
                                try {
                                    ImageIO.write(bufferedImage, "png", os);
                                    InputStream is = new ByteArrayInputStream(os.toByteArray());
                                    int defaultCloudType = OssPersistenConfig.getInstance().getState().getCloudType();
                                    CloudEnum cloudEnum = getCloudEnum(defaultCloudType);
                                    // todo-dong4j : (2019年03月17日 03:45) [获取不到文件名, 只能随机]
                                    String imageUrl = upload(cloudEnum, is, imageName);
                                    if (StringUtils.isNotBlank(imageUrl)) {
                                        // 在光标位置插入指定字符串
                                        insertImageElement(editor, imageUrl);
                                    }
                                } catch (IOException e) {
                                    // todo-dong4j : (2019年03月17日 03:20) [添加通知]
                                    log.trace("", e);
                                }
                            }
                            // 拷贝图片到目录
                            if (isCopyToDir) {
                                // 保存图片
                                VirtualFile currentFile = FileDocumentManager.getInstance().getFile(document);
                                assert currentFile != null;
                                File curDocument = new File(currentFile.getPath());
                                String savepath = state.getImageSavePath();
                                File imageDir = new File(curDocument.getParent(), savepath);
                                boolean checkDir = imageDir.exists() && imageDir.isDirectory();
                                if (checkDir || imageDir.mkdirs()) {
                                    File imageFile = new File(imageDir, imageName);
                                    try {
                                        ImageIO.write(bufferedImage, "png", imageFile);
                                        // 插入 markdown 标签
                                        insertImageElement(editor, curDocument.getParentFile().toPath().relativize(imageFile.toPath()).toFile());
                                    } catch (IOException e) {
                                        // todo-dong4j : (2019年03月17日 15:11) [消息通知]
                                        log.trace("", e);
                                    }
                                }
                            }
                            // 提前退出, 使执行默认的粘贴操作(如果是非文本, 只会粘贴文件名)
                            return;
                        }
                    }
                }
            }
        }

        // 执行默认的粘贴操作
        if (editorActionHandler != null) {
            editorActionHandler.execute(editor, caret, dataContext);
        }
    }

    /**
     * 插入 markdown image 标签
     *
     * @param editor    the editor
     * @param imageFile the image file
     */
    private void insertImageElement(final @NotNull Editor editor, File imageFile) {
        String relImagePath = imageFile.toString().replace('\\', '/');
        insertImageElement(editor, relImagePath);
    }

    /**
     * Insert image element.
     *
     * @param editor       the editor
     * @param relImagePath the rel image path
     */
    private void insertImageElement(@NotNull Editor editor, String relImagePath) {
        Runnable r = () -> EditorModificationUtil.insertStringAtCaret(editor, "![](" + relImagePath + ")");
        WriteCommandAction.runWriteCommandAction(editor.getProject(), r);
    }

    /**
     * 通过反射调用, 避免条件判断, 便于扩展
     * todo-dong4j : (2019年03月17日 14:13) [考虑将上传到具体的 OSS 使用 properties]
     *
     * @param cloudEnum   the cloud enum
     * @param inputStream the input stream
     * @return the string
     */
    private String upload(CloudEnum cloudEnum, InputStream inputStream, String fileName) {
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
