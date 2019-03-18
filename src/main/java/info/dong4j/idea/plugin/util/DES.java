package info.dong4j.idea.plugin.util;

import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;

/**
 * <p>Company: 科大讯飞股份有限公司-四川分公司</p>
 * <p>Description: 加解密工具类</p>
 *
 * @author dong4j
 * @date 2018 -08-26 00:30
 * @email sjdong3 @iflytek.com
 */
public final class DES {

    private final static String DES = "DES";

    /**
     * DES算法，加密
     *
     * @param data 待加密字符串
     * @param key  加密私钥，长度不能够小于8位
     * @return 加密后的字节数组 ，一般结合Base64编码使用
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
     * DES算法，解密
     *
     * @param src the src
     * @param key 解密私钥，长度不能够小于8位
     * @return 解密后的字节数组 string
     * @throws RuntimeException the runtime exception
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
     * 数据解密
     *
     * @param data the data
     * @param key  密钥
     * @return string string
     */
    public final static String decrypt(String data, String key) {
        key = fixKey(key);
        return new String(decrypt(hex2byte(data.getBytes()), key.getBytes()));
    }

    /**
     * 解密
     *
     * @param src 数据源
     * @param key 密钥，长度必须是8的倍数
     * @return 返回解密后的原始数据 byte [ ]
     * @throws RuntimeException the runtime exception
     */
    private static byte[] decrypt(byte[] src, byte[] key) throws RuntimeException {
        try {
            Cipher cipher = getCipher(key, Cipher.DECRYPT_MODE);
            return cipher.doFinal(src);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

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
     * key的长度必须是8的倍数，如果不是则用0补齐。
     *
     * @param key the key
     * @return string
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
     * 二行制转字符串
     *
     * @param b the b
     * @return string
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