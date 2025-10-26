package info.dong4j.idea.plugin.util.digest;

import info.dong4j.idea.plugin.util.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * HMAC 工具类
 * <p>
 * 提供多种 HMAC 算法的加密功能，包括 MD5、SHA1、SHA256、SHA384 和 SHA512 等算法的加密和十六进制格式输出。
 * 支持对字节数组、输入流和字符串进行加密操作，适用于需要数据完整性校验和身份认证的场景。
 *
 * @author dong4j
 * @version 1.0.0
 * @date 2021.02.14
 * @since 1.1.0
 */
public final class HmacUtils {
    /** 流处理缓冲区长度，用于控制数据读取和写入的缓冲大小 */
    private static final int STREAM_BUFFER_LENGTH = 1024;

    /**
     * Hmac 工具类的构造函数
     * <p>
     * 初始化 Hmac 工具类，用于提供 Hmac 相关的实用方法
     *
     * @since 1.1.0
     */
    public HmacUtils() {
    }

    /**
     * 获取HMAC MD5算法的Mac实例
     * <p>
     * 使用指定的密钥初始化并返回一个HMAC MD5算法的Mac对象，用于生成消息认证码。
     *
     * @param key 密钥字节数组
     * @return HMAC MD5算法的Mac实例
     * @since 1.1.0
     */
    public static Mac getHmacMd5(byte[] key) {
        return getInitializedMac(HmacAlgorithms.HMAC_MD5, key);
    }

    /**
     * 获取HMAC SHA1算法的Mac实例
     * <p>
     * 根据提供的密钥初始化并返回一个HMAC SHA1算法的Mac对象
     *
     * @param key 密钥字节数组
     * @return HMAC SHA1算法的Mac实例
     * @since 1.1.0
     */
    public static Mac getHmacSha1(byte[] key) {
        return getInitializedMac(HmacAlgorithms.HMAC_SHA_1, key);
    }

    /**
     * 获取HMAC SHA256算法的Mac实例
     * <p>
     * 根据给定的密钥初始化并返回一个使用HMAC SHA256算法的Mac对象
     *
     * @param key 密钥字节数组
     * @return HMAC SHA256算法的Mac实例
     * @since 1.1.0
     */
    public static Mac getHmacSha256(byte[] key) {
        return getInitializedMac(HmacAlgorithms.HMAC_SHA_256, key);
    }

    /**
     * 获取HMAC-SHA384算法的Mac实例
     * <p>
     * 根据提供的密钥初始化并返回HMAC-SHA384算法的Mac对象
     *
     * @param key 密钥字节数组
     * @return HMAC-SHA384算法的Mac实例
     * @since 1.1.0
     */
    public static Mac getHmacSha384(byte[] key) {
        return getInitializedMac(HmacAlgorithms.HMAC_SHA_384, key);
    }

    /**
     * 获取HMAC SHA512算法的Mac实例
     * <p>
     * 使用给定的密钥初始化并返回一个HMAC SHA512算法的Mac对象，用于生成消息认证码。
     *
     * @param key 密钥字节数组，用于初始化Mac实例
     * @return HMAC SHA512算法的Mac实例
     * @since 1.1.0
     */
    public static Mac getHmacSha512(byte[] key) {
        return getInitializedMac(HmacAlgorithms.HMAC_SHA_512, key);
    }

    /**
     * 获取初始化的MAC对象
     * <p>
     * 根据指定的算法和密钥初始化并返回一个MAC对象
     *
     * @param algorithm 算法类型，使用HmacAlgorithms枚举定义
     * @param key       密钥字节数组
     * @return 初始化后的MAC对象
     * @since 1.1.0
     */
    public static Mac getInitializedMac(HmacAlgorithms algorithm, byte[] key) {
        return getInitializedMac(algorithm.toString(), key);
    }

