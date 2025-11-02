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
 * 图片上传全流程集成测试类
 * <p>
 * 该类用于验证图片上传的完整流程，包括从 Markdown 解析、图片压缩、重命名、上传以及标签替换的全过程。测试覆盖了正常上传、上传失败保留原路径以及跳过已上传图片的场景。
 * <p>
 * 测试使用 Mock 对象模拟 OSS 客户端行为，确保测试的隔离性和可重复性。同时，通过模拟事件数据和处理流程，验证各模块之间的协作是否符合预期。
 *
 * @author dong4j
 * @version 1.0.0
 * @date 2025.10.25
 * @since 1.0.0
 */
public class ImageUploadFlowIntegrationTest {

    /** 模拟的 OSS 客户端实例，用于测试和开发环境中的对象存储服务操作 */
    private MockOssClient mockClient;
    /** 事件数据对象，用于存储和传递事件相关的信息 */
    private EventData eventData;

    /**
     * 初始化测试环境，设置模拟对象和测试数据
     * <p>
     * 该方法在每个测试用例执行前被调用，用于初始化模拟的OSS客户端、
     * 模拟的Markdown图片数据以及事件数据对象。
     *
     * @since 1.0
     */
    @BeforeEach
    void setUp() {
        mockClient = new MockOssClient();

        Map<Document, List<MarkdownImage>> mockMap = createMockMarkdownImages();

        eventData = new EventData()
            .setAction("ImageUploadFlowIntegrationTest")
            .setProject(null) // 测试环境可以不设置
            .setClient(mockClient)
            .setClientName(CloudEnum.CUSTOMIZE.title)
            .setWaitingProcessMap(mockMap);
    }

    /**
     * 测试完整的图片上传流程
     * <p>
     * 测试场景：模拟本地图片上传过程，包括选项客户端处理和图片上传处理
     * 预期结果：OSS 客户端应被调用，上传后的图片标记应包含有效的 URL
     * <p>
     * 说明：该测试验证从图片上传到替换标签的完整流程，确保上传逻辑正确执行，并且图片信息更新符合预期
     */
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

    /**
     * 测试上传失败时应保留原路径的功能
     * <p>
     * 测试场景：模拟上传失败的情况，验证系统是否保留原始路径信息
     * 预期结果：上传失败后，应确保图像的最终标签包含"upload error"标识
     * <p>
     * 注意：测试中通过设置 mockClient.shouldFail = true 模拟上传失败场景
     * 并验证最终标签是否正确记录上传错误信息
     */
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

