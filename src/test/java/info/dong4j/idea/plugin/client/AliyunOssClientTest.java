package info.dong4j.idea.plugin.client;

import com.intellij.testFramework.LightPlatformTestCase;

import java.io.*;

import lombok.extern.slf4j.Slf4j;

/**
 * <p>Company: no company</p>
 * <p>Description: ${description}</p>
 *
 * @author dong4j
 * @date 2019-03-18 16:13
 * @email dong4j@gmail.com
 */
@Slf4j
// @RunsInActiveStoreMode
public class AliyunOssClientTest extends LightPlatformTestCase {
    // @Test
    public void test() throws FileNotFoundException {
        AliyunOssClient aliyunOssClient = AliyunOssClient.getInstance();
        String url = aliyunOssClient.upload(new FileInputStream(new File("/Users/dong4j/Downloads/我可要开始皮了.png")), "我可要开始皮了.png");
        log.info(url);
    }
}