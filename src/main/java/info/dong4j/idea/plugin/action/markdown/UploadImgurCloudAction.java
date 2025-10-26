package info.dong4j.idea.plugin.action.markdown;

import info.dong4j.idea.plugin.client.OssClient;
import info.dong4j.idea.plugin.enums.CloudEnum;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.Icon;

import icons.MikIcons;
import lombok.extern.slf4j.Slf4j;

/**
 * 上传到 Imgur 云服务的动作类
 * <p>
 * 该类用于处理上传文件到 Imgur 云平台的相关操作，继承自 UploadActionBase 类，提供与 Imgur 云服务相关的图标、名称、可用性及客户端获取等功能。
 * <p>
 * 由于 Imgur 云服务目前不可用，该类的 available() 方法返回 false，表示该上传动作当前不可使用。
 *
 * @author dong4j
 * @version 0.0.1
 * @date 2021.02.14
 * @since 0.0.1
 */
@Slf4j
public final class UploadImgurCloudAction extends UploadActionBase {
    /**
     * 获取图标
     * <p>
     * 返回预定义的图标常量，用于表示 Imgur 图标
     *
     * @return 图标常量
     * @since 0.0.1
     */
    @NotNull
    @Contract(pure = true)
    @Override
    protected Icon getIcon() {
        return MikIcons.IMGUR;
    }

    /**
     * 判断当前对象是否可用
     * <p>
     * 该方法用于检查当前对象是否处于可用状态，返回布尔值表示可用性
     *
     * @return 如果对象可用返回 true，否则返回 false
     * @since 0.0.1
     */
    @Contract(pure = true)
    @Override
    boolean available() {
        return false;
    }

    /**
     * 获取名称
     * <p>
     * 返回预定义的名称常量，用于标识 Imgur 云服务的标题
     *
     * @return 名称常量
     * @since 0.0.1
     */
    @Nullable
    @Contract(pure = true)
    @Override
    String getName() {
        return CloudEnum.IMGUR_CLOUD.title;
    }

    /**
     * 获取OSS客户端实例
     * <p>
     * 返回配置好的OSS客户端对象，用于与阿里云对象存储服务进行交互
     *
     * @return OssClient 实例
     * @since 0.0.1
     */
    @Contract(pure = true)
    @Override
    OssClient getClient() {
        return null;
    }
}
