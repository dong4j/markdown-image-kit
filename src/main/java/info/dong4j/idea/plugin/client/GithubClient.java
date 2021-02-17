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

import info.dong4j.idea.plugin.enums.CloudEnum;
import info.dong4j.idea.plugin.settings.GithubOssState;
import info.dong4j.idea.plugin.settings.MikPersistenComponent;
import info.dong4j.idea.plugin.settings.MikState;
import info.dong4j.idea.plugin.settings.OssState;
import info.dong4j.idea.plugin.util.DES;
import info.dong4j.idea.plugin.util.GithubUtils;
import info.dong4j.idea.plugin.util.StringUtils;

import org.apache.http.util.Asserts;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.InputStream;
import java.util.Map;

import javax.swing.JPanel;

import lombok.extern.slf4j.Slf4j;

/**
 * <p>Description: oss client 实现步骤:
 * 1. 初始化配置: 从持久化配置中初始化 client
 * 2. 静态内部类获取 client 单例
 * 3. 实现 OssClient 接口
 * 4. 自定义 upload 逻辑</p>
 *
 * @author dong4j
 * @version 1.0.0
 * @email "mailto:dong4j@gmail.com"
 * @date 2020.04.22 01:17
 * @since 1.3.0
 */
@Slf4j
@Client(CloudEnum.GITHUB)
public class GithubClient implements OssClient {
    /** repos */
    private static String repos;
    /** branch */
    private static String branch;
    /** accesstoken */
    private static String token;
    /** filedir */
    private static String filedir;
    /** customEndpoint */
    private static boolean isCustomEndpoint;
    /** customEndpoint */
    private static String customEndpoint;

    static {
        init();
    }

    /**
     * 如果是第一次使用, ossClient == null, 使用持久化配置初始化
     * 1. 如果是第一次设置, 获取的持久化配置为 null, 则初始化 ossClient 失败
     *
     * @since 1.3.0
     */
    private static void init() {
        GithubOssState state = MikPersistenComponent.getInstance().getState().getGithubOssState();
        repos = state.getRepos();
        branch = state.getBranch();
        token = DES.decrypt(state.getToken(), MikState.GITHUB);
        String tempFileDir = state.getFiledir();
        filedir = StringUtils.isBlank(tempFileDir) ? "" : tempFileDir + "/";
    }

    /**
     * Gets instance.
     *
     * @return the instance
     * @since 1.3.0
     */
    @Contract(pure = true)
    public static GithubClient getInstance() {
        GithubClient client = (GithubClient) OssClient.INSTANCES.get(CloudEnum.GITHUB);
        if (client == null) {
            client = SingletonHandler.SINGLETON;
            OssClient.INSTANCES.put(CloudEnum.GITHUB, client);
        }
        return client;
    }

    /**
     * 使用缓存的 map 映射获取已初始化的 client, 避免创建多个实例
     *
     * @author dong4j
     * @version 0.0.1
     * @email "mailto:dong4j@gmail.com"
     * @date 2020.04.22 01:17
     * @since 1.3.0
     */
    private static class SingletonHandler {
        /** SINGLETON */
        private static final GithubClient SINGLETON = new GithubClient();
    }

    /**
     * 实现接口, 获取当前 client type
     *
     * @return the cloud typed
     * @since 1.3.0
     */
    @Override
    public CloudEnum getCloudType() {
        return CloudEnum.GITHUB;
    }

    /**
     * 通过文件流上传文件
     *
     * @param inputStream the input stream
     * @param fileName    the file name
     * @return the string
     * @since 1.3.0
     */
    @Override
    public String upload(InputStream inputStream, String fileName) throws Exception {
        return GithubUtils.putObject("/" + fileName,
                                     inputStream,
                                     repos,
                                     branch,
                                     token);
    }

    /**
     * 在设置界面点击 'Test' 按钮上传时调用, 通过 JPanel 获取当前配置
     * {@link info.dong4j.idea.plugin.settings.ProjectSettingsPage#testAndHelpListener()}
     *
     * @param inputStream the input stream
     * @param fileName    the file name
     * @param jPanel      the j panel
     * @return the string
     * @since 1.3.0
     */
    @Override
    public String upload(InputStream inputStream, String fileName, JPanel jPanel) throws Exception {
        Map<String, String> map = this.getTestFieldText(jPanel);
        String repos = map.get("repos");
        String branch = map.get("branch");
        String token = map.get("token");
        String filedir = map.get("filedir");
        String customEndpoint = map.get("customEndpoint");
        boolean isCustomEndpoint = Boolean.parseBoolean(map.get("isCustomEndpoint"));

        Asserts.notBlank(repos, "仓库名");
        Asserts.notBlank(branch, "分支名");
        Asserts.notBlank(token, "Token");

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

        GithubClient.repos = repos;
        GithubClient.filedir = filedir;
        // 主分支兼容处理
        GithubClient.branch = StringUtils.isNotBlank(branch) && branch.equals("master") ? "main" : branch;
        GithubClient.token = token;
        GithubClient.customEndpoint = customEndpoint;
        GithubClient.isCustomEndpoint = isCustomEndpoint;

        GithubClient client = GithubClient.getInstance();

        String url = client.upload(inputStream, fileName);

        if (StringUtils.isNotBlank(url)) {
            int hashcode = repos.hashCode() +
                           token.hashCode() +
                           branch.hashCode() +
                           (customEndpoint + isCustomEndpoint).hashCode();
            // 更新可用状态
            OssState.saveStatus(MikPersistenComponent.getInstance().getState().getGithubOssState(),
                                hashcode,
                                MikState.OLD_HASH_KEY);
        }
        return url;
    }
}
