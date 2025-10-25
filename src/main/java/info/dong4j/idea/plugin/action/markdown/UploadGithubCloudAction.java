package info.dong4j.idea.plugin.action.markdown;

import info.dong4j.idea.plugin.client.GithubClient;
import info.dong4j.idea.plugin.client.OssClient;
import info.dong4j.idea.plugin.enums.CloudEnum;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.Icon;

import icons.MikIcons;
import lombok.extern.slf4j.Slf4j;

/**
 * <p>Description: 上传到 github 事件</p>
 *
 * @author dong4j
 * @version 1.0.0
 * @email "mailto:dong4j@gmail.com"
 * @date 2021.02.14 18:40
 * @since 1.3.0
 */
@Slf4j
public final class UploadGithubCloudAction extends UploadActionBase {

    /**
     * Gets icon *
     *
     * @return the icon
     * @since 1.3.0
     */
    @NotNull
    @Contract(pure = true)
    @Override
    protected Icon getIcon() {
        return MikIcons.GITHUB;
    }

    /**
     * Gets name *
     *
     * @return the name
     * @since 1.3.0
     */
    @Nullable
    @Contract(pure = true)
    @Override
    String getName() {
        return CloudEnum.GITHUB.title;
    }

    /**
     * Gets client *
     *
     * @return the client
     * @since 1.3.0
     */
    @Contract(pure = true)
    @Override
    OssClient getClient() {
        return GithubClient.getInstance();
    }
}
