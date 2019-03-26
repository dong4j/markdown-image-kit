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

package info.dong4j.idea.plugin.chain.paste;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;

import info.dong4j.idea.plugin.MikBundle;
import info.dong4j.idea.plugin.chain.PasteActionHandler;
import info.dong4j.idea.plugin.client.OssClient;
import info.dong4j.idea.plugin.content.ImageContents;
import info.dong4j.idea.plugin.entity.EventData;
import info.dong4j.idea.plugin.enums.CloudEnum;
import info.dong4j.idea.plugin.settings.OssState;
import info.dong4j.idea.plugin.strategy.UploadFromPaste;
import info.dong4j.idea.plugin.strategy.Uploader;
import info.dong4j.idea.plugin.util.ClientUtils;
import info.dong4j.idea.plugin.util.UploadNotification;
import info.dong4j.idea.plugin.util.UploadUtils;

import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;

/**
 * <p>Company: 科大讯飞股份有限公司-四川分公司</p>
 * <p>Description: 图片上传操作</p>
 *
 * @author dong4j
 * @email sjdong3 @iflytek.com
 * @since 2019 -03-26 12:38
 */
@Slf4j
public class ImageUploadHandler extends PasteActionHandler {

    /**
     * 如果设置的默认图床不可用, 给出消息提示
     *
     * @return the boolean
     */
    @Override
    public boolean isEnabled(EventData data) {
        boolean isOpen = STATE.isUploadAndReplace() && STATE.isClipboardControl();
        boolean isAvailable = OssState.getStatus(STATE.getCloudType());
        if (isOpen && !isAvailable) {
            UploadNotification.notifyConfigurableError(data.getEditor().getProject(), OssState.getCloudType(STATE.getCloudType()).title);
        }
        // todo-dong4j : (2019年03月20日 17:32) [使用如下代码获取]
        //  http://www.jetbrains.org/intellij/sdk/docs/basics/persisting_state_of_components.html
        //  "PropertiesComponent.getInstance().setValue("PI__LAST_DIR_PATTERN", dirPattern);"
        return isOpen && isAvailable;
    }

    /**
     * 将临时文件 copy 到用户设置的目录下, 返回处理后的 markdown image mark list
     *
     * @return the boolean
     */
    @Override
    public boolean execute(EventData data) {
        log.trace("upload");

        Editor editor = data.getEditor();

        Task.Backgroundable task = new Task.Backgroundable(editor.getProject(),
                                MikBundle.message("mik.paste.upload.progress"),
                                true) {
            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                startBackgroupTask(indicator);

                Map<String, File> imageMap = data.getImageMap();
                List<String> markList = new ArrayList<>(imageMap.size());

                int totalProcessed = 0;
                int totalCount = imageMap.size();
                try {
                    for (Map.Entry<String, File> imageEntry : imageMap.entrySet()) {
                        String imageName = imageEntry.getKey();
                        // 上传到默认图床
                        CloudEnum cloudEnum = OssState.getCloudType(STATE.getCloudType());
                        OssClient client = ClientUtils.getInstance(cloudEnum);
                        if (client != null) {
                            indicator.setText2("Uploading " + imageName);
                            String imageUrl = Uploader.getInstance().setUploadWay(new UploadFromPaste(client, imageEntry.getValue())).upload();

                            if (StringUtils.isNotBlank(imageUrl)) {
                                indicator.setText2("Save Mark: " + imageUrl);
                                String newLineText = UploadUtils.getFinalImageMark("", imageUrl, imageUrl, ImageContents.LINE_BREAK);
                                // 保存上传后 markdown image mark
                                markList.add(newLineText);
                            }
                        } else {
                            UploadNotification.notifyConfigurableError(editor.getProject(), cloudEnum.title);
                        }
                        indicator.setFraction(++totalProcessed * 1.0 / totalCount);
                    }
                    data.setUploadedMarkList(markList);
                } finally {
                    endBackgroupTask(indicator);
                }
            }

            @Override
            public void onCancel() {
                log.trace("cancel callback");
            }

            @Override
            public void onSuccess() {
                log.trace("success callback");
            }

            @Override
            public void onFinished() {
                log.trace("finished callback");
                data.setUploadImageFinished(true);
            }
        };

        task.queue();

        return false;
    }
}
