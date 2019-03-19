package info.dong4j.idea.plugin.action;

import info.dong4j.idea.plugin.settings.OssPersistenConfig;
import info.dong4j.idea.plugin.settings.OssState;
import info.dong4j.idea.plugin.singleton.WeiboOssClient;

import org.jetbrains.annotations.Contract;

import java.io.*;
import java.util.Map;

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
public final class WeiboObjectStorageServiceAction extends AbstractObjectStorageServiceAction {

    private OssState.WeiboOssState weiboOssState = OssPersistenConfig.getInstance().getState().getWeiboOssState();

    @Contract(pure = true)
    @Override
    boolean isPassedTest() {
        boolean isPassedTest = weiboOssState.isPassedTest();
        Map<String, String> oldAndNewAuth = weiboOssState.getOldAndNewAuthInfo();
        return isPassedTest && oldAndNewAuth.get(OssState.OLD_HASH_KEY).equals(oldAndNewAuth.get(OssState.NEW_HASH_KEY));
    }

    @Override
    public String upload(File file) {
        WeiboOssClient weiboOssClient = WeiboOssClient.getInstance();
        return weiboOssClient.upload(file);
    }
}
