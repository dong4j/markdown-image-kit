package info.dong4j.idea.plugin.strategy;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;

import info.dong4j.idea.plugin.settings.AliyunOssState;
import info.dong4j.idea.plugin.settings.ImageManagerPersistenComponent;
import info.dong4j.idea.plugin.settings.ImageManagerState;
import info.dong4j.idea.plugin.settings.OssState;
import info.dong4j.idea.plugin.singleton.AliyunOssClient;
import info.dong4j.idea.plugin.util.DES;

import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.util.Map;

import javax.swing.JPanel;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * <p>Company: 科大讯飞股份有限公司-四川分公司</p>
 * <p>Description: used by {@link info.dong4j.idea.plugin.handler.PasteImageHandler #upload}
 * and {@link info.dong4j.idea.plugin.settings.ProjectSettingsPage #testAndHelpListener}
 * </p>
 *
 * @author dong4j
 * @email sjdong3 @iflytek.com
 * @since 2019 -03-17 02:11
 */
@Slf4j
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AliyunUploadStrategy implements UploadStrategy {
    private String bucketName;
    private String accessKey;
    private String accessSecretKey;
    private String endpoint;
    private String filedir;

    private AliyunOssState aliyunOssState = ImageManagerPersistenComponent.getInstance().getState().getAliyunOssState();

    /**
     * Uploads by reflection
     * {@link info.dong4j.idea.plugin.handler.PasteImageHandler #upload}
     *
     * @param inputStream the input stream
     * @param fileName    the file name
     * @return the string
     */
    @Override
    public String upload(InputStream inputStream, String fileName) {
        return uploadFromPaste(inputStream, fileName);
    }

    /**
     * Upload from state string.
     *
     * @param inputStream the input stream
     * @param fileName    the file name
     * @return the string
     */
    private String uploadFromPaste(InputStream inputStream, String fileName) {
        bucketName = aliyunOssState.getBucketName();
        accessKey = aliyunOssState.getAccessKey();
        accessSecretKey = DES.decrypt(aliyunOssState.getAccessSecretKey(), ImageManagerState.ALIYUN);
        endpoint = aliyunOssState.getEndpoint();
        filedir = aliyunOssState.getFiledir();

        return upload(inputStream,
                      fileName,
                      bucketName,
                      accessKey,
                      accessSecretKey,
                      endpoint,
                      filedir,
                      UploadWayEnum.FROM_PASTE);

    }

    /**
     * 直接从面板组件上获取最新配置, 不使用 state
     * {@link info.dong4j.idea.plugin.settings.ProjectSettingsPage #upload}
     *
     * @param inputStream the input stream
     * @param fileName    the file name
     * @param jPanel      the j panel
     * @return the string
     */
    public String uploadFromTest(InputStream inputStream, String fileName, JPanel jPanel) {
        Map<String, String> map = getTestFieldText(jPanel);

        bucketName = map.get("bucketName");
        accessKey = map.get("accessKey");
        accessSecretKey = map.get("secretKey");
        endpoint = map.get("endpoint");
        filedir = map.get("filedir");

        return upload(inputStream,
                      fileName,
                      bucketName,
                      accessKey,
                      accessSecretKey,
                      endpoint,
                      filedir,
                      UploadWayEnum.FROM_TEST);
    }

    /**
     * Upload string.
     *
     * @param inputStream     the input stream
     * @param fileName        the file name
     * @param bucketName      the bucketName name
     * @param accessKey       the access key
     * @param accessSecretKey the access secret key
     * @param endpoint        the endpoint
     * @param filedir     the temp file dir
     * @param uploadWayEnum   the upload way enum
     * @return the string
     */
    private String upload(InputStream inputStream,
                          String fileName,
                          String bucketName,
                          String accessKey,
                          String accessSecretKey,
                          String endpoint,
                          String filedir,
                          @NotNull UploadWayEnum uploadWayEnum) {

        filedir = StringUtils.isBlank(filedir) ? "" : filedir + "/";
        String url;
        AliyunOssClient aliyunOssClient = AliyunOssClient.getInstance();
        if (uploadWayEnum.equals(UploadWayEnum.FROM_TEST)) {
            OSS ossClient = new OSSClientBuilder().build(endpoint, accessKey, accessSecretKey);

            aliyunOssClient.setBucketName(bucketName);
            url = aliyunOssClient.upload(ossClient, inputStream, filedir, fileName);

            if (StringUtils.isNotBlank(url)) {
                int hashcode = bucketName.hashCode() +
                               accessKey.hashCode() +
                               accessSecretKey.hashCode() +
                               endpoint.hashCode();
                OssState.saveStatus(aliyunOssState, hashcode, ImageManagerState.OLD_HASH_KEY);
                aliyunOssClient.setOssClient(ossClient);
            }
        } else {
            url = aliyunOssClient.upload(inputStream, filedir, fileName);
        }
        return url;
    }
}
