package info.dong4j.idea.plugin.action.markdown;

import info.dong4j.idea.plugin.client.GiteeClient;
import info.dong4j.idea.plugin.client.OssClient;
import info.dong4j.idea.plugin.enums.CloudEnum;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.Icon;

import icons.MikIcons;
import lombok.extern.slf4j.Slf4j;

/**
 * 上传到 Gitee 云的事件类
 * <p>
 * 该类用于处理与 Gitee 云相关的上传操作，继承自 UploadActionBase 类，提供 Gitee 云的图标、名称和客户端获取方法。
 * 主要用于在集成开发环境中触发和管理 Gitee 云的文件上传事件。
 *
 * @author dong4j
 * @version 1.0.0
 * @date 2021.02.14
 * @since 1.4.0
 */
@Slf4j
public final class UploadGiteeCloudAction extends UploadActionBase {
    /**
     * 获取图标
     * <p>
     * 返回预定义的 GITEE 图标
     *
     * @return 图标对象
     * @since 1.4.0
     */
    @NotNull
    @Contract(pure = true)
    @Override
    protected Icon getIcon() {
        return MikIcons.GITEE;
    }

    /**
     * 获取名称
     * <p>
     * 返回预定义的名称常量，用于表示 GITEE 的标题名称。
     *
     * @return 名称常量
     * @since 1.4.0
     */
    @Nullable
    @Contract(pure = true)
    @Override
    String getName() {
        return CloudEnum.GITEE.title;
    }

    /**
     * 获取OSS客户端实例
     * <p>
     * 返回一个OSS客户端的单例实例，用于与OSS服务进行交互
     *
     * @return OssClient 实例
     * @since 1.4.0
     */
    @Contract(pure = true)
    @Override
    OssClient getClient() {
        return GiteeClient.getInstance();
    }
}
