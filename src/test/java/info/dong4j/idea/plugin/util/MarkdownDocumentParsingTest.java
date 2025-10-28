package info.dong4j.idea.plugin.util;

import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.testFramework.LightVirtualFile;

import info.dong4j.idea.plugin.entity.MarkdownImage;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Markdown 文档解析测试类
 * <p>
 * 用于测试 Markdown 文档中图片标签的识别与解析功能，验证不同格式的图片标记是否能被正确解析并提取相关信息。
 * 包括普通文本、带链接的图片、带属性的图片标签以及空标题图片等场景。
 *
 * @author 作者名
 * @version 1.0.0
 * @date 2025.10.24
 * @since 1.0.0
 */
public class MarkdownDocumentParsingTest {

    /**
     * 创建并返回一个空的Markdown文件虚拟对象
     * <p>
     * 用于生成一个名为"doc.md"的空Markdown文件虚拟对象，文件内容为空字符串
     *
     * @return 一个空的Markdown文件虚拟对象
     */
    private LightVirtualFile mdFile() {
        return new LightVirtualFile("doc.md", "");
    }

    /**
     * 测试多行混合标签的识别与解析功能
     * <p>
     * 测试场景：验证包含普通文本、图片链接和带属性的链接的多行文本是否能被正确解析
     * 预期结果：应正确识别并提取出3个图片链接，分别对应标题为"a"、"b"和"c"的图片
     * <p>
     * 注意：测试中使用了 {@link MarkdownUtils#analysisImageMark(VirtualFile, String, int)} 方法进行解析
     */
    @Test
    @DisplayName("多行混合标签应逐行被正确识别与解析")
    void multiLineMixedParsing() {
        String[] lines = new String[] {
            "普通文本，无图片",
            "![a](https://a.com/1.png)",
            "<a data-fancybox href='https://a.com/big.jpg'>![b](https://a.com/big.jpg)</a>",
            "![空标题]()",
            "![c](https://a.com/2.jpeg)  末尾有空格"
        };

        List<MarkdownImage> parsed = new ArrayList<>();
        for (int i = 0; i < lines.length; i++) {
            MarkdownImage mi = MarkdownUtils.analysisImageMark(mdFile(), lines[i], i);
            if (mi != null) {
                parsed.add(mi);
            }
        }

        assertEquals(3, parsed.size());
        assertEquals("a", parsed.get(0).getTitle());
        assertEquals("b", parsed.get(1).getTitle());
        assertEquals("c", parsed.get(2).getTitle());
    }
}


