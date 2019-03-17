package info.dong4j.idea.plugin.enums;

import info.dong4j.idea.plugin.content.ImageContents;

import org.jetbrains.annotations.Contract;

/**
 * <p>Company: 科大讯飞股份有限公司-四川分公司</p>
 * <p>Description: ${description}</p>
 *
 * @author dong4j
 * @date 2019-03-14 10:23
 * @email sjdong3@iflytek.com
 */
public enum ImageMarkEnum {
    LARGE_PICTURE(1, "点击看大图", ImageContents.LARG_IMAGE_MARK),
    COMMON_PICTURE(2, "正常的", ImageContents.COMMON_IMAGE_MARK),
    CUSTOM(3, "自定义", ""),
    ORIGINAL(4, "原始", ImageContents.DEFAULT_IMAGE_MARK);

    public int index;
    public String text;
    public String code;

    ImageMarkEnum(int index, String text, String code) {
        this.index = index;
        this.text = text;
        this.code = code;
    }

    @Contract(pure = true)
    public int getIndex() {
        return index;
    }

    @Contract(pure = true)
    public String getText() {
        return text;
    }

    @Contract(pure = true)
    public String getCode() {
        return code;
    }
}
