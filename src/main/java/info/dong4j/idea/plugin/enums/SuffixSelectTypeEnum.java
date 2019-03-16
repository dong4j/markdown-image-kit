package info.dong4j.idea.plugin.enums;

import org.jetbrains.annotations.Contract;

/**
 * <p>Company: 科大讯飞股份有限公司-四川分公司</p>
 * <p>Description: </p>
 *
 * @author dong4j
 * @date 2019-03-17 01:21
 * @email sjdong3@iflytek.com
 */
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