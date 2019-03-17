package info.dong4j.idea.plugin.handler;

import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionPlaces;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.Presentation;
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
import com.intellij.util.containers.hash.HashMap;

import info.dong4j.idea.plugin.content.ImageContents;
import info.dong4j.idea.plugin.content.MarkdownContents;
import info.dong4j.idea.plugin.enums.CloudEnum;
import info.dong4j.idea.plugin.settings.OssPersistenConfig;
import info.dong4j.idea.plugin.settings.OssState;
import info.dong4j.idea.plugin.util.CharacterUtils;
import info.dong4j.idea.plugin.util.EnumsUtils;
import info.dong4j.idea.plugin.util.ImageUtils;
import info.dong4j.idea.plugin.util.UploadUtils;

import net.coobird.thumbnailator.Thumbnails;

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


                // if (true) {
                //     PasteImageFromClipboard action = new PasteImageFromClipboard();
                //     AnActionEvent event = createAnEvent(action, dataContext);
                //     action.actionPerformed(event);
                //     return;
                // }

                // 根据配置操作. 是否开启 clioboard 监听; 是否将文件拷贝到目录; 是否开启上传到图床
                OssState state = OssPersistenConfig.getInstance().getState();

                boolean isClipboardControl = state.isClipboardControl();

                if (isClipboardControl) {
                    Map<DataFlavor, Object> clipboardData = ImageUtils.getDataFromClipboard();
                    // 只会循环一次
                    for (Map.Entry<DataFlavor, Object> entry : clipboardData.entrySet()) {
                        if (entry.getKey().equals(DataFlavor.javaFileListFlavor)) {
                            // 肯定是 List<File> 类型
                            @SuppressWarnings("unchecked") List<File> fileList = (List<File>) entry.getValue();
                            Map<String, Image> imageMap = new HashMap<>(fileList.size());
                            for (File file : fileList) {
                                // 先检查是否为图片类型
                                Image image = null;
                                try {
                                    File compressedFile = new File(System.getProperty("java.io.tmpdir") + file.getName());
                                    // 图片压缩
                                    if(file.isFile() && file.getName().endsWith("jpg")){
                                        ImageUtils.compress(file, compressedFile, state.getCompressBeforeUploadOfPercent() - 20);
                                    }
                                    image = ImageIO.read(compressedFile);
                                } catch (IOException ignored) {
                                }
                                String fileName = file.getName();
                                // 只要有一个文件不是 image, 就执行默认操作然后退出
                                if (image != null) {
                                    imageMap.put(fileName, image);
                                }
                            }

                            // 如果 image 的数量等于总文件数, 才执行,否则执行默认操作
                            if (imageMap.size() == fileList.size()) {
                                for (Map.Entry<String, Image> imageEntry : imageMap.entrySet()) {
                                    invoke(editor, document, imageEntry.getValue(), imageEntry.getKey());
                                }
                                // 处理后退出, 避免执行默认的 paste 操作
                                return;
                            }
                        } else {
                            // image 类型统一重命名, 因为获取不到文件名
                            String fileName = CharacterUtils.getRandomString(12) + ".png";
                            invoke(editor, document, (Image) entry.getValue(), fileName);
                            return;
                        }
                    }
                }
            }
        }
        defaultAction(editor, caret, dataContext);
    }

    private AnActionEvent createAnEvent(AnAction action, @NotNull DataContext context) {
        Presentation presentation = action.getTemplatePresentation().clone();
        return new AnActionEvent(null, context, ActionPlaces.UNKNOWN, presentation, ActionManager.getInstance(), 0);
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
            // 圆角处理
            bufferedImage = ImageUtils.toBufferedImage(ImageUtils.makeRoundedCorner(bufferedImage, 20));
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
                    // 如果勾选了上传且替换, 就不再插入本地的图片标签
                    if (!state.isUploadAndReplace()) {
                        File imageFileRelativizePath = curDocument.getParentFile().toPath().relativize(imageFile.toPath()).toFile();
                        String relImagePath = imageFileRelativizePath.toString().replace('\\', '/');
                        EditorModificationUtil.insertStringAtCaret(editor, "![](" + relImagePath + ")" + ImageContents.LINE_BREAK);
                    }
                    BufferedImage compressedImage = Thumbnails.of(bufferedImage)
                        .size(bufferedImage.getWidth(), bufferedImage.getHeight())
                        .outputQuality(state.getCompressBeforeUploadOfPercent() * 1.0 / 100).asBufferedImage();
                    ImageIO.write(compressedImage, "png", imageFile);
                    // 保存到文件后异步刷新缓存, 让图片显示到文件树中
                    VirtualFileManager.getInstance().syncRefresh();
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
            CloudEnum cloudEnum = EnumsUtils.getCloudEnum(defaultCloudType);
            // 此处进行异步处理, 不然上传大图时会卡死
            Runnable r = () -> {
                String imageUrl = upload(cloudEnum, is, imageName);
                if (StringUtils.isNotBlank(imageUrl)) {
                    String newLineText = UploadUtils.getFinalImageMark("", imageUrl, imageUrl, ImageContents.LINE_BREAK);
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
}
