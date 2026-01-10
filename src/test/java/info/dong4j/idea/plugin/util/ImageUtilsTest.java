package info.dong4j.idea.plugin.util;

import org.junit.Test;

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
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.imageio.ImageIO;

import lombok.extern.slf4j.Slf4j;

/**
 * 图片工具类测试
 * <p>
 * 该类主要用于测试图片相关的工具方法，包括从系统剪切板获取图片、将图片粘贴到Label控件、设置剪切板内容、获取文件扩展名等功能。
 * <p>
 * 同时包含多个测试方法，用于验证不同场景下的图片处理逻辑，如处理不同数据格式、临时文件创建、系统行分隔符获取等。
 *
 * @author dong4j
 * @version 1.0.0
 * @date 2025.10.24
 * @since 1.1.0
 */
@SuppressWarnings("D")
@Slf4j
public class ImageUtilsTest {
    /**
     * 测试获取剪贴板图片功能
     * <p>
     * 测试场景：调用获取剪贴板图片方法
     * 预期结果：应正确获取剪贴板中的图片数据
     */
    @Test
    public void test() {
        getClipboardImage();

    }

    /**
     * 测试从系统剪贴板获取图像数据的功能
     * <p>
     * 测试场景：检查系统剪贴板是否包含图像数据格式
     * 预期结果：若存在图像数据，应正确获取并转换为 BufferedImage 对象
     * <p>
     * 注意：该测试依赖于系统剪贴板中实际存在图像数据，可能需要手动复制图像到剪贴板以确保测试通过
     */
    @Test
    public void test2() {
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        if (clipboard.isDataFlavorAvailable(DataFlavor.imageFlavor)) {
            try {
                System.out.println(clipboard.getData(DataFlavor.imageFlavor));
                BufferedImage im = getImage((Image) clipboard.getData(DataFlavor.imageFlavor));
                System.out.println(im);
            } catch (UnsupportedFlavorException | IOException ignored) {
            }
        }
    }

    /**
     * 获取剪贴板中的图片
     * <p>
     * 从系统剪贴板中获取图片数据，并返回为 BufferedImage 对象。支持常见的图片格式，并兼容 Mac OS 的图像处理方式。
     *
     * @return 剪贴板中的图片，若剪贴板中没有图片或发生异常则返回 null
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
                } else if (obj22 instanceof Image) {//兼容mac os
                    // 简化处理，直接转换为 BufferedImage
                    image = getImage((Image) obj22);
                }
            }
        } catch (SecurityException | IllegalArgumentException | UnsupportedFlavorException | IOException ignored) {
        }
        return image;
    }

    /**
     * 将系统剪切板中的图片粘贴到Swing的Label控件中
     * <p>
     * 从系统剪切板获取图片，将其转换为二进制数据并记录日志
     *
     * @since 1.1.0
     */
    public void pasteClipboardImageAction() {
        BufferedImage bufferedimage = getClipboardImage();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {//把粘贴过来的图片转为为二进制(字节数组)
            ImageIO.write(Objects.requireNonNull(bufferedimage), "png", baos);
            byte[] a = baos.toByteArray();
            log.debug("粘贴的二维码大小:\t{}", a.length);
        } catch (IOException ignored) {
        }
    }

