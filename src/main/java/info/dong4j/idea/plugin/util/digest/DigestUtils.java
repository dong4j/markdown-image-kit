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

package info.dong4j.idea.plugin.util.digest;

import info.dong4j.idea.plugin.util.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * <p>Company: 成都返空汇网络技术有限公司 </p>
 * <p>Description: </p>
 *
 * @author dong4j
 * @version 1.0.0
 * @email "mailto:dong4j@gmail.com"
 * @date 2021.02.14 23:48
 * @since 1.1.0
 */
public class DigestUtils {
    /** STREAM_BUFFER_LENGTH */
    private static final int STREAM_BUFFER_LENGTH = 1024;

    /**
     * Digest utils
     *
     * @since 1.1.0
     */
    public DigestUtils() {
    }

    /**
     * Digest
     *
     * @param digest digest
     * @param data   data
     * @return the byte [ ]
     * @throws IOException io exception
     * @since 1.1.0
     */
    private static byte[] digest(MessageDigest digest, InputStream data) throws IOException {
        return updateDigest(digest, data).digest();
    }

    /**
     * Gets digest *
     *
     * @param algorithm algorithm
     * @return the digest
     * @since 1.1.0
     */
    public static MessageDigest getDigest(String algorithm) {
        try {
            return MessageDigest.getInstance(algorithm);
        } catch (NoSuchAlgorithmException var2) {
            throw new IllegalArgumentException(var2);
        }
    }

    /**
     * Gets md 2 digest *
     *
     * @return the md 2 digest
     * @since 1.1.0
     */
    public static MessageDigest getMd2Digest() {
        return getDigest("MD2");
    }

    /**
     * Gets md 5 digest *
     *
     * @return the md 5 digest
     * @since 1.1.0
     */
    public static MessageDigest getMd5Digest() {
        return getDigest("MD5");
    }

    /**
     * Gets sha 1 digest *
     *
     * @return the sha 1 digest
     * @since 1.1.0
     */
    public static MessageDigest getSha1Digest() {
        return getDigest("SHA-1");
    }

    /**
     * Gets sha 256 digest *
     *
     * @return the sha 256 digest
     * @since 1.1.0
     */
    public static MessageDigest getSha256Digest() {
        return getDigest("SHA-256");
    }

    /**
     * Gets sha 384 digest *
     *
     * @return the sha 384 digest
     * @since 1.1.0
     */
    public static MessageDigest getSha384Digest() {
        return getDigest("SHA-384");
    }

    /**
     * Gets sha 512 digest *
     *
     * @return the sha 512 digest
     * @since 1.1.0
     */
    public static MessageDigest getSha512Digest() {
        return getDigest("SHA-512");
    }

    /**
     * Gets sha digest *
     *
     * @return the sha digest
     * @since 1.1.0
     * @deprecated
     */
    @Deprecated
    public static MessageDigest getShaDigest() {
        return getSha1Digest();
    }

    /**
     * Md 2
     *
     * @param data data
     * @return the byte [ ]
     * @since 1.1.0
     */
    public static byte[] md2(byte[] data) {
        return getMd2Digest().digest(data);
    }

    /**
     * Md 2
     *
     * @param data data
     * @return the byte [ ]
     * @throws IOException io exception
     * @since 1.1.0
     */
    public static byte[] md2(InputStream data) throws IOException {
        return digest(getMd2Digest(), data);
    }

    /**
     * Md 2
     *
     * @param data data
     * @return the byte [ ]
     * @since 1.1.0
     */
    public static byte[] md2(String data) {
        return md2(StringUtils.getBytesUtf8(data));
    }

    /**
     * Md 2 hex
     *
     * @param data data
     * @return the string
     * @since 1.1.0
     */
    public static String md2Hex(byte[] data) {
        return Hex.encodeHexString(md2(data));
    }

    /**
     * Md 2 hex
     *
     * @param data data
     * @return the string
     * @throws IOException io exception
     * @since 1.1.0
     */
    public static String md2Hex(InputStream data) throws IOException {
        return Hex.encodeHexString(md2(data));
    }

    /**
     * Md 2 hex
     *
     * @param data data
     * @return the string
     * @since 1.1.0
     */
    public static String md2Hex(String data) {
        return Hex.encodeHexString(md2(data));
    }

    /**
     * Md 5
     *
     * @param data data
     * @return the byte [ ]
     * @since 1.1.0
     */
    public static byte[] md5(byte[] data) {
        return getMd5Digest().digest(data);
    }

    /**
     * Md 5
     *
     * @param data data
     * @return the byte [ ]
     * @throws IOException io exception
     * @since 1.1.0
     */
    public static byte[] md5(InputStream data) throws IOException {
        return digest(getMd5Digest(), data);
    }

