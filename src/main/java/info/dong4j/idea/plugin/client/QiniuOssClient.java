package info.dong4j.idea.plugin.client;

import info.dong4j.idea.plugin.enums.CloudEnum;
import info.dong4j.idea.plugin.enums.ZoneEnum;
import info.dong4j.idea.plugin.settings.MikPersistenComponent;
import info.dong4j.idea.plugin.settings.MikState;
import info.dong4j.idea.plugin.settings.OssState;
import info.dong4j.idea.plugin.settings.oss.QiniuOssSetting;
import info.dong4j.idea.plugin.settings.oss.QiniuOssState;
import info.dong4j.idea.plugin.util.EnumsUtils;
import info.dong4j.idea.plugin.util.PasswordManager;
import info.dong4j.idea.plugin.util.QiniuOssUtils;
import info.dong4j.idea.plugin.util.StringUtils;

import org.apache.http.util.Asserts;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Optional;

import lombok.extern.slf4j.Slf4j;

/**
 * 七牛云对象存储客户端实现类
 * <p>
 * 该类用于封装七牛云对象存储服务的客户端操作，支持上传文件功能，并提供与七牛云配置相关的属性获取和初始化逻辑。
 * 实现了 OssClient 接口，用于在不同云服务中统一调用对象存储功能。
 * <p>
 * 包含静态初始化逻辑，用于从配置中加载七牛云的端点、主机、访问密钥、秘密密钥和存储桶名称等信息。
 * 支持通过 getInstance 方法获取单例实例，确保配置信息的统一性和一致性。
 * <p>
 * 通过 upload 方法实现文件上传功能，支持两种调用方式：一种是直接使用默认配置，另一种是通过传入自定义配置参数进行上传。
 *
 * @author dong4j
 * @version 1.0.0
 * @date 2025.10.24
 * @since 1.0.0
 */
@Slf4j
@Client(CloudEnum.QINIU_CLOUD)
public class QiniuOssClient implements OssClient {
    /** 系统默认的过期时间，单位为毫秒，表示 10 年的毫秒数 */
    private static final long DEAD_LINE = 3600L * 1000 * 24 * 365 * 10;
    /** 服务端点地址 */
    private static String endpoint;
    /** 服务器主机地址 */
    private static String host;
    /** 访问密钥，用于身份验证和权限控制 */
    private static String accessKey;
    /** 秘钥，用于加密或验证操作 */
    private static String secretKey;
    /** 存储对象存储服务的存储桶名称 */
    private static String bucketName;

    static {
        init();
    }

    /**
     * 初始化OSS客户端配置信息
     * <p>
     * 用于在首次使用时初始化七牛云OSS的相关配置参数，包括端点、访问密钥、存储桶名称等。
     * 如果ossClient为null时调用此方法进行初始化。
     *
     * @since 0.0.1
     */
    private static void init() {
        QiniuOssState qiniuOssState = MikPersistenComponent.getInstance().getState().getQiniuOssState();
        endpoint = qiniuOssState.getEndpoint();
        accessKey = qiniuOssState.getAccessKey();
        secretKey = PasswordManager.getPassword(QiniuOssSetting.CREDENTIAL_ATTRIBUTES);
        bucketName = qiniuOssState.getBucketName();

        Optional<ZoneEnum> zone = EnumsUtils.getEnumObject(ZoneEnum.class, e -> e.getIndex() == qiniuOssState.getZoneIndex());
        host = zone.orElse(ZoneEnum.EAST_CHINA).host;
    }

    /**
     * 获取云类型
     * <p>
     * 返回当前配置的云类型枚举值，固定为七牛云
     *
     * @return 云类型枚举值
     * @since 0.0.1
     */
    @Override
    public CloudEnum getCloudType() {
        return CloudEnum.QINIU_CLOUD;
    }

    /**
     * 获取 QiniuOssClient 实例
     * <p>
     * 该方法用于获取 QiniuOssClient 的单例实例，若实例不存在则创建并缓存。
     *
     * @return QiniuOssClient 实例
     * @since 0.0.1
     */
    @Contract(pure = true)
    public static QiniuOssClient getInstance() {
        QiniuOssClient client = (QiniuOssClient) OssClient.INSTANCES.get(CloudEnum.QINIU_CLOUD);
        if (client == null) {
            client = SingletonHandler.SINGLETON;
            OssClient.INSTANCES.put(CloudEnum.QINIU_CLOUD, client);
        }
        return client;
    }

    /**
     * 单例模式处理类
     * <p>
     * 用于实现 QiniuOssClient 的单例模式，确保在整个应用中只存在一个 QiniuOssClient 实例。
     * 通过静态内部类的方式实现延迟加载和线程安全。
     * </p>
     *
     * @author dong4j
     * @version 0.0.1
     * @email mailto:dong4j@gmail.com
     * @date 2021.02.14
     * @since 0.0.1
     */
    private static class SingletonHandler {
        /** 单例模式实例，用于提供全局唯一的 QiniuOssClient 对象 */
        private static final QiniuOssClient SINGLETON = new QiniuOssClient();
    }