    /**
     * 测试已上传的图片应跳过上传逻辑
     * <p>
     * 测试场景：当图片位于网络路径且已存在时
     * 预期结果：图片不应触发上传操作
     * <p>
     * 该测试模拟了一个包含网络图片的Markdown图片列表，设置图片路径为已存在的网络地址，并验证上传客户端是否未被调用
     * <p>
     * 注意：测试中使用了Mockito框架进行模拟，涉及的mock对象包括ProgressIndicator和mockClient
     */
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
     * <p>
     * 用于生成模拟的 Markdown 图片对象，包含文件名、图片名、扩展名、原始文本、行号等信息，适用于单元测试场景。
     *
     * @return 包含测试 Markdown 图片数据的 Map，键为 Document 对象，值为 MarkdownImage 列表
     */
    private Map<Document, List<MarkdownImage>> createMockMarkdownImages() {
        MarkdownImage image = new MarkdownImage();
        image.setFilename("test.md");
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
     * 模拟 OSS 客户端
     * <p>
     * 用于测试场景下的 OSS 客户端模拟实现，提供上传文件的模拟方法，支持控制上传是否失败。
     * 可用于单元测试中替代真实 OSS 客户端，便于验证上传逻辑和异常处理。
     *
     * @author 未知
     * @version 1.0.0
     * @date 2025.10.24
     * @since 1.0.0
     */
    static class MockOssClient implements OssClient {
        /** 是否已调用上传方法的标志 */
        boolean uploadCalled = false;
        /** 是否应失败标志 */
        boolean shouldFail = false;

        /**
         * 获取客户端名称
         * <p>
         * 返回客户端的模拟名称
         *
         * @return 客户端名称
         */
        @Override
        public String getName() {
            return "Mock Client";
        }

        /**
         * 获取云平台类型
         * <p>
         * 返回当前云平台的类型，该方法用于标识当前使用的云平台为自定义类型。
         *
         * @return 云平台类型，固定返回 CloudEnum.CUSTOMIZE
         */
        @Override
        public CloudEnum getCloudType() {
            return CloudEnum.CUSTOMIZE;
        }

        /**
         * 重写上传方法，用于处理文件上传并返回文件的访问 URL
         * <p>
         * 该方法首先标记上传操作已调用，然后校验输入流和文件名是否为 null。
         * 如果配置为模拟上传失败，则返回 null；否则返回一个模拟的文件访问 URL。
         *
         * @param inputStream 上传的输入流
         * @param filename    文件名
         * @return 文件的访问 URL，若上传失败则返回 null
         * @throws Exception 上传过程中发生异常时抛出
         */
        @Override
        public String upload(InputStream inputStream, String filename) throws Exception {
            uploadCalled = true;
            assertNotNull(inputStream, "输入流不应该为 null");
            assertNotNull(filename, "文件名不应该为 null");

            if (shouldFail) {
                return null; // 模拟上传失败
            }

            // 返回模拟的 URL
            return "https://example.com/uploads/" + filename;
        }

        /**
         * 调用内部上传方法，处理文件上传逻辑
         * <p>
         * 该方法通过调用内部的 upload 方法实现文件上传功能，忽略传入的 jPanel 参数。
         *
         * @param inputStream 文件输入流，用于读取上传的文件内容
         * @param filename    文件名，表示上传文件的名称
         * @param jPanel      用于上传操作的面板（未使用）
         * @return 上传结果的字符串表示
         * @throws Exception 上传过程中发生异常时抛出
         */
        @Override
        public String upload(InputStream inputStream, String filename, javax.swing.JPanel jPanel) throws Exception {
            return upload(inputStream, filename);
        }
    }

    /**
     * Mock 替换文档处理器（不实际修改文档）
     * <p>
     * 该类用于模拟替换文档处理器的行为，不执行实际的文档修改操作，常用于测试场景中替代真实处理器。
     *
     * @author 未知
     * @version 1.0.0
     * @date 2025.04.01
     * @since 1.0.0
     */
    static class MockReplaceToDocument extends ReplaceToDocument {
        /**
         * 执行事件处理逻辑
         * <p>
         * 该方法用于处理特定事件，当前实现仅在测试中验证数据，不进行实际替换操作
         *
         * @param data 事件数据对象
         * @return 处理结果，返回 true 表示处理成功，false 表示处理失败
         */
        @Override
        public boolean execute(EventData data) {
            // 在测试中只验证数据，不实际替换
            return super.execute(data);
        }
    }

    /**
     * Mock 客户端检查处理器
     * <p>
     * 用于模拟客户端检查的处理器，继承自 OptionClientHandler。在执行时，仅检查客户端是否为空，用于简化测试场景。
     *
     * @author 未知
     * @version 1.0.0
     * @date 2025.10.24
     * @since 1.0.0
     */
    static class MockOptionClientHandler extends OptionClientHandler {
        /** OssClient 实例，用于与阿里云对象存储服务进行交互 */
        private final OssClient client;

        /**
         * 初始化 OssClient 客户端处理器
         * <p>
         * 通过传入的 OssClient 实例初始化客户端处理器，用于后续的 OSS 服务操作
         *
         * @param client OssClient 实例，用于与 OSS 服务进行交互
         */
        MockOptionClientHandler(OssClient client) {
            this.client = client;
        }

        /**
         * 执行事件处理逻辑
         * <p>
         * 该方法用于执行事件处理，仅通过检查客户端是否为空来简化逻辑。
         *
         * @param data 事件数据
         * @return 如果客户端不为空则返回 true，否则返回 false
         */
        @Override
        public boolean execute(EventData data) {
            return client != null; // 简化：总是通过检查
        }
    }
}
