package info.dong4j.idea.plugin.util;

import java.security.SecureRandom;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;

/**
 * DES 加解密工具类
 * <p>
 * 提供基于 DES 算法的加密和解密功能，支持字符串与字节数组之间的转换。
 * 主要用于数据的加密存储和安全传输，确保信息在传输过程中不被非法读取。
 * <p>
 * 该类封装了 DES 加密和解密的核心逻辑，包括密钥处理、二进制与十六进制转换等。
 * 使用时需注意密钥长度必须为8位或其倍数，否则会自动补0处理。
 *
 * @author dong4j
 * @version 1.0.0
 * @date 2025.10.24
 * @since 1.0.0
 */
public final class DES {
    /** DES 加密算法常量 */
    private final static String DES = "DES";

    /**
     * 使用DES算法对字符串进行加密
     * <p>
     * 该方法接收待加密的字符串和加密私钥，对数据进行DES加密处理，并返回加密后的字节数组的十六进制字符串表示。加密私钥长度不能小于8位。
     *
     * @param data 待加密的字符串
     * @param key  加密私钥，长度不能小于8位
     * @return 加密后的字节数组的十六进制字符串，一般结合Base64编码使用
     * @since 0.0.1
     */
    public static String encrypt(String data, String key) {
        if (data != null) {
            try {
                key = fixKey(key);
                return byte2hex(encrypt(data.getBytes(), key.getBytes()));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return null;
    }

    /**
     * 对给定的 key 进行补零处理，确保其长度为 8 的倍数。
     * <p>
     * 如果 key 为空或长度不是 8 的倍数，则在末尾补 0，使其达到 8 的倍数长度。
     *
     * @param key the key
     * @return 补零后的字符串
     * @since 0.0.1
     */
    private static String fixKey(String key) {
        if (key == null || key.length() == 0) {
            return "00000000";
        }

        int y = key.length() % 8;
        if (y != 0) {
            StringBuilder keyBuilder = new StringBuilder(key);
            for (int i = 0; i < 8 - y; i++) {
                keyBuilder.append('0');
            }
            key = keyBuilder.toString();
        }

        return key;
    }

    /**
     * 将字节数组转换为十六进制字符串
     * <p>
     * 该方法将输入的字节数组转换为大写的十六进制字符串表示形式，每个字节用两位十六进制字符表示。
     *
     * @param b 字节数组
     * @return 转换后的十六进制字符串
     */
    private static String byte2hex(byte[] b) {
        StringBuilder hs = new StringBuilder();
        String stmp;
        for (int n = 0; b != null && n < b.length; n++) {
            stmp = Integer.toHexString(b[n] & 0XFF);
            if (stmp.length() == 1) {
                hs.append('0');
            }
            hs.append(stmp);
        }
        return hs.toString().toUpperCase();
    }

    /**
     * 使用DES算法对数据进行加密
     * <p>
     * 该方法通过给定的加密密钥对源字节数组进行加密操作，并返回加密后的字节数组。
     *
     * @param src 源数据字节数组
     * @param key 加密密钥，长度不能小于8位
     * @return 加密后的字节数组
     * @throws RuntimeException 如果加密过程中发生异常，将抛出运行时异常
     * @since 0.0.1
     */
    public static byte[] encrypt(byte[] src, byte[] key) throws RuntimeException {
        try {
            Cipher cipher = getCipher(key, Cipher.ENCRYPT_MODE);
            return cipher.doFinal(src);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 根据密钥和加密类型获取Cipher对象
     * <p>
     * 使用给定的密钥和加密类型初始化并返回一个Cipher对象，用于加密或解密操作。
     *
     * @param key  密钥字节数组
     * @param type 加密或解密操作类型，例如 Cipher.ENCRYPT_MODE 或 Cipher.DECRYPT_MODE
     * @return 初始化后的Cipher对象
     * @throws RuntimeException 如果初始化过程中发生异常
     */
    private static Cipher getCipher(byte[] key, int type) {
        Cipher cipher;
        try {
            // DES算法要求有一个可信任的随机数源
            SecureRandom sr = new SecureRandom();
            // 从原始密匙数据创建一个DESKeySpec对象
            DESKeySpec dks = new DESKeySpec(key);
            // 创建一个密匙工厂，然后用它把DESKeySpec对象转换成
            // 一个SecretKey对象
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(DES);
            SecretKey securekey = keyFactory.generateSecret(dks);
            // Cipher对象实际完成解密操作
            cipher = Cipher.getInstance(DES);
            // 用密匙初始化Cipher对象
            cipher.init(type, securekey, sr);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return cipher;
    }

    /**
     * 对加密数据进行解密操作
     * <p>
     * 使用指定的密钥对传入的加密数据进行解密，并返回解密后的字符串结果
     *
     * @param data 加密后的数据
     * @param key  用于解密的密钥
     * @return 解密后的字符串，若解密失败则返回空字符串
     * @since 0.0.1
     */
    public static String decrypt(String data, String key) {
        key = fixKey(key);
        try {
            byte[] decrypt = decrypt(hex2byte(data.getBytes()), key.getBytes());
            return new String(decrypt);
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * 对加密数据进行解密操作
     * <p>
     * 使用指定密钥对加密后的数据进行解密，并返回解密后的原始字节数组
     *
     * @param src 加密后的数据，字节数组
     * @param key 解密使用的密钥，长度必须是8的倍数
     * @return 解密后的原始数据，字节数组
     * @throws BadPaddingException       如果解密数据填充无效时抛出
     * @throws IllegalBlockSizeException 如果解密数据块大小不合法时抛出
     * @since 0.0.1
     */
    private static byte[] decrypt(byte[] src, byte[] key) throws BadPaddingException, IllegalBlockSizeException {
        Cipher cipher = getCipher(key, Cipher.DECRYPT_MODE);
        return cipher.doFinal(src);
    }

    /**
     * 将十六进制字节数组转换为字节数组
     * <p>
     * 该方法接收一个十六进制表示的字节数组，将其转换为对应的字节数组。如果输入数组长度为奇数，则抛出异常。
     *
     * @param b 十六进制字节数组
     * @return 转换后的字节数组
     * @throws IllegalArgumentException 如果输入数组长度为奇数
     * @since 0.0.1
     */
    private static byte[] hex2byte(byte[] b) {
        if ((b.length % 2) != 0) {
            throw new IllegalArgumentException();
        }
        byte[] b2 = new byte[b.length / 2];
        for (int n = 0; n < b.length; n += 2) {
            String item = new String(b, n, 2);
            b2[n / 2] = (byte) Integer.parseInt(item, 16);
        }
        return b2;
    }
}
