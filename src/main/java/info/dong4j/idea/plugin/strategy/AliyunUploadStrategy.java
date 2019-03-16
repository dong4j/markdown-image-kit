package info.dong4j.idea.plugin.strategy;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;

import info.dong4j.idea.plugin.settings.OssPersistenConfig;
import info.dong4j.idea.plugin.settings.OssState;
import info.dong4j.idea.plugin.util.AliyunUploadUtils;

import org.apache.commons.lang.StringUtils;

import java.io.*;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * <p>Company: 科大讯飞股份有限公司-四川分公司</p>
 * <p>Description: </p>
 *
 * @author dong4j
 * @email sjdong3@iflytek.com
 * @since 2019-03-17 02:11
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

    private OssState.AliyunOssState aliyunOssState = OssPersistenConfig.getInstance().getState().getAliyunOssState();

    @Override
    public String upload(InputStream inputStream, String fileName) {
        // todo-dong4j : (2019年03月17日 03:34) [调用工具类实现上传(工具类做成单例的)]
        return uploadTest(inputStream, fileName);
    }

    private String uploadTest(InputStream inputStream, String fileName) {
        OssState.AliyunOssState aliyunOssState = OssPersistenConfig.getInstance().getState().getAliyunOssState();
        String tempBucketName = aliyunOssState.getBucketName();
        String tempAccessKey = aliyunOssState.getAccessKey();
        String tempAccessSecretKey = aliyunOssState.getAccessSecretKey();
        String tempEndpoint = aliyunOssState.getEndpoint();
        String tempFileDir = aliyunOssState.getFiledir();
        tempFileDir = StringUtils.isBlank(tempFileDir) ? "" : tempFileDir + "/";
        OSS oss = null;
        try {
            OSSClientBuilder ossClientBuilder = new OSSClientBuilder();
            // 返回读取指定资源的输入流
            oss = ossClientBuilder.build(tempEndpoint, tempAccessKey, tempAccessSecretKey);
            oss.putObject(tempBucketName,
                          tempFileDir + fileName,
                          inputStream);
            return AliyunUploadUtils.getUrl(tempFileDir, fileName);
        } catch (Exception e) {
            log.trace("", e);
        } finally {
            if (oss != null) {
                oss.shutdown();
            }
        }
        return "";
    }
}
