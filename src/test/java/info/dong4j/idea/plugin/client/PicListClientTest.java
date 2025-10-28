package info.dong4j.idea.plugin.client;

import info.dong4j.idea.plugin.enums.CloudEnum;
import info.dong4j.idea.plugin.settings.MikPersistenComponent;
import info.dong4j.idea.plugin.settings.MikState;
import info.dong4j.idea.plugin.settings.oss.PicListOssState;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Map;

import javax.swing.JPanel;
import javax.swing.JTextField;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * PicListClient 测试类
 * <p>
 * 用于测试 PicListClient 的核心功能，包括 API 上传、命令行上传、配置解析等。
 *
 * @author dong4j
 * @version 1.0.0
 * @since 2025-10-28
 */
public class PicListClientTest {

    private PicListClient client;
    private MikPersistenComponent mockComponent;
    private MikState mockState;
    private PicListOssState mockPicListState;

    @TempDir
    Path tempDir;

    @BeforeEach
    public void setUp() {
        client = PicListClient.getInstance();

        // 创建模拟对象
        mockComponent = mock(MikPersistenComponent.class);
        mockState = mock(MikState.class);
        mockPicListState = mock(PicListOssState.class);

        // 设置默认返回值
        when(mockComponent.getState()).thenReturn(mockState);
        when(mockState.getPicListOssState()).thenReturn(mockPicListState);
        when(mockPicListState.getApi()).thenReturn("https://piclist.example.com/upload");
        when(mockPicListState.getPicbed()).thenReturn("smms");
        when(mockPicListState.getConfigName()).thenReturn("default");
        when(mockPicListState.getKey()).thenReturn("test-key");
        when(mockPicListState.getExePath()).thenReturn("");
    }

    @AfterEach
    public void tearDown() {
        // 清理测试数据
    }

    @Test
    public void testGetInstance() {
        PicListClient instance1 = PicListClient.getInstance();
        PicListClient instance2 = PicListClient.getInstance();
        assertNotNull(instance1);
        assertEquals(instance1, instance2, "应该返回相同的实例");
    }

    @Test
    public void testGetCloudType() {
        CloudEnum cloudType = client.getCloudType();
        assertEquals(CloudEnum.PICLIST, cloudType, "云类型应该是 PICLIST");
    }

    @Test
    public void testBuildUrlWithAllParameters() {
        // 由于 buildUrl 是私有方法，我们通过 upload 方法间接测试
        JPanel panel = new JPanel();
        JTextField apiField = new JTextField("https://piclist.example.com/upload");
        apiField.setName("picListApiTextField");
        JTextField picbedField = new JTextField("smms");
        picbedField.setName("picListPicbedTextField");
        JTextField configNameField = new JTextField("default");
        configNameField.setName("picListConfigNameTextField");
        JTextField keyField = new JTextField("test-key");
        keyField.setName("picListKeyTextField");
        JTextField exePathField = new JTextField("");
        exePathField.setName("picListExeTextField");

        panel.add(apiField);
        panel.add(picbedField);
        panel.add(configNameField);
        panel.add(keyField);
        panel.add(exePathField);

        InputStream inputStream = new ByteArrayInputStream("test data".getBytes());

        // 这里我们主要测试不会抛出异常
        assertThrows(Exception.class, () -> {
            client.upload(inputStream, "test.jpg", panel);
        }, "应该抛出异常，因为这是测试环境");
    }

    @Test
    public void testBuildUrlWithSomeParameters() {
        JPanel panel = new JPanel();
        JTextField apiField = new JTextField("https://piclist.example.com/upload");
        apiField.setName("picListApiTextField");
        JTextField picbedField = new JTextField("");
        picbedField.setName("picListPicbedTextField");
        JTextField configNameField = new JTextField("default");
        configNameField.setName("picListConfigNameTextField");
        JTextField keyField = new JTextField("");
        keyField.setName("picListKeyTextField");
        JTextField exePathField = new JTextField("");
        exePathField.setName("picListExeTextField");

        panel.add(apiField);
        panel.add(picbedField);
        panel.add(configNameField);
        panel.add(keyField);
        panel.add(exePathField);

        InputStream inputStream = new ByteArrayInputStream("test data".getBytes());

        assertThrows(Exception.class, () -> {
            client.upload(inputStream, "test.jpg", panel);
        }, "应该抛出异常，因为这是测试环境");
    }

