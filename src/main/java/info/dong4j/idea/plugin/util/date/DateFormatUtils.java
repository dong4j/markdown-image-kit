//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package info.dong4j.idea.plugin.util.date;

import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * <p>Description: </p>
 *
 * @author dong4j
 * @version 1.0.0
 * @email "mailto:dong4j@gmail.com"
 * @date 2021.02.14 23:15
 * @since 1.1.0
 */
public class DateFormatUtils {
    /** SMTP_DATETIME_FORMAT */
    public static final FastDateFormat SMTP_DATETIME_FORMAT;

    /**
     * Date format utils
     *
     * @since 1.1.0
     */
    public DateFormatUtils() {
    }

    /**
     * Format
     *
     * @param date    date
     * @param pattern pattern
     * @return the string
     * @since 1.1.0
     */
    public static String format(Date date, String pattern) {
        return format(date, pattern, null, null);
    }

    /**
     * Format
     *
     * @param date     date
     * @param pattern  pattern
     * @param timeZone time zone
     * @param locale   locale
     * @return the string
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
