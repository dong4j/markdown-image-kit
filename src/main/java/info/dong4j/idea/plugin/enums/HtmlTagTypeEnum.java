package info.dong4j.idea.plugin.enums;

import org.jetbrains.annotations.Contract;

/**
 * <p>Company: 科大讯飞股份有限公司-四川分公司</p>
 * <p>Description: ${description}</p>
 *
 * @author dong4j
 * @date 2019-03-14 10:23
 * @email sjdong3@iflytek.com
 */
public enum HtmlTagTypeEnum {
    LARGE_PICTURE(1, "点击看大图", "<a data-fancybox title='${}' href='${}' >![${}](${})</a>"),
    COMMON_PICTURE(2, "正常的","<a title='${}' href='${}' >![${}](${})</a>"),
    CUSTOM(3, "自定义","");

    public int index;
    public String text;
    public String code;

    HtmlTagTypeEnum(int index, String text, String code) {
        this.index = index;
        this.text = text;
        this.code = code;
    }

    @Contract(pure = true)
    public int getIndex() {
        return index;
    }

    @Contract(pure = true)
    public String getText(){
        return text;
    }

    @Contract(pure = true)
    public String getCode() {
        return code;
    }
}
