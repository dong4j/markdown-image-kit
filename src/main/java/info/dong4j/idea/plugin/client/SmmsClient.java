package info.dong4j.idea.plugin.client;

import com.google.gson.Gson;

import info.dong4j.idea.plugin.entity.SmmsResult;
import info.dong4j.idea.plugin.enums.CloudEnum;
import info.dong4j.idea.plugin.util.IOUtils;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.jetbrains.annotations.Contract;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import javax.swing.JPanel;

import lombok.extern.slf4j.Slf4j;

/**
 * Smms 客户端实现类
 * <p>
 * 用于与 sm.ms 图床服务进行交互，提供图片上传功能。该类实现了 OssClient 接口，支持通过 HTTP 协议上传图片文件到 sm.ms 服务。
 * 支持两种上传方式：一种是标准的图片上传接口，另一种是用于特定场景的扩展上传方式（目前返回空字符串）。
 * <p>
 * 该类使用单例模式确保全局唯一实例，并通过 HttpClient 实现 HTTP 请求。
 *
 * @author dong4j
 * @version 0.0.1
 * @date 2021.02.14
 * @since 0.0.1
 */
@Slf4j
@Client(CloudEnum.SM_MS_CLOUD)
public class SmmsClient implements OssClient {
    /** 上传图片的固定 URL 地址 */
    private static final String UPLOAD_URL = "https://sm.ms/api/v2/upload";
    /** 客户端实例，用于执行网络请求 */
    private static Client client;

    /**
     * Smms client
     * <p>
     * 用于创建 Smms 客户端实例，提供与 Smms 服务交互的能力
     *
     * @since 0.0.1
     */
    @Contract(pure = true)
    private SmmsClient() {

    }

    /**
     * 获取云类型
     * <p>
     * 返回当前系统的云类型枚举值
     *
     * @return 云类型枚举值
     * @since 0.0.1
     */
    @Override
    public CloudEnum getCloudType() {
        return CloudEnum.SM_MS_CLOUD;
    }

    /**
     * 获取 SmmsClient 实例
     * <p>
     * 该方法用于获取 SmmsClient 的单例实例，若实例不存在则创建并缓存。
     *
     * @return SmmsClient 实例
     * @since 0.0.1
     */
    @Contract(pure = true)
    public static SmmsClient getInstance() {
        SmmsClient client = (SmmsClient) OssClient.INSTANCES.get(CloudEnum.SM_MS_CLOUD);
        if (client == null) {
            client = SingletonHandler.SINGLETON;
            OssClient.INSTANCES.put(CloudEnum.SM_MS_CLOUD, client);
        }
        return client;
    }

    /**
     * 单例处理器类
     * <p>
     * 用于管理 SmmsClient 的单例实例，确保在整个应用中只有一个 SmmsClient 对象被创建和使用。
     * 该类通过静态内部类的方式实现单例模式，具有线程安全性和延迟加载的优点。
     *
     * @author dong4j
     * @version 0.0.1
     * @date 2021.02.14
     * @email mailto:dong4j@gmail.com
     * @since 0.0.1
     */
    private static class SingletonHandler {
        /** 单例实例，用于提供全局唯一的 SmmsClient 实例 */
        private static final SmmsClient SINGLETON = new SmmsClient();
    }

    /**
     * 上传字符串数据
     * <p>
     * 将输入流中的字符串数据上传，并返回上传结果字符串
     *
     * @param inputStream 输入流，包含要上传的数据
     * @param fileName    文件名，用于标识上传的文件
     * @return 上传后的字符串结果
     * @throws Exception 上传过程中发生异常时抛出
     */
    @Override
    public String upload(InputStream inputStream, String fileName) throws Exception {
        if (client == null) {
            client = new Client();
        }
        return client.upload(inputStream, fileName);
    }

    /**
     * 上传文件功能，用于处理图片上传逻辑
     * <p>
     * 该方法用于执行文件上传操作，接收输入流、文件名和面板参数，返回上传结果字符串
     *
     * @param inputStream 输入流，用于读取上传的文件数据
     * @param fileName    文件名，表示上传的文件名称
     * @param jPanel      面板对象，可能用于界面交互或显示上传状态
     * @return 上传结果字符串，表示上传操作的返回信息
     * @throws Exception 上传过程中可能抛出的异常
     */
    @Override
    public String upload(InputStream inputStream, String fileName, JPanel jPanel) throws Exception {
        return "";
    }

    /**
     * 客户端工具类
     * <p>
     * 提供 HTTP 请求相关的客户端功能，主要用于文件上传等网络操作。内部使用 Apache HttpClient 实现，支持设置 User-Agent 并执行 POST 请求。
     * <p>
     * 该类封装了 HTTP 请求的细节，简化了网络请求的使用，适用于需要与外部服务进行数据交互的场景。
     *
     * @author dong4j
     * @version 0.0.1
     * @date 2021.02.14
     * @since 0.0.1
     */
    private static class Client {
        /** HTTP 客户端实例，用于发送 HTTP 请求并管理连接 */
        private final CloseableHttpClient client;

        /**
         * 初始化一个新的 Client 实例
         * <p>
         * 构造函数用于创建 Client 对象，并配置 HttpClient 实例。必须设置 User-Agent，否则会返回 403 错误。
         *
         * @since 0.0.1
         */
        Client() {
            HttpClientBuilder builder = HttpClients.custom();
            // 必须设置 UA, 不然会报 403
            builder.setUserAgent("Mozilla/5.0 (Windows; U; Windows NT 6.1; zh-CN; rv:1.9.2.6)");
            this.client = builder.build();
        }

        /**
         * 通过 HTTP 接口直接上传文件
         * <p>
         * 使用 MultipartEntityBuilder 构建请求实体，发送 POST 请求到指定的上传地址，并处理返回结果。
         * 如果上传成功且返回数据中包含 URL，则返回该 URL；否则返回错误信息。
         *
         * @param inputStream 文件输入流
         * @param fileName    文件名
         * @return 上传成功时返回文件 URL，失败时返回错误信息或空字符串
         * @throws Exception 上传过程中发生异常时抛出
         * @since 0.0.1
         */
        public String upload(InputStream inputStream, String fileName) throws Exception {
            HttpEntity reqEntity = MultipartEntityBuilder.create()
                .addBinaryBody("smfile", inputStream, ContentType.DEFAULT_BINARY, fileName)
                .build();

            HttpPost post = new HttpPost(UPLOAD_URL);
            post.setEntity(reqEntity);

            HttpResponse response = this.client.execute(post);

            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                byte[] res = EntityUtils.toByteArray(response.getEntity());
                String result = IOUtils.toString(res, StandardCharsets.UTF_8.name());
                SmmsResult smmsResult = new Gson().fromJson(result, SmmsResult.class);
                log.trace("{}", smmsResult);
                if (smmsResult.getData() == null) {
                    return smmsResult.getMessage();
                }
                return smmsResult.getData().getUrl();
            }
            return "";
        }
    }
}
