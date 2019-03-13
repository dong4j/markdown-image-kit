package info.dong4j.idea.plugin.enums;

import org.jetbrains.annotations.Contract;

public enum SuffixSelectTypeEnum {
    FILE_NAME(1, "文件名"),
    DATE_FILE_NAME(2, "日期-文件名"),
    RANDOM(3, "随机");

    public int index;
    public String name;

    SuffixSelectTypeEnum(int index, String name) {
        this.index = index;
        this.name = name;
    }

    @Contract(pure = true)
    public int getIndex() {
        return index;
    }

    @Contract(pure = true)
    public String getName() {
        return name;
    }
}