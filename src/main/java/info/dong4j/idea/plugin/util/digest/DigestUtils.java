package info.dong4j.idea.plugin.util.digest;

import info.dong4j.idea.plugin.util.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * 摘要工具类
 * <p>
 * 提供多种消息摘要算法的封装，包括 MD2、MD5、SHA 系列等，支持对字节数组、字符串和输入流进行加密处理，并提供十六进制格式的摘要结果。
 * <p>
 * 该工具类封装了常见的摘要算法，简化了使用 MessageDigest 的复杂度，提供统一的接口用于生成摘要值和十六进制字符串。
 *
 * @author dong4j
 * @version 1.0.0
 * @date 2021.02.14
 * @since 1.1.0
 */
public class DigestUtils {
    /** 流处理缓冲区长度，用于控制数据读取和写入的缓冲大小 */
    private static final int STREAM_BUFFER_LENGTH = 1024;

    /**
     * 消息摘要工具类的构造函数
     * <p>
     * 初始化消息摘要工具类，用于提供消息摘要相关的实用方法
     *
     * @since 1.1.0
     */
    public DigestUtils() {
    }

    /**
     * 对输入的数据流进行摘要计算，并返回摘要结果
     * <p>
     * 该方法使用指定的摘要算法对输入的字节流进行处理，最终生成摘要值。
     *
     * @param digest 摘要算法实例，如 MD5、SHA-1 等
     * @param data   输入的数据流
     * @return 摘要结果的字节数组
     * @throws IOException 如果在处理数据流时发生IO异常
     * @since 1.1.0
     */
    private static byte[] digest(MessageDigest digest, InputStream data) throws IOException {
        return updateDigest(digest, data).digest();
    }

    /**
     * 获取指定算法的 MessageDigest 实例
     * <p>
     * 根据传入的算法名称创建并返回对应的 MessageDigest 对象。如果指定算法不存在，将抛出 IllegalArgumentException。
     *
     * @param algorithm 算法名称，如 "MD5" 或 "SHA-256"
     * @return 指定算法的 MessageDigest 实例
     * @throws IllegalArgumentException 如果指定的算法不存在
     * @since 1.1.0
     */
    public static MessageDigest getDigest(String algorithm) {
        try {
            return MessageDigest.getInstance(algorithm);
        } catch (NoSuchAlgorithmException var2) {
            throw new IllegalArgumentException(var2);
        }
    }

    /**
     * 获取 MD2 摘要算法的 MessageDigest 实例
     * <p>
     * 该方法用于获取 MD2 算法对应的 MessageDigest 对象，用于后续的加密或哈希操作。
     *
     * @return MD2 算法的 MessageDigest 实例
     * @since 1.1.0
     */
    public static MessageDigest getMd2Digest() {
        return getDigest("MD2");
    }

    /**
     * 获取 MD5 摘要对象
     * <p>
     * 返回一个配置好的 MessageDigest 实例，用于计算 MD5 哈希值
     *
     * @return MD5 摘要对象
     * @since 1.1.0
     */
    public static MessageDigest getMd5Digest() {
        return getDigest("MD5");
    }

    /**
     * 获取 SHA-1 摘要算法的 MessageDigest 实例
     * <p>
     * 该方法用于返回 SHA-1 算法对应的 MessageDigest 对象，用于计算数据的哈希值。
     *
     * @return SHA-1 摘要算法的 MessageDigest 实例
     * @since 1.1.0
     */
    public static MessageDigest getSha1Digest() {
        return getDigest("SHA-1");
    }

    /**
     * 获取 SHA-256 摘要算法的 MessageDigest 实例
     * <p>
     * 该方法用于获取 SHA-256 算法的 MessageDigest 对象，可用于计算数据的哈希值。
     *
     * @return SHA-256 摘要算法的 MessageDigest 实例
     * @since 1.1.0
     */
    public static MessageDigest getSha256Digest() {
        return getDigest("SHA-256");
    }

