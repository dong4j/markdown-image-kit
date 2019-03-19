package info.dong4j.idea.plugin.strategy;

import com.qiniu.common.Zone;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.UploadManager;
import com.qiniu.util.Auth;

import info.dong4j.idea.plugin.settings.ImageManagerPersistenComponent;
import info.dong4j.idea.plugin.settings.ImageManagerState;
import info.dong4j.idea.plugin.settings.OssState;
import info.dong4j.idea.plugin.settings.QiniuOssState;
import info.dong4j.idea.plugin.singleton.QiniuOssClient;
import info.dong4j.idea.plugin.util.DES;

import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.util.List;

import javax.swing.JPanel;

import lombok.extern.slf4j.Slf4j;

/**
 * <p>Company: 科大讯飞股份有限公司-四川分公司</p>
 * <p>Description: </p>
 *
 * @author dong4j
 * @email sjdong3 @iflytek.com
 * @since 2019 -03-19 15:24
 */
@Slf4j
public class QiniuUploadStrategy implements UploadStrategy {
    private String endpoint;
    private String accessKey;
    private String secretKey;
    private String bucketName;

    private QiniuOssState qiniuOssState = ImageManagerPersistenComponent.getInstance().getState().getQiniuOssState();

    @Override
    public String upload(InputStream inputStream, String fileName) {
        return uploadFromState(inputStream, fileName);
    }

    /**
     * Upload from state string.
     *
     * @param inputStream the input stream
     * @param fileName    the file name
     * @return the string
     */
    @NotNull
    private String uploadFromState(InputStream inputStream, String fileName) {
        endpoint = qiniuOssState.getEndpoint();
        accessKey = qiniuOssState.getAccessKey();
        secretKey = DES.decrypt(qiniuOssState.getAccessSecretKey(), ImageManagerState.QINIU);
        bucketName = qiniuOssState.getBucketName();

        return upload(inputStream,
                      fileName,
                      bucketName,
                      accessKey,
                      secretKey,
                      endpoint, UploadWayEnum.FROM_PASTE);
    }

    /**
     * Upload from test string.
     * {@link info.dong4j.idea.plugin.settings.ProjectSettingsPage #upload}
     *
     * @param inputStream the input stream
     * @param fileName    the file name
     * @param jPanel      the j panel
     * @return the string
     */
    public String uploadFromTest(InputStream inputStream, String fileName, JPanel jPanel) {
        List<String> textList = getTestFieldText(jPanel);
        endpoint = textList.get(0);
        accessKey = textList.get(1);
        secretKey = textList.get(2);
        bucketName = textList.get(3);

        return upload(inputStream,
                      fileName,
                      bucketName,
                      accessKey,
                      secretKey,
                      endpoint, UploadWayEnum.FROM_TEST);
    }

    /**
     * Upload string.
     *
     * @param inputStream   the input stream
     * @param fileName      the file name
     * @param bucketName    the bucketName name
     * @param accessKey     the access key
     * @param secretKey     the secret key
     * @param endpoint      the endpoint
     * @param uploadWayEnum the upload way enum
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
                         UploadWayEnum uploadWayEnum) {

        String url;
        if (uploadWayEnum.equals(UploadWayEnum.FROM_TEST)) {
            Configuration cfg = new Configuration(Zone.zone0());
            UploadManager ossClient = new UploadManager(cfg);
            Auth auth = Auth.create(accessKey, secretKey);
            String upToken = auth.uploadToken(bucketName);

            QiniuOssClient.bucketName = bucketName;
            QiniuOssClient.upToken = upToken;

            url = QiniuOssClient.getInstance().upload(ossClient, inputStream, fileName);

            if (StringUtils.isNotBlank(url)) {
                int hashcode = bucketName.hashCode() +
                               accessKey.hashCode() +
                               secretKey.hashCode() +
                               endpoint.hashCode();
                OssState.saveStatus(qiniuOssState, hashcode, ImageManagerState.OLD_HASH_KEY);

                // 参数验证成功后直接设置 ossClient, 不要浪费
                QiniuOssClient.getInstance().setOssClient(ossClient);
            }
        } else {
            url = QiniuOssClient.getInstance().upload(inputStream, fileName);
        }
        return url;
    }
}
