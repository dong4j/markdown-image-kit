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

package info.dong4j.idea.plugin.chain;

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.progress.ProgressIndicator;

import info.dong4j.idea.plugin.entity.EventData;
import info.dong4j.idea.plugin.entity.MarkdownImage;
import info.dong4j.idea.plugin.notify.MikNotification;
import info.dong4j.idea.plugin.util.ImageUtils;

import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;

/**
 * <p>Company: 科大讯飞股份有限公司-四川分公司</p>
 * <p>Description: 图片压缩处理, 将 MarkdownImage 的文件修改 InputStream </p>
 *
 * @author dong4j
 * @email sjdong3 @iflytek.com
 * @since 2019 -03-28 09:23
 */
@Slf4j
public class ImageCompressionHandler extends BaseActionHandler {
    @Override
    public String getName() {
        return "图片压缩";
    }

    @Override
    public boolean isEnabled(EventData data) {
        return STATE.isCompress();
    }

    /**
     * 通过 InputStream 压缩, 处理后替换原来的 InputStream
     *
     * @param data the data
     * @return the boolean
     */
    @Override
    public boolean execute(EventData data) {
        ProgressIndicator indicator = data.getIndicator();
        int size = data.getSize();
        int totalProcessed = 0;
        Map<String, String> compressInfo = new HashMap<>(10);

        for (Map.Entry<Document, List<MarkdownImage>> imageEntry : data.getWaitingProcessMap().entrySet()) {
            int totalCount = imageEntry.getValue().size();
            for (MarkdownImage markdownImage : imageEntry.getValue()) {
                indicator.setFraction(((++totalProcessed * 1.0) + data.getIndex() * size) / totalCount * size);
                if (markdownImage.getInputStream() == null) {
                    continue;
                }
                indicator.setText2("process " + imageEntry.getValue());

                String imageName = markdownImage.getImageName();
                if (imageName.endsWith("gif")) {
                    continue;
                }

                InputStream inputStream = markdownImage.getInputStream();
                try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
                    long oldlength = inputStream.available();
                    ImageUtils.compress(inputStream, outputStream, STATE.getCompressBeforeUploadOfPercent());
                    long newLength = outputStream.toByteArray().length;
                    markdownImage.setInputStream(new ByteArrayInputStream(outputStream.toByteArray()));
                    buildConpress(compressInfo, imageName, oldlength, newLength);
                } catch (Exception e) {
                    log.trace("", e);
                }
            }
        }

        MikNotification.notifyCompressInfo(data.getProject(), compressInfo);
        return true;
    }

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
     * @param bytes
     * @return
     */
    @NotNull
    private static String bytesToKb(long bytes) {
        BigDecimal filesize = new BigDecimal(bytes);
        BigDecimal megabyte = new BigDecimal(1024 * 1024);
        float returnValue = filesize.divide(megabyte, 2, BigDecimal.ROUND_UP)
            .floatValue();
        if (returnValue > 1) {
            return (returnValue + "MB");
        }
        BigDecimal kilobyte = new BigDecimal(1024);
        returnValue = filesize.divide(kilobyte, 2, BigDecimal.ROUND_UP).floatValue();
        return (returnValue + "KB");
    }
}