    /**
     * 获取 SHA-384 摘要算法的 MessageDigest 实例
     * <p>
     * 该方法用于获取 SHA-384 哈希算法的 MessageDigest 对象，可用于计算数据的哈希值。
     *
     * @return SHA-384 摘要算法的 MessageDigest 实例
     * @since 1.1.0
     */
    public static MessageDigest getSha384Digest() {
        return getDigest("SHA-384");
    }

    /**
     * 获取 SHA-512 摘要算法的 MessageDigest 实例
     * <p>
     * 该方法用于获取 SHA-512 哈希算法的 MessageDigest 对象，可用于计算数据的哈希值。
     *
     * @return SHA-512 摘要算法的 MessageDigest 实例
     * @since 1.1.0
     */
    public static MessageDigest getSha512Digest() {
        return getDigest("SHA-512");
    }

    /**
     * 获取SHA摘要对象
     * <p>
     * 返回一个SHA摘要实例，用于进行SHA算法的加密处理。
     *
     * @return SHA摘要对象
     * @since 1.1.0
     * @deprecated 已弃用，请使用其他方法替代
     */
    @Deprecated
    public static MessageDigest getShaDigest() {
        return getSha1Digest();
    }

    /**
     * 使用 MD2 算法对输入数据进行哈希处理并返回哈希值
     * <p>
     * 该方法通过调用 MD2 哈希算法对传入的字节数组进行处理，生成对应的哈希值字节数组。
     *
     * @param data 需要进行哈希处理的输入数据，类型为字节数组
     * @return 返回 MD2 哈希处理后的字节数组结果
     * @since 1.1.0
     */
    public static byte[] md2(byte[] data) {
        return getMd2Digest().digest(data);
    }

    /**
     * 使用 MD2 算法对输入流进行哈希处理，生成字节数组结果
     * <p>
     * 该方法通过 MD2 算法计算输入流的哈希值，并返回对应的字节数组
     *
     * @param data 输入流，用于计算哈希值
     * @return 哈希计算后的字节数组
     * @throws IOException 如果在读取输入流或计算哈希过程中发生异常
     * @since 1.1.0
     */
    public static byte[] md2(InputStream data) throws IOException {
        return digest(getMd2Digest(), data);
    }

    /**
     * 使用 MD2 算法对输入数据进行哈希处理，返回哈希值的字节数组
     * <p>
     * 该方法将输入的字符串数据转换为 UTF-8 编码的字节数组，然后使用 MD2 算法生成哈希值。
     *
     * @param data 需要进行哈希处理的字符串数据
     * @return MD2 哈希值的字节数组
     * @since 1.1.0
     */
    public static byte[] md2(String data) {
        return md2(StringUtils.getBytesUtf8(data));
    }

    /**
     * 将字节数组数据转换为MD2哈希值的十六进制字符串
     * <p>
     * 该方法使用MD2算法对输入的字节数组进行哈希计算，并将结果转换为十六进制字符串格式。
     *
     * @param data 输入的字节数组数据
     * @return MD2哈希值的十六进制字符串
     */
    public static String md2Hex(byte[] data) {
        return Hex.encodeHexString(md2(data));
    }

    /**
     * 将输入流中的数据使用 MD2 算法进行哈希处理，并将结果转换为十六进制字符串
     * <p>
     * 该方法首先对输入流中的内容进行 MD2 哈希计算，然后将哈希值以十六进制格式返回。
     *
     * @param data 输入流，包含需要哈希的数据
     * @return 哈希后的十六进制字符串
     * @throws IOException 如果在读取输入流或进行哈希计算过程中发生IO异常
     * @since 1.1.0
     */
    public static String md2Hex(InputStream data) throws IOException {
        return Hex.encodeHexString(md2(data));
    }

    /**
     * 将字符串数据转换为MD2哈希值的十六进制表示
     * <p>
     * 该方法接收一个字符串参数，计算其MD2哈希值，并将结果以十六进制字符串形式返回
     *
     * @param data 需要转换的字符串数据
     * @return MD2哈希值的十六进制字符串
     * @since 1.1.0
     */
    public static String md2Hex(String data) {
        return Hex.encodeHexString(md2(data));
    }

