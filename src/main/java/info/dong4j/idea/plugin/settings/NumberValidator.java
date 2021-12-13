/*
 * MIT License
 *
 * Copyright (c) 2021 dong4j <dong4j@gmail.com>
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
 */

package info.dong4j.idea.plugin.settings;

import javax.swing.text.AttributeSet;
import javax.swing.text.PlainDocument;

/**
 * <p>Company: no company</p>
 * <p>Description: 限制输入数字的位数</p>
 *
 * @author dong4j
 * @version 0.0.1
 * @email "mailto:dong4j@gmail.com"
 * @date 2019.03.14 20:00
 * @since 0.0.1
 */
public class NumberValidator extends PlainDocument {

    /** serialVersionUID */
    private static final long serialVersionUID = 1L;

    /** Limit */
    private final int limit;

    /**
     * Number validator
     *
     * @param limit limit
     * @since 0.0.1
     */
    public NumberValidator(int limit) {
        super();
        this.limit = limit;
    }

    /**
     * Insert string
     *
     * @param offset offset
     * @param str    str
     * @param attr   attr
     * @since 0.0.1
     */
    @Override
    public void insertString(int offset, String str, AttributeSet attr)
        throws javax.swing.text.BadLocationException {
        if (str == null) {
            return;
        }
        if ((this.getLength() + str.length()) <= this.limit) {
            char[] upper = str.toCharArray();
            int length = 0;
            for (int i = 0; i < upper.length; i++) {
                if (upper[i] >= '0' && upper[i] <= '9') {
                    upper[length++] = upper[i];
                }
            }
            // 插入数字
            super.insertString(offset, new String(upper, 0, length), attr);
        }
    }

}
