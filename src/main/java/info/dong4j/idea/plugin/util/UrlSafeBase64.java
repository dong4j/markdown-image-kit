/*
 * MIT License
 *
 * Copyright (c) 2022 dong4j <dong4j@gmail.com>
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

import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * URL安全的Base64编码和解码
 *
 * @author dong4j
 * @version 1.0.0
 * @email "mailto:dong4j@fkhwl.com"
 * @date 2021.02.18 22:35
 * @since 1.6.1
 */
public final class UrlSafeBase64 {

    /**
     * Url safe base 64
     *
     * @since 1.6.1
     */
    private UrlSafeBase64() {
    }   // don't instantiate

    /**
     * 编码字符串
     *
     * @param data 待编码字符串
     * @return 结果字符串 string
     * @since 1.6.1
     */
    public static String encodeToString(String data) {
        return encodeToString(data.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 编码数据
     *
     * @param data 字节数组
     * @return 结果字符串 string
     * @since 1.6.1
     */
    public static String encodeToString(byte[] data) {
        return Base64.getUrlEncoder().encodeToString(data);
    }

    /**
     * 解码数据
     *
     * @param data 编码过的字符串
     * @return 原始数据 byte [ ]
     * @since 1.6.1
     */
    public static byte[] decode(String data) {
        return Base64.getUrlDecoder().decode(data);
    }
}