    /**
     * Md 5
     *
     * @param data data
     * @return the byte [ ]
     * @since 1.1.0
     */
    public static byte[] md5(String data) {
        return md5(StringUtils.getBytesUtf8(data));
    }

    /**
     * Md 5 hex
     *
     * @param data data
     * @return the string
     * @since 1.1.0
     */
    public static String md5Hex(byte[] data) {
        return Hex.encodeHexString(md5(data));
    }

    /**
     * Md 5 hex
     *
     * @param data data
     * @return the string
     * @throws IOException io exception
     * @since 1.1.0
     */
    public static String md5Hex(InputStream data) throws IOException {
        return Hex.encodeHexString(md5(data));
    }

    /**
     * Md 5 hex
     *
     * @param data data
     * @return the string
     * @since 1.1.0
     */
    public static String md5Hex(String data) {
        return Hex.encodeHexString(md5(data));
    }

    /**
     * Sha
     *
     * @param data data
     * @return the byte [ ]
     * @since 1.1.0
     * @deprecated
     */
    @Deprecated
    public static byte[] sha(byte[] data) {
        return sha1(data);
    }

    /**
     * Sha
     *
     * @param data data
     * @return the byte [ ]
     * @throws IOException io exception
     * @since 1.1.0
     * @deprecated
     */
    @Deprecated
    public static byte[] sha(InputStream data) throws IOException {
        return sha1(data);
    }

    /**
     * Sha
     *
     * @param data data
     * @return the byte [ ]
     * @since 1.1.0
     * @deprecated
     */
    @Deprecated
    public static byte[] sha(String data) {
        return sha1(data);
    }

    /**
     * Sha 1
     *
     * @param data data
     * @return the byte [ ]
     * @since 1.1.0
     */
    public static byte[] sha1(byte[] data) {
        return getSha1Digest().digest(data);
    }

    /**
     * Sha 1
     *
     * @param data data
     * @return the byte [ ]
     * @throws IOException io exception
     * @since 1.1.0
     */
    public static byte[] sha1(InputStream data) throws IOException {
        return digest(getSha1Digest(), data);
    }

    /**
     * Sha 1
     *
     * @param data data
     * @return the byte [ ]
     * @since 1.1.0
     */
    public static byte[] sha1(String data) {
        return sha1(StringUtils.getBytesUtf8(data));
    }

    /**
     * Sha 1 hex
     *
     * @param data data
     * @return the string
     * @since 1.1.0
     */
    public static String sha1Hex(byte[] data) {
        return Hex.encodeHexString(sha1(data));
    }

    /**
     * Sha 1 hex
     *
     * @param data data
     * @return the string
     * @throws IOException io exception
     * @since 1.1.0
     */
    public static String sha1Hex(InputStream data) throws IOException {
        return Hex.encodeHexString(sha1(data));
    }

    /**
     * Sha 1 hex
     *
     * @param data data
     * @return the string
     * @since 1.1.0
     */
    public static String sha1Hex(String data) {
        return Hex.encodeHexString(sha1(data));
    }

    /**
     * Sha 256
     *
     * @param data data
     * @return the byte [ ]
     * @since 1.1.0
     */
    public static byte[] sha256(byte[] data) {
        return getSha256Digest().digest(data);
    }

    /**
     * Sha 256
     *
     * @param data data
     * @return the byte [ ]
     * @throws IOException io exception
     * @since 1.1.0
     */
    public static byte[] sha256(InputStream data) throws IOException {
        return digest(getSha256Digest(), data);
    }

    /**
     * Sha 256
     *
     * @param data data
     * @return the byte [ ]
     * @since 1.1.0
     */
    public static byte[] sha256(String data) {
        return sha256(StringUtils.getBytesUtf8(data));
    }

    /**
     * Sha 256 hex
     *
     * @param data data
     * @return the string
     * @since 1.1.0
     */
    public static String sha256Hex(byte[] data) {
        return Hex.encodeHexString(sha256(data));
    }

    /**
     * Sha 256 hex
     *
     * @param data data
     * @return the string
     * @throws IOException io exception
     * @since 1.1.0
     */
    public static String sha256Hex(InputStream data) throws IOException {
        return Hex.encodeHexString(sha256(data));
    }

    /**
     * Sha 256 hex
     *
     * @param data data
     * @return the string
     * @since 1.1.0
     */
    public static String sha256Hex(String data) {
        return Hex.encodeHexString(sha256(data));
    }

