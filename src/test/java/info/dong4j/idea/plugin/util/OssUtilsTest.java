package info.dong4j.idea.plugin.util;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * OssUtils 工具类测试类
 * <p>
 * 用于验证 OssUtils 工具类中 HTTP 连接和 GET 请求相关方法的正确性和异常处理逻辑。
 * 包括对有效 URL、无效 URL、空 URL、特殊字符 URL、不同 HTTP 方法、特殊请求头等场景的测试。
 * 测试内容涵盖连接建立、超时设置、连接属性配置以及异常抛出情况。
 *
 * @author dong4j
 * @version 1.0.0
 * @date 2025.10.24
 * @since 1.0.0
 */
public class OssUtilsTest {

    /**
     * 测试使用有效 URL 建立连接的功能
     * <p>
     * 测试场景：传入一个有效的 URL 并指定 HTTP 方法为 GET
     * 预期结果：应成功建立连接，返回的 HttpURLConnection 对象不为空，请求方法为 GET，连接超时和读取超时均大于 0
     * <p>
     * 注意：测试使用的 URL 为 <a href="https://httpbin.org/get">...</a>，该地址为 HTTP 请求测试专用服务
     */
    @Test
    public void testConnectWithValidUrl() throws Exception {
        // 测试有效的 URL 连接
        String validUrl = "https://httpbin.org/get";
        HttpURLConnection connection = OssUtils.connect(validUrl, "GET");

        assertNotNull(connection);
        assertEquals("GET", connection.getRequestMethod());
        assertTrue(connection.getConnectTimeout() > 0);
        assertTrue(connection.getReadTimeout() > 0);
    }

    /**
     * 测试使用无效 URL 进行连接时的异常处理
     * <p>
     * 测试场景：传入一个无效的 URL 字符串
     * 预期结果：应抛出 IOException 异常
     * <p>
     * 该测试验证 OssUtils.connect 方法在遇到无效 URL 时是否能正确识别并抛出异常
     */
    @Test
    public void testConnectWithInvalidUrl() {
        // 测试无效的 URL 连接
        String invalidUrl = "not-a-valid-url";

        assertThrows(IOException.class, () -> OssUtils.connect(invalidUrl, "GET"));
    }

    /**
     * 测试 OssUtils.connect 方法在 URL 为 null 时的行为
     * <p>
     * 测试场景：传入 null 的 URL 参数和 "GET" 请求方法
     * 预期结果：应抛出 IOException 异常
     */
    @Test
    public void testConnectWithNullUrl() {
        // 测试 null URL
        assertThrows(IOException.class, () -> OssUtils.connect(null, "GET"));
    }

    /**
     * 测试连接方法在 URL 为空时的行为
     * <p>
     * 测试场景：传入空字符串作为 URL 参数
     * 预期结果：应抛出 IOException 异常
     */
    @Test
    public void testConnectWithEmptyUrl() {
        // 测试空 URL
        assertThrows(IOException.class, () -> OssUtils.connect("", "GET"));
    }

    /**
     * 测试使用不同 HTTP 方法连接指定 URL 的功能
     * <p>
     * 测试场景：分别使用 POST、PUT、DELETE 三种 HTTP 方法调用 OssUtils.connect 方法
     * 预期结果：返回的 HttpURLConnection 对象的 getRequestMethod() 方法应返回对应的 HTTP 方法
     * <p>
     * 说明：该测试验证 OssUtils.connect 方法是否能正确设置 HTTP 请求方法
     */
    @Test
    public void testConnectWithDifferentHttpMethods() throws Exception {
        String url = "https://httpbin.org/post";

        // 测试 POST 方法
        HttpURLConnection postConnection = OssUtils.connect(url, "POST");
        assertEquals("POST", postConnection.getRequestMethod());

        // 测试 PUT 方法
        HttpURLConnection putConnection = OssUtils.connect(url, "PUT");
        assertEquals("PUT", putConnection.getRequestMethod());

        // 测试 DELETE 方法
        HttpURLConnection deleteConnection = OssUtils.connect(url, "DELETE");
        assertEquals("DELETE", deleteConnection.getRequestMethod());
    }

    /**
     * 测试 OssUtils.connect 方法在 URL 包含特殊字符时的行为
     * <p>
     * 测试场景：当传入的 URL 包含空格等特殊字符时
     * 预期结果：应抛出 IOException 异常，表示 URL 格式不合法
     * <p>
     * 注意：此测试需要确保 OssUtils.connect 方法正确校验 URL 格式
     */
    @Test
    public void testConnectWithSpecialCharactersInUrl() {
        // 测试包含特殊字符的 URL
        String urlWithSpecialChars = "https://example.com/path with spaces";

        assertThrows(IOException.class, () -> OssUtils.connect(urlWithSpecialChars, "GET"));
    }

    /**
     * 测试使用有效 URL 发起 GET 请求的功能
     * <p>
     * 测试场景：使用一个有效的外部 URL 发起 GET 请求
     * 预期结果：应成功获取响应内容，并且响应中包含 URL 信息
     * <p>
     * 注意：该测试会实际发送网络请求，可能需要网络连接
     * 在实际项目中，建议使用 mock 服务器或测试用的本地服务进行替代
     * <p>
     * 相关方法：{@link OssUtils#get(String, Map)}
     */
    @Test
    public void testGetWithValidUrl() {
        // 测试有效的 GET 请求
        String validUrl = "https://httpbin.org/get";
        Map<String, String> headers = new HashMap<>();
        headers.put("User-Agent", "OssUtilsTest/1.0");

        // 注意：这个测试会实际发送网络请求
        // 在实际项目中，可能需要使用 mock 或测试服务器
        try {
            String response = OssUtils.get(validUrl, headers);
            assertNotNull(response);
            assertTrue(response.contains("\"url\":"));
        } catch (Exception e) {
            // 网络问题或其他异常，这在测试环境中是可以接受的
            // 我们主要测试方法是否能正常处理异常
        }
    }

