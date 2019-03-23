/*
 * MIT License
 *
 * Copyright (c) 2019 dong4j
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
 *
 */

package info.dong4j.idea.plugin.client;

import info.dong4j.idea.plugin.enums.CloudEnum;
import info.dong4j.idea.plugin.settings.ImageManagerPersistenComponent;
import info.dong4j.idea.plugin.settings.ImageManagerState;
import info.dong4j.idea.plugin.settings.OssState;
import info.dong4j.idea.plugin.settings.WeiboOssState;
import info.dong4j.idea.plugin.util.DES;
import info.dong4j.idea.plugin.weibo.CookieContext;
import info.dong4j.idea.plugin.weibo.UploadRequestBuilder;
import info.dong4j.idea.plugin.weibo.UploadResponse;
import info.dong4j.idea.plugin.weibo.WbpUploadRequest;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.util.Map;

import javax.swing.JPanel;

import lombok.extern.slf4j.Slf4j;

/**
 * <p>Company: 科大讯飞股份有限公司-四川分公司</p>
 * <p>Description: </p>
 *
 * @author dong4j
 * @email sjdong3 @iflytek.com
 * @since 2019 -03-18 09:57
 */
@Slf4j
@Client(CloudEnum.WEIBO_CLOUD)
public class WeiboOssClient implements OssClient{
    private static final Object LOCK = new Object();
    private static WbpUploadRequest ossClient = null;
    private WeiboOssState weiboOssState = ImageManagerPersistenComponent.getInstance().getState().getWeiboOssState();

    private WeiboOssClient() {
        // 反射调用时判断是否初始化
        checkClient();
    }

    private static class SingletonHandler {
        static {
            init();
        }

        private static WeiboOssClient singleton = new WeiboOssClient();
    }

    /**
     * 如果是第一次使用, ossClient == null
     */
    private static void init() {
        WeiboOssState weiboOssState = ImageManagerPersistenComponent.getInstance().getState().getWeiboOssState();
        String username = weiboOssState.getUserName();
        String password = DES.decrypt(weiboOssState.getPassword(), ImageManagerState.WEIBOKEY);

        try {
            ossClient = new UploadRequestBuilder()
                .setAcount(username, password)
                .build();
        } catch (Exception ignored) {
        }
    }

    /**
     * 在调用 ossClient 之前先检查, 如果为 null 就 init()
     */
    private static void checkClient() {
        synchronized (LOCK) {
            if (ossClient == null) {
                init();
            }
        }
    }

    /**
     * Set oss client.
     *
     * @param oss the oss
     */
    private void setOssClient(WbpUploadRequest oss) {
        ossClient = oss;
    }

    /**
     * Gets instance.
     *
     * @return the instance
     */
    @Contract(pure = true)
    public static WeiboOssClient getInstance() {
        return WeiboOssClient.SingletonHandler.singleton;
    }

    /**
     * 被 paste 操作调用 (反射).
     *
     * @param inputStream the input stream
     * @param fileName    the file name
     * @return the string
     * @throws IOException the io exception
     */
    @Override
    public String upload(InputStream inputStream, String fileName) {
        return upload(ossClient, inputStream, fileName);
    }

    /**
     * Upload string.
     *
     * @param ossClient   the oss client
     * @param inputStream the input stream
     * @param fileName    the file name
     * @return the string
     * @throws IOException the io exception
     */
    public String upload(WbpUploadRequest ossClient, InputStream inputStream, String fileName) {
        File file = new File(System.getProperty("java.io.tmpdir") + fileName);
        try {
            FileUtils.copyInputStreamToFile(inputStream, file);
            return upload(ossClient, file);
        } catch (IOException e) {
            log.trace("", e);
        }
        return "";
    }

    @Override
    public String getName() {
        return getCloudType().title;
    }

    @Override
    public CloudEnum getCloudType() {
        return CloudEnum.WEIBO_CLOUD;
    }
    /**
     * Upload string.
     *
     * @param file the file
     * @return the string
     * @throws IOException the io exception
     */
    @Override
    public String upload(File file) {
        return upload(ossClient, file);
    }

    /**
     * final upload method, process fileName
     *
     * @param ossClient the oss client
     * @param file      the file
     * @return the string
     * @throws IOException the io exception
     */
    public String upload(WbpUploadRequest ossClient, File file) {
        String url = "";
        UploadResponse response;
        try {
            // 微博上传处理不了 fileName, 因为会自动随机处理
            response = ossClient.upload(file);
            if (response.getResult().equals(UploadResponse.ResultStatus.SUCCESS)) {
                url = response.getImageInfo().getLarge();
            }
        } catch (IOException e) {
            log.trace("", e);
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
     */
    @Override
    public String upload(InputStream inputStream, String fileName, JPanel jPanel) {
        Map<String, String> map = getTestFieldText(jPanel);
        String username = map.get("username");
        String password = map.get("password");

        return upload(inputStream, fileName, username, password);
    }

    /**
     * test 按钮点击事件后请求, 成功后保留 client, paste 或者 右键 上传时使用
     *
     * @param inputStream the input stream
     * @param fileName    the file name
     * @param username    the username
     * @param password    the password
     * @return the string
     */
    @NotNull
    @Contract(pure = true)
    private String upload(InputStream inputStream,
                         String fileName,
                         String username,
                         String password) {

        WeiboOssClient weiboOssClient = WeiboOssClient.getInstance();
        CookieContext.getInstance().deleteCookie();
        WbpUploadRequest ossClient = new UploadRequestBuilder()
            .setAcount(username, password)
            .build();
        String url = weiboOssClient.upload(ossClient, inputStream, fileName);

        if (StringUtils.isNotBlank(url)) {
            int hashcode = username.hashCode() + password.hashCode();
            OssState.saveStatus(weiboOssState, hashcode, ImageManagerState.OLD_HASH_KEY);
            weiboOssClient.setOssClient(ossClient);
        }
        return url;
    }
}
