package info.dong4j.idea.plugin.client;

import info.dong4j.idea.plugin.enums.CloudEnum;
import info.dong4j.idea.plugin.settings.MikPersistenComponent;
import info.dong4j.idea.plugin.settings.MikState;
import info.dong4j.idea.plugin.settings.OssState;
import info.dong4j.idea.plugin.settings.oss.SmmsOssState;
import info.dong4j.idea.plugin.util.SmmsUtils;
import info.dong4j.idea.plugin.util.StringUtils;

import org.apache.http.util.Asserts;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.InputStream;
import java.util.Map;

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
    /** 客户端实例，用于执行网络请求 */
    private static Client client;
    /** 上传文件的 API 地址 */
    private static final String API = "https://sm.ms/api/v2/upload";
    private String token;

    /**
     * 获取 CustomOssClient 实例
     * <p>
     * 该方法用于获取自定义 OSS 客户端实例，若实例不存在则创建并缓存。
     *
     * @return 自定义 OSS 客户端实例
     * @since 1.5.0
     */
    @Contract(pure = true)
    public static SmmsClient getInstance() {
        SmmsClient client = (SmmsClient) OssClient.INSTANCES.get(CloudEnum.SM_MS_CLOUD);
        if (client == null) {
            client = SmmsClient.SingletonHandler.SINGLETON;
            OssClient.INSTANCES.put(CloudEnum.SM_MS_CLOUD, client);
        }
        return client;
    }

    /**
     * 单例处理类
     * <p>
     * 用于管理 CustomOssClient 的单例实例，通过缓存的 map 映射获取已初始化的 client，避免创建多个实例，确保全局唯一性。
     *
     * @author dong4j
     * @version 1.5.0
     * @date 2020.04.22
     * @since 1.5.0
     */
    private static class SingletonHandler {
        /** 单例模式实例，用于提供全局唯一的 CustomOssClient 实例 */
        private static final SmmsClient SINGLETON = new SmmsClient();
    }

    /**
     * 初始化OSS客户端相关配置
     * <p>
     * 如果是第一次使用，ossClient为null，此时会通过持久化配置进行初始化。
     * 若是第一次设置，获取的持久化配置为null，则初始化ossClient失败。
     *
     * @since 1.5.0
     */
    private void init() {
        SmmsOssState state = MikPersistenComponent.getInstance().getState().getSmmsOssState();
        this.token = state.getToken();
    }

    /**
     * 实现接口，获取当前客户端类型
     * <p>
     * 返回当前客户端所对应的云类型枚举值
     *
     * @return 云类型枚举值
     * @since 1.5.0
     */
    @Override
    public CloudEnum getCloudType() {
        return CloudEnum.SM_MS_CLOUD;
    }

    /**
     * 通过文件流上传文件
     * <p>
     * 使用输入流将文件上传至对象存储服务，并解析返回结果获取文件访问URL。
     *
     * @param inputStream 文件输入流，用于读取上传文件的内容
     * @param fileName    文件名，用于标识上传的文件
     * @return 上传成功后返回的文件访问URL
     * @throws Exception 上传过程中发生异常时抛出
     * @since 1.5.0
     */
    @Override
    public String upload(InputStream inputStream, String fileName) throws Exception {
        // 确保配置已初始化
        if (StringUtils.isBlank(token)) {
            init();
        }

        return SmmsUtils.putObject(API,
                                   this.token,
                                   fileName,
                                   inputStream);

    }

    /**
     * 在设置界面点击 'Test' 按钮上传时调用，用于获取当前配置并执行上传操作
     * <p>
     * 该方法通过传入的 JPanel 获取配置信息，包括 API 地址、请求 key、响应 URL 路径和 HTTP 方法，然后调用 upload 方法执行上传逻辑。
     *
     * @param inputStream 输入流，用于读取上传文件的内容
     * @param fileName    文件名，表示上传的文件名称
     * @param jPanel      JPanel 对象，用于获取当前界面配置信息
     * @return 上传操作的结果字符串
     * @throws Exception 上传过程中发生异常时抛出
     * @since 1.5.0
     */
    @Override
    public String upload(InputStream inputStream, String fileName, JPanel jPanel) throws Exception {
        Map<String, String> map = this.getTestFieldText(jPanel);
        String token = map.get("token");

        Asserts.notBlank(token, "Token 不能为空");

        return this.upload(token, inputStream, fileName);
    }

    /**
     * 处理测试按钮点击事件后的上传请求，成功后保留 client 信息，用于 paste 或右键上传时使用
     * <p>
     * 该方法接收上传所需的输入流、文件名、API、请求键、响应URL路径和HTTP方法，设置 CustomOssClient 相关参数，并执行上传操作。
     * 若上传成功且返回的URL不为空，则根据传入的API、请求键、响应URL路径和HTTP方法计算哈希值，并更新OSS状态。
     *
     * @param inputStream 上传的输入流
     * @param fileName    文件名
     * @return 上传成功后的URL字符串，若上传失败或无返回则可能为空
     * @throws Exception 上传过程中发生异常时抛出
     * @since 1.5.0
     */
    @NotNull
    @Contract(pure = true)
    public String upload(String token, InputStream inputStream,
                         String fileName) throws Exception {

        this.token = token;
        String url = this.upload(inputStream, fileName);

        if (StringUtils.isNotBlank(url)) {
            int hashcode = token.hashCode();
            // 更新可用状态
            OssState.saveStatus(MikPersistenComponent.getInstance().getState().getSmmsOssState(),
                                hashcode,
                                MikState.OLD_HASH_KEY);
        }
        return url;
    }
}
