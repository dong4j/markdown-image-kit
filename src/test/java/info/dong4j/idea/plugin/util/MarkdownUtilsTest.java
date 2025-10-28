package info.dong4j.idea.plugin.util;

import com.intellij.testFramework.LightVirtualFile;

import info.dong4j.idea.plugin.entity.MarkdownImage;
import info.dong4j.idea.plugin.enums.ImageLocationEnum;
import info.dong4j.idea.plugin.enums.ImageMarkEnum;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MarkdownUtilsTest {

    private LightVirtualFile mdFile() {
        return new LightVirtualFile("test.md", "");
    }

    @Test
    @DisplayName("网络图片标签应正确解析并识别为网络位置")
    void analysisNetworkImageMark() {
        String lineText = "![网络图片](https://example.com/image.jpg)";
        MarkdownImage image = MarkdownUtils.analysisImageMark(mdFile(), lineText, 0);

        assertNotNull(image);
        assertEquals("网络图片", image.getTitle());
        assertEquals("https://example.com/image.jpg", image.getPath());
        assertEquals("image.jpg", image.getImageName());
        assertEquals(".jpg", image.getExtension());
        assertEquals(ImageLocationEnum.NETWORK, image.getLocation());
        assertEquals(ImageMarkEnum.ORIGINAL, image.getImageMarkType());
    }

    @Test
    @DisplayName("HTML 包裹的大图应解析为 LARGE_PICTURE 并保留原始标记")
    void analysisLargeImageMark() {
        String lineText = "<a data-fancybox title='大图' href='https://example.com/big.jpg' >![大图](https://example.com/big.jpg)</a>";
        MarkdownImage image = MarkdownUtils.analysisImageMark(mdFile(), lineText, 0);

        assertNotNull(image);
        assertEquals(ImageMarkEnum.LARGE_PICTURE, image.getImageMarkType());
        assertTrue(image.getOriginalMark().contains("<a"));
    }

    @Test
    @DisplayName("空标题图片标签应解析为空标题")
    void analysisImageMarkWithEmptyTitle() {
        String lineText = "![](https://example.com/no-title.jpg)";
        MarkdownImage image = MarkdownUtils.analysisImageMark(mdFile(), lineText, 0);

        assertNotNull(image);
        assertEquals("", image.getTitle().trim());
        assertEquals("https://example.com/no-title.jpg", image.getPath());
    }

    @Test
    @DisplayName("非法图片标签应返回 null")
    void analysisImageMarkWithInvalidFormat() {
        String lineText = "这不是一个有效的图片标签";
        MarkdownImage image = MarkdownUtils.analysisImageMark(mdFile(), lineText, 0);

        assertNull(image);
    }

    @Test
    @DisplayName("从网络路径解析图片名")
    void getImageNameFromNetworkPath() {
        String mark = "![测试](https://example.com/images/test.jpg)";
        assertEquals("test.jpg", MarkdownUtils.getImageName(mark));
    }

    @Test
    @DisplayName("从本地相对路径解析图片名")
    void getImageNameFromLocalPath() {
        String mark = "![测试](./imgs/example.png)";
        assertEquals("example.png", MarkdownUtils.getImageName(mark));
    }

    @Test
    @DisplayName("常见图片扩展名应被正确解析")
    void analysisImageMarkWithDifferentFormats() {
        String[] formats = {".png", ".jpg", ".jpeg", ".gif"};
        for (String format : formats) {
            String lineText = "![测试图片](https://example.com/test" + format + ")";
            MarkdownImage image = MarkdownUtils.analysisImageMark(mdFile(), lineText, 0);
            assertNotNull(image, "format=" + format);
            assertEquals(format, image.getExtension(), "format=" + format);
        }
    }

    @Test
    @DisplayName("未标识的大图/普通图应识别为 ORIGINAL，自定义标识识别为 CUSTOM")
    void analysisCustomImageMark() {
        String custom = "<a class='custom' title='自定义' href='https://example.com/custom.jpg' >![自定义](https://example.com/custom.jpg)</a>";
        MarkdownImage imageCustom = MarkdownUtils.analysisImageMark(mdFile(), custom, 0);
        assertNotNull(imageCustom);
        assertEquals(ImageMarkEnum.CUSTOM, imageCustom.getImageMarkType());
    }
}

