/*
 * MIT License
 *
 * Copyright (c) 2020 dong4j <dong4j@gmail.com>
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

import info.dong4j.idea.plugin.content.ImageContents;
import info.dong4j.idea.plugin.entity.EventData;
import info.dong4j.idea.plugin.entity.MarkdownImage;
import info.dong4j.idea.plugin.util.ImageUtils;

import java.util.List;
import java.util.Map;

/**
 * <p>Company: no company</p>
 * <p>Description: 将直接上传 image 后的 url 写入到 clipboard</p>
 *
 * @author dong4j
 * @email dong4j@gmail.com
 * @since 2019-03-28 13:34
 */
public class InsertToClipboardHandler extends ActionHandlerAdapter {

    @Override
    public String getName() {
        return "写入到 clipboard";
    }

    @Override
    public boolean execute(EventData data) {
        ProgressIndicator indicator = data.getIndicator();
        int size = data.getSize();
        int totalProcessed = 0;
        StringBuilder marks = new StringBuilder();
        for (Map.Entry<Document, List<MarkdownImage>> imageEntry : data.getWaitingProcessMap().entrySet()) {
            int totalCount = imageEntry.getValue().size();
            for (MarkdownImage markdownImage : imageEntry.getValue()) {
                String imageName = markdownImage.getImageName();
                indicator.setText2("Processing " + imageName);

                marks.append(markdownImage.getFinalMark()).append(ImageContents.LINE_BREAK);

                indicator.setFraction(((++totalProcessed * 1.0) + data.getIndex() * size) / totalCount * size);
            }
        }
        ImageUtils.setStringToClipboard(marks.toString());
        return true;
    }
}