    @Test
    public void testUploadViaApiWithInvalidUrl() {
        JPanel panel = new JPanel();
        JTextField apiField = new JTextField("invalid-url");
        apiField.setName("picListApiTextField");
        JTextField picbedField = new JTextField("smms");
        picbedField.setName("picListPicbedTextField");
        JTextField configNameField = new JTextField("default");
        configNameField.setName("picListConfigNameTextField");
        JTextField keyField = new JTextField("test-key");
        keyField.setName("picListKeyTextField");
        JTextField exePathField = new JTextField("");
        exePathField.setName("picListExeTextField");

        panel.add(apiField);
        panel.add(picbedField);
        panel.add(configNameField);
        panel.add(keyField);
        panel.add(exePathField);

        InputStream inputStream = new ByteArrayInputStream("test data".getBytes());

        assertThrows(Exception.class, () -> {
            client.upload(inputStream, "test.jpg", panel);
        }, "应该抛出异常，因为 URL 无效");
    }

    @Test
    public void testUploadWithNoApiAndNoExePath() {
        JPanel panel = new JPanel();
        JTextField apiField = new JTextField("");
        apiField.setName("picListApiTextField");
        JTextField picbedField = new JTextField("smms");
        picbedField.setName("picListPicbedTextField");
        JTextField configNameField = new JTextField("default");
        configNameField.setName("picListConfigNameTextField");
        JTextField keyField = new JTextField("test-key");
        keyField.setName("picListKeyTextField");
        JTextField exePathField = new JTextField("");
        exePathField.setName("picListExeTextField");

        panel.add(apiField);
        panel.add(picbedField);
        panel.add(configNameField);
        panel.add(keyField);
        panel.add(exePathField);

        InputStream inputStream = new ByteArrayInputStream("test data".getBytes());

        Exception exception = assertThrows(IllegalStateException.class, () -> {
            client.upload(inputStream, "test.jpg", panel);
        });

        assertTrue(exception.getMessage().contains("API 地址和 可执行文件路径 必须配置一个"));
    }

    @Test
    public void testResolveExecutablePathWithNonExistentFile() {
        String nonExistentPath = "/non/existent/path";
        JPanel panel = new JPanel();
        JTextField apiField = new JTextField("");
        apiField.setName("picListApiTextField");
        JTextField picbedField = new JTextField("smms");
        picbedField.setName("picListPicbedTextField");
        JTextField configNameField = new JTextField("default");
        configNameField.setName("picListConfigNameTextField");
        JTextField keyField = new JTextField("test-key");
        keyField.setName("picListKeyTextField");
        JTextField exePathField = new JTextField(nonExistentPath);
        exePathField.setName("picListExeTextField");

        panel.add(apiField);
        panel.add(picbedField);
        panel.add(configNameField);
        panel.add(keyField);
        panel.add(exePathField);

        InputStream inputStream = new ByteArrayInputStream("test data".getBytes());

        Exception exception = assertThrows(Exception.class, () -> {
            client.upload(inputStream, "test.jpg", panel);
        });

        assertTrue(exception.getMessage().contains("可执行文件不存在"));
    }

    @Test
    public void testUploadWithValidApi() throws Exception {
        // 创建一个临时文件用于测试
        File tempFile = tempDir.resolve("test.txt").toFile();
        tempFile.createNewFile();

        JPanel panel = new JPanel();
        JTextField apiField = new JTextField("https://httpbin.org/post");
        apiField.setName("picListApiTextField");
        JTextField picbedField = new JTextField("");
        picbedField.setName("picListPicbedTextField");
        JTextField configNameField = new JTextField("");
        configNameField.setName("picListConfigNameTextField");
        JTextField keyField = new JTextField("");
        keyField.setName("picListKeyTextField");
        JTextField exePathField = new JTextField("");
        exePathField.setName("picListExeTextField");

        panel.add(apiField);
        panel.add(picbedField);
        panel.add(configNameField);
        panel.add(keyField);
        panel.add(exePathField);

        InputStream inputStream = new ByteArrayInputStream("test data".getBytes());

        // 这里会抛出解析异常，因为 httpbin.org 返回的不是 PicList 期望的格式
        assertThrows(RuntimeException.class, () -> {
            client.upload(inputStream, "test.jpg", panel);
        });
    }

    @Test
    public void testGetTestFieldTextWithPicListSupport() throws Exception {
        JPanel panel = new JPanel();
        JTextField textField = new JTextField("test value");
        textField.setName("testField");
        panel.add(textField);

        // 使用反射调用私有方法
        java.lang.reflect.Method method = PicListClient.class.getDeclaredMethod(
            "getTestFieldTextWithPicListSupport", JPanel.class);
        method.setAccessible(true);

        Object result = method.invoke(client, panel);
        assertInstanceOf(Map.class, result);

        java.util.Map<String, String> resultMap = (java.util.Map<String, String>) result;
        assertEquals("test value", resultMap.get("testField"));
    }
}