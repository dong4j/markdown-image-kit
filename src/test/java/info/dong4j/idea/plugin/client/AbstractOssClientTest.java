package info.dong4j.idea.plugin.client;

import com.intellij.testFramework.LightPlatformTestCase;

import java.util.ServiceLoader;

import lombok.extern.slf4j.Slf4j;

/**
 * <p>Company: no company</p>
 * <p>Description: ${description}</p>
 *
 * @author dong4j
 * @date 2019-03-22 15:16
 * @email dong4j@gmail.com
 */
@Slf4j
public class AbstractOssClientTest extends LightPlatformTestCase {

    public void test() {
        ServiceLoader<OssClient> loader = ServiceLoader.load(OssClient.class);
        for (OssClient ossClient : loader) {
            log.info("{}", ossClient);
        }
    }

}