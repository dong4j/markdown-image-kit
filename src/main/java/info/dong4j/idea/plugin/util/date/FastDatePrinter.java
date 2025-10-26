package info.dong4j.idea.plugin.util.date;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.text.DateFormat;
import java.text.DateFormatSymbols;
import java.text.FieldPosition;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 日期格式化打印器
 * <p>
 * FastDatePrinter 是一个用于格式化日期和时间的工具类，支持自定义格式模式，能够根据不同的格式要求将日期对象转换为字符串。
 * 它实现了 DatePrinter 接口，并且支持序列化，适用于需要将日期按照特定格式输出的场景。
 * 该类通过解析格式模式字符串，生成对应的格式规则，并在格式化过程中根据规则将日期信息写入 StringBuffer。
 * 支持多种日期时间字段的格式化，包括年份、月份、日期、小时、分钟、秒、毫秒、时区等。
 * 同时，该类还处理了文本和数字格式的特殊规则，如对 AM/PM 的处理、对时区的显示格式等。
 *
 * @author 原作者信息未提供
 * @version 1.0.0
 * @date 2025.10.24
 * @since 1.0.0
 */
@SuppressWarnings("D")
public class FastDatePrinter implements DatePrinter, Serializable {
    // A lot of the speed in this class comes from caching, but some comes
    // from the special int to StringBuffer conversion.
    //
    // The following produces a padded 2 digit number:
    //   buffer.append((char)(value / 10 + '0'));
    //   buffer.append((char)(value % 10 + '0'));
    //
    // Note that the fastest append to StringBuffer is a single char (used here).
    // Note that Integer.toString() is not called, the conversion is simply
    // taking the value and adding (mathematically) the ASCII value for '0'.
    // So, don't change this code! It works and is very fast.

    /** 序列化版本号，用于确保类的兼容性 */
    private static final long serialVersionUID = 1L;

    /** 表示完整的日期时间格式，包含日期、时间及时区信息 */
    public static final int FULL = DateFormat.FULL;
    /** 日期格式的长格式常量，用于表示完整的日期和时间信息 */
    public static final int LONG = DateFormat.LONG;
    /** 中等格式，对应 DateFormat 中的 MEDIUM 常量 */
    public static final int MEDIUM = DateFormat.MEDIUM;
    /** 短格式日期时间模式，对应 DateFormat.SHORT */
    public static final int SHORT = DateFormat.SHORT;

    /** 正则表达式模式 */
    private final String mPattern;
    /** 时区配置，用于确定时间显示和处理的时区 */
    private final TimeZone mTimeZone;
    /** 当前Locale配置，用于国际化显示和本地化处理 */
    private final Locale mLocale;
    /** 规则数组，用于存储当前配置的规则信息 */
    private transient Rule[] mRules;
    /** 用于估计最大长度的临时字段，不参与序列化 */
    private transient int mMaxLengthEstimate;

    // Constructor
    //-----------------------------------------------------------------------

    /**
     * 构造函数，用于初始化 FastDatePrinter 实例
     * <p>
     * 根据指定的日期格式模式、时区和语言环境进行初始化
     *
     * @param pattern  日期格式模式
     * @param timeZone 时区
     * @param locale   语言环境
     */
    protected FastDatePrinter(String pattern, TimeZone timeZone, Locale locale) {
        this.mPattern = pattern;
        this.mTimeZone = timeZone;
        this.mLocale = locale;

        this.init();
    }

    /**
     * 初始化方法，用于加载规则并计算最大长度估计值
     * <p>
     * 该方法首先调用 parsePattern 方法获取规则列表，然后将规则列表转换为数组存储在 mRules 中。
     * 接着遍历 mRules 数组，累加每个规则的长度估计值，最终将总和赋值给 mMaxLengthEstimate。
     *
     * @author 作者名
     * @since 指定版本
     */
    private void init() {
        List<Rule> rulesList = this.parsePattern();
        this.mRules = rulesList.toArray(new Rule[rulesList.size()]);

        int len = 0;
        for (int i = this.mRules.length; --i >= 0; ) {
            len += this.mRules[i].estimateLength();
        }

        this.mMaxLengthEstimate = len;
    }

    // Parse the pattern
    //-----------------------------------------------------------------------

    /**
     * 解析日期时间格式模式并生成对应的规则列表
     * <p>
     * 该方法根据指定的日期时间格式模式，解析其中的各个格式符号，并为每个符号创建对应的规则对象，最终返回规则列表。
     * 支持多种日期时间字段的解析，包括年份、月份、日期、时间等，并根据不同的格式符号生成相应的规则。
     *
     * @param 无 参数由方法内部使用成员变量获取
     * @return 包含解析后的日期时间格式规则的列表
     */
    protected List<Rule> parsePattern() {
        DateFormatSymbols symbols = new DateFormatSymbols(this.mLocale);
        List<Rule> rules = new ArrayList<Rule>();

        String[] ERAs = symbols.getEras();
        String[] months = symbols.getMonths();
        String[] shortMonths = symbols.getShortMonths();
        String[] weekdays = symbols.getWeekdays();
        String[] shortWeekdays = symbols.getShortWeekdays();
        String[] AmPmStrings = symbols.getAmPmStrings();

        int length = this.mPattern.length();
        int[] indexRef = new int[1];

        for (int i = 0; i < length; i++) {
            indexRef[0] = i;
            String token = this.parseToken(this.mPattern, indexRef);
            i = indexRef[0];

            int tokenLen = token.length();
            if (tokenLen == 0) {
                break;
            }

            Rule rule;
            char c = token.charAt(0);

            switch (c) {
                case 'G': // era designator (text)
                    rule = new TextField(Calendar.ERA, ERAs);
                    break;
                case 'y': // year (number)
                    if (tokenLen == 2) {
                        rule = TwoDigitYearField.INSTANCE;
                    } else {
                        rule = this.selectNumberRule(Calendar.YEAR, tokenLen < 4 ? 4 : tokenLen);
                    }
                    break;
                case 'M': // month in year (text and number)
                    if (tokenLen >= 4) {
                        rule = new TextField(Calendar.MONTH, months);
                    } else if (tokenLen == 3) {
                        rule = new TextField(Calendar.MONTH, shortMonths);
                    } else if (tokenLen == 2) {
                        rule = TwoDigitMonthField.INSTANCE;
                    } else {
                        rule = UnpaddedMonthField.INSTANCE;
                    }
                    break;
                case 'd': // day in month (number)
                    rule = this.selectNumberRule(Calendar.DAY_OF_MONTH, tokenLen);
                    break;
                case 'h': // hour in am/pm (number, 1..12)
                    rule = new TwelveHourField(this.selectNumberRule(Calendar.HOUR, tokenLen));
                    break;
                case 'H': // hour in day (number, 0..23)
                    rule = this.selectNumberRule(Calendar.HOUR_OF_DAY, tokenLen);
                    break;
                case 'm': // minute in hour (number)
                    rule = this.selectNumberRule(Calendar.MINUTE, tokenLen);
                    break;
                case 's': // second in minute (number)
                    rule = this.selectNumberRule(Calendar.SECOND, tokenLen);
                    break;
                case 'S': // millisecond (number)
                    rule = this.selectNumberRule(Calendar.MILLISECOND, tokenLen);
                    break;
                case 'E': // day in week (text)
                    rule = new TextField(Calendar.DAY_OF_WEEK, tokenLen < 4 ? shortWeekdays : weekdays);
                    break;
                case 'D': // day in year (number)
                    rule = this.selectNumberRule(Calendar.DAY_OF_YEAR, tokenLen);
                    break;
                case 'F': // day of week in month (number)
                    rule = this.selectNumberRule(Calendar.DAY_OF_WEEK_IN_MONTH, tokenLen);
                    break;
                case 'w': // week in year (number)
                    rule = this.selectNumberRule(Calendar.WEEK_OF_YEAR, tokenLen);
                    break;
                case 'W': // week in month (number)
                    rule = this.selectNumberRule(Calendar.WEEK_OF_MONTH, tokenLen);
                    break;
                case 'a': // am/pm marker (text)
                    rule = new TextField(Calendar.AM_PM, AmPmStrings);
                    break;
                case 'k': // hour in day (1..24)
                    rule = new TwentyFourHourField(this.selectNumberRule(Calendar.HOUR_OF_DAY, tokenLen));
                    break;
                case 'K': // hour in am/pm (0..11)
                    rule = this.selectNumberRule(Calendar.HOUR, tokenLen);
                    break;
                case 'X': // ISO 8601
                    rule = Iso8601_Rule.getRule(tokenLen);
                    break;
                case 'z': // time zone (text)
                    if (tokenLen >= 4) {
                        rule = new TimeZoneNameRule(this.mTimeZone, this.mLocale, TimeZone.LONG);
                    } else {
                        rule = new TimeZoneNameRule(this.mTimeZone, this.mLocale, TimeZone.SHORT);
                    }
                    break;
                case 'Z': // time zone (value)
                    if (tokenLen == 1) {
                        rule = TimeZoneNumberRule.INSTANCE_NO_COLON;
                    } else if (tokenLen == 2) {
                        rule = TimeZoneNumberRule.INSTANCE_ISO_8601;
                    } else {
                        rule = TimeZoneNumberRule.INSTANCE_COLON;
                    }
                    break;
                case '\'': // literal text
                    String sub = token.substring(1);
                    if (sub.length() == 1) {
                        rule = new CharacterLiteral(sub.charAt(0));
                    } else {
                        rule = new StringLiteral(sub);
                    }
                    break;
                default:
                    throw new IllegalArgumentException("Illegal pattern component: " + token);
            }

            rules.add(rule);
        }

        return rules;
    }

