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

import junit.framework.TestCase;

import java.io.FileInputStream;

import lombok.extern.slf4j.Slf4j;

/**
 * <p>Company: 成都返空汇网络技术有限公司</p>
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
