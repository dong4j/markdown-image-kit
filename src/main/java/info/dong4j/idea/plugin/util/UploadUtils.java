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

package info.dong4j.idea.plugin.util;

import info.dong4j.idea.plugin.content.ImageContents;
import info.dong4j.idea.plugin.settings.ImageManagerPersistenComponent;
import info.dong4j.idea.plugin.settings.ImageManagerState;

import org.apache.commons.lang.StringUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * <p>Company: 科大讯飞股份有限公司-四川分公司</p>
 * <p>Description: </p>
 *
 * @author dong4j
 * @email sjdong3 @iflytek.com
 * @since 2019 -03-17 20:17
 */
@Slf4j
public class UploadUtils {
    private static ImageManagerState state = ImageManagerPersistenComponent.getInstance().getState();

    /**
     * 根据是否替换标签替换为最终的标签
     * 所有标签保持统一格式
     * [](./imgs/a.png)
     * ![](https://ws2.sinaimg.cn/large/a.jpg)
     * <a title='' href='https://ws2.sinaimg.cn/large/a.jpg' >![](https://ws2.sinaimg.cn/large/a.jpg)</a>
     * 未开启标签替换:
     * 1. [](./imgs/a.png) --> ![](https://ws2.sinaimg.cn/large/a.jpg)
     * 2. ![](https://ws2.sinaimg.cn/large/a.jpg) --> ![](https://ws2.sinaimg.cn/large/a.jpg)
     * 3. <a title='' href='https://ws2.sinaimg.cn/large/a.jpg' >![](https://ws2.sinaimg.cn/large/a.jpg)</a> -> <a title='' href='https://ws2.sinaimg.cn/large/a.jpg' >![](https://ws2.sinaimg.cn/large/a.jpg)</a>
     * 开启标签替换 (按照设置的标签格式替换):
     * 1. [](./imgs/a.png) --> <a title='' href='https://ws2.sinaimg.cn/large/a.jpg' >![](https://ws2.sinaimg.cn/large/a.jpg)</a>
     * 2. ![](https://ws2.sinaimg.cn/large/a.jpg) -> <a title='' href='https://ws2.sinaimg.cn/large/a.jpg' >![](https://ws2.sinaimg.cn/large/a.jpg)</a>
     * 3. 与设置的标签一样则不处理
     *
     * @param title     the title
     * @param imageUrl  the image url    上传后的 url, 有可能为 ""
     * @param original  the original     如果为 "", 则使用此字段
     * @param endString the end string
     * @return the final image mark
     */
    public static String getFinalImageMark(String title, String imageUrl, String original, String endString) {
        boolean isChangeToHtmlTag = ImageManagerPersistenComponent.getInstance().getState().isChangeToHtmlTag();
        // 处理 imageUrl 为空的情况
        imageUrl = StringUtils.isBlank(imageUrl) ? original : imageUrl;
        // 默认标签格式
        String newLineText = ParserUtils.parse0(ImageContents.DEFAULT_IMAGE_MARK,
                                                title,
                                                imageUrl);
        if (isChangeToHtmlTag) {
            newLineText = ParserUtils.parse0(state.getTagTypeCode(),
                                             title,
                                             imageUrl,
                                             title,
                                             imageUrl);
        }
        return newLineText + endString;
    }
}
