package info.dong4j.idea.plugin.enums;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Optional;

/**
 * 图片编辑器枚举
 * <p>
 * 定义了支持的图片编辑器类型，包括 Shottr 和 CleanShot X。
 * 每个枚举值包含索引（value）、名称（name）和 URL scheme（scheme）三个属性。
 *
 * @author dong4j
 * @version 1.0.0
 * @date 2025.11.01
 * @since 1.0.0
 */
public enum ImageEditorEnum {
    /** CleanShot X 图片编辑器 */
    CLEANSHOT_X(0, "CleanShot X", "cleanshot://open-annotate?filepath="),
    /** Shottr 图片编辑器 */
    SHOTTR(1, "Shottr", "shottr://load/clipboard"),
    /**
     * Draw.io 图片编辑器
     *
     * @see ImageEditorEnum
     */
    DRAWIO(2, "Draw.io", "drawio://open?title=Image&url=");

    /** 索引值 */
    public final int value;
    /** 编辑器名称 */
    public final String name;
    /** URL scheme */
    public final String scheme;

    /**
     * 构造函数
     *
     * @param value  索引值
     * @param name   编辑器名称
     * @param scheme URL scheme
     */
    @Contract(pure = true)
    ImageEditorEnum(int value, @NotNull String name, @NotNull String scheme) {
        this.value = value;
        this.name = name;
        this.scheme = scheme;
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
     * 获取编辑器名称
     *
     * @return 编辑器名称
     */
    @Contract(pure = true)
    public String getName() {
        return this.name;
    }

    /**
     * 获取 URL scheme
     *
     * @return URL scheme
     */
    @Contract(pure = true)
    public String getScheme() {
        return this.scheme;
    }

    /**
     * 根据索引值获取对应的枚举
     *
     * @param value 索引值
     * @return 对应的枚举值，如果未找到则返回 null
     */
    @Nullable
    public static ImageEditorEnum of(int value) {
        Optional<ImageEditorEnum> enumValue = Arrays.stream(ImageEditorEnum.values())
            .filter(e -> e.getValue() == value)
            .findFirst();
        return enumValue.orElse(null);
    }

    /**
     * 获取所有名称数组
     * <p>
     * 按照枚举值的索引顺序返回名称数组，用于填充下拉框
     *
     * @return 名称数组
     */
    @NotNull
    public static String[] getNames() {
        ImageEditorEnum[] values = ImageEditorEnum.values();
        String[] names = new String[values.length];
        for (ImageEditorEnum editorEnum : values) {
            names[editorEnum.getValue()] = editorEnum.getName();
        }
        return names;
    }
}

