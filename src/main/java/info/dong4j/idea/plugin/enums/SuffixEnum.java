package info.dong4j.idea.plugin.enums;

import org.jetbrains.annotations.Contract;

/**
 * 后缀枚举类
 * <p>
 * 用于表示不同类型的文件后缀配置，包含文件名后缀、日期-文件名后缀和随机后缀三种类型。
 * 每个枚举值包含一个索引和一个名称，用于标识和描述对应的后缀类型。
 *
 * @author dong4j
 * @version 1.0.0
 * @date 2025.10.24
 * @since 1.0.0
 */
public enum SuffixEnum {
    /** 文件名后缀枚举 */
    FILE_NAME(0, "文件名"),
    /** 日期文件名后缀枚举，用于表示文件名中包含日期信息的格式 */
    DATE_FILE_NAME(1, "日期-文件名"),
    /** 随机后缀枚举值，表示随机生成的后缀类型 */
    RANDOM(2, "随机");
    /** 索引值，表示当前元素在集合或列表中的位置 */
    public final int index;
    /** 名称 */
    public final String name;

    /**
     * 枚举类，用于表示后缀类型
     * <p>
     * 构造函数，用于初始化枚举实例的索引和名称
     *
     * @param index 索引值
     * @param name  名称
     * @since 0.0.1
     */
    SuffixEnum(int index, String name) {
        this.index = index;
        this.name = name;
    }

    /**
     * 获取当前索引值
     * <p>
     * 返回该对象维护的索引值
     *
     * @return 当前索引值
     * @since 0.0.1
     */
    @Contract(pure = true)
    public int getIndex() {
        return this.index;
    }

    /**
     * 获取名称
     * <p>
     * 返回当前对象的名称属性值
     *
     * @return 名称
     * @since 0.0.1
     */
    @Contract(pure = true)
    public String getName() {
        return this.name;
    }
}
