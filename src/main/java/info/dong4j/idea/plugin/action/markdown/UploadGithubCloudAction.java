package info.dong4j.idea.plugin.action.markdown;

import info.dong4j.idea.plugin.client.GithubClient;
import info.dong4j.idea.plugin.client.OssClient;
import info.dong4j.idea.plugin.enums.CloudEnum;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.Icon;

import icons.MikIcons;
import lombok.extern.slf4j.Slf4j;

/**
 * 上传到 GitHub 云服务的操作类
 * <p>
 * 该类用于实现将文件上传到 GitHub 的具体操作逻辑，继承自通用的上传操作基类 UploadActionBase。
 * 提供了获取图标、名称和客户端等方法，用于统一处理云服务上传相关的配置和行为。
 *
 * @author dong4j
 * @version 1.0.0
 * @date 2021.02.14
 * @since 1.3.0
 */
@Slf4j
public final class UploadGithubCloudAction extends UploadActionBase {
    /**
     * 获取图标
     * <p>
     * 返回预定义的 GitHub 图标实例
     *
     * @return 图标对象
     * @since 1.3.0
     */
    @NotNull
    @Contract(pure = true)
    @Override
    protected Icon getIcon() {
        return MikIcons.GITHUB;
    }

    /**
     * 获取名称
     * <p>
     * 返回预定义的名称常量，当前为 GitHub 的标题名称。
     *
     * @return 名称字符串
     * @since 1.3.0
     */
    @Nullable
    @Contract(pure = true)
    @Override
    String getName() {
        return CloudEnum.GITHUB.title;
    }

    /**
     * 获取OSS客户端实例
     * <p>
     * 返回GithubClient的单例实例，用于与OSS服务进行交互
     *
     * @return OssClient 实例
     * @since 1.3.0
     */
    @Contract(pure = true)
    @Override
    OssClient getClient() {
        return GithubClient.getInstance();
    }
}
