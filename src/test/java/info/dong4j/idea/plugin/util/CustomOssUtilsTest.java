package info.dong4j.idea.plugin.util;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * CustomOssUtils 工具类测试
 * <p>
 * 测试 CustomOssUtils 类中的文件上传功能，包括参数处理、文件写入、HTTP 连接等。
 *
 * @author dong4j
 * @version 1.0.0
 * @since 2025-10-28
 */
public class CustomOssUtilsTest {

    private MockedStatic<OssUtils> ossUtilsMock;
    private HttpURLConnection mockConnection;

    @BeforeEach
    public void setUp() {
        // Mock OssUtils.connect 方法
        ossUtilsMock = mockStatic(OssUtils.class);
        mockConnection = mock(HttpURLConnection.class);
        
        try {
            when(OssUtils.connect(anyString(), anyString())).thenReturn(mockConnection);
        } catch (IOException e) {
            // 不应该发生
        }
    }

    @AfterEach
    public void tearDown() {
        // 关闭静态 mock
        if (ossUtilsMock != null) {
            ossUtilsMock.close();
        }
    }

    @Test
    public void testPutObjectWithValidParameters() throws Exception {
        // 设置 mock 行为
        when(mockConnection.getResponseCode()).thenReturn(200);
        when(mockConnection.getResponseMessage()).thenReturn("OK");
        when(mockConnection.getInputStream()).thenReturn(new ByteArrayInputStream("{}".getBytes()));
        when(mockConnection.getRequestMethod()).thenReturn("POST");
        when(mockConnection.getURL()).thenReturn(new URL("https://example.com/upload"));
        when(mockConnection.getContentType()).thenReturn("multipart/form-data");

        // 准备测试数据
        String api = "https://example.com/upload";
        String requestKey = "file";
        String httpMethod = "POST";
        String fileName = "test.jpg";
        InputStream inputStream = new ByteArrayInputStream("test image data".getBytes());
        Map<String, String> requestText = new HashMap<>();
        requestText.put("param1", "value1");
        Map<String, String> header = new HashMap<>();
        header.put("Authorization", "Bearer token");

        // 执行测试
        Map<String, String> result = CustomOssUtils.putObject(api, requestKey, httpMethod, fileName, inputStream, requestText, header);

        // 验证结果
        assertNotNull(result);
        assertTrue(result.containsKey("headerInfo"));
        assertTrue(result.containsKey("params"));
        assertTrue(result.containsKey("filePart"));
        assertTrue(result.containsKey("response"));
        assertTrue(result.containsKey("json"));

        // 验证 mock 调用
        verify(mockConnection).setRequestMethod("POST");
        verify(mockConnection).setDoOutput(true);
        verify(mockConnection).setDoInput(true);
        verify(mockConnection).setUseCaches(false);
        verify(mockConnection).connect();
        verify(mockConnection).disconnect();
    }

    @Test
    public void testPutObjectWithNullRequestText() throws Exception {
        // 设置 mock 行为
        when(mockConnection.getResponseCode()).thenReturn(200);
        when(mockConnection.getResponseMessage()).thenReturn("OK");
        when(mockConnection.getInputStream()).thenReturn(new ByteArrayInputStream("{}".getBytes()));
        when(mockConnection.getRequestMethod()).thenReturn("POST");
        when(mockConnection.getURL()).thenReturn(new URL("https://example.com/upload"));
        when(mockConnection.getContentType()).thenReturn("multipart/form-data");

        // 准备测试数据
        String api = "https://example.com/upload";
        String requestKey = "file";
        String httpMethod = "POST";
        String fileName = "test.jpg";
        InputStream inputStream = new ByteArrayInputStream("test image data".getBytes());
        Map<String, String> requestText = null; // null 参数
        Map<String, String> header = null; // null 头部

        // 执行测试
        Map<String, String> result = CustomOssUtils.putObject(api, requestKey, httpMethod, fileName, inputStream, requestText, header);

        // 验证结果
        assertNotNull(result);
        assertTrue(result.containsKey("headerInfo"));
        assertTrue(result.containsKey("params"));
        assertTrue(result.containsKey("filePart"));
        assertTrue(result.containsKey("response"));
        assertTrue(result.containsKey("json"));
    }

