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

package info.dong4j.idea.plugin.entity;

import info.dong4j.idea.plugin.enums.ImageLocationEnum;
import info.dong4j.idea.plugin.enums.ImageMarkEnum;

import java.io.*;

import lombok.Data;

/**
 * <p>Company: 科大讯飞股份有限公司-四川分公司</p>
 * <p>Description: </p>
 *
 * @author dong4j
 * @email sjdong3@iflytek.com
 * @since 2019-03-14 21:00
 */
@Data
public class MarkdownImage implements Serializable {
    private static final long serialVersionUID = -533088989259774894L;

    private String fileName;
    /** 原始文本 */
    private String originalLineText;
    /** 行数 */
    private int lineNumber;
    /** 行第一个字符偏移量 */
    private int lineStartOffset;
    /** 行最后一个字符偏移量 */
    private int lineEndOffset;
    /** 图片标题 */
    private String title;
    /** 图片地址 (如果是本地, 就是 fileName, 如果是网络, 就是 http/https 地址) */
    private String path;
    /** 图片位置 */
    private ImageLocationEnum location = ImageLocationEnum.LOCAL;
    /** 上传后的 url */
    private String uploadedUrl;
    /** 原始文本标签类型 */
    private ImageMarkEnum imageMarkType = ImageMarkEnum.ORIGINAL;
}
