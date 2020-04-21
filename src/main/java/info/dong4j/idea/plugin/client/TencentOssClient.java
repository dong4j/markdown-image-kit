package info.dong4j.idea.plugin.client;

import info.dong4j.idea.plugin.enums.CloudEnum;
import info.dong4j.idea.plugin.sdk.qcloud.cos.COSClient;
import info.dong4j.idea.plugin.sdk.qcloud.cos.ClientConfig;
import info.dong4j.idea.plugin.sdk.qcloud.cos.auth.BasicCOSCredentials;
import info.dong4j.idea.plugin.sdk.qcloud.cos.auth.COSCredentials;
import info.dong4j.idea.plugin.sdk.qcloud.cos.exception.CosClientException;
import info.dong4j.idea.plugin.sdk.qcloud.cos.model.ObjectMetadata;
import info.dong4j.idea.plugin.sdk.qcloud.cos.model.PutObjectRequest;
import info.dong4j.idea.plugin.sdk.qcloud.cos.model.StorageClass;
import info.dong4j.idea.plugin.sdk.qcloud.cos.region.Region;
import info.dong4j.idea.plugin.settings.MikPersistenComponent;
import info.dong4j.idea.plugin.settings.MikState;
import info.dong4j.idea.plugin.settings.OssState;
import info.dong4j.idea.plugin.settings.TencentOssState;
import info.dong4j.idea.plugin.util.DES;
import info.dong4j.idea.plugin.util.ImageUtils;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.util.Map;

import javax.swing.JPanel;

import lombok.extern.slf4j.Slf4j;

/**
 * <p>Company: no company</p>
 * <p>Description: oss client 实现步骤:
 * 1. 初始化配置: 从持久化配置中初始化 client
 * 2. 静态内部类获取 client 单例
 * 3. 实现 OssClient 接口
 * 4. 自定义 upload 逻辑</p>
 *
 * @author dong4j
 * @email dong4j@gmail.com
 * @since 2019 -07-08 16:39
 */
@Slf4j
@Client(CloudEnum.TENCENT_CLOUD)
public class TencentOssClient implements OssClient {

    private static COSClient ossClient = null;
    private static String bucketName;
    private static String regionName;

    static {
        init();
    }

    /**
     * 如果是第一次使用, ossClient == null, 使用持久化配置初始化
     * 1. 如果是第一次设置, 获取的持久化配置为 null, 则初始化 ossClient 失败
     */
    private static void init() {
        TencentOssState tencentOssState = MikPersistenComponent.getInstance().getState().getTencentOssState();
        bucketName = tencentOssState.getBucketName();
        String accessKey = tencentOssState.getAccessKey();
        String accessSecretKey = DES.decrypt(tencentOssState.getSecretKey(), MikState.TENCENT);
        regionName = tencentOssState.getRegionName();

        try {
            COSCredentials cred = new BasicCOSCredentials(accessKey, accessSecretKey);
            ClientConfig clientConfig = new ClientConfig(new Region(regionName));
            ossClient = new COSClient(cred, clientConfig);
        } catch (Exception ignored) {
        }
    }

    private void setBucketName(String newBucketName) {
        bucketName = newBucketName;
    }

    private void setRegionName(String newRegionName) {
        regionName = newRegionName;
    }

    /**
     * Set oss client.
     *
     * @param oss the oss
     */
    private void setOssClient(COSClient oss) {
        ossClient = oss;
    }

    /**
     * Gets instance.
     *
     * @return the instance
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
     * 使用缓存的 map 映射获取已初始化的 client, 避免创建多个实例
     */
    private static class SingletonHandler {
        private static final TencentOssClient SINGLETON = new TencentOssClient();
    }

    /**
     * 实现接口, 获取当前 client type
     *
     * @return the cloud typed
     */
    @Override
    public CloudEnum getCloudType() {
        return CloudEnum.TENCENT_CLOUD;
    }

    /**
     * 通过文件流上传文件
     *
     * @param inputStream the input stream
     * @param fileName    the file name
     * @return the string
     */
    @Override
    public String upload(InputStream inputStream, String fileName) {
        return this.upload(ossClient, inputStream, fileName);
    }

