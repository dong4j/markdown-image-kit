package info.dong4j.idea.plugin.action.markdown;

import info.dong4j.idea.plugin.client.OssClient;
import info.dong4j.idea.plugin.client.WeiboOssClient;
import info.dong4j.idea.plugin.enums.CloudEnum;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

import javax.swing.Icon;

import icons.MikIcons;
import lombok.extern.slf4j.Slf4j;

/**
 * 上传到微博 OSS 事件类
 * <p>
 * 该类用于表示上传文件到微博 OSS 的具体操作事件，继承自通用的上传事件基类 UploadActionBase。
 * 提供了获取图标、名称和 OSS 客户端实例的方法，用于在系统中标识和执行该上传操作。
 * <p>
 * 使用场景包括但不限于：在文件上传流程中，根据不同的存储服务类型创建对应的上传事件对象。
 *
 * @author dong4j
 * @version 0.0.1
 * @date 2021.02.14
 * @since 0.0.1
 */
@Slf4j
public final class UploadWeiboCloudAction extends UploadActionBase {
    /**
     * 获取图标
     * <p>
     * 返回预定义的微博OSS图标对象
     *
     * @return 图标对象
     * @since 0.0.1
     */
    @Contract(pure = true)
    @Override
    protected Icon getIcon() {
        return MikIcons.WEIBO_OSS;
    }

    /**
     * 获取名称
     * <p>
     * 返回预定义的名称常量，用于表示微博云的名称。
     *
     * @return 名称常量
     * @since 0.0.1
     */
    @Nullable
    @Contract(pure = true)
    @Override
    String getName() {
        return CloudEnum.WEIBO_CLOUD.title;
    }

    /**
     * 获取OSS客户端实例
     * <p>
     * 返回单例的OSS客户端对象，用于与对象存储服务进行交互
     *
     * @return OSS客户端实例
     * @since 0.0.1
     */
    @Contract(pure = true)
    @Override
    OssClient getClient() {
        return WeiboOssClient.getInstance();
    }
}