    /**
     * 解析模式字符串中的 token，根据字符类型决定是时间模式还是文本内容
     * <p>
     * 如果当前字符是字母，则将其视为时间模式的一部分，并连续追加相同字符。
     * 如果当前字符是单引号，则处理为文本内容，包括转义的单引号和切换引号状态。
     * 最终将解析后的 token 返回，并更新 indexRef 的值以反映当前解析位置。
     *
     * @param pattern  模式字符串，包含需要解析的 token
     * @param indexRef 用于记录当前解析位置的数组，其第一个元素为当前索引
     * @return 解析后的 token 字符串
     */
    protected String parseToken(String pattern, int[] indexRef) {
        StringBuilder buf = new StringBuilder();

        int i = indexRef[0];
        int length = pattern.length();

        char c = pattern.charAt(i);
        if (c >= 'A' && c <= 'Z' || c >= 'a' && c <= 'z') {
            // Scan a run of the same character, which indicates a time
            // pattern.
            buf.append(c);

            while (i + 1 < length) {
                char peek = pattern.charAt(i + 1);
                if (peek == c) {
                    buf.append(c);
                    i++;
                } else {
                    break;
                }
            }
        } else {
            // This will identify token as text.
            buf.append('\'');

            boolean inLiteral = false;

            for (; i < length; i++) {
                c = pattern.charAt(i);

                if (c == '\'') {
                    if (i + 1 < length && pattern.charAt(i + 1) == '\'') {
                        // '' is treated as escaped '
                        i++;
                        buf.append(c);
                    } else {
                        inLiteral = !inLiteral;
                    }
                } else if (!inLiteral &&
                           (c >= 'A' && c <= 'Z' || c >= 'a' && c <= 'z')) {
                    i--;
                    break;
                } else {
                    buf.append(c);
                }
            }
        }

        indexRef[0] = i;
        return buf.toString();
    }

    /**
     * 根据字段和填充方式选择对应的数字规则
     * <p>
     * 根据传入的字段和填充方式，返回相应的数字规则对象。
     * 填充方式为1时返回无填充数字字段，为2时返回两位数字字段，其他情况返回带填充数字字段。
     *
     * @param field   字段编号
     * @param padding 填充方式，1表示无填充，2表示两位填充，其他值表示带填充
     * @return 对应的数字规则对象
     */
    protected NumberRule selectNumberRule(int field, int padding) {
        switch (padding) {
            case 1:
                return new UnpaddedNumberField(field);
            case 2:
                return new TwoDigitNumberField(field);
            default:
                return new PaddedNumberField(field, padding);
        }
    }

    // Format methods
    //-----------------------------------------------------------------------

