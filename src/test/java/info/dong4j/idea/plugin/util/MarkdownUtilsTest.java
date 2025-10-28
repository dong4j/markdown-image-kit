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

/**
 * Markdown 工具类测试类
 * <p>
 * 该类用于测试 MarkdownUtils 工具类中与图片解析相关的功能，包括对网络图片、大图、空标题图片、非法图片标签等的解析能力，以及从不同路径中提取图片名称的功能。
 * <p>
 * 提供了多个测试用例，验证 Markdown 图片标记的解析逻辑是否符合预期，包括对 HTML 包裹图片、自定义标记图片等复杂情况的支持。
 *
 * @author 作者名
 * @version 1.0.0
 * @date 2025.10.24
 * @since 1.0.0
 */
public class MarkdownUtilsTest {

    /**
     * 创建并返回一个测试用的Markdown虚拟文件对象
     * <p>
     * 用于测试目的，生成一个名为"test.md"的空虚拟文件
     *
     * @return 测试用的Markdown虚拟文件对象
     */
    private LightVirtualFile mdFile() {
        return new LightVirtualFile("test.md", "");
    }

    /**
     * 测试网络图片标签的解析功能
     * <p>
     * 测试场景：解析包含网络图片标记的文本内容
     * 预期结果：应正确解析出图片标题、路径、文件名、扩展名、位置类型及标记类型
     * <p>
     * 该测试需要 MarkdownUtils 类中的 analysisImageMark 方法正常工作
     */
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

    /**
     * 测试 HTML 包裹的大图解析功能
     * <p>
     * 测试场景：当输入内容为包含 HTML 标签的大图时
     * 预期结果：应解析为 LARGE_PICTURE 类型，并保留原始 HTML 标记
     * <p>
     * 该测试验证 Markdown 工具类对 HTML 包裹大图的识别能力，确保能够正确识别并保留原始 HTML 结构
     */
    @Test
    @DisplayName("HTML 包裹的大图应解析为 LARGE_PICTURE 并保留原始标记")
    void analysisLargeImageMark() {
        String lineText = "<a data-fancybox title='大图' href='https://example.com/big.jpg' >![大图](https://example.com/big.jpg)</a>";
        MarkdownImage image = MarkdownUtils.analysisImageMark(mdFile(), lineText, 0);

        assertNotNull(image);
        assertEquals(ImageMarkEnum.LARGE_PICTURE, image.getImageMarkType());
        assertTrue(image.getOriginalMark().contains("<a"));
    }

    /**
     * 测试解析图片标签功能
     * <p>
     * 测试场景：当图片标签中标题为空时
     * 预期结果：应解析出空标题和正确的图片路径
     * <p>
     * 该测试验证Markdown解析器在遇到没有标题的图片标签时，能够正确处理并返回空字符串作为标题
     */
    @Test
    @DisplayName("空标题图片标签应解析为空标题")
    void analysisImageMarkWithEmptyTitle() {
        String lineText = "![](https://example.com/no-title.jpg)";
        MarkdownImage image = MarkdownUtils.analysisImageMark(mdFile(), lineText, 0);

        assertNotNull(image);
        assertEquals("", image.getTitle().trim());
        assertEquals("https://example.com/no-title.jpg", image.getPath());
    }

    /**
     * 测试非法图片标签的解析情况
     * <p>
     * 测试场景：传入一个格式不正确的字符串作为图片标签
     * 预期结果：应返回 null，表示解析失败
     * <p>
     * 该测试用于验证 {@link MarkdownUtils#analysisImageMark} 方法在遇到无效格式的图片标签时的处理逻辑
     */
    @Test
    @DisplayName("非法图片标签应返回 null")
    void analysisImageMarkWithInvalidFormat() {
        String lineText = "这不是一个有效的图片标签";
        MarkdownImage image = MarkdownUtils.analysisImageMark(mdFile(), lineText, 0);

        assertNull(image);
    }

    /**
     * 测试从网络路径中解析图片名的功能
     * <p>
     * 测试场景：输入包含网络路径的Markdown图片标记
     * 预期结果：应正确提取出图片文件名 "test.jpg"
     * <p>
     * 注意：测试使用了 {@link MarkdownUtils#getImageName(String)} 方法进行解析
     */
    @Test
    @DisplayName("从网络路径解析图片名")
    void getImageNameFromNetworkPath() {
        String mark = "![测试](https://example.com/images/test.jpg)";
        assertEquals("test.jpg", MarkdownUtils.getImageName(mark));
    }

    /**
     * 测试从本地相对路径中解析图片名的功能
     * <p>
     * 测试场景：输入一个包含本地相对路径的Markdown图片标记
     * 预期结果：应正确提取出图片文件名"example.png"
     * <p>
     * 注意：测试使用了MarkdownUtils.getImageName方法进行解析
     */
    @Test
    @DisplayName("从本地相对路径解析图片名")
    void getImageNameFromLocalPath() {
        String mark = "![测试](./imgs/example.png)";
        assertEquals("example.png", MarkdownUtils.getImageName(mark));
    }

    /**
     * 测试解析不同常见图片扩展名的功能
     * <p>
     * 测试场景：传入包含不同图片扩展名的Markdown图片标记
     * 预期结果：应正确解析并返回对应的图片扩展名
     * <p>
     * 测试覆盖的扩展名包括：.png、.jpg、.jpeg、.gif
     * <p>
     * 注意：测试中使用了 {@link MarkdownUtils#analysisImageMark} 方法进行图片解析
     */
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

    /**
     * 测试分析自定义图片标记的功能
     * <p>
     * 测试场景：输入包含自定义图片标记的 Markdown 内容
     * 预期结果：应正确识别为 CUSTOM 类型的图片标记
     * <p>
     * 该测试验证 MarkdownUtils.analysisImageMark 方法是否能够正确解析带有自定义类名的图片标记。
     * <p>
     * 注意：测试中使用了 MarkdownUtils 工具类的 mdFile() 方法生成 Markdown 文件内容，具体实现需参考相关代码。
     */
    @Test
    @DisplayName("未标识的大图/普通图应识别为 ORIGINAL，自定义标识识别为 CUSTOM")
    void analysisCustomImageMark() {
        String custom = "<a class='custom' title='自定义' href='https://example.com/custom.jpg' >![自定义](https://example.com/custom.jpg)</a>";
        MarkdownImage imageCustom = MarkdownUtils.analysisImageMark(mdFile(), custom, 0);
        assertNotNull(imageCustom);
        assertEquals(ImageMarkEnum.CUSTOM, imageCustom.getImageMarkType());
    }
}

