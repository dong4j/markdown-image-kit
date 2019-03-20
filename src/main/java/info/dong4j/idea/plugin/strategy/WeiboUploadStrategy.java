package info.dong4j.idea.plugin.strategy;

import info.dong4j.idea.plugin.settings.ImageManagerPersistenComponent;
import info.dong4j.idea.plugin.settings.ImageManagerState;
import info.dong4j.idea.plugin.settings.OssState;
import info.dong4j.idea.plugin.settings.WeiboOssState;
import info.dong4j.idea.plugin.singleton.WeiboOssClient;
import info.dong4j.idea.plugin.util.DES;
import info.dong4j.idea.plugin.weibo.CookieContext;
import info.dong4j.idea.plugin.weibo.UploadRequestBuilder;
import info.dong4j.idea.plugin.weibo.WbpUploadRequest;

import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.Contract;
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

    private WeiboOssState weiboOssState = ImageManagerPersistenComponent.getInstance().getState().getWeiboOssState();

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
    @NotNull
    private String uploadFromPaste(InputStream inputStream, String fileName) {
        String username = weiboOssState.getUserName();
        String password = DES.decrypt(weiboOssState.getPassword(), ImageManagerState.WEIBOKEY);

        return upload(inputStream, fileName, username, password, UploadWayEnum.FROM_PASTE);
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
        Map<String, String> map = getTestFieldText(jPanel);
        String username = map.get("username");
        String password = map.get("password");

        return upload(inputStream, fileName, username, password, UploadWayEnum.FROM_TEST);
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
                         String password,
                         UploadWayEnum uploadWayEnum) {

        String url;
        WeiboOssClient weiboOssClient = WeiboOssClient.getInstance();
        if (uploadWayEnum.equals(UploadWayEnum.FROM_TEST)) {
            CookieContext.getInstance().deleteCookie();
            WbpUploadRequest ossClient = new UploadRequestBuilder()
                .setAcount(username, password)
                .build();
            url = weiboOssClient.upload(ossClient, inputStream, fileName);

            if (StringUtils.isNotBlank(url)) {
                int hashcode = username.hashCode() + password.hashCode();
                OssState.saveStatus(weiboOssState, hashcode, ImageManagerState.OLD_HASH_KEY);
                weiboOssClient.setOssClient(ossClient);
            }
        } else {
            url = weiboOssClient.upload(inputStream, fileName);
        }
        return url;
    }
}
