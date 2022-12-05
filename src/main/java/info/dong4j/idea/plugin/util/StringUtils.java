package info.dong4j.idea.plugin.util;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * <p>Description:  </p>
 *
 * @author dong4j
 * @version 1.0.0
 * @email "mailto:dong4j@gmail.com"
 * @date 2021.02.14 22:45
 * @since 1.1.0
 */
public class StringUtils {

    /**
     * String utils
     *
     * @since 1.1.0
     */
    public StringUtils() {
    }

    /**
     * Is empty
     *
     * @param str str
     * @return the boolean
     * @since 1.1.0
     */
    public static boolean isEmpty(String str) {
        return str == null || str.length() == 0;
    }

    /**
     * Is not empty
     *
     * @param str str
     * @return the boolean
     * @since 1.1.0
     */
    public static boolean isNotEmpty(String str) {
        return !isEmpty(str);
    }

    /**
     * Is blank
     *
     * @param str str
     * @return the boolean
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
     * Is not blank
     *
     * @param str str
     * @return the boolean
     * @since 1.1.0
     */
    public static boolean isNotBlank(String str) {
        return !isBlank(str);
    }

    /**
     * Trim
     *
     * @param str str
     * @return the string
     * @since 1.1.0
     */
    public static String trim(String str) {
        return str == null ? null : str.trim();
    }

    /**
     * Get bytes utf 8
     *
     * @param string string
     * @return the byte [ ]
     * @since 1.1.0
     */
    public static byte[] getBytesUtf8(String string) {
        return getBytes(string, Charsets.UTF_8);
    }

    /**
     * Get bytes
     *
     * @param string  string
     * @param charset charset
     * @return the byte [ ]
     * @since 1.1.0
     */
    private static byte[] getBytes(String string, Charset charset) {
        return string == null ? null : string.getBytes(charset);
    }

    /**
     * Default if empty
     *
     * @param str        str
     * @param defaultStr default str
     * @return the string
     * @since 1.6.0
     */
    public static String defaultIfEmpty(String str, String defaultStr) {
        return isEmpty(str) ? defaultStr : str;
    }

    /**
     * Utf 8 bytes
     *
     * @param data data
     * @return the byte [ ]
     * @since 1.6.1
     */
    public static byte[] utf8Bytes(String data) {
        return data.getBytes(StandardCharsets.UTF_8);
    }

    /**
     * In string array
     *
     * @param s     s
     * @param array array
     * @return the boolean
     * @since 1.6.1
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
