package info.dong4j.idea.plugin.util;

import junit.framework.TestCase;

import java.io.FileInputStream;

import lombok.extern.slf4j.Slf4j;

/**
 * <p>Description:  </p>
 *
 * @author dong4j
 * @version 1.0.0
 * @email "mailto:dong4j@fkhwl.com"
 * @date 2021.02.18 18:47
 * @since 1.6.1
 */
@Slf4j
public class QiniuOssUtilsTest extends TestCase {
    /** secretId */
    private static final String secretId = System.getProperty("secretId");
    /** secretKey */
    private static final String secretKey = System.getProperty("secretKey");
    /** bucketName */
    private static final String bucketName = System.getProperty("bucketName");

    /**
     * Test
     *
     * @throws Exception exception
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
