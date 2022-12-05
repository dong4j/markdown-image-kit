package info.dong4j.idea.plugin.enums;

import info.dong4j.idea.plugin.content.ImageContents;

import org.jetbrains.annotations.Contract;

/**
 * <p>Description: ${description}</p>
 *
 * @author dong4j
 * @version 0.0.1
 * @email "mailto:dong4j@gmail.com"
 * @date 2019.03.14 10:23
 * @since 0.0.1
 */
public enum ImageMarkEnum {
    /** Large picture image mark enum */
    LARGE_PICTURE(1, "点击看大图", ImageContents.LARG_IMAGE_MARK),
    /** Common picture image mark enum */
    COMMON_PICTURE(2, "正常的", ImageContents.COMMON_IMAGE_MARK),
    /** Custom image mark enum */
    CUSTOM(3, "自定义", ""),
    /** Original image mark enum */
    ORIGINAL(4, "原始", ImageContents.DEFAULT_IMAGE_MARK);

    /** Index */
    public int index;
    /** Text */
    public String text;
    /** Code */
    public String code;

    /**
     * Image mark enum
     *
     * @param index index
     * @param text  text
     * @param code  code
     * @since 0.0.1
     */
    ImageMarkEnum(int index, String text, String code) {
        this.index = index;
        this.text = text;
        this.code = code;
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
     * Gets text *
     *
     * @return the text
     * @since 0.0.1
     */
    @Contract(pure = true)
    public String getText() {
        return this.text;
    }

    /**
     * Gets code *
     *
     * @return the code
     * @since 0.0.1
     */
    @Contract(pure = true)
    public String getCode() {
        return this.code;
    }
}
