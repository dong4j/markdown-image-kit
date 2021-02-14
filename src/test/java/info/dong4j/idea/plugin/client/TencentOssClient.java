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

package info.dong4j.idea.plugin.client;


import info.dong4j.idea.plugin.enums.CloudEnum;

import java.io.InputStream;

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
 * @version 1.0.0
 * @email "mailto:dong4j@gmail.com"
 * @date 2021.02.14 22:44
 * @since 2019.07.08 16:39
 */
@Slf4j
public class TencentOssClient implements OssClient {

    /** bucketName */
    private static String bucketName;
    /** regionName */
    private static String regionName;

    /**
     * Instantiates a new Tencent oss client.
     *
     * @since 1.1.0
     */
    private TencentOssClient() {
        checkClient();
    }

    /**
     * 在调用 ossClient 之前先检查, 如果为 null 就 init()
     *
     * @since 1.1.0
     */
    private static void checkClient() {
        init();
    }

    /**
     * 如果是第一次使用, ossClient == null, 使用持久化配置初始化
     *
     * @since 1.1.0
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

    /**
     * Gets cloud type *
     *
     * @return the cloud type
     * @since 1.1.0
     */
    @Override
    public CloudEnum getCloudType() {
        return CloudEnum.TENCENT_CLOUD;
    }

    /**
     * Upload
     *
     * @param inputStream input stream
     * @param fileName    file name
     * @return the string
     * @since 1.1.0
     */
    @Override
    public String upload(InputStream inputStream, String fileName) {

        return "";
    }

    /**
     * Upload
     *
     * @param inputStream input stream
     * @param fileName    file name
     * @param jPanel      j panel
     * @return the string
     * @since 1.1.0
     */
    @Override
    public String upload(InputStream inputStream, String fileName, JPanel jPanel) {
        return null;
    }

}
