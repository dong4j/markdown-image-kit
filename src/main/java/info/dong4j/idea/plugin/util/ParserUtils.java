package info.dong4j.idea.plugin.util;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.Serial;
import java.util.HashMap;
import java.util.Map;

/**
 * 字符串解析工具类
 * <p>
 * 提供字符串中占位符的解析功能，支持多种占位符格式，如 ${}、{}、$ {title}、$ {path} 以及 ![xxx](yyy) 等。
 * 可用于模板字符串的动态替换和图片标签的解析。
 *
 * @author dong4j
 * @version 0.0.1
 * @date 2018.08.15
 * @since 0.0.1
 */
@SuppressWarnings("D")
public final class ParserUtils {
    /**
     * 禁止实例化工具类
     * <p>
     * 该构造函数用于防止外部实例化 ParserUtils 工具类，确保其作为静态工具类使用。
     *
     * @since 0.0.1
     */
    @Contract(" -> fail")
    private ParserUtils() {
        throw new RuntimeException("Tool class does not support instantiation");
    }

    /**
     * 解析使用 ${} 的占位符
     * <p>
     * 将文本中的 ${} 占位符替换为对应的参数值
     *
     * @param text 要解析的文本
     * @param args 用于替换占位符的参数列表
     * @return 替换后的字符串
     * @since 0.0.1
     */
    public static String parse0(String text, Object... args) {
        return parse("${", "}", text, args);
    }

    /**
     * 将字符串text中由openToken和closeToken组成的占位符依次替换为args数组中的值
     * <p>
     * 该方法用于解析并替换字符串中的占位符。通过查找openToken和closeToken之间的内容，并用args数组中的对应值进行替换。支持转义字符，即当占位符前有反斜杠时，反斜杠会被忽略。
     *
     * @param openToken  占位符开始的标记
     * @param closeToken 占位符结束的标记
     * @param text       包含占位符的原始字符串
     * @param args       用于替换占位符的参数数组
     * @return 替换后的字符串
     * @since 0.0.1
     */
    @Contract("_, _, _, null -> param3")
    public static String parse(String openToken, String closeToken, String text, Object... args) {
        if (args == null || args.length <= 0) {
            return text;
        }
        int argsIndex = 0;

        if (text == null || text.isEmpty()) {
            return "";
        }
        char[] src = text.toCharArray();
        int offset = 0;
        // search open token
        int start = text.indexOf(openToken, offset);
        if (start == -1) {
            return text;
        }
        StringBuilder builder = new StringBuilder();
        StringBuilder expression = null;
        while (start > -1) {
            if (start > 0 && src[start - 1] == '\\') {
                // this open token is escaped. remove the backslash and continue.
                builder.append(src, offset, start - offset - 1).append(openToken);
                offset = start + openToken.length();
            } else {
                // found open token. let's search close token.
                if (expression == null) {
                    expression = new StringBuilder();
                } else {
                    expression.setLength(0);
                }
                builder.append(src, offset, start - offset);
                offset = start + openToken.length();
                int end = text.indexOf(closeToken, offset);
                while (end > -1) {
                    if (end > offset && src[end - 1] == '\\') {
                        // this close token is escaped. remove the backslash and continue.
                        expression.append(src, offset, end - offset - 1).append(closeToken);
                        offset = end + closeToken.length();
                        end = text.indexOf(closeToken, offset);
                    } else {
                        expression.append(src, offset, end - offset);
                        offset = end + closeToken.length();
                        break;
                    }
                }
                if (end == -1) {
                    // close token was not found.
                    builder.append(src, start, src.length - start);
                    offset = src.length;
                } else {
                    String value = (argsIndex <= args.length - 1) ?
                                   (args[argsIndex] == null ? "" : args[argsIndex].toString()) : expression.toString();
                    builder.append(value);
                    offset = end + closeToken.length();
                    argsIndex++;
                }
            }
            start = text.indexOf(openToken, offset);
        }
        if (offset < src.length) {
            builder.append(src, offset, src.length - offset);
        }
        return builder.toString();
    }

    /**
     * 解析包含 {} 占位符的文本，并用提供的参数替换占位符
     * <p>
     * 该方法用于处理类似 "Hello {0}" 的格式化字符串，将其中的 {0}、{1} 等占位符
     * 替换为对应的参数值。
     *
     * @param text 要解析的文本，其中包含占位符
     * @param args 用于替换占位符的参数列表
     * @return 替换后的字符串
     * @since 0.0.1
     */
    public static String parse1(String text, Object... args) {
        return parse("{", "}", text, args);
    }

    /**
     * 将模板字符串中的占位符替换为实际值
     * <p>
     * 该方法用于替换字符串中的 ${title} 和 ${path} 占位符为传入的对应参数值
     *
     * @param text  模板字符串
     * @param title 替换 ${title} 的实际值
     * @param path  替换 ${path} 的实际值
     * @return 替换后的字符串
     * @since 0.0.1
     */
    public static String parse2(String text, String title, String path) {
        return text.replaceAll("\\$\\{title}", title == null ? "" : title).replaceAll("\\$\\{path}", path);
    }

    /**
     * 解析图片标签 ![xxx](yyy)
     * <p>
     * 从给定的文本中提取图片描述和文件路径，返回包含这两个键值对的Map。
     *
     * @param text 要解析的文本内容
     * @return 包含描述和文件路径的Map，键为"describe"，值为xxx；键为"file"，值为yyy
     * @throws IllegalArgumentException 如果文本格式不正确，无法解析图片标签
     * @since 0.0.1
     */
    @NotNull
    @Contract("_ -> new")
    public static Map<String, String> parseImageTag(String text) {
        int start = text.indexOf("![");
        int end = text.indexOf("]");

        String describe = text.substring(start + 2, end);
        String file = text.substring(text.indexOf("(") + 1, text.indexOf(")"));
        return new HashMap<>(1) {
            /** 序列化版本号，用于确保类的兼容性 */
            @Serial
            private static final long serialVersionUID = 6465853189583120987L;

            {
                this.put(describe, file);
            }
        };
    }
}
