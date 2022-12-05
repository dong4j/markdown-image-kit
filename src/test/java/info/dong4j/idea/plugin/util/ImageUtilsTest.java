package info.dong4j.idea.plugin.util;

import com.intellij.xml.actions.xmlbeans.FileUtils;

import net.coobird.thumbnailator.Thumbnails;

import org.junit.Test;

import sun.awt.image.MultiResolutionCachedImage;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.imageio.ImageIO;

import lombok.extern.slf4j.Slf4j;

/**
 * <p>Description: ${description}</p>
 *
 * @author dong4j
 * @version 1.0.0
 * @email "mailto:dong4j@gmail.com"
 * @date 2019.03.17 16:45
 * @since 1.1.0
 */
@Slf4j
public class ImageUtilsTest {

    /**
     * Test.
     *
     * @since 1.1.0
     */
    @Test
    public void test() {
        getClipboardImage();

    }

    /**
     * Test 2.
     *
     * @since 1.1.0
     */
    @Test
    public void test2() {
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        if (clipboard.isDataFlavorAvailable(DataFlavor.imageFlavor)) {
            try {
                System.out.println(clipboard.getData(DataFlavor.imageFlavor));
                BufferedImage im = getImage((Image) clipboard.getData(DataFlavor.imageFlavor));
                System.out.println(im);
            } catch (UnsupportedFlavorException | IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Gets clipboard image.
     *
     * @return the clipboard image
     * @since 1.1.0
     */
    public static BufferedImage getClipboardImage() {
        Transferable trans = Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null);
        BufferedImage image = null;
        try {
            if (null != trans && trans.isDataFlavorSupported(DataFlavor.imageFlavor)) {
                Object obj22 = trans.getTransferData(DataFlavor.imageFlavor);
                if (obj22 instanceof BufferedImage) {
                    image = (BufferedImage) obj22;
                } else if (obj22 instanceof sun.awt.image.MultiResolutionCachedImage) {//兼容mac os
                    sun.awt.image.MultiResolutionCachedImage cachedImage = (sun.awt.image.MultiResolutionCachedImage) obj22;

                    List<Image> images = cachedImage.getResolutionVariants();
                    sun.awt.image.ToolkitImage toolkitImage =
                        (sun.awt.image.ToolkitImage) cachedImage.getScaledInstance(cachedImage.getWidth(null),
                                                                                   cachedImage.getHeight(null), Image.SCALE_SMOOTH);
                    if (null == toolkitImage) {
                        return null;
                    }

                    java.awt.image.FilteredImageSource filteredImageSource = (java.awt.image.FilteredImageSource) toolkitImage.getSource();
                    if (null == filteredImageSource) {
                        return null;
                    }

                    // sun.awt.image.OffScreenImageSource imageSource = (sun.awt.image.OffScreenImageSource) ReflectHWUtils
                    // .getObjectValue(filteredImageSource, "src");
                    // image = (BufferedImage) ReflectHWUtils.getObjectValue(imageSource, "image");
                    //					System.out.println(imageSource);
                }
            }
        } catch (SecurityException | IllegalArgumentException | UnsupportedFlavorException | IOException e) {
            e.printStackTrace();
        }
        return image;
    }

    /***
     * 把系统剪切板中的图片黏贴到swing的Label控件中
     * @since 1.1.0
     */
    public void pasteClipboardImageAction() {
        BufferedImage bufferedimage = getClipboardImage();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {//把粘贴过来的图片转为为二进制(字节数组)
            ImageIO.write(Objects.requireNonNull(bufferedimage), "png", baos);
            byte[] a = baos.toByteArray();
            log.info("粘贴的二维码大小:\t" + a.length);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Gets image.
     *
     * @param image the image
     * @return the image
     * @since 1.1.0
     */
    public static BufferedImage getImage(Image image) {
        if (image instanceof BufferedImage) {
            return (BufferedImage) image;
        }
        Lock lock = new ReentrantLock();
        Condition size = lock.newCondition(), data = lock.newCondition();
        ImageObserver o = (img, infoflags, x, y, width, height) -> {
            lock.lock();
            try {
                if ((infoflags & ImageObserver.ALLBITS) != 0) {
                    size.signal();
                    data.signal();
                    return false;
                }
                if ((infoflags & (ImageObserver.WIDTH | ImageObserver.HEIGHT)) != 0) {
                    size.signal();
                }
                return true;
            } finally {
                lock.unlock();
            }
        };
        BufferedImage bi;
        lock.lock();
        try {
            int width, height;
            while ((width = image.getWidth(o)) < 0 || (height = image.getHeight(o)) < 0) {
                size.awaitUninterruptibly();
            }
            bi = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = bi.createGraphics();
            try {
                g.setBackground(new Color(0, true));
                g.clearRect(0, 0, width, height);
                while (!g.drawImage(image, 0, 0, o)) {
                    data.awaitUninterruptibly();
                }
            } finally {
                g.dispose();
            }
        } finally {
            lock.unlock();
        }
        return bi;
    }


    /**
     * Test 4
     *
     * @since 1.1.0
     */
    @Test
    public void test4() {
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        if (clipboard.isDataFlavorAvailable(DataFlavor.imageFlavor)) {
            try {
                System.out.println(clipboard.getData(DataFlavor.imageFlavor));
                setImageClipboard((Image) clipboard.getData(DataFlavor.imageFlavor));
            } catch (UnsupportedFlavorException | IOException e) {
                e.printStackTrace();
            }
        }

        setImageClipboard(ImageUtils.loadImageFromFile(new File("/Users/dong4j/Downloads/我可要开始皮了.png")));
    }

    /**
     * 设置系统剪切板内容[内容为图片型]
     *
     * @param image the image
     * @since 1.1.0
     */
    public static void setImageClipboard(Image image) {

        ImageSelection imgSel = new ImageSelection(image);
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(imgSel, null);

        Image aaa = getImageClipboard();
        log.info("{}", aaa);
    }

    /**
     * The type Image selection.
     *
     * @author dong4j
     * @version 1.0.0
     * @email "mailto:dong4j@gmail.com"
     * @date 2021.02.14 22:44
     * @since 1.1.0
     */
    public static class ImageSelection implements Transferable {
        /** Image */
        private final Image image;

        /**
         * Instantiates a new Image selection.
         *
         * @param image the image
         * @since 1.1.0
         */
        public ImageSelection(Image image) {this.image = image;}

        /**
         * Get transfer data flavors
         *
         * @return the data flavor [ ]
         * @since 1.1.0
         */
        @Override
        public DataFlavor[] getTransferDataFlavors() {
            return new DataFlavor[] {DataFlavor.imageFlavor};
        }

        /**
         * Is data flavor supported
         *
         * @param flavor flavor
         * @return the boolean
         * @since 1.1.0
         */
        @Override
        public boolean isDataFlavorSupported(DataFlavor flavor) {
            return DataFlavor.imageFlavor.equals(flavor);
        }

        /**
         * Gets transfer data *
         *
         * @param flavor flavor
         * @return the transfer data
         * @throws UnsupportedFlavorException unsupported flavor exception
         * @throws IOException                io exception
         * @since 1.1.0
         */
        @Override
        public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
            if (!DataFlavor.imageFlavor.equals(flavor)) {
                throw new UnsupportedFlavorException(flavor);
            }
            return this.image;
        }
    }

    /**
     * 获取系统剪切板内容[剪切板中内容为图片型]
     *
     * @return the image clipboard
     * @since 1.1.0
     */
    public static Image getImageClipboard() {
        Transferable t = Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null);
        try {
            if (null != t && t.isDataFlavorSupported(DataFlavor.imageFlavor)) {
                Image image = (Image) t.getTransferData(DataFlavor.imageFlavor);
                return image;
            }
        } catch (UnsupportedFlavorException | IOException e) {
            //System.out.println("Error tip: "+e.getMessage());
        }
        return null;
    }

    /**
     * Test 7
     *
     * @since 1.1.0
     */
    @Test
    public void test7() {
        //创建剪切板对象
        Clipboard sysboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        //获得剪切板的内容,如果没有内容,就返回null
        Transferable cliptf = sysboard.getContents(null);
        if (cliptf != null) {
            //如果剪切板的内容是文件
            if (cliptf.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                String path = "";
                try {
                    //获得数据
                    Object o = cliptf.getTransferData(DataFlavor.javaFileListFlavor);
                    //tostring,转为字符串
                    path = o.toString();
                    System.out.println("path==" + o.toString());
                } catch (UnsupportedFlavorException | IOException e) {
                    e.printStackTrace();
                }
            }

            //顺便把剪切板里的文字和图片也提取出来
            //检查文本内容是否为文本内容
            if (cliptf.isDataFlavorSupported(DataFlavor.stringFlavor)) {
                String result = "";
                try {
                    result = (String) cliptf.getTransferData(DataFlavor.stringFlavor);
                    System.out.println("文本内容==" + result);
                } catch (UnsupportedFlavorException | IOException e) {
                    e.printStackTrace();
                }
            }
            //检查文本内容是否为文本内容
            if (cliptf.isDataFlavorSupported(DataFlavor.imageFlavor)) {
                Image image;
                try {
                    image = (MultiResolutionCachedImage) cliptf.getTransferData(DataFlavor.imageFlavor);
                    sun.awt.image.MultiResolutionCachedImage cachedImage = (sun.awt.image.MultiResolutionCachedImage) image;
                    List<Image> images = cachedImage.getResolutionVariants();

                    setImageClipboard(images.get(0));
                } catch (UnsupportedFlavorException | IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Test 10
     *
     * @since 1.1.0
     */
    @Test
    public void test10() {
        ImageUtils.compress(new File("/Users/dong4j/Downloads/c.gif"),
                            new File("/Users/dong4j/Downloads/d.gif"), 60);

    }

    /**
     * Test 11
     *
     * @since 1.1.0
     */
    @Test
    public void test11() {
        log.info("{}", ImageUtils.getFileExtension("aaa.png"));
    }

    /**
     * Test 12
     *
     * @throws IOException io exception
     * @since 1.1.0
     */
    @Test
    public void test12() throws IOException {

        Thumbnails.of("/Users/dong4j/Downloads/wade-meng-LgCj9qcrfhI.jpg").size(1280, 1024).outputFormat("png").toFile("/Users/dong4j" +
                                                                                                                       "/Develop/test.png");
    }

    /**
     * Test 13
     *
     * @throws IOException io exception
     * @since 1.1.0
     */
    @Test
    public void test13() throws IOException {
        // ImageUtils.compress(new File("/Users/dong4j/Downloads/wade-meng-LgCj9qcrfhI.jpg"),
        //                     new File("/Users/dong4j/Develop/test.jpg"), 60);

        BufferedImage bufferedImage = Thumbnails.of(new File("/Users/dong4j/Downloads/wade-meng-LgCj9qcrfhI.jpg"))
            .scale(1f)
            .outputQuality(0.6)
            .asBufferedImage();

        if (bufferedImage != null) {
            bufferedImage = ImageUtils.toBufferedImage(ImageUtils.makeRoundedCorner(bufferedImage, 80));

            Thumbnails.of(bufferedImage)
                .size(bufferedImage.getWidth(), bufferedImage.getHeight())
                .outputFormat("jpg")
                .toFile("/Users/dong4j/Develop/test2.jpg");

            // ImageIO.write(compressedImage, "png", new File("/Users/dong4j/Develop/test.png"));
        }
    }

    /**
     * Test 14
     *
     * @throws IOException io exception
     * @since 1.1.0
     */
    @Test
    public void test14() throws IOException {
        File in = new File("/Users/dong4j/Downloads/2019-03-25 23.18.31.gif");
        File out = new File("/Users/dong4j/Develop/test.gif");
        FileUtils.copyFile(in, out);
        BufferedImage bufferedImage = ImageIO.read(out);
        File out1 = new File("/Users/dong4j/Develop/test1.gif");
        ImageIO.write(bufferedImage, "gif", out1);
        // Image image = ImageIO.read(out);
    }

    /**
     * Test 15
     *
     * @throws IOException io exception
     * @since 1.1.0
     */
    @Test
    public void test15() throws IOException {
        File in = new File("/Users/dong4j/Downloads/xu.png");
        BufferedImage image = ImageIO.read(in);
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        ImageIO.write(image, "png", os);
        InputStream is = new ByteArrayInputStream(os.toByteArray());
        FileUtils.saveStreamContentAsFile("/Users/dong4j/Develop/test.png", is);
    }

    /**
     * Test 16
     *
     * @throws Exception exception
     * @since 1.1.0
     */
    @Test
    public void test16() throws Exception {
        File temp = File.createTempFile("testrunoobtmp", ".txt");
        System.out.println("文件路径: " + temp.getAbsolutePath());
        temp.deleteOnExit();
        BufferedWriter out = new BufferedWriter(new FileWriter(temp));
        out.write("aString");
        System.out.println("临时文件已创建:");
        out.close();
    }

    /**
     * Test 17
     *
     * @since 1.1.0
     */
    @Test
    public void test17() {
        // 获取不同系统的换行符
        String lineSeparator = System.lineSeparator();
        System.out.println(lineSeparator);
    }

    /**
     * File copy test
     *
     * @throws IOException io exception
     * @since 1.1.0
     */
    @Test
    public void fileCopyTest() throws IOException {
        File file1 = new File("/Users/dong4j/Downloads/xu.png");
        File file2 = new File("/Users/dong4j/Downloads/xu1.png");
        FileUtils.copyFile(file1, file2);
    }
}

