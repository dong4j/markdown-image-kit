/*
 * MIT License
 *
 * Copyright (c) 2019 dong4j <dong4j@gmail.com>
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
 *
 */

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
import info.dong4j.idea.plugin.settings.OssState;
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
        // todo-dong4j : (2019年03月20日 17:32) [使用如下代码获取]
        //  http://www.jetbrains.org/intellij/sdk/docs/basics/persisting_state_of_components.html
        //  "PropertiesComponent.getInstance().setValue("PI__LAST_DIR_PATTERN", dirPattern);"
        return OssState.getStatus(STATE.getCloudType()) && STATE.isUploadAndReplace() && STATE.isClipboardControl();
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
                            Runnable r = () -> {
                                OssClient client = ClientUtils.getInstance(cloudType.orElse(CloudEnum.WEIBO_CLOUD));
                                indicator.setText2("Uploading " + imageName);
                                String imageUrl = Uploader.getInstance().setUploadWay(new UploadFromPaste(client, inputStream, imageName)).upload();

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
                    indicator.setFraction(++totalProcessed * 1.0 / totalCount);
                }
                indicator.setFraction(1.0);
                indicator.popState();
                indicator.stop();
            }
        }.queue();
        return false;
    }
}
