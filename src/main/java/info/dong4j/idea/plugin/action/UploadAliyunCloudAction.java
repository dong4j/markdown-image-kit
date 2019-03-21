package info.dong4j.idea.plugin.action;

import info.dong4j.idea.plugin.icon.KitIcons;
import info.dong4j.idea.plugin.settings.ImageManagerPersistenComponent;
import info.dong4j.idea.plugin.settings.OssState;
import info.dong4j.idea.plugin.singleton.AliyunOssClient;
import info.dong4j.idea.plugin.singleton.OssClient;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import javax.swing.Icon;

import lombok.extern.slf4j.Slf4j;

/**
 * <p>Company: 科大讯飞股份有限公司-四川分公司</p>
 * <p>Description: 上传到阿里 OSS 事件</p>
 *
 * @author dong4j
 * @email sjdong3 @iflytek.com
 * @since 2019 -03-12 17:20
 */
@Slf4j
public final class UploadAliyunCloudAction extends AbstractUploadCloudAction {

    @NotNull
    @Contract(pure = true)
    @Override
    protected Icon getIcon() {
        return KitIcons.ALIYUN_OSS;
    }

    @Contract(pure = true)
    @Override
    boolean isAvailable() {
        return OssState.getStatus(ImageManagerPersistenComponent.getInstance().getState().getAliyunOssState());
    }

    @Contract(pure = true)
    @Override
    OssClient getOssClient() {
        return AliyunOssClient.getInstance();
    }
}
