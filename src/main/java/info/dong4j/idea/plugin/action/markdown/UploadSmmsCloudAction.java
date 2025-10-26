package info.dong4j.idea.plugin.action.markdown;

import info.dong4j.idea.plugin.client.OssClient;
import info.dong4j.idea.plugin.client.SmmsClient;
import info.dong4j.idea.plugin.enums.CloudEnum;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

import javax.swing.Icon;

import icons.MikIcons;

/**
 * 上传到SMMS云服务的动作类
 * <p>
 * 该类继承自 UploadActionBase，用于处理将文件上传到SMMS云服务的具体逻辑。
 * 提供了获取图标、名称和OSS客户端等方法，用于支持上传操作的可视化和执行。
 *
 * @author dong4j
 * @version 0.0.1
 * @date 2021.02.14
 * @since 0.0.1
 */
public final class UploadSmmsCloudAction extends UploadActionBase {
    /**
     * 获取图标
     * <p>
     * 返回预定义的图标常量 MikIcons.SM_MS
     *
     * @return 图标常量
     * @since 0.0.1
     */
    @Contract(pure = true)
    @Override
    protected Icon getIcon() {
        return MikIcons.SM_MS;
    }

    /**
     * 获取名称
     * <p>
     * 返回预定义的名称常量，用于标识云服务类型。
     *
     * @return 名称常量
     * @since 0.0.1
     */
    @Nullable
    @Contract(pure = true)
    @Override
    String getName() {
        return CloudEnum.SM_MS_CLOUD.title;
    }

    /**
     * 获取OSS客户端实例
     * <p>
     * 返回一个OSS客户端的单例实例，用于与对象存储服务进行交互
     *
     * @return OssClient 实例
     * @since 0.0.1
     */
    @Contract(pure = true)
    @Override
    OssClient getClient() {
        return SmmsClient.getInstance();
    }
}
