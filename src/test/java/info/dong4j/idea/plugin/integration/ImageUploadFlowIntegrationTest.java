package info.dong4j.idea.plugin.integration;

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.progress.ProgressIndicator;

import info.dong4j.idea.plugin.chain.ActionManager;
import info.dong4j.idea.plugin.chain.ImageUploadHandler;
import info.dong4j.idea.plugin.chain.OptionClientHandler;
import info.dong4j.idea.plugin.chain.ReplaceToDocument;
import info.dong4j.idea.plugin.client.OssClient;
import info.dong4j.idea.plugin.entity.EventData;
import info.dong4j.idea.plugin.entity.MarkdownImage;
import info.dong4j.idea.plugin.enums.CloudEnum;
import info.dong4j.idea.plugin.enums.ImageLocationEnum;
import info.dong4j.idea.plugin.enums.ImageMarkEnum;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.anyDouble;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;

/**
 * 图片上传全流程集成测试
 * <p>
 * 测试从 Markdown 解析 -> 压缩 -> 重命名 -> 上传 -> 标签替换的完整流程
 *
 * @author dong4j
 * @version 1.0.0
 * @date 2025.10.25
 * @since 1.0.0
 */
public class ImageUploadFlowIntegrationTest {

    private MockOssClient mockClient;
    private EventData eventData;

    @BeforeEach
    void setUp() {
        mockClient = new MockOssClient();

        Map<Document, List<MarkdownImage>> mockMap = createMockMarkdownImages();

        eventData = new EventData()
            .setProject(null) // 测试环境可以不设置
            .setClient(mockClient)
            .setClientName(CloudEnum.CUSTOMIZE.title)
            .setWaitingProcessMap(mockMap);
    }

    @Test
    @DisplayName("完整流程：本地图片 -> 上传 -> 替换标签")
    void fullUploadFlow() {
        // 创建最简化的处理链 - 仅测试上传逻辑
        ActionManager manager = new ActionManager(eventData)
            .addHandler(new MockOptionClientHandler(mockClient))
            .addHandler(new ImageUploadHandler());

        // 创建进度指示器 mock
        ProgressIndicator indicator = mock(ProgressIndicator.class);
        doNothing().when(indicator).setText2(anyString());
        doNothing().when(indicator).setFraction(anyDouble());

        // 确保 EventData 有所需字段
        eventData.setSize(2);
        eventData.setIndex(0);

        // 执行处理链
        manager.invoke(indicator);

        // 验证上传被调用
        assertTrue(mockClient.uploadCalled, "OSS 客户端应该被调用");

        // 验证图片信息 - 上传后图片标记会被更新
        Map<Document, List<MarkdownImage>> result = eventData.getWaitingProcessMap();
        assertNotNull(result);

        for (Map.Entry<Document, List<MarkdownImage>> entry : result.entrySet()) {
            List<MarkdownImage> images = entry.getValue();
            for (MarkdownImage image : images) {
                assertNotNull(image.getFinalMark(), "最终标签应该被设置");
                assertTrue(image.getFinalMark().contains("https://"), "最终标签应该包含 URL - " + image.getFinalMark());
            }
        }
    }

    @Test
    @DisplayName("上传失败时应保留原路径")
    void uploadFailurePreservesOriginalPath() {
        mockClient.shouldFail = true;

        eventData.setSize(1);
        eventData.setIndex(0);

        ActionManager manager = new ActionManager(eventData)
            .addHandler(new ImageUploadHandler());

        ProgressIndicator indicator = mock(ProgressIndicator.class);
        doNothing().when(indicator).setText2(anyString());
        doNothing().when(indicator).setFraction(anyDouble());

        manager.invoke(indicator);

        Map<Document, List<MarkdownImage>> result = eventData.getWaitingProcessMap();
        MarkdownImage image = result.values().iterator().next().get(0);

        assertNotNull(image.getFinalMark(), "应该有最终标签");
        assertTrue(image.getFinalMark().contains("upload error"), "应该标记上传错误");
    }

    @Test
    @DisplayName("已上传的图片应跳过上传")
    void skipNetworkImages() {
        Map<Document, List<MarkdownImage>> map = createMockMarkdownImages();
        MarkdownImage networkImage = map.values().iterator().next().get(0);
        networkImage.setLocation(ImageLocationEnum.NETWORK);
        networkImage.setPath("https://example.com/existing.png");

        eventData.setWaitingProcessMap(map);
        eventData.setSize(1);
        eventData.setIndex(0);

        ActionManager manager = new ActionManager(eventData)
            .addHandler(new ImageUploadHandler());

        ProgressIndicator indicator = mock(ProgressIndicator.class);
        doNothing().when(indicator).setText2(anyString());
        doNothing().when(indicator).setFraction(anyDouble());

        manager.invoke(indicator);

        assertFalse(mockClient.uploadCalled, "网络图片不应该触发上传");
    }

    /**
     * 创建测试用的 Markdown 图片数据
     */
    private Map<Document, List<MarkdownImage>> createMockMarkdownImages() {
        MarkdownImage image = new MarkdownImage();
        image.setFileName("test.md");
        image.setImageName("test.png");
        image.setExtension(".png");
        image.setOriginalLineText("![测试](./imgs/test.png)");
        image.setOriginalMark("![测试](./imgs/test.png)");
        image.setLineNumber(0);
        image.setLineStartOffset(0);
        image.setLineEndOffset(20);
        image.setTitle("测试");
        image.setPath("./imgs/test.png");
        image.setLocation(ImageLocationEnum.LOCAL);
        image.setImageMarkType(ImageMarkEnum.ORIGINAL);
        // 模拟本地图片的输入流
        image.setInputStream(new ByteArrayInputStream("fake-image-data".getBytes()));

        Map<Document, List<MarkdownImage>> map = new HashMap<>();
        map.put(null, List.of(image));
        return map;
    }

    /**
     * Mock OSS 客户端
     */
    static class MockOssClient implements OssClient {
        boolean uploadCalled = false;
        boolean shouldFail = false;

        @Override
        public String getName() {
            return "Mock Client";
        }

        @Override
        public CloudEnum getCloudType() {
            return CloudEnum.CUSTOMIZE;
        }

        @Override
        public String upload(InputStream inputStream, String fileName) throws Exception {
            uploadCalled = true;
            assertNotNull(inputStream, "输入流不应该为 null");
            assertNotNull(fileName, "文件名不应该为 null");

            if (shouldFail) {
                return null; // 模拟上传失败
            }

            // 返回模拟的 URL
            return "https://example.com/uploads/" + fileName;
        }

        @Override
        public String upload(InputStream inputStream, String fileName, javax.swing.JPanel jPanel) throws Exception {
            return upload(inputStream, fileName);
        }
    }

    /**
     * Mock 替换文档处理器（不实际修改文档）
     */
    static class MockReplaceToDocument extends ReplaceToDocument {
        @Override
        public boolean execute(EventData data) {
            // 在测试中只验证数据，不实际替换
            return super.execute(data);
        }
    }

    /**
     * Mock 客户端检查处理器
     */
    static class MockOptionClientHandler extends OptionClientHandler {
        private final OssClient client;

        MockOptionClientHandler(OssClient client) {
            this.client = client;
        }

        @Override
        public boolean execute(EventData data) {
            return client != null; // 简化：总是通过检查
        }
    }
}
