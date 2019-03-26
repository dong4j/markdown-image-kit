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
import info.dong4j.idea.plugin.content.ImageContents;
import info.dong4j.idea.plugin.entity.EventData;
import info.dong4j.idea.plugin.enums.InsertEnum;
import info.dong4j.idea.plugin.util.UploadUtils;

import java.util.ArrayList;
import java.util.List;

import lombok.extern.slf4j.Slf4j;

/**
 * <p>Company: 科大讯飞股份有限公司-四川分公司</p>
 * <p>Description: image mark 转换 </p>
 *
 * @author dong4j
 * @email sjdong3 @iflytek.com
 * @since 2019 -03-22 18:49
 */
@Slf4j
public class ImageLabelChangeHandler extends BaseActionHandler {

    @Override
    public boolean isEnabled(EventData data) {
        // 不管是否勾选替换标签,都执行, 在 getFinalImageMark 处理
        return true;
    }

    /**
     * 需要等待上传完成后才能执行这步操作
     *
     * @param data the data
     * @return the boolean
     */
    @Override
    public boolean execute(EventData data) {
        ProgressIndicator indicator = data.getIndicator();

        int size = data.getSize();
        indicator.setText2(MikBundle.message("mik.paste.change.progress"));

        boolean isPasteUpload = (InsertEnum.DOCUMENT.equals(data.getInsertType()) && STATE.isUploadAndReplace());
        if (isPasteUpload || InsertEnum.CLIPBOADR.equals(data.getInsertType())) {
            int totalProcessed = 0;
            List<String> oldMarks = data.getUploadedMarkList();
            int totalCount = oldMarks.size();

            List<String> changedMarks = new ArrayList<>(oldMarks.size());
            for (String mark : oldMarks) {
                String newLineText = UploadUtils.getFinalImageMark("", mark, mark, ImageContents.LINE_BREAK);
                changedMarks.add(newLineText);
                indicator.setFraction(((++totalProcessed * 1.0) + data.getIndex() * size) / totalCount * size);
            }
            data.setUploadedMarkList(changedMarks);
        }
        return true;
    }
}