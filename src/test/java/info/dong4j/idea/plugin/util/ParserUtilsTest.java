package info.dong4j.idea.plugin.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * 解析工具类测试类
 * <p>
 * 用于测试 ParserUtils 工具类中各种解析方法的正确性，包括占位符替换和图片标签解析功能。
 *
 * @author 作者名
 * @version 1.0.0
 * @date 2025.10.24
 * @since 1.0.0
 */
public class ParserUtilsTest {

    /**
     * 测试占位符解析器的基本替换功能
     * <p>
     * 测试场景：验证 parse0、parse1、parse2 方法对不同格式占位符的替换能力
     * 预期结果：替换后的字符串应与预期值一致
     * <p>
     * 测试用例说明：
     * 1. parse0 方法测试空占位符替换
     * 2. parse1 方法测试索引式占位符替换
     * 3. parse2 方法测试变量名式占位符替换
     */
    @Test
    @DisplayName("parse0/parse1/parse2 基本占位符替换")
    void placeholderParsers() {
        assertEquals("hello world", ParserUtils.parse0("hello ${}", "world"));
        assertEquals("A 1 B 2", ParserUtils.parse1("A {0} B {1}", 1, 2));
        assertEquals("title-x path-y", ParserUtils.parse2("title-${title} path-${path}", "x", "y"));
    }

    /**
     * 测试解析图片标签功能
     * <p>
     * 测试场景：解析类似 ![描述](路径) 的图片标签
     * 预期结果：应返回包含描述与路径的 Map，其中键为描述，值为路径
     * <p>
     * 注意：测试中使用了 ParserUtils.parseImageTag 方法进行解析
     */
    @Test
    @DisplayName("解析 ![xxx](yyy) 应返回包含描述与路径的 Map")
    void parseImageTag() {
        Map<String, String> map = ParserUtils.parseImageTag("![描述](路径)");
        assertEquals(1, map.size());
        assertEquals("路径", map.get("描述"));
    }
}