    @Test
    public void testPutObjectWithEmptyRequestText() throws Exception {
        // 设置 mock 行为
        when(mockConnection.getResponseCode()).thenReturn(200);
        when(mockConnection.getResponseMessage()).thenReturn("OK");
        when(mockConnection.getInputStream()).thenReturn(new ByteArrayInputStream("{}".getBytes()));
        when(mockConnection.getRequestMethod()).thenReturn("POST");
        when(mockConnection.getURL()).thenReturn(new URL("https://example.com/upload"));
        when(mockConnection.getContentType()).thenReturn("multipart/form-data");

        // 准备测试数据
        String api = "https://example.com/upload";
        String requestKey = "file";
        String httpMethod = "POST";
        String fileName = "test.jpg";
        InputStream inputStream = new ByteArrayInputStream("test image data".getBytes());
        Map<String, String> requestText = new HashMap<>(); // 空参数
        Map<String, String> header = new HashMap<>(); // 空头部

        // 执行测试
        Map<String, String> result = CustomOssUtils.putObject(api, requestKey, httpMethod, fileName, inputStream, requestText, header);

        // 验证结果
        assertNotNull(result);
        assertTrue(result.containsKey("headerInfo"));
        assertTrue(result.containsKey("params"));
        assertTrue(result.containsKey("filePart"));
        assertTrue(result.containsKey("response"));
        assertTrue(result.containsKey("json"));
    }

    @Test
    public void testPutObjectWithInvalidApi() throws Exception {
        // 设置 mock 行为，模拟连接失败
        when(OssUtils.connect(anyString(), anyString())).thenThrow(new IOException("Invalid URL"));

        // 准备测试数据
        String api = "invalid-url";
        String requestKey = "file";
        String httpMethod = "POST";
        String fileName = "test.jpg";
        InputStream inputStream = new ByteArrayInputStream("test image data".getBytes());
        Map<String, String> requestText = new HashMap<>();
        Map<String, String> header = new HashMap<>();

        // 执行测试并验证异常
        assertThrows(Exception.class, () -> {
            CustomOssUtils.putObject(api, requestKey, httpMethod, fileName, inputStream, requestText, header);
        });
    }

    @Test
    public void testPutObjectWithNullInputStream() throws Exception {
        // 设置 mock 行为
        when(mockConnection.getResponseCode()).thenReturn(200);
        when(mockConnection.getResponseMessage()).thenReturn("OK");
        when(mockConnection.getInputStream()).thenReturn(new ByteArrayInputStream("{}".getBytes()));
        when(mockConnection.getRequestMethod()).thenReturn("POST");
        when(mockConnection.getURL()).thenReturn(new URL("https://example.com/upload"));
        when(mockConnection.getContentType()).thenReturn("multipart/form-data");

        // 准备测试数据
        String api = "https://example.com/upload";
        String requestKey = "file";
        String httpMethod = "POST";
        String fileName = "test.jpg";
        InputStream inputStream = null; // null 输入流
        Map<String, String> requestText = new HashMap<>();
        Map<String, String> header = new HashMap<>();

        // 执行测试并验证异常
        assertThrows(Exception.class, () -> {
            CustomOssUtils.putObject(api, requestKey, httpMethod, fileName, inputStream, requestText, header);
        });
    }

    @Test
    public void testPutObjectWithEmptyFileName() throws Exception {
        // 设置 mock 行为
        when(mockConnection.getResponseCode()).thenReturn(200);
        when(mockConnection.getResponseMessage()).thenReturn("OK");
        when(mockConnection.getInputStream()).thenReturn(new ByteArrayInputStream("{}".getBytes()));
        when(mockConnection.getRequestMethod()).thenReturn("POST");
        when(mockConnection.getURL()).thenReturn(new URL("https://example.com/upload"));
        when(mockConnection.getContentType()).thenReturn("multipart/form-data");

        // 准备测试数据
        String api = "https://example.com/upload";
        String requestKey = "file";
        String httpMethod = "POST";
        String fileName = ""; // 空文件名
        InputStream inputStream = new ByteArrayInputStream("test image data".getBytes());
        Map<String, String> requestText = new HashMap<>();
        Map<String, String> header = new HashMap<>();

        // 执行测试
        Map<String, String> result = CustomOssUtils.putObject(api, requestKey, httpMethod, fileName, inputStream, requestText, header);

        // 验证结果
        assertNotNull(result);
    }

    @Test
    public void testPutObjectWithServerError() throws Exception {
        // 设置 mock 行为，模拟服务器错误
        when(mockConnection.getResponseCode()).thenReturn(500);
        when(mockConnection.getResponseMessage()).thenReturn("Internal Server Error");
        when(mockConnection.getErrorStream()).thenReturn(new ByteArrayInputStream("Server Error".getBytes()));
        when(mockConnection.getRequestMethod()).thenReturn("POST");
        when(mockConnection.getURL()).thenReturn(new URL("https://example.com/upload"));
        when(mockConnection.getContentType()).thenReturn("multipart/form-data");

        // 准备测试数据
        String api = "https://example.com/upload";
        String requestKey = "file";
        String httpMethod = "POST";
        String fileName = "test.jpg";
        InputStream inputStream = new ByteArrayInputStream("test image data".getBytes());
        Map<String, String> requestText = new HashMap<>();
        Map<String, String> header = new HashMap<>();

        // 执行测试
        Map<String, String> result = CustomOssUtils.putObject(api, requestKey, httpMethod, fileName, inputStream, requestText, header);

        // 验证结果
        assertNotNull(result);
        assertTrue(result.containsKey("response"));
        assertTrue(result.get("response").contains("500"));
        assertTrue(result.containsKey("json"));
    }

