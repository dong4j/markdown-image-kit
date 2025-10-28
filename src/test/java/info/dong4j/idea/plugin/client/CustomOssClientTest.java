package info.dong4j.idea.plugin.client;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

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