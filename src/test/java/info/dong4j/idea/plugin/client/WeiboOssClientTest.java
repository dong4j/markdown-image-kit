package info.dong4j.idea.plugin.client;

import com.intellij.testFramework.LightPlatformTestCase;
import com.intellij.testFramework.RunsInActiveStoreMode;

import java.io.*;

import lombok.extern.slf4j.Slf4j;

/**
 * <p>Company: 科大讯飞股份有限公司-四川分公司</p>
 * <p>Description: ${description}</p>
 *
 * @author dong4j
 * @date 2019-03-19 23:41
 * @email sjdong3@iflytek.com
 */
@Slf4j
@RunsInActiveStoreMode
public class WeiboOssClientTest extends LightPlatformTestCase {
    public void test1() throws FileNotFoundException {
        WeiboOssClient weiboOssClient = WeiboOssClient.getInstance();
        String url = weiboOssClient.upload(new FileInputStream(new File("/Users/dong4j/Downloads/我可要开始皮了.png")), "我可要开始皮了.png");
        log.info(url);
    }

}