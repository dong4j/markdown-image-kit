package info.dong4j.idea.plugin.util.digest;

import info.dong4j.idea.plugin.util.Charsets;

import java.nio.charset.Charset;

/**
 * <p>Description: </p>
 *
 * @author dong4j
 * @version 1.0.0
 * @email "mailto:dong4j@gmail.com"
 * @date 2021.02.14 23:41
 * @since 1.1.0
 */
public class Hex {
    /** DEFAULT_CHARSET */
    public static final Charset DEFAULT_CHARSET;
    /** DIGITS_LOWER */
    private static final char[] DIGITS_LOWER;
    /** DIGITS_UPPER */
    private static final char[] DIGITS_UPPER;

    /**
     * Encode hex
     *
     * @param data data
     * @return the char [ ]
     * @since 1.1.0
     */
    public static char[] encodeHex(byte[] data) {
        return encodeHex(data, true);
    }

    /**
     * Encode hex
     *
     * @param data        data
     * @param toLowerCase to lower case
     * @return the char [ ]
     * @since 1.1.0
     */
    public static char[] encodeHex(byte[] data, boolean toLowerCase) {
        return encodeHex(data, toLowerCase ? DIGITS_LOWER : DIGITS_UPPER);
    }

    /**
     * Encode hex
     *
     * @param data     data
     * @param toDigits to digits
     * @return the char [ ]
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
     * Encode hex string
     *
     * @param data data
     * @return the string
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
