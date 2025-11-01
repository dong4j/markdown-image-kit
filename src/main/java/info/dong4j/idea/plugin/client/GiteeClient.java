package info.dong4j.idea.plugin.client;

import com.intellij.credentialStore.CredentialAttributes;

import info.dong4j.idea.plugin.enums.CloudEnum;
import info.dong4j.idea.plugin.settings.MikPersistenComponent;
import info.dong4j.idea.plugin.settings.MikState;
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
 * Gitee 客户端实现类
 * <p>
 * 用于与 Gitee 平台进行交互，提供文件上传、获取客户端实例、构建图片 URL 等功能。该类通过静态内部类实现单例模式，确保全局唯一实例。支持从持久化配置中初始化客户端配置，并实现 OssClient 接口定义的方法。
 * </p>
 *
 * @author dong4j
 * @version 1.0.0
 * @date 2020.04.22
 * @since 1.4.0
 */
@Slf4j
@Client(CloudEnum.GITEE)
public class GiteeClient extends AbstractOpenClient {

    static {
        init();
    }

    /**
     * 初始化 GiteeOss 相关配置信息
     * <p>
     * 该方法用于在首次使用时初始化 ossClient，若持久化配置未设置或为空，则初始化失败。
     * 从持久化组件中获取 GiteeOss 的状态信息，包括仓库地址、分支、访问令牌以及文件目录。
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
     * 获取 GiteeClient 实例
     * <p>
     * 该方法用于获取 GiteeClient 的单例实例，若实例不存在则创建并缓存。
     *
     * @return GiteeClient 实例
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
     * 获取客户端实例
     * <p>
     * 返回当前配置的客户端对象，该方法用于获取与当前上下文关联的客户端实例
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
     * 该方法用于从传入的 MikState 对象中获取 Gitee 对应的状态信息
     *
     * @param state MikState 对象，包含所有配置状态信息
     * @return Gitee 对应的状态对象
     * @since 2.0.0
     */
    @Override
    protected AbstractOpenOssState getState(MikState state) {
        return state.getGiteeOssState();
    }

    /**
     * 将对象以指定的键存储到远程仓库
     * <p>
     * 通过给定的键和输入流，将数据上传至远程仓库
     *
     * @param key      存储数据的键
     * @param instream 存储数据的输入流
     * @throws Exception 如果存储过程中发生异常
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
     * 单例模式处理类
     * <p>
     * 用于确保 GiteeClient 实例在整个应用中只有一个，通过静态内部类实现延迟加载和线程安全。
     * 使用缓存的 map 映射获取已初始化的 client，避免创建多个实例。
     *
     * @author dong4j
     * @version 1.0.0
     * @date 2025.10.24
     * @since 1.4.0
     */
    private static class SingletonHandler {
        /** 单例模式实例，用于全局访问 GiteeClient 对象 */
        private static final GiteeClient SINGLETON = new GiteeClient();
    }

    /**
     * 实现接口，获取当前客户端类型
     * <p>
     * 返回当前客户端所对应的云平台类型枚举值
     *
     * @return 云平台类型枚举值
     * @since 1.4.0
     */
    @Override
    public CloudEnum getCloudType() {
        return CloudEnum.GITEE;
    }

    /**
     * 构建图片的URL地址
     * <p>
     * 根据提供的key参数拼接并返回图片的完整访问URL
     *
     * @param key 图片的路径或标识符
     * @return 图片的完整URL字符串
     * @since 1.4.0
     */
    @Override
    @NotNull
    public String buildImageUrl(String key) {
        // https://gitee.com/{owner}/{repos}/raw/{branch}{path};
        return "https://gitee.com/" + repos + "/raw/" + branch + key;
    }

    @Override
    protected CredentialAttributes credentialAttributes() {
        return GiteeSetting.CREDENTIAL_ATTRIBUTES;
    }

}
