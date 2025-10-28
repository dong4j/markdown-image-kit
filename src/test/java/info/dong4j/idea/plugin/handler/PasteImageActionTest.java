package info.dong4j.idea.plugin.handler;

import info.dong4j.idea.plugin.util.ImageUtils;

import org.junit.Assert;
import org.junit.Test;

import java.awt.Image;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import lombok.extern.slf4j.Slf4j;

/**
 * PasteImageActionTest
 * <p>
 * 用于测试从系统剪贴板粘贴图片并保存为文件的功能。该测试类主要验证图片数据的获取、转换和写入文件的流程。
 * <p>
 * 测试方法通过获取系统剪贴板中的图片数据，将其转换为 BufferedImage 并保存为 PNG 格式的图片文件。
 *
 * @author dong4j
 * @version 1.0.0
 * @date 2019.03.17
 * @since 1.1.0
 */
@Slf4j
public class PasteImageActionTest {
    /**
     * 测试从系统剪贴板获取并保存图片的功能
     * <p>
     * 测试场景：验证当剪贴板中包含支持的图片格式时，能否正确获取并保存为 PNG 文件
     * 预期结果：应成功保存图片到指定路径
     * <p>
     * 注意：该测试需要系统剪贴板中存在图片数据，且支持 DataFlavor.imageFlavor 格式
     */
    @Test
    public void test() throws IOException, UnsupportedFlavorException {
        Clipboard clipboard = java.awt.Toolkit.getDefaultToolkit().getSystemClipboard();
        Transferable cc = clipboard.getContents(null);
        if (cc != null && cc.isDataFlavorSupported(DataFlavor.imageFlavor)) {
            Image image = (Image) cc.getTransferData(DataFlavor.imageFlavor);
            // 保存图片
            BufferedImage bufferedImage = ImageUtils.toBufferedImage(image);
            File imageFile = new File("/Users/dong4j/Develop/", "test.png");
            Assert.assertNotNull(bufferedImage);
            ImageIO.write(bufferedImage, "png", imageFile);
        }
    }

}