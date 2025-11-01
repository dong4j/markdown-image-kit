package info.dong4j.idea.plugin.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * PlaceholderParser 测试类
 *
 * @author dong4j
 * @version 1.0.0
 * @date 2025.11.01
 * @since 2.2.0
 */
class PlaceholderParserTest {

    @Test
    void testParseFilename() {
        String template = "${filename}";
        String original = "test-image.png";
        String result = PlaceholderParser.parse(template, original);
        assertEquals("test-image.png", result);
    }

    @Test
    void testParseDatetime() {
        String template = "${datetime:yyyyMMdd}";
        String original = "test.png";
        String result = PlaceholderParser.parse(template, original);
        // 应该匹配8位数字的日期格式
        assertTrue(result.matches("\\d{8}\\.png"), "结果应该是日期格式：" + result);
    }

    @Test
    void testParseDatetimeWithFilename() {
        String template = "${datetime:yyyy-MM-dd}_${filename}";
        String original = "test-image.png";
        String result = PlaceholderParser.parse(template, original);
        // 应该匹配 yyyy-MM-dd_test-image.png 格式
        assertTrue(result.matches("\\d{4}-\\d{2}-\\d{2}_test-image\\.png"), "结果格式不正确：" + result);
    }

    @Test
    void testParseRandomString() {
        String template = "${string:6}";
        String original = "test.png";
        String result = PlaceholderParser.parse(template, original);
        // 应该是6个字符 + .png
        assertTrue(result.matches("[a-zA-Z0-9]{6}\\.png"), "结果格式不正确：" + result);
    }

    @Test
    void testParseRandomNumber() {
        String template = "${number:6}";
        String original = "test.png";
        String result = PlaceholderParser.parse(template, original);
        // 应该是6位数字 + .png
        assertTrue(result.matches("\\d{6}\\.png"), "结果格式不正确：" + result);
    }

    @Test
    void testParseComplexTemplate() {
        String template = "${datetime:yyyyMMdd}_${string:4}_${filename}";
        String original = "my-photo.jpg";
        String result = PlaceholderParser.parse(template, original);
        // 应该匹配 yyyyMMdd_xxxx_my-photo.jpg 格式
        assertTrue(result.matches("\\d{8}_[a-zA-Z0-9]{4}_my-photo\\.jpg"), "结果格式不正确：" + result);
    }

    @Test
    void testParseAutoExtension() {
        String template = "${datetime:yyyyMMdd}_${filename}";
        String original = "test.png";
        String result = PlaceholderParser.parse(template, original);
        // 应该自动添加 .png 扩展名
        assertTrue(result.matches("\\d{8}_test\\.png"), "结果格式不正确：" + result);
    }

    @Test
    void testParseWithPrefix() {
        String template = "img_${datetime:yyyyMMdd}_${number:6}";
        String original = "photo.png";
        String result = PlaceholderParser.parse(template, original);
        // 应该匹配 img_yyyyMMdd_nnnnnn.png 格式
        assertTrue(result.matches("img_\\d{8}_\\d{6}\\.png"), "结果格式不正确：" + result);
    }

    @Test
    void testParseMIKFormat() {
        String template = "MIK-${string:6}";
        String original = "image.jpg";
        String result = PlaceholderParser.parse(template, original);
        // 应该匹配 MIK-xxxxxx.jpg 格式
        assertTrue(result.matches("MIK-[a-zA-Z0-9]{6}\\.jpg"), "结果格式不正确：" + result);
    }

    @Test
    void testParseEmptyTemplate() {
        String template = "";
        String original = "test.png";
        String result = PlaceholderParser.parse(template, original);
        // 空模板应该返回原文件名
        assertEquals("test.png", result);
    }

    @Test
    void testParseNullTemplate() {
        String template = null;
        String original = "test.png";
        String result = PlaceholderParser.parse(template, original);
        // null模板应该返回原文件名
        assertEquals("test.png", result);
    }

