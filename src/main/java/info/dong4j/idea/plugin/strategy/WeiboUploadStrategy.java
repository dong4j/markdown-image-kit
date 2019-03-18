package info.dong4j.idea.plugin.strategy;

import info.dong4j.idea.plugin.settings.OssPersistenConfig;
import info.dong4j.idea.plugin.settings.OssState;
import info.dong4j.idea.plugin.util.DES;
import info.dong4j.idea.plugin.weibo.UploadRequestBuilder;
import info.dong4j.idea.plugin.weibo.UploadResponse;
import info.dong4j.idea.plugin.weibo.WbpUploadRequest;
import info.dong4j.idea.plugin.weibo.exception.Wbp4jException;

import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.Contract;
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
 * <p>Description: </p>
 *
 * @author dong4j
 * @email sjdong3 @iflytek.com
 * @since 2019 -03-17 02:12
 */
@Slf4j
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WeiboUploadStrategy implements UploadStrategy {
    private String username;
    private String password;

    private OssState.WeiboOssState weiboOssState = OssPersistenConfig.getInstance().getState().getWeiboOssState();

    @Override
    public String upload(InputStream inputStream, String fileName) {
        // todo-dong4j : (2019年03月17日 03:34) [调用工具类实现上传(工具类做成单例的)]
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
        String username = weiboOssState.getUserName();
        String password = DES.decrypt(weiboOssState.getPassword(), OssState.WEIBOKEY);

        return upload(inputStream, fileName, username, password);
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
        String username = textList.get(0);
        String password = textList.get(1);

        return upload(inputStream, fileName, username, password);
    }

    /**
     * Upload string.
     *
     * @param inputStream the input stream
     * @param fileName    the file name
     * @param username    the username
     * @param password    the password
     * @return the string
     */
    @NotNull
    @Contract(pure = true)
    public String upload(InputStream inputStream,
                         String fileName,
                         String username,
                         String password) {

        WbpUploadRequest request = new UploadRequestBuilder()
            .setAcount(username, password)
            .build();
        UploadResponse response;
        File file = new File(System.getProperty("java.io.tmpdir") + "test.png");
        String url = "";
        try (BufferedInputStream bi = new BufferedInputStream(inputStream);
             FileOutputStream fos = new FileOutputStream(file)) {
            byte[] by = new byte[1024];
            int len;
            while ((len = bi.read(by)) != -1) {
                fos.write(by, 0, len);
            }
            response = request.upload(file);
            if (response.getResult().equals(UploadResponse.ResultStatus.SUCCESS)) {
                url = response.getImageInfo().getLarge();
            }
        } catch (IOException | Wbp4jException e) {
            log.trace("", e);
        }
        weiboOssState.setPassedTest(StringUtils.isNotBlank(url));
        return url;
    }
}
