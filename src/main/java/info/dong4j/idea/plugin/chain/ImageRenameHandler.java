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

import info.dong4j.idea.plugin.entity.EventData;
import info.dong4j.idea.plugin.entity.MarkdownImage;
import info.dong4j.idea.plugin.enums.SuffixEnum;
import info.dong4j.idea.plugin.settings.MikPersistenComponent;
import info.dong4j.idea.plugin.settings.MikState;
import info.dong4j.idea.plugin.util.CharacterUtils;
import info.dong4j.idea.plugin.util.EnumsUtils;
import info.dong4j.idea.plugin.util.ImageUtils;

import org.apache.commons.lang3.time.DateFormatUtils;

import java.util.Date;
import java.util.Iterator;
import java.util.Optional;

import lombok.extern.slf4j.Slf4j;

/**
 * <p>Company: 科大讯飞股份有限公司-四川分公司</p>
 * <p>Description: 图片文件重命名</p>
 *
 * @author dong4j
 * @email sjdong3 @iflytek.com
 * @since 2019 -03-27 21:24
 */
@Slf4j
public class ImageRenameHandler extends ActionHandlerAdapter {

    private static final String PREFIX = "MIK-";

    @Override
    public String getName() {
        return "图片重命名";
    }

    @Override
    public boolean isEnabled(EventData data) {
        return STATE.isRename();
    }

    /**
     * 根据配置重新设置 imageName
     *
     * @param data the data
     * @return the boolean
     */
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
    //             markdownImage.setImageName(processFileName(imageName));
    //         }
    //     }
    //     return true;
    // }

    @Override
    public void invoke(EventData data, Iterator<MarkdownImage> imageIterator, MarkdownImage markdownImage) {

        String imageName = markdownImage.getImageName();
        MikState state = MikPersistenComponent.getInstance().getState();
        // 处理文件名有空格导致上传 gif 变为静态图的问题
        imageName = imageName.replaceAll("\\s*", "");
        int sufixIndex = state.getSuffixIndex();
        Optional<SuffixEnum> sufix = EnumsUtils.getEnumObject(SuffixEnum.class, e -> e.getIndex() == sufixIndex);
        SuffixEnum suffixEnum = sufix.orElse(SuffixEnum.FILE_NAME);
        switch (suffixEnum) {
            case FILE_NAME:
                break;
            case DATE_FILE_NAME:
                // 删除原来的时间前缀
                imageName = imageName.replace(DateFormatUtils.format(new Date(), "yyyy-MM-dd-"), "");
                imageName =  DateFormatUtils.format(new Date(), "yyyy-MM-dd-") + imageName;
                break;
            case RANDOM:
                if(!imageName.startsWith(PREFIX)){
                    imageName = PREFIX + CharacterUtils.getRandomString(6) + ImageUtils.getFileExtension(imageName);
                }
                break;
            default:
                break;
        }

        markdownImage.setImageName(imageName);
    }
}
