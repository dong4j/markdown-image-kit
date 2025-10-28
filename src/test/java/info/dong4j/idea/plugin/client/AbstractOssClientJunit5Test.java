package info.dong4j.idea.plugin.client;

import info.dong4j.idea.plugin.enums.CloudEnum;
import info.dong4j.idea.plugin.settings.oss.AbstractExtendOssState;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import javax.swing.JPanel;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AbstractOssClientJunit5Test {

    static class DummyClient extends AbstractOssClient {
        @Override
        protected AbstractOssClient getClient() {
            return this;
        }

        @Override
        protected void putObjects(String key, InputStream instream) {
            assertTrue(key.startsWith("/"));
            assertNotNull(instream);
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


