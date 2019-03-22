package info.dong4j.idea.plugin.client;

import com.intellij.testFramework.LightPlatformTestCase;

import java.util.ServiceLoader;

import lombok.extern.slf4j.Slf4j;

/**
 * <p>Company: 科大讯飞股份有限公司-四川分公司</p>
 * <p>Description: ${description}</p>
 *
 * @author dong4j
 * @date 2019-03-22 15:16
 * @email sjdong3@iflytek.com
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