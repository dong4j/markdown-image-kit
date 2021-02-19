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

import info.dong4j.idea.plugin.MikBundle;
import info.dong4j.idea.plugin.enums.CloudEnum;
import info.dong4j.idea.plugin.settings.MikPersistenComponent;
import info.dong4j.idea.plugin.settings.oss.AbstractOpenOssState;
import info.dong4j.idea.plugin.settings.oss.GithubOssState;
import info.dong4j.idea.plugin.settings.oss.GithubSetting;
import info.dong4j.idea.plugin.util.GithubUtils;
import info.dong4j.idea.plugin.util.PasswordManager;
import info.dong4j.idea.plugin.util.StringUtils;

import org.apache.http.util.Asserts;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.InputStream;

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
public class GithubClient extends AbstractOpenClient {

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
        token = PasswordManager.getPassword(GithubSetting.CREDENTIAL_ATTRIBUTES);
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
     * Gets client *
     *
     * @return the client
     * @since 1.3.0
     */
    @Override
    protected AbstractOpenClient getClient() {
        return getInstance();
    }

    /**
     * Gets state *
     *
     * @return the state
     * @since 1.3.0
     */
    @Override
    protected AbstractOpenOssState getState() {
        return MikPersistenComponent.getInstance().getState().getGithubOssState();
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
     * Process branch
     *
     * @param branch branch
     * @return the string
     * @since 1.4.0
     */
    @Override
    protected String processBranch(String branch) {
        return StringUtils.isNotBlank(branch) && branch.equals("master") ? "main" : branch;
    }

    /**
     * Put objects
     *
     * @param key      key
     * @param instream instream
     * @throws Exception exception
     * @since 1.3.0
     */
    @Override
    protected void putObjects(String key, InputStream instream) throws Exception {
        GithubUtils.putObject(key,
                              instream,
                              repos,
                              branch,
                              token);
    }

    /**
     * Build image url
     *
     * @param key key
     * @return the string
     * @since 1.4.0
     */
    @Override
    @NotNull
    public String buildImageUrl(String key) {
        return "https://raw.githubusercontent.com/" + repos + "/" + branch + key;
    }

    /**
     * Check
     *
     * @param branch
     * @since 1.4.0
     */
    @Override
    protected void check(String branch) {
        Asserts.check(!branch.equals("master"), MikBundle.message("error.branch.name"));
    }

}
