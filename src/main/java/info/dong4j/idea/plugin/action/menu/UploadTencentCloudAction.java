package info.dong4j.idea.plugin.action.menu;

import info.dong4j.idea.plugin.client.OssClient;
import info.dong4j.idea.plugin.client.TencentOssClient;
import info.dong4j.idea.plugin.enums.CloudEnum;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.Icon;

import icons.MikIcons;
import lombok.extern.slf4j.Slf4j;

/**
 * 上传到腾讯云 OSS 的操作类
 * <p>
 * 该类继承自 UploadActionBase，用于封装上传文件到腾讯云对象存储服务（OSS）的具体实现。
 * 提供了获取图标、名称和 OSS 客户端实例的方法，用于在上传过程中展示相关信息和与 OSS 服务交互。
 *
 * @author dong4j
 * @version 0.0.1
 * @date 2021.02.14
 * @since 0.0.1
 */
@Slf4j
public final class UploadTencentCloudAction extends UploadActionBase {
    /**
     * 获取图标
     * <p>
     * 返回预定义的腾讯图标实例
     *
     * @return 图标对象
     * @since 0.0.1
     */
    @NotNull
    @Contract(pure = true)
    @Override
    protected Icon getIcon() {
        return MikIcons.TENCENT;
    }

    /**
     * 获取名称
     * <p>
     * 返回预定义的名称常量，用于标识腾讯云。
     *
     * @return 名称常量
     * @since 0.0.1
     */
    @Nullable
    @Contract(pure = true)
    @Override
    String getName() {
        return CloudEnum.TENCENT_CLOUD.title;
    }

    /**
     * 获取OSS客户端实例
     * <p>
     * 返回一个OSS客户端的单例实例，用于与对象存储服务进行交互
     *
     * @return OSS客户端实例
     * @since 0.0.1
     */
    @Contract(pure = true)
    @Override
    OssClient getClient() {
        return TencentOssClient.getInstance();
    }
}