    /**
     * 根据算法和密钥初始化一个 MAC 对象
     * <p>
     * 该方法使用指定的算法和密钥创建并初始化一个 MAC 实例，用于后续的加密操作。
     *
     * @param algorithm 算法名称，如 "HmacSHA256"
     * @param key       密钥字节数组
     * @return 初始化后的 MAC 实例
     * @throws IllegalArgumentException 如果密钥为 null 或算法不可用时抛出
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
     * 使用 HMAC MD5 算法对给定值进行加密处理
     * <p>
     * 该方法通过指定的密钥生成 HMAC MD5 实例，并使用其对输入的字节数组进行加密，返回加密后的字节数组。
     *
     * @param key           密钥，用于生成 HMAC 实例
     * @param valueToDigest 需要加密的字节数组
     * @return 加密后的字节数组
     * @throws IllegalArgumentException 如果 HMAC MD5 初始化过程中发生异常
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
     * 使用 HMAC MD5 算法对输入流进行加密处理
     * <p>
     * 该方法使用指定的密钥对输入流中的数据进行 HMAC MD5 加密，并返回加密后的字节数组。
     *
     * @param key           密钥字节数组，用于加密计算
     * @param valueToDigest 需要加密的输入流
     * @return 加密后的字节数组
     * @throws IOException 如果加密过程中发生输入输出异常
     * @since 1.1.0
     */
    public static byte[] hmacMd5(byte[] key, InputStream valueToDigest) throws IOException {
        return updateHmac(getHmacMd5(key), valueToDigest).doFinal();
    }

    /**
     * 使用 HMAC MD5 算法对字符串进行加密处理
     * <p>
     * 该方法使用指定的密钥和待加密字符串生成 HMAC MD5 哈希值，并返回字节数组形式的结果
     *
     * @param key           密钥，用于加密的字符串
     * @param valueToDigest 需要加密的字符串内容
     * @return 加密后的字节数组
     * @since 1.1.0
     */
    public static byte[] hmacMd5(String key, String valueToDigest) {
        return hmacMd5(StringUtils.getBytesUtf8(key), StringUtils.getBytesUtf8(valueToDigest));
    }

    /**
     * 使用HMAC MD5算法生成十六进制字符串
     * <p>
     * 该方法对给定的值进行HMAC MD5加密，使用提供的密钥，并将结果转换为十六进制格式字符串。
     *
     * @param key           加密所使用的密钥
     * @param valueToDigest 需要加密的数据
     * @return 使用HMAC MD5加密后的十六进制字符串
     * @since 1.1.0
     */
    public static String hmacMd5Hex(byte[] key, byte[] valueToDigest) {
        return Hex.encodeHexString(hmacMd5(key, valueToDigest));
    }

    /**
     * 使用 HMAC MD5 算法对输入流进行加密并返回十六进制字符串
     * <p>
     * 该方法使用指定的密钥对输入流中的数据进行 HMAC MD5 加密处理，并将结果转换为十六进制字符串格式。
     *
     * @param key           密钥，用于加密计算
     * @param valueToDigest 需要加密的输入流
     * @return 加密后的十六进制字符串
     * @throws IOException 如果加密过程中发生输入输出异常
     * @since 1.1.0
     */
    public static String hmacMd5Hex(byte[] key, InputStream valueToDigest) throws IOException {
        return Hex.encodeHexString(hmacMd5(key, valueToDigest));
    }

    /**
     * 生成HMAC MD5的十六进制字符串
     * <p>
     * 使用指定的密钥对输入值进行HMAC MD5加密，并将结果转换为十六进制字符串格式返回
     *
     * @param key           加密使用的密钥
     * @param valueToDigest 需要加密的输入值
     * @return HMAC MD5加密后的十六进制字符串
     * @since 1.1.0
     */
    public static String hmacMd5Hex(String key, String valueToDigest) {
        return Hex.encodeHexString(hmacMd5(key, valueToDigest));
    }

    /**
     * 使用 HMAC-SHA1 算法对数据进行加密处理
     * <p>
     * 该方法使用指定的密钥对输入数据进行 HMAC-SHA1 加密，并返回加密后的字节数组。
     *
     * @param key           密钥字节数组
     * @param valueToDigest 需要加密的数据字节数组
     * @return 加密后的字节数组
     * @throws IllegalArgumentException 如果加密过程中发生异常
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
     * 使用 HMAC-SHA1 算法对输入流数据进行加密处理
     * <p>
     * 该方法使用指定的密钥对输入流中的数据进行 HMAC-SHA1 加密，并返回加密后的字节数组。
     *
     * @param key           密钥字节数组
     * @param valueToDigest 需要加密的输入流
     * @return 加密后的字节数组
     * @throws IOException 如果加密过程中发生IO异常
     * @since 1.1.0
     */
    public static byte[] hmacSha1(byte[] key, InputStream valueToDigest) throws IOException {
        return updateHmac(getHmacSha1(key), valueToDigest).doFinal();
    }

    /**
     * 使用 HMAC-SHA1 算法对字符串进行加密处理
     * <p>
     * 该方法接收一个密钥和一个需要加密的字符串，使用 UTF-8 编码转换为字节数组后，调用 hmacSha1 方法进行加密
     *
     * @param key           密钥，用于加密的字符串
     * @param valueToDigest 需要加密的字符串值
     * @return 加密后的字节数组
     * @since 1.1.0
     */
    public static byte[] hmacSha1(String key, String valueToDigest) {
        return hmacSha1(StringUtils.getBytesUtf8(key), StringUtils.getBytesUtf8(valueToDigest));
    }

