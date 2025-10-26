package info.dong4j.idea.plugin.util;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Base64 工具类
 * <p>
 * 提供 Base64 编码和解码相关的工具方法，支持标准 Base64 和 URL 安全的 Base64 编码方式。
 * 包括对字节数组和字符串的编码与解码操作，使用 UTF-8 字符集进行字符串转换。
 *
 * @author Juergen Hoeller
 * @author Gary Russell
 * @version 1.0.0
 * @date 2025.10.24
 * @since 4.1
 */
public abstract class Base64Utils {

    /** 默认字符集，使用 UTF-8 编码 */
    private static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;

    /**
     * 对给定的字节数组进行 Base64 编码。
     * <p>
     * 如果输入的字节数组为空，则直接返回原数组。否则使用标准的 Base64 编码器对字节数组进行编码。
     *
     * @param src 原始字节数组
     * @return Base64 编码后的字节数组
     */
    public static byte[] encode(byte[] src) {
        if (src.length == 0) {
            return src;
        }
        return Base64.getEncoder().encode(src);
    }

    /**
     * 对给定的字节数组进行 Base64 解码。
     * <p>
     * 该方法接收一个经过 Base64 编码的字节数组，并返回解码后的原始字节数组。
     *
     * @param src 经过 Base64 编码的字节数组
     * @return 解码后的原始字节数组
     */
    public static byte[] decode(byte[] src) {
        if (src.length == 0) {
            return src;
        }
        return Base64.getDecoder().decode(src);
    }

    /**
     * 使用 RFC 4648 定义的 "URL 和文件名安全字母表" 对给定的字节数组进行 Base64 编码。
     * <p>
     * 该方法将输入的字节数组转换为 URL 和文件名安全的 Base64 编码格式。
     *
     * @param src 原始字节数组
     * @return 编码后的字节数组
     * @since 4.2.4
     */
    public static byte[] encodeUrlSafe(byte[] src) {
        if (src.length == 0) {
            return src;
        }
        return Base64.getUrlEncoder().encode(src);
    }

    /**
     * 使用RFC 4648定义的"URL和文件名安全字母表"对给定的字节数组进行Base64解码。
     * <p>
     * 该方法用于将URL安全格式的Base64编码数据转换为原始字节数组。
     *
     * @param src 需要解码的Base64编码字节数组
     * @return 解码后的原始字节数组
     * @since 4.2.4
     */
    public static byte[] decodeUrlSafe(byte[] src) {
        if (src.length == 0) {
            return src;
        }
        return Base64.getUrlDecoder().decode(src);
    }

    /**
     * 将给定的字节数组进行 Base64 编码并转换为字符串。
     * <p>
     * 该方法接收一个字节数组作为输入，对其进行 Base64 编码后，使用 UTF-8 字符集将其转换为字符串返回。
     *
     * @param src 原始字节数组
     * @return Base64 编码后的字符串
     */
    public static String encodeToString(byte[] src) {
        if (src.length == 0) {
            return "";
        }
        return new String(encode(src), DEFAULT_CHARSET);
    }

    /**
     * 将给定的 UTF-8 字符串进行 Base64 解码，返回原始字节数组。
     * <p>
     * 该方法首先将输入的字符串转换为字节数组，然后进行 Base64 解码操作。
     *
     * @param src 需要解码的 UTF-8 字符串
     * @return 解码后的原始字节数组
     */
    public static byte[] decodeFromString(String src) {
        if (src.isEmpty()) {
            return new byte[0];
        }
        return decode(src.getBytes(DEFAULT_CHARSET));
    }

    /**
     * 使用 RFC 4648 定义的 "URL 和文件名安全字母表" 对给定的字节数组进行 Base64 编码，并返回 UTF-8 字符串。
     *
     * @param src 原始字节数组
     * @return 编码后的 UTF-8 字符串
     */
    public static String encodeToUrlSafeString(byte[] src) {
        return new String(encodeUrlSafe(src), DEFAULT_CHARSET);
    }

    /**
     * 使用 RFC 4648 的 "URL 和文件名安全字母表" 对给定的 UTF-8 字符串进行 Base64 解码
     * <p>
     * 将输入的 UTF-8 字符串转换为字节数组，并使用 URL 安全的 Base64 编码方式进行解码
     *
     * @param src 需要解码的 UTF-8 字符串
     * @return 原始字节数组
     */
    public static byte[] decodeFromUrlSafeString(String src) {
        return decodeUrlSafe(src.getBytes(DEFAULT_CHARSET));
    }

}
