package info.dong4j.idea.plugin.action.markdown;

import info.dong4j.idea.plugin.client.OssClient;
import info.dong4j.idea.plugin.client.QiniuOssClient;
import info.dong4j.idea.plugin.enums.CloudEnum;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

import javax.swing.Icon;

import icons.MikIcons;

/**
 * 上传到七牛云 OSS 事件类
 * <p>
 * 该类用于表示上传文件到七牛云对象存储服务（OSS）的事件操作，继承自通用的上传事件基类 UploadActionBase。
 * 提供了获取图标、名称和 OSS 客户端实例的方法，用于在系统中标识和执行七牛云 OSS 的上传操作。
 *
 * @author dong4j
 * @version 0.0.1
 * @date 2021.02.14
 * @since 0.0.1
 */
public final class UploadQiniuCloudAction extends UploadActionBase {
    /**
     * 获取图标
     * <p>
     * 返回预定义的 QINIU_OSS 图标常量
     *
     * @return 图标常量
     * @since 0.0.1
     */
    @Contract(pure = true)
    @Override
    protected Icon getIcon() {
        return MikIcons.QINIU_OSS;
    }

    /**
     * 获取名称
     * <p>
     * 返回预定义的名称常量，用于标识云服务提供商。
     *
     * @return 名称常量
     * @since 0.0.1
     */
    @Nullable
    @Contract(pure = true)
    @Override
    String getName() {
        return CloudEnum.QINIU_CLOUD.title;
    }

    /**
     * 获取OSS客户端实例
     * <p>
     * 返回一个OSS客户端对象，用于与对象存储服务进行交互
     *
     * @return OSS客户端实例
     * @since 0.0.1
     */
    @Contract(pure = true)
    @Override
    OssClient getClient() {
        return QiniuOssClient.getInstance();
    }
}
