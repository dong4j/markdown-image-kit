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

package info.dong4j.idea.plugin.chain.paste;

import info.dong4j.idea.plugin.chain.PasteActionHandler;
import info.dong4j.idea.plugin.entity.EventData;
import info.dong4j.idea.plugin.util.ImageUtils;

import org.apache.commons.io.FileUtils;

import java.io.*;
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
public class ImageCompressHandler extends PasteActionHandler {

    @Override
    public boolean isEnabled(EventData data) {
        return STATE.isCopyToDir() && STATE.isCompress();
    }

    @Override
    public boolean execute(EventData data) {
        for (Map.Entry<String, File> imageEntry : data.getImageMap().entrySet()) {
            String fileName = imageEntry.getKey();
            File willProcessedFile = imageEntry.getValue();
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
            data.getImageMap().put(fileName, temp);
        }
        return true;
    }
}