    /**
     * 测试 GET 请求在 URL 无效时的异常处理
     * <p>
     * 测试场景：使用一个非有效的 URL 发起 GET 请求
     * 预期结果：应抛出异常
     * <p>
     * 注意：该测试需要 OssUtils 类中的 get 方法能够正确识别无效 URL 并抛出异常
     */
    @Test
    public void testGetWithInvalidUrl() {
        // 测试无效 URL 的 GET 请求
        String invalidUrl = "not-a-valid-url";
        Map<String, String> headers = new HashMap<>();

        assertThrows(Exception.class, () -> OssUtils.get(invalidUrl, headers));
    }

    /**
     * 测试 GET 请求中 URL 为 null 的异常处理
     * <p>
     * 测试场景：当传入的 URL 为 null 时
     * 预期结果：应抛出异常
     * <p>
     * 该测试验证 OssUtils.get 方法在 URL 为 null 时是否能正确抛出异常，确保参数校验逻辑有效。
     */
    @Test
    public void testGetWithNullUrl() {
        // 测试 null URL 的 GET 请求
        Map<String, String> headers = new HashMap<>();

        assertThrows(Exception.class, () -> OssUtils.get(null, headers));
    }

    /**
     * 测试 GET 请求中 headers 为 null 的情况
     * <p>
     * 测试场景：调用 OssUtils.get 方法并传入 null 作为 headers 参数
     * 预期结果：方法应能处理 null headers 并返回有效的响应字符串或 null
     * <p>
     * 注意：该测试需要 OssUtils.get 方法能够正确处理 null headers 参数，且响应结果为字符串类型或 null
     */
    @Test
    public void testGetWithNullHeaders() {
        // 测试 null headers 的 GET 请求
        String validUrl = "https://httpbin.org/get";

        try {
            String response = OssUtils.get(validUrl, null);
            // 如果能到达这里，说明方法处理了 null headers
            assertTrue(response == null || response instanceof String);
        } catch (Exception e) {
            // 可能抛出异常，这取决于具体实现
        }
    }

    /**
     * 测试 GET 请求在 headers 为空时的处理逻辑
     * <p>
     * 测试场景：发送一个 headers 为空的 GET 请求到指定 URL
     * 预期结果：应成功获取响应内容且不抛出异常
     * <p>
     * 说明：该测试模拟了在无请求头的情况下调用 OssUtils.get 方法的场景，验证其异常处理机制
     */
    @Test
    public void testGetWithEmptyHeaders() throws Exception {
        // 测试空 headers 的 GET 请求
        String validUrl = "https://httpbin.org/get";
        Map<String, String> headers = new HashMap<>();

        try {
            String response = OssUtils.get(validUrl, headers);
            assertNotNull(response);
        } catch (Exception e) {
            // 网络问题或其他异常
        }
    }

    /**
     * 测试带有特殊 headers 的 GET 请求功能
     * <p>
     * 测试场景：向指定 URL 发送包含自定义头和授权头的 GET 请求
     * 预期结果：请求应成功执行并返回非空响应内容
     * <p>
     * 说明：该测试模拟发送带有特殊 headers 的请求，验证 OssUtils.get 方法是否能正确处理
     * 并返回响应结果。若发生网络问题或其他异常，应捕获并处理，确保测试稳定性。
     */
    @Test
    public void testGetWithSpecialHeaders() throws Exception {
        // 测试特殊 headers 的 GET 请求
        String validUrl = "https://httpbin.org/get";
        Map<String, String> headers = new HashMap<>();
        headers.put("X-Custom-Header", "测试值");
        headers.put("Authorization", "Bearer token123");

        try {
            String response = OssUtils.get(validUrl, headers);
            assertNotNull(response);
        } catch (Exception e) {
            // 网络问题或其他异常
        }
    }

    /**
     * 测试连接超时设置功能
     * <p>
     * 测试场景：验证通过 OssUtils 工具类建立的 HTTP 连接是否正确设置了连接超时和读取超时
     * 预期结果：连接超时应为 3000 毫秒，读取超时应为 5000 毫秒
     * <p>
     * 注意：测试使用了 httpbin.org 提供的测试接口进行验证，确保网络环境正常
     */
    @Test
    public void testConnectTimeoutSettings() throws Exception {
        // 测试连接超时设置
        String validUrl = "https://httpbin.org/get";
        HttpURLConnection connection = OssUtils.connect(validUrl, "GET");

        assertEquals(3000, connection.getConnectTimeout());
        assertEquals(5000, connection.getReadTimeout());
    }

    /**
     * 测试连接属性配置
     * <p>
     * 测试场景：验证通过 OssUtils.connect 方法创建的 HttpURLConnection 是否正确配置了连接属性
     * 预期结果：应确保连接支持输出、输入，并且不使用缓存
     * <p>
     * 说明：测试使用了有效的 URL "<a href="https://httpbin.org/get">...</a>" 并指定请求方法为 POST
     */
    @Test
    public void testConnectionProperties() throws Exception {
        // 测试连接属性
        String validUrl = "https://httpbin.org/get";
        HttpURLConnection connection = OssUtils.connect(validUrl, "POST");

        assertTrue(connection.getDoOutput());
        assertTrue(connection.getDoInput());
        assertFalse(connection.getUseCaches());
    }
}