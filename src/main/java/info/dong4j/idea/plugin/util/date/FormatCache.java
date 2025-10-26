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
 * 格式缓存类，用于缓存和创建 {@link Format} 实例。
 * <p>
 * 该类提供了一种机制，用于根据指定的模式、时区和语言环境创建日期时间格式化器，并通过缓存机制提高性能。
 * 支持通过样式（如 SHORT、MEDIUM、LONG、FULL）获取默认格式化器，也支持自定义模式创建格式化器。
 * <p>
 * 该类使用 {@link ConcurrentMap} 实现缓存，确保线程安全。
 *
 * @author 原作者
 * @version 1.0.0
 * @date 2025.10.24
 * @since 3.0
 */
abstract class FormatCache<F extends Format> {
    /** 无日期或无时间，用于与 DateFormat.SHORT 或 DateFormat.LONG 相同的参数 */
    static final int NONE = -1;

    /** 用于缓存多部分请求的实例，键为 MultipartKey，值为 F 类型 */
    private final ConcurrentMap<MultipartKey, F> cInstanceCache
        = new ConcurrentHashMap<MultipartKey, F>(7);

    /** 多部分键对应的日期时间实例缓存，用于提高性能 */
    private static final ConcurrentMap<MultipartKey, String> cDateTimeInstanceCache
        = new ConcurrentHashMap<MultipartKey, String>(7);

