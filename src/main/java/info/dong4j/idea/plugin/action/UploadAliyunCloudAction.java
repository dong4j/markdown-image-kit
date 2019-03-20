package info.dong4j.idea.plugin.action;

import info.dong4j.idea.plugin.icon.KitIcons;
import info.dong4j.idea.plugin.settings.ImageManagerPersistenComponent;
import info.dong4j.idea.plugin.singleton.AliyunOssClient;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.*;

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
    boolean isPassedTest() {
        return validFromState(ImageManagerPersistenComponent.getInstance().getState().getAliyunOssState());
    }

    @Override
    public String upload(File file) {
        return AliyunOssClient.getInstance().upload(file);
    }
}
