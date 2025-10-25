package info.dong4j.idea.plugin.util;

import org.junit.Test;

import java.io.FileInputStream;
import java.io.IOException;

/**
 * <p>Description:  </p>
 *
 * @author dong4j
 * @version 1.0.0
 * @email "mailto:dong4j@gmail.com"
 * @date 2020.04.25 16:36
 * @since 1.1.0
 */
public class AliyunOssUtilsTest {
    /** secretId */
    private static final String secretId = System.getProperty("secretId");
    /** secretKey */
    private static final String secretKey = System.getProperty("secretKey");
    /** bucketName */
    private static final String bucketName = System.getProperty("bucketName");
    // private static final String ossBucket = "xxx";
    // private static final String accessKeyId = "xxx";
    // private static final String secretAccessKey = "xxx";
    //可根据自己的oss产品自行更改域名
    // private static final String endpoint = "oss-cn-shanghai.aliyuncs.com/";

    /**
     * Test 1
     *
     * @throws IOException io exception
     * @since 1.1.0
     */
    @Test
    public void test_1() throws Exception {

        // key 必须使用 / 为前缀
        String putResult = AliyunOssUtils.putObject("/dddd.jpg",
                                                    new FileInputStream("/Users/dong4j/Downloads/xu.png"),
                                                    bucketName,
                                                    "oss-cn-hangzhou.aliyuncs.com",
                                                    secretId,
                                                    secretKey,
                                                    false,
                                                    null);
        System.out.println("putResult:" + putResult);
    }

}
