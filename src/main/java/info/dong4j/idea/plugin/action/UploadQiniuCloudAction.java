package info.dong4j.idea.plugin.action;

import info.dong4j.idea.plugin.client.OssClient;
import info.dong4j.idea.plugin.client.QiniuOssClient;
import info.dong4j.idea.plugin.icon.KitIcons;
import info.dong4j.idea.plugin.settings.ImageManagerPersistenComponent;
import info.dong4j.idea.plugin.settings.OssState;

import org.jetbrains.annotations.Contract;

import javax.swing.Icon;

/**
 * <p>Company: 科大讯飞股份有限公司-四川分公司</p>
 * <p>Description: 上传到七牛云 OSS 事件</p>
 *
 * @author dong4j
 * @email sjdong3@iflytek.com
 * @since 2019-03-14 17:09
 */
public final class UploadQiniuCloudAction extends AbstractUploadCloudAction {

    @Contract(pure = true)
    @Override
    protected Icon getIcon() {
        return KitIcons.QINIU_OSS;
    }

    @Contract(pure = true)
    @Override
    boolean isAvailable() {
        return OssState.getStatus(ImageManagerPersistenComponent.getInstance().getState().getQiniuOssState());
    }

    @Contract(pure = true)
    @Override
    OssClient getOssClient() {
        return QiniuOssClient.getInstance();
    }
}
