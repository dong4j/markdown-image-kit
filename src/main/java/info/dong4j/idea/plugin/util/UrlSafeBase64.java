package info.dong4j.idea.plugin.util;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * URL安全的Base64编码和解码
 *
 * @author dong4j
 * @version 1.0.0
 * @email "mailto:dong4j@fkhwl.com"
 * @date 2021.02.18 22:35
 * @since 1.6.1
 */
public final class UrlSafeBase64 {

    /**
     * Url safe base 64
     *
     * @since 1.6.1
     */
    private UrlSafeBase64() {
    }   // don't instantiate

    /**
     * 编码字符串
     *
     * @param data 待编码字符串
     * @return 结果字符串 string
     * @since 1.6.1
     */
    public static String encodeToString(String data) {
        return encodeToString(data.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 编码数据
     *
     * @param data 字节数组
     * @return 结果字符串 string
     * @since 1.6.1
     */
    public static String encodeToString(byte[] data) {
        return Base64.getUrlEncoder().encodeToString(data);
    }

    /**
     * 解码数据
     *
     * @param data 编码过的字符串
     * @return 原始数据 byte [ ]
     * @since 1.6.1
     */
    public static byte[] decode(String data) {
        return Base64.getUrlDecoder().decode(data);
    }
}
