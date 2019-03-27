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

import com.intellij.openapi.progress.ProgressIndicator;

import info.dong4j.idea.plugin.MikBundle;
import info.dong4j.idea.plugin.entity.EventData;
import info.dong4j.idea.plugin.notify.MikNotification;
import info.dong4j.idea.plugin.util.ImageUtils;

import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;

/**
 * <p>Company: 科大讯飞股份有限公司-四川分公司</p>
 * <p>Description: 图片压缩 </p>
 *
 * @author dong4j
 * @email sjdong3@iflytek.com
 * @since 2019-03-26 12:32
 */
@Slf4j
public class ImageCompressHandler extends BaseActionHandler {

    @Override
    public boolean isEnabled(EventData data) {
        return STATE.isCompress();
    }

    @Override
    public boolean execute(EventData data) {
        ProgressIndicator indicator = data.getIndicator();

        int size = data.getSize();
        indicator.setText2(MikBundle.message("mik.chain.compress.progress"));
        int totalCount = data.getImageMap().size();
        int totalProcessed = 0;

        Map<String, String> compressInfo = new HashMap<>(data.getImageMap().size());
        for (Map.Entry<String, File> imageEntry : data.getImageMap().entrySet()) {
            String fileName = imageEntry.getKey();
            File willProcessedFile = imageEntry.getValue();
            long oldFile = willProcessedFile.length();
            String oldFileSize = bytesToKb(oldFile);
            File temp = ImageUtils.buildTempFile(willProcessedFile.getName());
            // gif 需要特殊处理, 这里暂时不处理
            if (!willProcessedFile.getName().endsWith("gif")) {
                ImageUtils.compress(willProcessedFile, temp, STATE.getCompressBeforeUploadOfPercent());
            } else {
                try {
                    FileUtils.copyFile(willProcessedFile, temp);
                } catch (IOException e) {
                    log.trace("", e);
                }
            }
            long tempFileLength = temp.length();
            String tempFileSize = bytesToKb(tempFileLength);

            DecimalFormat df = new DecimalFormat("0.00");
            double percentDouble = tempFileLength * 1.0 / oldFile;
            String percent = df.format((1 - percentDouble) * 100);

            compressInfo.put(fileName, oldFileSize + " ---> " + tempFileSize + " --->  " + percent + "%");
            data.getImageMap().put(fileName, temp);
            indicator.setFraction(((++totalProcessed * 1.0) + data.getIndex() * size) / totalCount * size);
        }
        MikNotification.notifyCompressInfo(data.getProject(), compressInfo);
        return true;
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