    /**
     * 获取 BufferedImage 图像
     * <p>
     * 将传入的 Image 对象转换为 BufferedImage 类型。如果传入的已经是 BufferedImage，
     * 则直接返回；否则，通过异步加载图像数据并创建新的 BufferedImage。
     *
     * @param image 要转换的 Image 对象
     * @return 转换后的 BufferedImage 对象
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
     * 测试从系统剪贴板获取图像数据并设置到剪贴板图像的功能
     * <p>
     * 测试场景：检查系统剪贴板是否包含图像数据，若包含则读取并设置到剪贴板图像
     * 预期结果：成功读取剪贴板中的图像数据并调用 setImageClipboard 方法
     * <p>
     * 注意：该测试依赖于系统剪贴板中存在图像数据，若无图像数据则不会执行 setImageClipboard 方法
     */
    @Test
    public void test4() {
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        if (clipboard.isDataFlavorAvailable(DataFlavor.imageFlavor)) {
            try {
                System.out.println(clipboard.getData(DataFlavor.imageFlavor));
                setImageClipboard((Image) clipboard.getData(DataFlavor.imageFlavor));
            } catch (UnsupportedFlavorException | IOException ignored) {
            }
        }

        // setImageClipboard(ImageUtils.loadImageFromFile(new File("/Users/dong4j/Downloads/我可要开始皮了.png")));
    }

    /**
     * 设置系统剪切板内容，内容为图片类型
     * <p>
     * 该方法将指定的图片设置为系统剪切板的内容，并记录日志以验证设置结果
     *
     * @param image 要设置到剪切板的图片对象
     * @since 1.1.0
     */
    public static void setImageClipboard(Image image) {

        ImageSelection imgSel = new ImageSelection(image);
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(imgSel, null);

        Image aaa = getImageClipboard();
        log.debug("{}", aaa);
    }

    /**
     * 图像选择类
     * <p>
     * 用于封装图像数据并实现数据传输功能，主要支持图像格式的拖放和复制操作。
     * 该类实现了 Transferable 接口，用于在 Java 中进行数据的拖放操作。
     *
     * @author dong4j
     * @version 1.0.0
     * @date 2021.02.14
     * @since 1.1.0
     */
    public static class ImageSelection implements Transferable {
        /** 图像对象，用于表示和操作图像数据 */
        private final Image image;

        /**
         * 根据指定的图片实例初始化一个ImageSelection对象
         * <p>
         * 该构造方法用于创建ImageSelection实例，并将传入的图片对象赋值给内部属性
         *
         * @param image 要绑定的图片对象
         * @since 1.1.0
         */
        public ImageSelection(Image image) {this.image = image;}

        /**
         * 获取传输数据格式
         * <p>
         * 返回支持的数据格式数组，当前仅支持图像格式
         *
         * @return 数据格式数组
         * @since 1.1.0
         */
        @Override
        public DataFlavor[] getTransferDataFlavors() {
            return new DataFlavor[] {DataFlavor.imageFlavor};
        }

        /**
         * 判断是否支持指定的数据格式
         * <p>
         * 检查传入的数据格式是否为图像格式
         *
         * @param flavor 要检查的数据格式
         * @return 如果支持图像格式则返回 true，否则返回 false
         * @since 1.1.0
         */
        @Override
        public boolean isDataFlavorSupported(DataFlavor flavor) {
            return DataFlavor.imageFlavor.equals(flavor);
        }

        /**
         * 获取传输数据
         * <p>
         * 根据指定的DataFlavor获取对应的数据，若不支持该类型则抛出异常
         *
         * @param flavor 数据类型
         * @return 传输数据对象
         * @throws UnsupportedFlavorException 不支持的DataFlavor类型
         * @since 1.1.0
         */
        @Override
        public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException {
            if (!DataFlavor.imageFlavor.equals(flavor)) {
                throw new UnsupportedFlavorException(flavor);
            }
            return this.image;
        }
    }

