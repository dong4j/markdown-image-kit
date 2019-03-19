package info.dong4j.idea.plugin.action;

import info.dong4j.idea.plugin.settings.ImageManagerPersistenComponent;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

import java.io.*;

/**
 * <p>Company: 科大讯飞股份有限公司-四川分公司</p>
 * <p>Description: 上传到七牛云 OSS 事件</p>
 *
 * @author dong4j
 * @email sjdong3@iflytek.com
 * @since 2019-03-14 17:09
 */
public final class QiniuObjectStorageServiceAction extends AbstractObjectStorageServiceAction {

    @Contract(pure = true)
    @Override
    boolean isPassedTest() {
        return validFromState(ImageManagerPersistenComponent.getInstance().getState().getQiniuOssState());
    }

    @Nullable
    @Contract(pure = true)
    @Override
    String upload(File file) {
        return null;
    }
}
