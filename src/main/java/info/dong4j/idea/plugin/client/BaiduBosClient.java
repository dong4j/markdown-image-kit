package info.dong4j.idea.plugin.client;

import info.dong4j.idea.plugin.enums.CloudEnum;
import info.dong4j.idea.plugin.settings.MikPersistenComponent;
import info.dong4j.idea.plugin.settings.oss.AbstractExtendOssState;
import info.dong4j.idea.plugin.settings.oss.BaiduBosSetting;
import info.dong4j.idea.plugin.settings.oss.BaiduBosState;
import info.dong4j.idea.plugin.util.BaiduBosUtils;
import info.dong4j.idea.plugin.util.PasswordManager;
import info.dong4j.idea.plugin.util.StringUtils;

import org.jetbrains.annotations.Contract;

import java.io.InputStream;

import lombok.extern.slf4j.Slf4j;

/**
 * 百度云对象存储客户端
 * <p>
 * 该类用于封装百度云对象存储服务的客户端操作，继承自抽象类 AbstractOssClient，提供与百度云相关的文件上传、存储等核心功能。
 * 支持通过单例模式获取实例，确保全局唯一性，并通过配置信息初始化访问密钥、端点等参数。
 * <p>
 * 采用静态内部类 SingletonHandler 实现单例模式，保证线程安全且实现懒加载。
 * <p>
 * 该类主要依赖 MikPersistenComponent 获取配置状态，用于初始化访问密钥、端点和文件目录等信息。
 *
 * @author dong4j
 * @version 1.0.0
 * @date 2025.10.24
 * @since 1.0.0
 */
@Slf4j
@Client(CloudEnum.BAIDU_CLOUD)
public class BaiduBosClient extends AbstractOssClient {

    static {
        init();
    }

    /**
     * 初始化OSS客户端配置信息
     * <p>
     * 该方法用于在首次使用时初始化OSS相关的访问密钥、端点和文件目录等配置参数。
     * 如果ossClient为null，则执行初始化操作。
     *
     * @since 0.0.1
     */
    private static void init() {
        BaiduBosState baiduBosState = MikPersistenComponent.getInstance().getState().getBaiduBosState();
        endpoint = baiduBosState.getEndpoint();
        accessKey = baiduBosState.getAccessKey();
        accessSecretKey = PasswordManager.getPassword(BaiduBosSetting.CREDENTIAL_ATTRIBUTES);
        bucketName = baiduBosState.getBucketName();
        String tempFileDir = baiduBosState.getFiledir();
        filedir = StringUtils.isBlank(tempFileDir) ? "" : tempFileDir + "/";
        customEndpoint = baiduBosState.getCustomEndpoint();
        isCustomEndpoint = baiduBosState.getIsCustomEndpoint();
    }

    /**
     * 获取百度云对象存储服务的客户端实例
     * <p>
     * 该方法通过静态内部类实现单例模式，确保客户端实例的唯一性。
     * 实现方式保证了线程安全，并且支持懒加载，只有在首次调用时才会初始化实例。
     *
     * @return 百度云对象存储服务的客户端实例
     * @since 0.0.1
     */
    @Contract(pure = true)
    public static BaiduBosClient getInstance() {
        BaiduBosClient client = (BaiduBosClient) OssClient.INSTANCES.get(CloudEnum.BAIDU_CLOUD);
        if (client == null) {
            client = BaiduBosClient.SingletonHandler.SINGLETON;
            OssClient.INSTANCES.put(CloudEnum.BAIDU_CLOUD, client);
        }
        return client;
    }

    /**
     * 单例模式处理类
     * <p>
     * 用于确保 BaiduBosClient 实例在整个应用中只被创建一次，通过静态内部类实现线程安全的单例模式。
     * 提供了一个全局访问点，方便在不同模块中复用同一个客户端实例，避免重复创建带来的资源浪费。
     *
     * @author dong4j
     * @version 0.0.1
     * @email mailto:dong4j@gmail.com
     * @date 2020.04.25
     * @since 0.0.1
     */
    private static class SingletonHandler {
        /** 单例模式下的百度云对象存储服务客户端实例 */
        private static final BaiduBosClient SINGLETON = new BaiduBosClient();
    }

    /**
     * 获取云类型
     * <p>
     * 返回当前系统所使用的云类型枚举值
     *
     * @return 云类型枚举值
     * @since 0.0.1
     */
    @Override
    public CloudEnum getCloudType() {
        return CloudEnum.BAIDU_CLOUD;
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
     * 将对象上传到指定的存储桶
     * <p>
     * 通过给定的键和输入流，将数据上传至百度云对象存储服务（BOS）
     *
     * @param key      存储对象的键（Key）
     * @param instream 要上传的数据输入流
     * @throws Exception 上传过程中发生异常时抛出
     * @since 1.1.0
     */
    @Override
    protected void putObjects(String key, InputStream instream) throws Exception {
        BaiduBosUtils.putObject(key,
                                instream,
                                bucketName,
                                endpoint,
                                accessKey,
                                accessSecretKey,
                                isCustomEndpoint,
                                customEndpoint);
    }

    /**
     * 获取状态信息
     * <p>
     * 返回当前组件的状态对象，该状态对象用于表示与百度对象存储（BOS）相关的状态信息。
     *
     * @return 当前组件的抽象状态对象
     * @since 1.1.0
     */
    @Override
    protected AbstractExtendOssState getState() {
        return MikPersistenComponent.getInstance().getState().getBaiduBosState();
    }
}
