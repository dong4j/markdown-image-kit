package info.dong4j.idea.plugin.util;

import info.dong4j.idea.plugin.util.date.DateFormatUtils;

import org.jetbrains.annotations.NotNull;

import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.extern.slf4j.Slf4j;

/**
 * 占位符解析工具类
 * <p>
 * 用于解析图片重命名模板中的占位符，支持以下占位符：
 * <ul>
 *   <li>${datetime:format} - 日期时间格式化，如 ${datetime:yyyyMMdd}、${datetime:yyyy-MM-dd_HHmmss}</li>
 *   <li>${string:length} - 随机字符串，如 ${string:6} 生成6位随机字符串</li>
 *   <li>${number:length} - 随机数字，如 ${number:6} 生成6位随机数字</li>
 *   <li>${filename} - 原文件名（不含扩展名）</li>
 * </ul>
 * <p>
 * 示例模板：
 * <ul>
 *   <li>${filename} - 保持原文件名</li>
 *   <li>${datetime:yyyyMMdd}_${string:6} - 20250101_AbCdEf</li>
 *   <li>img_${datetime:yyyyMMddHHmmss}_${number:4} - img_20250101123456_1234</li>
 *   <li>${datetime:yyyy-MM-dd}_${filename} - 2025-01-01_原文件名</li>
 * </ul>
 * <p>
 * 注意：扩展名会自动保留，无需在模板中指定
 *
 * @author dong4j
 * @version 1.0.0
 * @date 2025.11.01
 * @since 2.2.0
 */
@Slf4j
public class PlaceholderParser {

    /** 日期时间占位符正则：${datetime:format} */
    private static final Pattern DATETIME_PATTERN = Pattern.compile("\\$\\{datetime:([^}]+)}");
    /** 随机字符串占位符正则：${string:length} */
    private static final Pattern STRING_PATTERN = Pattern.compile("\\$\\{string:(\\d+)}");
    /** 随机数字占位符正则：${number:length} */
    private static final Pattern NUMBER_PATTERN = Pattern.compile("\\$\\{number:(\\d+)}");
    /** 文件名占位符：${filename} */
    private static final String FILENAME_PLACEHOLDER = "${filename}";

    /**
     * 解析重命名模板
     * <p>
     * 根据提供的模板和原始文件名，解析所有占位符并返回最终的文件名
     *
     * @param template     重命名模板
     * @param originalName 原始文件名（包含扩展名）
     * @return 解析后的文件名
     * @since 2.2.0
     */
    public static String parse(@NotNull String template, @NotNull String originalName) {
        if (template.trim().isEmpty()) {
            log.warn("重命名模板为空，使用原文件名");
            return originalName;
        }

        String result = template;

        // 提取文件名和扩展名
        String filenameWithoutExt = ImageUtils.getFileNameWithoutExtension(originalName);
        String extension = ImageUtils.getFileExtension(originalName);

        // 移除文件名中的空格
        filenameWithoutExt = filenameWithoutExt.replaceAll("\\s+", "");

        try {
            // 1. 替换日期时间占位符
            result = parseDatetime(result);

            // 2. 替换随机字符串占位符
            result = parseRandomString(result);

            // 3. 替换随机数字占位符
            result = parseRandomNumber(result);

            // 4. 替换文件名占位符
            result = result.replace(FILENAME_PLACEHOLDER, filenameWithoutExt);

            // 5. 自动添加原文件扩展名
            result = result + extension;
            
            // 移除结果中的所有空格
            result = result.replaceAll("\\s+", "");

        } catch (Exception e) {
            log.error("解析重命名模板时发生错误: {}", e.getMessage(), e);
            return originalName;
        }

        return result;
    }

    /**
     * 解析日期时间占位符
     * <p>
     * 将 ${datetime:format} 替换为格式化的日期时间字符串
     *
     * @param text 待处理的文本
     * @return 替换后的文本
     * @since 2.2.0
     */
    private static String parseDatetime(@NotNull String text) {
        Matcher matcher = DATETIME_PATTERN.matcher(text);
        StringBuilder sb = new StringBuilder();

        while (matcher.find()) {
            String format = matcher.group(1);
            String dateTime;
            try {
                dateTime = DateFormatUtils.format(new Date(), format);
            } catch (Exception e) {
                log.warn("日期格式 [{}] 不正确，使用默认格式", format);
                dateTime = DateFormatUtils.format(new Date(), "yyyyMMdd");
            }
            matcher.appendReplacement(sb, Matcher.quoteReplacement(dateTime));
        }
        matcher.appendTail(sb);

        return sb.toString();
    }

