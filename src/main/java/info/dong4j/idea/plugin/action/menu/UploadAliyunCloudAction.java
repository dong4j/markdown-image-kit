package info.dong4j.idea.plugin.action.menu;

import info.dong4j.idea.plugin.client.AliyunOssClient;
import info.dong4j.idea.plugin.client.OssClient;
import info.dong4j.idea.plugin.enums.CloudEnum;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.Icon;

import icons.MikIcons;
import lombok.extern.slf4j.Slf4j;

/**
 * 上传到阿里 OSS 事件
 * <p>
 * 该类用于表示上传文件到阿里云对象存储服务（OSS）的事件操作，继承自通用的上传动作基类。
 * 提供了获取图标、名称和 OSS 客户端实例的方法，用于在系统中标识和执行上传操作。
 *
 * @author dong4j
 * @version 0.0.1
 * @date 2021.02.14
 * @since 0.0.1
 */
@Slf4j
public final class UploadAliyunCloudAction extends UploadActionBase {
    /**
     * 获取图标
     * <p>
     * 返回预定义的阿里云OSS图标实例
     *
     * @return 图标对象
     * @since 0.0.1
     */
    @NotNull
    @Contract(pure = true)
    @Override
    protected Icon getIcon() {
        return MikIcons.ALIYUN_OSS;
    }

    /**
     * 获取名称
     * <p>
     * 返回预定义的云服务商名称，当前返回阿里云的名称。
     *
     * @return 名称
     * @since 0.0.1
     */
    @Nullable
    @Contract(pure = true)
    @Override
    String getName() {
        return CloudEnum.ALIYUN_CLOUD.title;
    }

    /**
     * 获取OSS客户端实例
     * <p>
     * 返回阿里云OSS客户端的单例实例，用于与OSS服务进行交互
     *
     * @return OssClient 实例
     * @since 0.0.1
     */
    @Contract(pure = true)
    @Override
    OssClient getClient() {
        return AliyunOssClient.getInstance();
    }
}
