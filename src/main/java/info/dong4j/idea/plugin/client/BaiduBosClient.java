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

import java.io.IOException;
import java.io.InputStream;

import lombok.extern.slf4j.Slf4j;

/**
 * <p>Description: 百度云</p>
 *
 * @author dong4j
 * @version 0.0.1
 * @email "mailto:dong4j@gmail.com"
 * @date 2021.02.14 18:40
 * @since 0.0.1
 */
@Slf4j
@Client(CloudEnum.BAIDU_CLOUD)
public class BaiduBosClient extends AbstractOssClient {

    static {
        init();
    }

    /**
     * 如果是第一次使用, ossClient == null
     *
     * @since 0.0.1
     */
    private static void init() {
        BaiduBosState baiduBosState = MikPersistenComponent.getInstance().getState().getBaiduBosState();
        accessKey = baiduBosState.getAccessKey();
        accessSecretKey = PasswordManager.getPassword(BaiduBosSetting.CREDENTIAL_ATTRIBUTES);
        endpoint = baiduBosState.getEndpoint();
        String tempFileDir = baiduBosState.getFiledir();
        filedir = StringUtils.isBlank(tempFileDir) ? "" : tempFileDir + "/";

    }

    /**
     * 静态内部类实现单例
     * 为什么这样实现就是单例的？
     * 1. 因为这个类的实例化是靠静态内部类的静态常量实例化的;
     * 2. INSTANCE 是常量，因此只能赋值一次；它还是静态的，因此随着内部类一起加载;
     * 这样实现有什么好处？
     * 1. 我记得以前接触的懒汉式的代码好像有线程安全问题，需要加同步锁才能解决;
     * 2. 采用静态内部类实现的代码也是懒加载的，只有第一次使用这个单例的实例的时候才加载;
     * 3. 不会有线程安全问题;
     *
     * @return the instance
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
     * 使用缓存的 map 映射获取已初始化的 client, 避免创建多个实例
     *
     * @author dong4j
     * @version 0.0.1
     * @email "mailto:dong4j@gmail.com"
     * @date 2020.04.25 17:05
     * @since 0.0.1
     */
    private static class SingletonHandler {
        /** SINGLETON */
        private static final BaiduBosClient SINGLETON = new BaiduBosClient();
    }

    /**
     * Gets cloud type *
     *
     * @return the cloud type
     * @since 0.0.1
     */
    @Override
    public CloudEnum getCloudType() {
        return CloudEnum.BAIDU_CLOUD;
    }

    /**
     * Gets client *
     *
     * @return the client
     * @since 1.1.0
     */
    @Override
    protected AbstractOssClient getClient() {
        return getInstance();
    }

    /**
     * Put objects
     *
     * @param key      key
     * @param instream instream
     * @throws IOException io exception
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
     * Gets state *
     *
     * @return the state
     * @since 1.1.0
     */
    @Override
    protected AbstractExtendOssState getState() {
        return MikPersistenComponent.getInstance().getState().getBaiduBosState();
    }
}
