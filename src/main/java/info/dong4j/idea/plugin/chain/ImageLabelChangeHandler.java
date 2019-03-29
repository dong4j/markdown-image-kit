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

import info.dong4j.idea.plugin.MikBundle;
import info.dong4j.idea.plugin.entity.EventData;
import info.dong4j.idea.plugin.entity.MarkdownImage;
import info.dong4j.idea.plugin.enums.ImageLocationEnum;
import info.dong4j.idea.plugin.enums.ImageMarkEnum;
import info.dong4j.idea.plugin.util.ParserUtils;

import java.util.Iterator;

import lombok.extern.slf4j.Slf4j;

/**
 * <p>Company: 科大讯飞股份有限公司-四川分公司</p>
 * <p>Description: 替换已上传的图片标签 </p>
 *
 * @author dong4j
 * @email sjdong3 @iflytek.com
 * @since 2019 -03-22 18:49
 */
@Slf4j
public class ImageLabelChangeHandler extends ActionHandlerAdapter {

    @Override
    public String getName() {
        return "标签替换";
    }

    @Override
    public boolean isEnabled(EventData data) {
        return STATE.isChangeToHtmlTag();
    }

    // @Override
    // public boolean execute(EventData data) {
    //     ProgressIndicator indicator = data.getIndicator();
    //     int size = data.getSize();
    //     int totalProcessed = 0;
    //
    //     for (Map.Entry<Document, List<MarkdownImage>> imageEntry : data.getWaitingProcessMap().entrySet()) {
    //         int totalCount = imageEntry.getValue().size();
    //         for (MarkdownImage markdownImage : imageEntry.getValue()) {
    //             indicator.setFraction(((++totalProcessed * 1.0) + data.getIndex() * size) / totalCount * size);
    //             String imageName = markdownImage.getImageName();
    //             indicator.setText2("Processing " + imageName);
    //
    //
    //             // 如果是本地类型, 则不替换
    //             if (markdownImage.getLocation().equals(ImageLocationEnum.LOCAL)) {
    //                 continue;
    //             }
    //
    //             // 如果标签已经与配置的类型一致, 则不替换
    //             ImageMarkEnum currentMarkType = markdownImage.getImageMarkType();
    //             if (STATE.getTagType().equals(currentMarkType.text)) {
    //                 continue;
    //             }
    //
    //             String finalMark;
    //             // 最后替换与配置不一致的标签
    //             String typeCode = STATE.getTagTypeCode();
    //             if (MikBundle.message("mik.change.mark.message").equals(typeCode)) {
    //                 finalMark = "自定义标签格式设置错误, 请重新设置";
    //             } else {
    //                 finalMark = ParserUtils.parse2(typeCode,
    //                                                markdownImage.getTitle(),
    //                                                markdownImage.getPath());
    //             }
    //             markdownImage.setFinalMark(finalMark);
    //
    //         }
    //     }
    //     return true;
    // }

    @Override
    public void invoke(Iterator<MarkdownImage> imageIterator, MarkdownImage markdownImage) {
        // 如果是本地类型, 则不替换
        if (markdownImage.getLocation().equals(ImageLocationEnum.LOCAL)) {
            return;
        }

        // 如果标签已经与配置的类型一致, 则不替换
        ImageMarkEnum currentMarkType = markdownImage.getImageMarkType();
        if (STATE.getTagType().equals(currentMarkType.text)) {
            return;
        }

        String finalMark;
        // 最后替换与配置不一致的标签
        String typeCode = STATE.getTagTypeCode();
        if (MikBundle.message("mik.change.mark.message").equals(typeCode)) {
            finalMark = "自定义标签格式设置错误, 请重新设置";
        } else {
            finalMark = ParserUtils.parse2(typeCode,
                                           markdownImage.getTitle(),
                                           markdownImage.getPath());
        }
        markdownImage.setFinalMark(finalMark);
    }
}