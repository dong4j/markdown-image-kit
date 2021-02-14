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

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPublicKeySpec;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

/**
 * <p>Company: no company</p>
 * <p>Description: </p>
 *
 * @author dong4j
 * @version 0.0.1
 * @email "mailto:dong4j@gmail.com"
 * @date 2019.03.17 23:22
 * @since 0.0.1
 */
public final class RSAEncodeUtils {
    /** HEX_CHAR */
    private static final char[] HEX_CHAR = {'0', '1', '2', '3', '4', '5',
                                            '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

    /** RSA_ALGORITHM */
    private static final String RSA_ALGORITHM = "RSA";

    /**
     * Encode
     *
     * @param toEncode to encode
     * @param pubKey   pub key
     * @param pubExp   pub exp
     * @return the string
     * @throws NoSuchAlgorithmException  no such algorithm exception
     * @throws NoSuchPaddingException    no such padding exception
     * @throws InvalidKeySpecException   invalid key spec exception
     * @throws InvalidKeyException       invalid key exception
     * @throws BadPaddingException       bad padding exception
     * @throws IllegalBlockSizeException illegal block size exception
     * @since 0.0.1
     */
    @NotNull
    public static String encode(String toEncode, String pubKey, String pubExp)
        throws NoSuchAlgorithmException, NoSuchPaddingException,
               InvalidKeySpecException, InvalidKeyException, BadPaddingException,
               IllegalBlockSizeException {

        KeyFactory keyFactory = KeyFactory.getInstance(RSA_ALGORITHM);
        BigInteger modulus = new BigInteger(pubKey, 16);
        BigInteger publicExponent = new BigInteger(pubExp, 16);
        RSAPublicKeySpec rsaPublicKeySpec = new RSAPublicKeySpec(modulus, publicExponent);
        PublicKey publicKey = keyFactory.generatePublic(rsaPublicKeySpec);
        Cipher cipher = Cipher.getInstance(RSA_ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        byte[] encodeStr = cipher.doFinal(toEncode.getBytes());
        return bytesToHex(encodeStr);
    }

    /**
     * Bytes to hex
     *
     * @param bytes bytes
     * @return the string
     * @since 0.0.1
     */
    @NotNull
    @Contract(value = "_ -> new", pure = true)
    private static String bytesToHex(byte[] bytes) {
        char[] buf = new char[bytes.length * 2];
        int index = 0;
        for (byte b : bytes) {
            buf[index++] = HEX_CHAR[b >>> 4 & 0xf];
            buf[index++] = HEX_CHAR[b & 0xf];
        }
        return new String(buf);
    }
}
