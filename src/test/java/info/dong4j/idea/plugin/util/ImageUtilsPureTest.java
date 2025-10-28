package info.dong4j.idea.plugin.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.awt.image.BufferedImage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class ImageUtilsPureTest {

    @Test
    @DisplayName("scaleImage null 与零尺寸返回 null")
    void scaleImageEdge() {
        assertNull(ImageUtils.scaleImage(null, 100, 100));
        BufferedImage img = new BufferedImage(10, 10, BufferedImage.TYPE_INT_ARGB);
        assertNull(ImageUtils.scaleImage(img, 0, 10));
        assertNull(ImageUtils.scaleImage(img, 10, 0));
    }

    @Test
    @DisplayName("getImageType 常见扩展名映射正确")
    void imageTypeMapping() {
        assertEquals("image/gif", ImageUtils.getImageType("a.gif"));
        assertEquals("image/png", ImageUtils.getImageType("a.PNG"));
        assertEquals("image/jpeg", ImageUtils.getImageType("a.jpeg"));
        assertEquals("image/jpeg", ImageUtils.getImageType("a.jpg"));
        assertEquals("", ImageUtils.getImageType("a.bmp"));
    }

    @Test
    @DisplayName("getFileExtension 返回点号后缀")
    void extension() {
        assertEquals(".png", ImageUtils.getFileExtension("a.png"));
    }
}


