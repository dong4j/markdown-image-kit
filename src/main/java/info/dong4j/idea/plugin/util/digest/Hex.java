package info.dong4j.idea.plugin.util.digest;

import info.dong4j.idea.plugin.util.Charsets;

import java.nio.charset.Charset;

/**
 * Hex 工具类
 * <p>
 * 提供将字节数组转换为十六进制字符串的工具方法，支持大小写转换。
 * 主要用于数据编码和格式化输出。
 *
 * @author dong4j
 * @version 1.0.0
 * @date 2021.02.14
 * @since 1.1.0
 */
public class Hex {
    /** 默认字符集，用于指定编码格式 */
    public static final Charset DEFAULT_CHARSET;
    /** 用于表示小写字母的字符数组 */
    private static final char[] DIGITS_LOWER;
    /** 数字字符数组，用于表示大写形式的数字字符 */
    private static final char[] DIGITS_UPPER;

    /**
     * 将字节数组转换为十六进制字符数组
     * <p>
     * 该方法用于将输入的字节数组转换为对应的十六进制字符串表示形式。
     *
     * @param data 需要转换的字节数组
     * @return 十六进制字符数组
     * @since 1.1.0
     */
    public static char[] encodeHex(byte[] data) {
        return encodeHex(data, true);
    }

    /**
     * 将字节数组编码为十六进制字符数组
     * <p>
     * 该方法将输入的字节数组转换为十六进制表示形式，并根据参数决定输出是否为小写字母。
     *
     * @param data        需要编码的字节数组
     * @param toLowerCase 一个布尔值，表示编码后的十六进制字符是否使用小写字母
     * @return 十六进制字符数组
     * @since 1.1.0
     */
    public static char[] encodeHex(byte[] data, boolean toLowerCase) {
        return encodeHex(data, toLowerCase ? DIGITS_LOWER : DIGITS_UPPER);
    }

    /**
     * 将字节数组转换为十六进制字符数组
     * <p>
     * 该方法将输入的字节数组转换为十六进制表示形式的字符数组。每个字节被拆分为两个十六进制字符。
     *
     * @param data     需要转换的字节数组
     * @param toDigits 用于映射十六进制值的字符数组，通常为 "0123456789abcdef" 或类似格式
     * @return 转换后的十六进制字符数组
     * @since 1.1.0
     */
    protected static char[] encodeHex(byte[] data, char[] toDigits) {
        int l = data.length;
        char[] out = new char[l << 1];
        int i = 0;

        for (int var5 = 0; i < l; ++i) {
            out[var5++] = toDigits[(240 & data[i]) >>> 4];
            out[var5++] = toDigits[15 & data[i]];
        }

        return out;
    }

    /**
     * 将字节数组转换为十六进制字符串
     * <p>
     * 该方法接收一个字节数组作为输入，将其转换为对应的十六进制字符串表示形式。
     *
     * @param data 要转换的字节数组
     * @return 转换后的十六进制字符串
     * @since 1.1.0
     */
    public static String encodeHexString(byte[] data) {
        return new String(encodeHex(data));
    }

    static {
        DEFAULT_CHARSET = Charsets.UTF_8;
        DIGITS_LOWER = new char[] {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
        DIGITS_UPPER = new char[] {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
    }
}