    @Test
    public void testWriteFileWithValidParameters() throws Exception {
        // 使用反射调用私有方法
        java.lang.reflect.Method writeFileMethod = CustomOssUtils.class.getDeclaredMethod(
            "writeFile", String.class, String.class, InputStream.class, java.io.OutputStream.class);
        writeFileMethod.setAccessible(true);

        // 准备测试数据
        String requestKey = "file";
        String fileName = "test.jpg";
        InputStream inputStream = new ByteArrayInputStream("test image data".getBytes());
        java.io.OutputStream outputStream = new java.io.ByteArrayOutputStream();

        // 执行测试
        String result = (String) writeFileMethod.invoke(null, requestKey, fileName, inputStream, outputStream);

        // 验证结果
        assertNotNull(result);
        assertTrue(result.contains("请求上传文件部分"));
    }

    @Test
    public void testWriteParamsWithValidParameters() throws Exception {
        // 使用反射调用私有方法
        java.lang.reflect.Method writeParamsMethod = CustomOssUtils.class.getDeclaredMethod(
            "writeParams", Map.class, java.io.OutputStream.class);
        writeParamsMethod.setAccessible(true);

        // 准备测试数据
        Map<String, String> requestText = new HashMap<>();
        requestText.put("param1", "value1");
        requestText.put("param2", "value2");
        java.io.OutputStream outputStream = new java.io.ByteArrayOutputStream();

        // 执行测试
        String result = (String) writeParamsMethod.invoke(null, requestText, outputStream);

        // 验证结果
        assertNotNull(result);
        assertTrue(result.contains("请求参数部分"));
        assertTrue(result.contains("param1"));
        assertTrue(result.contains("param2"));
    }

    @Test
    public void testWriteParamsWithNullParameters() throws Exception {
        // 使用反射调用私有方法
        java.lang.reflect.Method writeParamsMethod = CustomOssUtils.class.getDeclaredMethod(
            "writeParams", Map.class, java.io.OutputStream.class);
        writeParamsMethod.setAccessible(true);

        // 执行测试
        String result = (String) writeParamsMethod.invoke(null, null, new java.io.ByteArrayOutputStream());

        // 验证结果
        assertNotNull(result);
        assertTrue(result.contains("请求参数部分"));
        assertTrue(result.contains("空"));
    }

    @Test
    public void testWriteParamsWithEmptyParameters() throws Exception {
        // 使用反射调用私有方法
        java.lang.reflect.Method writeParamsMethod = CustomOssUtils.class.getDeclaredMethod(
            "writeParams", Map.class, java.io.OutputStream.class);
        writeParamsMethod.setAccessible(true);

        // 准备测试数据
        Map<String, String> requestText = new HashMap<>();
        java.io.OutputStream outputStream = new java.io.ByteArrayOutputStream();

        // 执行测试
        String result = (String) writeParamsMethod.invoke(null, requestText, outputStream);

        // 验证结果
        assertNotNull(result);
        assertTrue(result.contains("请求参数部分"));
        assertTrue(result.contains("空"));
    }

    @Test
    public void testBoundaryGeneration() throws Exception {
        // 测试边界标识符的生成
        java.lang.reflect.Field boundaryField = CustomOssUtils.class.getDeclaredField("BOUNDARY");
        boundaryField.setAccessible(true);
        String boundary = (String) boundaryField.get(null);

        // 验证边界标识符不为空且不包含连字符
        assertNotNull(boundary);
        assertTrue(boundary.length() > 0);
        assertTrue(boundary.matches("[a-z0-9]+")); // 只包含小写字母和数字
    }

    @Test
    public void testConnectionDisconnectCalled() throws Exception {
        // 设置 mock 行为
        when(mockConnection.getResponseCode()).thenReturn(200);
        when(mockConnection.getResponseMessage()).thenReturn("OK");
        when(mockConnection.getInputStream()).thenReturn(new ByteArrayInputStream("{}".getBytes()));
        when(mockConnection.getRequestMethod()).thenReturn("POST");
        when(mockConnection.getURL()).thenReturn(new URL("https://example.com/upload"));
        when(mockConnection.getContentType()).thenReturn("multipart/form-data");

        // 准备测试数据
        String api = "https://example.com/upload";
        String requestKey = "file";
        String httpMethod = "POST";
        String fileName = "test.jpg";
        InputStream inputStream = new ByteArrayInputStream("test image data".getBytes());
        Map<String, String> requestText = new HashMap<>();
        Map<String, String> header = new HashMap<>();

        // 执行测试
        CustomOssUtils.putObject(api, requestKey, httpMethod, fileName, inputStream, requestText, header);

        // 验证 disconnect 方法被调用
        verify(mockConnection, times(1)).disconnect();
    }
}