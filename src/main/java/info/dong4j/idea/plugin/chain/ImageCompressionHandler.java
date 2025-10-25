package info.dong4j.idea.plugin.chain;

import info.dong4j.idea.plugin.MikBundle;
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
 * <p>Description: 图片压缩处理, 将 MarkdownImage 的文件修改 InputStream </p>
 *
 * @author dong4j
 * @version 0.0.1
 * @email "mailto:dong4j@gmail.com"
 * @date 2021.02.14 18:40
 * @since 0.0.1
 */
@Slf4j
public class ImageCompressionHandler extends ActionHandlerAdapter {
    /**
     * Gets name *
     *
     * @return the name
     * @since 0.0.1
     */
    @Override
    public String getName() {
        return MikBundle.message("mik.action.progress.title");
    }

    /**
     * Is enabled
     *
     * @param data data
     * @return the boolean
     * @since 0.0.1
     */
    @Override
    public boolean isEnabled(EventData data) {
        return STATE.isCompress();
    }

    /**
     * Invoke
     *
     * @param data          data
     * @param imageIterator image iterator
     * @param markdownImage markdown image
     * @since 0.0.1
     */
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
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            ImageUtils.compress(inputStream, outputStream, STATE.getCompressBeforeUploadOfPercent());
            markdownImage.setInputStream(new ByteArrayInputStream(outputStream.toByteArray()));
        } catch (Exception e) {
            log.trace("", e);
        }
    }

    /**
     * Build conpress.
     *
     * @param compressInfo the compress info
     * @param imageName    the image name
     * @param oldlength    the oldlength
     * @param newLength    the new length
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
     * byte(字节)根据长度转成kb(千字节)和mb(兆字节)
     *
     * @param bytes bytes
     * @return string
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
