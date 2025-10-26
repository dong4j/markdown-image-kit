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
 * RSA 加密工具类
 * <p>
 * 提供基于 RSA 算法的加密功能，支持使用公钥对字符串进行加密操作。该工具类封装了 RSA 加密的完整流程，包括密钥解析、加密执行和字节数组转十六进制字符串等操作。
 * </p>
 *
 * @author dong4j
 * @version 0.0.1
 * @date 2019.03.17
 * @since 0.0.1
 */
public final class RSAEncodeUtils {
    /** 16 进制字符数组，用于将字节转换为十六进制字符串表示 */
    private static final char[] HEX_CHAR = {'0', '1', '2', '3', '4', '5',
                                            '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
    /** RSA 加密算法名称 */
    private static final String RSA_ALGORITHM = "RSA";

    /**
     * 使用公钥对字符串进行加密
     * <p>
     * 该方法通过给定的公钥和公钥指数，使用RSA算法对输入字符串进行加密，并返回加密后的十六进制字符串。
     *
     * @param toEncode 需要加密的字符串
     * @param pubKey   公钥的十六进制表示
     * @param pubExp   公钥指数的十六进制表示
     * @return 加密后的十六进制字符串
     * @throws NoSuchAlgorithmException  如果指定的算法不可用
     * @throws NoSuchPaddingException    如果指定的填充方式不可用
     * @throws InvalidKeySpecException   如果密钥规范无效
     * @throws InvalidKeyException       如果密钥无效
     * @throws BadPaddingException       如果填充无效
     * @throws IllegalBlockSizeException 如果块大小非法
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
     * 将字节数组转换为十六进制字符串
     * <p>
     * 该方法将输入的字节数组转换为对应的十六进制字符串表示形式，每个字节被转换为两个十六进制字符。
     *
     * @param bytes 需要转换的字节数组
     * @return 转换后的十六进制字符串
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