    /**
     * 使用 MD5 算法对字节数组进行哈希处理并返回结果
     * <p>
     * 该方法通过调用 MD5 摘要算法对输入的字节数组进行加密处理，返回加密后的字节数组。
     *
     * @param data 需要加密的字节数组
     * @return 加密后的字节数组
     * @since 1.1.0
     */
    public static byte[] md5(byte[] data) {
        return getMd5Digest().digest(data);
    }

    /**
     * 使用 MD5 算法对输入流进行哈希处理，返回字节数组结果
     * <p>
     * 该方法通过创建 MD5 哈希对象，读取输入流中的数据并计算其哈希值，最终返回字节数组形式的哈希结果。
     *
     * @param data 输入流，包含需要哈希的数据
     * @return 返回 MD5 哈希后的字节数组
     * @throws IOException 如果在读取输入流或计算哈希过程中发生 I/O 异常
     * @since 1.1.0
     */
    public static byte[] md5(InputStream data) throws IOException {
        return digest(getMd5Digest(), data);
    }

    /**
     * 使用 MD5 算法对字符串进行加密并返回字节数组
     * <p>
     * 该方法将输入的字符串转换为 UTF-8 编码的字节数组，然后使用 MD5 算法进行加密处理。
     *
     * @param data 需要加密的字符串数据
     * @return 加密后的字节数组
     * @since 1.1.0
     */
    public static byte[] md5(String data) {
        return md5(StringUtils.getBytesUtf8(data));
    }

    /**
     * 对字节数组数据进行 MD5 哈希计算并返回十六进制字符串
     * <p>
     * 该方法接收一个字节数组作为输入，计算其 MD5 哈希值，并将结果以十六进制字符串形式返回。
     *
     * @param data 需要进行 MD5 计算的字节数组
     * @return MD5 哈希值的十六进制字符串
     * @since 1.1.0
     */
    public static String md5Hex(byte[] data) {
        return Hex.encodeHexString(md5(data));
    }

    /**
     * 对输入的流数据进行 MD5 哈希计算，并返回十六进制格式的字符串结果
     * <p>
     * 该方法读取输入流中的数据，计算其 MD5 值，然后使用 Hex 编码工具将结果转换为十六进制字符串
     *
     * @param data 输入流，包含需要计算哈希的数据
     * @return 计算后的 MD5 值的十六进制字符串
     * @throws IOException 如果在读取输入流或计算哈希过程中发生 I/O 异常
     * @since 1.1.0
     */
    public static String md5Hex(InputStream data) throws IOException {
        return Hex.encodeHexString(md5(data));
    }

    /**
     * 对字符串数据进行 MD5 哈希处理并返回十六进制格式的结果
     * <p>
     * 该方法接收一个字符串参数，对其进行 MD5 哈希计算，并将结果以十六进制字符串形式返回。
     *
     * @param data 需要进行 MD5 哈希处理的字符串数据
     * @return MD5 哈希后的十六进制字符串
     * @since 1.1.0
     */
    public static String md5Hex(String data) {
        return Hex.encodeHexString(md5(data));
    }

    /**
     * 已弃用的SHA哈希计算方法
     * <p>
     * 使用SHA-1算法对输入数据进行哈希计算，返回字节数组结果。
     *
     * @param data 需要进行哈希计算的输入数据
     * @return 哈希计算后的字节数组
     * @since 1.1.0
     * @deprecated 该方法已弃用，请使用更安全的哈希算法替代
     */
    @Deprecated
    public static byte[] sha(byte[] data) {
        return sha1(data);
    }

    /**
     * 已弃用的方法，用于计算输入流的SHA哈希值
     * <p>
     * 该方法通过调用 sha1 方法实现，但已被标记为过时，建议使用更安全的哈希算法替代
     *
     * @param data 输入流，用于计算哈希值
     * @return 哈希值的字节数组
     * @throws IOException 如果发生输入输出错误
     * @since 1.1.0
     * @deprecated 不推荐使用，建议使用更安全的哈希算法
     */
    @Deprecated
    public static byte[] sha(InputStream data) throws IOException {
        return sha1(data);
    }

