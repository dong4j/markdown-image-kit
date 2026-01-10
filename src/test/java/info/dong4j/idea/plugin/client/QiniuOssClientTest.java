package info.dong4j.idea.plugin.client;

import com.intellij.testFramework.LightPlatformTestCase;

import java.io.File;
import java.io.FileInputStream;

import lombok.extern.slf4j.Slf4j;

/**
 * 七牛云对象存储客户端测试类
 * <p>
 * 用于测试七牛云对象存储客户端的基本功能，包括文件上传等操作
 * </p>
 *
 * @author dong4j
 * @version 1.0.0
 * @date 2019.03.19
 * @since 1.1.0
 */
@Slf4j
public class QiniuOssClientTest extends LightPlatformTestCase {
    /**
     * 测试文件上传功能
     * <p>
     * 创建七牛云OSS客户端实例，上传指定路径的文件，并记录上传后的URL
     *
     * @throws Exception 上传过程中可能抛出的异常
     * @since 1.1.0
     */
    public void test1() throws Exception {
        QiniuOssClient qiniuOssClient = QiniuOssClient.getInstance();
        String url = qiniuOssClient.upload(new FileInputStream(new File("/Users/dong4j/Downloads/我可要开始皮了.png")), "我可要开始皮了.png");
        log.debug(url);
    }
}
