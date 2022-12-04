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

package info.dong4j.idea.plugin.util.digest;

import info.dong4j.idea.plugin.util.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * <p>Description: </p>
 *
 * @author dong4j
 * @version 1.0.0
 * @email "mailto:dong4j@gmail.com"
 * @date 2021.02.14 23:48
 * @since 1.1.0
 */
public final class HmacUtils {
    /** STREAM_BUFFER_LENGTH */
    private static final int STREAM_BUFFER_LENGTH = 1024;

    /**
     * Hmac utils
     *
     * @since 1.1.0
     */
    public HmacUtils() {
    }

    /**
     * Gets hmac md 5 *
     *
     * @param key key
     * @return the hmac md 5
     * @since 1.1.0
     */
    public static Mac getHmacMd5(byte[] key) {
        return getInitializedMac(HmacAlgorithms.HMAC_MD5, key);
    }

    /**
     * Gets hmac sha 1 *
     *
     * @param key key
     * @return the hmac sha 1
     * @since 1.1.0
     */
    public static Mac getHmacSha1(byte[] key) {
        return getInitializedMac(HmacAlgorithms.HMAC_SHA_1, key);
    }

    /**
     * Gets hmac sha 256 *
     *
     * @param key key
     * @return the hmac sha 256
     * @since 1.1.0
     */
    public static Mac getHmacSha256(byte[] key) {
        return getInitializedMac(HmacAlgorithms.HMAC_SHA_256, key);
    }

    /**
     * Gets hmac sha 384 *
     *
     * @param key key
     * @return the hmac sha 384
     * @since 1.1.0
     */
    public static Mac getHmacSha384(byte[] key) {
        return getInitializedMac(HmacAlgorithms.HMAC_SHA_384, key);
    }

    /**
     * Gets hmac sha 512 *
     *
     * @param key key
     * @return the hmac sha 512
     * @since 1.1.0
     */
    public static Mac getHmacSha512(byte[] key) {
        return getInitializedMac(HmacAlgorithms.HMAC_SHA_512, key);
    }

    /**
     * Gets initialized mac *
     *
     * @param algorithm algorithm
     * @param key       key
     * @return the initialized mac
     * @since 1.1.0
     */
    public static Mac getInitializedMac(HmacAlgorithms algorithm, byte[] key) {
        return getInitializedMac(algorithm.toString(), key);
    }

    /**
     * Gets initialized mac *
     *
     * @param algorithm algorithm
     * @param key       key
     * @return the initialized mac
     * @since 1.1.0
     */
    public static Mac getInitializedMac(String algorithm, byte[] key) {
        if (key == null) {
            throw new IllegalArgumentException("Null key");
        } else {
            try {
                SecretKeySpec keySpec = new SecretKeySpec(key, algorithm);
                Mac mac = Mac.getInstance(algorithm);
                mac.init(keySpec);
                return mac;
            } catch (NoSuchAlgorithmException var4) {
                throw new IllegalArgumentException(var4);
            } catch (InvalidKeyException var5) {
                throw new IllegalArgumentException(var5);
            }
        }
    }

    /**
     * Hmac md 5
     *
     * @param key           key
     * @param valueToDigest value to digest
     * @return the byte [ ]
     * @since 1.1.0
     */
    public static byte[] hmacMd5(byte[] key, byte[] valueToDigest) {
        try {
            return getHmacMd5(key).doFinal(valueToDigest);
        } catch (IllegalStateException var3) {
            throw new IllegalArgumentException(var3);
        }
    }

    /**
     * Hmac md 5
     *
     * @param key           key
     * @param valueToDigest value to digest
     * @return the byte [ ]
     * @throws IOException io exception
     * @since 1.1.0
     */
    public static byte[] hmacMd5(byte[] key, InputStream valueToDigest) throws IOException {
        return updateHmac(getHmacMd5(key), valueToDigest).doFinal();
    }

    /**
     * Hmac md 5
     *
     * @param key           key
     * @param valueToDigest value to digest
     * @return the byte [ ]
     * @since 1.1.0
     */
    public static byte[] hmacMd5(String key, String valueToDigest) {
        return hmacMd5(StringUtils.getBytesUtf8(key), StringUtils.getBytesUtf8(valueToDigest));
    }

    /**
     * Hmac md 5 hex
     *
     * @param key           key
     * @param valueToDigest value to digest
     * @return the string
     * @since 1.1.0
     */
    public static String hmacMd5Hex(byte[] key, byte[] valueToDigest) {
        return Hex.encodeHexString(hmacMd5(key, valueToDigest));
    }

    /**
     * Hmac md 5 hex
     *
     * @param key           key
     * @param valueToDigest value to digest
     * @return the string
     * @throws IOException io exception
     * @since 1.1.0
     */
    public static String hmacMd5Hex(byte[] key, InputStream valueToDigest) throws IOException {
        return Hex.encodeHexString(hmacMd5(key, valueToDigest));
    }

