package info.dong4j.idea.plugin.action.markdown;

import info.dong4j.idea.plugin.client.CustomOssClient;
import info.dong4j.idea.plugin.client.OssClient;
import info.dong4j.idea.plugin.enums.CloudEnum;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

import javax.swing.Icon;

import icons.MikIcons;

/**
 * <p>Description: 上传到自定义 OSS 事件</p>
 *
 * @author dong4j
 * @version 1.0.0
 * @email "mailto:dong4j@gmail.com"
 * @date 2021.02.14 18:40
 * @since 1.5.0
 */
public final class UploadCustomCloudAction extends UploadActionBase {

    /**
     * Gets icon *
     *
     * @return the icon
     * @since 1.5.0
     */
    @Contract(pure = true)
    @Override
    protected Icon getIcon() {
        return MikIcons.CUSTOM;
    }

    /**
     * Gets name *
     *
     * @return the name
     * @since 1.5.0
     */
    @Nullable
    @Contract(pure = true)
    @Override
    String getName() {
        return CloudEnum.CUSTOMIZE.title;
    }

    /**
     * Gets client *
     *
     * @return the client
     * @since 1.5.0
     */
    @Contract(pure = true)
    @Override
    OssClient getClient() {
        return CustomOssClient.getInstance();
    }
}
