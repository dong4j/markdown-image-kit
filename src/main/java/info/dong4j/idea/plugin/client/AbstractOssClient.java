package info.dong4j.idea.plugin.client;

import com.intellij.credentialStore.CredentialAttributes;

import info.dong4j.idea.plugin.settings.MikPersistenComponent;
import info.dong4j.idea.plugin.settings.MikState;
import info.dong4j.idea.plugin.settings.OssState;
import info.dong4j.idea.plugin.settings.oss.AbstractExtendOssState;
import info.dong4j.idea.plugin.util.PasswordManager;
import info.dong4j.idea.plugin.util.StringUtils;

import org.apache.http.util.Asserts;
import org.jetbrains.annotations.NotNull;

import java.io.InputStream;
import java.util.Map;

import javax.swing.JPanel;

/**
 * OSS客户端抽象类
 * <p>
 * 提供OSS客户端的基础功能和通用操作，包括文件上传、配置管理以及与状态存储的交互。该类作为所有具体OSS客户端实现的基类，定义了上传文件的核心逻辑和配置参数。
 * <p>
 * 该类包含多个静态字段用于存储OSS连接相关的配置信息，如bucket名称、访问密钥、端点等。同时提供了上传文件的方法，支持自定义端点和HTTPS协议。
 * <p>
 * 该类还封装了与状态管理相关的操作，如保存上传状态，用于后续的文件操作或状态恢复。
 *
 * @author dong4j
 * @version 1.0.0
 * @date 2021.02.16
 * @since 1.0.0
 */
public abstract class AbstractOssClient implements OssClient {
    /** HTTPS 协议标识符 */
    public static final String URL_PROTOCOL_HTTPS = "https";
    /** HTTP 协议标识符 */
    public static final String URL_PROTOCOL_HTTP = "http";
    /** 存储对象的存储桶名称 */
    protected static String bucketName;
    /** 文件存储目录路径 */
    protected static String filedir;
    /** 访问密钥，用于身份验证和权限控制 */
    protected static String accessKey;
    /** 访问密钥，用于身份验证和请求签名 */
    protected static String accessSecretKey;
    /** 服务端点地址 */
    protected static String endpoint;
    /** 是否使用自定义端点 */
    protected static boolean isCustomEndpoint;
    /** 自定义端点地址，用于指定服务的自定义访问路径 */
    protected static String customEndpoint;

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
        AbstractExtendOssState ossState = this.getState(state);

        String bucketName = ossState.getBucketName();
        String accessKey = ossState.getAccessKey();
        String secretKey = PasswordManager.getPassword(credentialAttributes());
        String endpoint = ossState.getEndpoint();
        String filedir = ossState.getFiledir();
        String customEndpoint = ossState.getCustomEndpoint();
        boolean isCustomEndpoint = Boolean.TRUE.equals(ossState.getIsCustomEndpoint());

        Asserts.notBlank(bucketName, "Bucket");
        Asserts.notBlank(accessKey, "Access Key");
        Asserts.notBlank(secretKey, "Secret Key");
        Asserts.notBlank(endpoint, "Endpoint");

