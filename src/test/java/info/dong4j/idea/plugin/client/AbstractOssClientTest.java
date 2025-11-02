package info.dong4j.idea.plugin.client;

import com.intellij.testFramework.LightPlatformTestCase;

import java.util.ServiceLoader;

import lombok.extern.slf4j.Slf4j;

/**
 * 抽象的OSS客户端测试类
 * <p>
 * 提供OSS客户端测试的基础框架，用于加载并测试不同的OSS客户端实现。
 * 通过ServiceLoader机制加载所有实现OssClient接口的客户端，并进行日志输出。
 * 该类可作为其他OSS客户端测试类的父类，实现统一的测试逻辑。
 *
 * @author dong4j
 * @version 1.0.0
 * @date 2019.03.22
 * @since 1.1.0
 */
@Slf4j
public class AbstractOssClientTest extends LightPlatformTestCase {
    /**
     * 测试OssClient服务加载器功能
     * <p>
     * 通过ServiceLoader加载所有实现OssClient接口的实现类，并遍历输出每个实例
     *
     * @since 1.1.0
     */
    public void test() {
        ServiceLoader<OssClient> loader = ServiceLoader.load(OssClient.class);
        for (OssClient ossClient : loader) {
            log.trace("{}", ossClient);
        }
    }

}