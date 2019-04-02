/*
 * MIT License
 *
 * Copyright (c) 2019 dong4j <dong4j@gmail.com>
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

import com.qiniu.common.QiniuException;
import com.qiniu.http.Response;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.UploadManager;
import com.qiniu.util.Auth;

import info.dong4j.idea.plugin.enums.CloudEnum;
import info.dong4j.idea.plugin.enums.ZoneEnum;
import info.dong4j.idea.plugin.settings.MikPersistenComponent;
import info.dong4j.idea.plugin.settings.MikState;
import info.dong4j.idea.plugin.settings.OssState;
import info.dong4j.idea.plugin.settings.QiniuOssState;
import info.dong4j.idea.plugin.util.DES;
import info.dong4j.idea.plugin.util.EnumsUtils;

import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.net.*;
import java.util.Map;
import java.util.Optional;

import javax.swing.JPanel;

import lombok.extern.slf4j.Slf4j;

/**
 * <p>Company: 科大讯飞股份有限公司-四川分公司</p>
 * <p>Description: </p>
 *
 * @author dong4j
 * @email sjdong3 @iflytek.com
 * @since 2019-03-19 15:25
 */
@Slf4j
@Client(CloudEnum.QINIU_CLOUD)
public class QiniuOssClient implements OssClient {
    private static final long DEAD_LINE = 3600L * 1000 * 24 * 365 * 10;
    private static final Object LOCK = new Object();
    private static String token;
    private static UploadManager ossClient = null;
    private static String domain;
    private QiniuOssState qiniuOssState = MikPersistenComponent.getInstance().getState().getQiniuOssState();

    private QiniuOssClient() {
        checkClient();
    }

    /**
     * 在调用 ossClient 之前先检查, 如果为 null 就 init()
     */
    private static void checkClient() {
        synchronized (LOCK) {
            if (ossClient == null) {
                init();
            }
        }
    }

    /**
     * 如果是第一次使用, ossClient == null
     */
    private static void init() {
        QiniuOssState qiniuOssState = MikPersistenComponent.getInstance().getState().getQiniuOssState();
        domain = qiniuOssState.getEndpoint();
        String accessKey = qiniuOssState.getAccessKey();
        String secretKey = DES.decrypt(qiniuOssState.getAccessSecretKey(), MikState.QINIU);
        String bucketName = qiniuOssState.getBucketName();

        Optional<ZoneEnum> zone = EnumsUtils.getEnumObject(ZoneEnum.class, e -> e.getIndex() == qiniuOssState.getZoneIndex());
        try {
            Configuration cfg = new Configuration(zone.orElse(ZoneEnum.EAST_CHINA).zone);
            ossClient = new UploadManager(cfg);
            buildToken(Auth.create(accessKey, secretKey), bucketName);
        } catch (Exception ignored) {
        }
    }

    /**
     * Build token string.
     *
     * @param auth       the auth
     * @param bucketName the bucket name
     * @return the string
     */
    private static void buildToken(Auth auth, String bucketName) {
        token = auth.uploadToken(bucketName, null, DEAD_LINE, null, true);
    }

    @Override
    public CloudEnum getCloudType() {
        return CloudEnum.QINIU_CLOUD;
    }

    /**
     * Upload string.
     *
     * @param inputStream the input stream
     * @param fileName    the file name
     * @return the string
     */
    @Override
    public String upload(InputStream inputStream, String fileName) {
        return upload(ossClient, inputStream, fileName);
    }

    /**
     * Upload from test string.
     * {@link info.dong4j.idea.plugin.settings.ProjectSettingsPage#testAndHelpListener()}
     *
     * @param inputStream the input stream
     * @param fileName    the file name
     * @param jPanel      the j panel
     * @return the string
     */
    @Override
    public String upload(InputStream inputStream, String fileName, JPanel jPanel) {
        Map<String, String> map = getTestFieldText(jPanel);
        int zoneIndex = Integer.parseInt(map.get("zoneIndex"));
        String bucketName = map.get("bucketName");
        String accessKey = map.get("accessKey");
        String secretKey = map.get("secretKey");
        String endpoint = map.get("domain");

        return upload(inputStream,
                      fileName,
                      bucketName,
                      accessKey,
                      secretKey,
                      endpoint,
                      zoneIndex);
    }

    /**
     * test 按钮点击事件后请求, 成功后保留 client, paste 或者 右键 上传时使用
     *
     * @param inputStream the input stream
     * @param fileName    the file name
     * @param bucketName  the bucketName name
     * @param accessKey   the access key
     * @param secretKey   the secret key
     * @param endpoint    the endpoint
     * @param zoneIndex   the zone index
     * @return the string
     */
    @NotNull
    @Contract(pure = true)
    public String upload(InputStream inputStream,
                         String fileName,
                         String bucketName,
                         String accessKey,
                         String secretKey,
                         String endpoint,
                         int zoneIndex) {


        QiniuOssClient qiniuOssClient = QiniuOssClient.getInstance();
        Optional<ZoneEnum> zone = EnumsUtils.getEnumObject(ZoneEnum.class, e -> e.getIndex() == zoneIndex);
        Configuration cfg = new Configuration(zone.orElse(ZoneEnum.EAST_CHINA).zone);
        UploadManager ossClient = new UploadManager(cfg);
        Auth auth = Auth.create(accessKey, secretKey);
        // 重新生成 token
        QiniuOssClient.buildToken(auth, bucketName);

        qiniuOssClient.setDomain(endpoint);
        String url = qiniuOssClient.upload(ossClient, inputStream, fileName);

        if (StringUtils.isNotBlank(url)) {
            int hashcode = bucketName.hashCode() +
                           accessKey.hashCode() +
                           secretKey.hashCode() +
                           endpoint.hashCode() +
                           zoneIndex;
            OssState.saveStatus(qiniuOssState, hashcode, MikState.OLD_HASH_KEY);
            qiniuOssClient.setOssClient(ossClient);
        }
        return url;
    }

    /**
     * Gets instance.
     *
     * @return the instance
     */
    @Contract(pure = true)
    public static QiniuOssClient getInstance() {
        return QiniuOssClient.SingletonHandler.singleton;
    }

    /**
     * Set domain.
     *
     * @param newDomain the new domain
     */
    private void setDomain(String newDomain) {
        domain = newDomain;
    }

    /**
     * Set oss client.
     *
     * @param oss the oss
     */
    private void setOssClient(UploadManager oss) {
        ossClient = oss;
    }

    /**
     * Upload string.
     *
     * @param ossClient   the oss client
     * @param inputStream the input stream
     * @param fileName    the file name
     * @return the string
     */
    public String upload(UploadManager ossClient, InputStream inputStream, String fileName) {
        try {
            ossClient.put(inputStream, fileName, token, null, null);
            // 拼接 url, 需要正确配置域名 (https://developer.qiniu.com/fusion/kb/1322/how-to-configure-cname-domain-name)
            URL url = new URL(domain);
            log.trace("getUserInfo = {}", url.getUserInfo());
            if (StringUtils.isBlank(url.getPath())) {
                domain = domain + "/";
            } else {
                domain = domain.endsWith("/") ? domain : domain + "/";
            }
            return domain + fileName;
        } catch (QiniuException ex) {
            Response r = ex.response;
            log.trace(r.toString());
            try {
                log.trace(r.bodyString());
            } catch (QiniuException ignored) {
            }
        } catch (MalformedURLException e) {
            log.trace("", e);
        }
        return "";
    }

    private static class SingletonHandler {
        private static QiniuOssClient singleton = new QiniuOssClient();

        static {
            checkClient();
        }
    }
}
