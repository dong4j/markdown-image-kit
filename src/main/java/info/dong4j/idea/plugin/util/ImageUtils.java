/*
 * MIT License
 *
 * Copyright (c) 2021 dong4j <dong4j@gmail.com>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package info.dong4j.idea.plugin.util;

import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.ui.JBColor;
import com.intellij.util.containers.hash.HashMap;
import com.intellij.util.ui.UIUtil;

import info.dong4j.idea.plugin.content.ImageContents;
import info.dong4j.idea.plugin.content.MikContents;
import info.dong4j.idea.plugin.enums.FileType;
import info.dong4j.idea.plugin.enums.ImageMediaType;

import net.coobird.thumbnailator.Thumbnails;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.geom.AffineTransform;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.image.FilteredImageSource;
import java.awt.image.ImageFilter;
import java.awt.image.ImageProducer;
import java.awt.image.RGBImageFilter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

import lombok.extern.slf4j.Slf4j;

/**
 * <p>Company: no company</p>
 * <p>Description: </p>
 *
 * @author dong4j
 * @version 0.0.1
 * @email "mailto:dong4j@gmail.com"
 * @date 2019.03.16 12:12
 * @since 0.0.1
 */
@Slf4j
public final class ImageUtils {

    /**
     * Gets image from clipboard.
     *
     * @return the image from clipboard
     * @since 0.0.1
     */
    @Nullable
    public static Image getImageFromClipboard() {
        Transferable transferable = Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null);
        if (transferable != null && transferable.isDataFlavorSupported(DataFlavor.imageFlavor)) {
            try {
                return (Image) transferable.getTransferData(DataFlavor.imageFlavor);
            } catch (IOException | UnsupportedFlavorException ignored) {
                // 如果 clipboard 没有图片, 不处理
            }
        }
        return null;
    }

    /**
     * Gets data from clipboard.
     *
     * @return the data from clipboard  map 中只有一对 kev-value
     * @since 0.0.1
     */
    @Nullable
    public static Map<DataFlavor, Object> getDataFromClipboard() {
        Map<DataFlavor, Object> data = new HashMap<>(1);
        Transferable transferable = Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null);
        if (transferable != null) {
            // 如果剪切板的内容是文件
            try {
                DataFlavor dataFlavor;
                if (transferable.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                    // List<File>
                    dataFlavor = DataFlavor.javaFileListFlavor;
                } else if (transferable.isDataFlavorSupported(DataFlavor.imageFlavor)) {
                    // Image
                    dataFlavor = DataFlavor.imageFlavor;
                } else {
                    return null;
                }
                Object object = transferable.getTransferData(dataFlavor);
                data.put(dataFlavor, object);
            } catch (IOException | UnsupportedFlavorException ignored) {
                // 如果 clipboard 没有文件, 不处理
            }
        }
        return data;
    }

    /**
     * 把文本设置到剪贴板（复制）
     *
     * @param text the text
     * @since 0.0.1
     */
    public static void setStringToClipboard(String text) {
        // 获取系统剪贴板
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        // 封装文本内容
        Transferable trans = new StringSelection(text);
        // 把文本内容设置到系统剪贴板
        clipboard.setContents(trans, null);
    }

    /**
     * Scale image buffered image.
     *
     * @param sourceImage the source image
     * @param newWidth    the new width
     * @param newHeight   the new height
     * @return the buffered image
     * @since 0.0.1
     */
    @Contract("null, _, _ -> null")
    public static BufferedImage scaleImage(BufferedImage sourceImage, int newWidth, int newHeight) {
        if (sourceImage == null) {
            return null;
        }

        if (newWidth == 0 || newHeight == 0) {
            return null;
        }

        AffineTransform at = AffineTransform.getScaleInstance((double) newWidth / sourceImage.getWidth(null),
                                                              (double) newHeight / sourceImage.getHeight(null));

        //  http://nickyguides.digital-digest.com/bilinear-vs-bicubic.htm
        AffineTransformOp op = new AffineTransformOp(at, AffineTransformOp.TYPE_BILINEAR);
        return op.filter(sourceImage, null);
    }

    /**
     * Save.
     *
     * @param image  the image
     * @param file   the file
     * @param format the format
     * @since 0.0.1
     */
    public static void save(BufferedImage image, File file, String format) {
        try {
            // ignore returned boolean
            ImageIO.write(image, format, file);
        } catch (Throwable e) {
            System.out.println("Write error for " + file.getPath() + ": " + e.getMessage());
        }
    }

    /**
     * Load image from file buffered image.
     *
     * @param cachedImageFile the cached image file
     * @return Could be <code>null</code> if the image could not be read from the file (because of whatever strange     reason).
     * @since 0.0.1
     */
    @Contract("null -> null")
    static BufferedImage loadImageFromFile(File cachedImageFile) {
        if (cachedImageFile == null || !cachedImageFile.isFile()) {
            return null;
        }

        try {
            for (int i = 0; i < 3; i++) {
                BufferedImage read;
                try {
                    read = ImageIO.read(cachedImageFile);
                } catch (IndexOutOfBoundsException e) {
                    System.err.print("*");
                    System.err.println("could not read" + cachedImageFile);
                    continue;
                }

                if (i > 0) {
                    System.err.println();
                }

                return read;
            }
        } catch (Throwable e) {
            System.err.println("deleting " + cachedImageFile);
            cachedImageFile.delete();
            return null;
        }

        return null;
    }

    /**
     * Load image from url buffered image.
     *
     * @param imageURL the image url
     * @return the buffered image
     * @since 0.0.1
     */
    @Nullable
    public static BufferedImage loadImageFromURL(String imageURL) {
        try {
            return toBufferedImage(new ImageIcon(new URL(imageURL)).getImage());
        } catch (MalformedURLException ignored) {
        }
        return null;
    }

    /**
     * To buffered image buffered image.
     *
     * @param src the src
     * @return the buffered image
     * @since 0.0.1
     */
    @Nullable
    public static BufferedImage toBufferedImage(Image src) {
        if (src instanceof BufferedImage) {
            return (BufferedImage) src;
        }

        int w = src.getWidth(null);
        int h = src.getHeight(null);
        if (w < 0 || h < 0) {
            return null;
        }

        // other options
        int type = BufferedImage.TYPE_INT_ARGB;
        BufferedImage dest = UIUtil.createImage(w, h, type);
        Graphics2D g2 = dest.createGraphics();
        g2.drawImage(src, 0, 0, null);
        g2.dispose();

        return dest;
    }

    /**
     * http://stackoverflow.com/questions/7603400/how-to-make-a-rounded-corner-image-in-java
     *
     * @param image        the image
     * @param cornerRadius the corner radius
     * @return the buffered image
     * @since 0.0.1
     */
    public static BufferedImage makeRoundedCorner(@NotNull BufferedImage image, int cornerRadius) {
        int w = image.getWidth();
        int h = image.getHeight();
        BufferedImage output = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = output.createGraphics();
        g2.setComposite(AlphaComposite.Src);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(JBColor.WHITE);
        g2.fill(new RoundRectangle2D.Float(0, 0, w, h, cornerRadius, cornerRadius));
        g2.setComposite(AlphaComposite.SrcAtop);
        g2.drawImage(image, 0, 0, null);

        g2.dispose();

        output.setRGB(3, 3, 123);
        return output;
    }


    /**
     * http://stackoverflow.com/questions/464825/converting-transparent-gif-png-to-jpeg-using-java
     *
     * @param image the image
     * @return the buffered image
     * @since 0.0.1
     */
    public static BufferedImage removeAlpha(@NotNull BufferedImage image) {
        BufferedImage bufferedImage = new BufferedImage(image.getWidth(null), image.getHeight(null), BufferedImage.TYPE_INT_RGB);
        Graphics g = bufferedImage.createGraphics();
        //Color.WHITE estes the background to white. You can use any other color
        g.drawImage(image, 0, 0, bufferedImage.getWidth(), bufferedImage.getHeight(), Color.WHITE, null);
        g.dispose();

        return bufferedImage;
    }


    /**
     * http://stackoverflow.com/questions/665406/how-to-make-a-color-transparent-in-a-bufferedimage-and-save-as-png
     *
     * @param image the image
     * @return the image
     * @since 0.0.1
     */
    public static Image whiteToTransparent(@NotNull BufferedImage image) {
        ImageFilter filter = new RGBImageFilter() {
            final int markerRGB = JBColor.WHITE.getRGB() | 0xFF000000;

            @Override
            public final int filterRGB(int x, int y, int rgb) {
                if ((rgb | 0xFF000000) == this.markerRGB) {
                    // Mark the alpha bits as zero - transparent
                    return 0x00FFFFFF & rgb;
                } else {
                    // nothing to do
                    return rgb;
                }
            }
        };

        ImageProducer ip = new FilteredImageSource(image.getSource(), filter);
        return Toolkit.getDefaultToolkit().createImage(ip);
    }

    /**
     * Is image file boolean.
     *
     * @param filePath the file path
     * @return the boolean
     * @since 0.0.1
     */
    public static boolean isImageFile(String filePath) {
        return isImageFile(new File(filePath));
    }

    /**
     * 通过 ImageReader 来解码这个 file 并返回一个 BufferedImage 对象
     * 如果找不到合适的 javax.imageio.ImageReader 则会返回 null, 则认为这不是图片文件
     *
     * @param file the file
     * @return the boolean
     * @since 0.0.1
     */
    public static boolean isImageFile(File file) {
        Image image = getImage(file);
        return image != null;
    }

    /**
     * Gets image *
     *
     * @param file file
     * @return the image
     * @since 1.6.2
     */
    private static Image getImage(File file) {
        try {
            return ImageIO.read(file);
        } catch (IOException ignored) {
        }
        return null;
    }

    /**
     * Compress.
     *
     * @param in      the in
     * @param out     the out
     * @param percent the percent
     * @since 0.0.1
     */
    public static void compress(File in, File out, int percent) {
        try {
            Thumbnails.of(in)
                .scale(1f)
                .outputQuality(percent * 1.0 / 100)
                .toFile(out);
        } catch (IOException e) {
            log.trace("", e);
        }
    }

    /**
     * Compress.
     *
     * @param in      the in
     * @param out     the out
     * @param percent the percent
     * @since 0.0.1
     */
    public static void compress(InputStream in, OutputStream out, int percent) {
        try {
            Thumbnails.of(in)
                .scale(1f)
                .outputQuality(percent * 1.0 / 100)
                .toOutputStream(out);
        } catch (IOException e) {
            log.trace("", e);
        }
    }

    /**
     * Compress.
     *
     * @param in      the in
     * @param out     the out
     * @param percent the percent
     * @since 0.0.1
     */
    public static void compress(InputStream in, File out, int percent) {
        try {
            Thumbnails.of(in)
                .scale(1f)
                .outputQuality(percent * 1.0 / 100)
                .toFile(out);
        } catch (IOException e) {
            log.trace("", e);
        }
    }

    /**
     * Description: 判断OSS服务文件上传时文件的contentType
     *
     * @param fileName the file name
     * @return String string
     * @since 0.0.1
     */
    public static String getImageType(String fileName) {
        String extension = getFileExtension(fileName);
        switch (extension.toLowerCase()) {
            case ".gif":
                return ImageMediaType.GIF.toString();
            case ".png":
                return ImageMediaType.PNG.toString();
            case ".jpg":
            case ".jpeg":
                return ImageMediaType.JPEG.toString();
            default:
                return "";
        }
    }

    /**
     * Get file suffix string.
     *
     * @param fileName the file name
     * @return the string
     * @since 0.0.1
     */
    @NotNull
    public static String getFileExtension(@NotNull String fileName) {
        return fileName.substring(fileName.lastIndexOf("."));
    }

    /**
     * Gets file type.
     *
     * @param is the is
     * @return the file type
     * @throws IOException the io exception
     * @since 0.0.1
     */
    @Nullable
    public static FileType getFileType(InputStream is) throws IOException {
        byte[] src = new byte[28];
        is.read(src, 0, 28);
        StringBuilder stringBuilder = new StringBuilder();
        for (byte b : src) {
            int v = b & 0xFF;
            String hv = Integer.toHexString(v).toUpperCase();
            if (hv.length() < 2) {
                stringBuilder.append(0);
            }
            stringBuilder.append(hv);
        }
        FileType[] fileTypes = FileType.values();
        for (FileType fileType : fileTypes) {
            if (stringBuilder.toString().startsWith(fileType.getValue())) {
                return fileType;
            }
        }
        return null;
    }

    /**
     * Build temp file file.
     *
     * @param fileName the file name
     * @return the file
     * @since 0.0.1
     */
    @NotNull
    @Contract("_ -> new")
    public static File buildTempFile(String fileName) {
        return new File(System.getProperty("java.io.tmpdir") + fileName);
    }

    /**
     * 递归遍历目录, 返回所有 Image 文件
     *
     * @param virtualFile the virtual file
     * @return the list
     * @since 0.0.1
     */
    public static List<VirtualFile> recursivelyImageFile(VirtualFile virtualFile) {
        List<VirtualFile> imageFiles = new ArrayList<>();
        /*
         * 递归遍历子文件
         *
         * @param root     the root         父文件
         * @param filter   the filter       过滤器
         * @param iterator the iterator     处理方式
         * @return the boolean
         */
        VfsUtilCore.iterateChildrenRecursively(virtualFile,
                                               file -> {
                                                   // todo-dong4j : (2019年03月15日 13:02) [从 .gitignore 中获取忽略的文件]
                                                   boolean allowAccept =
                                                       file.isDirectory() && !file.getName().equals(MikContents.NODE_MODULES_FILE);
                                                   if (allowAccept || ImageContents.IMAGE_TYPE_NAME.equals(file.getFileType().getName())) {
                                                       log.trace("accept = {}", file.getPath());
                                                       return true;
                                                   }
                                                   return false;
                                               },
                                               fileOrDir -> {
                                                   // todo-dong4j : (2019年03月15日 13:04) [处理 markdown 逻辑实现]
                                                   if (!fileOrDir.isDirectory()) {
                                                       log.trace("processFile = {}", fileOrDir.getName());
                                                       imageFiles.add(fileOrDir);
                                                   }
                                                   return true;
                                               });
        return imageFiles;
    }

    /**
     * Is valid for file boolean.
     *
     * @param file the file
     * @return the boolean
     * @since 0.0.1
     */
    public static boolean isValidForFile(PsiFile file) {
        if (file == null) {
            return false;
        }
        if (!isImageFile(file)) {
            return false;
        }
        // 不可写时按钮不可用
        return file.isWritable();
    }

    /**
     * Is image file boolean.
     *
     * @param file the file
     * @return the boolean
     * @since 0.0.1
     */
    private static boolean isImageFile(PsiFile file) {
        return ImageContents.IMAGE_TYPE_NAME.equals(file.getFileType().getName());
    }

    /**
     * Is image file boolean.
     *
     * @param file the file
     * @return the boolean
     * @since 0.0.1
     */
    public static boolean isImageFile(VirtualFile file) {
        return ImageContents.IMAGE_TYPE_NAME.equals(file.getFileType().getName());
    }

    /**
     * Watermark from text
     *
     * @param srcImg   src img
     * @param fileName file name
     * @param text     text
     * @return the file
     * @since y.y.y
     */
    public static File watermarkFromText(Image srcImg, String fileName, String text) {
        BufferedImage bufImg = null;
        if (srcImg != null) {
            int srcImgWidth = srcImg.getWidth(null);
            int srcImgHeight = srcImg.getHeight(null);
            // 加水印
            bufImg = new BufferedImage(srcImgWidth,
                                       srcImgHeight,
                                       BufferedImage.TYPE_INT_RGB);
            // 获取 Graphics2D 对象
            Graphics2D g = bufImg.createGraphics();
            // 设置绘图区域
            g.drawImage(srcImg, 0, 0, srcImgWidth, srcImgHeight, null);
            // 设置字体
            Font font = new Font("宋体", Font.PLAIN, 16);
            // 根据图片的背景设置水印颜色
            g.setColor(JBColor.GREEN);
            g.setFont(font);
            // 获取文字长度
            int len = g.getFontMetrics(
                g.getFont()).charsWidth(text.toCharArray(),
                                        0,
                                        text.length());
            int x = srcImgWidth - len - 10;
            int y = srcImgHeight - 20;
            g.drawString(text, x, y);
            g.dispose();
        }

        File tempFile = buildTempFile(fileName);
        try (FileOutputStream outImgStream = new FileOutputStream(tempFile)) {
            if (bufImg != null) {
                // 输出图片
                ImageIO.write(bufImg, "png", outImgStream);
                outImgStream.flush();
            }
        } catch (Exception ignored) {
        }

        return tempFile;
    }

    /**
     * 給图片添加文字水印
     *
     * @param file file
     * @since 1.6.2
     */
    public static File watermarkFromText(File file, String text) {
        // 读取原图片信息
        Image srcImg = getImage(file);
        return watermarkFromText(srcImg, file.getName(), text);
    }
}
