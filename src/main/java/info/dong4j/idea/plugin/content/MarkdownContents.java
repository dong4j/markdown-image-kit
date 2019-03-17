package info.dong4j.idea.plugin.content;

/**
 * <p>Company: 科大讯飞股份有限公司-四川分公司</p>
 * <p>Description: </p>
 *
 * @author dong4j
 * @email sjdong3@iflytek.com
 * @since 2019-03-16 12:18
 */
public interface MarkdownContents {
    /** markdown 文件后缀 */
    String MARKDOWN_FILE_SUFIX = ".md";
    /** markdown file type, 必须安装了 markdown 插件才能识别为此类型 */
    String MARKDOWN_FILE_TYPE = "Markdown";
    /** 默认的 image 标签替换类型 */
    String DEFAULT_IMAGE_MARK = "![${}](${})";
    /** 点击查看大图, 需要添加 js 支持 */
    String LARG_IMAGE_MARK = "<a data-fancybox title='${}' href='${}' >![${}](${})</a>";
    /** 就一个 a 标签, 点击能在新页面查看图片 */
    String COMMON_IMAGE_MARK = "<a title='${}' href='${}' >![${}](${})</a>";
}
