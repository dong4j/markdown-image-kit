package info.dong4j.idea.plugin.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;

/**
 * IO 工具类
 * <p>
 * 提供与输入输出流相关的实用方法，包括字节流转换为字符串、流之间的数据复制等功能。
 * 该类主要用于简化 IO 操作，提高代码的可读性和复用性。
 * </p>
 *
 * @author dong4j
 * @version 1.0.0
 * @date 2021.02.14
 * @since 1.1.0
 */
public class IOUtils {
    /**
     * 将字节数组转换为字符串
     * <p>
     * 使用系统默认字符集将输入的字节数组转换为对应的字符串表示。
     *
     * @param input 输入的字节数组
     * @return 转换后的字符串
     * @throws IOException 如果转换过程中发生I/O错误
     * @since 1.1.0
     */
    @Deprecated
    public static String toString(byte[] input) throws IOException {
        return new String(input, Charset.defaultCharset());
    }

    /**
     * 将字节数组转换为字符串
     * <p>
     * 使用指定的编码格式将字节数组转换为对应的字符串表示
     *
     * @param input    需要转换的字节数组
     * @param encoding 转换时使用的字符编码
     * @return 转换后的字符串
     * @throws IOException 如果编码不支持或转换过程中发生错误
     * @since 1.1.0
     */
    public static String toString(byte[] input, String encoding) throws IOException {
        return new String(input, Charsets.toCharset(encoding));
    }

    /**
     * 将输入流的内容复制到输出流中
     * <p>
     * 该方法用于将输入流中的数据复制到输出流中，并返回实际复制的字节数。如果复制的字节数超过
     * Integer.MAX_VALUE，则返回 -1。
     *
     * @param input  要读取数据的输入流
     * @param output 要写入数据的输出流
     * @return 实际复制的字节数，若超过 Integer.MAX_VALUE 则返回 -1
     * @throws IOException 如果在复制过程中发生I/O错误
     * @since 1.1.0
     */
    public static int copy(InputStream input, OutputStream output) throws IOException {
        long count = copyLarge(input, output);
        return count > 2147483647L ? -1 : (int) count;
    }

    /**
     * 将输入流中的数据复制到输出流中
     * <p>
     * 该方法使用指定的缓冲区大小，将输入流的内容复制到输出流中。
     *
     * @param input      要读取数据的输入流
     * @param output     要写入数据的输出流
     * @param bufferSize 缓冲区大小，用于控制每次读取和写入的数据量
     * @return 复制的字节数
     * @throws IOException 如果在读取或写入过程中发生I/O错误
     * @since 1.1.0
     */
    public static long copy(InputStream input, OutputStream output, int bufferSize) throws IOException {
        return copyLarge(input, output, new byte[bufferSize]);
    }

    /**
     * 复制大量数据从输入流到输出流
     * <p>
     * 该方法用于高效地将数据从输入流复制到输出流，适用于处理大文件或大数据量的传输。
     *
     * @param input  要读取数据的输入流
     * @param output 要写入数据的输出流
     * @return 复制的字节数
     * @throws IOException 如果在读取或写入过程中发生I/O错误
     * @since 1.1.0
     */
    public static long copyLarge(InputStream input, OutputStream output) throws IOException {
        return copy(input, output, 4096);
    }

    /**
     * 复制大量数据从输入流到输出流
     * <p>
     * 使用指定的字节数组作为缓冲区，将输入流中的数据复制到输出流中，适用于复制大量数据的场景。
     *
     * @param input  要读取数据的输入流
     * @param output 要写入数据的输出流
     * @param buffer 用于存储数据的字节数组缓冲区
     * @return 复制的总字节数
     * @throws IOException 如果在读取或写入过程中发生I/O错误
     * @since 1.1.0
     */
    public static long copyLarge(InputStream input, OutputStream output, byte[] buffer) throws IOException {
        long count;
        int n;
        for (count = 0L; -1 != (n = input.read(buffer)); count += n) {
            output.write(buffer, 0, n);
        }

        return count;
    }
}