    /**
     * Hmac md 5 hex
     *
     * @param key           key
     * @param valueToDigest value to digest
     * @return the string
     * @since 1.1.0
     */
    public static String hmacMd5Hex(String key, String valueToDigest) {
        return Hex.encodeHexString(hmacMd5(key, valueToDigest));
    }

    /**
     * Hmac sha 1
     *
     * @param key           key
     * @param valueToDigest value to digest
     * @return the byte [ ]
     * @since 1.1.0
     */
    public static byte[] hmacSha1(byte[] key, byte[] valueToDigest) {
        try {
            return getHmacSha1(key).doFinal(valueToDigest);
        } catch (IllegalStateException var3) {
            throw new IllegalArgumentException(var3);
        }
    }

    /**
     * Hmac sha 1
     *
     * @param key           key
     * @param valueToDigest value to digest
     * @return the byte [ ]
     * @throws IOException io exception
     * @since 1.1.0
     */
    public static byte[] hmacSha1(byte[] key, InputStream valueToDigest) throws IOException {
        return updateHmac(getHmacSha1(key), valueToDigest).doFinal();
    }

    /**
     * Hmac sha 1
     *
     * @param key           key
     * @param valueToDigest value to digest
     * @return the byte [ ]
     * @since 1.1.0
     */
    public static byte[] hmacSha1(String key, String valueToDigest) {
        return hmacSha1(StringUtils.getBytesUtf8(key), StringUtils.getBytesUtf8(valueToDigest));
    }

    /**
     * Hmac sha 1 hex
     *
     * @param key           key
     * @param valueToDigest value to digest
     * @return the string
     * @since 1.1.0
     */
    public static String hmacSha1Hex(byte[] key, byte[] valueToDigest) {
        return Hex.encodeHexString(hmacSha1(key, valueToDigest));
    }

    /**
     * Hmac sha 1 hex
     *
     * @param key           key
     * @param valueToDigest value to digest
     * @return the string
     * @throws IOException io exception
     * @since 1.1.0
     */
    public static String hmacSha1Hex(byte[] key, InputStream valueToDigest) throws IOException {
        return Hex.encodeHexString(hmacSha1(key, valueToDigest));
    }

    /**
     * Hmac sha 1 hex
     *
     * @param key           key
     * @param valueToDigest value to digest
     * @return the string
     * @since 1.1.0
     */
    public static String hmacSha1Hex(String key, String valueToDigest) {
        return Hex.encodeHexString(hmacSha1(key, valueToDigest));
    }

    /**
     * Hmac sha 256
     *
     * @param key           key
     * @param valueToDigest value to digest
     * @return the byte [ ]
     * @since 1.1.0
     */
    public static byte[] hmacSha256(byte[] key, byte[] valueToDigest) {
        try {
            return getHmacSha256(key).doFinal(valueToDigest);
        } catch (IllegalStateException var3) {
            throw new IllegalArgumentException(var3);
        }
    }

    /**
     * Hmac sha 256
     *
     * @param key           key
     * @param valueToDigest value to digest
     * @return the byte [ ]
     * @throws IOException io exception
     * @since 1.1.0
     */
    public static byte[] hmacSha256(byte[] key, InputStream valueToDigest) throws IOException {
        return updateHmac(getHmacSha256(key), valueToDigest).doFinal();
    }

    /**
     * Hmac sha 256
     *
     * @param key           key
     * @param valueToDigest value to digest
     * @return the byte [ ]
     * @since 1.1.0
     */
    public static byte[] hmacSha256(String key, String valueToDigest) {
        return hmacSha256(StringUtils.getBytesUtf8(key), StringUtils.getBytesUtf8(valueToDigest));
    }

    /**
     * Hmac sha 256 hex
     *
     * @param key           key
     * @param valueToDigest value to digest
     * @return the string
     * @since 1.1.0
     */
    public static String hmacSha256Hex(byte[] key, byte[] valueToDigest) {
        return Hex.encodeHexString(hmacSha256(key, valueToDigest));
    }

    /**
     * Hmac sha 256 hex
     *
     * @param key           key
     * @param valueToDigest value to digest
     * @return the string
     * @throws IOException io exception
     * @since 1.1.0
     */
    public static String hmacSha256Hex(byte[] key, InputStream valueToDigest) throws IOException {
        return Hex.encodeHexString(hmacSha256(key, valueToDigest));
    }

    /**
     * Hmac sha 256 hex
     *
     * @param key           key
     * @param valueToDigest value to digest
     * @return the string
     * @since 1.1.0
     */
    public static String hmacSha256Hex(String key, String valueToDigest) {
        return Hex.encodeHexString(hmacSha256(key, valueToDigest));
    }

    /**
     * Hmac sha 384
     *
     * @param key           key
     * @param valueToDigest value to digest
     * @return the byte [ ]
     * @since 1.1.0
     */
    public static byte[] hmacSha384(byte[] key, byte[] valueToDigest) {
        try {
            return getHmacSha384(key).doFinal(valueToDigest);
        } catch (IllegalStateException var3) {
            throw new IllegalArgumentException(var3);
        }
    }