    /**
     * 使用 HMAC-SHA1 算法对字节数组进行加密并返回十六进制字符串
     * <p>
     * 该方法使用指定的密钥和待加密数据生成 HMAC-SHA1 哈希值，并将结果转换为十六进制字符串格式。
     *
     * @param key           密钥字节数组
     * @param valueToDigest 待加密的字节数组
     * @return HMAC-SHA1 哈希值的十六进制字符串
     * @since 1.1.0
     */
    public static String hmacSha1Hex(byte[] key, byte[] valueToDigest) {
        return Hex.encodeHexString(hmacSha1(key, valueToDigest));
    }

    /**
     * 使用HMAC SHA-1算法对输入流进行加密并返回十六进制字符串
     * <p>
     * 该方法首先使用HMAC SHA-1算法对给定的输入流进行加密处理，然后将加密结果转换为十六进制字符串格式返回。
     *
     * @param key           加密使用的密钥
     * @param valueToDigest 需要加密的输入流
     * @return 加密后的十六进制字符串
     * @throws IOException 如果加密或处理过程中发生I/O异常
     * @since 1.1.0
     */
    public static String hmacSha1Hex(byte[] key, InputStream valueToDigest) throws IOException {
        return Hex.encodeHexString(hmacSha1(key, valueToDigest));
    }

    /**
     * 使用 HMAC-SHA1 算法对字符串进行加密并返回十六进制格式的结果
     * <p>
     * 该方法使用指定的密钥和待加密字符串，通过 HMAC-SHA1 算法生成加密结果，并将其转换为十六进制字符串格式返回。
     *
     * @param key           密钥，用于加密操作
     * @param valueToDigest 需要加密的字符串值
     * @return 返回 HMAC-SHA1 加密后的十六进制字符串
     * @since 1.1.0
     */
    public static String hmacSha1Hex(String key, String valueToDigest) {
        return Hex.encodeHexString(hmacSha1(key, valueToDigest));
    }

    /**
     * 使用 HMAC-SHA256 算法对数据进行加密处理
     * <p>
     * 该方法使用指定的密钥对输入数据进行 HMAC-SHA256 加密，并返回加密后的字节数组。
     *
     * @param key           密钥字节数组
     * @param valueToDigest 需要加密的数据字节数组
     * @return 加密后的字节数组
     * @throws IllegalArgumentException 如果加密过程中发生异常
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
     * 使用 HMAC-SHA256 算法对输入流中的数据进行加密处理
     * <p>
     * 该方法首先根据提供的密钥生成 HMAC-SHA256 对象，然后使用该对象对输入流中的数据进行处理，并返回最终的加密结果。
     *
     * @param key           用于加密的密钥字节数组
     * @param valueToDigest 需要加密的数据输入流
     * @return 加密后的字节数组
     * @throws IOException 如果在处理输入流或加密过程中发生 I/O 异常
     * @since 1.1.0
     */
    public static byte[] hmacSha256(byte[] key, InputStream valueToDigest) throws IOException {
        return updateHmac(getHmacSha256(key), valueToDigest).doFinal();
    }

    /**
     * 使用 HMAC-SHA256 算法对字符串进行加密处理
     * <p>
     * 该方法使用指定的密钥和待加密字符串生成 HMAC-SHA256 哈希值，并返回字节数组形式的结果
     *
     * @param key           密钥，用于加密的字符串
     * @param valueToDigest 需要加密的字符串内容
     * @return 加密后的字节数组
     * @since 1.1.0
     */
    public static byte[] hmacSha256(String key, String valueToDigest) {
        return hmacSha256(StringUtils.getBytesUtf8(key), StringUtils.getBytesUtf8(valueToDigest));
    }

    /**
     * 使用 HMAC-SHA256 算法对数据进行加密并返回十六进制字符串
     * <p>
     * 该方法使用指定的密钥和待加密数据，计算 HMAC-SHA256 哈希值，并将结果转换为十六进制字符串格式。
     *
     * @param key           密钥字节数组
     * @param valueToDigest 待加密的数据字节数组
     * @return 返回 HMAC-SHA256 哈希值的十六进制字符串
     * @since 1.1.0
     */
    public static String hmacSha256Hex(byte[] key, byte[] valueToDigest) {
        return Hex.encodeHexString(hmacSha256(key, valueToDigest));
    }

