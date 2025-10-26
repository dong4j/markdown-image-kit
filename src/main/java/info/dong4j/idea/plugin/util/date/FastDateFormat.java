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

/**
 * 快速日期格式化类
 * <p>
 * 该类用于格式化日期和时间，提供高效的日期格式化功能，支持自定义格式模式、时区和语言环境。
 * 通过缓存机制优化性能，适用于需要频繁格式化日期的场景。
 *
 * @author 原作者信息
 * @version 1.0.0
 * @date 2025.10.24
 * @since 1.0.0
 */
public class FastDateFormat extends Format implements DatePrinter {
    /** 序列化版本号，用于确保类的兼容性 */
    private static final long serialVersionUID = 2L;
    /** 表示完整状态的常量，值为 0 */
    public static final int FULL = 0;
    /** 长整型常量，表示某种特定的长度类型标识 */
    public static final int LONG = 1;
    /** 中等优先级常量，值为 2 */
    public static final int MEDIUM = 2;
    /** 短类型常量，表示短整型数值 */
    public static final int SHORT = 3;
    /** 用于缓存 FastDateFormat 实例，提高日期格式化性能 */
    private static final FormatCache<FastDateFormat> cache = new FormatCache<FastDateFormat>() {
        /**
         * 创建 FastDateFormat 实例
         * <p>
         * 根据指定的日期格式模式、时区和语言环境创建 FastDateFormat 对象
         *
         * @param pattern  日期格式模式，如 "yyyy-MM-dd"
         * @param timeZone 时区，如 TimeZone.getTimeZone("GMT+8")
         * @param locale   语言环境，如 Locale.CHINA
         * @return 创建的 FastDateFormat 实例
         */
        @Override
        protected FastDateFormat createInstance(String pattern, TimeZone timeZone, Locale locale) {
            return new FastDateFormat(pattern, timeZone, locale);
        }
    };
    /** 时间格式化打印机，用于格式化日期时间输出 */
    private final FastDatePrinter printer;


    /**
     * 获取指定模式和区域设置的日期格式化对象
     * <p>
     * 该方法用于根据给定的日期格式模式和区域设置，获取一个日期格式化对象。
     * 通过缓存机制提高性能，避免重复创建相同格式的实例。
     *
     * @param pattern 日期格式模式，如 "yyyy-MM-dd"
     * @param locale  区域设置，用于指定日期格式的语言和文化习惯
     * @return 返回一个FastDateFormat实例，用于格式化日期
     */
    public static FastDateFormat getInstance(String pattern, Locale locale) {
        return cache.getInstance(pattern, null, locale);
    }

    /**
     * 获取指定模式、时区和语言环境的日期格式化对象
     * <p>
     * 通过缓存机制获取已存在的日期格式化实例，避免重复创建对象，提高性能。
     *
     * @param pattern  日期格式模式，如 "yyyy-MM-dd"
     * @param timeZone 时区信息
     * @param locale   语言环境
     * @return 指定格式的日期格式化对象
     */
    public static FastDateFormat getInstance(String pattern, TimeZone timeZone, Locale locale) {
        return cache.getInstance(pattern, timeZone, locale);
    }


    /**
     * 使用指定的模式、时区和语言环境创建 FastDateFormat 实例
     * <p>
     * 该构造方法用于初始化 FastDateFormat 对象，指定日期时间格式的模式、时区以及语言环境。
     *
     * @param pattern  日期时间格式的模式字符串
     * @param timeZone 时区信息
     * @param locale   语言环境
     */
    protected FastDateFormat(String pattern, TimeZone timeZone, Locale locale) {
        this(pattern, timeZone, locale, null);
    }

    /**
     * 构造一个 FastDateFormat 对象
     * <p>
     * 使用指定的日期格式模式、时区、语言环境和世纪开始日期来初始化格式化对象。
     *
     * @param pattern      日期格式模式，如 "yyyy-MM-dd"
     * @param timeZone     时区信息，用于日期格式化时的时区转换
     * @param locale       语言环境，用于日期格式化时的本地化设置
     * @param centuryStart 世纪开始日期，用于确定世纪的起始点
     */
    protected FastDateFormat(String pattern, TimeZone timeZone, Locale locale, Date centuryStart) {
        this.printer = new FastDatePrinter(pattern, timeZone, locale);
    }

