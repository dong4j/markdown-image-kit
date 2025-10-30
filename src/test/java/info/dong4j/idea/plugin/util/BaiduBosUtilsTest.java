package info.dong4j.idea.plugin.util;

import org.junit.Test;

import java.io.FileInputStream;

/**
 * 百度云对象存储（BOS）工具类测试
 * <p>
 * 用于测试百度云对象存储服务的相关功能，包括上传对象等操作。该类通过读取系统属性获取 BOS 认证信息和存储桶名称，用于执行测试用例。
 *
 * @author dong4j
 * @version 1.0.0
 * @date 2020.04.25
 * @since 1.1.0
 */
public class BaiduBosUtilsTest {
    /** secretId 是从系统属性中获取的密钥标识符，用于标识安全凭证 */
    private static final String secretId = System.getProperty("secretId");
    /** 用于加密或验证的密钥，从系统属性中获取 */
    private static final String secretKey = System.getProperty("secretKey");
    /** bucketName 是从系统属性中获取的存储桶名称，用于指定对象存储服务的目标存储桶 */
    private static final String bucketName = System.getProperty("bucketName");
    // private static final String ossBucket = "xxx";
    // private static final String accessKeyId = "xxx";
    // private static final String secretAccessKey = "xxx";

    /**
     * 测试 BaiduBosUtils 的 putObject 方法
     * <p>
     * 测试场景：验证使用以 / 为前缀的 key 进行文件上传的逻辑
     * 预期结果：应成功上传文件并返回正确的结果
     * <p>
     * 注意：测试需要本地文件系统支持，文件路径为 /Users/dong4j/Downloads/mik.webp
     */
    @Test
    public void test_1() throws Exception {

        // key 必须使用 / 为前缀
        String putResult = BaiduBosUtils.putObject("/c/xu.jpg",
                                                   new FileInputStream("/Users/dong4j/Downloads/mik.webp"),
                                                   bucketName,
                                                   "bj.bcebos.com",
                                                   secretId,
                                                   secretKey,
                                                   false,
                                                   null);
        System.out.println("putResult:" + putResult);
    }

}
