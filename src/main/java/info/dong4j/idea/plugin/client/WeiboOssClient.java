package info.dong4j.idea.plugin.client;

import com.intellij.openapi.util.io.FileUtil;

import info.dong4j.idea.plugin.enums.CloudEnum;
import info.dong4j.idea.plugin.settings.MikPersistenComponent;
import info.dong4j.idea.plugin.settings.MikState;
import info.dong4j.idea.plugin.settings.OssState;
import info.dong4j.idea.plugin.settings.oss.WeiboOssSetting;
import info.dong4j.idea.plugin.settings.oss.WeiboOssState;
import info.dong4j.idea.plugin.util.ImageUtils;
import info.dong4j.idea.plugin.util.PasswordManager;
import info.dong4j.idea.plugin.util.StringUtils;
import info.dong4j.idea.plugin.weibo.CookieContext;
import info.dong4j.idea.plugin.weibo.UploadRequestBuilder;
import info.dong4j.idea.plugin.weibo.UploadResponse;
import info.dong4j.idea.plugin.weibo.WbpUploadRequest;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import javax.swing.JPanel;

import lombok.extern.slf4j.Slf4j;

/**
 * 微博云存储客户端类
 * <p>
 * 该类用于封装与微博云存储相关的客户端操作，提供上传文件到微博云存储的功能。支持通过反射被调用，适用于Paste操作和测试按钮点击事件。主要功能包括初始化OSS客户端、获取实例、上传文件等。
 * <p>
 * 该类使用单例模式确保全局唯一实例，并通过静态初始化方法确保在首次使用时创建OSS客户端对象。上传过程中会根据用户名和密码创建新的OSS客户端，并在上传成功后保存状态信息。
 *
 * @author dong4j
 * @version 1.0.0
 * @date 2025.10.24
 * @since 1.0.0
 */
@Slf4j
@Client(CloudEnum.WEIBO_CLOUD)
public class WeiboOssClient implements OssClient {
    /** ossClient 用于执行对象存储服务的上传请求 */
    private static WbpUploadRequest ossClient = null;
    /** 微博 OSS 状态信息，用于存储和获取微博相关 OSS 配置状态 */
    private final WeiboOssState weiboOssState = MikPersistenComponent.getInstance().getState().getWeiboOssState();

    static {
        init();
    }

    /**
     * 初始化OSS客户端连接
     * <p>
     * 该方法用于在首次使用时初始化OSS客户端实例。如果ossClient尚未创建，则通过获取的用户名和密码构建OSS客户端。
     *
     * @since 0.0.1
     */
    private static void init() {
        WeiboOssState weiboOssState = MikPersistenComponent.getInstance().getState().getWeiboOssState();
        String username = weiboOssState.getUsername();
        String password = PasswordManager.getPassword(WeiboOssSetting.CREDENTIAL_ATTRIBUTES);

        try {
            ossClient = new UploadRequestBuilder()
                .setAcount(username, password)
                .build();
        } catch (Exception ignored) {
        }
    }

    /**
     * 获取云类型
     * <p>
     * 返回当前服务所使用的云类型枚举值
     *
     * @return 云类型枚举值
     * @since 0.0.1
     */
    @Override
    public CloudEnum getCloudType() {
        return CloudEnum.WEIBO_CLOUD;
    }

    /**
     * 设置 OSS 客户端实例
     * <p>
     * 用于将传入的 OSS 客户端对象赋值给成员变量，供后续操作使用
     *
     * @param oss OSS 客户端对象
     * @since 0.0.1
     */
    private void setOssClient(WbpUploadRequest oss) {
        ossClient = oss;
    }

    /**
     * 获取 WeiboOssClient 实例
     * <p>
     * 该方法用于获取 WeiboOssClient 的单例实例。如果实例不存在，则创建并缓存。
     *
     * @return WeiboOssClient 实例
     * @since 0.0.1
     */
    @Contract(pure = true)
    public static WeiboOssClient getInstance() {
        WeiboOssClient client = (WeiboOssClient) OssClient.INSTANCES.get(CloudEnum.WEIBO_CLOUD);
        if (client == null) {
            client = WeiboOssClient.SingletonHandler.SINGLETON;
            OssClient.INSTANCES.put(CloudEnum.WEIBO_CLOUD, client);
        }
        return client;
    }

    /**
     * 单例处理器类
     * <p>
     * 用于管理 WeiboOssClient 的单例实例，确保在整个应用中只存在一个 WeiboOssClient 实例，提供对微博 OSS 服务的统一访问。
     *
     * @author dong4j
     * @version 0.0.1
     * @email mailto:dong4j@gmail.com
     * @date 2021.02.14
     * @since 0.0.1
     */
    private static class SingletonHandler {
        /** 单例模式实例，用于提供全局唯一的 WeiboOssClient 实例 */
        private static final WeiboOssClient SINGLETON = new WeiboOssClient();
    }