    /**
     * Hmac sha 384
     *
     * @param key           key
     * @param valueToDigest value to digest
     * @return the byte [ ]
     * @throws IOException io exception
     * @since 1.1.0
     */
    public static byte[] hmacSha384(byte[] key, InputStream valueToDigest) throws IOException {
        return updateHmac(getHmacSha384(key), valueToDigest).doFinal();
    }

    /**
     * Hmac sha 384
     *
     * @param key           key
     * @param valueToDigest value to digest
     * @return the byte [ ]
     * @since 1.1.0
     */
    public static byte[] hmacSha384(String key, String valueToDigest) {
        return hmacSha384(StringUtils.getBytesUtf8(key), StringUtils.getBytesUtf8(valueToDigest));
    }

    /**
     * Hmac sha 384 hex
     *
     * @param key           key
     * @param valueToDigest value to digest
     * @return the string
     * @since 1.1.0
     */
    public static String hmacSha384Hex(byte[] key, byte[] valueToDigest) {
        return Hex.encodeHexString(hmacSha384(key, valueToDigest));
    }

    /**
     * Hmac sha 384 hex
     *
     * @param key           key
     * @param valueToDigest value to digest
     * @return the string
     * @throws IOException io exception
     * @since 1.1.0
     */
    public static String hmacSha384Hex(byte[] key, InputStream valueToDigest) throws IOException {
        return Hex.encodeHexString(hmacSha384(key, valueToDigest));
    }

    /**
     * Hmac sha 384 hex
     *
     * @param key           key
     * @param valueToDigest value to digest
     * @return the string
     * @since 1.1.0
     */
    public static String hmacSha384Hex(String key, String valueToDigest) {
        return Hex.encodeHexString(hmacSha384(key, valueToDigest));
    }

    /**
     * Hmac sha 512
     *
     * @param key           key
     * @param valueToDigest value to digest
     * @return the byte [ ]
     * @since 1.1.0
     */
    public static byte[] hmacSha512(byte[] key, byte[] valueToDigest) {
        try {
            return getHmacSha512(key).doFinal(valueToDigest);
        } catch (IllegalStateException var3) {
            throw new IllegalArgumentException(var3);
        }
    }

    /**
     * Hmac sha 512
     *
     * @param key           key
     * @param valueToDigest value to digest
     * @return the byte [ ]
     * @throws IOException io exception
     * @since 1.1.0
     */
    public static byte[] hmacSha512(byte[] key, InputStream valueToDigest) throws IOException {
        return updateHmac(getHmacSha512(key), valueToDigest).doFinal();
    }

    /**
     * Hmac sha 512
     *
     * @param key           key
     * @param valueToDigest value to digest
     * @return the byte [ ]
     * @since 1.1.0
     */
    public static byte[] hmacSha512(String key, String valueToDigest) {
        return hmacSha512(StringUtils.getBytesUtf8(key), StringUtils.getBytesUtf8(valueToDigest));
    }

    /**
     * Hmac sha 512 hex
     *
     * @param key           key
     * @param valueToDigest value to digest
     * @return the string
     * @since 1.1.0
     */
    public static String hmacSha512Hex(byte[] key, byte[] valueToDigest) {
        return Hex.encodeHexString(hmacSha512(key, valueToDigest));
    }

    /**
     * Hmac sha 512 hex
     *
     * @param key           key
     * @param valueToDigest value to digest
     * @return the string
     * @throws IOException io exception
     * @since 1.1.0
     */
    public static String hmacSha512Hex(byte[] key, InputStream valueToDigest) throws IOException {
        return Hex.encodeHexString(hmacSha512(key, valueToDigest));
    }

    /**
     * Hmac sha 512 hex
     *
     * @param key           key
     * @param valueToDigest value to digest
     * @return the string
     * @since 1.1.0
     */
    public static String hmacSha512Hex(String key, String valueToDigest) {
        return Hex.encodeHexString(hmacSha512(key, valueToDigest));
    }

    /**
     * Update hmac
     *
     * @param mac           mac
     * @param valueToDigest value to digest
     * @return the mac
     * @since 1.1.0
     */
    public static Mac updateHmac(Mac mac, byte[] valueToDigest) {
        mac.reset();
        mac.update(valueToDigest);
        return mac;
    }

    /**
     * Update hmac
     *
     * @param mac           mac
     * @param valueToDigest value to digest
     * @return the mac
     * @throws IOException io exception
     * @since 1.1.0
     */
    public static Mac updateHmac(Mac mac, InputStream valueToDigest) throws IOException {
        mac.reset();
        byte[] buffer = new byte[1024];

        for (int read = valueToDigest.read(buffer, 0, 1024); read > -1; read = valueToDigest.read(buffer, 0, 1024)) {
            mac.update(buffer, 0, read);
        }

        return mac;
    }

    /**
     * Update hmac
     *
     * @param mac           mac
     * @param valueToDigest value to digest
     * @return the mac
     * @since 1.1.0
     */
    public static Mac updateHmac(Mac mac, String valueToDigest) {
        mac.reset();
        mac.update(StringUtils.getBytesUtf8(valueToDigest));
        return mac;
    }
}
