package info.dong4j.idea.plugin.client;

import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.auth.COSCredentials;
import com.qcloud.cos.exception.CosClientException;
import com.qcloud.cos.model.ObjectMetadata;
import com.qcloud.cos.model.PutObjectRequest;
import com.qcloud.cos.model.PutObjectResult;
import com.qcloud.cos.model.StorageClass;
import com.qcloud.cos.region.Region;

import info.dong4j.idea.plugin.enums.CloudEnum;
import info.dong4j.idea.plugin.settings.MikPersistenComponent;
import info.dong4j.idea.plugin.settings.MikState;
import info.dong4j.idea.plugin.settings.OssState;
import info.dong4j.idea.plugin.settings.TencentOssState;
import info.dong4j.idea.plugin.util.DES;
import info.dong4j.idea.plugin.util.ImageUtils;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.util.Map;

import javax.swing.JPanel;

import lombok.extern.slf4j.Slf4j;

/**
 * <p>Company: 科大讯飞股份有限公司-四川分公司</p>
 * <p>Description: oss client 实现步骤:
 * 1. 初始化配置: 从持久化配置中初始化 client
 * 2. 静态内部类获取 client 单例
 * 3. 实现 OssClient 接口
 * 4. 自定义 upload 逻辑</p>
 *
 * @author dong4j
 * @email sjdong3 @iflytek.com
 * @since 2019 -07-08 16:39
 */
@Slf4j
@Client(CloudEnum.TENCENT_CLOUD)
public class TencentOssClient implements OssClient {

    private static final Object LOCK = new Object();
    private static COSClient ossClient = null;
    private static String bucketName;
    private static String regionName;

    private TencentOssState tencentOssState = MikPersistenComponent.getInstance().getState().getTencentOssState();

    /**
     * Instantiates a new Tencent oss client.
     */
    private TencentOssClient() {
        checkClient();
    }

    /**
     * 在调用 ossClient 之前先检查, 如果为 null 就 init()
     */
    private static void checkClient() {
        synchronized (LOCK) {
            if (ossClient == null) {
                init();
            }
        }
    }

    /**
     * 如果是第一次使用, ossClient == null, 使用持久化配置初始化
     */
    private static void init() {
        TencentOssState tencentOssState = MikPersistenComponent.getInstance().getState().getTencentOssState();
        bucketName = tencentOssState.getBucketName();
        String accessKey = tencentOssState.getAccessKey();
        String accessSecretKey = DES.decrypt(tencentOssState.getAccessSecretKey(), MikState.TENCENT);
        regionName = tencentOssState.getRegionName();

        try {
            // 1 初始化用户身份信息(secretId, secretKey)
            COSCredentials cred = new BasicCOSCredentials(accessKey, accessSecretKey);
            // 2 设置bucket的区域, COS地域的简称请参照 https://www.qcloud.com/document/product/436/6224
            ClientConfig clientConfig = new ClientConfig(new Region(regionName));
            // 3 生成cos客户端
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
     * Gets instance.
     *
     * @return the instance
     */
    @Contract(pure = true)
    public static TencentOssClient getInstance() {
        return TencentOssClient.SingletonHandler.singleton;
    }

    private static class SingletonHandler {
        private static TencentOssClient singleton = new TencentOssClient();

        static {
            checkClient();
        }
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
        return upload(ossClient, inputStream, fileName);
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
        Map<String, String> map = getTestFieldText(jPanel);
        String bucketName = map.get("bucketName");
        String accessKey = map.get("accessKey");
        String secretKey = map.get("secretKey");
        String regionName = map.get("regionName");

        return upload(inputStream,
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

        setBucketName(bucketName);
        setRegionName(regionName);

        // 1 初始化用户身份信息 (secretId, secretKey)
        COSCredentials cred = new BasicCOSCredentials(accessKey, secretKey);
        // 2 设置 bucket 的区域, COS地域的简称请参照 https://www.qcloud.com/document/product/436/6224
        ClientConfig clientConfig = new ClientConfig(new Region(regionName));
        // 3 生成 cos 客户端
        COSClient ossClient = new COSClient(cred, clientConfig);

        String url = tencentOssClient.upload(ossClient, inputStream, fileName);

        if (org.apache.commons.lang.StringUtils.isNotBlank(url)) {
            int hashcode = bucketName.hashCode() +
                           accessKey.hashCode() +
                           secretKey.hashCode() +
                           regionName.hashCode();
            OssState.saveStatus(tencentOssState, hashcode, MikState.OLD_HASH_KEY);
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
    public String upload(@NotNull COSClient ossClient, InputStream inputStream, String fileName) {
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
            PutObjectResult putObjectResult = ossClient.putObject(putObjectRequest);
            // 拼接 url = <BucketName-APPID>.cos.region_name.myqcloud.com/key
            return "http://" + bucketName + ".cos." + regionName + ".myqcloud.com/" + fileName;
        } catch (CosClientException e) {
            e.printStackTrace();
        }
        return "";
    }
}