    /**
     * 格式化对象为字符串，使用指定的格式化器进行处理
     * <p>
     * 调用内部的printer对象的format方法，将传入的对象格式化到指定的StringBuffer中
     *
     * @param obj        要格式化的对象
     * @param toAppendTo 用于存储格式化结果的StringBuffer
     * @param pos        用于标识格式化位置的FieldPosition对象
     * @return 格式化后的StringBuffer对象
     */
    @Override
    public StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition pos) {
        return this.printer.format(obj, toAppendTo, pos);
    }

    /**
     * 解析给定的字符串源并返回解析后的对象
     * <p>
     * 该方法用于解析输入的字符串数据，并根据指定的解析位置返回解析结果。
     *
     * @param source 需要解析的字符串源
     * @param pos    解析位置对象，用于记录解析过程中的状态
     * @return 解析后的对象，若解析失败则返回 null
     */
    @Override
    public Object parseObject(String source, @NotNull ParsePosition pos) {
        return null;
    }

    /**
     * 格式化给定的时间戳为字符串
     * <p>
     * 将毫秒数转换为可读的时间格式，并通过打印机输出结果
     *
     * @param millis 要格式化的时间戳（毫秒）
     * @return 格式化后的时间字符串
     */
    @Override
    public String format(long millis) {
        return this.printer.format(millis);
    }

    /**
     * 格式化日期对象为字符串
     * <p>
     * 调用内部打印机对象的格式化方法，将传入的日期对象转换为对应的字符串表示形式
     *
     * @param date 要格式化的日期对象
     * @return 格式化后的字符串
     */
    @Override
    public String format(Date date) {
        return this.printer.format(date);
    }

    /**
     * 根据日历对象格式化日期时间字符串
     * <p>
     * 调用打印器的格式化方法，将日历对象转换为对应的日期时间字符串
     *
     * @param calendar 日历对象，用于获取日期时间信息
     * @return 格式化后的日期时间字符串
     */
    @Override
    public String format(Calendar calendar) {
        return this.printer.format(calendar);
    }

    /**
     * 格式化给定的时间毫秒值为字符串
     * <p>
     * 将时间毫秒值转换为特定格式的字符串，并写入指定的StringBuffer对象中。
     *
     * @param millis 时间毫秒值
     * @param buf    用于存储格式化结果的StringBuffer对象
     * @return 格式化后的StringBuffer对象
     */
    @Override
    public StringBuffer format(long millis, StringBuffer buf) {
        return this.printer.format(millis, buf);
    }

    /**
     * 格式化日期对象为字符串，并写入指定的 StringBuffer 中
     * <p>
     * 调用内部的 printer 对象的 format 方法，将日期对象格式化后写入给定的 StringBuffer
     *
     * @param date 要格式化的日期对象
     * @param buf  用于存储格式化结果的 StringBuffer
     * @return 格式化后的 StringBuffer
     */
    @Override
    public StringBuffer format(Date date, StringBuffer buf) {
        return this.printer.format(date, buf);
    }

    /**
     * 格式化日历对象为字符串缓冲区
     * <p>
     * 将传入的日历对象按照指定格式转换为字符串，并存储到给定的字符串缓冲区中。
     *
     * @param calendar 要格式化的日历对象
     * @param buf      存储格式化结果的字符串缓冲区
     * @return 格式化后的字符串缓冲区
     */
    @Override
    public StringBuffer format(Calendar calendar, StringBuffer buf) {
        return this.printer.format(calendar, buf);
    }

    /**
     * 获取当前打印模式的字符串表示
     * <p>
     * 调用打印机对象的getPattern方法，返回当前设置的打印模式。
     *
     * @return 当前打印模式的字符串表示
     */
    @Override
    public String getPattern() {
        return this.printer.getPattern();
    }

    /**
     * 获取当前打印机的时区信息
     * <p>
     * 调用打印机对象的 getTimeZone 方法，返回其配置的时区信息
     *
     * @return 打印机的时区信息
     */
    @Override
    public TimeZone getTimeZone() {
        return this.printer.getTimeZone();
    }

    /**
     * 获取当前打印器的Locale信息
     * <p>
     * 该方法用于返回与当前打印器关联的Locale对象，通常用于确定语言和区域设置。
     *
     * @return 当前打印器的Locale对象
     */
    @Override
    public Locale getLocale() {
        return this.printer.getLocale();
    }

    /**
     * 获取最大长度估计值
     * <p>
     * 调用打印机对象获取最大长度估计值并返回
     *
     * @return 最大长度估计值
     */
    public int getMaxLengthEstimate() {
        return this.printer.getMaxLengthEstimate();
    }

    /**
     * 重写 equals 方法，用于比较两个 FastDateFormat 对象是否相等
     * <p>
     * 该方法首先检查传入对象是否为 FastDateFormat 类型，如果不是则直接返回 false。
     * 如果是，则比较两个对象的 printer 字段是否相等。
     *
     * @param obj 要比较的对象
     * @return 如果对象相等则返回 true，否则返回 false
     */
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof FastDateFormat other)) {
            return false;
        } else {
            return this.printer.equals(other.printer);
        }
    }

    /**
     * 重写 hashCode 方法，用于根据 Printer 对象生成对象的哈希值
     * <p>
     * 该方法通过调用 Printer 类的 hashCode 方法来返回当前对象的哈希值
     *
     * @return 当前对象的哈希值
     */
    @Override
    public int hashCode() {
        return this.printer.hashCode();
    }

    /**
     * 返回对象的字符串表示形式
     * <p>
     * 该方法重写 Object 类的 toString 方法，返回包含日期格式模式、语言环境和时区信息的字符串
     *
     * @return 对象的字符串表示
     */
    @Override
    public String toString() {
        return "FastDateFormat[" + this.printer.getPattern() + "," + this.printer.getLocale() + "," + this.printer.getTimeZone().getID() + "]";
    }

    /**
     * 应用规则到日历对象并更新缓冲区内容
     * <p>
     * 该方法将根据给定的日历对象和缓冲区，应用相应的规则进行格式化或处理，并返回更新后的缓冲区。
     *
     * @param calendar 日历对象，用于获取日期和时间信息
     * @param buf      缓冲区对象，用于存储和更新格式化后的内容
     * @return 格式化或处理后的缓冲区
     */
    protected StringBuffer applyRules(Calendar calendar, StringBuffer buf) {
        return this.printer.applyRules(calendar, buf);
    }
}