    /**
     * 使用 HMAC-SHA256 算法对输入流进行加密并返回十六进制字符串
     * <p>
     * 该方法使用指定的密钥对输入流中的数据进行 HMAC-SHA256 加密处理，并将结果转换为十六进制字符串格式。
     *
     * @param key           密钥，用于加密计算
     * @param valueToDigest 需要加密的数据输入流
     * @return 加密后的十六进制字符串
     * @throws IOException 如果加密过程中发生输入输出异常
     * @since 1.1.0
     */
    public static String hmacSha256Hex(byte[] key, InputStream valueToDigest) throws IOException {
        return Hex.encodeHexString(hmacSha256(key, valueToDigest));
    }

    /**
     * 使用HMAC SHA-256算法对字符串进行加密并返回十六进制格式的结果
     * <p>
     * 该方法首先调用hmacSha256方法生成加密后的字节数组，然后使用Hex工具类将其转换为十六进制字符串
     *
     * @param key           加密使用的密钥
     * @param valueToDigest 需要加密的原始字符串
     * @return HMAC SHA-256加密后的十六进制字符串
     * @since 1.1.0
     */
    public static String hmacSha256Hex(String key, String valueToDigest) {
        return Hex.encodeHexString(hmacSha256(key, valueToDigest));
    }

    /**
     * 使用 HMAC-SHA384 算法对数据进行加密处理
     * <p>
     * 该方法使用指定的密钥对输入数据进行 HMAC-SHA384 加密，并返回加密后的字节数组。
     *
     * @param key           密钥字节数组
     * @param valueToDigest 需要加密的数据字节数组
     * @return 加密后的字节数组
     * @throws IllegalArgumentException 如果加密过程中发生异常
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
     * 使用 HMAC-SHA384 算法对输入流数据进行加密处理
     * <p>
     * 该方法使用指定的密钥对输入流中的数据进行 HMAC-SHA384 加密，并返回加密后的字节数组。
     *
     * @param key           密钥字节数组
     * @param valueToDigest 需要加密的输入流
     * @return 加密后的字节数组
     * @throws IOException 如果加密过程中发生IO异常
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
     * 使用 HMAC-SHA384 算法对数据进行加密并返回十六进制字符串
     * <p>
     * 该方法使用指定的密钥和待加密数据，通过 HMAC-SHA384 算法生成哈希值，并将结果转换为十六进制字符串格式。
     *
     * @param key           密钥字节数组
     * @param valueToDigest 待加密的数据字节数组
     * @return 返回 HMAC-SHA384 加密后的十六进制字符串
     * @since 1.1.0
     */
    public static String hmacSha384Hex(byte[] key, byte[] valueToDigest) {
        return Hex.encodeHexString(hmacSha384(key, valueToDigest));
    }

    /**
     * 使用 HMAC-SHA384 算法对输入流进行加密并返回十六进制字符串
     * <p>
     * 该方法使用指定的密钥对输入流中的数据进行 HMAC-SHA384 加密处理，并将结果转换为十六进制字符串格式。
     *
     * @param key           密钥字节数组
     * @param valueToDigest 需要加密的输入流
     * @return 加密后的十六进制字符串
     * @throws IOException 如果加密过程中发生输入输出异常
     * @since 1.1.0
     */
    public static String hmacSha384Hex(byte[] key, InputStream valueToDigest) throws IOException {
        return Hex.encodeHexString(hmacSha384(key, valueToDigest));
    }

    /**
     * 使用 HMAC-SHA384 算法对字符串进行加密并返回十六进制格式的结果
     * <p>
     * 该方法使用指定的密钥和待加密字符串，通过 HMAC-SHA384 算法生成加密结果，并将其转换为十六进制字符串格式返回。
     *
     * @param key           密钥，用于加密操作
     * @param valueToDigest 需要加密的字符串值
     * @return 返回 HMAC-SHA384 加密后的十六进制字符串
     * @since 1.1.0
     */
    public static String hmacSha384Hex(String key, String valueToDigest) {
        return Hex.encodeHexString(hmacSha384(key, valueToDigest));
    }

