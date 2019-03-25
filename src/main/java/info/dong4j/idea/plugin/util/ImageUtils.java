/*
 * MIT License
 *
 * Copyright (c) 2019 dong4j <dong4j@gmail.com>
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
 *
 */

package info.dong4j.idea.plugin.util;

import com.intellij.ui.JBColor;
import com.intellij.util.containers.hash.HashMap;
import com.intellij.util.ui.UIUtil;
import com.siyeh.ig.portability.mediatype.ImageMediaType;

import info.dong4j.idea.plugin.enums.FileType;

import net.coobird.thumbnailator.Thumbnails;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
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
import java.io.*;
import java.net.*;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

import lombok.extern.slf4j.Slf4j;

/**
 * <p>Company: 科大讯飞股份有限公司-四川分公司</p>
 * <p>Description: </p>
 *
 * @author dong4j
 * @date 2019 -03-16 12:12
 * @email sjdong3 @iflytek.com
 */
@Slf4j
public final class ImageUtils {

    /**
     * Gets image from clipboard.
     *
     * @return the image from clipboard
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
     * Scale image buffered image.
     *
     * @param sourceImage the source image
     * @param newWidth    the new width
     * @param newHeight   the new height
     * @return the buffered image
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
     * Save string.
     *
     * @param image     the image
     * @param preFix    the pre fix
     * @param format    the format
     * @param accessKey the access key
     * @param secretKey the secret key
     * @param upHost    the up host
     * @return the string
     * @throws IOException the io exception
     */
    public static String save(BufferedImage image, String preFix, String format, String accessKey, String secretKey, String upHost) throws IOException {

        ByteArrayOutputStream byteOutputStream = new ByteArrayOutputStream();
        ImageIO.write(image, format, byteOutputStream);
        return QiniuUploadUtils.uploadImage(byteOutputStream.toByteArray(), preFix, accessKey, secretKey, upHost);

    }

    /**
     * Load image from file buffered image.
     *
     * @param cachedImageFile the cached image file
     * @return Could be <code>null</code> if the image could not be read from the file (because of whatever strange     reason).
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
     */
    public static Image whiteToTransparent(@NotNull BufferedImage image) {
        ImageFilter filter = new RGBImageFilter() {
            int markerRGB = JBColor.WHITE.getRGB() | 0xFF000000;

            @Override
            public final int filterRGB(int x, int y, int rgb) {
                if ((rgb | 0xFF000000) == markerRGB) {
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
     */
    private static boolean isImageFile(File file) {
        try {
            Image image = ImageIO.read(file);
            return image != null;
        } catch (IOException ex) {
            return false;
        }
    }

    /**
     * Compress.
     *
     * @param in      the in
     * @param out     the out
     * @param percent the percent
     */
    public static void compress(File in, File out, int percent) {
        try {
            Thumbnails.of(in)
                .scale(1f)
                .outputQuality(percent * 1.0 / 100)
                .toFile(out);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Description: 判断OSS服务文件上传时文件的contentType
     *
     * @param fileName the file name
     * @return String string
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
     */
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
}
