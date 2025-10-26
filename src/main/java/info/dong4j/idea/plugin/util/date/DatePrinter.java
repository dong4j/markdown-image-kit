package info.dong4j.idea.plugin.util.date;

import java.text.FieldPosition;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * DatePrinter 接口
 * <p>
 * 提供日期格式化功能，支持对毫秒值、Date 对象和 Calendar 对象进行格式化操作，可将格式化结果输出为字符串或追加到 StringBuffer 中。
 * 该接口定义了多种格式化方法，适用于需要自定义日期格式化逻辑的场景。
 * </p>
 *
 * @author 未知
 * @version 1.0.0
 * @date 2025.01.01
 * @since 2.1
 */
public interface DatePrinter {
    /**
     * 格式化毫秒数的 long 值。
     * <p>
     * 将给定的毫秒数转换为对应的格式化字符串。
     *
     * @param millis 毫秒数值
     * @return 格式化后的字符串
     * @since 2.1
     */
    String format(long millis);

    /**
     * 根据日期对象格式化日期字符串
     * <p>
     * 使用 GregorianCalendar 对日期对象进行格式化，返回对应的字符串表示
     *
     * @param date 要格式化的日期对象
     * @return 格式化后的日期字符串
     */
    String format(Date date);

    /**
     * 格式化一个 Calendar 对象。
     * <p>
     * 将传入的 Calendar 实例转换为对应的字符串表示形式。
     *
     * @param calendar 要格式化的日历对象
     * @return 格式化后的字符串
     */
    String format(Calendar calendar);

    /**
     * 将毫秒时间值格式化到指定的字符串缓冲区中。
     * <p>
     * 该方法将给定的毫秒数转换为可读的时间格式，并写入到提供的字符串缓冲区中。
     *
     * @param millis 毫秒时间值
     * @param buf    要写入格式化后时间的字符串缓冲区
     * @return 指定的字符串缓冲区
     */
    StringBuffer format(long millis, StringBuffer buf);

    /**
     * 将 Date 对象格式化到指定的 StringBuffer 中，使用 GregorianCalendar 进行格式化。
     *
     * @param date 要格式化的日期对象
     * @param buf  格式化输出的目标缓冲区
     * @return 指定的字符串缓冲区
     */
    StringBuffer format(Date date, StringBuffer buf);

    /**
     * 根据日历对象格式化内容到指定的字符串缓冲区
     * <p>
     * 将给定的日历对象按照指定格式转换为字符串，并写入到指定的缓冲区中。
     *
     * @param calendar 日历对象，用于格式化
     * @param buf      目标缓冲区，用于存储格式化后的内容
     * @return 指定的字符串缓冲区，包含格式化后的内容
     */
    StringBuffer format(Calendar calendar, StringBuffer buf);

    // Accessors
    //-----------------------------------------------------------------------

    /**
     * 获取此打印机使用的格式模式。
     * <p>
     * 返回格式模式，该模式与 java.text.SimpleDateFormat 兼容。
     *
     * @return 格式模式，与 java.text.SimpleDateFormat 兼容
     */
    String getPattern();

    /**
     * 获取此打印机使用的时区
     * <p>
     * 此时区始终用于 {@code Date} 的打印操作。
     *
     * @return 时区对象
     */
    TimeZone getTimeZone();

    /**
     * 获取此打印机使用的语言环境
     * <p>
     * 返回当前打印机所使用语言环境的配置信息
     *
     * @return 语言环境
     */
    Locale getLocale();

    /**
     * 格式化一个日期、日历或毫秒数对象。
     * <p>
     * 该方法用于将给定的对象格式化为字符串，并追加到指定的缓冲区中。
     * 注意：位置参数 pos 被忽略。
     *
     * @param obj        需要格式化的对象，可以是 Date、Calendar 或 Long 类型
     * @param toAppendTo 用于追加格式化结果的缓冲区
     * @param pos        位置参数，用于记录格式化后的位置信息，此处不使用
     * @return 格式化后的缓冲区
     */
    StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition pos);
}
