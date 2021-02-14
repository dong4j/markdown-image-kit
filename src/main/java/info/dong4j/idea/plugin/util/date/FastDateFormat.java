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

import org.jetbrains.annotations.NotNull;

import java.text.FieldPosition;
import java.text.Format;
import java.text.ParsePosition;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class FastDateFormat extends Format implements DatePrinter {
    private static final long serialVersionUID = 2L;
    public static final int FULL = 0;
    public static final int LONG = 1;
    public static final int MEDIUM = 2;
    public static final int SHORT = 3;
    private static final FormatCache<FastDateFormat> cache = new FormatCache<FastDateFormat>() {
        @Override
        protected FastDateFormat createInstance(String pattern, TimeZone timeZone, Locale locale) {
            return new FastDateFormat(pattern, timeZone, locale);
        }
    };
    private final FastDatePrinter printer;


    public static FastDateFormat getInstance(String pattern, Locale locale) {
        return (FastDateFormat) cache.getInstance(pattern, (TimeZone) null, locale);
    }

    public static FastDateFormat getInstance(String pattern, TimeZone timeZone, Locale locale) {
        return (FastDateFormat) cache.getInstance(pattern, timeZone, locale);
    }


    protected FastDateFormat(String pattern, TimeZone timeZone, Locale locale) {
        this(pattern, timeZone, locale, (Date) null);
    }

    protected FastDateFormat(String pattern, TimeZone timeZone, Locale locale, Date centuryStart) {
        this.printer = new FastDatePrinter(pattern, timeZone, locale);
    }

    @Override
    public StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition pos) {
        return this.printer.format(obj, toAppendTo, pos);
    }

    @Override
    public Object parseObject(String source, @NotNull ParsePosition pos) {
        return null;
    }

    @Override
    public String format(long millis) {
        return this.printer.format(millis);
    }

    @Override
    public String format(Date date) {
        return this.printer.format(date);
    }

    @Override
    public String format(Calendar calendar) {
        return this.printer.format(calendar);
    }

    @Override
    public StringBuffer format(long millis, StringBuffer buf) {
        return this.printer.format(millis, buf);
    }

    @Override
    public StringBuffer format(Date date, StringBuffer buf) {
        return this.printer.format(date, buf);
    }

    @Override
    public StringBuffer format(Calendar calendar, StringBuffer buf) {
        return this.printer.format(calendar, buf);
    }

    @Override
    public String getPattern() {
        return this.printer.getPattern();
    }

    @Override
    public TimeZone getTimeZone() {
        return this.printer.getTimeZone();
    }

    @Override
    public Locale getLocale() {
        return this.printer.getLocale();
    }

    public int getMaxLengthEstimate() {
        return this.printer.getMaxLengthEstimate();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof FastDateFormat)) {
            return false;
        } else {
            FastDateFormat other = (FastDateFormat) obj;
            return this.printer.equals(other.printer);
        }
    }

    @Override
    public int hashCode() {
        return this.printer.hashCode();
    }

    @Override
    public String toString() {
        return "FastDateFormat[" + this.printer.getPattern() + "," + this.printer.getLocale() + "," + this.printer.getTimeZone().getID() + "]";
    }

    protected StringBuffer applyRules(Calendar calendar, StringBuffer buf) {
        return this.printer.applyRules(calendar, buf);
    }
}
