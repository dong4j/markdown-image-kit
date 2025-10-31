package info.dong4j.idea.plugin.chain;

import info.dong4j.idea.plugin.MikBundle;
import info.dong4j.idea.plugin.action.intention.IntentionActionBase;
import info.dong4j.idea.plugin.entity.EventData;
import info.dong4j.idea.plugin.entity.MarkdownImage;
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
        return IntentionActionBase.getState().isCompress();
    }

    /**
     * 处理Markdown图片数据，压缩图片流并更新图片对象
     * <p>
     * 该方法用于处理Markdown图片数据，首先检查图片流是否为空，若为空则从迭代器中移除该图片。
     * 若图片名称以"gif"结尾，则直接返回。否则，使用ImageUtils工具类对图片进行压缩处理，并将压缩后的流设置回图片对象。
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

        if (imageName.endsWith("gif")) {
            return;
        }

        InputStream inputStream = markdownImage.getInputStream();
        int compressPercent = IntentionActionBase.getState().getCompressBeforeUploadOfPercent();

        try {
            // 先将输入流读取到字节数组，确保可以重新读取
            ByteArrayOutputStream tempOut = new ByteArrayOutputStream();
            inputStream.transferTo(tempOut);
            byte[] originalBytes = tempOut.toByteArray();
            tempOut.close(); // 关闭临时输出流

            // 如果开启了"转换成 webp"，直接从原始字节数组转换成 webp，使用配置的压缩质量
            if (IntentionActionBase.getState().isConvertToWebp()) {
                String ext = markdownImage.getExtension();
                boolean alreadyWebp = "webp".equalsIgnoreCase(ext);
                if (!alreadyWebp) {
                    // 直接从原始字节数组转换成 webp，使用配置的质量参数
                    try (ByteArrayOutputStream webpOut = new ByteArrayOutputStream()) {
                        try (ByteArrayInputStream originalIn = new ByteArrayInputStream(originalBytes)) {
                            ImageUtils.toWebp(originalIn, webpOut, compressPercent);
                        }
                        byte[] webpBytes = webpOut.toByteArray();
                        if (webpBytes.length > 0) {
                            // 替换流为 webp 数据
                            markdownImage.setInputStream(new ByteArrayInputStream(webpBytes));
                            // 更新文件名与扩展名为 .webp（保持同名）
                            String baseName = imageName;
                            int dot = baseName.lastIndexOf('.');
                            if (dot > 0) {
                                baseName = baseName.substring(0, dot);
                            }
                            String newName = baseName + ".webp";
                            markdownImage.setImageName(newName);
                            markdownImage.setFileName(newName);
                            markdownImage.setExtension("webp");

                            // 更新文件路径，将后缀改为 .webp
                            String originalPath = markdownImage.getPath();
                            if (originalPath != null && !originalPath.isEmpty()) {
                                // 如果路径中包含文件扩展名，则替换为 .webp
                                int pathDot = originalPath.lastIndexOf('.');
                                int pathSeparator = Math.max(originalPath.lastIndexOf('/'), originalPath.lastIndexOf('\\'));
                                // 确保点号在最后一个分隔符之后（即文件扩展名位置）
                                if (pathDot > pathSeparator) {
                                    String newPath = originalPath.substring(0, pathDot) + ".webp";
                                    markdownImage.setPath(newPath);
                                } else {
                                    // 如果没有扩展名，直接追加 .webp
                                    markdownImage.setPath(originalPath + ".webp");
                                }
                            }
                        } else {
                            // 转换失败，回退到普通压缩
                            webpOut.close(); // 关闭之前打开的流
                            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                            try (ByteArrayInputStream fallbackIn = new ByteArrayInputStream(originalBytes)) {
                                ImageUtils.compress(fallbackIn, outputStream, compressPercent);
                            }
                            markdownImage.setInputStream(new ByteArrayInputStream(outputStream.toByteArray()));
                            outputStream.close();
                        }
                    }
                    // 确保webpOut被关闭
                } else {
                    // 已经是 webp，直接压缩
                    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                    try (ByteArrayInputStream originalIn = new ByteArrayInputStream(originalBytes)) {
                        ImageUtils.compress(originalIn, outputStream, compressPercent);
                    }
                    markdownImage.setInputStream(new ByteArrayInputStream(outputStream.toByteArray()));
                    outputStream.close();
                }
            } else {
                // 未开启 webp 转换，仅压缩
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                try (ByteArrayInputStream originalIn = new ByteArrayInputStream(originalBytes)) {
                    ImageUtils.compress(originalIn, outputStream, compressPercent);
                }
                markdownImage.setInputStream(new ByteArrayInputStream(outputStream.toByteArray()));
                outputStream.close();
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