    /**
     * 获取使用默认模式、时区和语言环境的格式化器实例。
     * <p>
     * 通过默认的日期时间格式、时区和语言环境创建并返回一个格式化器实例。
     *
     * @return 日期/时间格式化器
     */
    public F getInstance() {
        return this.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, TimeZone.getDefault(), Locale.getDefault());
    }

    /**
     * 根据指定的格式模式、时区和语言环境获取一个格式化实例。
     * <p>
     * 该方法创建并返回一个基于指定格式模式的日期时间格式化器。如果指定的时区或语言环境为 null，则使用系统默认值。
     * 同时，该方法会尝试从缓存中获取已存在的实例，若不存在则创建新实例并缓存。
     *
     * @param pattern  格式模式，需与 java.text.SimpleDateFormat 兼容，不可为 null
     * @param timeZone 时区，若为 null 则使用默认时区
     * @param locale   语言环境，若为 null 则使用默认语言环境
     * @return 基于指定格式模式的日期时间格式化器实例
     * @throws IllegalArgumentException 如果格式模式无效或为 null 时抛出
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
     * 创建一个基于指定模式、时区和语言环境的格式实例。
     * <p>
     * 使用给定的模式、时区和语言环境生成一个格式对象。
     *
     * @param pattern  与 java.text.SimpleDateFormat 兼容的模式，此参数不会为 null。
     * @param timeZone 时区，此参数不会为 null。
     * @param locale   语言环境，此参数不会为 null。
     * @return 基于模式的日期/时间格式器
     * @throws IllegalArgumentException 如果模式无效或为 null。
     */
    abstract protected F createInstance(String pattern, TimeZone timeZone, Locale locale);
    // This must remain private, see LANG-884

    /**
     * 根据指定的日期样式、时间样式、时区和语言环境获取一个本地化的日期时间格式化器实例。
     * <p>
     * 该方法通过传入的日期样式和时间样式参数，结合指定的时区和语言环境，生成对应的日期时间格式化器。
     * 如果传入的时区或语言环境为 null，则使用系统默认值。
     *
     * @param dateStyle 日期样式：FULL, LONG, MEDIUM, 或 SHORT，若为 null 表示格式中不包含日期部分
     * @param timeStyle 时间样式：FULL, LONG, MEDIUM, 或 SHORT，若为 null 表示格式中不包含时间部分
     * @param timeZone  可选的时区，覆盖格式化日期的时区，若为 null 则使用默认语言环境的时区
     * @param locale    可选的语言环境，覆盖系统语言环境，若为 null 则使用系统默认语言环境
     * @return 一个本地化的标准日期时间格式化器实例
     * @throws IllegalArgumentException 如果指定的语言环境没有定义日期时间格式模式
     */
    private F getDateTimeInstance(Integer dateStyle, Integer timeStyle, TimeZone timeZone, Locale locale) {
        if (locale == null) {
            locale = Locale.getDefault();
        }
        String pattern = getPatternForStyle(dateStyle, timeStyle, locale);
        return this.getInstance(pattern, timeZone, locale);
    }
    // package protected, for access from FastDateFormat; do not make public or protected

    /**
     * 根据指定的日期格式、时间格式、时区和语言环境获取一个日期时间格式化器实例。
     * <p>
     * 该方法用于创建一个本地化的标准日期时间格式化器，支持自定义日期和时间的显示格式。
     *
     * @param dateStyle 日期格式：FULL, LONG, MEDIUM, 或 SHORT
     * @param timeStyle 时间格式：FULL, LONG, MEDIUM, 或 SHORT
     * @param timeZone  可选的时区，覆盖格式化日期的时区，若为 null 则使用默认语言环境
     * @param locale    可选的语言环境，覆盖系统语言环境
     * @return 一个本地化的标准日期时间格式化器
     * @throws IllegalArgumentException 如果语言环境没有定义日期时间格式则抛出
     */
    F getDateTimeInstance(int dateStyle, int timeStyle, TimeZone timeZone, Locale locale) {
        return this.getDateTimeInstance(Integer.valueOf(dateStyle), Integer.valueOf(timeStyle), timeZone, locale);
    }
    // package protected, for access from FastDateFormat; do not make public or protected

    /**
     * 根据指定的日期格式、时区和语言环境获取一个日期格式化实例。
     * <p>
     * 该方法通过指定的日期样式、时区和语言环境创建一个本地化的日期时间格式化器。
     *
     * @param dateStyle 日期样式，可选值为 FULL、LONG、MEDIUM 或 SHORT
     * @param timeZone  可选的时区，若不为 null 则覆盖格式化日期的时区，null 表示使用默认语言环境
     * @param locale    可选的语言环境，若不为 null 则覆盖系统语言环境
     * @return 一个本地化的标准日期时间格式化器
     * @throws IllegalArgumentException 如果指定的语言环境没有定义日期时间格式模式
     */
    F getDateInstance(int dateStyle, TimeZone timeZone, Locale locale) {
        return this.getDateTimeInstance(Integer.valueOf(dateStyle), null, timeZone, locale);
    }
    // package protected, for access from FastDateFormat; do not make public or protected

    /**
     * 获取一个时间格式化器实例，使用指定的样式、时区和语言环境。
     * <p>
     * 根据给定的时间样式、时区和语言环境创建一个本地化的标准时间格式化器。
     *
     * @param timeStyle 时间样式：FULL, LONG, MEDIUM, 或 SHORT
     * @param timeZone  可选的时区，覆盖格式化日期的时区，null 表示使用默认语言环境
     * @param locale    可选的语言环境，覆盖系统语言环境
     * @return 本地化的标准时间格式化器
     * @throws IllegalArgumentException 如果语言环境没有定义日期/时间格式
     */
    F getTimeInstance(int timeStyle, TimeZone timeZone, Locale locale) {
        return this.getDateTimeInstance(null, timeStyle, timeZone, locale);
    }
    // package protected, for access from test code; do not make public or protected

    /**
     * 根据指定的日期和时间格式样式以及区域设置，获取对应的格式模式字符串。
     * <p>
     * 该方法用于生成符合指定样式和区域设置的日期时间格式模式。如果日期样式或时间样式为 null，则表示不包含对应的日期或时间部分。方法会使用缓存机制提高性能，避免重复创建格式对象。
     *
     * @param dateStyle 日期样式：FULL, LONG, MEDIUM, 或 SHORT，若为 null 表示格式中不包含日期部分
     * @param timeStyle 时间样式：FULL, LONG, MEDIUM, 或 SHORT，若为 null 表示格式中不包含时间部分
     * @param locale    指定的区域设置对象，用于本地化格式
     * @return 对应的本地化日期时间格式模式字符串
     * @throws IllegalArgumentException 如果指定的区域设置没有定义日期时间格式模式
     */
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
     * 多部分键辅助类
     * <p>
     * 用于存储多部分 Map 键，支持多个对象作为键的组合。该类重写了 equals 和 hashCode 方法，以确保基于键数组的比较和哈希计算正确。
     * </p>
     *
     * @author 作者姓名
     * @version 1.0.0
     * @date 2025.10.24
     * @since 1.0.0
     */
    private static class MultipartKey {
        /** keys 用于存储需要处理的键对象数组 */
        private final Object[] keys;
        /** 用于缓存对象的哈希值，以提高 equals 和 hashCode 方法的性能 */
        private int hashCode;

        /**
         * 构造一个 MultipartKey 实例，用于保存指定的对象集合。
         * <p>
         * 每个对象可以为 null，该构造方法用于创建包含多个键的复合键对象。
         *
         * @param keys 要保存的对象集合，每个元素可以为 null
         */
        public MultipartKey(Object... keys) {
            this.keys = keys;
        }

        /**
         * 重写 equals 方法，用于比较两个 MultipartKey 对象是否相等
         * <p>
         * 由于该内部静态类仅在泛型 ConcurrentHashMap 中使用，不会与其它类型对象进行比较，因此省略常规的 equals 方法实现
         *
         * @param obj 要比较的对象
         * @return 如果两个对象的 keys 数组相等，则返回 true；否则返回 false
         */
        @Override
        public boolean equals(Object obj) {
            // Eliminate the usual boilerplate because
            // this inner static class is only used in a generic ConcurrentHashMap
            // which will not compare against other Object types
            return Arrays.equals(this.keys, ((MultipartKey) obj).keys);
        }

        /**
         * 重写 hashCode 方法，根据 keys 集合中的元素计算当前对象的哈希值
         * <p>
         * 如果当前对象的哈希值尚未计算，则遍历 keys 集合，将每个非空元素的哈希值
         * 通过特定算法累加，最终缓存并返回该哈希值
         *
         * @return 当前对象的哈希值
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
