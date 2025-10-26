//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package info.dong4j.idea.plugin.util.date;

import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * 日期格式工具类
 * <p>
 * 提供日期格式化相关工具方法，支持自定义格式、时区和语言环境的日期格式化操作。
 * 包含一个静态常量用于 SMTP 协议中的日期时间格式。
 *
 * @author dong4j
 * @version 1.0.0
 * @date 2021.02.14
 * @since 1.1.0
 */
public class DateFormatUtils {
    /** SMTP 日期时间格式化对象，用于格式化邮件相关的日期时间信息 */
    public static final FastDateFormat SMTP_DATETIME_FORMAT;

    /**
     * 日期格式化工具类的构造函数
     * <p>
     * 初始化日期格式化工具类，用于提供日期格式化相关功能
     *
     * @since 1.1.0
     */
    public DateFormatUtils() {
    }

    /**
     * 根据日期和格式模式将日期对象格式化为字符串
     * <p>
     * 该方法用于将给定的日期对象按照指定的格式模式转换为对应的字符串表示形式。
     *
     * @param date    要格式化的日期对象
     * @param pattern 日期格式模式，如 "yyyy-MM-dd"
     * @return 格式化后的字符串
     * @since 1.1.0
     */
    public static String format(Date date, String pattern) {
        return format(date, pattern, null, null);
    }

    /**
     * 根据指定日期、格式、时区和语言环境格式化日期为字符串
     * <p>
     * 使用 FastDateFormat 实例将日期对象按照指定格式、时区和语言环境转换为对应的字符串表示。
     *
     * @param date     要格式化的日期对象
     * @param pattern  日期格式模式，如 "yyyy-MM-dd"
     * @param timeZone 时区信息，用于指定日期格式化的时区
     * @param locale   语言环境，用于指定日期格式化的语言和区域设置
     * @return 格式化后的日期字符串
     * @since 1.1.0
     */
    public static String format(Date date, String pattern, TimeZone timeZone, Locale locale) {
        FastDateFormat df = FastDateFormat.getInstance(pattern, timeZone, locale);
        return df.format(date);
    }

    static {
        SMTP_DATETIME_FORMAT = FastDateFormat.getInstance("EEE, dd MMM yyyy HH:mm:ss Z", Locale.US);
    }
}
