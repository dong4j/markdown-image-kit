package info.dong4j.idea.plugin.singleton;

import com.google.gson.Gson;

import com.intellij.testFramework.LightPlatformTestCase;
import com.qiniu.common.QiniuException;
import com.qiniu.common.Zone;
import com.qiniu.http.Response;
import com.qiniu.storage.BucketManager;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.UploadManager;
import com.qiniu.storage.model.DefaultPutRet;
import com.qiniu.storage.model.FileInfo;
import com.qiniu.util.Auth;

import java.io.*;
import java.nio.charset.StandardCharsets;

import lombok.extern.slf4j.Slf4j;

/**
 * <p>Company: 科大讯飞股份有限公司-四川分公司</p>
 * <p>Description: ${description}</p>
 *
 * @author dong4j
 * @date 2019-03-19 15:35
 * @email sjdong3@iflytek.com
 */
@Slf4j
public class QiniuOssClientTest extends LightPlatformTestCase {
    public void test() {
        String bucket = "markdown-images";
        String accessKey = "9VECTxfLzW5gF1rtvNw-Iht_JzztUyviBl9bI3dh";
        String secretKey = "7d0PD-goR08lRip5qAlD9GDyrs2CB3TvNHi4eUW7";
        String preFix = "markdown-image-paste";
        String endpoint = "http://qiniu.dong4j.info/";

        String url = upload(new File("/Users/dong4j/Downloads/我可要开始皮了.png"),
               bucket,
               accessKey,
               secretKey,
               endpoint);
        log.info("{}", url);
    }

    public static String upload(InputStream inputStream, String bucket, String preFix, String accessKey, String secretKey) {
        //构造一个带指定Zone对象的配置类
        Configuration cfg = new Configuration(Zone.zone0());
        //...其他参数参考类注释

        UploadManager uploadManager = new UploadManager(cfg);

        //默认不指定key的情况下，以文件内容的hash值作为文件名
        String key = null;

        byte[] uploadBytes = "hello qiniu cloud".getBytes(StandardCharsets.UTF_8);
        ByteArrayInputStream byteInputStream = new ByteArrayInputStream(uploadBytes);
        Auth auth = Auth.create(accessKey, secretKey);
        String upToken = auth.uploadToken(bucket);

        try {
            Response response = uploadManager.put(byteInputStream, key, upToken, null, null);
            //解析上传成功的结果
            DefaultPutRet putRet = new Gson().fromJson(response.bodyString(), DefaultPutRet.class);
            log.info(putRet.key);
            log.info(putRet.hash);
        } catch (QiniuException ex) {
            Response r = ex.response;
            log.info(r.toString());
            try {
                log.info(r.bodyString());
            } catch (QiniuException ex2) {
                //ignore
            }
        }

        return "";
    }

    public static String upload(byte[] uploadBytes, String preFix, String bucket, String accessKey, String secretKey) {
        Configuration cfg = new Configuration(Zone.zone2());
        UploadManager uploadManager = new UploadManager(cfg);
        String key = "";
        Auth auth = Auth.create(accessKey, secretKey);
        String upToken = auth.uploadToken(bucket);
        log.info("token: " + upToken);
        try {
            Response response = uploadManager.put(uploadBytes, preFix + "-" + System.currentTimeMillis(), upToken);
            DefaultPutRet putRet = new Gson().fromJson(response.bodyString(), DefaultPutRet.class);
            key = putRet.key;
            log.info("key: " + putRet.key);
            log.info("hash: " + putRet.hash);
        } catch (QiniuException ex) {
            Response r = ex.response;
            log.info(r.toString());
            try {
                log.info(r.bodyString());
            } catch (QiniuException ignored) {
            }
        }
        return key;
    }

    public static String upload(File file, String bucket, String accessKey, String secretKey, String endpoint) {
        //构造一个带指定Zone对象的配置类
        Configuration cfg = new Configuration(Zone.zone0());
        //...其他参数参考类注释

        UploadManager uploadManager = new UploadManager(cfg);
        //默认不指定key的情况下，以文件内容的hash值作为文件名
        String key = "test.png";

        Auth auth = Auth.create(accessKey, secretKey);
        String upToken = auth.uploadToken(bucket);
        BucketManager bucketManager = new BucketManager(auth, cfg);

        try {
            Response response = uploadManager.put(file, key, upToken);
            //解析上传成功的结果
            DefaultPutRet putRet = new Gson().fromJson(response.bodyString(), DefaultPutRet.class);
            log.info(putRet.key);
            log.info(putRet.hash);

            FileInfo fileInfo = bucketManager.stat(bucket, key);
            System.out.println(fileInfo.hash);
            System.out.println(fileInfo.fsize);
            System.out.println(fileInfo.mimeType);
            System.out.println(fileInfo.putTime);
            return endpoint + key;
        } catch (QiniuException ex) {
            Response r = ex.response;
            log.info(r.toString());
            try {
                log.info(r.bodyString());
            } catch (QiniuException ex2) {
                //ignore
            }
        }
        return "";
    }

    public void test1(){
        QiniuOssClient qiniuOssClient = QiniuOssClient.getInstance();
        String url = qiniuOssClient.upload(new File("/Users/dong4j/Downloads/我可要开始皮了.png"));
        log.info(url);
    }
}