package info.dong4j.idea.plugin.enums;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Optional;

/**
 * 插入图片时的操作枚举
 * <p>
 * 定义了插入图片时的不同操作类型，包括无操作、复制到不同位置、上传等操作。
 * 每个枚举值包含索引（value）和描述（desc）两个属性。
 *
 * @author dong4j
 * @version 1.0.0
 * @date 2025.11.01
 * @since 1.0.0
 */
public enum InsertImageActionEnum {
    /** 无特殊操作 */
    NONE(0, "无特殊操作"),
    /** 复制图片到当前文件夹 */
    COPY_TO_CURRENT(1, "复制图片到当前文件夹 (./)"),
    /** 复制图片到 assets 文件夹 */
    COPY_TO_ASSETS(2, "复制图片到 ./assets 文件夹"),
    /** 复制图片到文件名相关的 assets 文件夹 */
    COPY_TO_FILENAME_ASSETS(3, "复制图片到 ./${filename}.assets 文件夹"),
    /** 上传图片 */
    UPLOAD(4, "上传图片"),
    /** 复制到指定路径 */
    COPY_TO_CUSTOM(5, "复制到指定路径");

    /** 索引值 */
    public final int value;
    /** 描述信息 */
    public final String desc;

    /**
     * 构造函数
     *
     * @param value 索引值
     * @param desc  描述信息
     */
    @Contract(pure = true)
    InsertImageActionEnum(int value, @NotNull String desc) {
        this.value = value;
        this.desc = desc;
    }

    /**
     * 获取索引值
     *
     * @return 索引值
     */
    @Contract(pure = true)
    public int getValue() {
        return this.value;
    }

    /**
     * 获取描述信息
     *
     * @return 描述信息
     */
    @Contract(pure = true)
    public String getDesc() {
        return this.desc;
    }

    /**
     * 根据索引值获取对应的枚举
     *
     * @param value 索引值
     * @return 对应的枚举值，如果未找到则返回 null
     */
    public static InsertImageActionEnum of(int value) {
        Optional<InsertImageActionEnum> enumValue = Arrays.stream(InsertImageActionEnum.values())
            .filter(e -> e.getValue() == value)
            .findFirst();
        return enumValue.orElse(null);
    }

    /**
     * 获取所有描述信息数组
     * <p>
     * 按照枚举值的索引顺序返回描述信息数组，用于填充下拉框
     *
     * @return 描述信息数组
     */
    @NotNull
    public static String[] getDescriptions() {
        InsertImageActionEnum[] values = InsertImageActionEnum.values();
        String[] descriptions = new String[values.length];
        for (InsertImageActionEnum actionEnum : values) {
            descriptions[actionEnum.getValue()] = actionEnum.getDesc();
        }
        return descriptions;
    }
}

