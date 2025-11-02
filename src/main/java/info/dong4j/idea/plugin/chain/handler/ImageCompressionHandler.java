package info.dong4j.idea.plugin.chain.handler;

import info.dong4j.idea.plugin.MikBundle;
import info.dong4j.idea.plugin.action.intention.IntentionActionBase;
import info.dong4j.idea.plugin.entity.EventData;
import info.dong4j.idea.plugin.entity.MarkdownImage;
import info.dong4j.idea.plugin.enums.ImageMediaType;
import info.dong4j.idea.plugin.util.ImageUtils;

import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.Iterator;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;

/**
 * 图片压缩处理类
 * <p>
 * 用于处理 Markdown 图片的压缩操作，将图片的 InputStream 进行压缩处理，并更新为压缩后的输入流。
 * 支持对非 GIF 格式的图片进行压缩，同时记录压缩前后的大小及压缩率。
 * 该类继承自 ActionHandlerAdapter，用于在特定事件触发时执行图片压缩逻辑。
 *
 * @author dong4j
 * @version 0.0.1
 * @date 2021.02.14
 * @since 0.0.1
 */
@Slf4j
public class ImageCompressionHandler extends ActionHandlerAdapter {
    /**
     * 获取名称
     * <p>
     * 返回预定义的名称字符串，用于表示操作进度标题
     *
     * @return 名称字符串
     * @since 0.0.1
     */
    @Override
    public String getName() {
        return MikBundle.message("mik.action.progress.title");
    }

    /**
     * 判断当前状态是否启用压缩功能
     * <p>
     * 根据当前状态判断是否启用压缩功能，返回对应的布尔值
     *
     * @param data 事件数据，用于上下文信息
     * @return 是否启用压缩功能
     * @since 0.0.1
     */
    @Override
    public boolean isEnabled(EventData data) {
        return IntentionActionBase.getState().isCompress() || IntentionActionBase.getState().isConvertToWebp();
    }

    /**
     * 处理Markdown图片数据，压缩图片流并更新图片对象
     * <p>
     * 该方法用于处理Markdown图片数据，首先检查图片流是否为空，若为空则从迭代器中移除该图片。
     * 若图片名称以"gif"结尾，则直接返回。否则，根据配置进行压缩或转换为webp处理：
     * 1. 如果都开启了：先尝试转为webp，失败就回退到普通压缩
     * 2. 如果只开启了图片压缩：直接压缩
     * 3. 如果只开启了转换成webp：尝试转为webp，失败就不压缩
     *
     * @param data          事件数据对象
     * @param imageIterator 图片迭代器，用于遍历和移除图片
     * @param markdownImage Markdown图片对象，包含图片名称和输入流
     */
    @SuppressWarnings("D")
    @Override
    public void invoke(EventData data, Iterator<MarkdownImage> imageIterator, MarkdownImage markdownImage) {
        String imageName = markdownImage.getImageName();
        if (markdownImage.getInputStream() == null) {
            log.trace("inputstream 为 null, remove markdownImage = {}", markdownImage);
            imageIterator.remove();
            return;
        }

        if (imageName.endsWith(ImageMediaType.GIF.getExtensionWithoutDot()) || imageName.endsWith(ImageMediaType.SVG_XML.getExtensionWithoutDot())) {
            return;
        }

        InputStream inputStream = markdownImage.getInputStream();
        boolean isCompressEnabled = IntentionActionBase.getState().isCompress();
        boolean isWebpEnabled = IntentionActionBase.getState().isConvertToWebp();
        int compressPercent = IntentionActionBase.getState().getCompressBeforeUploadOfPercent();
        int webpQuality = IntentionActionBase.getState().getWebpQuality();

        try {
            // 先将输入流读取到字节数组，确保可以重新读取
            ByteArrayOutputStream tempOut = new ByteArrayOutputStream();
            inputStream.transferTo(tempOut);
            byte[] originalBytes = tempOut.toByteArray();
            tempOut.close(); // 关闭临时输出流

            // 判断是否已经是webp格式
            String ext = markdownImage.getExtension();
            boolean alreadyWebp = ImageMediaType.WEBP.getExtensionWithoutDot().equalsIgnoreCase(ext);

            // 情况1：如果都开启了，先尝试转webp，失败就回退到普通压缩
            if (isCompressEnabled && isWebpEnabled) {
                if (!alreadyWebp) {
                    // 尝试转换为webp
                    boolean webpSuccess = tryConvertToWebp(markdownImage, originalBytes, webpQuality, imageName);
                    if (!webpSuccess) {
                        // webp转换失败，回退到普通压缩
                        log.trace("webp转换失败，回退到普通压缩: {}", imageName);
                        compressImage(markdownImage, originalBytes, compressPercent);
                    }
                } else {
                    // 已经是webp，直接压缩
                    compressImage(markdownImage, originalBytes, compressPercent);
                }
            }
            // 情况2：如果只开启了图片压缩，直接压缩
            else if (isCompressEnabled) {
                compressImage(markdownImage, originalBytes, compressPercent);
            }
            // 情况3：如果只开启了转换成webp，尝试转webp，失败就不压缩
            else if (isWebpEnabled) {
                if (!alreadyWebp) {
                    boolean webpSuccess = tryConvertToWebp(markdownImage, originalBytes, webpQuality, imageName);
                    if (!webpSuccess) {
                        // webp转换失败，不压缩，保持原样
                        log.trace("WebP 转换失败，保持原样: {}", imageName);
                        markdownImage.setInputStream(new ByteArrayInputStream(originalBytes));
                    }
                } else {
                    // 如果已经是webp，不需要转换，但需要重新设置输入流（因为原始流已被读取）
                    log.trace("图片已经是webp格式，不需要转换: {}", imageName);
                    markdownImage.setInputStream(new ByteArrayInputStream(originalBytes));
                }
            }

            // 关闭原始输入流
            try {
                inputStream.close();
            } catch (Exception ignored) {
            }
        } catch (Exception e) {
            log.trace("", e);
            // 发生异常时也要确保关闭流
            try {
                inputStream.close();
            } catch (Exception ignored) {
            }
        }
    }

