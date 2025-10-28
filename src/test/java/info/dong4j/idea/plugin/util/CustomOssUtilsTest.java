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

import static org.junit.jupiter.api.Assertions.assertFalse;
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
 * 该类用于测试 CustomOssUtils 工具类中与对象上传相关的功能，包括文件上传、参数写入、HTTP 连接处理等核心逻辑。测试覆盖了正常上传、空参数、空输入流、无效 API 地址、服务器错误等场景，确保上传功能的健壮性和异常处理能力。
 *
 * @author dong4j
 * @version 1.0.0
 * @date 2025.10.28
 * @since 1.0.0
 */
public class CustomOssUtilsTest {

    /** 用于模拟 OssUtils 类的静态方法调用 */
    private MockedStatic<OssUtils> ossUtilsMock;
    /** 模拟的 HTTP 连接对象，用于测试或替代真实网络连接 */
    private HttpURLConnection mockConnection;

    /**
     * 初始化测试环境，模拟 OssUtils.connect 方法的行为
     * <p>
     * 该方法用于在测试前设置模拟对象，确保在测试过程中 OssUtils.connect
     * 返回预设的 HttpURLConnection 实例，便于进行单元测试。
     *
     * @since 1.0
     */
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

    /**
     * 每次测试结束后执行的清理方法
     * <p>
     * 用于关闭静态 mock 对象，释放资源
     *
     * @since 1.0
     */
    @AfterEach
    public void tearDown() {
        // 关闭静态 mock
        if (ossUtilsMock != null) {
            ossUtilsMock.close();
        }
    }

    /**
     * 测试 CustomOssUtils.putObject 方法在参数有效时的正常行为
     * <p>
     * 测试场景：传入有效的 API 地址、请求键、HTTP 方法、文件名、输入流、请求参数和请求头
     * 预期结果：方法应成功执行并返回包含 headerInfo、params、filePart、response 和 json 的结果 Map
     * <p>
     * 该测试验证了方法对 mock 连接对象的正确调用，包括设置请求方法、输出输入流、连接和断开连接等操作
     * <p>
     * 注意：测试中使用了 mock 对象模拟 HTTP 连接行为，确保测试不依赖真实网络环境
     */
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

    /**
     * 测试 putObject 方法在请求文本为 null 的情况下的行为
     * <p>
     * 测试场景：调用 putObject 方法时传入 null 的 requestText 参数
     * 预期结果：方法应正常执行，并返回包含 headerInfo、params、filePart、response 和 json 键的 Map 对象
     * <p>
     * 说明：该测试模拟了 HTTP 连接对象，设置其返回码、消息、输入流和相关属性为预设值，用于验证 CustomOssUtils 类在处理 null 请求文本时的逻辑正确性
     */
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

    /**
     * 测试 putObject 方法在请求文本为空时的行为
     * <p>
     * 测试场景：当请求文本为空时，调用 putObject 方法上传文件
     * 预期结果：应返回包含 headerInfo、params、filePart、response 和 json 的结果 Map
     * <p>
     * 该测试模拟了 HTTP 连接的行为，包括响应码、响应消息、输入流和请求方法等
     * 验证结果中是否包含预期的键，确保方法能够正确处理空请求参数的情况
     */
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

