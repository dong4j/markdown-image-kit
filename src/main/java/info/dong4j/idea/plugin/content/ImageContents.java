/*
 * MIT License
 *
 * Copyright (c) 2019 dong4j
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

package info.dong4j.idea.plugin.content;

/**
 * <p>Company: 科大讯飞股份有限公司-四川分公司</p>
 * <p>Description: </p>
 *
 * @author dong4j
 * @email sjdong3@iflytek.com
 * @since 2019-03-15 18:29
 */
public interface ImageContents {
    String IMAGE_TYPE_NAME = "Image";
    String HTML_TAG_A_START = "<a";
    String HTML_TAG_A_END = "a>";
    String IMAGE_MARK_PREFIX = "![";
    String IMAGE_MARK_MIDDLE = "](";
    String IMAGE_MARK_SUFFIX = ")";
    String IMAGE_LOCATION = "http";
    String LINE_BREAK = "\n";
    /** 默认的 image 标签替换类型 */
    String DEFAULT_IMAGE_MARK = "![${}](${})";
    /** 点击查看大图, 需要添加 js 支持 */
    String LARG_IMAGE_MARK = "<a data-fancybox title='${}' href='${}' >" + DEFAULT_IMAGE_MARK + "</a>";
    String LARG_IMAGE_MARK_ID = LARG_IMAGE_MARK.substring(1, 23);
    /** 就一个 a 标签, 点击能在新页面查看图片 */
    String COMMON_IMAGE_MARK = "<a title='${}' href='${}' >" + DEFAULT_IMAGE_MARK + "</a>";
    String COMMON_IMAGE_MARK_ID = COMMON_IMAGE_MARK.substring(1, 9);
}