    /**
     * 格式化对象为字符串，支持 Date、Calendar 和 Long 类型
     * <p>
     * 根据传入的对象类型，调用相应的格式化方法进行处理。如果对象类型不支持，则抛出异常。
     *
     * @param obj        要格式化的对象
     * @param toAppendTo 用于接收格式化结果的 StringBuffer
     * @param pos        用于定位格式化位置的 FieldPosition 对象
     * @return 格式化后的 StringBuffer
     * @throws IllegalArgumentException 如果对象类型不支持
     */
    @Override
    public StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition pos) {
        if (obj instanceof Date) {
            return this.format((Date) obj, toAppendTo);
        } else if (obj instanceof Calendar) {
            return this.format((Calendar) obj, toAppendTo);
        } else if (obj instanceof Long) {
            return this.format(((Long) obj).longValue(), toAppendTo);
        } else {
            throw new IllegalArgumentException("Unknown class: " +
                                               (obj == null ? "<null>" : obj.getClass().getName()));
        }
    }

    /**
     * 根据毫秒数格式化日期字符串
     * <p>
     * 将给定的毫秒时间转换为 Calendar 对象，并应用格式化规则生成对应的日期字符串。
     *
     * @param millis 毫秒时间戳
     * @return 格式化后的日期字符串
     */
    @Override
    public String format(long millis) {
        Calendar c = this.newCalendar();  // hard code GregorianCalendar
        c.setTimeInMillis(millis);
        return this.applyRulesToString(c);
    }

    /**
     * 将日历对象转换为字符串表示，应用相关规则
     * <p>
     * 该方法使用指定的日历对象，应用规则生成字符串表示，并返回结果。
     *
     * @param c 日历对象，用于生成字符串
     * @return 转换后的字符串表示
     */
    private String applyRulesToString(Calendar c) {
        return this.applyRules(c, new StringBuffer(this.mMaxLengthEstimate)).toString();
    }

    /**
     * 创建并返回一个新的 GregorianCalendar 实例
     * <p>
     * 使用指定的时间区域和语言环境初始化一个新的 GregorianCalendar 对象
     *
     * @return 新创建的 GregorianCalendar 实例
     */
    private GregorianCalendar newCalendar() {
        // hard code GregorianCalendar
        return new GregorianCalendar(this.mTimeZone, this.mLocale);
    }

    /**
     * 格式化给定的日期对象为字符串
     * <p>
     * 将传入的日期对象设置到日历实例中，并应用格式化规则生成对应的字符串表示。
     *
     * @param date 要格式化的日期对象
     * @return 格式化后的日期字符串
     */
    @Override
    public String format(Date date) {
        Calendar c = this.newCalendar();  // hard code GregorianCalendar
        c.setTime(date);
        return this.applyRulesToString(c);
    }

    /**
     * 根据日历对象格式化日期字符串
     * <p>
     * 使用指定的日历对象进行日期格式化，并返回格式化后的字符串结果
     *
     * @param calendar 日历对象，用于获取日期时间信息
     * @return 格式化后的日期字符串
     */
    @Override
    public String format(Calendar calendar) {
        return this.format(calendar, new StringBuffer(this.mMaxLengthEstimate)).toString();
    }

    /**
     * 格式化指定时间戳为日期字符串并写入给定的 StringBuffer
     * <p>
     * 将传入的时间戳转换为 Date 对象，然后调用 format 方法将日期格式化为字符串，并写入到指定的 StringBuffer 中。
     *
     * @param millis 时间戳（毫秒）
     * @param buf    用于存储格式化后日期字符串的 StringBuffer
     * @return 格式化后日期字符串的 StringBuffer
     */
    @Override
    public StringBuffer format(long millis, StringBuffer buf) {
        return this.format(new Date(millis), buf);
    }

    /**
     * 根据给定的日期对象格式化日期，并将结果写入指定的字符串缓冲区
     * <p>
     * 该方法首先创建一个日历实例（默认使用GregorianCalendar），设置其时间，然后应用格式化规则将日期格式化到缓冲区中。
     *
     * @param date 日期对象，需要被格式化的日期
     * @param buf  用于存储格式化结果的字符串缓冲区
     * @return 格式化后的字符串缓冲区
     */
    @Override
    public StringBuffer format(Date date, StringBuffer buf) {
        Calendar c = this.newCalendar();  // hard code GregorianCalendar
        c.setTime(date);
        return this.applyRules(c, buf);
    }

    /**
     * 格式化日历对象为字符串缓冲区
     * <p>
     * 该方法将指定的日历对象按照一定的格式规则转换为字符串，并存储在提供的字符串缓冲区中。
     *
     * @param calendar 要格式化的日历对象
     * @param buf      存储格式化结果的字符串缓冲区
     * @return 格式化后的字符串缓冲区
     */
    @Override
    public StringBuffer format(Calendar calendar, StringBuffer buf) {
        return this.applyRules(calendar, buf);
    }

    /**
     * 应用规则到给定的日历对象，并将结果追加到缓冲区中
     * <p>
     * 遍历所有规则，调用每个规则的appendTo方法，将格式化后的内容追加到缓冲区中。
     *
     * @param calendar 日历对象，用于获取日期和时间信息
     * @param buf      缓冲区，用于存储格式化后的内容
     * @return 格式化后的内容
     */
    protected StringBuffer applyRules(Calendar calendar, StringBuffer buf) {
        for (Rule rule : this.mRules) {
            rule.appendTo(buf, calendar);
        }
        return buf;
    }

    /**
     * 获取当前模式字符串
     * <p>
     * 返回该实例中存储的模式字符串值。
     *
     * @return 当前模式字符串
     */
    @Override
    public String getPattern() {
        return this.mPattern;
    }

    /**
     * 获取当前时区信息
     * <p>
     * 返回该实例关联的时区对象
     *
     * @return 当前时区对象
     */
    @Override
    public TimeZone getTimeZone() {
        return this.mTimeZone;
    }

    /**
     * 获取当前的Locale信息
     * <p>
     * 返回当前配置的Locale对象，用于国际化和本地化处理。
     *
     * @return 当前Locale对象
     */
    @Override
    public Locale getLocale() {
        return this.mLocale;
    }

    /**
     * 获取最大长度估计值
     * <p>
     * 返回当前对象的最大长度估计值。
     *
     * @return 最大长度估计值
     */
    public int getMaxLengthEstimate() {
        return this.mMaxLengthEstimate;
    }

    // Basics
    //-----------------------------------------------------------------------

    /**
     * 判断当前对象是否与指定对象相等
     * <p>
     * 该方法用于比较两个 FastDatePrinter 实例是否具有相同的模式、时区和语言环境
     *
     * @param obj 要比较的对象
     * @return 如果对象相等则返回 true，否则返回 false
     */
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof FastDatePrinter other)) {
            return false;
        }
        return this.mPattern.equals(other.mPattern)
               && this.mTimeZone.equals(other.mTimeZone)
               && this.mLocale.equals(other.mLocale);
    }

    /**
     * 生成对象的哈希码
     * <p>
     * 根据模式、时区和语言环境计算并返回对象的哈希码，用于对象的哈希表操作。
     *
     * @return 对象的哈希码值
     */
    @Override
    public int hashCode() {
        return this.mPattern.hashCode() + 13 * (this.mTimeZone.hashCode() + 13 * this.mLocale.hashCode());
    }

    /**
     * 返回 FastDatePrinter 对象的字符串表示形式
     * <p>
     * 该方法用于生成一个包含格式模式、区域设置和时区标识的字符串，用于对象的调试或日志输出。
     *
     * @return 包含格式模式、区域设置和时区标识的字符串，格式为 "FastDatePrinter[模式,区域设置,时区ID]"
     */
    @Override
    public String toString() {
        return "FastDatePrinter[" + this.mPattern + "," + this.mLocale + "," + this.mTimeZone.getID() + "]";
    }

    // Serializing
    //-----------------------------------------------------------------------

    /**
     * 反序列化对象
     * <p>
     * 该方法在反序列化对象时被调用，用于恢复对象状态。首先调用默认的反序列化方法，然后执行初始化操作。
     *
     * @param in 对象输入流
     * @throws IOException            如果反序列化过程中发生输入输出错误
     * @throws ClassNotFoundException 如果反序列化过程中找不到类
     */
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        this.init();
    }

    /**
     * 将整数转换为两位数字字符并追加到字符串缓冲区中
     * <p>
     * 该方法将给定的整数值转换为两位数字字符，分别代表十位和个位，并追加到指定的 StringBuffer 对象中。
     * 如果值小于10，则个位数前面会补零。
     *
     * @param buffer 字符串缓冲区，用于追加转换后的两位数字字符
     * @param value  要转换的整数值
     */
    private static void appendDigits(StringBuffer buffer, int value) {
        buffer.append((char) (value / 10 + '0'));
        buffer.append((char) (value % 10 + '0'));
    }

    // Rules
    //-----------------------------------------------------------------------

    /**
     * 规则接口
     * <p>
     * 定义了规则的基本操作，用于估计长度和将规则内容追加到字符串缓冲区中。该接口可以被不同的规则实现类继承或实现，以支持多样化的规则逻辑。
     *
     * @author 作者名
     * @version 1.0.0
     * @date 2025.10.24
     * @since 1.0.0
     */
    private interface Rule {
        /**
         * 估算字符串长度
         * <p>
         * 返回一个估计的字符串长度值，具体实现逻辑可能基于某种算法或规则
         *
         * @return 估计的字符串长度
         */
        int estimateLength();

        /**
         * 将指定的日历对象格式化后追加到字符串缓冲区中
         * <p>
         * 该方法将日历对象转换为字符串形式，并追加到给定的字符串缓冲区中。
         *
         * @param buffer   要追加内容的字符串缓冲区
         * @param calendar 要格式化的日历对象
         */
        void appendTo(StringBuffer buffer, Calendar calendar);
    }

    /**
     * 数字规则接口
     * <p>
     * 定义处理数字类型规则的接口，继承自通用规则接口 Rule，用于将数字值追加到字符串缓冲区中
     *
     * @author dong4j
     * @version 1.0.0
     * @date 2025.10.24
     * @since 1.0.0
     */
    private interface NumberRule extends Rule {
        /**
         * 将指定整数值追加到字符串缓冲区中
         * <p>
         * 该方法将整数值转换为字符串，并追加到传入的 StringBuffer 对象中
         *
         * @param buffer 要追加内容的字符串缓冲区
         * @param value  要追加的整数值
         */
        void appendTo(StringBuffer buffer, int value);
    }

    /**
     * 字符字面量规则记录类
     * <p>
     * 用于表示一个字符字面量的规则实现，主要负责估计字符占用的长度以及将字符追加到字符串缓冲区中。
     * 该类实现了 {@link Rule} 接口，提供字符字面量的处理逻辑。
     *
     * @author 未知
     * @version 1.0.0
     * @date 2025.04.01
     * @since 1.0.0
     */
    private record CharacterLiteral(char mValue) implements Rule {

        /**
         * 估算当前对象的长度
         * <p>
         * 该方法用于估算当前对象的长度，返回值为固定值 1。
         *
         * @return 当前对象的估算长度
         */
        @Override
            public int estimateLength() {
                return 1;
            }

        /**
         * 将当前对象的值追加到指定的 StringBuffer 中
         * <p>
         * 该方法将当前对象的 mValue 字段内容追加到传入的 StringBuffer 对象中。
         *
         * @param buffer   要追加内容的目标 StringBuffer 对象
         * @param calendar 日期日历对象（此处未使用）
         */
        @Override
            public void appendTo(StringBuffer buffer, Calendar calendar) {
                buffer.append(this.mValue);
            }
        }

    /**
     * 字符串字面量记录类
     * <p>
     * 用于表示固定字符串值的规则实现，提供字符串长度估算和追加到缓冲区的功能。
     * <p>
     * 该类实现了 {@link Rule} 接口，主要用于处理字符串类型的字面量数据，适用于需要对字符串进行长度计算和拼接的场景。
     *
     * @author dong4j
     * @version 1.0.0
     * @date 2025.10.24
     * @since 1.0.0
     */
    private record StringLiteral(String mValue) implements Rule {

        /**
         * 估计当前字符串的长度
         * <p>
         * 返回当前字符串的长度，用于在需要预估长度时提供快速访问
         *
         * @return 当前字符串的长度
         */
        @Override
            public int estimateLength() {
                return this.mValue.length();
            }

        /**
         * 将当前对象的值追加到指定的 StringBuffer 中
         * <p>
         * 该方法将当前对象的 mValue 字段内容追加到传入的 StringBuffer 对象中。
         *
         * @param buffer   要追加内容的目标 StringBuffer 对象
         * @param calendar 日期日历对象（此处未使用）
         */
        @Override
            public void appendTo(StringBuffer buffer, Calendar calendar) {
                buffer.append(this.mValue);
            }
        }

    /**
     * 文本字段规则记录类
     * <p>
     * 用于表示文本字段的规则，支持根据指定字段从值数组中获取对应值，并估算字段长度。
     * 该类实现了 {@link Rule} 接口，提供字段长度估算和值拼接功能。
     *
     * @author 未知
     * @version 1.0.0
     * @date 2025.10.24
     * @since 1.0.0
     */
    private record TextField(int mField, String[] mValues) implements Rule {

        /**
         * 估算字符串数组中单个字符串的最大长度
         * <p>
         * 遍历数组中的每个字符串，计算其长度，并返回最大值
         *
         * @return 字符串数组中最大字符串的长度
         */
        @Override
            public int estimateLength() {
                int max = 0;
                for (int i = this.mValues.length; --i >= 0; ) {
                    int len = this.mValues[i].length();
                    if (len > max) {
                        max = len;
                    }
                }
                return max;
            }

        /**
         * 将当前对象的值按照指定日历信息追加到字符串缓冲区中
         * <p>
         * 根据传入的日历对象获取对应的字段值，并将其追加到缓冲区中
         *
         * @param buffer   要追加内容的字符串缓冲区
         * @param calendar 用于获取字段值的日历对象
         */
        @Override
            public void appendTo(StringBuffer buffer, Calendar calendar) {
                buffer.append(this.mValues[calendar.get(this.mField)]);
            }
        }

    /**
     * 无填充数字字段规则类
     * <p>
     * 用于定义不带前导零的数字字段格式化规则，支持将数字值按照指定字段位置追加到字符串缓冲区中。
     * <p>
     * 该类实现了 NumberRule 接口，主要功能是根据字段索引获取对应的数字值，并按照特定格式追加到目标字符串中。
     *
     * @author dong4j
     * @version 1.0.0
     * @date 2025.10.24
     * @since 1.0.0
     */
    private record UnpaddedNumberField(int mField) implements NumberRule {

        /**
         * 估算当前对象的长度
         * <p>
         * 返回一个固定值 4，表示当前对象的估算长度。
         *
         * @return 对象的估算长度
         */
        @Override
            public int estimateLength() {
                return 4;
            }

        /**
         * 将当前对象的值追加到指定的 StringBuffer 中，使用给定的 Calendar 对象进行格式化。
         * <p>
         * 该方法会根据 Calendar 的时间信息，将当前字段的值按照指定格式添加到缓冲区中。
         *
         * @param buffer   要追加内容的 StringBuffer 对象
         * @param calendar 用于时间格式化的 Calendar 对象
         */
        @Override
            public void appendTo(StringBuffer buffer, Calendar calendar) {
                this.appendTo(buffer, calendar.get(this.mField));
            }

        /**
         * 将指定的整数值追加到字符串缓冲区中
         * <p>
         * 根据数值的大小，以不同的方式将其转换为字符或字符串并追加到缓冲区中：
         * - 如果数值小于10，直接转换为对应的字符并追加
         * - 如果数值在10到99之间，调用appendDigits方法进行处理
         * - 如果数值大于等于100，直接追加整数值
         *
         * @param buffer 字符串缓冲区，用于追加转换后的字符串
         * @param value  需要追加的整数值
         */
        @Override
            public void appendTo(StringBuffer buffer, int value) {
                if (value < 10) {
                    buffer.append((char) (value + '0'));
                } else if (value < 100) {
                    appendDigits(buffer, value);
                } else {
                    buffer.append(value);
                }
            }
        }

    /**
     * 未补零的月份字段规则类
     * <p>
     * 用于处理不带前导零的月份格式化逻辑，主要负责将 Calendar 中的月份值格式化为字符串，并追加到 StringBuffer 中。
     * 月份值小于 10 时，直接追加字符；否则调用 appendDigits 方法进行格式化。
     *
     * @author 未知
     * @version 1.0.0
     * @date 2025.10.24
     * @since 1.0.0
     */
    private static class UnpaddedMonthField implements NumberRule {
        /** 月份字段实例，用于表示不带前导零的月份值 */
        static final UnpaddedMonthField INSTANCE = new UnpaddedMonthField();

        /**
         * 构造函数，用于初始化 UnpaddedMonthField 对象
         * <p>
         * 该构造函数调用父类的无参构造函数，用于初始化月份字段
         */
        UnpaddedMonthField() {
            super();
        }

        /**
         * 估算当前对象的长度
         * <p>
         * 返回一个预设的固定值 2，用于表示当前对象的估算长度。
         *
         * @return 当前对象的估算长度
         */
        @Override
        public int estimateLength() {
            return 2;
        }

        /**
         * 将当前对象的信息追加到指定的 StringBuffer 中，使用给定的日历对象的月份信息
         * <p>
         * 该方法会调用另一个重载的 appendTo 方法，并传入日历对象中月份的值（月份从1开始计算）
         *
         * @param buffer   要追加信息的 StringBuffer 对象
         * @param calendar 日历对象，用于获取月份信息
         */
        @Override
        public void appendTo(StringBuffer buffer, Calendar calendar) {
            this.appendTo(buffer, calendar.get(Calendar.MONTH) + 1);
        }

        /**
         * 将整数值追加到指定的 StringBuffer 对象中，以字符形式表示
         * <p>
         * 如果值小于 10，则直接将其转换为对应的字符并追加；否则调用 appendDigits 方法进行处理
         *
         * @param buffer 用于追加字符的 StringBuffer 对象
         * @param value  需要追加的整数值
         */
        @Override
        public final void appendTo(StringBuffer buffer, int value) {
            if (value < 10) {
                buffer.append((char) (value + '0'));
            } else {
                appendDigits(buffer, value);
            }
        }
    }

    /**
     * 用于表示带有固定位数的数字字段的记录类
     * <p>
     * 该类实现了 NumberRule 接口，用于格式化数字字段，确保输出的数字具有指定的位数，不足位数时前面补零。
     * 主要用于数据格式化场景，如日志记录、数据展示等。
     *
     * @author 未知
     * @version 1.0.0
     * @date 2025.10.24
     * @since 1.0.0
     */
    private record PaddedNumberField(int mField, int mSize) implements NumberRule {
        /**
         * 验证数字字段的大小是否满足最小要求
         * <p>
         * 检查当前数字字段的大小是否小于3，若小于则抛出非法参数异常，表示应使用无填充数字字段或两位数字字段。
         *
         * @param mSize 数字字段的大小
         * @throws IllegalArgumentException 当数字字段大小小于3时抛出
         */
        private PaddedNumberField {
            if (mSize < 3) {
                // Should use UnpaddedNumberField or TwoDigitNumberField.
                throw new IllegalArgumentException();
            }
        }

        /**
         * 估算当前对象的长度
         * <p>
         * 返回对象内部存储数据的大小，用于快速获取长度信息而无需实际计算
         *
         * @return 当前对象存储数据的大小
         */
        @Override
            public int estimateLength() {
                return this.mSize;
            }

        /**
         * 将当前对象的字段值追加到指定的 StringBuffer 中
         * <p>
         * 使用指定日历对象获取当前字段的值，并将其格式化后追加到 StringBuffer 中
         *
         * @param buffer   要追加内容的 StringBuffer 对象
         * @param calendar 用于获取字段值的日历对象
         */
        @Override
            public void appendTo(StringBuffer buffer, Calendar calendar) {
                this.appendTo(buffer, calendar.get(this.mField));
            }

        /**
         * 将指定的整数值追加到给定的 StringBuffer 中，以固定长度格式显示
         * <p>
         * 该方法首先用零填充 StringBuffer 到指定的长度，然后从右向左将数字的每一位写入缓冲区
         *
         * @param buffer 用于追加数字的 StringBuffer 对象
         * @param value  需要追加的整数值
         */
        @Override
            public void appendTo(StringBuffer buffer, int value) {
                // pad the buffer with adequate zeros
                for (int digit = 0; digit < this.mSize; ++digit) {
                    buffer.append('0');
                }
                // backfill the buffer with non-zero digits
                int index = buffer.length();
                for (; value > 0; value /= 10) {
                    buffer.setCharAt(--index, (char) ('0' + value % 10));
                }
            }
        }

    /**
     * 两位数字字段规则类
     * <p>
     * 用于格式化两位数字字段，根据指定的字段值进行格式化处理，确保输出为两位数字格式。
     * 如果值小于100，则使用补零方式格式化；否则直接输出原始值。
     *
     * @author 未知
     * @version 1.0.0
     * @date 2025.04.01
     * @since 1.0.0
     */
    private record TwoDigitNumberField(int mField) implements NumberRule {

        /**
         * 估算当前对象的长度
         * <p>
         * 该方法用于返回一个估计的长度值，具体实现为固定返回 2。
         *
         * @return 估计的长度值
         */
        @Override
            public int estimateLength() {
                return 2;
            }

        /**
         * 将当前对象的字段值追加到指定的 StringBuffer 中
         * <p>
         * 使用指定日历对象获取当前字段的值，并追加到缓冲区中
         *
         * @param buffer   要追加内容的 StringBuffer 对象
         * @param calendar 用于获取字段值的日历对象
         */
        @Override
            public void appendTo(StringBuffer buffer, Calendar calendar) {
                this.appendTo(buffer, calendar.get(this.mField));
            }

        /**
         * 将整数值追加到指定的 StringBuffer 中
         * <p>
         * 如果值小于 100，则调用 appendDigits 方法进行格式化追加；否则直接追加原始值。
         *
         * @param buffer 要追加内容的 StringBuffer 对象
         * @param value  要追加的整数值
         */
        @Override
            public void appendTo(StringBuffer buffer, int value) {
                if (value < 100) {
                    appendDigits(buffer, value);
                } else {
                    buffer.append(value);
                }
            }
        }

    /**
     * 两位数年份字段规则类
     * <p>
     * 用于处理两位数年份的格式化规则，主要应用于日期时间格式化场景中，将年份的后两位数字提取并追加到字符串缓冲区中。
     * 该类实现了 NumberRule 接口，提供估计长度和格式化年份的方法。
     *
     * @author 未知
     * @version 1.0.0
     * @date 2025.10.24
     * @since 1.0.0
     */
    private static class TwoDigitYearField implements NumberRule {
        /** 两位数的年份字段实例，用于表示年份的两位数格式 */
        static final TwoDigitYearField INSTANCE = new TwoDigitYearField();

        /**
         * 两位数年份字段的构造函数
         * <p>
         * 初始化两位数年份字段对象，继承自父类构造函数
         */
        TwoDigitYearField() {
            super();
        }

        /**
         * 估算当前对象的长度
         * <p>
         * 返回一个固定值 2，表示当前对象的估算长度
         *
         * @return 估算长度
         */
        @Override
        public int estimateLength() {
            return 2;
        }

        /**
         * 将当前对象的信息追加到指定的 StringBuffer 中，使用给定的年份的后两位进行格式化
         * <p>
         * 该方法会将当前对象的格式化信息追加到传入的 StringBuffer 对象中，年份部分使用传入的年份的后两位进行表示
         *
         * @param buffer 要追加信息的 StringBuffer 对象
         * @param year   年份的后两位数字，用于格式化输出
         */
        @Override
        public void appendTo(StringBuffer buffer, Calendar calendar) {
            this.appendTo(buffer, calendar.get(Calendar.YEAR) % 100);
        }

        /**
         * 将整数值追加到指定的 StringBuffer 对象中
         * <p>
         * 该方法调用 appendDigits 方法，将整数值转换为字符串并追加到缓冲区
         *
         * @param buffer 用于存储追加内容的 StringBuffer 对象
         * @param value  需要追加的整数值
         */
        @Override
        public final void appendTo(StringBuffer buffer, int value) {
            appendDigits(buffer, value);
        }
    }

    /**
     * 两位数月份字段规则类
     * <p>
     * 用于格式化日期中的月份字段，确保月份以两位数字的形式输出。该类实现了 {@link NumberRule} 接口，提供月份值的长度估计和格式化功能。
     * <p>
     * 该类为单例模式，通过 {@link #INSTANCE} 提供全局唯一实例。
     *
     * @author 未知
     * @version 1.0.0
     * @date 2025.10.24
     * @since 1.0.0
     */
    private static class TwoDigitMonthField implements NumberRule {
        /** 两位数月份字段的单例实例 */
        static final TwoDigitMonthField INSTANCE = new TwoDigitMonthField();

        /**
         * 初始化一个两位数的月份字段
         * <p>
         * 用于创建一个表示两位数月份的字段对象，通常用于日期格式化或解析场景
         *
         * @author 作者姓名
         * @since 指定版本
         */
        TwoDigitMonthField() {
            super();
        }

        /**
         * 估算当前对象的长度
         * <p>
         * 该方法返回一个固定的长度值 2，用于估算对象的长度。
         *
         * @return 固定的长度值 2
         */
        @Override
        public int estimateLength() {
            return 2;
        }

        /**
         * 将当前对象的内容追加到指定的 StringBuffer 中，并使用给定的 Calendar 对象的月份信息
         * <p>
         * 该方法会调用另一个重载的 appendTo 方法，并传入 Calendar 对象中月份的值（月份从 1 开始计数）
         *
         * @param buffer   要追加内容的 StringBuffer 对象
         * @param calendar 用于获取月份信息的 Calendar 对象
         */
        @Override
        public void appendTo(StringBuffer buffer, Calendar calendar) {
            this.appendTo(buffer, calendar.get(Calendar.MONTH) + 1);
        }

        /**
         * 将指定的整数值追加到 StringBuffer 中
         * <p>
         * 该方法调用 appendDigits 方法，将整数值转换为字符串并追加到 StringBuffer 对象中。
         *
         * @param buffer 要追加内容的 StringBuffer 对象
         * @param value  要追加的整数值
         */
        @Override
        public final void appendTo(StringBuffer buffer, int value) {
            appendDigits(buffer, value);
        }
    }

    /**
     * 表示12小时制的数字字段规则
     * <p>
     * 该记录类用于处理基于12小时制的数字字段格式化逻辑，继承自NumberRule接口，实现了对时间字段的格式化操作。
     * 在格式化时，会根据日历对象获取对应的12小时值，并按照指定的规则进行处理。
     *
     * @author 未知
     * @version 1.0.0
     * @date 2025.10.24
     * @since 1.0.0
     */
    private record TwelveHourField(NumberRule mRule) implements NumberRule {

        /**
         * 估计当前规则的长度
         * <p>
         * 调用内部规则对象的 estimateLength 方法，返回其估计的长度值
         *
         * @return 当前规则估计的长度
         */
        @Override
            public int estimateLength() {
                return this.mRule.estimateLength();
            }

        /**
         * 将当前规则的小时值格式化追加到指定的 StringBuffer 中
         * <p>
         * 该方法首先从 Calendar 对象中获取小时值，若小时值为 0，则将其替换为最小最大小时值加 1（即 12 小时制的 12）。然后调用 mRule 的 appendTo 方法，将格式化后的小时值追加到 StringBuffer 中。
         *
         * @param buffer   要追加内容的 StringBuffer 对象
         * @param calendar 用于获取小时值的 Calendar 对象
         */
        @Override
            public void appendTo(StringBuffer buffer, Calendar calendar) {
                int value = calendar.get(Calendar.HOUR);
                if (value == 0) {
                    value = calendar.getLeastMaximum(Calendar.HOUR) + 1;
                }
                this.mRule.appendTo(buffer, value);
            }

        /**
         * 将指定的值追加到 StringBuffer 中
         * <p>
         * 调用内部规则对象的 appendTo 方法，将值追加到缓冲区中
         *
         * @param buffer 要追加内容的 StringBuffer 对象
         * @param value  要追加的整数值
         */
        @Override
            public void appendTo(StringBuffer buffer, int value) {
                this.mRule.appendTo(buffer, value);
            }
        }

    /**
     * 表示一个基于24小时制的数字字段规则
     * <p>
     * 该记录类实现了 {@link NumberRule} 接口，用于处理基于24小时制的时间字段格式化逻辑。它通过委托方式调用内部的 {@link NumberRule} 实例来执行具体的格式化操作。
     * <p>
     * 在格式化过程中，若获取到的小时值为0，则将其转换为24小时制的最大值加1（即24），以确保符合24小时制的表示规范。
     *
     * @author 未知
     * @version 1.0.0
     * @date 2025.10.24
     * @since 1.0.0
     */
    private record TwentyFourHourField(NumberRule mRule) implements NumberRule {

        /**
         * 估计当前规则的长度
         * <p>
         * 调用内部规则对象的 estimateLength 方法，返回其估计的长度值
         *
         * @return 当前规则估计的长度
         */
        @Override
            public int estimateLength() {
                return this.mRule.estimateLength();
            }

        /**
         * 将当前规则的时间格式化并追加到指定的 StringBuffer 中
         * <p>
         * 该方法首先从 Calendar 对象中获取小时值，若小时值为 0，则将其调整为 24 小时制的 24。
         * 然后调用 mRule 的 appendTo 方法，将格式化后的时间值追加到 StringBuffer 中。
         *
         * @param buffer   用于追加格式化后时间的 StringBuffer 对象
         * @param calendar 用于获取时间信息的 Calendar 对象
         */
        @Override
            public void appendTo(StringBuffer buffer, Calendar calendar) {
                int value = calendar.get(Calendar.HOUR_OF_DAY);
                if (value == 0) {
                    value = calendar.getMaximum(Calendar.HOUR_OF_DAY) + 1;
                }
                this.mRule.appendTo(buffer, value);
            }

        /**
         * 将指定的整数值追加到 StringBuffer 中
         * <p>
         * 调用内部规则对象的 appendTo 方法，将整数值格式化后追加到缓冲区中
         *
         * @param buffer 要追加内容的 StringBuffer 对象
         * @param value  要追加的整数值
         */
        @Override
            public void appendTo(StringBuffer buffer, int value) {
                this.mRule.appendTo(buffer, value);
            }
        }

    //-----------------------------------------------------------------------

    /** 时间区域显示缓存，用于存储时间区域的显示名称 */
    private static final ConcurrentMap<TimeZoneDisplayKey, String> cTimeZoneDisplayCache =
        new ConcurrentHashMap<TimeZoneDisplayKey, String>(7);

    /**
     * 获取时区的显示名称
     * <p>
     * 根据指定的时区、是否夏令时、显示样式和区域设置，获取对应的时区显示名称。若缓存中不存在该值，则通过时区对象计算并缓存结果。
     *
     * @param tz       时区对象
     * @param daylight 是否启用夏令时
     * @param style    显示样式，如 {@link java.text.DateFormat#FULL}、{@link java.text.DateFormat#LONG} 等
     * @param locale   区域设置，用于本地化显示名称
     * @return 时区的显示名称
     */
    static String getTimeZoneDisplay(TimeZone tz, boolean daylight, int style, Locale locale) {
        TimeZoneDisplayKey key = new TimeZoneDisplayKey(tz, daylight, style, locale);
        String value = cTimeZoneDisplayCache.get(key);
        if (value == null) {
            // This is a very slow call, so cache the results.
            value = tz.getDisplayName(daylight, style, locale);
            String prior = cTimeZoneDisplayCache.putIfAbsent(key, value);
            if (prior != null) {
                value = prior;
            }
        }
        return value;
    }

    /**
     * 时间区名称规则类
     * <p>
     * 用于根据指定的时区、本地化设置和格式样式，生成对应的时间区名称规则。该类实现了 {@link Rule} 接口，主要用于格式化时间相关的显示内容，支持标准时间和夏令时的区分。
     * <p>
     * 该类通过构造函数初始化时区、本地化设置和格式样式，并根据这些参数获取标准时间和夏令时的显示名称。在格式化过程中，会根据日历中的夏令时偏移量决定使用标准时间还是夏令时名称。
     *
     * @author 未知
     * @version 1.0.0
     * @date 2025.10.24
     * @since 1.0.0
     */
    private static class TimeZoneNameRule implements Rule {
        /** 当前Locale配置 */
        private final Locale mLocale;
        /** 样式标识符，用于表示当前界面或元素的显示样式 */
        private final int mStyle;
        /** 标准值 */
        private final String mStandard;
        /** 白天模式启用标志 */
        private final String mDaylight;

        /**
         * 构造一个TimeZoneNameRule对象
         * <p>
         * 根据指定的时区、本地化设置和样式，初始化标准时间和夏令时的显示名称。
         *
         * @param timeZone 时区对象
         * @param locale   本地化设置
         * @param style    显示样式，用于控制名称的格式
         */
        TimeZoneNameRule(TimeZone timeZone, Locale locale, int style) {
            this.mLocale = locale;
            this.mStyle = style;

            this.mStandard = getTimeZoneDisplay(timeZone, false, style, locale);
            this.mDaylight = getTimeZoneDisplay(timeZone, true, style, locale);
        }

        /**
         * 估计当前时间格式化字符串的长度
         * <p>
         * 由于无法直接访问将要传递给 appendTo 方法的 Calendar 对象，因此基于构造函数传入的 TimeZone 来估算字符串长度。
         *
         * @return 时间格式化字符串的估计长度
         */
        @Override
        public int estimateLength() {
            // We have no access to the Calendar object that will be passed to
            // appendTo so base estimate on the TimeZone passed to the
            // constructor
            return Math.max(this.mStandard.length(), this.mDaylight.length());
        }

        /**
         * 将当前时间区域信息追加到指定的 StringBuffer 中
         * <p>
         * 根据日历对象获取时区信息，并根据是否包含夏令时偏移量，调用相应的方法获取时区显示字符串，追加到缓冲区中。
         *
         * @param buffer   用于追加时区信息的 StringBuffer 对象
         * @param calendar 包含时间信息的日历对象，用于获取时区和夏令时偏移量
         */
        @Override
        public void appendTo(StringBuffer buffer, Calendar calendar) {
            TimeZone zone = calendar.getTimeZone();
            if (calendar.get(Calendar.DST_OFFSET) != 0) {
                buffer.append(getTimeZoneDisplay(zone, true, this.mStyle, this.mLocale));
            } else {
                buffer.append(getTimeZoneDisplay(zone, false, this.mStyle, this.mLocale));
            }
        }
    }

    /**
     * 时间区数字规则记录类
     * <p>
     * 用于表示时间区数字格式的规则，支持不同格式的时区偏移量表示方式，如是否使用冒号分隔小时和分钟，是否遵循ISO 8601标准。
     * 提供了对时区偏移量的格式化方法，用于将 Calendar 对象中的时区信息转换为字符串表示。
     *
     * @author 未知
     * @version 1.0.0
     * @date 2025.10.24
     * @since 1.0.0
     */
    private record TimeZoneNumberRule(boolean mColon, boolean mISO8601) implements Rule {
        /** 用于表示冒号的时区数字规则实例 */
        static final TimeZoneNumberRule INSTANCE_COLON = new TimeZoneNumberRule(true, false);
        /** 无冒号的时间区数字规则实例 */
        static final TimeZoneNumberRule INSTANCE_NO_COLON = new TimeZoneNumberRule(false, false);
        /** ISO 8601 标准的时间区数字规则实例，表示时间区数字的格式符合 ISO 8601 规范 */
        static final TimeZoneNumberRule INSTANCE_ISO_8601 = new TimeZoneNumberRule(true, true);

        /**
         * 估算当前对象的长度
         * <p>
         * 返回一个固定的长度值 5，用于估算当前对象的长度。
         *
         * @return 固定的长度值 5
         */
        @Override
            public int estimateLength() {
                return 5;
            }

        /**
         * 将时间戳格式化为ISO 8601格式的字符串并追加到给定的StringBuffer中
         * <p>
         * 根据指定的日历对象，判断是否使用UTC时间区，并根据时区偏移量格式化时间偏移部分。
         * 如果时区偏移为负数，则使用'-'表示，否则使用'+'表示。格式化结果追加到StringBuffer中。
         *
         * @param buffer   用于追加格式化后时间偏移字符串的StringBuffer对象
         * @param calendar 用于获取时间信息的日历对象
         */
        @Override
            public void appendTo(StringBuffer buffer, Calendar calendar) {
                if (this.mISO8601 && calendar.getTimeZone().getID().equals("UTC")) {
                    buffer.append("Z");
                    return;
                }

                int offset = calendar.get(Calendar.ZONE_OFFSET) + calendar.get(Calendar.DST_OFFSET);

                if (offset < 0) {
                    buffer.append('-');
                    offset = -offset;
                } else {
                    buffer.append('+');
                }

                int hours = offset / (60 * 60 * 1000);
                appendDigits(buffer, hours);

                if (this.mColon) {
                    buffer.append(':');
                }

                int minutes = offset / (60 * 1000) - 60 * hours;
                appendDigits(buffer, minutes);
            }
        }

    /**
     * ISO 8601 时间格式规则记录类
     * <p>
     * 用于定义和表示 ISO 8601 标准中时间格式的规则，支持根据不同的时间长度（如小时、小时分钟、小时:分钟）生成对应的格式字符串。
     * <p>
     * 该类实现了 {@link Rule} 接口，提供时间格式的估计长度和格式化输出功能。
     *
     * @author dong4j
     * @version 1.0.0
     * @date 2025.10.24
     * @since 1.0.0
     */
    private record Iso8601_Rule(int length) implements Rule {

            // Sign TwoDigitHours or Z
        /** ISO8601 时间格式化规则，用于处理两位小时数或时区信息 */
        static final Iso8601_Rule ISO8601_HOURS = new Iso8601_Rule(3);
            // Sign TwoDigitHours Minutes or Z
        /** ISO8601 格式的时间规则，用于表示小时和分钟部分 */
        static final Iso8601_Rule ISO8601_HOURS_MINUTES = new Iso8601_Rule(5);
            // Sign TwoDigitHours : Minutes or Z
        /** 表示 ISO8601 格式中 "小时:分钟" 的解析规则 */
        static final Iso8601_Rule ISO8601_HOURS_COLON_MINUTES = new Iso8601_Rule(6);

        /**
         * 根据令牌长度获取对应的 ISO 8601 格式规则
         * <p>
         * 根据传入的令牌长度，返回对应的 ISO 8601 格式规则枚举值。支持的长度为 1、2、3。
         *
         * @param tokenLen 令牌长度
         * @return 对应的 ISO 8601 格式规则
         * @throws IllegalArgumentException 当令牌长度不合法时抛出
         */
        static Iso8601_Rule getRule(int tokenLen) {
                switch (tokenLen) {
                    case 1:
                        return Iso8601_Rule.ISO8601_HOURS;
                    case 2:
                        return Iso8601_Rule.ISO8601_HOURS_MINUTES;
                    case 3:
                        return Iso8601_Rule.ISO8601_HOURS_COLON_MINUTES;
                    default:
                        throw new IllegalArgumentException("invalid number of X");
                }
            }

        /**
         * 估算当前对象的长度
         * <p>
         * 返回该对象的长度属性值，用于长度估算。
         *
         * @return 当前对象的长度
         */
        @Override
            public int estimateLength() {
                return this.length;
            }

        /**
         * 将时间区域偏移信息追加到指定的 StringBuffer 中
         * <p>
         * 根据给定的 Calendar 对象获取时间区域偏移信息，并将其格式化为 "Z" 或 "+HH:mm"、"-HH:mm" 的形式追加到 StringBuffer 中。
         *
         * @param buffer   用于追加格式化后时间区域偏移信息的 StringBuffer 对象
         * @param calendar 包含时间区域信息的 Calendar 对象
         */
        @Override
            public void appendTo(StringBuffer buffer, Calendar calendar) {
                int zoneOffset = calendar.get(Calendar.ZONE_OFFSET);
                if (zoneOffset == 0) {
                    buffer.append("Z");
                    return;
                }

                int offset = zoneOffset + calendar.get(Calendar.DST_OFFSET);

                if (offset < 0) {
                    buffer.append('-');
                    offset = -offset;
                } else {
                    buffer.append('+');
                }

                int hours = offset / (60 * 60 * 1000);
                appendDigits(buffer, hours);

                if (this.length < 5) {
                    return;
                }

                if (this.length == 6) {
                    buffer.append(':');
                }

                int minutes = offset / (60 * 1000) - 60 * hours;
                appendDigits(buffer, minutes);
            }
        }

    // ----------------------------------------------------------------------

    /**
     * 时间区域显示键
     * <p>
     * 用于表示时间区域的显示键，包含时间区域、样式和语言环境信息。该类主要用于生成唯一的时间区域显示标识，支持基于不同样式和语言环境的区分。
     * <p>
     * 该类通过重写 hashCode 和 equals 方法，确保基于时间区域、样式和语言环境的唯一性判断。
     *
     * @author 未知
     * @version 1.0.0
     * @date 2025.10.24
     * @since 1.0.0
     */
    private static class TimeZoneDisplayKey {
        /** 时区配置，用于确定时间显示和处理的时区 */
        private final TimeZone mTimeZone;
        /** 样式标识符，用于表示当前界面或组件的样式类型 */
        private final int mStyle;
        /** 当前Locale配置 */
        private final Locale mLocale;

        /**
         * 构造一个 TimeZoneDisplayKey 对象
         * <p>
         * 根据指定的时区、是否启用夏令时、样式和区域设置初始化对象
         *
         * @param timeZone 时区对象
         * @param daylight 是否启用夏令时
         * @param style    显示样式
         * @param locale   区域设置
         */
        TimeZoneDisplayKey(TimeZone timeZone,
                           boolean daylight, int style, Locale locale) {
            this.mTimeZone = timeZone;
            if (daylight) {
                this.mStyle = style | 0x80000000;
            } else {
                this.mStyle = style;
            }
            this.mLocale = locale;
        }

        /**
         * 生成对象的哈希码
         * <p>
         * 根据对象的样式、区域设置和时区信息计算并返回哈希码
         *
         * @return 对象的哈希码值
         */
        @Override
        public int hashCode() {
            return (this.mStyle * 31 + this.mLocale.hashCode()) * 31 + this.mTimeZone.hashCode();
        }

        /**
         * 重写 equals 方法，用于比较两个 TimeZoneDisplayKey 对象是否相等
         * <p>
         * 比较逻辑基于时间区、样式和语言环境三个属性是否完全一致
         *
         * @param obj 要比较的对象
         * @return 如果对象相等则返回 true，否则返回 false
         */
        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj instanceof TimeZoneDisplayKey other) {
                return
                    this.mTimeZone.equals(other.mTimeZone) &&
                    this.mStyle == other.mStyle &&
                    this.mLocale.equals(other.mLocale);
            }
            return false;
        }
    }
}
