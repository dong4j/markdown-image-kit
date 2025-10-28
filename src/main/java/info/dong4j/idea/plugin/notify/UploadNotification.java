package info.dong4j.idea.plugin.notify;

import com.intellij.ide.actions.RevealFileAction;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationAction;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;

import info.dong4j.idea.plugin.MikBundle;
import info.dong4j.idea.plugin.exception.UploadException;
import info.dong4j.idea.plugin.settings.ProjectSettingsPage;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.List;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;

/**
 * 上传后消息通知类
 * <p>
 * 用于在文件上传过程中或完成后，向用户发送通知消息，包括上传失败、配置错误等场景的通知。
 * 支持通过超链接跳转至设置面板或帮助页面，提升用户操作体验。
 * </p>
 *
 * @author dong4j
 * @version 0.0.1
 * @date 2019.03.22
 * @since 0.0.1
 */
@Slf4j
@SuppressWarnings( {"jol", "DuplicatedCode"})
public class UploadNotification extends MikNotification {
    /** 上传已完成状态标识 */
    private static final String UPLOAD_FINSHED = MikBundle.message("notification.upload.finished");

    /**
     * 创建一个新的上传通知对象
     * <p>
     * 该构造方法用于初始化一个上传通知实例，传入标题、内容和通知类型
     *
     * @param title   通知的标题
     * @param content 通知的内容
     * @param type    通知的类型，需为 {@link NotificationType} 枚举值
     * @since 0.0.1
     */
    public UploadNotification(@NotNull String title,
                              @NotNull String content,
                              @NotNull NotificationType type) {
        super(title, content, type);
    }

    /**
     * 上传失败时通知用户，并可打开设置面板
     * <p>
     * 该方法用于在上传失败时向用户发送通知，同时提供文件链接、帮助链接和设置链接，以便用户进行后续操作。
     *
     * @param project 项目对象，用于获取相关链接信息
     * @since 0.0.1
     */
    public static void notifyUploadFailure(Project project) {
        notifyUploadFailure(new UploadException(""), project);
    }

    /**
     * 上传失败时通知用户，并提供配置链接以打开设置面板
     * <p>
     * 该方法在上传失败时显示错误通知，包含错误详情和配置链接。点击链接可打开项目设置面板。
     *
     * @param e       上传异常对象，包含错误信息
     * @param project 当前项目对象，用于关联通知和设置面板
     * @since 0.0.1
     */
    public static void notifyUploadFailure(UploadException e, Project project) {
        String details = e.getMessage();
        String template = "<p><a href=\"\">%s</a></p>";
        String content = String.format(template, MikBundle.message("notification.configure.oss"));
        if (StringUtil.isNotEmpty(details)) {
            content = String.format("<p>%s</p>%s", details, content);
        }
        Notification notification = new Notification(MIK_NOTIFICATION_GROUP,
                                                     MikBundle.message("notification.upload.failed"),
                                                     content, NotificationType.ERROR);
        // 使用 addAction 替代过时的 setListener
        notification.addAction(new NotificationAction(MikBundle.message("notification.configure.oss")) {
            @Override
            public void actionPerformed(@NotNull AnActionEvent e, @NotNull Notification notification) {
                ProjectSettingsPage configurable = new ProjectSettingsPage();
                // 打开设置面板
                ShowSettingsUtil.getInstance().editConfigurable(project, configurable);
                notification.expire();
            }
        });
        Notifications.Bus.notify(notification, project);
    }

    /**
     * 通知上传失败信息
     * <p>
     * 根据提供的文件未找到列表和上传失败列表，构建通知内容并发送警告通知。
     *
     * @param fileNotFoundListMap   文件未找到列表映射，键为VirtualFile，值为字符串列表
     * @param uploadFailuredListMap 上传失败列表映射，键为VirtualFile，值为字符串列表
     * @param project               项目对象，用于指定通知的目标项目
     */
    public static void notifyUploadFailure(Map<VirtualFile, List<String>> fileNotFoundListMap,
                                           Map<VirtualFile, List<String>> uploadFailuredListMap,
                                           Project project) {
        StringBuilder content = new StringBuilder();

        StringBuilder fileNotFoundContent = new StringBuilder();
        StringBuilder uploadFailuredContent = new StringBuilder();

        if (!fileNotFoundListMap.isEmpty()) {
            buildContent(fileNotFoundListMap, fileNotFoundContent, MikBundle.message("notification.image.not.found"));
        }

        if (!uploadFailuredListMap.isEmpty()) {
            buildContent(uploadFailuredListMap, uploadFailuredContent, MikBundle.message("notification.image.upload.failed"));
        }
        content.append(fileNotFoundContent).append(uploadFailuredContent);
        Notification notification = new Notification(MIK_NOTIFICATION_GROUP, MikBundle.message("notification.upload.warning"),
                                                     content.toString(), NotificationType.WARNING);
        // 使用 addAction 替代过时的 setListener
        notification.addAction(new NotificationAction(MikBundle.message("notification.open.file")) {
            @Override
            public void actionPerformed(@NotNull AnActionEvent e, @NotNull Notification notification) {
                // Action 点击时，尝试打开第一个可用文件
                if (!fileNotFoundListMap.isEmpty()) {
                    VirtualFile firstFile = fileNotFoundListMap.keySet().iterator().next();
                    try {
                        RevealFileAction.openFile(new File(firstFile.getPath()));
                    } catch (Exception ex) {
                        log.warn("Failed to open file: {}", firstFile.getPath(), ex);
                    }
                }
                notification.expire();
            }
        });
        Notifications.Bus.notify(notification, project);
    }

