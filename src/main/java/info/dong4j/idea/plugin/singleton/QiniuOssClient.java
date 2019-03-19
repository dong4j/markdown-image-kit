package info.dong4j.idea.plugin.singleton;

import com.qiniu.common.QiniuException;
import com.qiniu.common.Zone;
import com.qiniu.http.Response;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.UploadManager;
import com.qiniu.util.Auth;

import info.dong4j.idea.plugin.settings.ImageManagerPersistenComponent;
import info.dong4j.idea.plugin.settings.ImageManagerState;
import info.dong4j.idea.plugin.settings.QiniuOssState;
import info.dong4j.idea.plugin.util.DES;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.*;

import lombok.extern.slf4j.Slf4j;

/**
 * <p>Company: 科大讯飞股份有限公司-四川分公司</p>
 * <p>Description: </p>
 *
 * @author dong4j
 * @email sjdong3 @iflytek.com
 * @since 2019 -03-19 15:25
 */
@Slf4j
public class QiniuOssClient {
    /**
     * The constant bucketName.
     */
    public static String bucketName;
    /**
     * The constant upToken.
     */
    public static String upToken;

    private final Object lock = new Object();
    private static UploadManager ossClient = null;
    private static String endpoint;

    private QiniuOssClient() {
    }

    private static class SingletonHandler {
        static {
            init();
        }

        private static QiniuOssClient singleton = new QiniuOssClient();
    }

    /**
     * 如果是第一次使用, ossClient == null
     */
    private static void init() {
        QiniuOssState qiniuOssState = ImageManagerPersistenComponent.getInstance().getState().getQiniuOssState();
        endpoint = qiniuOssState.getEndpoint();
        String accessKey = qiniuOssState.getAccessKey();
        String secretKey = DES.decrypt(qiniuOssState.getAccessSecretKey(), ImageManagerState.QINIU);
        bucketName = qiniuOssState.getBucketName();
        // 设置区域
        Configuration cfg = new Configuration(Zone.zone2());
        ossClient = new UploadManager(cfg);
        Auth auth = Auth.create(accessKey, secretKey);
        upToken = auth.uploadToken(bucketName);
    }

    /**
     * 在调用 ossClient 之前先检查, 如果为 null 就 init()
     */
    private boolean checkClient() {
        synchronized (lock) {
            if (ossClient == null) {
                init();
            }
            return ossClient != null;
        }
    }

    /**
     * Set oss client.
     *
     * @param oss the oss
     */
    public void setOssClient(UploadManager oss) {
        ossClient = oss;
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
     * Upload string.
     *
     * @param file the file
     * @return the string
     */
    @NotNull
    public String upload(File file) {
        try (BufferedInputStream bufferedInputStream = new BufferedInputStream(new FileInputStream(file))) {
            return upload(ossClient, bufferedInputStream, file.getName());
        } catch (IOException e) {
            log.trace("", e);
        }
        return "";
    }

    /**
     * Upload string.
     *
     * @param inputStream the input stream
     * @param fileName    the file name
     * @return the string
     */
    public String upload(InputStream inputStream, String fileName) {
        return upload(ossClient, inputStream, fileName);
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
            ossClient.put(inputStream, fileName, upToken, null, null);
            // 拼接 url, 需要正确配置域名 (https://developer.qiniu.com/fusion/kb/1322/how-to-configure-cname-domain-name)
            return endpoint + "/" + fileName;
        } catch (QiniuException ex) {
            Response r = ex.response;
            log.trace(r.toString());
            try {
                log.info(r.bodyString());
            } catch (QiniuException ignored) {
            }
        }
        return "";
    }
}
