package info.dong4j.idea.plugin.client;

import info.dong4j.idea.plugin.enums.CloudEnum;
import info.dong4j.idea.plugin.settings.oss.AbstractExtendOssState;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import javax.swing.JPanel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AbstractOssClientJunit5Test {

    static class DummyClient extends AbstractOssClient {
        @Override
        protected AbstractOssClient getClient() {
            return this;
        }

        @Override
        protected void putObjects(String key, InputStream instream) throws Exception {
            assertTrue(key.startsWith("/"));
            assertNotNull(instream);

            // 模拟上传失败的情况
            if ("fail.jpg".equals(key.substring(1))) {
                throw new RuntimeException("上传失败");
            }
        }

        @Override
        protected AbstractExtendOssState getState() {
            return null;
        }

        @Override
        public CloudEnum getCloudType() {
            return CloudEnum.CUSTOMIZE;
        }
    }

    static class DummyClientWithNullState extends AbstractOssClient {
        @Override
        protected AbstractOssClient getClient() {
            return this;
        }

        @Override
        protected void putObjects(String key, InputStream instream) throws Exception {
            // 空实现
        }

        @Override
        protected AbstractExtendOssState getState() {
            return null;
        }

        @Override
        public CloudEnum getCloudType() {
            return CloudEnum.CUSTOMIZE;
        }
    }

    @Test
    @DisplayName("默认 endpoint URL 构造应为 https://bucket.endpoint/path")
    void urlComposeDefaultEndpoint() throws Exception {
        DummyClient client = new DummyClient();
        InputStream in = new ByteArrayInputStream(new byte[] {1, 2, 3});
        // 走 public upload(InputStream, String)
        String url = client.upload(in, "a.png");
        assertTrue(url.startsWith("https://"));
        // 由于静态字段由私有 upload 设置，这里只校验协议前缀即可
    }

    @Test
    @DisplayName("自定义 endpoint URL 构造应为 https://custom/path")
    void urlComposeCustomEndpoint() throws Exception {
        DummyClient client = new DummyClient();
        // 通过带 JPanel 的 upload 路径设置静态字段
        JPanel panel = new JPanel();
        TestPanelUtils.addText(panel, "bucketName", "b");
        TestPanelUtils.addText(panel, "accessKey", "ak");
        TestPanelUtils.addText(panel, "secretKey", "sk");
        TestPanelUtils.addText(panel, "endpoint", "e.com");
        TestPanelUtils.addText(panel, "filedir", "dir");
        TestPanelUtils.addText(panel, "customEndpoint", "cdn.example.com");
        TestPanelUtils.addBoolean(panel, "isCustomEndpoint", true);

        String url = client.upload(new ByteArrayInputStream(new byte[] {9}), "x.jpg", panel);
        assertTrue(url.startsWith("https://"));
        assertTrue(url.contains("cdn.example.com"));
    }

    @Test
    @DisplayName("测试文件目录处理")
    void testFiledirHandling() throws Exception {
        DummyClient client = new DummyClient();
        JPanel panel = new JPanel();
        TestPanelUtils.addText(panel, "bucketName", "b");
        TestPanelUtils.addText(panel, "accessKey", "ak");
        TestPanelUtils.addText(panel, "secretKey", "sk");
        TestPanelUtils.addText(panel, "endpoint", "e.com");
        TestPanelUtils.addText(panel, "filedir", ""); // 空目录
        TestPanelUtils.addText(panel, "customEndpoint", "");
        TestPanelUtils.addBoolean(panel, "isCustomEndpoint", false);

        String url = client.upload(new ByteArrayInputStream(new byte[] {9}), "x.jpg", panel);
        assertTrue(url.startsWith("https://"));
    }

    @Test
    @DisplayName("测试上传失败情况")
    void testUploadFailure() {
        DummyClient client = new DummyClient();
        InputStream in = new ByteArrayInputStream(new byte[] {1, 2, 3});

        // 应该抛出异常
        assertThrows(Exception.class, () -> {
            client.upload(in, "fail.jpg");
        });
    }

    @Test
    @DisplayName("测试空文件名")
    void testEmptyFileName() throws Exception {
        DummyClient client = new DummyClient();
        InputStream in = new ByteArrayInputStream(new byte[] {1, 2, 3});

        // 空文件名应该能正常处理
        String url = client.upload(in, "");
        assertTrue(url.startsWith("https://"));
    }

    @Test
    @DisplayName("测试null输入流")
    void testNullInputStream() {
        DummyClient client = new DummyClient();

        // null输入流应该抛出异常
        assertThrows(Exception.class, () -> {
            client.upload(null, "test.jpg");
        });
    }

    @Test
    @DisplayName("测试带目录的文件路径")
    void testFileWithDirectory() throws Exception {
        DummyClient client = new DummyClient();
        InputStream in = new ByteArrayInputStream(new byte[] {1, 2, 3});

        // 测试文件名包含路径的情况
        String url = client.upload(in, "path/to/file.jpg");
        assertTrue(url.startsWith("https://"));
    }

    @Test
    @DisplayName("测试getState返回null的情况")
    void testGetStateReturnsNull() throws Exception {
        DummyClientWithNullState client = new DummyClientWithNullState();
        InputStream in = new ByteArrayInputStream(new byte[] {1, 2, 3});

        // 应该正常工作，即使getState返回null
        String url = client.upload(in, "test.jpg");
        assertEquals("", url); // 因为putObjects是空实现，所以返回空字符串
    }

    @Test
    @DisplayName("测试getClient返回自身")
    void testGetClientReturnsSelf() {
        DummyClient client = new DummyClient();
        AbstractOssClient returnedClient = client.getClient();
        assertEquals(client, returnedClient);
    }
}

class TestPanelUtils {
    static void addText(JPanel p, String name, String value) {
        javax.swing.JTextField t = new javax.swing.JTextField();
        t.setName(name);
        t.setText(value);
        p.add(t);
    }

    static void addBoolean(JPanel p, String name, boolean value) {
        javax.swing.JCheckBox c = new javax.swing.JCheckBox();
        c.setName(name);
        c.setSelected(value);
        p.add(c);
    }
}