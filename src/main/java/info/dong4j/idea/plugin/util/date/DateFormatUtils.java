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

//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package info.dong4j.idea.plugin.util.date;

import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * <p>Company: 成都返空汇网络技术有限公司 </p>
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
