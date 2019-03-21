package info.dong4j.idea.plugin.action;

import info.dong4j.idea.plugin.icon.KitIcons;
import info.dong4j.idea.plugin.settings.ImageManagerPersistenComponent;
import info.dong4j.idea.plugin.settings.OssState;
import info.dong4j.idea.plugin.singleton.OssClient;
import info.dong4j.idea.plugin.singleton.WeiboOssClient;

import org.jetbrains.annotations.Contract;

import javax.swing.Icon;

import lombok.extern.slf4j.Slf4j;

/**
 * <p>Company: 科大讯飞股份有限公司-四川分公司</p>
 * <p>Description: 上传到微博 OSS 事件</p>
 *
 * @author dong4j
 * @email sjdong3@iflytek.com
 * @since 2019-03-14 16:39
 */
@Slf4j
public final class UploadWeiboCloudAction extends AbstractUploadCloudAction {
    @Contract(pure = true)
    @Override
    protected Icon getIcon() {
        return KitIcons.WEIBO_OSS;
    }

    @Contract(pure = true)
    @Override
    boolean isAvailable() {
        return OssState.getStatus(ImageManagerPersistenComponent.getInstance().getState().getWeiboOssState());
    }

    @Contract(pure = true)
    @Override
    OssClient getOssClient() {
        return WeiboOssClient.getInstance();
    }
}
