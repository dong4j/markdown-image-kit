package info.dong4j.idea.plugin.client;

import info.dong4j.idea.plugin.enums.CloudEnum;
import info.dong4j.idea.plugin.settings.MikPersistenComponent;
import info.dong4j.idea.plugin.settings.MikState;
import info.dong4j.idea.plugin.settings.OssState;
import info.dong4j.idea.plugin.settings.oss.TencentOssSetting;
import info.dong4j.idea.plugin.settings.oss.TencentOssState;
import info.dong4j.idea.plugin.util.PasswordManager;
import info.dong4j.idea.plugin.util.StringUtils;
import info.dong4j.idea.plugin.util.TencentCosUtils;

import org.apache.http.util.Asserts;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.InputStream;

import lombok.extern.slf4j.Slf4j;

/**
 * 腾讯云对象存储服务（OSS）客户端实现类
 * <p>
 * 该类用于实现 OssClient 接口，提供腾讯云 OSS 的文件上传功能。主要职责包括：
 * 1. 从持久化配置中初始化 OSS 客户端配置信息；
 * 2. 通过单例模式管理 OSS 客户端实例，确保全局唯一；
 * 3. 支持通过文件流上传文件到腾讯云 OSS；
 * 4. 提供测试上传功能，用于验证配置是否正确；
 * 5. 在上传成功后保存状态信息，用于后续校验或记录。
 * <p>
 * 该类采用单例模式，确保系统中只有一个 OSS 客户端实例。同时，支持通过自定义配置进行上传操作，适用于需要灵活配置的场景。
 *
 * @author dong4j
 * @version 1.0.0
 * @date 2025.10.24
 * @since 1.0.0
 */
@Slf4j
@Client(CloudEnum.TENCENT_CLOUD)
public class TencentOssClient implements OssClient {
    /** 存储对象所属的存储桶名称 */
    private static String bucketName;
    /** 区域名称 */
    private static String regionName;
    /** 访问密钥，用于身份验证和权限控制 */
    private static String accessKey;
    /** 访问密钥，用于身份验证和请求签名 */
    private static String accessSecretKey;

    static {
        init();
    }

    /**
     * 初始化OSS客户端配置
     * <p>
     * 检查是否为首次使用，若为首次使用则从持久化配置中加载OSS相关参数，包括存储桶名称、访问密钥、访问密钥ID和区域名称。
     *
     * @since 0.0.1
     */
    private static void init() {
        TencentOssState tencentOssState = MikPersistenComponent.getInstance().getState().getTencentOssState();
        bucketName = tencentOssState.getBucketName();
        accessKey = tencentOssState.getAccessKey();
        accessSecretKey = PasswordManager.getPassword(TencentOssSetting.CREDENTIAL_ATTRIBUTES);
        regionName = tencentOssState.getRegionName();

    }

    /**
     * 获取腾讯云对象存储服务（OSS）客户端实例
     * <p>
     * 该方法用于获取腾讯云OSS客户端的单例实例。如果实例不存在，则创建一个新的实例并缓存。
     *
     * @return 腾讯云OSS客户端实例
     * @since 0.0.1
     */
    @Contract(pure = true)
    public static TencentOssClient getInstance() {
        TencentOssClient client = (TencentOssClient) OssClient.INSTANCES.get(CloudEnum.TENCENT_CLOUD);
        if (client == null) {
            client = SingletonHandler.SINGLETON;
            OssClient.INSTANCES.put(CloudEnum.TENCENT_CLOUD, client);
        }
        return client;
    }

    /**
     * 单例模式实现类，用于管理腾讯云对象存储服务（OSS）客户端的单例实例
     * <p>
     * 通过静态内部类实现单例模式，确保在多线程环境下安全地创建和获取唯一实例
     * 避免重复创建多个客户端实例，提高资源利用率和性能
     *
     * @author dong4j
     * @version 0.0.1
     * @email "mailto:dong4j@gmail.com"
     * @date 2020.04.22
     * @since 0.0.1
     */
    private static class SingletonHandler {
        /** 单例实例，用于提供全局唯一的腾讯云 OSS 客户端服务 */
        private static final TencentOssClient SINGLETON = new TencentOssClient();
    }

    /**
     * 实现接口，获取当前客户端类型
     * <p>
     * 返回当前客户端所对应的云服务商类型枚举值
     *
     * @return 云服务商类型枚举值
     * @since 0.0.1
     */
    @Override
    public CloudEnum getCloudType() {
        return CloudEnum.TENCENT_CLOUD;
    }

    /**
     * 通过文件流上传文件到腾讯云对象存储服务
     * <p>
     * 使用给定的文件流和文件名，将文件上传至腾讯云 COS 服务。内部调用 TencentCosUtils.putObject 方法完成上传操作。
     *
     * @param inputStream 文件流，用于读取上传的文件内容
     * @param filename    文件名，用于指定上传文件的存储路径和名称
     * @return 上传结果的字符串表示（如成功或错误信息）
     * @throws Exception 上传过程中发生异常时抛出
     * @since 0.0.1
     */
    @Override
    public String upload(InputStream inputStream, String filename) throws Exception {
        // 拼接 url = <BucketName-APPID>.cos.region_name.myqcloud.com/key
        return TencentCosUtils.putObject("/" + filename,
                                         inputStream,
                                         bucketName,
                                         regionName,
                                         accessKey,
                                         accessSecretKey);
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
        TencentOssState tencentOssState = state.getTencentOssState();
        String bucketName = tencentOssState.getBucketName();
        String accessKey = tencentOssState.getAccessKey();
        String secretKey = PasswordManager.getPassword(TencentOssSetting.CREDENTIAL_ATTRIBUTES);
        String regionName = tencentOssState.getRegionName();

        Asserts.notBlank(bucketName, "Bucket");
        Asserts.notBlank(accessKey, "Access Key");
        Asserts.notBlank(secretKey, "Secret Key");
        Asserts.notBlank(regionName, "RegionName");

        return this.upload(inputStream,
                           filename,
                           bucketName,
                           accessKey,
                           secretKey,
                           regionName);
    }

    /**
     * 处理上传按钮点击事件，用于上传文件到腾讯云OSS服务
     * <p>
     * 该方法在用户点击上传按钮后调用，用于将文件上传至指定的OSS存储桶，并在上传成功后保存相关状态信息。
     *
     * @param inputStream 文件输入流，用于读取上传的文件内容
     * @param filename    上传文件的名称
     * @param bucketName  上传文件的目标存储桶名称
     * @param accessKey   访问OSS服务的密钥
     * @param secretKey   访问OSS服务的密钥
     * @param regionName  服务所在的区域名称
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
                         String regionName) throws Exception {

        TencentOssClient.bucketName = bucketName;
        TencentOssClient.regionName = regionName;
        TencentOssClient.accessKey = accessKey;
        TencentOssClient.accessSecretKey = secretKey;

        TencentOssClient tencentOssClient = TencentOssClient.getInstance();

        String url = tencentOssClient.upload(inputStream, filename);

        if (StringUtils.isNotBlank(url)) {
            int hashcode = bucketName.hashCode() +
                           secretKey.hashCode() +
                           accessKey.hashCode() +
                           regionName.hashCode();
            // 更新可用状态
            OssState.saveStatus(MikPersistenComponent.getInstance().getState().getTencentOssState(),
                                hashcode,
                                MikState.OLD_HASH_KEY);
        }
        return url;
    }
}
