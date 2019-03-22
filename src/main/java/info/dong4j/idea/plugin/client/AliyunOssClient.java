package info.dong4j.idea.plugin.client;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.ObjectMetadata;

import info.dong4j.idea.plugin.MikBundle;
import info.dong4j.idea.plugin.settings.AliyunOssState;
import info.dong4j.idea.plugin.settings.ImageManagerPersistenComponent;
import info.dong4j.idea.plugin.settings.ImageManagerState;
import info.dong4j.idea.plugin.settings.OssState;
import info.dong4j.idea.plugin.util.DES;
import info.dong4j.idea.plugin.util.ImageUtils;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.net.*;
import java.util.Date;
import java.util.Map;

import javax.swing.JPanel;

import lombok.extern.slf4j.Slf4j;

/**
 * <p>Company: 科大讯飞股份有限公司-四川分公司</p>
 * <p>Description: 右键上传一次或者点击测试按钮时初始化一次</p>
 *
 * @author dong4j
 * @email sjdong3 @iflytek.com
 * @since 2019 -03-18 09:57
 */
@Slf4j
public class AliyunOssClient extends AbstractOssClient {
    /**
     * The constant URL_PROTOCOL_HTTPS.
     */
    public static final String URL_PROTOCOL_HTTPS = "https";
    private static final String URL_PROTOCOL_HTTP = "http";
    private AliyunOssState aliyunOssState = ImageManagerPersistenComponent.getInstance().getState().getAliyunOssState();
    private static final Object LOCK = new Object();
    private static String bucketName;
    private static String filedir;
    private static OSS ossClient = null;

    private AliyunOssClient() {
        checkClient();
    }

    private static class SingletonHandler {
        static {
            checkClient();
        }

        private static AliyunOssClient singleton = new AliyunOssClient();
    }

    /**
     * 如果是第一次使用, ossClient == null
     */
    private static void init() {
        AliyunOssState aliyunOssState = ImageManagerPersistenComponent.getInstance().getState().getAliyunOssState();
        bucketName = aliyunOssState.getBucketName();
        String accessKey = aliyunOssState.getAccessKey();
        String accessSecretKey = DES.decrypt(aliyunOssState.getAccessSecretKey(), ImageManagerState.ALIYUN);
        String endpoint = aliyunOssState.getEndpoint();
        String tempFileDir = aliyunOssState.getFiledir();
        filedir = StringUtils.isBlank(tempFileDir) ? "" : tempFileDir + "/";
        try {
            ossClient = new OSSClientBuilder().build(endpoint, accessKey, accessSecretKey);
        } catch (Exception ignored) {
        }
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
     * Set bucket name.
     *
     * @param newBucketName the new bucket name
     */
    private void setBucketName(String newBucketName) {
        bucketName = newBucketName;
    }

    /**
     * Gets instance.
     *
     * @return the instance
     */
    @Contract(pure = true)
    public static AliyunOssClient getInstance() {
        return SingletonHandler.singleton;
    }

    /**
     * Set oss client.
     *
     * @param oss the oss
     */
    private void setOssClient(OSS oss) {
        ossClient = oss;
    }

    /**
     * Gets url.
     *
     * @param ossClient the oss client
     * @param filedir   the filedir
     * @param fileName  the name
     * @return the url
     */
    private String getUrl(@NotNull OSS ossClient, String filedir, String fileName) {
        Date expiration = new Date(System.currentTimeMillis() + 3600L * 1000 * 24 * 365 * 10);
        URL url = ossClient.generatePresignedUrl(bucketName, filedir + fileName, expiration);
        if (url != null) {
            String[] split = url.toString().split("\\?");
            String uri = split[0];
            if (url.getProtocol().equals(URL_PROTOCOL_HTTP)) {
                uri = uri.replace(URL_PROTOCOL_HTTP, URL_PROTOCOL_HTTPS);
            }
            return uri;
        }
        return "";
    }

    @Override
    public String getName() {
        return MikBundle.message("oss.client.aliyun");
    }

    /**
     * Upload string.
     *
     * @param file the file
     * @return the string
     */
    @Override
    public String upload(File file) {
        try (BufferedInputStream bufferedInputStream = new BufferedInputStream(new FileInputStream(file))) {
            return upload(bufferedInputStream, file.getName());
        } catch (IOException e) {
            log.trace("", e);
        }
        return "";
    }

    /**
     * 上传到OSS服务器  如果同名文件会覆盖服务器上的
     *
     * @param inputStream 文件流
     * @param fileName    文件名称 包括后缀名
     * @return 出错返回 "" ,唯一MD5数字签名
     */
    @Override
    public String upload(InputStream inputStream, String fileName) {
        return upload(inputStream, filedir, fileName);
    }

    /**
     * Upload string.
     *
     * @param inputStream the inputStream
     * @param filedir     the filedir
     * @param fileName    the file name
     * @return the string
     */
    public String upload(InputStream inputStream, String filedir, String fileName) {
        return upload(ossClient, inputStream, filedir, fileName);
    }

    /**
     * Upload string.
     *
     * @param ossClient the ossClient client
     * @param instream  the instream
     * @param filedir   the filedir
     * @param fileName  the file name
     * @return the string
     */
    public String upload(OSS ossClient,
                         @NotNull InputStream instream,
                         String filedir,
                         @NotNull String fileName) {
        fileName = processFileName(fileName);
        try {
            // 创建上传 Object 的 Metadata
            ObjectMetadata objectMetadata = new ObjectMetadata();
            objectMetadata.setContentLength(instream.available());
            objectMetadata.setCacheControl("no-cache");
            objectMetadata.setHeader("Pragma", "no-cache");
            objectMetadata.setContentType(ImageUtils.getImageType(fileName));
            objectMetadata.setContentDisposition("inline;filename=" + fileName);
            ossClient.putObject(bucketName, filedir + fileName, instream, objectMetadata);
            return getUrl(ossClient, filedir, fileName);
        } catch (IOException e) {
            log.trace("", e);
        } finally {
            try {
                instream.close();
            } catch (IOException e) {
                log.trace("", e);
            }
        }
        return "";
    }

    /**
     * 直接从面板组件上获取最新配置, 不使用 state
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

        String bucketName = map.get("bucketName");
        String accessKey = map.get("accessKey");
        String accessSecretKey = map.get("secretKey");
        String endpoint = map.get("endpoint");
        String filedir = map.get("filedir");

        return upload(inputStream,
                      fileName,
                      bucketName,
                      accessKey,
                      accessSecretKey,
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
     */
    private String upload(InputStream inputStream,
                          String fileName,
                          String bucketName,
                          String accessKey,
                          String accessSecretKey,
                          String endpoint,
                          String filedir) {

        filedir = org.apache.commons.lang.StringUtils.isBlank(filedir) ? "" : filedir + "/";

        AliyunOssClient aliyunOssClient = AliyunOssClient.getInstance();
        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKey, accessSecretKey);

        aliyunOssClient.setBucketName(bucketName);
        String url = aliyunOssClient.upload(ossClient, inputStream, filedir, fileName);

        if (org.apache.commons.lang.StringUtils.isNotBlank(url)) {
            int hashcode = bucketName.hashCode() +
                           accessKey.hashCode() +
                           accessSecretKey.hashCode() +
                           endpoint.hashCode();
            OssState.saveStatus(aliyunOssState, hashcode, ImageManagerState.OLD_HASH_KEY);
            aliyunOssClient.setOssClient(ossClient);
        }
        return url;
    }
}