    /**
     * Sha 384
     *
     * @param data data
     * @return the byte [ ]
     * @since 1.1.0
     */
    public static byte[] sha384(byte[] data) {
        return getSha384Digest().digest(data);
    }

    /**
     * Sha 384
     *
     * @param data data
     * @return the byte [ ]
     * @throws IOException io exception
     * @since 1.1.0
     */
    public static byte[] sha384(InputStream data) throws IOException {
        return digest(getSha384Digest(), data);
    }

    /**
     * Sha 384
     *
     * @param data data
     * @return the byte [ ]
     * @since 1.1.0
     */
    public static byte[] sha384(String data) {
        return sha384(StringUtils.getBytesUtf8(data));
    }

    /**
     * Sha 384 hex
     *
     * @param data data
     * @return the string
     * @since 1.1.0
     */
    public static String sha384Hex(byte[] data) {
        return Hex.encodeHexString(sha384(data));
    }

    /**
     * Sha 384 hex
     *
     * @param data data
     * @return the string
     * @throws IOException io exception
     * @since 1.1.0
     */
    public static String sha384Hex(InputStream data) throws IOException {
        return Hex.encodeHexString(sha384(data));
    }

    /**
     * Sha 384 hex
     *
     * @param data data
     * @return the string
     * @since 1.1.0
     */
    public static String sha384Hex(String data) {
        return Hex.encodeHexString(sha384(data));
    }

    /**
     * Sha 512
     *
     * @param data data
     * @return the byte [ ]
     * @since 1.1.0
     */
    public static byte[] sha512(byte[] data) {
        return getSha512Digest().digest(data);
    }

    /**
     * Sha 512
     *
     * @param data data
     * @return the byte [ ]
     * @throws IOException io exception
     * @since 1.1.0
     */
    public static byte[] sha512(InputStream data) throws IOException {
        return digest(getSha512Digest(), data);
    }

    /**
     * Sha 512
     *
     * @param data data
     * @return the byte [ ]
     * @since 1.1.0
     */
    public static byte[] sha512(String data) {
        return sha512(StringUtils.getBytesUtf8(data));
    }

    /**
     * Sha 512 hex
     *
     * @param data data
     * @return the string
     * @since 1.1.0
     */
    public static String sha512Hex(byte[] data) {
        return Hex.encodeHexString(sha512(data));
    }

    /**
     * Sha 512 hex
     *
     * @param data data
     * @return the string
     * @throws IOException io exception
     * @since 1.1.0
     */
    public static String sha512Hex(InputStream data) throws IOException {
        return Hex.encodeHexString(sha512(data));
    }

    /**
     * Sha 512 hex
     *
     * @param data data
     * @return the string
     * @since 1.1.0
     */
    public static String sha512Hex(String data) {
        return Hex.encodeHexString(sha512(data));
    }

    /**
     * Sha hex
     *
     * @param data data
     * @return the string
     * @since 1.1.0
     * @deprecated
     */
    @Deprecated
    public static String shaHex(byte[] data) {
        return sha1Hex(data);
    }

    /**
     * Sha hex
     *
     * @param data data
     * @return the string
     * @throws IOException io exception
     * @since 1.1.0
     * @deprecated
     */
    @Deprecated
    public static String shaHex(InputStream data) throws IOException {
        return sha1Hex(data);
    }

    /**
     * Sha hex
     *
     * @param data data
     * @return the string
     * @since 1.1.0
     * @deprecated
     */
    @Deprecated
    public static String shaHex(String data) {
        return sha1Hex(data);
    }

    /**
     * Update digest
     *
     * @param messageDigest message digest
     * @param valueToDigest value to digest
     * @return the message digest
     * @since 1.1.0
     */
    public static MessageDigest updateDigest(MessageDigest messageDigest, byte[] valueToDigest) {
        messageDigest.update(valueToDigest);
        return messageDigest;
    }

    /**
     * Update digest
     *
     * @param digest digest
     * @param data   data
     * @return the message digest
     * @throws IOException io exception
     * @since 1.1.0
     */
    public static MessageDigest updateDigest(MessageDigest digest, InputStream data) throws IOException {
        byte[] buffer = new byte[1024];

        for (int read = data.read(buffer, 0, 1024); read > -1; read = data.read(buffer, 0, 1024)) {
            digest.update(buffer, 0, read);
        }

        return digest;
    }

    /**
     * Update digest
     *
     * @param messageDigest message digest
     * @param valueToDigest value to digest
     * @return the message digest
     * @since 1.1.0
     */
    public static MessageDigest updateDigest(MessageDigest messageDigest, String valueToDigest) {
        messageDigest.update(StringUtils.getBytesUtf8(valueToDigest));
        return messageDigest;
    }
}
