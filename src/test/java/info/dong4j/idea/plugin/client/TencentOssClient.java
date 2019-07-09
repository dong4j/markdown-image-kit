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
import info.dong4j.idea.plugin.util.ImageUtils;

import java.io.*;

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
public class TencentOssClient implements OssClient {

    private static COSClient ossClient = null;
    private static String bucketName;
    private static String regionName;

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
        if (ossClient == null) {
            init();
        }
    }

    /**
     * 如果是第一次使用, ossClient == null, 使用持久化配置初始化
     */
    private static void init() {
        bucketName = System.getProperty("bucketName");
        String accessSecretKey = System.getProperty("secretKey");
        String accessKey = System.getProperty("secretId");
        regionName = "ap-chengdu";

        try {
            // 1 初始化用户身份信息(secretId, secretKey)
            COSCredentials cred = new BasicCOSCredentials(accessKey, accessSecretKey);
            // 2 设置bucket的区域, COS地域的简称请参照 https://www.qcloud.com/document/product/436/6224
            ClientConfig clientConfig = new ClientConfig(new Region(regionName));
            // 3 生成cos客户端
            ossClient = new COSClient(cred, clientConfig);
        } catch (Exception e) {
            log.trace("", e);
        }
    }

    @Override
    public CloudEnum getCloudType() {
        return CloudEnum.TENCENT_CLOUD;
    }

    @Override
    public String upload(InputStream inputStream, String fileName) {
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
            log.error("upload error", e);
        }
        return "";
    }

    @Override
    public String upload(InputStream inputStream, String fileName, JPanel jPanel) {
        return null;
    }

}
