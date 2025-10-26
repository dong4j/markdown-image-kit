package info.dong4j.idea.plugin.enums;

import info.dong4j.idea.plugin.content.ImageContents;

import org.jetbrains.annotations.Contract;

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
    /** 大图标记枚举值，表示图片上的“点击看大图”标记 */
    LARGE_PICTURE(1, "点击看大图", ImageContents.LARG_IMAGE_MARK),
    /** 图片标记类型枚举，表示正常图片的标记类型 */
    COMMON_PICTURE(2, "正常的", ImageContents.COMMON_IMAGE_MARK),
    /** 自定义图片标记枚举，用于表示自定义图片标记类型 */
    CUSTOM(3, "自定义", ""),
    /** 原始图片标记枚举，表示图片的原始状态 */
    ORIGINAL(4, "原始", ImageContents.DEFAULT_IMAGE_MARK);
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
}
