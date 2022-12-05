package info.dong4j.idea.plugin.client;

import com.intellij.testFramework.LightPlatformTestCase;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import lombok.extern.slf4j.Slf4j;

/**
 * <p>Description: ${description}</p>
 *
 * @author dong4j
 * @version 1.0.0
 * @email "mailto:dong4j@gmail.com"
 * @date 2019.03.19 15:35
 * @since 1.1.0
 */
@Slf4j
public class QiniuOssClientTest extends LightPlatformTestCase {

    /**
     * Test 1
     *
     * @throws FileNotFoundException file not found exception
     * @since 1.1.0
     */
    public void test1() throws Exception {
        QiniuOssClient qiniuOssClient = QiniuOssClient.getInstance();
        String url = qiniuOssClient.upload(new FileInputStream(new File("/Users/dong4j/Downloads/我可要开始皮了.png")), "我可要开始皮了.png");
        log.info(url);
    }
}
