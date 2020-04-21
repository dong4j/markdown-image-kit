/*
 * MIT License
 *
 * Copyright (c) 2020 dong4j <dong4j@gmail.com>
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
 *
 */

package info.dong4j.idea.plugin.client;


import info.dong4j.idea.plugin.enums.CloudEnum;
import info.dong4j.idea.plugin.util.QcloudCosUtils;

import org.junit.Test;

import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Objects;

import lombok.extern.slf4j.Slf4j;

/**
 * <p>Company: no company</p>
 * <p>Description: 腾讯与 COS 简单上传文件 </p>
 *
 * @author dong4j
 * @version x.x.x
 * @email dong4j @gmail.com
 * @date 2019 -04-16 20:11
 */
@Slf4j
public class TencentOssClientTest {
    /** secretId */
    private static final String secretId = System.getProperty("secretId");
    /** secretKey */
    private static final String secretKey = System.getProperty("secretKey");
    /** bucketName */
    // bucket名需包含appid
    private static final String bucketName = System.getProperty("bucketName");
    
    /**
     * Test
     */
    @Test
    public void test() {
    }

    /**
     * Test 2
     */
    @Test
    public void test2() {
        // 实例化被 @Client 标识的 client, 存入到 map 中
        Class<?> clz = null;
        try {
            clz = Class.forName(CloudEnum.TENCENT_CLOUD.feature);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        try {
            Constructor<?> constructor = Objects.requireNonNull(clz).getDeclaredConstructor();
            constructor.setAccessible(true);
            OssClient uploader = (OssClient) constructor.newInstance();

            OssClient.INSTANCES.put(CloudEnum.TENCENT_CLOUD, uploader);

            this.upload();
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
            log.trace("", e);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * Upload *
     *
     * @throws FileNotFoundException file not found exception
     */
    private void upload() throws FileNotFoundException {
        OssClient uploader = OssClient.INSTANCES.get(CloudEnum.TENCENT_CLOUD);
        log.info("{}", uploader.getName());
        String url = uploader.upload(new FileInputStream(new File("/Users/dong4j/Downloads/xu.png")), "x2.png");
        log.info("url = {}", url);
    }


    /**
     * Test web api *
     *
     * @throws FileNotFoundException file not found exception
     */
    @Test
    public void test_web_api() throws FileNotFoundException {
        String putResult = QcloudCosUtils.putObject(new FileInputStream(new File("/Users/dong4j/Downloads/05B3AB1C-BBA9-4113-B212-10A914D0CC18.jpg")),
                                                    "/test/jjj.jpg",
                                                    secretKey,
                                                    secretId,
                                                    bucketName,
                                                    "ap-chengdu");
        System.out.println("putResult:" + putResult);

        String getResult = QcloudCosUtils.getUrl(bucketName, "ap-chengdu", putResult);
        System.out.println("getResult:" + getResult);
    }
}
