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
import info.dong4j.idea.plugin.entity.MarkdownImage;
import info.dong4j.idea.plugin.enums.InsertEnum;
import info.dong4j.idea.plugin.util.ImageUtils;
import info.dong4j.idea.plugin.util.PsiDocumentUtils;

import java.util.List;

import lombok.extern.slf4j.Slf4j;

/**
 * <p>Company: 科大讯飞股份有限公司-四川分公司</p>
 * <p>Description: 插入标签</p>
 *
 * @author dong4j
 * @email sjdong3 @iflytek.com
 * @since 2019 -03-26 12:40
 */
@Slf4j
public class ImageLabelInsertHandler extends BaseActionHandler {

    @Override
    public String getName() {
        return null;
    }

    @Override
    public boolean isEnabled(EventData data) {
        if (InsertEnum.DOCUMENT.equals(data.getInsertType())) {
            return STATE.isClipboardControl() && (STATE.isCopyToDir() || STATE.isUploadAndReplace());
        } else {
            return InsertEnum.CLIPBOADR.equals(data.getInsertType()) || InsertEnum.INTENTION.equals(data.getInsertType());
        }
    }

    /**
     * 如果只是 save, 则使用 saveMarkList, 如果是 upload, 则使用 uploadedMarkList
     * 如果是从图片直接上传, 则写入 clipboard, 否则写入 markdown 文件
     *
     * @return the boolean
     */
    @Override
    public boolean execute(EventData data) {
        log.trace("insert mark");
        ProgressIndicator indicator = data.getIndicator();

        int size = data.getSize();
        indicator.setText2(MikBundle.message("mik.paste.change.progress"));
        int totalProcessed = 0;

        List<String> markList;

        boolean isPasteUpload = (InsertEnum.DOCUMENT.equals(data.getInsertType()) && STATE.isUploadAndReplace());
        boolean isIntention = InsertEnum.INTENTION.equals(data.getInsertType());
        boolean isClipboard = InsertEnum.CLIPBOADR.equals(data.getInsertType());
        if (isPasteUpload || isIntention || isClipboard) {
            markList = data.getUploadedMarkList();
        } else {
            markList = data.getSaveMarkList();
        }

        int totalCount = markList.size();

        StringBuilder stringBuilder = new StringBuilder();
        for (String mark : markList) {
            stringBuilder.append(mark);
            indicator.setFraction(((++totalProcessed * 1.0) + data.getIndex() * size) / totalCount * size);
        }

        String marks = stringBuilder.toString();
        if (InsertEnum.DOCUMENT.equals(data.getInsertType())) {
            PsiDocumentUtils.insertDocument(marks, data.getEditor());
        } else if (isClipboard) {
            ImageUtils.setStringToClipboard(marks);
        } else if(isIntention){
            MarkdownImage markdownImage = data.getMarkdownImage();
            // quickfix 只会有一个 url
            markdownImage.setUploadedUrl(markList.get(0));
            PsiDocumentUtils.commitAndSaveDocument(data.getProject(), data.getEditor().getDocument(), data.getMarkdownImage());
        }
        return true;
    }
}
