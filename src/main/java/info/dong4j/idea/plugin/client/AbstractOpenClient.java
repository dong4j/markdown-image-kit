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

import info.dong4j.idea.plugin.settings.MikState;
import info.dong4j.idea.plugin.settings.OssState;
import info.dong4j.idea.plugin.settings.oss.AbstractOpenOssSetting;
import info.dong4j.idea.plugin.settings.oss.AbstractOpenOssState;
import info.dong4j.idea.plugin.util.StringUtils;

import org.apache.http.util.Asserts;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.InputStream;
import java.util.Map;

import javax.swing.JPanel;

/**
 * <p>Company: 成都返空汇网络技术有限公司</p>
 * <p>Description:  </p>
 *
 * @author dong4j
 * @version 1.0.0
 * @email "mailto:dong4j@fkhwl.com"
 * @date 2021.02.17 17:34
 * @since 1.4.0
 */
public abstract class AbstractOpenClient implements OssClient {

    /** repos */
    protected static String repos;
    /** branch */
    protected static String branch;
    /** accesstoken */
    protected static String token;
    /** filedir */
    protected static String filedir;
    /** customEndpoint */
    protected static boolean isCustomEndpoint;
    /** customEndpoint */
    protected static String customEndpoint;

    /**
     * Upload
     *
     * @param inputStream input stream
     * @param fileName    file name
     * @return the string
     * @throws Exception exception
     * @since 1.6.0
     */
    @Override
    public String upload(InputStream inputStream, String fileName) throws Exception {
        String key = filedir + fileName;
        if (!key.startsWith("/")) {
            key = "/" + key;
        }

        this.putObjects(key, inputStream);

        if (isCustomEndpoint) {
            return "https://" + customEndpoint + key;
        }
        return this.buildImageUrl(key);
    }

    /**
     * 在设置界面点击 'Test' 按钮上传时调用, 通过 JPanel 获取当前配置
     * {@link info.dong4j.idea.plugin.settings.ProjectSettingsPage#testAndHelpListener()}
     *
     * @param inputStream the input stream
     * @param fileName    the file name
     * @param jPanel      the j panel
     * @return the string
     * @throws Exception exception
     * @since 1.3.0
     */
    @Override
    public String upload(InputStream inputStream, String fileName, JPanel jPanel) throws Exception {
        Map<String, String> map = this.getTestFieldText(jPanel);
        String repos = map.get("repos");
        repos = AbstractOpenOssSetting.REPOS_HINT.equals(repos) ? "" : repos;
        String branch = map.get("branch");
        String token = map.get("token");
        String filedir = map.get("filedir");
        String customEndpoint = map.get("customEndpoint");
        boolean isCustomEndpoint = Boolean.parseBoolean(map.get("isCustomEndpoint"));

        Asserts.notBlank(repos, "仓库名");
        Asserts.notBlank(branch, "分支名");
        Asserts.notBlank(token, "Token");


        this.check(branch);

        return this.upload(inputStream,
                           fileName,
                           repos,
                           branch,
                           token,
                           filedir,
                           isCustomEndpoint,
                           customEndpoint);
    }

    /**
     * Check
     *
     * @param branch branch
     * @since 1.4.0
     */
    protected void check(String branch) {
    }

    /**
     * test 按钮点击事件后请求, 成功后保留 client, paste 或者 右键 上传时使用
     *
     * @param inputStream      the input stream
     * @param fileName         the file name
     * @param repos            the repos name
     * @param branch           the access key
     * @param token            the secret key
     * @param filedir          filedir
     * @param isCustomEndpoint is custom endpoint
     * @param customEndpoint   custom endpoint
     * @return the string
     * @throws Exception exception
     * @since 1.3.0
     */
    @NotNull
    @Contract(pure = true)
    public String upload(InputStream inputStream,
                         String fileName,
                         String repos,
                         String branch,
                         String token,
                         String filedir,
                         boolean isCustomEndpoint,
                         String customEndpoint) throws Exception {

        filedir = StringUtils.isBlank(filedir) ? "" : filedir + "/";

        AbstractOpenClient.repos = repos;
        AbstractOpenClient.filedir = filedir;
        // 主分支兼容处理
        AbstractOpenClient.branch = this.processBranch(branch);
        AbstractOpenClient.token = token;
        AbstractOpenClient.customEndpoint = customEndpoint;
        AbstractOpenClient.isCustomEndpoint = isCustomEndpoint;

        AbstractOpenClient client = this.getClient();

        String url = client.upload(inputStream, fileName);

        if (StringUtils.isNotBlank(url)) {
            int hashcode = repos.hashCode() +
                           token.hashCode() +
                           branch.hashCode() +
                           (customEndpoint + isCustomEndpoint).hashCode();
            // 更新可用状态
            OssState.saveStatus(this.getState(),
                                hashcode,
                                MikState.OLD_HASH_KEY);
        }
        return url;
    }

    /**
     * Gets client *
     *
     * @return the client
     * @since 1.3.0
     */
    protected abstract AbstractOpenClient getClient();

    /**
     * Gets state *
     *
     * @return the state
     * @since 1.3.0
     */
    protected abstract AbstractOpenOssState getState();

    /**
     * Process branch
     *
     * @param branch branch
     * @return the string
     * @since 1.4.0
     */
    protected String processBranch(String branch) {
        return branch;
    }

    /**
     * Put objects
     *
     * @param key      key
     * @param instream instream
     * @throws Exception exception
     * @since 1.3.0
     */
    protected abstract void putObjects(String key, InputStream instream) throws Exception;

    /**
     * Build image url
     *
     * @param key key
     * @return the string
     * @since 1.4.0
     */
    @NotNull
    protected abstract String buildImageUrl(String key);

}
