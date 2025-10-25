package info.dong4j.idea.plugin.client;

import com.intellij.testFramework.LightPlatformTestCase;

import java.util.ServiceLoader;

import lombok.extern.slf4j.Slf4j;

/**
 * <p>Description: ${description}</p>
 *
 * @author dong4j
 * @version 1.0.0
 * @email "mailto:dong4j@gmail.com"
 * @date 2019.03.22 15:16
 * @since 1.1.0
 */
@Slf4j
public class AbstractOssClientTest extends LightPlatformTestCase {

    /**
     * Test
     *
     * @since 1.1.0
     */
    public void test() {
        ServiceLoader<OssClient> loader = ServiceLoader.load(OssClient.class);
        for (OssClient ossClient : loader) {
            log.info("{}", ossClient);
        }
    }

}