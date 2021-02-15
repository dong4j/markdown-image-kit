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

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.InputStream;
import java.util.Map;

import javax.swing.JPanel;

import lombok.extern.slf4j.Slf4j;

/**
 * <p>Company: no company</p>
 * <p>Description: 百度云</p>
 *
 * @author dong4j
 * @version 0.0.1
 * @email "mailto:dong4j@gmail.com"
 * @date 2021.02.14 18:40
 * @since 2019.07.08 17:07
 */
@Slf4j
@Client(CloudEnum.BAIDU_CLOUD)
public class BaiduOssClient implements OssClient {

    /** ossClient */
    private static OssClient ossClient = null;

    static{
        init();
    }

    /**
     * 如果是第一次使用, ossClient == null, 使用持久化配置初始化 SDK client
     *
     * @since 0.0.1
     */
    @Contract(pure = true)
    private static void init() {

    }

    /**
     * Gets instance.
     *
     * @return the instance
     * @since 0.0.1
     */
    @Contract(pure = true)
    public static BaiduOssClient getInstance() {
        BaiduOssClient client = (BaiduOssClient)OssClient.INSTANCES.get(CloudEnum.BAIDU_CLOUD);
        if(client == null){
            client = BaiduOssClient.SingletonHandler.SINGLETON;
            OssClient.INSTANCES.put(CloudEnum.BAIDU_CLOUD, client);
        }
        return client;
    }

    /**
     * 使用缓存的 map 映射获取已初始化的 client, 避免创建多个实例
     *
     * @author dong4j
     * @version 0.0.1
     * @email "mailto:dong4j@gmail.com"
     * @date 2021.02.14 18:40
     * @since 0.0.1
     */
    private static class SingletonHandler {
        /** SINGLETON */
        private static final BaiduOssClient SINGLETON = new BaiduOssClient();
    }

    /**
     * Set oss client.
     *
     * @param oss the oss
     * @since 0.0.1
     */
    private void setOssClient(OssClient oss) {
        ossClient = oss;
    }

    /**
     * 实现接口, 获取当前 client type
     *
     * @return the cloud typed
     * @since 0.0.1
     */
    @Override
    public CloudEnum getCloudType() {
        return CloudEnum.BAIDU_CLOUD;
    }

    /**
     * 通过文件流上传文件
     *
     * @param inputStream the input stream
     * @param fileName    the file name
     * @return the string
     * @since 0.0.1
     */
    @Override
    public String upload(InputStream inputStream, String fileName) throws Exception {
        return this.upload(ossClient, inputStream, fileName);
    }

    /**
     * 在设置界面点击 'Test' 按钮上传时调用, 通过 JPanel 获取当前配置
     * {@link info.dong4j.idea.plugin.settings.ProjectSettingsPage#testAndHelpListener()}
     *
     * @param inputStream the input stream
     * @param fileName    the file name
     * @param jPanel      the j panel
     * @return the string
     * @since 0.0.1
     */
    @Override
    public String upload(InputStream inputStream, String fileName, JPanel jPanel) throws Exception {
        Map<String, String> map = this.getTestFieldText(jPanel);
        String bucketName = map.get("bucketName");
        String accessKey = map.get("accessKey");
        String secretKey = map.get("secretKey");

        return this.upload(inputStream,
                           fileName,
                           bucketName,
                           accessKey,
                           secretKey);
    }

    /**
     * test 按钮点击事件后请求, 成功后保留 client, paste 或者 右键 上传时使用
     *
     * @param inputStream the input stream
     * @param fileName    the file name
     * @param bucketName  the bucketName name
     * @param accessKey   the access key
     * @param secretKey   the secret key
     * @return the string
     * @since 0.0.1
     */
    @NotNull
    @Contract(pure = true)
    public String upload(InputStream inputStream,
                         String fileName,
                         String bucketName,
                         String accessKey,
                         String secretKey) {

        // 1. 使用 SDK 生成 client
        // 2. 调用 SDK 上传文件
        // 3. 计算 hashcode
        // 4. 保存有效的 client
        return "url";
    }

    /**
     * 调用 SDK 上传文件
     *
     * @param ossClient   the oss client
     * @param inputStream the input stream
     * @param fileName    the file name
     * @return the string
     * @since 0.0.1
     */
    public String upload(@NotNull OssClient ossClient, InputStream inputStream, String fileName) {
        return "";
    }
}
