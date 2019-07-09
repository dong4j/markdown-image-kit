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

package info.dong4j.idea.plugin.enums;

import info.dong4j.idea.plugin.content.ImageContents;

import org.jetbrains.annotations.Contract;

/**
 * <p>Company: no company</p>
 * <p>Description: ${description}</p>
 *
 * @author dong4j
 * @date 2019-03-14 10:23
 * @email dong4j@gmail.com
 */
public enum ImageMarkEnum {
    LARGE_PICTURE(1, "点击看大图", ImageContents.LARG_IMAGE_MARK),
    COMMON_PICTURE(2, "正常的", ImageContents.COMMON_IMAGE_MARK),
    CUSTOM(3, "自定义", ""),
    ORIGINAL(4, "原始", ImageContents.DEFAULT_IMAGE_MARK);

    public int index;
    public String text;
    public String code;

    ImageMarkEnum(int index, String text, String code) {
        this.index = index;
        this.text = text;
        this.code = code;
    }

    @Contract(pure = true)
    public int getIndex() {
        return index;
    }

    @Contract(pure = true)
    public String getText() {
        return text;
    }

    @Contract(pure = true)
    public String getCode() {
        return code;
    }
}
