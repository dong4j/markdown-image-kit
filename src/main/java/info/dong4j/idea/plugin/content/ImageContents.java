package info.dong4j.idea.plugin.content;

/**
 * <p>Description: </p>
 *
 * @author dong4j
 * @version 0.0.1
 * @email "mailto:dong4j@gmail.com"
 * @date 2021.02.14 18:40
 * @since 0.0.1
 */
public interface ImageContents {
    /** IMAGE_TYPE_NAME */
    String IMAGE_TYPE_NAME = "Image";
    /** HTML_TAG_A_START */
    String HTML_TAG_A_START = "<a";
    /** HTML_TAG_A_END */
    String HTML_TAG_A_END = "a>";
    /** IMAGE_MARK_PREFIX */
    String IMAGE_MARK_PREFIX = "![";
    /** IMAGE_MARK_MIDDLE */
    String IMAGE_MARK_MIDDLE = "](";
    /** IMAGE_MARK_SUFFIX */
    String IMAGE_MARK_SUFFIX = ")";
    /** IMAGE_LOCATION */
    String IMAGE_LOCATION = "http";
    /** LINE_BREAK */
    // String LINE_BREAK = System.lineSeparator();
    String LINE_BREAK = "\n";
    /** 默认的 image 标签替换类型 */
    String DEFAULT_IMAGE_MARK = "![${title}](${path})";
    /** 点击查看大图, 需要添加 js 支持 */
    String LARG_IMAGE_MARK = "<a data-fancybox title='${title}' href='${path}' >" + DEFAULT_IMAGE_MARK + "</a>";
    /** LARG_IMAGE_MARK_ID */
    String LARG_IMAGE_MARK_ID = LARG_IMAGE_MARK.substring(1, 23);
    /** 就一个 a 标签, 点击能在新页面查看图片 */
    String COMMON_IMAGE_MARK = "<a title='${title}' href='${path}' >" + DEFAULT_IMAGE_MARK + "</a>";
    /** COMMON_IMAGE_MARK_ID */
    String COMMON_IMAGE_MARK_ID = COMMON_IMAGE_MARK.substring(1, 9);
}
