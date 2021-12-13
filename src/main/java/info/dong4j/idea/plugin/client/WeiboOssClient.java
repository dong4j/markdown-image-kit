/*
 * MIT License
 *
 * Copyright (c) 2021 dong4j <dong4j@gmail.com>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package info.dong4j.idea.plugin.client;

import com.intellij.openapi.util.io.FileUtil;

import info.dong4j.idea.plugin.enums.CloudEnum;
import info.dong4j.idea.plugin.settings.MikPersistenComponent;
import info.dong4j.idea.plugin.settings.MikState;
import info.dong4j.idea.plugin.settings.OssState;
import info.dong4j.idea.plugin.settings.oss.WeiboOssSetting;
import info.dong4j.idea.plugin.settings.oss.WeiboOssState;
import info.dong4j.idea.plugin.util.ImageUtils;
import info.dong4j.idea.plugin.util.PasswordManager;
import info.dong4j.idea.plugin.util.StringUtils;
import info.dong4j.idea.plugin.weibo.CookieContext;
import info.dong4j.idea.plugin.weibo.UploadRequestBuilder;
import info.dong4j.idea.plugin.weibo.UploadResponse;
import info.dong4j.idea.plugin.weibo.WbpUploadRequest;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import javax.swing.JPanel;

import lombok.extern.slf4j.Slf4j;

/**
 * <p>Company: no company</p>
 * <p>Description: </p>
 *
 * @author dong4j
 * @version 0.0.1
 * @email "mailto:dong4j@gmail.com"
 * @date 2021.02.14 18:40
 * @since 0.0.1
 */
@Slf4j
@Client(CloudEnum.WEIBO_CLOUD)
public class WeiboOssClient implements OssClient {

    /** ossClient */
    private static WbpUploadRequest ossClient = null;

    /** Weibo oss state */
    private final WeiboOssState weiboOssState = MikPersistenComponent.getInstance().getState().getWeiboOssState();

    static {
        init();
    }

    /**
     * 如果是第一次使用, ossClient == null
     *
     * @since 0.0.1
     */
    private static void init() {
        WeiboOssState weiboOssState = MikPersistenComponent.getInstance().getState().getWeiboOssState();
        String username = weiboOssState.getUsername();
        String password = PasswordManager.getPassword(WeiboOssSetting.CREDENTIAL_ATTRIBUTES);

        try {
            ossClient = new UploadRequestBuilder()
                .setAcount(username, password)
                .build();
        } catch (Exception ignored) {
        }
    }

    /**
     * Gets cloud type *
     *
     * @return the cloud type
     * @since 0.0.1
     */
    @Override
    public CloudEnum getCloudType() {
        return CloudEnum.WEIBO_CLOUD;
    }

    /**
     * Set oss client.
     *
     * @param oss the oss
     * @since 0.0.1
     */
    private void setOssClient(WbpUploadRequest oss) {
        ossClient = oss;
    }

    /**
     * Gets instance.
     *
     * @return the instance
     * @since 0.0.1
     */
    @Contract(pure = true)
    public static WeiboOssClient getInstance() {
        WeiboOssClient client = (WeiboOssClient) OssClient.INSTANCES.get(CloudEnum.WEIBO_CLOUD);
        if (client == null) {
            client = WeiboOssClient.SingletonHandler.SINGLETON;
            OssClient.INSTANCES.put(CloudEnum.WEIBO_CLOUD, client);
        }
        return client;
    }

    /**
     * <p>Company: 成都返空汇网络技术有限公司 </p>
     * <p>Description: </p>
     *
     * @author dong4j
     * @version 0.0.1
     * @email "mailto:dong4j@gmail.com"
     * @date 2021.02.14 18:40
     * @since 0.0.1
     */
    private static class SingletonHandler {
        /** SINGLETON */
        private static final WeiboOssClient SINGLETON = new WeiboOssClient();
    }

    /**
     * 被 paste 操作调用 (反射).
     *
     * @param inputStream the input stream
     * @param fileName    the file name
     * @return the string
     * @throws IOException the io exception
     * @since 0.0.1
     */
    @Override
    public String upload(InputStream inputStream, String fileName) throws Exception {
        return this.upload(ossClient, inputStream, fileName);
    }

    /**
     * Upload string.
     *
     * @param ossClient   the oss client
     * @param inputStream the input stream
     * @param fileName    the file name
     * @return the string
     * @throws IOException the io exception
     * @since 0.0.1
     */
    public String upload(WbpUploadRequest ossClient, InputStream inputStream, String fileName) throws Exception {
        File file = ImageUtils.buildTempFile(fileName);
        FileUtil.copy(inputStream, new FileOutputStream(file));
        return this.upload(ossClient, file);
    }

    /**
     * final upload method, process fileName
     *
     * @param ossClient the oss client
     * @param file      the file
     * @return the string
     * @throws IOException the io exception
     * @since 0.0.1
     */
    public String upload(@NotNull WbpUploadRequest ossClient, File file) throws Exception {
        String url = "";
        // 微博上传处理不了 fileName, 因为会自动随机处理
        UploadResponse response = ossClient.upload(file);
        if (response.getResult().equals(UploadResponse.ResultStatus.SUCCESS)) {
            url = response.getImageInfo().getLarge();
        }
        return url;
    }

    /**
     * "Upload Test" 按钮被点击后调用 (反射)
     * {@link info.dong4j.idea.plugin.settings.ProjectSettingsPage#testAndHelpListener()}
     *
     * @param inputStream the input stream
     * @param fileName    the file name
     * @param jPanel      the j panel
     * @return the string
     * @since 0.0.1
     */
    @Override
    public String upload(InputStream inputStream, String fileName, JPanel jPanel) throws Exception {
        Map<String, String> map = this.getTestFieldText(jPanel);
        String username = map.get("username");
        String password = map.get("password");

        return this.upload(inputStream, fileName, username, password);
    }

    /**
     * test 按钮点击事件后请求, 成功后保留 client, paste 或者 右键 上传时使用
     *
     * @param inputStream the input stream
     * @param fileName    the file name
     * @param username    the username
     * @param password    the password
     * @return the string
     * @since 0.0.1
     */
    @NotNull
    @Contract(pure = true)
    private String upload(InputStream inputStream,
                          String fileName,
                          String username,
                          String password) throws Exception {

        WeiboOssClient weiboOssClient = WeiboOssClient.getInstance();
        CookieContext.getInstance().deleteCookie();
        WbpUploadRequest ossClient = new UploadRequestBuilder()
            .setAcount(username, password)
            .build();
        String url = weiboOssClient.upload(ossClient, inputStream, fileName);
        if (StringUtils.isNotBlank(url)) {
            int hashcode = username.hashCode() + password.hashCode();
            OssState.saveStatus(this.weiboOssState, hashcode, MikState.OLD_HASH_KEY);
            weiboOssClient.setOssClient(ossClient);
        }
        return url;
    }

}
