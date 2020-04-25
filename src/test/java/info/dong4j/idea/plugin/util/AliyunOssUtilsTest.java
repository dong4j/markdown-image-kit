package info.dong4j.idea.plugin.util;

import org.junit.Test;

import java.io.*;

/**
 * <p>Company: 成都返空汇网络技术有限公司</p>
 * <p>Description:  </p>
 *
 * @author dong4j
 * @email "mailto:dongshijie@fkhwl.com"
 * @date 2020.04.25 16:36
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

    @Test
    public void test_1() throws IOException {

        // key 必须使用 / 为前缀
        String putResult = AliyunOssUtils.putObject("/dddd.jpg",
                                                    new FileInputStream(new File("/Users/dong4j/Downloads/mik.png")),
                                                    bucketName,
                                                    "oss-cn-hangzhou.aliyuncs.com/",
                                                    secretId,
                                                    secretKey);
        System.out.println("putResult:" + putResult);
    }

}
