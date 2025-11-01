package info.dong4j.idea.plugin.client;

import com.intellij.credentialStore.CredentialAttributes;

import info.dong4j.idea.plugin.MikBundle;
import info.dong4j.idea.plugin.enums.CloudEnum;
import info.dong4j.idea.plugin.settings.MikPersistenComponent;
import info.dong4j.idea.plugin.settings.MikState;
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
 * GitHub 云存储客户端实现类
 * <p>
 * 该类用于实现 GitHub 云存储服务的客户端功能，支持初始化配置、获取单例实例、上传文件、构建图片 URL 等操作。通过静态内部类实现单例模式，确保全局唯一实例。同时支持对分支名称的处理和校验，如将 "master" 转换为 "main"。
 * </p>
 *
 * @author dong4j
 * @version 1.0.0
 * @date 2020.04.22
 * @since 1.3.0
 */
@Slf4j
@Client(CloudEnum.GITHUB)
public class GithubClient extends AbstractOpenClient {

    static {
        init();
    }

    /**
     * 初始化 GitHub Oss 相关配置
     * <p>
     * 检查是否为首次使用，若 ossClient 为 null，则使用持久化配置进行初始化。
     * 包括获取仓库名、分支名、访问令牌以及文件目录等信息。
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
     * 获取GithubClient实例
     * <p>
     * 该方法用于获取GithubClient的单例实例，若实例不存在则创建并缓存
     *
     * @return GithubClient实例
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
     * 获取客户端实例
     * <p>
     * 返回当前配置的客户端实例，用于与外部服务进行通信
     *
     * @return 客户端实例
     * @since 1.3.0
     */
    @Override
    protected AbstractOpenClient getClient() {
        return getInstance();
    }

    /**
     * 从 MikState 中获取对应的状态
     * <p>
     * 该方法用于从传入的 MikState 对象中获取 GitHub 对应的状态信息
     *
     * @param state MikState 对象，包含所有配置状态信息
     * @return GitHub 对应的状态对象
     * @since 2.0.0
     */
    @Override
    protected AbstractOpenOssState getState(MikState state) {
        return state.getGithubOssState();
    }

    /**
     * 单例处理器类
     * <p>
     * 使用静态内部类实现单例模式，确保 GithubClient 实例的唯一性，避免重复创建多个实例。
     * 该类主要用于管理 GithubClient 的单例访问，提供线程安全的初始化方式。
     *
     * @author dong4j
     * @version 0.0.1
     * @date 2020.04.22
     * @email mailto:dong4j@gmail.com
     * @since 1.3.0
     */
    private static class SingletonHandler {
        /** 单例模式实例，用于全局访问 GithubClient 对象 */
        private static final GithubClient SINGLETON = new GithubClient();
    }

    /**
     * 实现接口，获取当前客户端类型
     * <p>
     * 返回当前客户端所对应的云类型枚举值
     *
     * @return 云类型枚举值
     * @since 1.3.0
     */
    @Override
    public CloudEnum getCloudType() {
        return CloudEnum.GITHUB;
    }

    /**
     * 处理分支名称，将 "master" 转换为 "main"，其他分支名称保持不变
     * <p>
     * 如果传入的分支名称不为空且等于 "master"，则返回 "main"；否则返回原始分支名称
     *
     * @param branch 分支名称
     * @return 处理后的分支名称
     * @since 1.4.0
     */
    @Override
    protected String processBranch(String branch) {
        return StringUtils.isNotBlank(branch) && branch.equals("master") ? "main" : branch;
    }

    /**
     * 将对象以指定的键存储到远程仓库
     * <p>
     * 通过给定的键和输入流，将数据上传至远程仓库
     *
     * @param key      存储数据的键
     * @param instream 存储数据的输入流
     * @throws Exception 上传过程中发生异常时抛出
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
     * 构建图片的URL地址
     * <p>
     * 根据提供的key参数拼接并返回图片的完整URL路径。
     *
     * @param key 图片的标识符或路径参数
     * @return 构建完成的图片URL字符串
     * @since 1.4.0
     */
    @Override
    @NotNull
    public String buildImageUrl(String key) {
        return "https://raw.githubusercontent.com/" + repos + "/" + branch + key;
    }

    /**
     * 检查分支名称是否为 "master"
     * <p>
     * 该方法用于验证传入的分支名称是否为 "master"，若为 "master" 则抛出异常提示。
     *
     * @param branch 分支名称
     * @since 1.4.0
     */
    @Override
    protected void check(String branch) {
        Asserts.check(!branch.equals("master"), MikBundle.message("error.branch.name"));
    }

    @Override
    protected CredentialAttributes credentialAttributes() {
        return GithubSetting.CREDENTIAL_ATTRIBUTES;
    }
}
