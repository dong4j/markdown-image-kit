package info.dong4j.idea.plugin.weibo;

import info.dong4j.idea.plugin.util.WeiboUploadUtils;
import info.dong4j.idea.plugin.weibo.exception.Wbp4jException;

import org.junit.Test;

import java.io.*;

/**
 * <p>Company: 科大讯飞股份有限公司-四川分公司</p>
 * <p>Description: ${description}</p>
 *
 * @author dong4j
 * @date 2019-03-17 22:42
 * @email sjdong3@iflytek.com
 */
public class WbpUploadRequestTest {

    @Test
    public void build() throws IOException, Wbp4jException {
        WbpUploadRequest request = new UploadRequestBuilder()
            .setAcount(System.getProperty("username"), System.getProperty("password"))
            .build();
        UploadResponse response = request.upload(new File("/Users/dong4j/Downloads/NewInstance.png"));
        System.out.println(response.getResult());
        System.out.println(response.getMessage());
        System.out.println(response.getImageInfo());

        System.out.println(WeiboUploadUtils.getImageUrl(response.getImageInfo().getPid(), "large", true));
    }
}