package info.dong4j.idea.plugin.strategy;

import info.dong4j.idea.plugin.settings.OssPersistenConfig;
import info.dong4j.idea.plugin.settings.OssState;

import java.io.*;

/**
 * <p>Company: 科大讯飞股份有限公司-四川分公司</p>
 * <p>Description: </p>
 *
 * @author dong4j
 * @email sjdong3@iflytek.com
 * @since 2019-03-17 02:12
 */
public class WeiboUploadStrategy implements UploadStrategy{
    private String userName;
    private String password;

    private OssState.WeiboOssState weiboOssState = OssPersistenConfig.getInstance().getState().getWeiboOssState();

    @Override
    public String upload(InputStream inputStream, String fileName) {
        return null;
    }
}
