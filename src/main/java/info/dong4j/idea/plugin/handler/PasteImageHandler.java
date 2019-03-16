package info.dong4j.idea.plugin.handler;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
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
import info.dong4j.idea.plugin.settings.OssPersistenConfig;
import info.dong4j.idea.plugin.settings.OssState;
import info.dong4j.idea.plugin.util.AliyunUploadUtils;
import info.dong4j.idea.plugin.util.ImageUtils;

import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.Image;
import java.awt.datatransfer.Transferable;
import java.awt.image.BufferedImage;
import java.io.*;

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
                    OssState.QiniuOssState qiniuOssState = OssPersistenConfig.getInstance().getState().getQiniuOssState();
                    String accessKey = qiniuOssState.getAccessKey();
                    String secretKey = qiniuOssState.getAccessSecretKey();
                    String bucket = qiniuOssState.getBucketName();
                    String upHost = qiniuOssState.getUrl();
                    // String key = ImageUtils.save(bufferedImage, "preFix", "png", accessKey, secretKey, bucket);
                    // String imageUrl = upHost + "/" + key;

                    String imageUrl = uploadTest();

                    // 在光标位置插入指定字符串
                    WriteCommandAction.runWriteCommandAction(editor.getProject(),
                                                             () -> EditorModificationUtil.insertStringAtCaret(editor,
                                                                                                              "![](" + imageUrl + ")"));
                    return;
                }
            }
        }

        if (editorActionHandler != null) {
            editorActionHandler.execute(editor, caret, dataContext);
        }
    }

    private String uploadTest(){
        OssState.AliyunOssState aliyunOssState = OssPersistenConfig.getInstance().getState().getAliyunOssState();
        String tempBucketName = aliyunOssState.getBucketName();
        String tempAccessKey = aliyunOssState.getAccessKey();
        String tempAccessSecretKey = aliyunOssState.getAccessSecretKey();
        String tempEndpoint = aliyunOssState.getEndpoint();
        String tempFileDir = aliyunOssState.getFiledir();
        tempFileDir = StringUtils.isBlank(tempFileDir) ? "" : tempFileDir + "/";
        OSS oss = null;
        try {
            OSSClientBuilder ossClientBuilder = new OSSClientBuilder();
            // 返回读取指定资源的输入流
            InputStream is = this.getClass().getResourceAsStream("/test.png");
            oss = ossClientBuilder.build(tempEndpoint, tempAccessKey, tempAccessSecretKey);
            oss.putObject(tempBucketName,
                          tempFileDir + "test.png",
                          is);
            return AliyunUploadUtils.getUrl(tempFileDir, "test.png");
        } catch (Exception e) {
            log.trace("", e);
        } finally {
            if (oss != null) {
                oss.shutdown();
            }
        }
        return "";
    }

    @Override
    public void execute(Editor editor, DataContext dataContext, Producer<Transferable> producer) {

    }
}