        return this.upload(inputStream,
                           filename,
                           bucketName,
                           accessKey,
                           secretKey,
                           endpoint,
                           filedir,
                           isCustomEndpoint,
                           customEndpoint);
    }

    /**
     * 从面板组件上直接获取最新配置信息，不使用 state
     * <p>
     * 该方法通过面板组件获取配置参数，并进行必要的校验，最后调用 upload 方法上传文件
     *
     * @param inputStream 输入流
     * @param filename    文件名
     * @param jPanel      面板组件
     * @return 上传结果字符串
     * @throws Exception 上传过程中发生异常时抛出
     */
    @Override
    public String upload(InputStream inputStream, String filename, JPanel jPanel) throws Exception {
        Map<String, String> map = this.getTestFieldText(jPanel);

        String bucketName = map.get("bucketName");
        String accessKey = map.get("accessKey");
        String secretKey = map.get("secretKey");
        String endpoint = map.get("endpoint");
        String filedir = map.get("filedir");
        String customEndpoint = map.get("customEndpoint");
        boolean isCustomEndpoint = Boolean.parseBoolean(map.get("isCustomEndpoint"));

        Asserts.notBlank(bucketName, "Bucket");
        Asserts.notBlank(accessKey, "Access Key");
        Asserts.notBlank(secretKey, "Secret Key");
        Asserts.notBlank(endpoint, "Endpoint");

        return this.upload(inputStream,
                           filename,
                           bucketName,
                           accessKey,
                           secretKey,
                           endpoint,
                           filedir,
                           isCustomEndpoint,
                           customEndpoint);
    }

    /**
     * 处理上传按钮点击事件，用于上传文件并返回上传后的URL
     * <p>
     * 该方法用于处理上传操作，设置OSS客户端相关参数，并执行上传操作。
     * 上传成功后，若URL不为空，会根据相关参数生成哈希值并保存状态。
     *
     * @param inputStream      上传文件的输入流
     * @param filename         上传文件的文件名
     * @param bucketName       存储桶名称
     * @param accessKey        访问密钥
     * @param accessSecretKey  访问密钥的密文
     * @param endpoint         服务端点
     * @param filedir          临时文件存储目录
     * @param isCustomEndpoint 是否使用自定义端点
     * @param customEndpoint   自定义端点地址
     * @return 上传成功后的文件URL
     * @throws Exception 上传过程中发生异常时抛出
     * @since 0.0.1
     */
    private String upload(InputStream inputStream,
                          String filename,
                          String bucketName,
                          String accessKey,
                          String accessSecretKey,
                          String endpoint,
                          String filedir,
                          boolean isCustomEndpoint,
                          String customEndpoint) throws Exception {

        filedir = StringUtils.isBlank(filedir) ? "" : filedir + "/";

        AbstractOssClient.filedir = filedir;
        AbstractOssClient.bucketName = bucketName;
        AbstractOssClient.accessKey = accessKey;
        AbstractOssClient.accessSecretKey = accessSecretKey;
        AbstractOssClient.endpoint = endpoint;
        AbstractOssClient.customEndpoint = customEndpoint;
        AbstractOssClient.isCustomEndpoint = isCustomEndpoint;

        AbstractOssClient client = this.getClient();

        String url = client.upload(inputStream, filename);

        if (StringUtils.isNotBlank(url)) {
            int hashcode = bucketName.hashCode() +
                           accessKey.hashCode() +
                           accessSecretKey.hashCode() +
                           endpoint.hashCode() +
                           (customEndpoint + isCustomEndpoint).hashCode();

            OssState.saveStatus(this.getState(),
                                hashcode,
                                MikState.OLD_HASH_KEY);
        }
        return url;
    }

    /**
     * 上传文件到OSS服务器，若存在同名文件则覆盖
     * <p>
     * 将输入流中的文件内容上传至指定路径，并返回文件的访问URL
     *
     * @param instream 文件的输入流
     * @param filename 文件名
     * @return 若上传过程中发生错误则返回空字符串，否则返回文件的唯一MD5数字签名
     * @throws Exception 上传过程中发生异常时抛出
     * @since 0.0.1
     */
    @Override
    public String upload(@NotNull InputStream instream,
                         @NotNull String filename) throws Exception {
        String key = filedir + filename;
        if (!key.startsWith("/")) {
            key = "/" + key;
        }

        this.putObjects(key, instream);

        // 如果没有报错说明上传成功, 开始组装 url
        if (isCustomEndpoint) {
            return "https://" + customEndpoint + key;
        }
        return "https://" + bucketName + "." + endpoint + key;
    }

    /**
     * 获取客户端实例
     * <p>
     * 返回当前配置的OSS客户端实例，用于执行OSS相关的操作
     *
     * @return 客户端实例
     * @since 1.3.0
     */
    protected abstract AbstractOssClient getClient();

    /**
     * 将对象以指定的键存储到数据源中
     * <p>
     * 该方法用于将输入流中的数据以指定的键写入存储系统
     *
     * @param key      存储时使用的键
     * @param instream 存储的数据源输入流
     * @throws Exception 如果存储过程中发生异常
     */
    protected abstract void putObjects(String key, InputStream instream) throws Exception;

    /**
     * 获取状态
     * <p>
     * 返回当前对象的状态信息（从持久化组件中获取）
     *
     * @return 当前对象的状态
     * @since 1.3.0
     */
    protected AbstractExtendOssState getState() {
        return getState(MikPersistenComponent.getInstance().getState());
    }

    /**
     * 从 MikState 中获取对应的状态
     * <p>
     * 该方法用于从传入的 MikState 对象中获取当前客户端对应的状态信息
     *
     * @param state MikState 对象，包含所有配置状态信息
     * @return 当前客户端对应的状态对象
     * @since 2.0.0
     */
    protected abstract AbstractExtendOssState getState(MikState state);

    /**
     * 获取凭证属性
     * <p>
     * 返回当前凭证的属性信息，具体实现由子类提供
     *
     * @return 凭证属性
     * @since 1.6.0
     */
    protected abstract CredentialAttributes credentialAttributes();

}
