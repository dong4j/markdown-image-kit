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
 * 上传到京东云事件类
 * <p>
 * 该类用于表示上传文件到京东云的事件，继承自 UploadActionBase 类，提供与京东云相关的上传操作定义。
 * 主要用于在系统中标识和处理针对京东云存储服务的上传请求。
 *
 * @author dong4j
 * @version 0.0.1
 * @date 2021.02.14
 * @since 0.0.1
 */
@Slf4j
public final class UploadJingdongCloudAction extends UploadActionBase {
    /**
     * 获取图标
     * <p>
     * 返回京东图标实例
     *
     * @return 京东图标
     * @since 0.0.1
     */
    @NotNull
    @Contract(pure = true)
    @Override
    protected Icon getIcon() {
        return MikIcons.JINGDONG;
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
     * 返回预定义的名称常量，用于表示京东云。
     *
     * @return 名称常量
     * @since 0.0.1
     */
    @Nullable
    @Contract(pure = true)
    @Override
    String getName() {
        return CloudEnum.JINGDONG_CLOUD.title;
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