    /**
     * 使用 HMAC-SHA512 算法对数据进行加密处理
     * <p>
     * 根据提供的密钥和待加密数据，生成 HMAC-SHA512 加密结果
     *
     * @param key           密钥字节数组
     * @param valueToDigest 待加密的数据字节数组
     * @return 加密后的字节数组
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
     * 使用 HMAC-SHA512 算法对输入流数据进行加密处理
     * <p>
     * 该方法使用指定的密钥对输入流中的数据进行 HMAC-SHA512 加密，并返回加密后的字节数组
     *
     * @param key           密钥字节数组
     * @param valueToDigest 需要加密的输入流
     * @return 加密后的字节数组
     * @throws IOException 如果加密过程中发生IO异常
     * @since 1.1.0
     */
    public static byte[] hmacSha512(byte[] key, InputStream valueToDigest) throws IOException {
        return updateHmac(getHmacSha512(key), valueToDigest).doFinal();
    }

    /**
     * 使用 HMAC-SHA512 算法对字符串进行加密处理
     * <p>
     * 该方法接收一个密钥和一个待加密的字符串，使用 HMAC-SHA512 算法生成加密结果，并返回字节数组。
     *
     * @param key           密钥，用于加密操作
     * @param valueToDigest 需要加密的字符串值
     * @return 加密后的字节数组
     * @since 1.1.0
     */
    public static byte[] hmacSha512(String key, String valueToDigest) {
        return hmacSha512(StringUtils.getBytesUtf8(key), StringUtils.getBytesUtf8(valueToDigest));
    }

    /**
     * 使用 HMAC-SHA512 算法对数据进行加密并返回十六进制字符串
     * <p>
     * 该方法使用指定的密钥和待加密数据生成 HMAC-SHA512 哈希值，并将结果转换为十六进制字符串格式。
     *
     * @param key           密钥，用于生成 HMAC 哈希
     * @param valueToDigest 需要加密的数据内容
     * @return 返回 HMAC-SHA512 哈希值的十六进制字符串
     * @since 1.1.0
     */
    public static String hmacSha512Hex(byte[] key, byte[] valueToDigest) {
        return Hex.encodeHexString(hmacSha512(key, valueToDigest));
    }

    /**
     * 使用 HMAC-SHA512 算法对输入流数据进行加密并返回十六进制字符串
     * <p>
     * 该方法使用指定的密钥对输入流中的数据进行 HMAC-SHA512 加密操作，并将结果转换为十六进制字符串格式。
     *
     * @param key           密钥，用于 HMAC 计算
     * @param valueToDigest 需要加密的数据输入流
     * @return 加密后的十六进制字符串
     * @throws IOException 如果在处理输入流或加密过程中发生异常
     * @since 1.1.0
     */
    public static String hmacSha512Hex(byte[] key, InputStream valueToDigest) throws IOException {
        return Hex.encodeHexString(hmacSha512(key, valueToDigest));
    }

    /**
     * 使用 HMAC-SHA512 算法对字符串进行加密并返回十六进制格式的结果
     * <p>
     * 该方法使用指定的密钥和待加密字符串，通过 HMAC-SHA512 算法生成摘要，并将结果转换为十六进制字符串。
     *
     * @param key           密钥，用于生成 HMAC 的密钥
     * @param valueToDigest 需要加密的字符串值
     * @return 返回 HMAC-SHA512 加密后的十六进制字符串
     * @since 1.1.0
     */
    public static String hmacSha512Hex(String key, String valueToDigest) {
        return Hex.encodeHexString(hmacSha512(key, valueToDigest));
    }

    /**
     * 更新HMAC值
     * <p>
     * 使用指定的字节数组对给定的HMAC对象进行更新，重新计算HMAC值。
     *
     * @param mac           要更新的HMAC对象
     * @param valueToDigest 需要进行哈希处理的字节数组
     * @return 更新后的HMAC对象
     * @since 1.1.0
     */
    public static Mac updateHmac(Mac mac, byte[] valueToDigest) {
        mac.reset();
        mac.update(valueToDigest);
        return mac;
    }

    /**
     * 更新HMAC值
     * <p>
     * 使用给定的输入流对HMAC进行更新操作，将输入数据逐步处理并更新到HMAC对象中。
     *
     * @param mac           要更新的HMAC对象
     * @param valueToDigest 需要进行HMAC计算的数据输入流
     * @return 更新后的HMAC对象
     * @throws IOException 如果在读取输入流时发生IO异常
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
     * 更新HMAC值
     * <p>
     * 使用指定的值进行哈希计算并更新给定的HMAC对象
     *
     * @param mac           要更新的HMAC对象
     * @param valueToDigest 需要进行哈希计算的字符串值
     * @return 更新后的HMAC对象
     * @since 1.1.0
     */
    public static Mac updateHmac(Mac mac, String valueToDigest) {
        mac.reset();
        mac.update(StringUtils.getBytesUtf8(valueToDigest));
        return mac;
    }
}
