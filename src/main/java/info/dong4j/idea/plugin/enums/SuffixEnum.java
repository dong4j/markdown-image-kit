package info.dong4j.idea.plugin.enums;

import org.jetbrains.annotations.Contract;

/**
 * <p>Description: </p>
 *
 * @author dong4j
 * @version 0.0.1
 * @email "mailto:dong4j@gmail.com"
 * @date 2019.03.17 01:21
 * @since 0.0.1
 */
public enum SuffixEnum {
    /** File name suffix enum */
    FILE_NAME(0, "文件名"),
    /** Date file name suffix enum */
    DATE_FILE_NAME(1, "日期-文件名"),
    /** Random suffix enum */
    RANDOM(2, "随机");

    /** Index */
    public int index;
    /** Name */
    public String name;

    /**
     * Suffix enum
     *
     * @param index index
     * @param name  name
     * @since 0.0.1
     */
    SuffixEnum(int index, String name) {
        this.index = index;
        this.name = name;
    }

    /**
     * Gets index *
     *
     * @return the index
     * @since 0.0.1
     */
    @Contract(pure = true)
    public int getIndex() {
        return this.index;
    }

    /**
     * Gets name *
     *
     * @return the name
     * @since 0.0.1
     */
    @Contract(pure = true)
    public String getName() {
        return this.name;
    }
}
