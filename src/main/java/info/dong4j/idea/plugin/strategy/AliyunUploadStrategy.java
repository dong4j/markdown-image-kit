package info.dong4j.idea.plugin.strategy;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;

import info.dong4j.idea.plugin.settings.OssPersistenConfig;
import info.dong4j.idea.plugin.settings.OssState;
import info.dong4j.idea.plugin.util.AliyunUploadUtils;

import org.apache.commons.lang.StringUtils;

import java.awt.Component;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.JTextField;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * <p>Company: 科大讯飞股份有限公司-四川分公司</p>
 * <p>Description: </p>
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

    private OssState.AliyunOssState aliyunOssState = OssPersistenConfig.getInstance().getState().getAliyunOssState();

    @Override
    public String upload(InputStream inputStream, String fileName) {
        // todo-dong4j : (2019年03月17日 03:34) [调用工具类实现上传(工具类做成单例的)]
        return uploadTest(inputStream, fileName);
    }

    public String upload(InputStream inputStream, String fileName, JPanel jPanel){
        // 保存认证信息, 这个顺序是确定的
        List<String> textList = new ArrayList<>();
        Component[] components = jPanel.getComponents();
        for(Component c: components){
            if(c instanceof JTextField){
                JTextField textField = (JTextField)c;
                textList.add(textField.getText());
            }
        }
        String tempBucketName = textList.get(0);
        String tempAccessKey = textList.get(1);
        String tempAccessSecretKey = textList.get(2);
        String tempEndpoint = textList.get(3);
        String tempFileDir = textList.get(5);
        tempFileDir = StringUtils.isBlank(tempFileDir) ? "" : tempFileDir + "/";

        return upload(inputStream, fileName, tempBucketName, tempAccessKey, tempAccessSecretKey, tempEndpoint, tempFileDir);
    }

    /**
     * 每次新建实例来上传, 主要用于设置页的 test 按钮
     *
     * @param inputStream the input stream
     * @param fileName    the file name
     * @return the string
     */
    private String uploadTest(InputStream inputStream, String fileName) {
        OssState.AliyunOssState aliyunOssState = OssPersistenConfig.getInstance().getState().getAliyunOssState();
        String tempBucketName = aliyunOssState.getBucketName();
        String tempAccessKey = aliyunOssState.getAccessKey();
        String tempAccessSecretKey = aliyunOssState.getAccessSecretKey();
        String tempEndpoint = aliyunOssState.getEndpoint();
        String tempFileDir = aliyunOssState.getFiledir();
        tempFileDir = StringUtils.isBlank(tempFileDir) ? "" : tempFileDir + "/";

        return upload(inputStream, fileName, tempBucketName, tempAccessKey, tempAccessSecretKey, tempEndpoint, tempFileDir);
    }

    private String upload(InputStream inputStream,
                          String fileName,
                          String tempBucketName,
                          String tempAccessKey,
                          String tempAccessSecretKey,
                          String tempEndpoint,
                          String tempFileDir) {
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
