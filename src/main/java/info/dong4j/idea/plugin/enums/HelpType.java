package info.dong4j.idea.plugin.enums;

/**
 * 帮助类型枚举
 * <p>
 * 定义系统中支持的帮助类型，用于标识不同类别的帮助请求或功能模块。
 * 包括设置类帮助、通知类帮助和自定义帮助三种类型。
 * </p>
 *
 * @author dong4j
 * @version 0.0.1
 * @email "mailto:dong4j@gmail.com"
 * @date 2019.03.25
 * @since 0.0.1
 */
public enum HelpType {
    /** 设置帮助类型 */
    SETTING("setting"),
    /** 通知帮助类型 */
    NOTTIFY("notify"),
    /** 自定义帮助类型 */
    CUSTOM("custom");
    /** 查询条件字段，用于存储 SQL 查询语句或过滤条件 */
    public final String where;

    /**
     * 根据指定的 where 参数创建 HelpType 实例
     * <p>
     * 该构造方法用于初始化 HelpType 对象，传入的 where 参数表示帮助类型所属的上下文或位置。
     *
     * @param where 指定帮助类型的位置或上下文信息
     */
    HelpType(String where) {
        this.where = where;
    }
}
