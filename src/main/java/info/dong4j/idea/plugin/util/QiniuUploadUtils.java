package info.dong4j.idea.plugin.util;

import com.google.gson.Gson;

import com.qiniu.common.QiniuException;
import com.qiniu.common.Zone;
import com.qiniu.http.Response;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.UploadManager;
import com.qiniu.storage.model.DefaultPutRet;
import com.qiniu.util.Auth;

/**
 * <p>Company: 科大讯飞股份有限公司-四川分公司</p>
 * <p>Description: </p>
 *
 * @author dong4j
 * @date 2019 -03-16 12:12
 * @email sjdong3 @iflytek.com
 */
public final class QiniuUploadUtils {

    /**
     * Upload image string.
     *
     * @param uploadBytes the upload bytes
     * @param preFix      the pre fix
     * @param accessKey   the access key
     * @param secretKey   the secret key
     * @param bucket      the bucketName
     * @return the string
     */
    static String uploadImage(byte[] uploadBytes, String preFix, String accessKey, String secretKey, String bucket) {
        if (preFix == null || preFix.isEmpty()) {
            preFix = "markdown-image-paste";
        }

        Configuration cfg = new Configuration(Zone.zone2());
        UploadManager uploadManager = new UploadManager(cfg);
        String key = "";
        Auth auth = Auth.create(accessKey, secretKey);
        String upToken = auth.uploadToken(bucket);
        System.out.println("token: " + upToken);
        try {
            Response response = uploadManager.put(uploadBytes, preFix + "-" + System.currentTimeMillis(), upToken);
            DefaultPutRet putRet = new Gson().fromJson(response.bodyString(), DefaultPutRet.class);
            key = putRet.key;
            System.out.println("key: " + putRet.key);
            System.out.println("hash: " + putRet.hash);
        } catch (QiniuException ex) {
            Response r = ex.response;
            System.err.println(r.toString());
            try {
                System.err.println(r.bodyString());
            } catch (QiniuException ignored) {
            }
        }
        return key;
    }

    /**
     * Gets token.
     *
     * @param accessKey the access key
     * @param secretKey the secret key
     * @param bucket    the bucketName
     * @return the token
     */
    public static String getToken(String accessKey, String secretKey, String bucket) {
        Auth auth = Auth.create(accessKey, secretKey);
        return auth.uploadToken(bucket);
    }
}