    /**
     * 根据文件映射关系构建内容字符串
     * <p>
     * 遍历传入的文件与图片列表的映射关系，将每个文件的名称和对应的图片列表
     * 以HTML格式追加到内容构建器中，用于生成带有链接和列表的页面内容。
     *
     * @param listMap 文件与图片列表的映射关系
     * @param content 用于构建内容的字符串构建器
     * @param s       用于插入到链接前的文本
     */
    private static void buildContent(Map<VirtualFile, List<String>> listMap, StringBuilder content, String s) {
        for (Map.Entry<VirtualFile, List<String>> entry : listMap.entrySet()) {
            String fileName = entry.getKey().getName();
            List<String> images = entry.getValue();

            // 使用占位符构建链接行
            String linkTemplate = "<p>%s <a href='%s'>%s %s</a></p>";
            String linkContent = String.format(linkTemplate,
                                               s,  // {0} - 提示文本（如 "Image Not Found:"）
                                               entry.getKey().getUrl(),  // {1} - 文件URL
                                               MikBundle.message("notification.open.file"),  // {2} - "Open File"
                                               fileName);  // {3} - 文件名
            content.append(linkContent);

            // 使用占位符构建列表项
            String itemTemplate = "<p><li>%s</li></p>";
            for (String image : images) {
                content.append(String.format(itemTemplate, image));
            }
        }
    }

    /**
     * 上传完成后发送通知
     * <p>
     * 创建并发送一个上传完成的通知，包含指定内容和标题。
     *
     * @param content 通知内容，可包含HTML标签
     * @since 0.0.1
     */
    public static void notifyUploadFinshed(String content) {
        Notification notification = new Notification(MIK_NOTIFICATION_GROUP, content, NotificationType.INFORMATION);
        notification.setTitle(UPLOAD_FINSHED);
        // 可使用 HTML 标签
        notification.setContent(content);
        Notifications.Bus.notify(notification);
    }

    /**
     * 上传时检测到配置错误并通知用户
     * <p>
     * 当上传过程中发现配置错误时，生成通知内容并发送给用户，引导用户进行配置检查或查看帮助文档。
     *
     * @param project    项目对象，用于指定通知的目标项目
     * @param actionName 操作名称，用于构建通知内容中的链接文本
     * @since 0.0.1
     */
    public static void notifyConfigurableError(Project project, String actionName) {
        // 使用占位符构建完整的通知内容
        String template = "<p><a href=''>%s</a></p><br /><p>%s</p><br /><p>%s <a href='%s'>Help</a></p>";
        String content = String.format(template,
                                       MikBundle.message("notification.configure.action", actionName),  // {0}
                                       MikBundle.message("notification.help.or.configure"),  // {1}
                                       MikBundle.message("notification.may.need.help"),  // {2}
                                       HELP_URL);  // {3}

        Notification notification = new Notification(MIK_NOTIFICATION_GROUP,
                                                     MikBundle.message("notification.configurable.error"),
                                                     content,
                                                     NotificationType.ERROR);
        // 添加设置动作
        notification.addAction(new NotificationAction(MikBundle.message("notification.configure.oss")) {
            @Override
            public void actionPerformed(@NotNull AnActionEvent e, @NotNull Notification notification) {
                ProjectSettingsPage configurable = new ProjectSettingsPage();
                // 打开设置面板
                ShowSettingsUtil.getInstance().editConfigurable(project, configurable);
                notification.expire();
            }
        });
        Notifications.Bus.notify(notification, project);
    }
}
