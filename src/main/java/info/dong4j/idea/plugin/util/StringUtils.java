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

package info.dong4j.idea.plugin.util;

import java.nio.charset.Charset;

/**
 * <p>Company: 成都返空汇网络技术有限公司</p>
 * <p>Description:  </p>
 *
 * @author dong4j
 * @version 1.0.0
 * @email "mailto:dong4j@gmail.com"
 * @date 2021.02.14 22:45
 * @since 1.1.0
 */
public class StringUtils {

    /**
     * String utils
     *
     * @since 1.1.0
     */
    public StringUtils() {
    }

    /**
     * Is empty
     *
     * @param str str
     * @return the boolean
     * @since 1.1.0
     */
    public static boolean isEmpty(String str) {
        return str == null || str.length() == 0;
    }

    /**
     * Is not empty
     *
     * @param str str
     * @return the boolean
     * @since 1.1.0
     */
    public static boolean isNotEmpty(String str) {
        return !isEmpty(str);
    }

    /**
     * Is blank
     *
     * @param str str
     * @return the boolean
     * @since 1.1.0
     */
    public static boolean isBlank(String str) {
        int strLen;
        if (str != null && (strLen = str.length()) != 0) {
            for (int i = 0; i < strLen; ++i) {
                if (!Character.isWhitespace(str.charAt(i))) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Is not blank
     *
     * @param str str
     * @return the boolean
     * @since 1.1.0
     */
    public static boolean isNotBlank(String str) {
        return !isBlank(str);
    }

    /**
     * Trim
     *
     * @param str str
     * @return the string
     * @since 1.1.0
     */
    public static String trim(String str) {
        return str == null ? null : str.trim();
    }

    /**
     * Get bytes utf 8
     *
     * @param string string
     * @return the byte [ ]
     * @since 1.1.0
     */
    public static byte[] getBytesUtf8(String string) {
        return getBytes(string, Charsets.UTF_8);
    }

    /**
     * Get bytes
     *
     * @param string  string
     * @param charset charset
     * @return the byte [ ]
     * @since 1.1.0
     */
    private static byte[] getBytes(String string, Charset charset) {
        return string == null ? null : string.getBytes(charset);
    }

    /**
     * Default if empty
     *
     * @param str        str
     * @param defaultStr default str
     * @return the string
     * @since 1.6.0
     */
    public static String defaultIfEmpty(String str, String defaultStr) {
        return isEmpty(str) ? defaultStr : str;
    }

}
