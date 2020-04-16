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

import info.dong4j.idea.plugin.MikBundle;
import info.dong4j.idea.plugin.entity.EventData;
import info.dong4j.idea.plugin.entity.MarkdownImage;
import info.dong4j.idea.plugin.enums.ImageLocationEnum;
import info.dong4j.idea.plugin.enums.ImageMarkEnum;
import info.dong4j.idea.plugin.util.ParserUtils;

import java.util.Iterator;

import lombok.extern.slf4j.Slf4j;

/**
 * <p>Company: no company</p>
 * <p>Description: 替换已上传的图片标签 </p>
 *
 * @author dong4j
 * @email dong4j@gmail.com
 * @since 2019-03-22 18:49
 */
@Slf4j
public class ImageLabelChangeHandler extends ActionHandlerAdapter {
    private static final String MESSAGE = "自定义标签格式设置错误, 请重新设置";

    @Override
    public String getName() {
        return "标签替换";
    }

    @Override
    public boolean isEnabled(EventData data) {
        return STATE.isChangeToHtmlTag();
    }

    @Override
    public void invoke(EventData data, Iterator<MarkdownImage> imageIterator, MarkdownImage markdownImage) {
        // 如果是本地类型, 则不替换
        if (markdownImage.getLocation().equals(ImageLocationEnum.LOCAL)) {
            return;
        }

        // 只替换原始类型的标签, 避免全部替换(使用右键上传时, 根据类型替换为指定标签, 如果已经替换过则不处理)
        ImageMarkEnum currentMarkType = markdownImage.getImageMarkType();
        if(ImageMarkEnum.ORIGINAL.equals(currentMarkType)){
            change(markdownImage);
        }
    }

    /**
     * Change.
     *
     * @param markdownImage the markdown image
     */
    public static void change(MarkdownImage markdownImage) {
        String finalMark;
        // 最后替换与配置不一致的标签
        String typeCode = STATE.getTagTypeCode();
        if (MikBundle.message("mik.change.mark.message").equals(typeCode)) {
            finalMark = MESSAGE;
        } else {
            finalMark = ParserUtils.parse2(typeCode,
                                           markdownImage.getTitle(),
                                           markdownImage.getPath());
        }
        markdownImage.setFinalMark(finalMark);
    }
}
