package info.dong4j.idea.plugin.util;

import org.junit.Test;

import java.io.FileInputStream;

/**
 * 阿里云OSS工具类测试类
 * <p>
 * 用于测试阿里云OSS工具类AliyunOssUtils的功能，包括文件上传等操作。
 * 该类通过读取系统属性获取OSS相关的配置信息，如secretId、secretKey和bucketName。
 * 提供了一个测试方法test_1，用于验证文件上传功能是否正常。
 *
 * @author dong4j
 * @version 1.0.0
 * @date 2020.04.25
 * @since 1.1.0
 */
public class AliyunOssUtilsTest {
    /** secretId 是从系统属性中获取的密钥标识符，用于标识安全凭证 */
    private static final String secretId = System.getProperty("secretId");
    /** secretKey 是用于加密解密操作的密钥，从系统属性中获取 */
    private static final String secretKey = System.getProperty("secretKey");
    /** 存储桶名称，从系统属性中获取 */
    private static final String bucketName = System.getProperty("bucketName");
    // private static final String ossBucket = "xxx";
    // private static final String accessKeyId = "xxx";
    // private static final String secretAccessKey = "xxx";
    //可根据自己的oss产品自行更改域名
    // private static final String endpoint = "oss-cn-shanghai.aliyuncs.com/";

    /**
     * 测试 AliyunOssUtils.putObject 方法的正确性
     * <p>
     * 测试场景：验证使用以 / 为前缀的 key 进行文件上传的逻辑
     * 预期结果：应成功上传文件并返回正确的结果
     * <p>
     * 注意：测试需要本地文件路径和 OSS 配置信息，确保测试环境已正确配置
     */
    @Test
    public void test_1() throws Exception {

        // key 必须使用 / 为前缀
        String putResult = AliyunOssUtils.putObject("/dddd.jpg",
                                                    new FileInputStream("/Users/dong4j/Downloads/mik.webp"),
                                                    bucketName,
                                                    "oss-cn-hangzhou.aliyuncs.com",
                                                    secretId,
                                                    secretKey,
                                                    false,
                                                    null);
        System.out.println("putResult:" + putResult);
    }

}
