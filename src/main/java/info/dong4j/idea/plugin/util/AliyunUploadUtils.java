package info.dong4j.idea.plugin.util;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.ObjectMetadata;
import com.aliyun.oss.model.PutObjectResult;

import info.dong4j.idea.plugin.enums.SuffixSelectTypeEnum;
import info.dong4j.idea.plugin.exception.ImgException;
import info.dong4j.idea.plugin.settings.OssPersistenConfig;

import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.Date;

import lombok.extern.slf4j.Slf4j;

/**
 * <p>Company: 科大讯飞股份有限公司-四川分公司</p>
 * <p>Description: Aliyun OSS 文件上传</p>
 *
 * @author dong4j
 * @email sjdong3 @iflytek.com
 * @since 2019 -03-13 10:33
 */
@Slf4j
public final class AliyunUploadUtils {
    public static final String URL_PROTOCOL_HTTP = "http";
    public static final String URL_PROTOCOL_HTTPS = "https";
    private static String bucketName;
    /** 文件存储目录 */
    private static String filedir;
    private static OSS ossClient;
    private static String sufix;
    private static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-");

    static {
        init();
    }

    private static void init() {
        OssPersistenConfig ossPersistenConfig = OssPersistenConfig.getInstance();
        bucketName = ossPersistenConfig.getState().getAliyunOssState().getBucketName();
        String accessKeyId = ossPersistenConfig.getState().getAliyunOssState().getAccessKey();
        String accessKeySecret = ossPersistenConfig.getState().getAliyunOssState().getAccessSecretKey();
        String endpoint = ossPersistenConfig.getState().getAliyunOssState().getEndpoint();
        String tempFileDir = ossPersistenConfig.getState().getAliyunOssState().getFiledir();
        filedir = StringUtils.isBlank(tempFileDir) ? "" : tempFileDir + "/";
        sufix = ossPersistenConfig.getState().getAliyunOssState().getSuffix();
        try {
            OSSClientBuilder ossClientBuilder = new OSSClientBuilder();
            ossClient = ossClientBuilder.build(endpoint, accessKeyId, accessKeySecret);
        } catch (Exception ignored) {
        }
    }

    public static void reset() {
        init();
    }

    public static void destory() {
        if (ossClient != null) {
            ossClient.shutdown();
        }
    }

    /**
     * 上传图片
     *
     * @param url the url
     */
    public static void uploadImg2Oss(String url) {
        File fileOnServer = new File(url);
        FileInputStream fin;
        try {
            fin = new FileInputStream(fileOnServer);
            String[] split = url.split("/");
            uploadFile2OSS(fin, split[split.length - 1]);
        } catch (FileNotFoundException e) {
            throw new ImgException("图片上传失败");
        }
    }

    /**
     * Upload img 2 ossClient string.
     *
     * @param file the file
     * @return the string
     */
    public static String uploadImg2Oss(File file) {
        String name = getSufixName(file.getName());
        try {
            uploadFile2OSS(new FileInputStream(file), name);
            return name;
        } catch (Exception e) {
            throw new ImgException("upload error");
        }
    }

    private static String getSufixName(String fileName) {
        if (SuffixSelectTypeEnum.FILE_NAME.name.equals(sufix)) {
            return fileName;
        } else if (SuffixSelectTypeEnum.DATE_FILE_NAME.name.equals(sufix)) {
            // todo-dong4j : (2019年03月13日 18:01) [修改为线程安全的]
            return dateFormat.format(new Date()) + fileName;
        } else if (SuffixSelectTypeEnum.RANDOM.name.equals(sufix)) {
            return CharacterUtils.getRandomString(12) + fileName.substring(fileName.lastIndexOf("."));
        } else {
            return "";
        }
    }

    /**
     * 上传到OSS服务器  如果同名文件会覆盖服务器上的
     *
     * @param instream 文件流
     * @param fileName 文件名称 包括后缀名
     * @return 出错返回 "" ,唯一MD5数字签名
     */
    public static String uploadFile2OSS(InputStream instream, String fileName) {
        return uploadFile2OSS(instream, filedir, fileName);
    }


    /**
     * Upload file 2 ossClient string.
     *
     * @param instream the instream
     * @param filedir  the filedir
     * @param fileName the file name
     * @return the string
     */
    public static String uploadFile2OSS(InputStream instream, String filedir, String fileName) {
        return uploadFile2OSS(ossClient, instream, filedir, fileName);
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
    public static String uploadFile2OSS(OSS ossClient,
                                        InputStream instream,
                                        String filedir,
                                        String fileName) {
        String ret = "";
        try {
            // 创建上传 Object 的 Metadata
            ObjectMetadata objectMetadata = new ObjectMetadata();
            objectMetadata.setContentLength(instream.available());
            objectMetadata.setCacheControl("no-cache");
            objectMetadata.setHeader("Pragma", "no-cache");
            objectMetadata.setContentType(getcontentType(fileName.substring(fileName.lastIndexOf("."))));
            objectMetadata.setContentDisposition("inline;filename=" + fileName);
            // 上传文件
            PutObjectResult putResult = ossClient.putObject(bucketName, filedir + fileName, instream, objectMetadata);
            ret = putResult.getETag();
        } catch (IOException e) {
            log.trace(e.getMessage(), e);
        } finally {
            try {
                if (instream != null) {
                    instream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return ret;
    }

    /**
     * Description: 判断OSS服务文件上传时文件的contentType
     *
     * @param FilenameExtension 文件后缀
     * @return String
     */
    private static String getcontentType(String FilenameExtension) {
        if (".bmp".equalsIgnoreCase(FilenameExtension)) {
            return "image/bmp";
        }
        if (".gif".equalsIgnoreCase(FilenameExtension)) {
            return "image/gif";
        }
        if (".jpeg".equalsIgnoreCase(FilenameExtension) ||
            ".jpg".equalsIgnoreCase(FilenameExtension) ||
            ".png".equalsIgnoreCase(FilenameExtension)) {
            return "image/jpeg";
        }
        if (".html".equalsIgnoreCase(FilenameExtension)) {
            return "text/html";
        }
        if (".txt".equalsIgnoreCase(FilenameExtension)) {
            return "text/plain";
        }
        if (".vsd".equalsIgnoreCase(FilenameExtension)) {
            return "application/vnd.visio";
        }
        if (".pptx".equalsIgnoreCase(FilenameExtension) ||
            ".ppt".equalsIgnoreCase(FilenameExtension)) {
            return "application/vnd.ms-powerpoint";
        }
        if (".docx".equalsIgnoreCase(FilenameExtension) ||
            ".doc".equalsIgnoreCase(FilenameExtension)) {
            return "application/msword";
        }
        if (".xml".equalsIgnoreCase(FilenameExtension)) {
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
    public static String getUrl(String name) {
        return getUrl(filedir, name);
    }

    /**
     * Gets url.
     *
     * @param filedir the filedir
     * @param name    the name
     * @return the url
     */
    public static String getUrl(String filedir, String name) {
        // 设置URL过期时间为10年  3600l* 1000*24*365*10
        Date expiration = new Date(System.currentTimeMillis() + 3600L * 1000 * 24 * 365 * 10);
        // 生成URL
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
