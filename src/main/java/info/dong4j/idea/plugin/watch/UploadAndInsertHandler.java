package info.dong4j.idea.plugin.watch;

import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorModificationUtil;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;

import info.dong4j.idea.plugin.MikBundle;
import info.dong4j.idea.plugin.client.OssClient;
import info.dong4j.idea.plugin.content.ImageContents;
import info.dong4j.idea.plugin.enums.CloudEnum;
import info.dong4j.idea.plugin.settings.ImageManagerPersistenComponent;
import info.dong4j.idea.plugin.strategy.UploadFromPaste;
import info.dong4j.idea.plugin.strategy.Uploader;
import info.dong4j.idea.plugin.util.ClientUtils;
import info.dong4j.idea.plugin.util.EnumsUtils;
import info.dong4j.idea.plugin.util.ImageUtils;
import info.dong4j.idea.plugin.util.UploadUtils;

import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Map;
import java.util.Optional;

import javax.imageio.ImageIO;

import lombok.extern.slf4j.Slf4j;

/**
 * <p>Company: 科大讯飞股份有限公司-四川分公司</p>
 * <p>Description: </p>
 *
 * @author dong4j
 * @email sjdong3@iflytek.com
 * @since 2019-03-22 18:48
 */
@Slf4j
public class UploadAndInsertHandler extends PasteActionHandler {
    public UploadAndInsertHandler(@NotNull Editor editor, Map<String, Image> imageMap) {
        super(editor, imageMap);
    }

    @Override
    public boolean isEnabled() {
        return STATE.isUploadAndReplace() && STATE.isClipboardControl();
    }

    @Override
    public boolean execute() {
        log.trace("UploadAndReplaceHandler");

        new Task.Backgroundable(editor.getProject(), MikBundle.message("mik.paste.upload.and.insert.progress"), true) {
            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                indicator.setFraction(0.0);
                indicator.pushState();
                indicator.setIndeterminate(false);

                int totalProcessed = 0;
                int totalCount = imageMap.size();
                for (Map.Entry<String, Image> imageEntry : imageMap.entrySet()) {
                    String imageName = imageEntry.getKey();
                    BufferedImage bufferedImage = ImageUtils.toBufferedImage(imageEntry.getValue());
                    // todo-dong4j : (2019年03月22日 21:03) []
                    if (bufferedImage != null) {
                        bufferedImage = ImageUtils.toBufferedImage(ImageUtils.makeRoundedCorner(bufferedImage, 20));
                        try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
                            assert bufferedImage != null;
                            ImageIO.write(bufferedImage, "png", os);
                            InputStream inputStream = new ByteArrayInputStream(os.toByteArray());
                            // 上传到默认图床
                            int index = ImageManagerPersistenComponent.getInstance().getState().getCloudType();
                            Optional<CloudEnum> cloudType = EnumsUtils.getEnumObject(CloudEnum.class, e -> e.getIndex() == index);
                            // 此处进行异步处理, 不然上传大图时会卡死
                            Runnable r = () -> {
                                OssClient client = ClientUtils.getInstance(cloudType.orElse(CloudEnum.WEIBO_CLOUD));
                                indicator.setText2("Uploading " + imageName);
                                String imageUrl = new Uploader().setUploadWay(new UploadFromPaste(client, inputStream, imageName)).upload();

                                if (StringUtils.isNotBlank(imageUrl)) {
                                    indicator.setText2("Replace " + imageUrl);
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
                    indicator.setFraction( ++totalProcessed * 1.0 / totalCount);
                }
                indicator.setFraction(1.0);
                indicator.popState();
                indicator.stop();
            }
        }.queue();
        return false;
    }
}
