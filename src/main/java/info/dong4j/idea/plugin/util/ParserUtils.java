/*
 * MIT License
 *
 * Copyright (c) 2021 dong4j <dong4j@gmail.com>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package info.dong4j.idea.plugin.util;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

/**
 * <p>Company: no company</p>
 * <p>Description:替换字符串中 ${} 或者 {} 等占位符的工具类</p>
 *
 * @author dong4j
 * @version 0.0.1
 * @email "mailto:dong4j@gmail.com"
 * @date 2018.08.15 10:27
 * @since 0.0.1
 */
public final class ParserUtils {
    /**
     * Parser utils
     *
     * @since 0.0.1
     */
    @Contract(" -> fail")
    private ParserUtils() {
        throw new RuntimeException("Tool class does not support instantiation");
    }

    /**
     * 解析使用 ${} 的占位符
     *
     * @param text the text
     * @param args the args
     * @return the string
     * @since 0.0.1
     */
    public static String parse0(String text, Object... args) {
        return parse("${", "}", text, args);
    }

    /**
     * 将字符串text中由openToken和closeToken组成的占位符依次替换为args数组中的值
     *
     * @param openToken  the open token     占位符开始
     * @param closeToken the close token    占位符结束
     * @param text       the text           包含占位符的字符串
     * @param args       the args           被替换的参数
     * @return string string
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
     * 解析使用 {} 的占位符
     *
     * @param text the text
     * @param args the args
     * @return the string
     * @since 0.0.1
     */
    public static String parse1(String text, Object... args) {
        return parse("{", "}", text, args);
    }

    /**
     * Parse 2 string.
     *
     * @param text  the text
     * @param title the title
     * @param path  the path
     * @return the string
     * @since 0.0.1
     */
    public static String parse2(String text, String title, String path) {
        return text.replaceAll("\\$\\{title}", title).replaceAll("\\$\\{path}", path);
    }

    /**
     * 解析 ![xxx](yyy)
     *
     * @param text the text
     * @return the map describe = xxx; file = yyy
     * @since 0.0.1
     */
    @NotNull
    @Contract("_ -> new")
    public static Map<String, String> parseImageTag(String text) {
        int start = text.indexOf("![");
        int end = text.indexOf("]");

        String describe = text.substring(start + 2, end);
        String file = text.substring(text.indexOf("(") + 1, text.indexOf(")"));
        return new HashMap<String, String>(1) {
            private static final long serialVersionUID = 6465853189583120987L;

            {
                this.put(describe, file);
            }
        };
    }
}
