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
    String HTML_TAG_A_START = "<a";
    String HTML_TAG_A_END = "a>";
    String IMAGE_MARK_PREFIX = "![";
    String IMAGE_MARK_MIDDLE = "](";
    String IMAGE_MARK_SUFFIX = ")";
    String IMAGE_LOCATION = "http";
    String EXTEND_HTML_MARK = "<a data-fancybox title='${}' href='${}' >![${}](${})</a>";
    String COMMON_HTML_MARK = "<a title='${}' href='${}' >![${}](${})</a>";
}
