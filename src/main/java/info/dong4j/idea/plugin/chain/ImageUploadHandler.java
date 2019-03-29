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
import info.dong4j.idea.plugin.enums.ImageLocationEnum;
import info.dong4j.idea.plugin.enums.ImageMarkEnum;

import org.apache.commons.lang.StringUtils;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;

/**
 * <p>Company: 科大讯飞股份有限公司-四川分公司</p>
 * <p>Description: 图片上传操作</p>
 *
 * @author dong4j
 * @email sjdong3 @iflytek.com
 * @since 2019 -03-26 12:38
 */
@Slf4j
public class ImageUploadHandler extends BaseActionHandler {

    @Override
    public String getName() {
        return "图片上传";
    }

    @Override
    public boolean isEnabled(EventData data){
        // 如果开启
        return true;
    }

    /**
     * 只上传 location = LOCAL 的数据
     *
     * @param data the data
     * @return the boolean
     */
    @Override
    public boolean execute(EventData data) {
        ProgressIndicator indicator = data.getIndicator();
        int size = data.getSize();
        int totalProcessed = 0;

        for (Map.Entry<Document, List<MarkdownImage>> imageEntry : data.getWaitingProcessMap().entrySet()) {
            int totalCount = imageEntry.getValue().size();
            Iterator<MarkdownImage> imageIterator = imageEntry.getValue().iterator();
            while (imageIterator.hasNext()) {
                MarkdownImage markdownImage = imageIterator.next();

                indicator.setFraction(((++totalProcessed * 1.0) + data.getIndex() * size) / totalCount * size);
                String imageName = markdownImage.getImageName();
                indicator.setText2("Processing " + imageName);


                // 已上传过的不处理, 此时 finalmark 为 null, 替换是忽略
                if (ImageLocationEnum.NETWORK.equals(markdownImage.getLocation())) {
                    continue;
                }

                if(StringUtils.isBlank(imageName) || markdownImage.getInputStream() == null){
                    log.trace("inputstream 为 null 或者 imageName 为 null, remove markdownImage = {}", markdownImage);
                    imageIterator.remove();
                    continue;
                }

                String imageUrl = data.getClient().upload(markdownImage.getInputStream(), markdownImage.getImageName());
                indicator.setText2("Uploading " + imageName);
                if (StringUtils.isBlank(imageUrl)) {
                    imageUrl = "upload error";
                    markdownImage.setLocation(ImageLocationEnum.LOCAL);
                }
                String mark = "![](" + imageUrl + ")";
                markdownImage.setOriginalLineText(mark);
                markdownImage.setOriginalMark(mark);
                markdownImage.setPath(imageUrl);
                markdownImage.setLocation(ImageLocationEnum.NETWORK);
                markdownImage.setImageMarkType(ImageMarkEnum.ORIGINAL);
                markdownImage.setFinalMark(mark);
            }
        }
        return true;
    }
}
