package info.dong4j.idea.plugin.util;

import org.jetbrains.annotations.Contract;

import java.util.HashMap;
import java.util.Map;

/**
 * <p>Company: 科大讯飞股份有限公司-四川分公司</p>
 * <p>Description:替换字符串中 ${} 或者 {} 等占位符的工具类</p>
 *
 * @author dong4j
 * @date 2018 -08-15 10:27
 * @email sjdong3 @iflytek.com
 */
public abstract class ParserUtils {
    @Contract(" -> fail")
    private ParserUtils() {
        throw new RuntimeException("Tool class does not support instantiation");
    }

    /**
     * 将字符串text中由openToken和closeToken组成的占位符依次替换为args数组中的值
     *
     * @param openToken  the open token     占位符开始
     * @param closeToken the close token    占位符结束
     * @param text       the text           包含占位符的字符串
     * @param args       the args           被替换的参数
     * @return string string
     */
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
        final StringBuilder builder = new StringBuilder();
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
     * 解析使用 ${} 的占位符
     *
     * @param text the text
     * @param args the args
     * @return the string
     */
    public static String parse0(String text, Object... args) {
        return parse("${", "}", text, args);
    }

    /**
     * 解析使用 {} 的占位符
     *
     * @param text the text
     * @param args the args
     * @return the string
     */
    public static String parse1(String text, Object... args) {
        return parse("{", "}", text, args);
    }

    /**
     * 解析 ![xxx](yyy)
     *
     * @param text the text
     * @return the map describe = xxx; file = yyy
     */
    public static Map<String, String> parseImageTag(String text){
        int start = text.indexOf("![");
        int end = text.indexOf("]");

        String describe = text.substring(start + 2, end);
        String file = text.substring(text.indexOf("(") + 1, text.indexOf(")"));
        return new HashMap<String, String>(1){
            {
                put(describe, file);
            }
        };
    }
}