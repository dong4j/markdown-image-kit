package info.dong4j.idea.plugin.client;


import info.dong4j.idea.plugin.enums.CloudEnum;

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
        init();
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

        return "";
    }

    @Override
    public String upload(InputStream inputStream, String fileName, JPanel jPanel) {
        return null;
    }

}
