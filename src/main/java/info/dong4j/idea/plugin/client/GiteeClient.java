/*
 * MIT License
 *
 * Copyright (c) 2022 dong4j <dong4j@gmail.com>
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
import info.dong4j.idea.plugin.settings.MikPersistenComponent;
import info.dong4j.idea.plugin.settings.oss.AbstractOpenOssState;
import info.dong4j.idea.plugin.settings.oss.GiteeOssState;
import info.dong4j.idea.plugin.settings.oss.GiteeSetting;
import info.dong4j.idea.plugin.util.GiteeUtils;
import info.dong4j.idea.plugin.util.PasswordManager;
import info.dong4j.idea.plugin.util.StringUtils;

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
 * @since 1.4.0
 */
@Slf4j
@Client(CloudEnum.GITEE)
public class GiteeClient extends AbstractOpenClient {

    static {
        init();
    }

    /**
     * 如果是第一次使用, ossClient == null, 使用持久化配置初始化
     * 1. 如果是第一次设置, 获取的持久化配置为 null, 则初始化 ossClient 失败
     *
     * @since 1.4.0
     */
    private static void init() {
        GiteeOssState state = MikPersistenComponent.getInstance().getState().getGiteeOssState();
        repos = state.getRepos();
        branch = state.getBranch();
        token = PasswordManager.getPassword(GiteeSetting.CREDENTIAL_ATTRIBUTES);
        String tempFileDir = state.getFiledir();
        filedir = StringUtils.isBlank(tempFileDir) ? "" : tempFileDir + "/";
    }

    /**
     * Gets instance.
     *
     * @return the instance
     * @since 1.4.0
     */
    @Contract(pure = true)
    public static GiteeClient getInstance() {
        GiteeClient client = (GiteeClient) OssClient.INSTANCES.get(CloudEnum.GITEE);
        if (client == null) {
            client = SingletonHandler.SINGLETON;
            OssClient.INSTANCES.put(CloudEnum.GITEE, client);
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
        return MikPersistenComponent.getInstance().getState().getGiteeOssState();
    }

    /**
     * Put objects
     *
     * @param key      key
     * @param instream instream
     * @since 1.3.0
     */
    @Override
    protected void putObjects(String key, InputStream instream) throws Exception {
        GiteeUtils.putObject(key,
                             instream,
                             repos,
                             branch,
                             token);
    }

    /**
     * 使用缓存的 map 映射获取已初始化的 client, 避免创建多个实例
     *
     * @author dong4j
     * @version 0.0.1
     * @email "mailto:dong4j@gmail.com"
     * @date 2020.04.22 01:17
     * @since 1.4.0
     */
    private static class SingletonHandler {
        /** SINGLETON */
        private static final GiteeClient SINGLETON = new GiteeClient();
    }

    /**
     * 实现接口, 获取当前 client type
     *
     * @return the cloud typed
     * @since 1.4.0
     */
    @Override
    public CloudEnum getCloudType() {
        return CloudEnum.GITEE;
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
        // https://gitee.com/{owner}/{repos}/raw/{branch}{path};
        return "https://gitee.com/" + repos + "/raw/" + branch + key;
    }

}
