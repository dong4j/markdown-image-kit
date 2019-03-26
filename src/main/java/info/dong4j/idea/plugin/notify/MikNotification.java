/*
 * MIT License
 *
 * Copyright (c) 2019 dong4j
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

package info.dong4j.idea.plugin.notify;

import com.google.gson.Gson;

import com.intellij.icons.AllIcons;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationDisplayType;
import com.intellij.notification.NotificationGroup;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.Balloon;

import info.dong4j.idea.plugin.MikBundle;
import info.dong4j.idea.plugin.entity.HelpResult;
import info.dong4j.idea.plugin.enums.HelpType;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;

/**
 * <p>Company: 科大讯飞股份有限公司-四川分公司</p>
 * <p>Description: </p>
 *
 * @author dong4j
 * @email sjdong3 @iflytek.com
 * @since 2019 -03-26 22:20
 */
@Slf4j
public class MikNotification extends Notification {
    /**
     * The Help url.
     */
    static final String HELP_URL = helpUrl(HelpType.NOTTIFY.where);
    /**
     * The Upload notification group.
     */
    static final String MIK_NOTIFICATION_GROUP = "MIK Group";
    private static final String MIK_NOTIFICATION_NONE_GROUP = "Image Kit Group";
    /** 注册到通知 */
    protected static final NotificationGroup NOTIFICATION_GROUP = new NotificationGroup(MIK_NOTIFICATION_GROUP, NotificationDisplayType.BALLOON, true, null, AllIcons.Gutter.Colors);
    protected static final NotificationGroup NOTIFICATION_NONE_GROUP = new NotificationGroup(MIK_NOTIFICATION_NONE_GROUP, NotificationDisplayType.NONE, true, null, AllIcons.Debugger.ShowCurrentFrame);

    /**
     * Instantiates a new Upload notification.
     *
     * @param title   the title
     * @param content the content
     * @param type    the type
     */
    MikNotification(@NotNull String title,
                    @NotNull String content,
                    @NotNull NotificationType type) {
        super(MIK_NOTIFICATION_GROUP, title, content, type);
    }

    /**
     * 获取帮助页
     *
     * @param where the where
     * @return the string
     */
    public static String helpUrl(String where) {
        HttpClient client = new HttpClient();
        PostMethod method = new PostMethod(MikBundle.message("mik.help.rest.url") + where);
        client.getParams().setContentCharset("UTF-8");
        HelpResult helpResult = null;
        try {
            client.executeMethod(method);
            String response = method.getResponseBodyAsString(1000);
            helpResult = new Gson().fromJson(response, HelpResult.class);

        } catch (IOException e) {
            log.trace("", e);
        } finally {
            method.releaseConnection();
        }
        assert helpResult != null;
        return helpResult.getUrl();
    }

    /**
     * Hide balloon.
     *
     * @param balloon the balloon
     */
    static void hideBalloon(Balloon balloon) {
        if (balloon != null) {
            balloon.hide();
        }
    }

    /**
     * Notify compress info.
     *
     * @param project the project
     */
    public static void notifyCompressInfo(Project project, Map<String, String> compressInfo) {
        StringBuilder content = new StringBuilder();
        for(Map.Entry<String, String> entry : compressInfo.entrySet()){
            content.append("<p>").append(entry.getKey()).append("\t\t\t").append(entry.getValue()).append("</p>");
        }
        Notifications.Bus.notify(new Notification(MIK_NOTIFICATION_NONE_GROUP,
                                                  "<p>Compress Finished: </p>",
                                                  content.toString(),
                                                  NotificationType.INFORMATION), project);

    }
}
