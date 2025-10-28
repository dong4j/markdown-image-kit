package info.dong4j.idea.plugin.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ParserUtilsTest {

    @Test
    @DisplayName("parse0/parse1/parse2 基本占位符替换")
    void placeholderParsers() {
        assertEquals("hello world", ParserUtils.parse0("hello ${}", "world"));
        assertEquals("A 1 B 2", ParserUtils.parse1("A {0} B {1}", 1, 2));
        assertEquals("title-x path-y", ParserUtils.parse2("title-${title} path-${path}", "x", "y"));
    }

    @Test
    @DisplayName("解析 ![xxx](yyy) 应返回包含描述与路径的 Map")
    void parseImageTag() {
        Map<String, String> map = ParserUtils.parseImageTag("![描述](路径)");
        assertEquals(1, map.size());
        assertEquals("路径", map.get("描述"));
    }
}