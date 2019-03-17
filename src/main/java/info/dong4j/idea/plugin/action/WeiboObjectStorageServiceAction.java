package info.dong4j.idea.plugin.action;

import info.dong4j.idea.plugin.settings.OssPersistenConfig;
import info.dong4j.idea.plugin.settings.OssState;
import info.dong4j.idea.plugin.weibo.UploadRequestBuilder;
import info.dong4j.idea.plugin.weibo.UploadResponse;
import info.dong4j.idea.plugin.weibo.WbpUploadRequest;
import info.dong4j.idea.plugin.weibo.exception.Wbp4jException;

import org.jetbrains.annotations.Contract;

import java.io.*;

/**
 * <p>Company: 科大讯飞股份有限公司-四川分公司</p>
 * <p>Description: 上传到微博 OSS 事件</p>
 *
 * @author dong4j
 * @email sjdong3@iflytek.com
 * @since 2019-03-14 16:39
 */
public final class WeiboObjectStorageServiceAction extends AbstractObjectStorageServiceAction {

    private OssState.WeiboOssState weiboOssState = OssPersistenConfig.getInstance().getState().getWeiboOssState();

    @Contract(pure = true)
    @Override
    boolean isPassedTest() {
        return OssPersistenConfig.getInstance().getState().getWeiboOssState().isPassedTest();
    }

    @Override
    public String upload(File file) {
        WbpUploadRequest request = new UploadRequestBuilder()
            .setAcount(weiboOssState.getUserName(), weiboOssState.getPassword())
            .build();
        UploadResponse response = null;
        String url = "";
        try {
            response = request.upload(file);
        } catch (IOException | Wbp4jException e) {
            e.printStackTrace();
        }
        if (response != null && response.getResult().equals(UploadResponse.ResultStatus.SUCCESS)) {
            url = response.getImageInfo().getLarge();
        }
        return url;
    }
}
