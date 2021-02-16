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

import info.dong4j.idea.plugin.settings.AbstractExtendOssState;
import info.dong4j.idea.plugin.settings.MikState;
import info.dong4j.idea.plugin.settings.OssState;
import info.dong4j.idea.plugin.util.StringUtils;

import org.apache.http.util.Asserts;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import javax.swing.JPanel;

/**
 * <p>Company: 成都返空汇网络技术有限公司</p>
 * <p>Description:  </p>
 *
 * @author dong4j
 * @version 1.0.0
 * @email "mailto:dong4j@fkhwl.com"
 * @date 2021.02.16 13:38
 * @since 1.1.0
 */
public abstract class AbstractOssClient implements OssClient {
    /** URL_PROTOCOL_HTTPS */
    public static final String URL_PROTOCOL_HTTPS = "https";
    /** URL_PROTOCOL_HTTP */
    public static final String URL_PROTOCOL_HTTP = "http";

    /** bucketName */
    protected static String bucketName;
    /** filedir */
    protected static String filedir;
    /** accessKey */
    protected static String accessKey;
    /** accessSecretKey */
    protected static String accessSecretKey;
    /** endpoint */
    protected static String endpoint;
    /** customEndpoint */
    protected static boolean isCustomEndpoint;
    /** customEndpoint */
    protected static String customEndpoint;

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
        String customEndpoint = map.get("customEndpoint");
        boolean isCustomEndpoint = Boolean.parseBoolean(map.get("isCustomEndpoint"));

        Asserts.notBlank(bucketName, "Bucket");
        Asserts.notBlank(accessKey, "Access Key");
        Asserts.notBlank(secretKey, "Secret Key");
        Asserts.notBlank(endpoint, "Endpoint");

        return this.upload(inputStream,
                           fileName,
                           bucketName,
                           accessKey,
                           secretKey,
                           endpoint,
                           filedir,
                           isCustomEndpoint,
                           customEndpoint);
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
                          String filedir,
                          boolean isCustomEndpoint,
                          String customEndpoint) throws Exception {

        filedir = StringUtils.isBlank(filedir) ? "" : filedir + "/";

        AbstractOssClient.filedir = filedir;
        AbstractOssClient.bucketName = bucketName;
        AbstractOssClient.accessKey = accessKey;
        AbstractOssClient.accessSecretKey = accessSecretKey;
        AbstractOssClient.endpoint = endpoint;
        AbstractOssClient.customEndpoint = customEndpoint;
        AbstractOssClient.isCustomEndpoint = isCustomEndpoint;

        AbstractOssClient client = this.getClient();

        String url = client.upload(inputStream, fileName);

        if (StringUtils.isNotBlank(url)) {
            int hashcode = bucketName.hashCode() +
                           accessKey.hashCode() +
                           accessSecretKey.hashCode() +
                           endpoint.hashCode() +
                           (customEndpoint + isCustomEndpoint).hashCode();

            OssState.saveStatus(this.getState(),
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

        this.putObjects(key, instream);

        if (isCustomEndpoint) {
            return "https://" + customEndpoint + "/" + filedir + fileName;
        }
        return "https://" + bucketName + "." + endpoint + "/" + filedir + fileName;
    }

    protected abstract AbstractOssClient getClient();

    protected abstract void putObjects(String key, InputStream instream) throws IOException;

    protected abstract AbstractExtendOssState getState();
}
