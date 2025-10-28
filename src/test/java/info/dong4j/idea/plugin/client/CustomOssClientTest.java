package info.dong4j.idea.plugin.client;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import info.dong4j.idea.plugin.enums.CloudEnum;
import info.dong4j.idea.plugin.util.StringUtils;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * 自定义OSS客户端测试类
 * <p>
 * 用于验证CustomOssClient类中URL解析和客户端功能的正确性，包括JSON路径解析、边界情况处理、实例获取以及上传功能的测试。
 * <p>
 * 该测试类通过反射调用私有方法，确保对核心逻辑的全面覆盖，包括正常情况、空值、嵌套路径、特殊字符处理等场景。
 *
 * @author dong4j
 * @version 1.0.0
 * @date 2025.10.28
 * @since 1.0.0
 */
public class CustomOssClientTest {
    /**
     * 测试从JSON中提取URL的功能
     * <p>
     * 测试场景：验证 {@link CustomOssClient#extractUrlFromJson(JsonElement, String)} 方法在不同JSON结构和输入情况下的行为
     * 预期结果：正确提取指定路径的URL，当路径不存在或输入无效时应返回空字符串
     * <p>
     * 测试用例包括：
     * 1. 直接URL格式的JSON
     * 2. 嵌套URL格式的JSON
     * 3. 路径不存在的情况
     * 4. 空JSON对象
     * 5. null输入
     */
    @Test
    public void testExtractUrlFromJson() {
        CustomOssClient client = CustomOssClient.getInstance();

        // 测试直接URL格式
        String json1 = "{\"url\": \"https://example.com/image.jpg\"}";
        JsonElement element1 = JsonParser.parseString(json1);
        // 使用反射调用私有方法
        String url1 = invokeExtractUrlFromJson(client, element1, "url");
        assertEquals("https://example.com/image.jpg", url1);

        // 测试嵌套URL格式
        String json2 = "{\"data\": {\"url\": \"https://example.com/image.jpg\"}}";
        JsonElement element2 = JsonParser.parseString(json2);
        String url2 = invokeExtractUrlFromJson(client, element2, "data.url");
        assertEquals("https://example.com/image.jpg", url2);

        // 测试不存在的路径
        String url3 = invokeExtractUrlFromJson(client, element1, "data.url");
        assertTrue(StringUtils.isBlank(url3));

        // 测试空JSON
        JsonElement element4 = JsonParser.parseString("{}");
        String url4 = invokeExtractUrlFromJson(client, element4, "url");
        assertTrue(StringUtils.isBlank(url4));

        // 测试null输入
        String url5 = invokeExtractUrlFromJson(client, null, "url");
        assertTrue(StringUtils.isBlank(url5));
    }

