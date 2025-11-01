package info.dong4j.idea.plugin.util;

import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.ui.JBColor;
import com.intellij.util.ui.ImageUtil;

import info.dong4j.idea.plugin.content.ImageContents;
import info.dong4j.idea.plugin.content.MikContents;
import info.dong4j.idea.plugin.enums.FileType;
import info.dong4j.idea.plugin.enums.ImageMediaType;

import net.coobird.thumbnailator.Thumbnails;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.AlphaComposite;
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
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

import lombok.extern.slf4j.Slf4j;

/**
 * 图片工具类
 * <p>
 * 提供一系列图片处理相关的静态方法，包括从剪贴板获取图片、图片缩放、保存图片、加载图片、判断文件类型、生成临时文件、添加文字水印等功能。
 * 该类主要用于简化图片操作，提高代码复用性。
 *
 * @author dong4j
 * @version 1.0.0
 * @date 2025.10.24
 * @since 0.0.1
 */
@Slf4j
public final class ImageUtils {
    /**
     * 从剪贴板中获取图片
     * <p>
     * 该方法尝试从系统剪贴板中获取支持的图片数据，并返回对应的图片对象。如果剪贴板中不包含图片数据或获取失败，则返回 null。
     *
     * @return 从剪贴板中获取的图片对象，若未找到或获取失败则返回 null
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
     * 从剪切板中获取数据
     * <p>
     * 读取系统剪切板内容，并根据支持的数据类型（如文件列表、图片、字符串等）提取对应的数据，返回一个包含单一键值对的 Map。
     * 优先级：文件列表 > 图片 > 字符串
     *
     * @return 从剪切板获取到的数据，Map 中只包含一对 key-value，若剪切板内容不支持或获取失败则返回 null
     * @since 0.0.1
     */
    @Nullable
    public static Map<DataFlavor, Object> getDataFromClipboard() {
        Map<DataFlavor, Object> data = new HashMap<>(1);
        Transferable transferable = Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null);
        if (transferable != null) {
            // 按优先级检查剪切板内容类型
            try {
                DataFlavor dataFlavor;
                if (transferable.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                    // List<File> - 优先级最高
                    dataFlavor = DataFlavor.javaFileListFlavor;
                } else if (transferable.isDataFlavorSupported(DataFlavor.imageFlavor)) {
                    // Image - 优先级第二
                    dataFlavor = DataFlavor.imageFlavor;
                } else if (transferable.isDataFlavorSupported(DataFlavor.stringFlavor)) {
                    // String - 优先级第三（支持网络图片 URL）
                    dataFlavor = DataFlavor.stringFlavor;
                } else {
                    return null;
                }
                Object object = transferable.getTransferData(dataFlavor);
                data.put(dataFlavor, object);
            } catch (IOException | UnsupportedFlavorException ignored) {
                // 如果 clipboard 获取失败, 不处理
            }
        }
        return data;
    }

    /**
     * 把文本设置到剪贴板（复制）
     * <p>
     * 该方法用于将指定的文本内容复制到系统的剪贴板中，以便其他应用程序可以粘贴使用。
     *
     * @param text 要设置到剪贴板的文本内容
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
     * 对给定的 BufferedImage 进行缩放操作，生成指定尺寸的新图片。
     * <p>
     * 该方法首先检查源图片和目标尺寸是否有效，若无效则返回 null。否则，使用仿射变换和双线性插值算法对图片进行缩放。
     *
     * @param sourceImage 源图片，若为 null 则返回 null
     * @param newWidth    缩放后图片的宽度
     * @param newHeight   缩放后图片的高度
     * @return 缩放后的 BufferedImage，若输入无效则返回 null
     * @throws NullPointerException 如果 sourceImage 为 null 且未正确处理
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
     * 将 BufferedImage 图像保存到指定的文件路径，并使用指定的格式
     * <p>
     * 该方法使用 ImageIO 类将图像写入文件，若保存过程中发生异常，会打印错误信息到控制台
     *
     * @param image  需要保存的图像对象
     * @param file   保存图像的目标文件对象
     * @param format 保存图像的文件格式，如 "jpg"、"png" 等
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
     * 从文件加载图像到 BufferedImage 对象。
     * <p>
     * 尝试读取指定文件中的图像数据，并返回对应的 BufferedImage 对象。如果文件无效或读取失败，返回 null。
     *
     * @param cachedImageFile 要读取的图像文件
     * @return 返回读取到的 BufferedImage 对象，若读取失败或文件无效则返回 null
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
            //noinspection ResultOfMethodCallIgnored
            cachedImageFile.delete();
            return null;
        }

        return null;
    }

    /**
     * 从指定的URL加载图片并返回BufferedImage对象。
     * <p>
     * 该方法通过给定的图片URL创建ImageIcon，再将其转换为BufferedImage返回。
     * 如果URL无效或转换过程中发生异常，将返回null。
     *
     * @param imageURL 图片的URL地址
     * @return 转换后的BufferedImage对象，若加载失败则返回null
     * @since 0.0.1
     */
    @Nullable
    public static BufferedImage loadImageFromURL(String imageURL) {
        try {
            return toBufferedImage(new ImageIcon(new URI(imageURL).toURL()).getImage());
        } catch (Exception ignored) {
        }
        return null;
    }

    /**
     * 将给定的图像转换为 BufferedImage 对象
     * <p>
     * 如果传入的图像已经是 BufferedImage 类型，则直接返回；否则创建一个新的 BufferedImage
     * 并将原图像绘制到新图像上。
     *
     * @param src 需要转换的图像对象
     * @return 转换后的 BufferedImage 对象，可能为 null
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
        BufferedImage dest = ImageUtil.createImage(w, h, type);
        Graphics2D g2 = dest.createGraphics();
        g2.drawImage(src, 0, 0, null);
        g2.dispose();

        return dest;
    }

    /**
     * 创建带有圆角的图片
     * <p>
     * 该方法将给定的图片转换为带有指定圆角半径的圆角图片。首先创建一个与原图片大小相同的透明图片，然后使用图形绘制工具绘制圆角矩形作为背景，并将原图片绘制在圆角矩形之上。
     *
     * @param image        需要处理的原始图片
     * @param cornerRadius 圆角的半径，单位为像素
     * @return 处理后的带有圆角的图片
     * @since 0.0.1
     */
    public static BufferedImage makeRoundedCorner(@NotNull BufferedImage image, int cornerRadius) {
        int w = image.getWidth();
        int h = image.getHeight();
        BufferedImage output = ImageUtil.createImage(w, h, BufferedImage.TYPE_INT_ARGB);
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
     * 移除图像的透明通道，将其转换为不透明的 RGB 图像
     * <p>
     * 该方法将输入的带有透明通道的图像转换为不透明的 RGB 图像，背景颜色默认为白色。
     *
     * @param image 需要处理的图像对象
     * @return 转换后的不透明 RGB 图像
     * @since 0.0.1
     */
    public static BufferedImage removeAlpha(@NotNull BufferedImage image) {
        BufferedImage bufferedImage = ImageUtil.createImage((image.getWidth(null)), image.getHeight(null), BufferedImage.TYPE_INT_RGB);
        Graphics g = bufferedImage.createGraphics();
        //Color.WHITE estes the background to white. You can use any other color
        g.drawImage(image, 0, 0, bufferedImage.getWidth(), bufferedImage.getHeight(), JBColor.WHITE, null);
        g.dispose();

        return bufferedImage;
    }

    /**
     * 将 BufferedImage 中的白色像素转换为透明，并生成新的 Image 对象
     * <p>
     * 该方法通过创建一个自定义的图像过滤器，将图像中所有白色像素的 alpha 通道设为 0，从而实现透明效果。适用于将白色背景变为透明的图像处理场景。
     *
     * @param image 需要处理的 BufferedImage 对象
     * @return 处理后的 Image 对象，白色像素变为透明
     * @throws NullPointerException 如果传入的 image 为 null
     */
    public static Image whiteToTransparent(@NotNull BufferedImage image) {
        ImageFilter filter = new RGBImageFilter() {
            /** 标记颜色的 RGB 值，用于表示特定的标记颜色 */
            final int markerRGB = JBColor.WHITE.getRGB() | 0xFF000000;

            /**
             * 根据指定坐标和颜色值过滤RGB颜色值
             * <p>
             * 该方法用于处理图像像素颜色值，若颜色值与标记颜色匹配，则设置透明度为0（完全透明）；否则返回原颜色值。
             *
             * @param x   像素的x坐标
             * @param y   像素的y坐标
             * @param rgb 像素的原始RGB颜色值
             * @return 处理后的RGB颜色值
             */
            @Override
            public int filterRGB(int x, int y, int rgb) {
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
     * 判断给定文件路径是否指向图片文件
     * <p>
     * 通过文件路径创建File对象，并调用内部方法判断是否为图片文件
     *
     * @param filePath 文件路径
     * @return 如果是图片文件返回true，否则返回false
     * @since 0.0.1
     */
    public static boolean isImageFile(String filePath) {
        return isImageFile(new File(filePath));
    }

    /**
     * 判断给定文件是否为图片文件
     * <p>
     * 通过 ImageReader 来解码文件并返回一个 BufferedImage 对象。如果找不到合适的 javax.imageio.ImageReader，则返回 null，认为该文件不是图片文件。
     *
     * @param file 要判断的文件对象
     * @return 如果文件是图片文件则返回 true，否则返回 false
     * @since 0.0.1
     */
    public static boolean isImageFile(File file) {
        Image image = getImage(file);
        return image != null;
    }

    /**
     * 从文件中读取图像
     * <p>
     * 使用ImageIO工具类尝试从指定文件中加载图像，若读取失败则返回null
     *
     * @param file 要读取的图像文件路径
     * @return 读取到的图像对象，若读取失败则返回null
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
     * 对文件进行压缩处理
     * <p>
     * 使用 Thumbnails 工具对指定的输入文件进行压缩，并将结果保存到输出文件中。
     * 压缩比例由传入的百分比参数决定。
     *
     * @param in      需要压缩的输入文件
     * @param out     压缩后的输出文件
     * @param percent 压缩比例，取值范围为 0-100
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
     * 对输入流中的图像进行压缩，并将结果写入输出流
     * <p>
     * 该方法使用 Thumbnails 工具对图像进行压缩处理，压缩比例由 percent 参数控制。
     *
     * @param in      输入流，包含需要压缩的图像数据
     * @param out     输出流，用于写入压缩后的图像数据
     * @param percent 压缩比例，取值范围为 0-100，表示压缩质量百分比
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
     * 对输入的图像流进行压缩，并保存到指定的文件中。
     * <p>
     * 该方法使用 Thumbnails 工具对图像进行压缩处理，压缩比例由 percent 参数决定。
     * 压缩后的图像将保存到指定的输出文件中。
     *
     * @param in      输入的图像流
     * @param out     压缩后的图像保存路径
     * @param percent 压缩比例，取值范围为 0-100，表示压缩到原始大小的百分比
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
     * 将输入流中的图片转换为 WebP 格式并输出到指定输出流
     * <p>
     * 依赖 ImageIO 对 WebP 的支持（例如通过相应的插件），否则可能失败。
     *
     * @param in  原始图片输入流
     * @param out WebP 图片输出流
     */
    public static void toWebp(InputStream in, OutputStream out) {
        try {
            Thumbnails.of(in)
                .scale(1f)
                .outputFormat("webp")
                .toOutputStream(out);
        } catch (IOException e) {
            log.trace("", e);
        }
    }

    /**
     * 将输入流中的图片转换为 WebP 格式并输出到指定输出流，支持指定质量参数
     * <p>
     * 依赖 ImageIO 对 WebP 的支持（例如通过相应的插件），否则可能失败。
     * 使用配置的质量参数，充分利用 WebP 兼备质量与压缩比的特性。
     *
     * @param in      原始图片输入流
     * @param out     WebP 图片输出流
     * @param percent 压缩质量，取值范围为 0-100，表示压缩质量百分比
     */
    public static void toWebp(InputStream in, OutputStream out, int percent) {
        try {
            Thumbnails.of(in)
                // 保持原始图片尺寸
                .scale(1f)
                .outputFormat("webp")
                .outputQuality(percent * 1.0 / 100)
                .toOutputStream(out);
        } catch (Exception e) {
            log.trace("", e);
        }
    }

    /**
     * 判断OSS服务文件上传时文件的contentType
     * <p>
     * 根据文件名后缀判断对应的图片类型，并返回对应的MIME类型字符串
     *
     * @param fileName 文件名
     * @return 返回对应的图片MIME类型字符串，若无法识别则返回空字符串
     */
    public static String getImageType(String fileName) {
        return ImageMediaType.fromFileName(fileName);
    }

    /**
     * 获取文件的后缀字符串
     * <p>
     * 通过文件名获取文件的后缀部分，例如对于文件名"example.txt"，返回"txt"
     *
     * @param fileName 文件名
     * @return 文件后缀字符串
     * @since 0.0.1
     */
    @NotNull
    public static String getFileExtension(@NotNull String fileName) {
        return fileName.substring(fileName.lastIndexOf("."));
    }

    /**
     * 根据输入流获取文件类型
     * <p>
     * 通过读取输入流的前28个字节，将其转换为十六进制字符串，并与已知的文件类型值进行匹配，返回对应的文件类型。
     *
     * @param is 输入流，用于读取文件头信息
     * @return 匹配的文件类型，若未找到匹配项则返回 null
     * @throws IOException 如果读取输入流时发生异常
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
     * 创建一个临时文件
     * <p>
     * 根据给定的文件名在系统默认的临时目录下创建一个新的临时文件。
     *
     * @param fileName 文件名
     * @return 新创建的临时文件对象
     * @since 0.0.1
     */
    @NotNull
    @Contract("_ -> new")
    public static File buildTempFile(String fileName) {
        return new File(System.getProperty("java.io.tmpdir") + fileName);
    }

    /**
     * 递归遍历目录，收集所有图片文件
     * <p>
     * 该方法通过递归方式遍历指定目录及其子目录，筛选出所有图片文件并添加到结果列表中。
     *
     * @param virtualFile 要遍历的虚拟文件对象，表示目录入口
     * @return 包含所有图片文件的虚拟文件列表
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
     * 判断文件是否有效
     * <p>
     * 检查给定的文件是否为图片文件，并且是否可写。如果文件为空或不是图片文件，则返回 false。
     *
     * @param file 文件对象
     * @return 文件是否有效（即是否为图片文件且可写）
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
     * 判断给定文件是否为图片文件
     * <p>
     * 通过检查文件类型名称是否为图片类型来判断该文件是否为图片文件
     *
     * @param file 文件对象
     * @return 如果是图片文件返回 true，否则返回 false
     * @since 0.0.1
     */
    private static boolean isImageFile(PsiFile file) {
        return ImageContents.IMAGE_TYPE_NAME.equals(file.getFileType().getName());
    }

    /**
     * 判断给定文件是否为图片文件
     * <p>
     * 通过比较文件类型名称与预定义的图片类型名称，判断该文件是否为图片类型
     *
     * @param file 要判断的文件对象
     * @return 如果是图片文件返回 true，否则返回 false
     * @since 0.0.1
     */
    public static boolean isImageFile(VirtualFile file) {
        return ImageContents.IMAGE_TYPE_NAME.equals(file.getFileType().getName());
    }

    /**
     * 根据文本内容为图片添加水印并生成临时文件
     * <p>
     * 该方法接收一张图片、文件名和水印文本，为图片添加水印后，生成临时文件并返回。
     *
     * @param srcImg   原始图片对象
     * @param fileName 生成的文件名
     * @param text     水印文本内容
     * @return 生成的临时文件对象
     * @since y.y.y
     */
    public static File watermarkFromText(Image srcImg, String fileName, String text) {
        BufferedImage bufImg = null;
        if (srcImg != null) {
            int srcImgWidth = srcImg.getWidth(null);
            int srcImgHeight = srcImg.getHeight(null);
            // 加水印
            bufImg = ImageUtil.createImage(srcImgWidth,
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
     * <p>
     * 读取指定图片文件，并在其上添加文字水印，返回添加水印后的图片文件。
     *
     * @param file 要添加水印的图片文件
     * @return 添加水印后的图片文件
     * @since 1.6.2
     */
    public static File watermarkFromText(File file, String text) {
        // 读取原图片信息
        Image srcImg = getImage(file);
        return watermarkFromText(srcImg, file.getName(), text);
    }
}
