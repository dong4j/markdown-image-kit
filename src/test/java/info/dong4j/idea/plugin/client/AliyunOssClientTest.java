package info.dong4j.idea.plugin.client;

import com.intellij.testFramework.LightPlatformTestCase;

import java.io.FileNotFoundException;
import java.util.Objects;

import lombok.extern.slf4j.Slf4j;

/**
 * 阿里云OSS客户端测试类
 * <p>
 * 用于测试阿里云OSS客户端的基本功能，包括文件上传操作。该类继承自LightPlatformTestCase，主要用于单元测试场景。
 *
 * @author dong4j
 * @version 1.0.0
 * @date 2019.03.18
 * @since 1.1.0
 */
@Slf4j
public class AliyunOssClientTest extends LightPlatformTestCase {
    /**
     * 测试阿里云OSS上传功能
     * <p>
     * 该方法用于测试通过阿里云OSS客户端上传文件的功能，读取指定路径的图片文件并上传至OSS，输出生成的文件URL。
     *
     * @throws FileNotFoundException 如果文件未找到，抛出异常
     * @since 1.1.0
     */
    public void test() throws Exception {
        AliyunOssClient aliyunOssClient = AliyunOssClient.getInstance();
        String url = aliyunOssClient.upload(Objects.requireNonNull(this.getClass().getResourceAsStream("/mik.webp")), "mik.webp");
        log.debug(url);
    }
}