    /**
     * 已弃用的SHA哈希计算方法
     * <p>
     * 使用SHA算法对输入字符串进行哈希计算，返回字节数组。
     *
     * @param data 需要进行哈希计算的字符串数据
     * @return 哈希计算后的字节数组
     * @since 1.1.0
     * @deprecated 该方法已弃用，请使用更安全的哈希算法替代
     */
    @Deprecated
    public static byte[] sha(String data) {
        return sha1(data);
    }

    /**
     * 使用 SHA-1 算法对输入数据进行哈希处理并返回结果
     * <p>
     * 该方法将输入的字节数组通过 SHA-1 算法生成哈希值，并以字节数组形式返回
     *
     * @param data 需要进行哈希处理的输入数据，类型为字节数组
     * @return 返回经过 SHA-1 哈希处理后的字节数组
     * @since 1.1.0
     */
    public static byte[] sha1(byte[] data) {
        return getSha1Digest().digest(data);
    }

    /**
     * 使用 SHA-1 算法对输入流中的数据进行哈希处理，返回哈希值的字节数组
     * <p>
     * 该方法读取输入流中的数据，并使用 SHA-1 算法生成数据的哈希值，最终以字节数组形式返回。
     *
     * @param data 输入流，包含需要哈希的数据
     * @return 哈希值的字节数组
     * @throws IOException 如果在读取输入流或处理数据时发生 I/O 异常
     * @since 1.1.0
     */
    public static byte[] sha1(InputStream data) throws IOException {
        return digest(getSha1Digest(), data);
    }

    /**
     * 使用 SHA-1 算法对输入数据进行哈希处理，返回字节数组
     * <p>
     * 该方法将输入的字符串数据转换为 UTF-8 编码的字节数组，然后使用 SHA-1 算法生成哈希值。
     *
     * @param data 需要进行哈希处理的字符串数据
     * @return 哈希处理后的字节数组
     * @since 1.1.0
     */
    public static byte[] sha1(String data) {
        return sha1(StringUtils.getBytesUtf8(data));
    }

    /**
     * 对字节数组数据进行 SHA-1 哈希计算，并返回十六进制字符串形式的结果
     * <p>
     * 该方法使用 SHA-1 算法对输入的字节数组进行哈希处理，然后将结果转换为十六进制字符串格式。
     *
     * @param data 需要进行哈希处理的字节数组
     * @return 返回 SHA-1 哈希值的十六进制字符串
     * @since 1.1.0
     */
    public static String sha1Hex(byte[] data) {
        return Hex.encodeHexString(sha1(data));
    }

    /**
     * 对输入的 InputStream 数据进行 SHA-1 哈希计算，并返回十六进制格式的字符串结果
     * <p>
     * 该方法使用 SHA-1 算法对传入的输入流进行哈希处理，最终将结果转换为十六进制字符串形式返回
     *
     * @param data 输入流，包含需要进行哈希计算的数据
     * @return 返回 SHA-1 哈希值的十六进制字符串
     * @throws IOException 如果在读取输入流或进行哈希计算过程中发生 I/O 错误
     * @since 1.1.0
     */
    public static String sha1Hex(InputStream data) throws IOException {
        return Hex.encodeHexString(sha1(data));
    }

    /**
     * 对字符串数据进行 SHA-1 哈希计算并返回十六进制格式的结果
     * <p>
     * 该方法接收一个字符串参数，使用 SHA-1 算法生成其哈希值，然后将其转换为十六进制字符串格式返回。
     *
     * @param data 需要进行哈希计算的字符串数据
     * @return 返回 SHA-1 哈希值的十六进制字符串
     * @since 1.1.0
     */
    public static String sha1Hex(String data) {
        return Hex.encodeHexString(sha1(data));
    }

    /**
     * 使用 SHA-256 算法对输入数据进行哈希处理，返回哈希结果的字节数组
     * <p>
     * 该方法通过调用 SHA-256 哈希算法对传入的字节数组进行处理，生成对应的哈希值。
     *
     * @param data 需要进行哈希处理的字节数组
     * @return 哈希处理后的字节数组
     * @since 1.1.0
     */
    public static byte[] sha256(byte[] data) {
        return getSha256Digest().digest(data);
    }

