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
import info.dong4j.idea.plugin.util.ConvertUtil;
import info.dong4j.idea.plugin.util.ImageUtils;

import java.io.*;
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
                File temp = ImageUtils.buildTempFile(imageName);
                try (OutputStream outputStream = new ByteArrayOutputStream()) {
                    long oldlength = inputStream.available();

                    ImageUtils.compress(inputStream, outputStream, STATE.getCompressBeforeUploadOfPercent());

                    ((ByteArrayOutputStream) outputStream).toByteArray()
                    long newLength = outputStream.
                    markdownImage.setInputStream(ConvertUtil.parse(outputStream));
                } catch (Exception e) {
                    log.trace("", e);
                } finally {
                    temp.deleteOnExit();
                }
            }
        }

        MikNotification.notifyCompressInfo(data.getProject(), compressInfo);
        return true;
    }
}