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
                Image imageFromClipboard = ImageUtils.getImageFromClipboard();
                if (imageFromClipboard != null) {
                    BufferedImage bufferedImage = ImageUtils.toBufferedImage(imageFromClipboard);
                    ByteArrayOutputStream os = new ByteArrayOutputStream();
                    if (bufferedImage != null) {
                        // 统一转成 png
                        try {
                            ImageIO.write(bufferedImage, "png", os);
                        } catch (IOException e) {
                            // todo-dong4j : (2019年03月17日 03:20) [添加通知]
                            log.trace("", e);
                            return;
                        }
                        InputStream is = new ByteArrayInputStream(os.toByteArray());
                        int defaultCloudType = OssPersistenConfig.getInstance().getState().getCloudType();
                        CloudEnum cloudEnum = getCloudEnum(defaultCloudType);
                        // todo-dong4j : (2019年03月17日 03:45) [获取不到文件名, 只能随机]
                        String imageUrl = upload(cloudEnum, is, CharacterUtils.getRandomString(12) + ".png");
                        if (StringUtils.isNotBlank(imageUrl)) {
                            // 在光标位置插入指定字符串
                            WriteCommandAction.runWriteCommandAction(editor.getProject(),
                                                                     () -> EditorModificationUtil.insertStringAtCaret(editor,
                                                                                                                      "![](" + imageUrl + ")"));
                        }
                    }

                    return;
                }
            }
        }

        if (editorActionHandler != null) {
            editorActionHandler.execute(editor, caret, dataContext);
        }
    }

    /**
     * 通过反射调用调用, 避免条件判断
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
