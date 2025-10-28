package info.dong4j.idea.plugin.util;

import com.intellij.testFramework.LightVirtualFile;

import info.dong4j.idea.plugin.entity.MarkdownImage;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MarkdownDocumentParsingTest {

    private LightVirtualFile mdFile() {
        return new LightVirtualFile("doc.md", "");
    }

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


