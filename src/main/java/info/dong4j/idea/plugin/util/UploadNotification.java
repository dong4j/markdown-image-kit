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

package info.dong4j.idea.plugin.util;

import com.intellij.ide.BrowserUtil;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationDisplayType;
import com.intellij.notification.NotificationGroup;
import com.intellij.notification.NotificationListener;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;

import info.dong4j.idea.plugin.MikBundle;
import info.dong4j.idea.plugin.exception.UploadException;
import info.dong4j.idea.plugin.settings.ProjectSettingsPage;

import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

import javax.swing.event.HyperlinkEvent;

import lombok.extern.slf4j.Slf4j;

/**
 * <p>Company: 科大讯飞股份有限公司-四川分公司</p>
 * <p>Description: 上传后消息通知 </p>
 *
 * @author dong4j
 * @date 2019 -03-22 22:57
 * @email sjdong3 @iflytek.com
 */
@Slf4j
public class UploadNotification extends Notification {
    private static final String HELP_URL = MikBundle.message("mik.help");
    private static final String UPLOAD_NOTIFICATION_GROUP = "MIK Group";
    private static final String UPLOAD_FINSHED = "Upload Finshed";
    /** 注册到通知 */
    private static final NotificationGroup NOTIFICATION_GROUP = new NotificationGroup(UPLOAD_NOTIFICATION_GROUP, NotificationDisplayType.TOOL_WINDOW, true);

    /**
     * Instantiates a new Upload notification.
     *
     * @param title   the title
     * @param content the content
     * @param type    the type
     */
    public UploadNotification(@NotNull String title,
                              @NotNull String content,
                              @NotNull NotificationType type) {
        super(UPLOAD_NOTIFICATION_GROUP, title, content, type);
    }

    /**
     * 上传失败通知, 可打开设置面板
     * 文件链接, 帮助链接, 设置链接
     * todo-dong4j : (2019年03月23日 15:14) [{@link com.intellij.openapi.fileTypes.impl.ApproveRemovedMappingsActivity}]
     *
     * @param e       the e
     * @param project the project
     */
    public static void notifyUploadFailure(UploadException e, Project project) {
        String details = e.getMessage();
        String content = "<p><a href=\"\">Configure oss...</a></p>";
        if (!StringUtil.isEmpty(details)) {
            content = "<p>" + details + "</p>" + content;
        }
        Notifications.Bus.notify(new Notification(UPLOAD_NOTIFICATION_GROUP, "Upload Failured",
                                                  content, NotificationType.ERROR,
                                                  (notification, event) -> {
                                                      ProjectSettingsPage configurable = new ProjectSettingsPage();
                                                      // 打开设置面板
                                                      ShowSettingsUtil.getInstance().editConfigurable(project, configurable);
                                                      // 点击超链接后关闭通知
                                                      if (event.getEventType().equals(HyperlinkEvent.EventType.ENTERED)) {
                                                          notification.hideBalloon();
                                                      }
                                                  }), project);
    }

    /**
     * 上传完成后通知
     *
     * @param content the content
     */
    public static void notifyUploadFinshed(String content) {
        Notification notification = new Notification(UPLOAD_NOTIFICATION_GROUP, null, NotificationType.INFORMATION);
        notification.setTitle(UPLOAD_FINSHED);
        // 可使用 HTML 标签
        notification.setContent(content);
        Notifications.Bus.notify(notification);
    }

    /**
     * 上传时检查到配置错误时通知
     *
     * @param project the project
     */
    public static void notifyConfigurableError(Project project, String actionName) {
        String content = "<p><a href=''>Configure " + actionName + "</a></p>";
        content = "<p>You may need to reset your account. Please be sure to <b>test</b> it after the setup is complete.</p>" + content;
        content = content + "<p>Or you may need a little <a href='" + HELP_URL + "'>Help</a></p>";
        Notifications.Bus.notify(new Notification(UPLOAD_NOTIFICATION_GROUP,
                                                  "Configurable Error",
                                                  content,
                                                  NotificationType.ERROR,
                                                  new NotificationListener.Adapter() {
                                                      @Override
                                                      protected void hyperlinkActivated(@NotNull Notification notification, @NotNull HyperlinkEvent e) {
                                                          String url = e.getDescription();
                                                          log.trace("{}",e.getDescription());
                                                          if(StringUtils.isBlank(url)){
                                                              ProjectSettingsPage configurable = new ProjectSettingsPage();
                                                              // 打开设置面板
                                                              ShowSettingsUtil.getInstance().editConfigurable(project, configurable);
                                                          } else {
                                                              BrowserUtil.browse(url);
                                                          }
                                                          if(notification.getBalloon() != null){
                                                              notification.getBalloon().hide();
                                                          }
                                                      }
                                                  }), project);
    }
}