package info.dong4j.idea.plugin.util;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * 字符串工具类
 * <p>
 * 提供一系列字符串处理的实用方法，包括字符串是否为空、是否为白字符、去除空格、获取字节等操作。
 * 该工具类适用于日常开发中对字符串进行各种判断和转换的场景。
 *
 * @author dong4j
 * @version 1.0.0
 * @date 2021.02.14
 * @since 1.1.0
 */
public class StringUtils {
    /**
     * 字符串工具类的构造函数
     * <p>
     * 初始化字符串工具类实例，提供常用字符串处理方法
     *
     * @since 1.1.0
     */
    public StringUtils() {
    }

    /**
     * 判断字符串是否为空
     * <p>
     * 如果字符串为 null 或者长度为 0，则返回 true
     *
     * @param str 要判断的字符串
     * @return 如果字符串为空返回 true，否则返回 false
     * @since 1.1.0
     */
    public static boolean isEmpty(String str) {
        return str == null || str.isEmpty();
    }

    /**
     * 判断字符串是否非空
     * <p>
     * 检查传入的字符串是否不为 null 且长度大于 0
     *
     * @param str 要检查的字符串
     * @return 如果字符串非空返回 true，否则返回 false
     * @since 1.1.0
     */
    public static boolean isNotEmpty(String str) {
        return !isEmpty(str);
    }

    /**
     * 判断字符串是否为空白
     * <p>
     * 如果字符串为 null 或者所有字符都是空白字符（如空格、制表符等），则返回 true
     *
     * @param str 要判断的字符串
     * @return 如果字符串为空白则返回 true，否则返回 false
     * @since 1.1.0
     */
    public static boolean isBlank(String str) {
        int strLen;
        if (str != null && (strLen = str.length()) != 0) {
            for (int i = 0; i < strLen; ++i) {
                if (!Character.isWhitespace(str.charAt(i))) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * 判断字符串是否非空
     * <p>
     * 检查传入的字符串是否不为空，即字符串长度大于0且不全为空格
     *
     * @param str 要检查的字符串
     * @return 如果字符串非空，返回true；否则返回false
     * @since 1.1.0
     */
    public static boolean isNotBlank(String str) {
        return !isBlank(str);
    }

    /**
     * 对字符串进行修剪操作，去除首尾空白字符
     * <p>
     * 如果输入字符串为 null，则返回 null；否则返回去除首尾空白字符后的字符串
     *
     * @param str 需要修剪的字符串
     * @return 修剪后的字符串，若输入为 null 则返回 null
     * @since 1.1.0
     */
    public static String trim(String str) {
        return str == null ? null : str.trim();
    }

    /**
     * 将字符串转换为 UTF-8 编码的字节数组
     * <p>
     * 该方法使用 UTF-8 编码对输入字符串进行编码，并返回对应的字节数组
     *
     * @param string 需要转换的字符串
     * @return UTF-8 编码的字节数组
     * @since 1.1.0
     */
    public static byte[] getBytesUtf8(String string) {
        return getBytes(string, Charsets.UTF_8);
    }

    /**
     * 将字符串转换为字节数组
     * <p>
     * 根据指定的字符集将输入字符串转换为对应的字节数组，若输入字符串为null，则返回null。
     *
     * @param string  需要转换的字符串，可以为null
     * @param charset 用于转换的字符集
     * @return 转换后的字节数组，若输入字符串为null则返回null
     * @since 1.1.0
     */
    private static byte[] getBytes(String string, Charset charset) {
        return string == null ? null : string.getBytes(charset);
    }

    /**
     * 如果字符串为空则返回默认值，否则返回原字符串
     * <p>
     * 该方法用于判断传入的字符串是否为空，若为空则返回指定的默认字符串，否则返回原字符串。
     *
     * @param str        需要检查是否为空的字符串
     * @param defaultStr 若字符串为空时返回的默认字符串
     * @return 原字符串（若不为空）或默认字符串（若为空）
     * @since 1.6.0
     */
    public static String defaultIfEmpty(String str, String defaultStr) {
        return isEmpty(str) ? defaultStr : str;
    }

    /**
     * 将字符串转换为UTF-8编码的字节数组
     * <p>
     * 该方法使用标准的UTF-8字符集将输入字符串转换为对应的字节数组
     *
     * @param data 需要转换的字符串数据
     * @return 转换后的字节数组
     * @since 1.6.1
     */
    public static byte[] utf8Bytes(String data) {
        return data.getBytes(StandardCharsets.UTF_8);
    }

    /**
     * 判断字符串是否存在于指定的字符串数组中
     * <p>
     * 遍历字符串数组，检查目标字符串是否存在于数组中，若存在则返回true，否则返回false
     *
     * @param s     要查找的字符串
     * @param array 要查找的字符串数组
     * @return 如果字符串存在于数组中返回true，否则返回false
     */
    public static boolean inStringArray(String s, String[] array) {
        for (String x : array) {
            if (x.equals(s)) {
                return true;
            }
        }
        return false;
    }

}
