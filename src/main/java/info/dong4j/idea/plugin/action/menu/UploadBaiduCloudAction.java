package info.dong4j.idea.plugin.action.menu;

import info.dong4j.idea.plugin.client.BaiduBosClient;
import info.dong4j.idea.plugin.client.OssClient;
import info.dong4j.idea.plugin.enums.CloudEnum;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.Icon;

import icons.MikIcons;
import lombok.extern.slf4j.Slf4j;

/**
 * 上传到百度 OSS 事件
 * <p>
 * 该类用于表示上传文件到百度云存储（OSS）的事件操作，继承自 UploadActionBase 类，提供与百度云存储相关的图标、名称和客户端获取方法。
 * <p>
 * 主要用于在系统中触发或处理上传文件到百度云存储的业务逻辑。
 *
 * @author dong4j
 * @version 0.0.1
 * @date 2021.02.14
 * @since 0.0.1
 */
@Slf4j
public final class UploadBaiduCloudAction extends UploadActionBase {
    /**
     * 获取图标
     * <p>
     * 返回预定义的百度图标实例
     *
     * @return 百度图标对象
     * @since 0.0.1
     */
    @NotNull
    @Contract(pure = true)
    @Override
    protected Icon getIcon() {
        return MikIcons.BAIDU;
    }

    /**
     * 获取名称
     * <p>
     * 返回预定义的名称常量，用于标识百度云。
     *
     * @return 名称常量
     * @since 0.0.1
     */
    @Nullable
    @Contract(pure = true)
    @Override
    String getName() {
        return CloudEnum.BAIDU_CLOUD.title;
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
        return BaiduBosClient.getInstance();
    }
}
