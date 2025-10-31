package info.dong4j.idea.plugin.notify;

import com.google.gson.Gson;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.Balloon;

import info.dong4j.idea.plugin.content.MikContents;
import info.dong4j.idea.plugin.entity.HelpResult;
import info.dong4j.idea.plugin.enums.HelpType;
import info.dong4j.idea.plugin.util.IOUtils;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;

/**
 * 通知类，用于处理和展示各种通知信息
 * <p>
 * 该类继承自 Notification，提供了自定义的通知功能，包括帮助链接获取、通知隐藏以及压缩信息通知等操作。
 * 支持通过 HTTP 请求动态获取帮助页面链接，并提供统一的通知展示方式。
 * <p>
 * 该类使用了日志记录和 HTTP 客户端进行网络请求，适用于需要展示用户操作反馈、系统状态信息或帮助文档的场景。
 *
 * @author dong4j
 * @version 0.0.1
 * @date 2021.02.14
 * @since 0.0.1
 */
@SuppressWarnings("jol")
@Slf4j
public class MikNotification extends Notification {
    /** about:blank 是一个特殊的 URI，通常用于表示空的或默认的文档 */
    public static final String ABOUT_BLANK = "about:blank";
    /** 帮助页面的 URL 地址 */
    static final String HELP_URL = helpUrl(HelpType.NOTTIFY.where);
    /** 上传通知组的名称 */
    static final String MIK_NOTIFICATION_GROUP = "MIK Group";
    /** 图片工具通知无组标识 */
    private static final String MIK_NOTIFICATION_NONE_GROUP = "Image Kit Group";

    /**
     * 创建一个新的上传通知对象
     * <p>
     * 该构造函数用于初始化一个上传通知，指定标题、内容和通知类型
     *
     * @param title   通知的标题
     * @param content 通知的内容
     * @param type    通知的类型，用于标识通知的类别
     * @since 0.0.1
     */
    MikNotification(@NotNull String title,
                    @NotNull String content,
                    @NotNull NotificationType type) {
        super(MIK_NOTIFICATION_GROUP, title, content, type);
    }

    /**
     * 获取帮助页面的URL
     * <p>
     * 根据传入的参数构造帮助页面的URL，若参数为"custom"则返回预设的自定义帮助页面地址，否则通过HTTP请求获取帮助信息并返回对应的URL。
     *
     * @param where 指定帮助页面的类型或标识，如"custom"表示自定义帮助页面
     * @return 返回帮助页面的URL字符串
     * @since 0.0.1
     */
    public static String helpUrl(String where) {
        HelpResult helpResult = null;

        HttpClientBuilder builder = HttpClients.custom();
        // 必须设置 UA, 不然会报 403
        builder.setUserAgent("Mozilla/5.0 (Windows; U; Windows NT 6.1; zh-CN; rv:1.9.2.6)");
        CloseableHttpClient client = builder.build();

        HttpGet httpGet = new HttpGet(String.format(MikContents.HELP_REST_URL, where));

        RequestConfig requestConfig = RequestConfig.custom()
            .setConnectTimeout(3000)
            .setConnectionRequestTimeout(1000)
            .setSocketTimeout(3000)
            .build();

        httpGet.setConfig(requestConfig);

        try {
            HttpResponse response = client.execute(httpGet);

            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                byte[] res = EntityUtils.toByteArray(response.getEntity());
                String result = IOUtils.toString(res, StandardCharsets.UTF_8.name());
                helpResult = new Gson().fromJson(result, HelpResult.class);
                log.trace("{}", helpResult);
            }

        } catch (IOException e) {
            log.trace("", e);
        } finally {
            try {
                client.close();
            } catch (IOException ignored) {
            }
        }
        if (helpResult == null) {
            return ABOUT_BLANK;
        }

        return helpResult.getUrl();
    }

    /**
     * 隐藏气球
     * <p>
     * 如果气球对象不为 null，则调用其 hide 方法进行隐藏操作
     *
     * @param balloon 气球对象
     * @since 0.0.1
     */
    static void hideBalloon(Balloon balloon) {
        if (balloon != null) {
            balloon.hide();
        }
    }

    /**
     * 通知压缩信息完成情况
     * <p>
     * 将压缩信息转换为HTML格式，并通过通知系统显示给用户
     *
     * @param project      项目对象，用于指定通知的目标
     * @param compressInfo 压缩信息的键值对集合
     * @since 0.0.1
     */
    public static void notifyCompressInfo(Project project, Map<String, String> compressInfo) {
        StringBuilder content = new StringBuilder();
        for (Map.Entry<String, String> entry : compressInfo.entrySet()) {
            content.append("<p>").append(entry.getKey()).append("\t\t\t").append(entry.getValue()).append("</p>");
        }
        //noinspection DialogTitleCapitalization
        Notifications.Bus.notify(new Notification(MIK_NOTIFICATION_NONE_GROUP,
                                                  "<p>Compress Finished: </p>",
                                                  content.toString(),
                                                  NotificationType.INFORMATION), project);

    }
}
