package info.dong4j.idea.plugin.util;

import junit.framework.TestCase;

import java.io.FileInputStream;

import lombok.extern.slf4j.Slf4j;

/**
 * 七牛OSS工具类测试类
 * <p>
 * 用于测试QiniuOssUtils工具类的功能，包括上传文件到七牛OSS的接口调用。
 * 该类通过读取系统属性获取七牛OSS的认证信息和存储桶名称，用于构建测试场景。
 *
 * @author dong4j
 * @version 1.0.0
 * @date 2021.02.18
 * @since 1.6.1
 */
@Slf4j
public class QiniuOssUtilsTest extends TestCase {
    /** secretId 是从系统属性中获取的密钥标识符，用于标识安全凭证 */
    private static final String secretId = System.getProperty("secretId");
    /** secretKey 是用于加密解密操作的密钥，从系统属性中获取 */
    private static final String secretKey = System.getProperty("secretKey");
    /** 存储桶名称，从系统属性中获取 */
    private static final String bucketName = System.getProperty("bucketName");

    /**
     * 测试七牛云OSS上传功能
     * <p>
     * 用于验证七牛云OSS的文件上传接口是否正常工作，测试过程中会使用指定的文件路径进行上传操作
     *
     * @throws Exception 上传过程中发生异常时抛出
     * @since 1.6.1
     */
    public void test() throws Exception {
        // key 必须使用 / 为前缀
        QiniuOssUtils.putObject("xu.jpg",
                                new FileInputStream("/Users/dong4j/Downloads/xu.png"),
                                bucketName,
                                "upload.qiniup.com",
                                secretId,
                                secretKey);
    }
}