    /**
     * 获取系统剪切板中的图片内容
     * <p>
     * 从系统剪切板中获取图片类型的数据，并返回对应的Image对象。如果剪切板中不包含图片数据或发生异常，则返回null。
     *
     * @return 系统剪切板中的图片对象，若不存在或获取失败则返回null
     * @since 1.1.0
     */
    public static Image getImageClipboard() {
        Transferable t = Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null);
        try {
            if (null != t && t.isDataFlavorSupported(DataFlavor.imageFlavor)) {
                return (Image) t.getTransferData(DataFlavor.imageFlavor);
            }
        } catch (UnsupportedFlavorException | IOException e) {
            //System.out.println("Error tip: "+e.getMessage());
        }
        return null;
    }

    /**
     * 测试剪切板内容提取功能
     * <p>
     * 测试目标：验证程序能否正确读取剪切板中的文件列表、文本内容和图片数据
     * 测试场景：剪切板中包含文件列表、文本和图片时
     * 预期结果：应能正确获取并处理剪切板中的不同数据类型
     * <p>
     * 注意：该测试需要剪切板中存在相应的数据内容才能正常执行
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
                    System.out.println("path==" + o);
                } catch (UnsupportedFlavorException | IOException ignored) {
                }
            }

            //顺便把剪切板里的文字和图片也提取出来
            //检查文本内容是否为文本内容
            if (cliptf.isDataFlavorSupported(DataFlavor.stringFlavor)) {
                String result = "";
                try {
                    result = (String) cliptf.getTransferData(DataFlavor.stringFlavor);
                    System.out.println("文本内容==" + result);
                } catch (UnsupportedFlavorException | IOException ignored) {
                }
            }
            //检查文本内容是否为文本内容
            if (cliptf.isDataFlavorSupported(DataFlavor.imageFlavor)) {
                Image image;
                try {
                    image = (Image) cliptf.getTransferData(DataFlavor.imageFlavor);
                    setImageClipboard(image);
                } catch (UnsupportedFlavorException | IOException ignored) {
                }
            }
        }
    }

    /**
     * 测试图片压缩功能
     * <p>
     * 测试场景：验证图片压缩方法是否能够正确执行，但当前测试中硬编码路径已被注释，需确保测试路径配置正确或使用 mock 数据进行测试
     * 预期结果：压缩操作应正常执行，且目标文件应存在
     */
    @Test
    public void test10() {
        // ImageUtils.compress(new File("/Users/dong4j/Downloads/c.gif"),
        //                     new File("/Users/dong4j/Downloads/d.gif"), 60);
        // 注释掉硬编码路径的测试
    }

    /**
     * 测试获取文件扩展名功能
     * <p>
     * 测试场景：输入一个带有扩展名的文件名 "aaa.png"
     * 预期结果：应正确返回文件扩展名 "png"
     * <p>
     * 该测试用于验证 ImageUtils.getFileExtension 方法在正常情况下的行为
     */
    @Test
    public void test11() {
        log.debug("{}", ImageUtils.getFileExtension("aaa.png"));
    }

    /**
     * 测试图片缩略图生成功能
     * <p>
     * 测试场景：验证图片缩略图生成逻辑是否正常，包括指定尺寸和输出格式
     * 预期结果：应正确生成指定尺寸和格式的缩略图文件
     * <p>
     * 注意：当前测试代码中硬编码路径已被注释，实际测试需配置有效图片路径和输出目录
     */
    @Test
    public void test12() {
        // Thumbnails.of("/Users/dong4j/Downloads/wade-meng-LgCj9qcrfhI.jpg").size(1280, 1024).outputFormat("png").toFile("/Users/dong4j" +
        //                                                                                                                "/Develop/test
        //                                                                                                                .png");
        // 注释掉硬编码路径的测试
    }

    /**
     * 测试 ImageUtils 工具类的压缩功能
     * <p>
     * 测试场景：注释掉硬编码路径的测试用例，用于验证压缩逻辑是否正确
     * 预期结果：压缩后的图像应保存到指定路径，且质量符合预期
     * <p>
     * 注意：该测试方法当前未执行实际操作，因为相关代码已被注释掉，需确保压缩逻辑在未注释状态下正常工作
     */
    @Test
    public void test13() {
        // 注释掉硬编码路径的测试
        // ImageUtils.compress(new File("/Users/dong4j/Downloads/wade-meng-LgCj9qcrfhI.jpg"),
        //                     new File("/Users/dong4j/Develop/test.jpg"), 60);

        // BufferedImage bufferedImage = Thumbnails.of(new File("/Users/dong4j/Downloads/wade-meng-LgCj9qcrfhI.jpg"))
        //     .scale(1f)
        //     .outputQuality(0.6)
        //     .asBufferedImage();

        // if (bufferedImage != null) {
        //     bufferedImage = ImageUtils.toBufferedImage(ImageUtils.makeRoundedCorner(bufferedImage, 80));

        //     Thumbnails.of(bufferedImage)
        //         .size(bufferedImage.getWidth(), bufferedImage.getHeight())
        //         .outputFormat("jpg")
        //         .toFile("/Users/dong4j/Develop/test2.jpg");

        //     // ImageIO.write(compressedImage, "png", new File("/Users/dong4j/Develop/test.png"));
        // }
    }

    /**
     * 测试文件复制与图像处理功能
     * <p>
     * 测试场景：验证文件复制及图像格式转换的流程
     * 预期结果：文件应能正确复制并转换为指定格式
     * <p>
     * 注意：当前测试代码中硬编码路径已被注释掉，需根据实际路径进行配置才能运行
     */
    @Test
    public void test14() {
        // 注释掉硬编码路径的测试
        // File in = new File("/Users/dong4j/Downloads/2019-03-25 23.18.31.gif");
        // File out = new File("/Users/dong4j/Develop/test.gif");
        // FileUtils.copyFile(in, out);
        // BufferedImage bufferedImage = ImageIO.read(out);
        // File out1 = new File("/Users/dong4j/Develop/test1.gif");
        // ImageIO.write(bufferedImage, "gif", out1);
        // Image image = ImageIO.read(out);
    }

    /**
     * 测试文件流处理功能
     * <p>
     * 测试场景：验证文件读取、转换及保存的流程是否正常
     * 预期结果：应成功完成文件的读取、转换和保存操作
     * <p>
     * 注意：当前测试中硬编码路径已被注释，实际测试需配置有效文件路径
     */
    @Test
    public void test15() {
        // 注释掉硬编码路径的测试
        // File in = new File("/Users/dong4j/Downloads/mik.webp");
        // BufferedImage image = ImageIO.read(in);
        // ByteArrayOutputStream os = new ByteArrayOutputStream();
        // ImageIO.write(image, "png", os);
        // InputStream is = new ByteArrayInputStream(os.toByteArray());
        // FileUtils.saveStreamContentAsFile("/Users/dong4j/Develop/test.png", is);
    }

    /**
     * 测试创建临时文件并写入内容的功能
     * <p>
     * 测试场景：创建一个临时文件并写入字符串 "aString"
     * 预期结果：临时文件应成功创建并写入内容，文件路径应正确输出
     * <p>
     * 注意：该测试会在程序结束时自动删除临时文件
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
     * 测试获取系统换行符功能
     * <p>
     * 测试场景：验证不同操作系统下获取的换行符是否正确
     * 预期结果：应返回当前系统对应的换行符（如 Windows 下为 "\r\n"，Linux 下为 "\n"）
     * <p>
     * 注意：该测试通过 System.lineSeparator() 方法获取换行符，无需特殊场景即可运行
     */
    @Test
    public void test17() {
        // 获取不同系统的换行符
        String lineSeparator = System.lineSeparator();
        System.out.println(lineSeparator);
    }

    /**
     * 测试文件复制功能
     * <p>
     * 测试场景：验证文件复制方法的正确性，注释掉了硬编码路径的测试用例，实际测试时需替换为有效路径
     * 预期结果：文件应能成功复制到目标路径
     */
    @Test
    public void fileCopyTest() {
        // 注释掉硬编码路径的测试
        // File file1 = new File("/Users/dong4j/Downloads/mik.webp");
        // File file2 = new File("/Users/dong4j/Downloads/xu1.png");
        // FileUtils.copyFile(file1, file2);
    }
}

