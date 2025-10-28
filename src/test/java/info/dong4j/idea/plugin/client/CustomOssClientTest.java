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
 * 用于测试CustomOssClient类中的URL解析功能
 * </p>
 *
 * @author dong4j
 * @version 1.0.0
 * @since 2025-10-28
 */
public class CustomOssClientTest {

    /**
     * 测试从JSON中提取URL的功能
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
     * 测试边界情况：空路径和复杂嵌套
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
     * 测试客户端实例获取和云类型
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
     * 通过反射调用私有方法extractUrlFromJson
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