package info.dong4j.idea.plugin.client;

import info.dong4j.idea.plugin.enums.CloudEnum;
import info.dong4j.idea.plugin.settings.MikPersistenComponent;
import info.dong4j.idea.plugin.settings.oss.AbstractExtendOssState;
import info.dong4j.idea.plugin.settings.oss.AliyunOssSetting;
import info.dong4j.idea.plugin.settings.oss.AliyunOssState;
import info.dong4j.idea.plugin.util.AliyunOssUtils;
import info.dong4j.idea.plugin.util.PasswordManager;
import info.dong4j.idea.plugin.util.StringUtils;

import org.jetbrains.annotations.Contract;

import java.io.InputStream;

import lombok.extern.slf4j.Slf4j;

/**
 * 阿里云OSS客户端类
 * <p>
 * 该类用于封装阿里云对象存储服务（OSS）的客户端操作，支持初始化、获取客户端实例、上传对象等功能。
 * 实现了单例模式，确保系统中只有一个阿里云OSS客户端实例，避免重复初始化和资源浪费。
 * 通过静态内部类SingletonHandler实现线程安全的懒加载单例模式，保证在首次使用时才初始化实例。
 * 支持从配置中读取访问密钥、端点等信息，用于构建OSS客户端连接。
 *
 * @author dong4j
 * @version 1.1.0
 * @date 2025.10.24
 * @since 0.0.1
 */
@Slf4j
@Client(CloudEnum.ALIYUN_CLOUD)
public class AliyunOssClient extends AbstractOssClient {

    static {
        init();
    }

    /**
     * 初始化OSS客户端相关配置信息
     * <p>
     * 该方法用于在首次使用时初始化OSS客户端所需的访问密钥、端点和文件目录等配置信息。
     *
     * @since 0.0.1
     */
    private static void init() {
        AliyunOssState aliyunOssState = MikPersistenComponent.getInstance().getState().getAliyunOssState();
        accessKey = aliyunOssState.getAccessKey();
        accessSecretKey = PasswordManager.getPassword(AliyunOssSetting.CREDENTIAL_ATTRIBUTES);
        endpoint = aliyunOssState.getEndpoint();
        String tempFileDir = aliyunOssState.getFiledir();
        filedir = StringUtils.isBlank(tempFileDir) ? "" : tempFileDir + "/";
    }

    /**
     * 获取阿里云OSS客户端单例实例
     * <p>
     * 通过静态内部类实现单例模式，确保线程安全且支持懒加载。该方法返回阿里云OSS客户端的唯一实例。
     *
     * @return 阿里云OSS客户端实例
     * @since 0.0.1
     */
    @Contract(pure = true)
    public static AliyunOssClient getInstance() {
        AliyunOssClient client = (AliyunOssClient) OssClient.INSTANCES.get(CloudEnum.ALIYUN_CLOUD);
        if (client == null) {
            client = SingletonHandler.SINGLETON;
            OssClient.INSTANCES.put(CloudEnum.ALIYUN_CLOUD, client);
        }
        return client;
    }

    /**
     * 获取OSS客户端实例
     * <p>
     * 返回当前配置的OSS客户端对象，用于与对象存储服务进行交互
     *
     * @return OSS客户端实例
     * @since 1.1.0
     */
    @Override
    protected AbstractOssClient getClient() {
        return getInstance();
    }

    /**
     * 将对象上传到阿里云OSS存储服务
     * <p>
     * 通过阿里云OSS SDK将指定的输入流中的对象以指定的键值存储到指定的存储桶中
     *
     * @param key      存储对象的键值
     * @param instream 要上传的输入流
     * @throws Exception 上传过程中发生异常时抛出
     * @since 1.1.0
     */
    @Override
    protected void putObjects(String key, InputStream instream) throws Exception {
        AliyunOssUtils.putObject(key,
                                 instream,
                                 bucketName,
                                 endpoint,
                                 accessKey,
                                 accessSecretKey,
                                 isCustomEndpoint,
                                 customEndpoint);
    }

    /**
     * 获取当前组件的状态信息
     * <p>
     * 通过获取MikPersistenComponent单例实例，进而获取其状态对象，并返回Aliyun Oss相关的状态信息。
     *
     * @return 当前Aliyun Oss组件的状态对象
     * @since 1.1.0
     */
    @Override
    protected AbstractExtendOssState getState() {
        return MikPersistenComponent.getInstance().getState().getAliyunOssState();
    }

    /**
     * 单例模式处理类
     * <p>
     * 使用静态内部类实现单例模式，确保 AliyunOssClient 实例的唯一性，避免重复创建多个实例。
     * 通过静态内部类的加载机制保证线程安全，并在首次使用时初始化实例。
     *
     * @author dong4j
     * @version 0.0.1
     * @email "mailto:dong4j@gmail.com"
     * @date 2020.04.25
     * @since 0.0.1
     */
    private static class SingletonHandler {
        /** 单例模式实例，用于提供全局唯一的 AliyunOssClient 实例 */
        private static final AliyunOssClient SINGLETON = new AliyunOssClient();
    }

    /**
     * 获取云类型
     * <p>
     * 返回当前配置的云类型枚举值，固定为阿里云
     *
     * @return 云类型枚举值
     * @since 0.0.1
     */
    @Override
    public CloudEnum getCloudType() {
        return CloudEnum.ALIYUN_CLOUD;
    }

}