    /**
     * 使用 SHA-256 算法对输入流中的数据进行哈希处理，返回哈希结果的字节数组
     * <p>
     * 该方法通过创建 SHA-256 哈希对象，读取输入流中的数据并计算哈希值，最终返回字节数组形式的哈希结果
     *
     * @param data 输入流，包含需要哈希处理的数据
     * @return 哈希结果的字节数组
     * @throws IOException 如果在读取输入流或计算哈希过程中发生 I/O 错误
     * @since 1.1.0
     */
    public static byte[] sha256(InputStream data) throws IOException {
        return digest(getSha256Digest(), data);
    }

    /**
     * 使用 SHA-256 算法对输入数据进行哈希处理，返回字节数组结果
     * <p>
     * 该方法将字符串数据转换为 UTF-8 编码的字节数组，然后使用 SHA-256 算法计算其哈希值。
     *
     * @param data 需要进行哈希处理的字符串数据
     * @return 数据的 SHA-256 哈希值，以字节数组形式返回
     * @since 1.1.0
     */
    public static byte[] sha256(String data) {
        return sha256(StringUtils.getBytesUtf8(data));
    }

    /**
     * 对字节数组数据进行 SHA-256 哈希计算，并返回十六进制字符串形式的结果
     * <p>
     * 该方法使用 SHA-256 算法对输入的字节数组进行哈希处理，然后将结果转换为十六进制字符串格式。
     *
     * @param data 需要进行哈希处理的字节数组
     * @return SHA-256 哈希结果的十六进制字符串
     * @since 1.1.0
     */
    public static String sha256Hex(byte[] data) {
        return Hex.encodeHexString(sha256(data));
    }

    /**
     * 对输入的 InputStream 数据进行 SHA-256 哈希计算，并返回十六进制格式的字符串结果
     * <p>
     * 该方法首先使用 SHA-256 算法对输入流中的数据进行哈希处理，然后将结果转换为十六进制字符串格式。
     *
     * @param data 输入的字节流数据
     * @return 返回 SHA-256 哈希值的十六进制字符串
     * @throws IOException 如果在读取输入流或处理数据时发生 I/O 错误
     * @since 1.1.0
     */
    public static String sha256Hex(InputStream data) throws IOException {
        return Hex.encodeHexString(sha256(data));
    }

    /**
     * 对输入的字符串数据进行 SHA-256 哈希计算，并返回十六进制格式的哈希值
     * <p>
     * 该方法使用 SHA-256 算法对传入的字符串进行加密处理，最终以十六进制字符串形式返回结果
     *
     * @param data 需要加密的原始字符串数据
     * @return 返回 SHA-256 哈希计算后的十六进制字符串
     * @since 1.1.0
     */
    public static String sha256Hex(String data) {
        return Hex.encodeHexString(sha256(data));
    }

    /**
     * 使用 SHA-384 算法对输入数据进行哈希处理，返回字节数组结果
     * <p>
     * 该方法通过调用 SHA-384 摘要算法，对传入的字节数组进行加密处理，并返回加密后的字节数组。
     *
     * @param data 需要进行哈希处理的输入数据，类型为字节数组
     * @return 加密后的字节数组
     * @since 1.1.0
     */
    public static byte[] sha384(byte[] data) {
        return getSha384Digest().digest(data);
    }

    /**
     * 使用 SHA-384 算法对输入流中的数据进行哈希处理，返回哈希结果的字节数组
     * <p>
     * 该方法通过创建 SHA-384 哈希对象，并对输入流中的数据进行处理，最终返回计算得到的哈希值。
     *
     * @param data 输入流，包含需要哈希处理的数据
     * @return 哈希处理后的字节数组
     * @throws IOException 如果在读取输入流或处理数据时发生异常
     * @since 1.1.0
     */
    public static byte[] sha384(InputStream data) throws IOException {
        return digest(getSha384Digest(), data);
    }

    /**
     * 使用 SHA-384 算法对输入数据进行哈希处理，返回字节数组
     * <p>
     * 该方法将输入的字符串数据转换为 UTF-8 编码的字节数组，然后使用 SHA-384 算法生成哈希值。
     *
     * @param data 需要进行哈希处理的字符串数据
     * @return 哈希处理后的字节数组
     * @since 1.1.0
     */
    public static byte[] sha384(String data) {
        return sha384(StringUtils.getBytesUtf8(data));
    }

