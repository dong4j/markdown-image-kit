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

//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package info.dong4j.idea.plugin.util.digest;

/**
 * <p>Description: </p>
 *
 * @author dong4j
 * @version 1.0.0
 * @email "mailto:dong4j@gmail.com"
 * @date 2021.02.14 23:45
 * @since 1.1.0
 */
public enum HmacAlgorithms {
    /** Hmac md 5 hmac algorithms */
    HMAC_MD5("HmacMD5"),
    /** Hmac sha 1 hmac algorithms */
    HMAC_SHA_1("HmacSHA1"),
    /** Hmac sha 256 hmac algorithms */
    HMAC_SHA_256("HmacSHA256"),
    /** Hmac sha 384 hmac algorithms */
    HMAC_SHA_384("HmacSHA384"),
    /** Hmac sha 512 hmac algorithms */
    HMAC_SHA_512("HmacSHA512");

    /** Algorithm */
    private final String algorithm;

    /**
     * Hmac algorithms
     *
     * @param algorithm algorithm
     * @since 1.1.0
     */
    HmacAlgorithms(String algorithm) {
        this.algorithm = algorithm;
    }

    /**
     * To string
     *
     * @return the string
     * @since 1.1.0
     */
    @Override
    public String toString() {
        return this.algorithm;
    }
}
