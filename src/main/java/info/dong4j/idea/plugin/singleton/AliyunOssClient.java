package info.dong4j.idea.plugin.singleton;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.ObjectMetadata;

import info.dong4j.idea.plugin.enums.SuffixEnum;
import info.dong4j.idea.plugin.settings.AliyunOssState;
import info.dong4j.idea.plugin.settings.ImageManagerPersistenComponent;
import info.dong4j.idea.plugin.settings.ImageManagerState;
import info.dong4j.idea.plugin.util.CharacterUtils;
import info.dong4j.idea.plugin.util.DES;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.Date;

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
public class AliyunOssClient {
    private static final String URL_PROTOCOL_HTTP = "http";
    /**
     * The constant URL_PROTOCOL_HTTPS.
     */
    public static final String URL_PROTOCOL_HTTPS = "https";
    private final Object lock = new Object();
    private static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-");

    /**
     * The constant bucketName.
     */
    public static String bucketName;
    private static String fileDir;
    private static String sufix;
    private static OSS ossClient = null;

    private AliyunOssClient() {
    }

    private static class SingletonHandler {
        static {
            init();
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
        fileDir = StringUtils.isBlank(tempFileDir) ? "" : tempFileDir + "/";
        sufix = aliyunOssState.getSuffix();
        try {
            ossClient = new OSSClientBuilder().build(endpoint, accessKey, accessSecretKey);
        } catch (Exception ignored) {
        }
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
    public void setOssClient(OSS oss) {
        ossClient = oss;
    }

    /**
     * Upload img 2 ossClient string.
     *
     * @param file the file
     * @return the string
     */
    public String upload(File file) {
        String name = getSufixName(file.getName());
        try {
            return upload(new FileInputStream(file), name);
        } catch (Exception e) {
            log.trace("", e);
        }
        return "";
    }

    private static String getSufixName(String fileName) {
        if (SuffixEnum.FILE_NAME.name.equals(sufix)) {
            return fileName;
        } else if (SuffixEnum.DATE_FILE_NAME.name.equals(sufix)) {
            // todo-dong4j : (2019年03月13日 18:01) [修改为线程安全的]
            return dateFormat.format(new Date()) + fileName;
        } else if (SuffixEnum.RANDOM.name.equals(sufix)) {
            return CharacterUtils.getRandomString(8) + fileName.substring(fileName.lastIndexOf("."));
        } else {
            return fileName;
        }
    }

    /**
     * 上传到OSS服务器  如果同名文件会覆盖服务器上的
     *
     * @param inputStream 文件流
     * @param fileName    文件名称 包括后缀名
     * @return 出错返回 "" ,唯一MD5数字签名
     */
    private String upload(InputStream inputStream, String fileName) {
        return upload(inputStream, fileDir, fileName);
    }


    /**
     * Upload file 2 ossClient string.
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
     * Upload file 2 ossClient string.
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
        try {
            // 创建上传 Object 的 Metadata
            ObjectMetadata objectMetadata = new ObjectMetadata();
            objectMetadata.setContentLength(instream.available());
            objectMetadata.setCacheControl("no-cache");
            objectMetadata.setHeader("Pragma", "no-cache");
            objectMetadata.setContentType(getcontentType(fileName.substring(fileName.lastIndexOf("."))));
            objectMetadata.setContentDisposition("inline;filename=" + fileName);
            if (checkClient()) {
                ossClient.putObject(bucketName, filedir + fileName, instream, objectMetadata);
                return getUrl(filedir, fileName);
            }
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
     * Description: 判断OSS服务文件上传时文件的contentType
     *
     * @param filenameExtension 文件后缀
     * @return String
     */
    private String getcontentType(String filenameExtension) {
        if (".bmp".equalsIgnoreCase(filenameExtension)) {
            return "image/bmp";
        }
        if (".gif".equalsIgnoreCase(filenameExtension)) {
            return "image/gif";
        }
        if (".jpeg".equalsIgnoreCase(filenameExtension) ||
            ".jpg".equalsIgnoreCase(filenameExtension) ||
            ".png".equalsIgnoreCase(filenameExtension)) {
            return "image/jpeg";
        }
        if (".html".equalsIgnoreCase(filenameExtension)) {
            return "text/html";
        }
        if (".txt".equalsIgnoreCase(filenameExtension)) {
            return "text/plain";
        }
        if (".vsd".equalsIgnoreCase(filenameExtension)) {
            return "application/vnd.visio";
        }
        if (".pptx".equalsIgnoreCase(filenameExtension) ||
            ".ppt".equalsIgnoreCase(filenameExtension)) {
            return "application/vnd.ms-powerpoint";
        }
        if (".docx".equalsIgnoreCase(filenameExtension) ||
            ".doc".equalsIgnoreCase(filenameExtension)) {
            return "application/msword";
        }
        if (".xml".equalsIgnoreCase(filenameExtension)) {
            return "text/xml";
        }
        return "image/jpeg";
    }

    /**
     * 获得url链接
     *
     * @param name the name
     * @return url url
     */
    public String getUrl(String name) {
        if (checkClient()) {
            return getUrl(fileDir, name);
        }
        return "";
    }

    /**
     * Gets url.
     *
     * @param filedir the filedir
     * @param name    the name
     * @return the url
     */
    public String getUrl(String filedir, String name) {
        return getUrl(ossClient, filedir, name);
    }

    /**
     * Gets url.
     *
     * @param ossClient the oss client
     * @param filedir   the filedir
     * @param name      the name
     * @return the url
     */
    public String getUrl(OSS ossClient, String filedir, String name) {
        Date expiration = new Date(System.currentTimeMillis() + 3600L * 1000 * 24 * 365 * 10);
        URL url = ossClient.generatePresignedUrl(bucketName, filedir + name, expiration);
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
}
