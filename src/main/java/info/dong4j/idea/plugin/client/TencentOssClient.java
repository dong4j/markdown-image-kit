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
import info.dong4j.idea.plugin.settings.MikPersistenComponent;
import info.dong4j.idea.plugin.settings.MikState;
import info.dong4j.idea.plugin.settings.OssState;
import info.dong4j.idea.plugin.settings.oss.TencentOssSetting;
import info.dong4j.idea.plugin.settings.oss.TencentOssState;
import info.dong4j.idea.plugin.util.PasswordManager;
import info.dong4j.idea.plugin.util.QcloudCosUtils;
import info.dong4j.idea.plugin.util.StringUtils;

import org.apache.http.util.Asserts;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.InputStream;
import java.util.Map;

import javax.swing.JPanel;

import lombok.extern.slf4j.Slf4j;

/**
 * <p>Description: oss client 实现步骤:
 * 1. 初始化配置: 从持久化配置中初始化 client
 * 2. 静态内部类获取 client 单例
 * 3. 实现 OssClient 接口
 * 4. 自定义 upload 逻辑</p>
 *
 * @author dong4j
 * @version 0.0.1
 * @email "mailto:dong4j@gmail.com"
 * @date 2020.04.22 01:17
 * @since 0.0.1
 */
@Slf4j
@Client(CloudEnum.TENCENT_CLOUD)
public class TencentOssClient implements OssClient {
    /** bucketName */
    private static String bucketName;
    /** regionName */
    private static String regionName;
    /** accessKey */
    private static String accessKey;
    /** accessSecretKey */
    private static String accessSecretKey;

    static {
        init();
    }

    /**
     * 如果是第一次使用, ossClient == null, 使用持久化配置初始化
     * 1. 如果是第一次设置, 获取的持久化配置为 null, 则初始化 ossClient 失败
     *
     * @since 0.0.1
     */
    private static void init() {
        TencentOssState tencentOssState = MikPersistenComponent.getInstance().getState().getTencentOssState();
        bucketName = tencentOssState.getBucketName();
        accessKey = tencentOssState.getAccessKey();
        accessSecretKey = PasswordManager.getPassword(TencentOssSetting.CREDENTIAL_ATTRIBUTES);
        regionName = tencentOssState.getRegionName();

    }

    /**
     * Gets instance.
     *
     * @return the instance
     * @since 0.0.1
     */
    @Contract(pure = true)
    public static TencentOssClient getInstance() {
        TencentOssClient client = (TencentOssClient) OssClient.INSTANCES.get(CloudEnum.TENCENT_CLOUD);
        if (client == null) {
            client = SingletonHandler.SINGLETON;
            OssClient.INSTANCES.put(CloudEnum.TENCENT_CLOUD, client);
        }
        return client;
    }

    /**
     * 使用缓存的 map 映射获取已初始化的 client, 避免创建多个实例
     *
     * @author dong4j
     * @version 0.0.1
     * @email "mailto:dong4j@gmail.com"
     * @date 2020.04.22 01:17
     * @since 0.0.1
     */
    private static class SingletonHandler {
        /** SINGLETON */
        private static final TencentOssClient SINGLETON = new TencentOssClient();
    }

    /**
     * 实现接口, 获取当前 client type
     *
     * @return the cloud typed
     * @since 0.0.1
     */
    @Override
    public CloudEnum getCloudType() {
        return CloudEnum.TENCENT_CLOUD;
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
        // 拼接 url = <BucketName-APPID>.cos.region_name.myqcloud.com/key
        return QcloudCosUtils.putObject("/" + fileName,
                                        inputStream,
                                        bucketName,
                                        regionName,
                                        accessKey,
                                        accessSecretKey);
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
        String regionName = map.get("regionName");

        Asserts.notBlank(bucketName, "Bucket");
        Asserts.notBlank(accessKey, "Access Key");
        Asserts.notBlank(secretKey, "Secret Key");
        Asserts.notBlank(regionName, "RegionName");

        return this.upload(inputStream,
                           fileName,
                           bucketName,
                           accessKey,
                           secretKey,
                           regionName);
    }

    /**
     * test 按钮点击事件后请求, 成功后保留 client, paste 或者 右键 上传时使用
     *
     * @param inputStream the input stream
     * @param fileName    the file name
     * @param bucketName  the bucketName name
     * @param accessKey   the access key
     * @param secretKey   the secret key
     * @param regionName  the region name
     * @return the string
     * @since 0.0.1
     */
    @NotNull
    @Contract(pure = true)
    public String upload(InputStream inputStream,
                         String fileName,
                         String bucketName,
                         String accessKey,
                         String secretKey,
                         String regionName) throws Exception {

        TencentOssClient.bucketName = bucketName;
        TencentOssClient.regionName = regionName;
        TencentOssClient.accessKey = accessKey;
        TencentOssClient.accessSecretKey = secretKey;

        TencentOssClient tencentOssClient = TencentOssClient.getInstance();

        String url = tencentOssClient.upload(inputStream, fileName);

        if (StringUtils.isNotBlank(url)) {
            int hashcode = bucketName.hashCode() +
                           secretKey.hashCode() +
                           accessKey.hashCode() +
                           regionName.hashCode();
            // 更新可用状态
            OssState.saveStatus(MikPersistenComponent.getInstance().getState().getTencentOssState(),
                                hashcode,
                                MikState.OLD_HASH_KEY);
        }
        return url;
    }
}