    /**
     * 在设置界面点击 'Test' 按钮上传时调用, 通过 JPanel 获取当前配置
     * {@link info.dong4j.idea.plugin.settings.ProjectSettingsPage#testAndHelpListener()}
     *
     * @param inputStream the input stream
     * @param fileName    the file name
     * @param jPanel      the j panel
     * @return the string
     */
    @Override
    public String upload(InputStream inputStream, String fileName, JPanel jPanel) {
        Map<String, String> map = this.getTestFieldText(jPanel);
        String bucketName = map.get("bucketName");
        String accessKey = map.get("accessKey");
        String secretKey = map.get("secretKey");
        String regionName = map.get("regionName");

        return this.upload(inputStream,
                           fileName,
                           bucketName,
                           accessKey,
                           secretKey,
                           regionName);
    }

    /**
     * test 按钮点击事件后请求, 成功后保留 client, paste 或者 右键 上传时使用
     *
     * @param inputStream the input stream
     * @param fileName    the file name
     * @param bucketName  the bucketName name
     * @param accessKey   the access key
     * @param secretKey   the secret key
     * @param regionName  the region name
     * @return the string
     */
    @NotNull
    @Contract(pure = true)
    public String upload(InputStream inputStream,
                         String fileName,
                         String bucketName,
                         String accessKey,
                         String secretKey,
                         String regionName) {

        TencentOssClient tencentOssClient = TencentOssClient.getInstance();

        this.setBucketName(bucketName);
        this.setRegionName(regionName);

        // 1 初始化用户身份信息 (secretId, secretKey)
        COSCredentials cred = new BasicCOSCredentials(accessKey, secretKey);
        // 2 设置 bucket 的区域, COS地域的简称请参照 https://www.qcloud.com/document/product/436/6224
        ClientConfig clientConfig = new ClientConfig(new Region(regionName));
        // 3 生成 cos 客户端
        COSClient ossClient = new COSClient(cred, clientConfig);

        String url = tencentOssClient.upload(ossClient, inputStream, fileName);

        if (StringUtils.isNotBlank(url)) {
            int hashcode = bucketName.hashCode() +
                           secretKey.hashCode() +
                           accessKey.hashCode() +
                           regionName.hashCode();
            // 更新可用状态
            OssState.saveStatus(MikPersistenComponent.getInstance().getState().getTencentOssState(),
                                hashcode,
                                MikState.OLD_HASH_KEY);
            // 保存经过验证的 client
            tencentOssClient.setOssClient(ossClient);
        }
        return url;
    }

    /**
     * 调用 SDK 上传文件
     *
     * @param ossClient   the oss client
     * @param inputStream the input stream
     * @param fileName    the file name
     * @return the string
     */
    public String upload(@NotNull COSClient ossClient, @NotNull InputStream inputStream, String fileName) {
        ObjectMetadata objectMetadata = new ObjectMetadata();
        // 从输入流上传必须制定content length, 否则http客户端可能会缓存所有数据，存在内存OOM的情况
        try {
            objectMetadata.setContentLength(inputStream.available());
        } catch (IOException e) {
            log.error("get inputStream available length error", e);
            return "";
        }
        // 默认下载时根据cos路径key的后缀返回响应的contenttype, 上传时设置contenttype会覆盖默认值
        objectMetadata.setContentType(ImageUtils.getImageType(fileName));

        PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, fileName, inputStream, objectMetadata);
        // 设置存储类型, 默认是标准(Standard), 低频(standard_ia)
        putObjectRequest.setStorageClass(StorageClass.Standard);

        try {
            ossClient.putObject(putObjectRequest);
            // 拼接 url = <BucketName-APPID>.cos.region_name.myqcloud.com/key
            return "http://" + bucketName + ".cos." + regionName + ".myqcloud.com/" + fileName;
        } catch (CosClientException e) {
            log.trace("upload error", e);
        }
        return "";
    }
}
