package info.dong4j.idea.plugin.util;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * URL安全的Base64编码和解码工具类
 * <p>
 * 提供URL安全的Base64编码和解码功能，适用于需要在URL或JSON等场景中安全传输二进制数据的场景。
 * 支持字符串和字节数组的编码与解码操作。
 *
 * @author dong4j
 * @version 1.0.0
 * @date 2021.02.18
 * @since 1.6.1
 */
public final class UrlSafeBase64 {
    /**
     * 私有构造函数，防止实例化
     * <p>
     * 该构造函数为私有，确保 UrlSafeBase64 类不能被外部实例化
     *
     * @since 1.6.1
     */
    private UrlSafeBase64() {
    }   // don't instantiate

    /**
     * 对字符串进行编码并返回结果字符串
     * <p>
     * 将输入的字符串转换为字节数组（使用 UTF-8 编码），然后进行编码处理，最终返回编码后的字符串结果。
     *
     * @param data 待编码的字符串
     * @return 编码后的字符串
     * @since 1.6.1
     */
    public static String encodeToString(String data) {
        return encodeToString(data.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 对字节数组进行编码并返回结果字符串
     * <p>
     * 使用 URL 安全的 Base64 编码方式对输入的字节数组进行编码，返回编码后的字符串结果。
     *
     * @param data 字节数组，需要被编码的数据
     * @return 编码后的字符串
     * @since 1.6.1
     */
    public static String encodeToString(byte[] data) {
        return Base64.getUrlEncoder().encodeToString(data);
    }

    /**
     * 对编码过的字符串进行解码，返回原始字节数组
     * <p>
     * 使用 Base64 的 URL 安全解码方式对输入字符串进行解码操作
     *
     * @param data 编码过的字符串
     * @return 原始数据 byte[]
     * @since 1.6.1
     */
    public static byte[] decode(String data) {
        return Base64.getUrlDecoder().decode(data);
    }
}