    /**
     * 对字节数组进行 SHA-384 哈希计算，并返回十六进制字符串形式的结果
     * <p>
     * 该方法使用 SHA-384 算法对输入的字节数组进行哈希处理，然后将结果转换为十六进制字符串。
     *
     * @param data 需要进行哈希处理的字节数组
     * @return 返回 SHA-384 哈希值的十六进制字符串
     * @since 1.1.0
     */
    public static String sha384Hex(byte[] data) {
        return Hex.encodeHexString(sha384(data));
    }

    /**
     * 对输入的流数据进行 SHA-384 哈希计算，并返回十六进制格式的字符串结果
     * <p>
     * 该方法使用 SHA-384 算法对传入的输入流进行哈希处理，然后将结果转换为十六进制字符串
     *
     * @param data 输入流，包含需要哈希处理的数据
     * @return 返回 SHA-384 哈希值的十六进制字符串
     * @throws IOException 如果在读取输入流或处理数据时发生异常
     * @since 1.1.0
     */
    public static String sha384Hex(InputStream data) throws IOException {
        return Hex.encodeHexString(sha384(data));
    }

    /**
     * 对输入的字符串数据进行 SHA-384 哈希计算，并返回十六进制格式的结果
     * <p>
     * 该方法使用 SHA-384 算法对传入的字符串进行哈希处理，然后将结果转换为十六进制字符串形式。
     *
     * @param data 需要进行哈希处理的字符串数据
     * @return 返回 SHA-384 哈希计算后的十六进制字符串
     * @since 1.1.0
     */
    public static String sha384Hex(String data) {
        return Hex.encodeHexString(sha384(data));
    }

    /**
     * 使用 SHA-512 算法对输入数据进行哈希处理，返回哈希结果的字节数组
     * <p>
     * 该方法通过调用 SHA-512 哈希算法对传入的字节数组进行处理，生成对应的哈希值。
     *
     * @param data 需要进行哈希处理的字节数组
     * @return 哈希处理后的字节数组
     * @since 1.1.0
     */
    public static byte[] sha512(byte[] data) {
        return getSha512Digest().digest(data);
    }

    /**
     * 使用 SHA-512 算法对输入流中的数据进行哈希处理，返回字节数组
     * <p>
     * 该方法通过调用 SHA-512 哈希算法，对传入的输入流数据进行处理，生成对应的哈希值
     *
     * @param data 需要进行哈希处理的输入流
     * @return 哈希处理后的字节数组
     * @throws IOException 如果在读取输入流或处理数据时发生异常
     * @since 1.1.0
     */
    public static byte[] sha512(InputStream data) throws IOException {
        return digest(getSha512Digest(), data);
    }

    /**
     * 使用 SHA-512 算法对字符串数据进行哈希处理，返回字节数组
     * <p>
     * 该方法将输入的字符串转换为 UTF-8 编码的字节数组，然后使用 SHA-512 算法生成哈希值。
     *
     * @param data 需要哈希处理的字符串数据
     * @return 哈希处理后的字节数组
     * @since 1.1.0
     */
    public static byte[] sha512(String data) {
        return sha512(StringUtils.getBytesUtf8(data));
    }

    /**
     * 对字节数组数据进行 SHA-512 哈希计算，并返回十六进制格式的字符串结果
     * <p>
     * 该方法使用 SHA-512 算法对输入的字节数组进行哈希处理，然后将结果转换为十六进制字符串形式。
     *
     * @param data 需要进行哈希计算的字节数组
     * @return 返回 SHA-512 哈希值的十六进制字符串
     * @since 1.1.0
     */
    public static String sha512Hex(byte[] data) {
        return Hex.encodeHexString(sha512(data));
    }