    /**
     * 尝试将图片转换为webp格式
     *
     * @param markdownImage Markdown图片对象
     * @param originalBytes 原始图片字节数组
     * @param webpQuality   webp质量（0-100）
     * @param imageName     图片名称
     * @return 是否转换成功
     */
    private boolean tryConvertToWebp(MarkdownImage markdownImage, byte[] originalBytes, int webpQuality, String imageName) {
        try (ByteArrayOutputStream webpOut = new ByteArrayOutputStream()) {
            try (ByteArrayInputStream originalIn = new ByteArrayInputStream(originalBytes)) {
                ImageUtils.toWebp(originalIn, webpOut, webpQuality);
            }
            byte[] webpBytes = webpOut.toByteArray();
            if (webpBytes.length > 0) {
                // 转换成功，替换流为webp数据
                markdownImage.setInputStream(new ByteArrayInputStream(webpBytes));
                // 更新文件名与扩展名为.webp
                String baseName = imageName;
                int dot = baseName.lastIndexOf('.');
                if (dot > 0) {
                    baseName = baseName.substring(0, dot);
                }
                String newName = baseName + ImageMediaType.WEBP.getExtension();
                markdownImage.setImageName(newName);
                markdownImage.setFilename(newName);
                markdownImage.setExtension(ImageMediaType.WEBP.getExtensionWithoutDot());

                // 更新文件路径，将后缀改为.webp
                String originalPath = markdownImage.getPath();
                if (originalPath != null && !originalPath.isEmpty()) {
                    int pathDot = originalPath.lastIndexOf('.');
                    int pathSeparator = Math.max(originalPath.lastIndexOf('/'), originalPath.lastIndexOf('\\'));
                    if (pathDot > pathSeparator) {
                        String newPath = originalPath.substring(0, pathDot) + ImageMediaType.WEBP.getExtension();
                        markdownImage.setPath(newPath);
                    } else {
                        markdownImage.setPath(originalPath + ImageMediaType.WEBP.getExtension());
                    }
                }
                return true;
            }
        } catch (Exception e) {
            log.trace("转换为webp时发生异常: {}", imageName, e);
        }
        return false;
    }

    /**
     * 压缩图片
     *
     * @param markdownImage   Markdown图片对象
     * @param originalBytes   原始图片字节数组
     * @param compressPercent 压缩比例（0-100）
     */
    private void compressImage(MarkdownImage markdownImage, byte[] originalBytes, int compressPercent) {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            try (ByteArrayInputStream originalIn = new ByteArrayInputStream(originalBytes)) {
                ImageUtils.compress(originalIn, outputStream, compressPercent);
            }
            markdownImage.setInputStream(new ByteArrayInputStream(outputStream.toByteArray()));
        } catch (Exception e) {
            log.trace("压缩图片时发生异常", e);
        }
    }


    /**
     * 构建压缩信息，计算并存储压缩前后的大小及压缩率
     * <p>
     * 根据传入的压缩信息、图片名称、原始大小和压缩后大小，计算压缩率并存储到压缩信息映射中
     *
     * @param compressInfo 压缩信息映射，用于存储压缩结果
     * @param imageName    图片名称
     * @param oldlength    压缩前的大小（字节）
     * @param newLength    压缩后的大小（字节）
     * @since 0.0.1
     */
    private void buildConpress(@NotNull Map<String, String> compressInfo,
                               String imageName,
                               long oldlength,
                               long newLength) {
        String oldSize = bytesToKb(oldlength);
        String newSize = bytesToKb(newLength);

        DecimalFormat df = new DecimalFormat("0.00");
        double percentDouble = newLength * 1.0 / oldlength;
        String percent = df.format((1 - percentDouble) * 100);
        String message = oldSize + " ---> " + newSize + " --->  " + percent + "%";
        compressInfo.put(imageName, message);
    }

    /**
     * 将字节单位转换为千字节（KB）或兆字节（MB）并返回字符串表示
     * <p>
     * 根据输入的字节数计算对应的KB或MB值，并保留两位小数，单位为KB或MB。
     *
     * @param bytes 要转换的字节数
     * @return 转换后的字符串，格式为"X.XXKB"或"X.XXMB"
     * @since 0.0.1
     */
    @NotNull
    private static String bytesToKb(long bytes) {
        BigDecimal filesize = new BigDecimal(bytes);
        BigDecimal megabyte = new BigDecimal(1024 * 1024);
        float returnValue = filesize.divide(megabyte, 2, RoundingMode.UP)
            .floatValue();
        if (returnValue > 1) {
            return (returnValue + "MB");
        }
        BigDecimal kilobyte = new BigDecimal(1024);
        returnValue = filesize.divide(kilobyte, 2, RoundingMode.UP).floatValue();
        return (returnValue + "KB");
    }
}