    /**
     * 上传字符串内容到指定文件路径，并返回文件的访问路径
     * <p>
     * 该方法通过七牛云OSS工具将输入流中的内容上传至指定文件名的路径，并构建文件的访问URL返回。
     *
     * @param inputStream 输入流，包含要上传的数据内容
     * @param filename    文件名，用于标识上传的文件
     * @return 文件的访问路径
     * @throws Exception 上传过程中发生异常时抛出
     */
    @Override
    public String upload(InputStream inputStream, String filename) throws Exception {
        QiniuOssUtils.putObject(filename, inputStream, bucketName, host, accessKey, secretKey);

        URL url;
        try {
            url = new URI(endpoint).toURL();
        } catch (MalformedURLException | URISyntaxException e) {
            throw new IOException("Invalid URL: " + endpoint, e);
        }
        log.debug("getUserInfo = {}", url.getUserInfo());
        if (StringUtils.isBlank(url.getPath())) {
            endpoint = endpoint + "/";
        } else {
            endpoint = endpoint.endsWith("/") ? endpoint : endpoint + "/";
        }
        return endpoint + filename;
    }

    /**
     * "Upload Test" 按钮测试上传（新接口）
     * <p>
     * 该方法用于执行"Upload Test"按钮的反射调用，接收输入流、文件名和MikState作为参数，从state中获取最新配置并执行上传。
     * 这是新的测试接口，优先使用此接口进行测试上传。
     *
     * @param inputStream 输入流，用于读取上传文件的数据
     * @param filename    文件名，表示上传文件的名称
     * @param state       MikState对象，包含所有配置状态信息
     * @return 处理结果字符串
     * @throws Exception 通用异常，用于封装可能发生的各种错误
     * @since 2.0.0
     */
    @Override
    public String upload(InputStream inputStream, String filename, MikState state) throws Exception {
        QiniuOssState qiniuOssState = state.getQiniuOssState();
        int zoneIndex = qiniuOssState.getZoneIndex();
        String bucketName = qiniuOssState.getBucketName();
        String accessKey = qiniuOssState.getAccessKey();
        String secretKey = PasswordManager.getPassword(QiniuOssSetting.CREDENTIAL_ATTRIBUTES);
        String endpoint = qiniuOssState.getEndpoint();

        Asserts.notBlank(bucketName, "Bucket");
        Asserts.notBlank(accessKey, "Access Key");
        Asserts.notBlank(secretKey, "Secret Key");
        Asserts.notBlank(endpoint, "Domain");

        return this.upload(inputStream,
                           filename,
                           bucketName,
                           accessKey,
                           secretKey,
                           endpoint,
                           zoneIndex);
    }

    /**
     * 处理上传按钮点击事件，用于上传文件到七牛云存储
     * <p>
     * 该方法用于在用户点击上传按钮后，将文件上传至指定的七牛云存储桶。支持通过输入流上传文件，并在上传成功后保存客户端信息。
     * <p>
     * 上传完成后，若返回的URL不为空，则根据相关参数计算哈希值，并更新存储状态。
     *
     * @param inputStream 文件的输入流
     * @param filename    文件名
     * @param bucketName  七牛云存储桶名称
     * @param accessKey   七牛云访问密钥
     * @param secretKey   七牛云密钥
     * @param endpoint    七牛云服务端点
     * @param zoneIndex   区域索引，用于选择七牛云区域
     * @return 上传成功后返回的文件URL
     * @throws Exception 上传过程中发生异常时抛出
     */
    @NotNull
    @Contract(pure = true)
    public String upload(InputStream inputStream,
                         String filename,
                         String bucketName,
                         String accessKey,
                         String secretKey,
                         String endpoint,
                         int zoneIndex) throws Exception {

        Optional<ZoneEnum> zone = EnumsUtils.getEnumObject(ZoneEnum.class, e -> e.getIndex() == zoneIndex);
        QiniuOssClient.host = zone.orElse(ZoneEnum.EAST_CHINA).host;
        QiniuOssClient.bucketName = bucketName;
        QiniuOssClient.accessKey = accessKey;
        QiniuOssClient.secretKey = secretKey;
        QiniuOssClient.endpoint = endpoint;

        QiniuOssClient client = QiniuOssClient.getInstance();

        String url = client.upload(inputStream, filename);

        if (StringUtils.isNotBlank(url)) {
            int hashcode = bucketName.hashCode() +
                           accessKey.hashCode() +
                           secretKey.hashCode() +
                           endpoint.hashCode() +
                           zoneIndex;
            // 更新可用状态
            OssState.saveStatus(MikPersistenComponent.getInstance().getState().getQiniuOssState(),
                                hashcode,
                                MikState.OLD_HASH_KEY);
        }
        return url;
    }

}
