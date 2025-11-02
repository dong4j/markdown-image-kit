package info.dong4j.idea.plugin.enums;

import info.dong4j.idea.plugin.content.ImageContents;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Optional;

/**
 * 图片标记枚举
 * <p>
 * 定义图片标记的类型，包括大图、正常图、自定义图和原始图。每个枚举值包含对应的索引、显示文本和标记代码。
 * <p>
 * 该枚举用于标识不同类型的图片标记，常用于图片处理或展示逻辑中，根据标记类型执行相应的操作。
 *
 * @author dong4j
 * @version 0.0.1
 * @date 2019.03.14
 * @since 0.0.1
 */
public enum ImageMarkEnum {
    /** 原始图片标记枚举，表示图片的原始状态 */
    ORIGINAL(0, "原始", ImageContents.DEFAULT_IMAGE_MARK),
    /** 正常图片标记枚举，表示正常图片的标记类型 */
    COMMON_PICTURE(1, "正常", ImageContents.COMMON_IMAGE_MARK),
    /** 大图标记枚举值，表示图片上的"点击看大图"标记 */
    LARGE_PICTURE(2, "点击放大", ImageContents.LARG_IMAGE_MARK),
    /** 自定义图片标记枚举，用于表示自定义图片标记类型 */
    CUSTOM(3, "自定义", "");
    /** 索引值，表示当前元素在集合或列表中的位置 */
    public final int index;
    /** 文本内容 */
    public final String text;
    /** 代码标识符 */
    public final String code;

    /**
     * 枚举类，用于表示图片标记类型
     * <p>
     * 该枚举通过索引、文本和编码三个属性来标识不同的图片标记
     *
     * @param index 索引值，用于标识枚举项的顺序
     * @param text  显示文本，用于用户界面展示
     * @param code  编码值，用于系统内部标识
     * @since 0.0.1
     */
    ImageMarkEnum(int index, String text, String code) {
        this.index = index;
        this.text = text;
        this.code = code;
    }

    /**
     * 获取当前对象的索引值
     * <p>
     * 返回该对象内部维护的索引属性值
     *
     * @return 当前对象的索引值
     * @since 0.0.1
     */
    @Contract(pure = true)
    public int getIndex() {
        return this.index;
    }

    /**
     * 获取文本内容
     * <p>
     * 返回当前对象存储的文本信息
     *
     * @return 文本内容
     * @since 0.0.1
     */
    @Contract(pure = true)
    public String getText() {
        return this.text;
    }

    /**
     * 获取代码值
     * <p>
     * 返回当前对象存储的代码值
     *
     * @return 代码值
     * @since 0.0.1
     */
    @Contract(pure = true)
    public String getCode() {
        return this.code;
    }

    /**
     * 根据索引值获取对应的枚举
     *
     * @param index 索引值
     * @return 对应的枚举值，如果未找到则返回 COMMON_PICTURE
     */
    @NotNull
    public static ImageMarkEnum of(int index) {
        Optional<ImageMarkEnum> enumValue = Arrays.stream(ImageMarkEnum.values())
            .filter(e -> e.getIndex() == index)
            .findFirst();
        return enumValue.orElse(COMMON_PICTURE);
    }

    /**
     * 根据文本获取对应的枚举
     *
     * @param text 文本内容
     * @return 对应的枚举值，如果未找到则返回 null
     */
    @Nullable
    public static ImageMarkEnum ofText(@Nullable String text) {
        if (text == null || text.trim().isEmpty()) {
            return null;
        }
        Optional<ImageMarkEnum> enumValue = Arrays.stream(ImageMarkEnum.values())
            .filter(e -> e.getText().equals(text))
            .findFirst();
        return enumValue.orElse(null);
    }

    /**
     * 获取所有描述信息数组（仅包含用于 UI 显示的选项）
     * <p>
     * 按照枚举值的索引顺序返回描述信息数组，用于填充下拉框
     * 注意：不包含 ORIGINAL 类型，因为它不用于标签替换选择
     *
     * @return 描述信息数组
     */
    @NotNull
    public static String[] getDescriptions() {
        return new String[] {
            ORIGINAL.getText() + ": " + ORIGINAL.getCode(),
            COMMON_PICTURE.getText() + ": " + COMMON_PICTURE.getCode(),
            LARGE_PICTURE.getText() + ": " + LARGE_PICTURE.getCode(),
            CUSTOM.getText() + ": 使用 ${title} 和 ${path} 占位符构建标签"
        };
    }

}
