package info.dong4j.idea.plugin.util;

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

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

/**
 * <p>Company: 科大讯飞股份有限公司-四川分公司</p>
 * <p>Description: </p>
 *
 * @author dong4j
 * @date 2019 -03-16 12:12
 * @email sjdong3 @iflytek.com
 */
public class ImageUtils {

    /**
     * Gets image from clipboard.
     *
     * @return the image from clipboard
     */
    public static Image getImageFromClipboard() {
        Transferable transferable = Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null);
        try {
            if (transferable != null && transferable.isDataFlavorSupported(DataFlavor.imageFlavor)) {
                return (Image) transferable.getTransferData(DataFlavor.imageFlavor);
            } else {
                return null;
            }

        } catch (UnsupportedFlavorException e) {
            e.printStackTrace();
        } catch (IOException e) {
            throw new RuntimeException();
        }

        return null;
    }


    /**
     * Scale image buffered image.
     *
     * @param sourceImage the source image
     * @param newWidth    the new width
     * @param newHeight   the new height
     * @return the buffered image
     */
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
     * To buffered image buffered image.
     *
     * @param src the src
     * @return the buffered image
     */
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
        BufferedImage dest = new BufferedImage(w, h, type);
        Graphics2D g2 = dest.createGraphics();
        g2.drawImage(src, 0, 0, null);
        g2.dispose();

        return dest;
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
    public static BufferedImage loadImageFromFile(File cachedImageFile) {
        if (cachedImageFile == null || !cachedImageFile.isFile()) {
            return null;
        }

        try {
            for (int i = 0; i < 3; i++) {
                BufferedImage read = null;
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
    public static BufferedImage loadImageFromURL(String imageURL) {
        try {
            return toBufferedImage(new ImageIcon(new URL(imageURL)).getImage());
        } catch (MalformedURLException ignored) {
        }

        return null;
    }


    /**
     * http://stackoverflow.com/questions/7603400/how-to-make-a-rounded-corner-image-in-java
     *
     * @param image        the image
     * @param cornerRadius the corner radius
     * @return the buffered image
     */
    public static BufferedImage makeRoundedCorner(BufferedImage image, int cornerRadius) {
        int w = image.getWidth();
        int h = image.getHeight();
        BufferedImage output = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = output.createGraphics();
        g2.setComposite(AlphaComposite.Src);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(Color.WHITE);
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
    public static BufferedImage removeAlpha(BufferedImage image) {
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
    public static Image whiteToTransparent(BufferedImage image) {
        ImageFilter filter = new RGBImageFilter() {
            public int markerRGB = Color.WHITE.getRGB() | 0xFF000000;

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
}