    /**
     * 测试从 JSON 中提取 URL 的边界情况和复杂场景
     * <p>
     * 测试目标：验证 {@link CustomOssClient#extractUrlFromJson(JsonElement, String)} 方法在不同输入情况下的行为
     * <p>
     * 测试场景：
     * 1. 当路径为空字符串时，应返回空字符串
     * 2. 当路径为 null 时，应返回空字符串
     * 3. 当路径深度嵌套时，应正确提取到目标 URL
     * 4. 当路径中包含点号时，应无法找到对应的 URL（点号被当作分隔符）
     * 5. 当目标值为数字类型时，应将其转换为字符串返回
     * 6. 当目标值为对象类型时，应返回空字符串（对象无法转换为字符串）
     * <p>
     * 预期结果：所有测试用例应符合上述预期行为
     */
    @Test
    public void testExtractUrlFromJsonEdgeCases() {
        CustomOssClient client = CustomOssClient.getInstance();

        // 测试空路径
        String json1 = "{\"url\": \"https://example.com/image.jpg\"}";
        JsonElement element1 = JsonParser.parseString(json1);
        String url1 = invokeExtractUrlFromJson(client, element1, "");
        assertTrue(StringUtils.isBlank(url1));

        // 测试null路径
        String url2 = invokeExtractUrlFromJson(client, element1, null);
        assertTrue(StringUtils.isBlank(url2));

        // 测试深度嵌套
        String json3 = "{\"level1\": {\"level2\": {\"level3\": {\"url\": \"https://example.com/image.jpg\"}}}}";
        JsonElement element3 = JsonParser.parseString(json3);
        String url3 = invokeExtractUrlFromJson(client, element3, "level1.level2.level3.url");
        assertEquals("https://example.com/image.jpg", url3);

        // 测试路径中包含点号的情况
        String json4 = "{\"data.url\": \"https://example.com/image.jpg\"}";
        JsonElement element4 = JsonParser.parseString(json4);
        String url4 = invokeExtractUrlFromJson(client, element4, "data.url");
        assertTrue(StringUtils.isBlank(url4)); // 应该无法找到，因为点号被当作分隔符

        // 测试非字符串值
        String json5 = "{\"url\": 123}";
        JsonElement element5 = JsonParser.parseString(json5);
        String url5 = invokeExtractUrlFromJson(client, element5, "url");
        assertEquals("123", url5); // 数字会被转换为字符串

        // 测试对象值
        String json6 = "{\"url\": {\"value\": \"https://example.com/image.jpg\"}}";
        JsonElement element6 = JsonParser.parseString(json6);
        String url6 = invokeExtractUrlFromJson(client, element6, "url");
        assertTrue(StringUtils.isBlank(url6)); // 对象无法转换为字符串
    }

    /**
     * 测试客户端实例获取和云类型配置
     * <p>
     * 测试场景：验证 CustomOssClient 是否为单例模式，并检查其云类型是否正确
     * 预期结果：两个通过 getInstance() 获取的实例应为同一对象，且云类型应为 CUSTOMIZE
     * <p>
     * 注意：该测试依赖 CustomOssClient 的单例实现和云类型配置
     */
    @Test
    public void testClientInstanceAndCloudType() {
        CustomOssClient client1 = CustomOssClient.getInstance();
        CustomOssClient client2 = CustomOssClient.getInstance();

        // 测试单例
        assertEquals(client1, client2);

        // 测试云类型
        assertEquals(CloudEnum.CUSTOMIZE, client1.getCloudType());
    }

    /**
     * 测试上传功能的边界情况
     * <p>
     * 测试场景：验证上传方法在输入参数为非法值时的行为，包括空文件名和null输入流
     * 预期结果：应抛出异常，且返回结果为空字符串
     */
    @Test
    public void testUploadEdgeCases() {
        CustomOssClient client = CustomOssClient.getInstance();

        // 测试空文件名
        try {
            String result = client.upload(null, "");
            assertTrue(StringUtils.isBlank(result));
        } catch (Exception e) {
            // 期望抛出异常
        }

        // 测试null输入流
        try {
            String result = client.upload(null, "test.jpg");
            assertTrue(StringUtils.isBlank(result));
        } catch (Exception e) {
            // 期望抛出异常
        }
    }

    /**
     * 通过反射调用CustomOssClient类的私有方法extractUrlFromJson
     * <p>
     * 该方法使用反射机制获取并调用CustomOssClient类中定义的私有方法extractUrlFromJson，
     * 传入指定的JsonElement对象和路径字符串，返回解析后的URL字符串。
     *
     * @param client CustomOssClient实例，用于调用私有方法
     * @param json   传入的JsonElement对象，用于解析URL信息
     * @param path   路径字符串，用于指定解析的路径
     * @return 解析得到的URL字符串
     * @throws RuntimeException 如果反射调用过程中发生异常，将异常信息封装后抛出
     */
    private String invokeExtractUrlFromJson(CustomOssClient client, JsonElement json, String path) {
        try {
            // 使用反射获取方法
            java.lang.reflect.Method method = CustomOssClient.class.getDeclaredMethod(
                "extractUrlFromJson", JsonElement.class, String.class);
            method.setAccessible(true);
            Object result = method.invoke(client, json, path);
            return (String) result;
        } catch (Exception e) {
            fail("调用extractUrlFromJson方法时发生异常: " + e.getMessage());
            return null;
        }
    }
}