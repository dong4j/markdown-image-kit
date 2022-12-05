package info.dong4j.idea.plugin.action.markdown;

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
 * <p>Description: 上传到百度 OSS 事件</p>
 *
 * @author dong4j
 * @version 0.0.1
 * @email "mailto:dong4j@gmail.com"
 * @date 2021.02.14 18:40
 * @since 0.0.1
 */
@Slf4j
public final class UploadBaiduCloudAction extends UploadActionBase {

    /**
     * Gets icon *
     *
     * @return the icon
     * @since 0.0.1
     */
    @NotNull
    @Contract(pure = true)
    @Override
    protected Icon getIcon() {
        return MikIcons.BAIDU;
    }

    /**
     * Gets name *
     *
     * @return the name
     * @since 0.0.1
     */
    @Nullable
    @Contract(pure = true)
    @Override
    String getName() {
        return CloudEnum.BAIDU_CLOUD.title;
    }

    /**
     * Gets client *
     *
     * @return the client
     * @since 0.0.1
     */
    @Contract(pure = true)
    @Override
    OssClient getClient() {
        return BaiduBosClient.getInstance();
    }
}