    /**
     * 对输入的字节流数据进行 SHA-512 哈希计算，并返回十六进制格式的字符串结果
     * <p>
     * 该方法首先使用 SHA-512 算法对输入的字节流进行哈希处理，然后将结果转换为十六进制字符串格式。
     *
     * @param data 输入的字节流数据
     * @return SHA-512 哈希值的十六进制字符串
     * @throws IOException 如果在处理字节流时发生输入输出异常
     * @since 1.1.0
     */
    public static String sha512Hex(InputStream data) throws IOException {
        return Hex.encodeHexString(sha512(data));
    }

    /**
     * 对字符串数据进行 SHA-512 哈希计算并返回十六进制格式的结果
     * <p>
     * 该方法接收一个字符串参数，使用 SHA-512 算法生成其哈希值，并将结果转换为十六进制字符串格式返回。
     *
     * @param data 需要进行哈希计算的字符串数据
     * @return 返回 SHA-512 哈希值的十六进制字符串
     * @since 1.1.0
     */
    public static String sha512Hex(String data) {
        return Hex.encodeHexString(sha512(data));
    }

    /**
     * 已弃用的SHA哈希计算方法，返回十六进制字符串
     * <p>
     * 该方法已过时，建议使用其他哈希算法替代
     *
     * @param data 需要计算哈希的数据
     * @return 十六进制格式的哈希字符串
     * @since 1.1.0
     * @deprecated
     */
    @Deprecated
    public static String shaHex(byte[] data) {
        return sha1Hex(data);
    }

    /**
     * 已弃用的方法，用于计算输入流数据的SHA-1哈希值并返回十六进制字符串
     * <p>
     * 该方法已过时，建议使用其他方法替代。
     *
     * @param data 输入流数据
     * @return 输入数据的SHA-1哈希值十六进制字符串
     * @throws IOException 如果读取输入流时发生异常
     * @since 1.1.0
     * @deprecated
     */
    @Deprecated
    public static String shaHex(InputStream data) throws IOException {
        return sha1Hex(data);
    }

    /**
     * 已弃用的SHA哈希计算方法，用于生成数据的SHA十六进制字符串
     * <p>
     * 该方法已过时，建议使用更安全的加密算法替代。原功能为对输入数据进行SHA哈希运算并返回十六进制格式的结果。
     *
     * @param data 需要进行哈希运算的输入数据
     * @return 数据的SHA十六进制字符串
     * @since 1.1.0
     * @deprecated 不推荐使用，使用更安全的加密方式
     */
    @Deprecated
    public static String shaHex(String data) {
        return sha1Hex(data);
    }

    /**
     * 更新消息摘要
     * <p>
     * 将指定的字节数组值添加到消息摘要中，并返回更新后的消息摘要对象。
     *
     * @param messageDigest 消息摘要对象
     * @param valueToDigest 需要进行摘要计算的字节数组
     * @return 更新后的消息摘要对象
     * @since 1.1.0
     */
    public static MessageDigest updateDigest(MessageDigest messageDigest, byte[] valueToDigest) {
        messageDigest.update(valueToDigest);
        return messageDigest;
    }

    /**
     * 更新消息摘要
     * <p>
     * 将输入流中的数据逐步读取并更新到指定的消息摘要对象中。
     *
     * @param digest 消息摘要对象
     * @param data   输入流，包含需要处理的数据
     * @return 更新后的消息摘要对象
     * @throws IOException 如果在读取输入流时发生异常
     * @since 1.1.0
     */
    public static MessageDigest updateDigest(MessageDigest digest, InputStream data) throws IOException {
        byte[] buffer = new byte[1024];

        for (int read = data.read(buffer, 0, 1024); read > -1; read = data.read(buffer, 0, 1024)) {
            digest.update(buffer, 0, read);
        }

        return digest;
    }

    /**
     * 更新消息摘要对象的内容
     * <p>
     * 将指定的字符串值转换为字节数组，并使用 UTF-8 编码更新消息摘要对象。
     *
     * @param messageDigest 消息摘要对象
     * @param valueToDigest 需要摘要的字符串值
     * @return 更新后的消息摘要对象
     * @since 1.1.0
     */
    public static MessageDigest updateDigest(MessageDigest messageDigest, String valueToDigest) {
        messageDigest.update(StringUtils.getBytesUtf8(valueToDigest));
        return messageDigest;
    }
}
