package info.dong4j.idea.plugin.action.markdown;

import info.dong4j.idea.plugin.client.CustomOssClient;
import info.dong4j.idea.plugin.client.OssClient;
import info.dong4j.idea.plugin.enums.CloudEnum;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

import javax.swing.Icon;

import icons.MikIcons;

/**
 * 上传到自定义 OSS 事件
 * <p>
 * 该类用于表示上传文件到自定义 OSS 服务的事件，继承自 UploadActionBase 类，提供与自定义 OSS 客户端相关的操作，包括获取图标、名称和 OSS 客户端实例。
 * <p>
 * 主要用于在系统中触发上传文件到自定义 OSS 服务的流程，支持图标展示、名称显示以及与具体 OSS 客户端的集成。
 *
 * @author dong4j
 * @version 1.0.0
 * @date 2021.02.14
 * @since 1.5.0
 */
public final class UploadCustomCloudAction extends UploadActionBase {
    /**
     * 获取图标
     * <p>
     * 返回自定义图标实例
     *
     * @return 图标对象
     * @since 1.5.0
     */
    @Contract(pure = true)
    @Override
    protected Icon getIcon() {
        return MikIcons.CUSTOM;
    }

    /**
     * 获取名称
     * <p>
     * 返回自定义名称的标题值，用于表示当前对象的名称信息。
     *
     * @return 名称的标题值
     * @since 1.5.0
     */
    @Nullable
    @Contract(pure = true)
    @Override
    String getName() {
        return CloudEnum.CUSTOMIZE.title;
    }

    /**
     * 获取OSS客户端实例
     * <p>
     * 返回一个OSS客户端的单例实例，用于与OSS服务进行交互
     *
     * @return OSS客户端实例
     * @since 1.5.0
     */
    @Contract(pure = true)
    @Override
    OssClient getClient() {
        return CustomOssClient.getInstance();
    }
}
