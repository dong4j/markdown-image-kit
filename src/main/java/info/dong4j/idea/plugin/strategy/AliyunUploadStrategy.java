package info.dong4j.idea.plugin.strategy;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;

import info.dong4j.idea.plugin.settings.AliyunOssState;
import info.dong4j.idea.plugin.settings.ImageManagerPersistenComponent;
import info.dong4j.idea.plugin.settings.ImageManagerState;
import info.dong4j.idea.plugin.singleton.AliyunOssClient;
import info.dong4j.idea.plugin.util.DES;

import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.util.List;

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
    private String fileDir;

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
        // todo-dong4j : (2019年03月17日 03:34) [调用工具类实现上传(工具类做成单例的)]
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
        String bucketName = aliyunOssState.getBucketName();
        String accessKey = aliyunOssState.getAccessKey();
        String accessSecretKey = aliyunOssState.getAccessSecretKey();
        accessSecretKey = DES.decrypt(accessSecretKey, ImageManagerState.ALIYUN);
        String endpoint = aliyunOssState.getEndpoint();
        String filedir = aliyunOssState.getFiledir();

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
        // 保存认证信息, 这个顺序是确定的
        List<String> textList = getTestFieldText(jPanel);

        String bucketName = textList.get(0);
        String accessKey = textList.get(1);
        String accessSecretKey = textList.get(5);
        String endpoint = textList.get(2);
        String filedir = textList.get(4);

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
        if (uploadWayEnum.equals(UploadWayEnum.FROM_TEST)) {
            OSS ossClient = new OSSClientBuilder().build(endpoint, accessKey, accessSecretKey);

            AliyunOssClient.bucketName = bucketName;
            url = AliyunOssClient.getInstance().upload(ossClient, inputStream, filedir, fileName);
            if (StringUtils.isNotBlank(url)) {
                int hashcode = bucketName.hashCode() +
                               accessKey.hashCode() +
                               accessSecretKey.hashCode() +
                               endpoint.hashCode();
                saveStatus(aliyunOssState, hashcode);
                // 参数验证成功后直接设置 ossClient, 不要浪费
                AliyunOssClient.getInstance().setOssClient(ossClient);
            }
        } else {
            url = AliyunOssClient.getInstance().getUrl(filedir, fileName);
        }
        return url;
    }
}
