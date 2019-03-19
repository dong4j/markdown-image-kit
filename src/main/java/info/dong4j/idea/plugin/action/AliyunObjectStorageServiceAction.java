package info.dong4j.idea.plugin.action;

import info.dong4j.idea.plugin.settings.OssPersistenConfig;
import info.dong4j.idea.plugin.settings.OssState;
import info.dong4j.idea.plugin.singleton.AliyunOssClient;

import org.jetbrains.annotations.Contract;

import java.io.*;
import java.util.Map;

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
public final class AliyunObjectStorageServiceAction extends AbstractObjectStorageServiceAction {
    private OssState.AliyunOssState aliyunOssState = OssPersistenConfig.getInstance().getState().getAliyunOssState();

    @Contract(pure = true)
    @Override
    boolean isPassedTest() {
        boolean isPassedTest = aliyunOssState.isPassedTest();
        Map<String, String> oldAndNewAuth = aliyunOssState.getOldAndNewAuthInfo();
        return isPassedTest && oldAndNewAuth.get(OssState.OLD_HASH_KEY).equals(oldAndNewAuth.get(OssState.NEW_HASH_KEY));
    }

    @Override
    public String upload(File file) {
        AliyunOssClient client = AliyunOssClient.getInstance();
        return client.upload(new File(file.getPath()));
    }
}
