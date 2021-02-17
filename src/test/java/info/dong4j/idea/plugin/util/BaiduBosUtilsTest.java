/*
 * MIT License
 *
 * Copyright (c) 2021 dong4j <dong4j@gmail.com>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package info.dong4j.idea.plugin.util;

import org.junit.Test;

import java.io.FileInputStream;
import java.io.IOException;

/**
 * <p>Company: 成都返空汇网络技术有限公司</p>
 * <p>Description:  </p>
 *
 * @author dong4j
 * @version 1.0.0
 * @email "mailto:dong4j@gmail.com"
 * @date 2020.04.25 16:36
 * @since 1.1.0
 */
public class BaiduBosUtilsTest {
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
        String putResult = BaiduBosUtils.putObject("/c/xu.jpg",
                                                   new FileInputStream("/Users/dong4j/Downloads/xu.png"),
                                                   bucketName,
                                                   "bj.bcebos.com",
                                                   secretId,
                                                   secretKey,
                                                   false,
                                                   null);
        System.out.println("putResult:" + putResult);
    }

}
