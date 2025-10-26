package info.dong4j.idea.plugin.content;

/**
 * 图片内容处理接口
 * <p>
 * 提供图片相关标记和常量定义，用于在文本中插入图片链接或富文本格式的图片展示。包含基础的图片标记格式、HTML标签定义以及用于生成不同图片展示方式的字符串常量。
 * <p>
 * 支持生成普通图片链接、点击查看大图（需配合JavaScript实现）等格式，并提供对应的ID截取方式。
 *
 * @author dong4j
 * @version 0.0.1
 * @date 2021.02.14
 */
public interface ImageContents {
    /** 图片类型名称常量，用于标识图片资源类型 */
    String IMAGE_TYPE_NAME = "Image";
    /** HTML 标签开始标记，用于标识 <a 标签的起始 */
    String HTML_TAG_A_START = "<a";
    /** HTML 标签 a 的结束标签 */
    String HTML_TAG_A_END = "a>";
    /** 图片标记前缀，用于标识图片标记的起始符号 */
    String IMAGE_MARK_PREFIX = "![";
    /** 图片中间标记符号，用于标识图片在文本中的中间位置 */
    String IMAGE_MARK_MIDDLE = "](";
    /** 图片标记后缀 */
    String IMAGE_MARK_SUFFIX = ")";
    /** 图片存储路径的默认前缀 */
    String IMAGE_LOCATION = "http";
    // String LINE_BREAK = System.lineSeparator();
    /** 换行符，用于在字符串中表示换行 */
    String LINE_BREAK = "\n";
    /** 默认的 image 标签替换类型，用于替换 Markdown 中的图片占位符 */
    String DEFAULT_IMAGE_MARK = "![${title}](${path})";
    /** 点击查看大图的标记，包含 JavaScript 支持以实现弹窗预览功能 */
    String LARG_IMAGE_MARK = "<a data-fancybox title='${title}' href='${path}' >" + DEFAULT_IMAGE_MARK + "</a>";
    /** 大图标记ID，由LARG_IMAGE_MARK字段的前23个字符生成 */
    String LARG_IMAGE_MARK_ID = LARG_IMAGE_MARK.substring(1, 23);
    /** 包含图片的通用超链接标记，点击可在新页面查看图片 */
    String COMMON_IMAGE_MARK = "<a title='${title}' href='${path}' >" + DEFAULT_IMAGE_MARK + "</a>";
    /** 常量字段，表示图片标记的固定ID，由COMMON_IMAGE_MARK的前8位字符组成 */
    String COMMON_IMAGE_MARK_ID = COMMON_IMAGE_MARK.substring(1, 9);
}