    /**
     * 测试使用无效 API 地址上传对象时的异常处理
     * <p>
     * 测试场景：当调用 OssUtils.connect 方法时传入无效的 API 地址，模拟连接失败
     * 预期结果：应抛出 IOException 异常，提示 "Invalid URL"
     * <p>
     * 注意：该测试依赖 OssUtils.connect 方法的 mock 行为，需确保相关 mock 配置正确
     */
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
        assertThrows(Exception.class, () -> CustomOssUtils.putObject(api, requestKey, httpMethod, fileName, inputStream, requestText,
                                                                     header));
    }

    /**
     * 测试 putObject 方法在输入流为 null 时的行为
     * <p>
     * 测试场景：当调用 putObject 方法且输入流为 null 时
     * 预期结果：应抛出异常
     * <p>
     * 该测试验证了在输入流为 null 的情况下，CustomOssUtils.putObject 方法是否能够正确识别并抛出异常。
     * <p>
     * 注意：测试中使用了 mock 对象模拟 HTTP 连接行为，以确保测试环境的可控性。
     */
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
        assertThrows(Exception.class, () -> CustomOssUtils.putObject(api, requestKey, httpMethod, fileName, inputStream, requestText,
                                                                     header));
    }

    /**
     * 测试 putObject 方法在文件名为空字符串时的行为
     * <p>
     * 测试场景：上传文件时文件名为空
     * 预期结果：应返回非空的响应结果
     * <p>
     * 该测试模拟了 HTTP 连接的响应，验证当文件名为空时，方法是否能正确处理并返回结果
     */
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

    /**
     * 测试 putObject 方法在服务器返回错误时的处理逻辑
     * <p>
     * 测试场景：模拟服务器返回 500 错误状态码及错误信息
     * 预期结果：应返回包含错误响应码和错误信息的 Map 结构
     * <p>
     * 该测试验证当服务器发生错误时，CustomOssUtils.putObject 方法是否能够正确捕获并返回错误信息
     * <p>
     * 注意：测试中使用了 mock 对象模拟网络连接行为，包括响应码、响应消息和错误流
     */
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

    /**
     * 测试 writeFile 方法在传入有效参数时的执行行为
     * <p>
     * 测试场景：验证通过反射调用私有方法时，是否能正确处理文件上传逻辑
     * 预期结果：返回的字符串应包含 "请求上传文件部分"，表明方法执行成功
     * <p>
     * 注意：此测试使用反射调用私有方法，需确保方法签名与实际一致
     */
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

    /**
     * 测试 writeParams 方法在传入有效参数时的执行行为
     * <p>
     * 测试场景：使用反射调用 CustomOssUtils 类的私有 writeParams 方法，传入包含参数的 Map 和 OutputStream
     * 预期结果：方法应返回非空字符串，并且该字符串包含 "请求参数部分" 以及传入的参数名 param1 和 param2
     * <p>
     * 注意：该测试需要通过反射调用私有方法，因此需要设置方法为可访问
     */
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

    /**
     * 测试 writeParams 方法在参数为 null 时的处理逻辑
     * <p>
     * 测试场景：当传入的参数为 null 时，调用 writeParams 方法
     * 预期结果：返回的字符串应包含 "请求参数部分" 和 "空" 的关键字，表示参数为空的情况被正确处理
     * <p>
     * 注意：该测试通过反射调用私有方法 writeParams，验证其在参数为 null 时的行为
     */
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

    /**
     * 测试 writeParams 方法在参数为空时的处理逻辑
     * <p>
     * 测试场景：当传入的参数 Map 为空时
     * 预期结果：方法应正确生成包含"请求参数部分"和"空"关键字的字符串结果
     * <p>
     * 该测试通过反射调用私有方法 writeParams，验证其在参数为空情况下的输出内容
     */
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

    /**
     * 测试边界标识符的生成逻辑
     * <p>
     * 测试场景：验证 CustomOssUtils 类中 BOUNDARY 字段的值是否符合预期
     * 预期结果：边界标识符不为空，且仅包含小写字母和数字，不包含连字符
     * <p>
     * 注意：该测试需要直接访问 CustomOssUtils 类的私有常量 BOUNDARY，通过反射获取其值进行验证
     */
    @Test
    public void testBoundaryGeneration() throws Exception {
        // 测试边界标识符的生成
        java.lang.reflect.Field boundaryField = CustomOssUtils.class.getDeclaredField("BOUNDARY");
        boundaryField.setAccessible(true);
        String boundary = (String) boundaryField.get(null);

        // 验证边界标识符不为空且不包含连字符
        assertNotNull(boundary);
        assertFalse(boundary.isEmpty());
        assertTrue(boundary.matches("[a-z0-9]+")); // 只包含小写字母和数字
    }

    /**
     * 测试 CustomOssUtils 的 putObject 方法是否正确调用了连接的 disconnect 方法
     * <p>
     * 测试场景：模拟一个 HTTP 连接对象，并验证在上传文件过程中该连接是否被正确关闭
     * 预期结果：disconnect 方法应被调用一次
     * <p>
     * 该测试通过 mock 对象模拟 HTTP 连接行为，确保在 putObject 方法执行完毕后，连接的 disconnect 方法被正确调用
     */
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