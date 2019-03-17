package info.dong4j.idea.plugin.content;

/**
 * <p>Company: 科大讯飞股份有限公司-四川分公司</p>
 * <p>Description: </p>
 *
 * @author dong4j
 * @email sjdong3@iflytek.com
 * @since 2019-03-15 18:29
 */
public interface ImageContents {
    String IMAGE_TYPE_NAME = "Image";
    String HTML_TAG_A_START = "<a";
    String HTML_TAG_A_END = "a>";
    String IMAGE_MARK_PREFIX = "![";
    String IMAGE_MARK_MIDDLE = "](";
    String IMAGE_MARK_SUFFIX = ")";
    String IMAGE_LOCATION = "http";
    String LINE_BREAK = "\n";
    /** 默认的 image 标签替换类型 */
    String DEFAULT_IMAGE_MARK = "![${}](${})";
    /** 点击查看大图, 需要添加 js 支持 */
    String LARG_IMAGE_MARK = "<a data-fancybox title='${}' href='${}' >" + DEFAULT_IMAGE_MARK + "</a>";
    String LARG_IMAGE_MARK_ID = LARG_IMAGE_MARK.substring(1, 23);
    /** 就一个 a 标签, 点击能在新页面查看图片 */
    String COMMON_IMAGE_MARK = "<a title='${}' href='${}' >" + DEFAULT_IMAGE_MARK + "</a>";
    String COMMON_IMAGE_MARK_ID = COMMON_IMAGE_MARK.substring(1, 9);
}
