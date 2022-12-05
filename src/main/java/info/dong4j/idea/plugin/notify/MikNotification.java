package info.dong4j.idea.plugin.notify;

import com.google.gson.Gson;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.Balloon;

import info.dong4j.idea.plugin.MikBundle;
import info.dong4j.idea.plugin.entity.HelpResult;
import info.dong4j.idea.plugin.enums.HelpType;
import info.dong4j.idea.plugin.util.IOUtils;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
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
 * <p>Description: </p>
 *
 * @author dong4j
 * @version 0.0.1
 * @email "mailto:dong4j@gmail.com"
 * @date 2021.02.14 18:40
 * @since 0.0.1
 */
@SuppressWarnings("jol")
@Slf4j
public class MikNotification extends Notification {
    /** CUSTOM_OSS_API_DEMO */
    public static final String CUSTOM_OSS_API_DEMO = "https://github.com/dong4j/mik-help";
    /** ABOUT_BLANK */
    public static final String ABOUT_BLANK = "about:blank";
    /**
     * The Help url.
     */
    static final String HELP_URL = helpUrl(HelpType.NOTTIFY.where);
    /**
     * The Upload notification group.
     */
    static final String MIK_NOTIFICATION_GROUP = "MIK Group";
    /** MIK_NOTIFICATION_NONE_GROUP */
    private static final String MIK_NOTIFICATION_NONE_GROUP = "Image Kit Group";

    /**
     * Instantiates a new Upload notification.
     *
     * @param title   the title
     * @param content the content
     * @param type    the type
     * @since 0.0.1
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
     * @since 0.0.1
     */
    public static String helpUrl(String where) {
        if ("custom".equals(where)) {
            return CUSTOM_OSS_API_DEMO;
        }
        HelpResult helpResult = null;

        HttpClientBuilder builder = HttpClients.custom();
        // 必须设置 UA, 不然会报 403
        builder.setUserAgent("Mozilla/5.0 (Windows; U; Windows NT 6.1; zh-CN; rv:1.9.2.6)");
        CloseableHttpClient client = builder.build();

        HttpPost httpPost = new HttpPost(MikBundle.message("mik.help.rest.url") + where);

        RequestConfig requestConfig = RequestConfig.custom()
            .setConnectTimeout(3000).setConnectionRequestTimeout(1000)
            .setSocketTimeout(3000).build();

        httpPost.setConfig(requestConfig);

        try {
            HttpResponse response = client.execute(httpPost);

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
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (helpResult == null) {
            return ABOUT_BLANK;
        }

        return helpResult.getUrl();
    }

    /**
     * Hide balloon.
     *
     * @param balloon the balloon
     * @since 0.0.1
     */
    static void hideBalloon(Balloon balloon) {
        if (balloon != null) {
            balloon.hide();
        }
    }

    /**
     * Notify compress info.
     *
     * @param project      the project
     * @param compressInfo compress info
     * @since 0.0.1
     */
    public static void notifyCompressInfo(Project project, Map<String, String> compressInfo) {
        StringBuilder content = new StringBuilder();
        for (Map.Entry<String, String> entry : compressInfo.entrySet()) {
            content.append("<p>").append(entry.getKey()).append("\t\t\t").append(entry.getValue()).append("</p>");
        }
        Notifications.Bus.notify(new Notification(MIK_NOTIFICATION_NONE_GROUP,
                                                  "<p>Compress Finished: </p>",
                                                  content.toString(),
                                                  NotificationType.INFORMATION), project);

    }
}
