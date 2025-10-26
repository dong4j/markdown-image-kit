//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package info.dong4j.idea.plugin.util;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * 字符集工具类
 * <p>
 * 提供常用字符集的定义和转换方法，用于处理不同编码格式的字符串和字节数据。
 * 包含 ISO_8859_1、US_ASCII、UTF_16、UTF_16BE、UTF_16LE、UTF_8 等标准字符集的常量定义。
 * 提供了将字符串转换为字符集对象的方法，以及获取所有必需字符集的映射。
 *
 * @author dong4j
 * @version 1.0.0
 * @date 2025.10.24
 * @since 1.1.0
 */
public class Charsets {
    /** ISO-8859-1 字符集，表示一种单字节编码的字符集 */
    public static final Charset ISO_8859_1;
    /** US_ASCII 字符集，表示美国ASCII字符编码 */
    public static final Charset US_ASCII;
    /** UTF-16 字符集 */
    public static final Charset UTF_16;
    /** UTF-16 Big Endian 字符集 */
    public static final Charset UTF_16BE;
    /** UTF-16 Little Endian 字符集 */
    public static final Charset UTF_16LE;
    /** UTF-8 字符集 */
    public static final Charset UTF_8;

    /**
     * 默认构造函数，用于初始化Charsets类的实例
     * <p>
     * 该构造函数无具体逻辑，主要用于实例化Charsets类
     *
     * @since 1.1.0
     */
    public Charsets() {
    }

    /**
     * 获取系统要求的字符集集合
     * <p>
     * 返回一个包含标准字符集的不可修改的有序映射，按照字符集名称的不区分大小写顺序排列。
     *
     * @return 包含标准字符集的不可修改有序映射
     * @since 1.1.0
     */
    public static SortedMap<String, Charset> requiredCharsets() {
        TreeMap<String, Charset> m = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        m.put(StandardCharsets.ISO_8859_1.name(), StandardCharsets.ISO_8859_1);
        m.put(StandardCharsets.US_ASCII.name(), StandardCharsets.US_ASCII);
        m.put(StandardCharsets.UTF_16.name(), StandardCharsets.UTF_16);
        m.put(StandardCharsets.UTF_16BE.name(), StandardCharsets.UTF_16BE);
        m.put(StandardCharsets.UTF_16LE.name(), StandardCharsets.UTF_16LE);
        m.put(StandardCharsets.UTF_8.name(), StandardCharsets.UTF_8);
        return Collections.unmodifiableSortedMap(m);
    }

    /**
     * 将传入的字符集对象转换为实际使用的字符集
     * <p>
     * 如果传入的字符集对象为 null，则返回系统默认字符集；否则返回传入的字符集对象。
     *
     * @param charset 传入的字符集对象，可以为 null
     * @return 实际使用的字符集对象
     * @since 1.1.0
     */
    public static Charset toCharset(Charset charset) {
        return charset == null ? Charset.defaultCharset() : charset;
    }

    /**
     * 将字符串转换为对应的字符集
     * <p>
     * 如果传入的字符集字符串为 null，则返回系统默认字符集；否则，根据传入的字符集字符串返回对应的 Charset 对象。
     *
     * @param charset 字符集名称，可以为 null
     * @return 对应的 Charset 对象
     * @since 1.1.0
     */
    public static Charset toCharset(String charset) {
        return charset == null ? Charset.defaultCharset() : Charset.forName(charset);
    }

    static {
        ISO_8859_1 = StandardCharsets.ISO_8859_1;
        US_ASCII = StandardCharsets.US_ASCII;
        UTF_16 = StandardCharsets.UTF_16;
        UTF_16BE = StandardCharsets.UTF_16BE;
        UTF_16LE = StandardCharsets.UTF_16LE;
        UTF_8 = StandardCharsets.UTF_8;
    }
}
