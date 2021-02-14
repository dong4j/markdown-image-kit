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

package info.dong4j.idea.plugin.util.date;

import java.text.DateFormat;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * <p>FormatCache is a cache and factory for {@link Format}s.</p>
 *
 * @version $Id: FormatCache 892161 2009-12-18 07:21:10Z  $
 * @since 3.0
 */
abstract class FormatCache<F extends Format> {
    /**
     * No date or no time.  Used in same parameters as DateFormat.SHORT or DateFormat.LONG
     */
    static final int NONE = -1;

    private final ConcurrentMap<MultipartKey, F> cInstanceCache
        = new ConcurrentHashMap<MultipartKey, F>(7);

    private static final ConcurrentMap<MultipartKey, String> cDateTimeInstanceCache
        = new ConcurrentHashMap<MultipartKey, String>(7);

    /**
     * <p>Gets a formatter instance using the default pattern in the
     * default timezone and locale.</p>
     *
     * @return a date/time formatter
     */
    public F getInstance() {
        return this.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, TimeZone.getDefault(), Locale.getDefault());
    }

    /**
     * <p>Gets a formatter instance using the specified pattern, time zone
     * and locale.</p>
     *
     * @param pattern  {@link java.text.SimpleDateFormat} compatible
     *                 pattern, non-null
     * @param timeZone the time zone, null means use the default TimeZone
     * @param locale   the locale, null means use the default Locale
     * @return a pattern based date/time formatter
     * @throws IllegalArgumentException if pattern is invalid
     *                                  or <code>null</code>
     */
    public F getInstance(String pattern, TimeZone timeZone, Locale locale) {
        if (pattern == null) {
            throw new NullPointerException("pattern must not be null");
        }
        if (timeZone == null) {
            timeZone = TimeZone.getDefault();
        }
        if (locale == null) {
            locale = Locale.getDefault();
        }
        MultipartKey key = new MultipartKey(pattern, timeZone, locale);
        F format = this.cInstanceCache.get(key);
        if (format == null) {
            format = this.createInstance(pattern, timeZone, locale);
            F previousValue = this.cInstanceCache.putIfAbsent(key, format);
            if (previousValue != null) {
                // another thread snuck in and did the same work
                // we should return the instance that is in ConcurrentMap
                format = previousValue;
            }
        }
        return format;
    }

    /**
     * <p>Create a format instance using the specified pattern, time zone
     * and locale.</p>
     *
     * @param pattern  {@link java.text.SimpleDateFormat} compatible pattern, this will not be null.
     * @param timeZone time zone, this will not be null.
     * @param locale   locale, this will not be null.
     * @return a pattern based date/time formatter
     * @throws IllegalArgumentException if pattern is invalid
     *                                  or <code>null</code>
     */
    abstract protected F createInstance(String pattern, TimeZone timeZone, Locale locale);

    /**
     * <p>Gets a date/time formatter instance using the specified style,
     * time zone and locale.</p>
     *
     * @param dateStyle date style: FULL, LONG, MEDIUM, or SHORT, null indicates no date in format
     * @param timeStyle time style: FULL, LONG, MEDIUM, or SHORT, null indicates no time in format
     * @param timeZone  optional time zone, overrides time zone of
     *                  formatted date, null means use default Locale
     * @param locale    optional locale, overrides system locale
     * @return a localized standard date/time formatter
     * @throws IllegalArgumentException if the Locale has no date/time
     *                                  pattern defined
     */
    // This must remain private, see LANG-884
    private F getDateTimeInstance(Integer dateStyle, Integer timeStyle, TimeZone timeZone, Locale locale) {
        if (locale == null) {
            locale = Locale.getDefault();
        }
        String pattern = getPatternForStyle(dateStyle, timeStyle, locale);
        return this.getInstance(pattern, timeZone, locale);
    }

    /**
     * <p>Gets a date/time formatter instance using the specified style,
     * time zone and locale.</p>
     *
     * @param dateStyle date style: FULL, LONG, MEDIUM, or SHORT
     * @param timeStyle time style: FULL, LONG, MEDIUM, or SHORT
     * @param timeZone  optional time zone, overrides time zone of
     *                  formatted date, null means use default Locale
     * @param locale    optional locale, overrides system locale
     * @return a localized standard date/time formatter
     * @throws IllegalArgumentException if the Locale has no date/time
     *                                  pattern defined
     */
    // package protected, for access from FastDateFormat; do not make public or protected
    F getDateTimeInstance(int dateStyle, int timeStyle, TimeZone timeZone, Locale locale) {
        return this.getDateTimeInstance(Integer.valueOf(dateStyle), Integer.valueOf(timeStyle), timeZone, locale);
    }

    /**
     * <p>Gets a date formatter instance using the specified style,
     * time zone and locale.</p>
     *
     * @param dateStyle date style: FULL, LONG, MEDIUM, or SHORT
     * @param timeZone  optional time zone, overrides time zone of
     *                  formatted date, null means use default Locale
     * @param locale    optional locale, overrides system locale
     * @return a localized standard date/time formatter
     * @throws IllegalArgumentException if the Locale has no date/time
     *                                  pattern defined
     */
    // package protected, for access from FastDateFormat; do not make public or protected
    F getDateInstance(int dateStyle, TimeZone timeZone, Locale locale) {
        return this.getDateTimeInstance(Integer.valueOf(dateStyle), null, timeZone, locale);
    }

    /**
     * <p>Gets a time formatter instance using the specified style,
     * time zone and locale.</p>
     *
     * @param timeStyle time style: FULL, LONG, MEDIUM, or SHORT
     * @param timeZone  optional time zone, overrides time zone of
     *                  formatted date, null means use default Locale
     * @param locale    optional locale, overrides system locale
     * @return a localized standard date/time formatter
     * @throws IllegalArgumentException if the Locale has no date/time
     *                                  pattern defined
     */
    // package protected, for access from FastDateFormat; do not make public or protected
    F getTimeInstance(int timeStyle, TimeZone timeZone, Locale locale) {
        return this.getDateTimeInstance(null, Integer.valueOf(timeStyle), timeZone, locale);
    }

    /**
     * <p>Gets a date/time format for the specified styles and locale.</p>
     *
     * @param dateStyle date style: FULL, LONG, MEDIUM, or SHORT, null indicates no date in format
     * @param timeStyle time style: FULL, LONG, MEDIUM, or SHORT, null indicates no time in format
     * @param locale    The non-null locale of the desired format
     * @return a localized standard date/time format
     * @throws IllegalArgumentException if the Locale has no date/time pattern defined
     */
    // package protected, for access from test code; do not make public or protected
    static String getPatternForStyle(Integer dateStyle, Integer timeStyle, Locale locale) {
        MultipartKey key = new MultipartKey(dateStyle, timeStyle, locale);

        String pattern = cDateTimeInstanceCache.get(key);
        if (pattern == null) {
            try {
                DateFormat formatter;
                if (dateStyle == null) {
                    formatter = DateFormat.getTimeInstance(timeStyle.intValue(), locale);
                } else if (timeStyle == null) {
                    formatter = DateFormat.getDateInstance(dateStyle.intValue(), locale);
                } else {
                    formatter = DateFormat.getDateTimeInstance(dateStyle.intValue(), timeStyle.intValue(), locale);
                }
                pattern = ((SimpleDateFormat) formatter).toPattern();
                String previous = cDateTimeInstanceCache.putIfAbsent(key, pattern);
                if (previous != null) {
                    // even though it doesn't matter if another thread put the pattern
                    // it's still good practice to return the String instance that is
                    // actually in the ConcurrentMap
                    pattern = previous;
                }
            } catch (ClassCastException ex) {
                throw new IllegalArgumentException("No date time pattern for locale: " + locale);
            }
        }
        return pattern;
    }

    // ----------------------------------------------------------------------

    /**
     * <p>Helper class to hold multi-part Map keys</p>
     */
    private static class MultipartKey {
        private final Object[] keys;
        private int hashCode;

        /**
         * Constructs an instance of <code>MultipartKey</code> to hold the specified objects.
         *
         * @param keys the set of objects that make up the key.  Each key may be null.
         */
        public MultipartKey(Object... keys) {
            this.keys = keys;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean equals(Object obj) {
            // Eliminate the usual boilerplate because
            // this inner static class is only used in a generic ConcurrentHashMap
            // which will not compare against other Object types
            return Arrays.equals(this.keys, ((MultipartKey) obj).keys);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int hashCode() {
            if (this.hashCode == 0) {
                int rc = 0;
                for (Object key : this.keys) {
                    if (key != null) {
                        rc = rc * 7 + key.hashCode();
                    }
                }
                this.hashCode = rc;
            }
            return this.hashCode;
        }
    }

}