    /**
     * 处理 paste 操作的上传方法，通过反射调用。
     * <p>
     * 该方法用于接收输入流和文件名，执行上传操作。
     *
     * @param inputStream 输入流，用于读取上传的数据
     * @param fileName    文件名，表示上传的文件名称
     * @return 上传结果的字符串表示
     * @throws IOException 如果上传过程中发生I/O错误
     * @since 0.0.1
     */
    @Override
    public String upload(InputStream inputStream, String fileName) throws Exception {
        return this.upload(ossClient, inputStream, fileName);
    }

    /**
     * 上传字符串内容到OSS服务
     * <p>
     * 该方法将输入流中的内容写入临时文件，并调用上传文件的方法将文件上传至OSS。
     *
     * @param ossClient   OSS客户端对象
     * @param inputStream 输入流，包含需要上传的数据
     * @param fileName    文件名，用于生成临时文件名
     * @return 上传成功后的文件路径或标识符
     * @throws IOException 如果在处理输入流或文件操作时发生异常
     */
    public String upload(WbpUploadRequest ossClient, InputStream inputStream, String fileName) throws Exception {
        File file = ImageUtils.buildTempFile(fileName);
        FileUtil.copy(inputStream, new FileOutputStream(file));
        return this.upload(ossClient, file);
    }

    /**
     * 处理文件上传，根据上传请求和文件生成对应的URL
     * <p>
     * 该方法用于执行文件上传操作，若上传成功则返回图片的较大尺寸URL，否则返回空字符串。
     *
     * @param ossClient 上传请求对象，用于与OSS服务交互
     * @param file      需要上传的文件对象
     * @return 上传成功时返回图片的较大尺寸URL，否则返回空字符串
     * @throws IOException 发生I/O错误时抛出
     */
    public String upload(@NotNull WbpUploadRequest ossClient, File file) throws Exception {
        String url = "";
        // 微博上传处理不了 fileName, 因为会自动随机处理
        UploadResponse response = ossClient.upload(file);
        if (response.getResult().equals(UploadResponse.ResultStatus.SUCCESS)) {
            url = response.getImageInfo().getLarge();
        }
        return url;
    }

    /**
     * "Upload Test" 按钮被点击后调用，用于处理上传操作
     * <p>
     * 该方法通过获取面板中的测试字段信息，调用另一个上传方法执行实际上传逻辑
     *
     * @param inputStream 输入流，用于读取上传数据
     * @param fileName    文件名，表示上传的文件名称
     * @param jPanel      面板对象，用于获取测试字段信息
     * @return 返回上传结果字符串
     * @throws Exception 上传过程中可能抛出的异常
     */
    @Override
    public String upload(InputStream inputStream, String fileName, JPanel jPanel) throws Exception {
        Map<String, String> map = this.getTestFieldText(jPanel);
        String username = map.get("username");
        String password = map.get("password");

        return this.upload(inputStream, fileName, username, password);
    }

    /**
     * 处理上传请求，用于测试按钮点击事件或上传操作
     * <p>
     * 该方法接收输入流、文件名、用户名和密码，通过微博OSS客户端执行上传操作。
     * 上传成功后，若URL不为空，则保存状态信息并设置OSS客户端。
     *
     * @param inputStream 输入流，用于读取上传文件的内容
     * @param fileName    文件名，表示上传文件的名称
     * @param username    用户名，用于身份验证
     * @param password    密码，用于身份验证
     * @return 上传成功后的URL，若上传失败或URL为空则返回空字符串
     * @throws Exception 上传过程中发生异常时抛出
     */
    @NotNull
    @Contract(pure = true)
    private String upload(InputStream inputStream,
                          String fileName,
                          String username,
                          String password) throws Exception {

        WeiboOssClient weiboOssClient = WeiboOssClient.getInstance();
        CookieContext.getInstance().deleteCookie();
        WbpUploadRequest ossClient = new UploadRequestBuilder()
            .setAcount(username, password)
            .build();
        String url = weiboOssClient.upload(ossClient, inputStream, fileName);
        if (StringUtils.isNotBlank(url)) {
            int hashcode = username.hashCode() + password.hashCode();
            OssState.saveStatus(this.weiboOssState, hashcode, MikState.OLD_HASH_KEY);
            weiboOssClient.setOssClient(ossClient);
        }
        return url;
    }

}
