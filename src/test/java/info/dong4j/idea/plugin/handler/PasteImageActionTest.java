package info.dong4j.idea.plugin.handler;

import info.dong4j.idea.plugin.util.ImageUtils;

import org.junit.Test;

import java.awt.Image;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.image.BufferedImage;
import java.io.*;

import javax.imageio.ImageIO;

import lombok.extern.slf4j.Slf4j;

/**
 * <p>Company: 科大讯飞股份有限公司-四川分公司</p>
 * <p>Description: ${description}</p>
 *
 * @author dong4j
 * @date 2019-03-17 12:46
 * @email sjdong3@iflytek.com
 */
@Slf4j
public class PasteImageActionTest {
    @Test
    public void test() throws IOException, UnsupportedFlavorException {
        Clipboard clipboard = java.awt.Toolkit.getDefaultToolkit().getSystemClipboard();
        Transferable cc = clipboard.getContents(null);
        if (cc != null && cc.isDataFlavorSupported(DataFlavor.imageFlavor)) {
            Image image = (Image) cc.getTransferData(DataFlavor.imageFlavor);
            // 保存图片
            BufferedImage bufferedImage = ImageUtils.toBufferedImage(image);
            File imageFile = new File("/Users/dong4j/Develop/",  "test.png");
            ImageIO.write(bufferedImage, "png", imageFile);
        }
    }

}