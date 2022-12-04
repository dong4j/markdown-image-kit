/*
 * MIT License
 *
 * Copyright (c) 2022 dong4j <dong4j@gmail.com>
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

package info.dong4j.idea.plugin.util.date;

import java.text.FieldPosition;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * <p>DatePrinter is the "missing" interface for the format methods of
 * {@link java.text.DateFormat}.</p>
 *
 * @since 3.2
 */
public interface DatePrinter {

    /**
     * <p>Formats a millisecond {@code long} value.</p>
     *
     * @param millis the millisecond value to format
     * @return the formatted string
     * @since 2.1
     */
    String format(long millis);

    /**
     * <p>Formats a {@code Date} object using a {@code GregorianCalendar}.</p>
     *
     * @param date the date to format
     * @return the formatted string
     */
    String format(Date date);

    /**
     * <p>Formats a {@code Calendar} object.</p>
     *
     * @param calendar the calendar to format
     * @return the formatted string
     */
    String format(Calendar calendar);

    /**
     * <p>Formats a milliseond {@code long} value into the
     * supplied {@code StringBuffer}.</p>
     *
     * @param millis the millisecond value to format
     * @param buf    the buffer to format into
     * @return the specified string buffer
     */
    StringBuffer format(long millis, StringBuffer buf);

    /**
     * <p>Formats a {@code Date} object into the
     * supplied {@code StringBuffer} using a {@code GregorianCalendar}.</p>
     *
     * @param date the date to format
     * @param buf  the buffer to format into
     * @return the specified string buffer
     */
    StringBuffer format(Date date, StringBuffer buf);

    /**
     * <p>Formats a {@code Calendar} object into the
     * supplied {@code StringBuffer}.</p>
     *
     * @param calendar the calendar to format
     * @param buf      the buffer to format into
     * @return the specified string buffer
     */
    StringBuffer format(Calendar calendar, StringBuffer buf);

    // Accessors
    //-----------------------------------------------------------------------

    /**
     * <p>Gets the pattern used by this printer.</p>
     *
     * @return the pattern, {@link java.text.SimpleDateFormat} compatible
     */
    String getPattern();

    /**
     * <p>Gets the time zone used by this printer.</p>
     *
     * <p>This zone is always used for {@code Date} printing. </p>
     *
     * @return the time zone
     */
    TimeZone getTimeZone();

    /**
     * <p>Gets the locale used by this printer.</p>
     *
     * @return the locale
     */
    Locale getLocale();

    /**
     * <p>Formats a {@code Date}, {@code Calendar} or
     * {@code Long} (milliseconds) object.</p>
     * <p>
     * See {@link java.text.DateFormat#format(Object, StringBuffer, FieldPosition)}
     *
     * @param obj        the object to format
     * @param toAppendTo the buffer to append to
     * @param pos        the position - ignored
     * @return the buffer passed in
     */
    StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition pos);
}
