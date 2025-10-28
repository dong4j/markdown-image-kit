package info.dong4j.idea.plugin.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.awt.image.BufferedImage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * 图像工具类测试类
 * <p>
 * 用于验证 {@link ImageUtils} 工具类中相关方法的正确性，包括图像缩放、图像类型识别以及文件扩展名提取等功能的测试用例。
 *
 * @author 作者名
 * @version 1.0.0
 * @date 2025.10.24
 * @since 1.0.0
 */
public class ImageUtilsPureTest {

    /**
     * 测试 scaleImage 方法在边缘情况下的行为
     * <p>
     * 测试场景：输入为 null 或目标尺寸为零时
     * 预期结果：应返回 null
     * <p>
     * 该测试验证了当输入图像为 null 或目标宽度/高度为零时，scaleImage 方法能够正确返回 null，避免无效操作。
     */
    @Test
    @DisplayName("scaleImage null 与零尺寸返回 null")
    void scaleImageEdge() {
        assertNull(ImageUtils.scaleImage(null, 100, 100));
        BufferedImage img = new BufferedImage(10, 10, BufferedImage.TYPE_INT_ARGB);
        assertNull(ImageUtils.scaleImage(img, 0, 10));
        assertNull(ImageUtils.scaleImage(img, 10, 0));
    }

    /**
     * 测试获取图片类型方法对常见扩展名的映射是否正确
     * <p>
     * 测试场景：验证不同图片扩展名是否能正确映射到对应的 MIME 类型
     * 预期结果：常见扩展名如 gif、PNG、jpeg、jpg 应返回正确的 MIME 类型，bmp 扩展名应返回空字符串
     */
    @Test
    @DisplayName("getImageType 常见扩展名映射正确")
    void imageTypeMapping() {
        assertEquals("image/gif", ImageUtils.getImageType("a.gif"));
        assertEquals("image/png", ImageUtils.getImageType("a.PNG"));
        assertEquals("image/jpeg", ImageUtils.getImageType("a.jpeg"));
        assertEquals("image/jpeg", ImageUtils.getImageType("a.jpg"));
        assertEquals("", ImageUtils.getImageType("a.bmp"));
    }

    /**
     * 测试获取文件扩展名功能
     * <p>
     * 测试场景：输入文件名为 "a.png"，期望返回点号后缀
     * 预期结果：应返回 ".png"
     */
    @Test
    @DisplayName("getFileExtension 返回点号后缀")
    void extension() {
        assertEquals(".png", ImageUtils.getFileExtension("a.png"));
    }
}