    @Test
    void testParseWithSpaces() {
        String template = "${filename}";
        String original = "test image with spaces.png";
        String result = PlaceholderParser.parse(template, original);
        // 空格应该被移除
        assertEquals("testimagewithspaces.png", result);
    }

    @Test
    void testValidateTemplate() {
        assertTrue(PlaceholderParser.validateTemplate("${filename}"));
        assertTrue(PlaceholderParser.validateTemplate("${datetime:yyyyMMdd}_${string:6}"));
        assertTrue(PlaceholderParser.validateTemplate("prefix_${filename}_suffix"));

        assertFalse(PlaceholderParser.validateTemplate(""));
        assertFalse(PlaceholderParser.validateTemplate(null));
        assertFalse(PlaceholderParser.validateTemplate("${unclosed"));
        assertFalse(PlaceholderParser.validateTemplate("unopened}"));
    }

    @Test
    void testGetDefaultTemplate() {
        String defaultTemplate = PlaceholderParser.getDefaultTemplate();
        assertEquals("${filename}", defaultTemplate);
    }

    @Test
    void testGetPresetTemplates() {
        String[] presets = PlaceholderParser.getPresetTemplates();
        assertNotNull(presets);
        assertTrue(presets.length > 0);
        assertEquals("${filename}", presets[0]);
    }

    @Test
    void testParseMultipleDatetimeFormats() {
        // 测试不同的日期格式
        String[] templates = {
            "${datetime:yyyy}",
            "${datetime:yyyy-MM}",
            "${datetime:yyyy-MM-dd}",
            "${datetime:yyyyMMddHHmmss}",
            "${datetime:HH:mm:ss}"
        };

        for (String template : templates) {
            String result = PlaceholderParser.parse(template, "test.png");
            assertNotNull(result);
            assertTrue(result.endsWith(".png"), "结果应该以.png结尾：" + result);
        }
    }

    @Test
    void testParseDifferentExtensions() {
        String template = "${filename}";

        assertEquals("test.png", PlaceholderParser.parse(template, "test.png"));
        assertEquals("test.jpg", PlaceholderParser.parse(template, "test.jpg"));
        assertEquals("test.gif", PlaceholderParser.parse(template, "test.gif"));
        assertEquals("test.webp", PlaceholderParser.parse(template, "test.webp"));
    }

    @Test
    void testParseRandomStringLength() {
        // 测试不同长度的随机字符串
        for (int length = 1; length <= 10; length++) {
            String template = "${string:" + length + "}";
            String result = PlaceholderParser.parse(template, "test.png");
            String pattern = "[a-zA-Z0-9]{" + length + "}\\.png";
            assertTrue(result.matches(pattern),
                       "长度" + length + "的随机字符串格式不正确：" + result);
        }
    }

    @Test
    void testParseRandomNumberLength() {
        // 测试不同长度的随机数字
        for (int length = 1; length <= 10; length++) {
            String template = "${number:" + length + "}";
            String result = PlaceholderParser.parse(template, "test.png");
            String pattern = "\\d{" + length + "}\\.png";
            assertTrue(result.matches(pattern),
                       "长度" + length + "的随机数字格式不正确：" + result);
        }
    }

    @Test
    void testParseFilenameWithSuffix() {
        String template = "${filename}_backup";
        String original = "document.pdf";
        String result = PlaceholderParser.parse(template, original);
        assertEquals("document_backup.pdf", result);
    }

    @Test
    void testParseComplexFilename() {
        String template = "${datetime:yyyy}/${datetime:MM}/${datetime:dd}_${filename}";
        String original = "report-2025.pdf";
        String result = PlaceholderParser.parse(template, original);
        // 应该匹配 yyyy/MM/dd_report-2025.pdf 格式
        assertTrue(result.matches("\\d{4}/\\d{2}/\\d{2}_report-2025\\.pdf"),
                   "复杂文件名格式不正确：" + result);
    }
}