    /**
     * 解析随机字符串占位符
     * <p>
     * 将 ${string:length} 替换为指定长度的随机字符串（大小写字母混合）
     *
     * @param text 待处理的文本
     * @return 替换后的文本
     * @since 2.2.0
     */
    private static String parseRandomString(@NotNull String text) {
        return parseRandomPattern(text, STRING_PATTERN, 32, CharacterUtils::getRandomString);
    }

    /**
     * 解析随机数字占位符
     * <p>
     * 将 ${number:length} 替换为指定长度的随机数字字符串
     *
     * @param text 待处理的文本
     * @return 替换后的文本
     * @since 2.2.0
     */
    private static String parseRandomNumber(@NotNull String text) {
        return parseRandomPattern(text, NUMBER_PATTERN, 16, CharacterUtils::getRandomNumber);
    }

    /**
     * 通用随机模式解析方法
     *
     * @param text          待处理的文本
     * @param pattern       正则表达式模式
     * @param maxLength     最大长度限制
     * @param generatorFunc 随机字符生成函数
     * @return 替换后的文本
     */
    private static String parseRandomPattern(@NotNull String text,
                                             @NotNull Pattern pattern,
                                             int maxLength,
                                             @NotNull java.util.function.Function<Integer, String> generatorFunc) {
        Matcher matcher = pattern.matcher(text);
        StringBuilder sb = new StringBuilder();

        while (matcher.find()) {
            int length = Integer.parseInt(matcher.group(1));
            // 限制长度范围
            length = Math.max(1, Math.min(length, maxLength));
            String randomStr = generatorFunc.apply(length);
            matcher.appendReplacement(sb, Matcher.quoteReplacement(randomStr));
        }
        matcher.appendTail(sb);

        return sb.toString();
    }

    /**
     * 验证模板格式是否正确
     * <p>
     * 检查模板中的占位符格式是否正确
     *
     * @param template 重命名模板
     * @return 如果模板格式正确返回 true，否则返回 false
     * @since 2.2.0
     */
    public static boolean validateTemplate(@NotNull String template) {
        if (template.trim().isEmpty()) {
            return false;
        }

        try {
            // 检查是否有未闭合的占位符
            int openBraces = 0;
            for (char c : template.toCharArray()) {
                if (c == '{') {
                    openBraces++;
                } else if (c == '}') {
                    openBraces--;
                    if (openBraces < 0) {
                        return false;
                    }
                }
            }

            return openBraces == 0;
        } catch (Exception e) {
            log.error("验证模板时发生错误: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 获取默认模板
     * <p>
     * 返回默认的重命名模板
     *
     * @return 默认模板字符串
     * @since 2.2.0
     */
    public static String getDefaultTemplate() {
        return "${filename}";
    }

    /**
     * 获取预设模板列表
     * <p>
     * 返回一组常用的预设模板供用户选择
     *
     * @return 预设模板数组
     * @since 2.2.0
     */
    public static String[] getPresetTemplates() {
        return new String[] {
            "${filename}",
            "${datetime:yyyyMMdd}_${string:6}",
            "${datetime:yyyyMMddHHmmss}_${string:4}",
            "${datetime:yyyy-MM-dd}_${filename}",
            "img_${datetime:yyyyMMdd}_${number:6}",
            "${string:6}",
            "${datetime:yyyyMMdd_HHmmss}",
            "MIK-${string:6}"
        };
    }

    /**
     * 获取预设模板说明
     * <p>
     * 返回预设模板的说明文本
     *
     * @return 预设模板说明数组
     * @since 2.2.0
     */
    public static String[] getPresetTemplateDescriptions() {
        return new String[] {
            "保持原文件名",
            "日期_随机字符串 (20250101_AbCdEf)",
            "日期时间_随机字符串 (20250101123456_AbCd)",
            "日期-原文件名 (2025-01-01_filename)",
            "img_日期_随机数字 (img_20250101_123456)",
            "随机字符串 (AbCdEf)",
            "日期时间 (20250101_123456)",
            "MIK前缀_随机字符串 (MIK-AbCdEf)"
        };
    }
}

