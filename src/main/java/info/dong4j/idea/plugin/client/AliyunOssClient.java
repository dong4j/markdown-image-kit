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
import info.dong4j.idea.plugin.settings.AliyunOssState;
import info.dong4j.idea.plugin.settings.MikPersistenComponent;
import info.dong4j.idea.plugin.settings.MikState;
import info.dong4j.idea.plugin.settings.OssState;
import info.dong4j.idea.plugin.util.AliyunOssUtils;
import info.dong4j.idea.plugin.util.DES;
import info.dong4j.idea.plugin.util.StringUtils;

import org.apache.http.util.Asserts;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.InputStream;
import java.util.Map;

import javax.swing.JPanel;

import lombok.extern.slf4j.Slf4j;

/**
 * <p>Company: no company</p>
 * <p>Description: 右键上传一次或者点击测试按钮时初始化一次</p>
 *
 * @author dong4j
 * @version 0.0.1
 * @email "mailto:dong4j@gmail.com"
 * @date 2020.04.25 17:05
 * @since 2019.03.18 09:57
 */
@Slf4j
@Client(CloudEnum.ALIYUN_CLOUD)
public class AliyunOssClient implements OssClient {
    /**
     * The constant URL_PROTOCOL_HTTPS.
     */
    public static final String URL_PROTOCOL_HTTPS = "https";
    /** URL_PROTOCOL_HTTP */
    private static final String URL_PROTOCOL_HTTP = "http";

    /** bucketName */
    private static String bucketName;
    /** filedir */
    private static String filedir;
    /** accessKey */
    private static String accessKey;
    /** accessSecretKey */
    private static String accessSecretKey;
    /** endpoint */
    private static String endpoint;

    static {
        init();
    }

    /**
     * 如果是第一次使用, ossClient == null
     *
     * @since 0.0.1
     */
    private static void init() {
        AliyunOssState aliyunOssState = MikPersistenComponent.getInstance().getState().getAliyunOssState();
        accessKey = aliyunOssState.getAccessKey();
        accessSecretKey = DES.decrypt(aliyunOssState.getAccessSecretKey(), MikState.ALIYUN);
        endpoint = aliyunOssState.getEndpoint();
        String tempFileDir = aliyunOssState.getFiledir();
        filedir = StringUtils.isBlank(tempFileDir) ? "" : tempFileDir + "/";

    }

    /**
     * 静态内部类实现单例
     * 为什么这样实现就是单例的？
     * 1. 因为这个类的实例化是靠静态内部类的静态常量实例化的;
     * 2. INSTANCE 是常量，因此只能赋值一次；它还是静态的，因此随着内部类一起加载;
     * 这样实现有什么好处？
     * 1. 我记得以前接触的懒汉式的代码好像有线程安全问题，需要加同步锁才能解决;
     * 2. 采用静态内部类实现的代码也是懒加载的，只有第一次使用这个单例的实例的时候才加载;
     * 3. 不会有线程安全问题;
     *
     * @return the instance
     * @since 0.0.1
     */
    @Contract(pure = true)
    public static AliyunOssClient getInstance() {
        AliyunOssClient client = (AliyunOssClient) OssClient.INSTANCES.get(CloudEnum.ALIYUN_CLOUD);
        if (client == null) {
            client = SingletonHandler.SINGLETON;
            OssClient.INSTANCES.put(CloudEnum.ALIYUN_CLOUD, client);
        }
        return client;
    }

    /**
     * 使用缓存的 map 映射获取已初始化的 client, 避免创建多个实例
     *
     * @author dong4j
     * @version 0.0.1
     * @email "mailto:dong4j@gmail.com"
     * @date 2020.04.25 17:05
     * @since 0.0.1
     */
    private static class SingletonHandler {
        /** SINGLETON */
        private static final AliyunOssClient SINGLETON = new AliyunOssClient();
    }

    /**
     * Gets cloud type *
     *
     * @return the cloud type
     * @since 0.0.1
     */
    @Override
    public CloudEnum getCloudType() {
        return CloudEnum.ALIYUN_CLOUD;
    }

    /**
     * 直接从面板组件上获取最新配置, 不使用 state
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
        String endpoint = map.get("endpoint");
        String filedir = map.get("filedir");

        Asserts.notBlank(bucketName, "Bucket 必填");
        Asserts.notBlank(accessKey, "Access Key 必填");
        Asserts.notBlank(secretKey, "Secret Key 必填");
        Asserts.notBlank(endpoint, "Endpoint 必填");

        return this.upload(inputStream,
                           fileName,
                           bucketName,
                           accessKey,
                           secretKey,
                           endpoint,
                           filedir);
    }

    /**
     * test 按钮点击事件后请求, 成功后保留 client, paste 或者 右键 上传时使用
     *
     * @param inputStream     the input stream
     * @param fileName        the file name
     * @param bucketName      the bucketName name
     * @param accessKey       the access key
     * @param accessSecretKey the access secret key
     * @param endpoint        the endpoint
     * @param filedir         the temp file dir
     * @return the string
     * @since 0.0.1
     */
    private String upload(InputStream inputStream,
                          String fileName,
                          String bucketName,
                          String accessKey,
                          String accessSecretKey,
                          String endpoint,
                          String filedir) throws Exception {

        filedir = StringUtils.isBlank(filedir) ? "" : filedir + "/";

        AliyunOssClient.filedir = filedir;
        AliyunOssClient.bucketName = bucketName;
        AliyunOssClient.accessKey = accessKey;
        AliyunOssClient.accessSecretKey = accessSecretKey;
        AliyunOssClient.endpoint = endpoint;

        AliyunOssClient aliyunOssClient = AliyunOssClient.getInstance();
        String url = aliyunOssClient.upload(inputStream, fileName);

        if (StringUtils.isNotBlank(url)) {
            int hashcode = bucketName.hashCode() +
                           accessKey.hashCode() +
                           accessSecretKey.hashCode() +
                           endpoint.hashCode();
            OssState.saveStatus(MikPersistenComponent.getInstance().getState().getAliyunOssState(),
                                hashcode,
                                MikState.OLD_HASH_KEY);
        }
        return url;
    }

    /**
     * 上传到OSS服务器  如果同名文件会覆盖服务器上的
     *
     * @param instream instream
     * @param fileName file name
     * @return 出错返回 "" ,唯一MD5数字签名
     * @since 0.0.1
     */
    @Override
    public String upload(@NotNull InputStream instream,
                         @NotNull String fileName) throws Exception {
        String key = filedir + fileName;
        if (!key.startsWith("/")) {
            key = "/" + key;
        }
        AliyunOssUtils.putObject(key,
                                 instream,
                                 bucketName,
                                 endpoint,
                                 accessKey,
                                 accessSecretKey);
        return "https://" + bucketName + "." + endpoint + "/" + filedir + fileName;
    }

}
