package info.dong4j.idea.plugin.content;

/**
 * Markdown 内容接口
 * <p>
 * 定义与 Markdown 文件相关的常量，包括文件后缀和文件类型名称。该接口用于标识和处理 Markdown 格式的内容。
 *
 * @author dong4j
 * @version 0.0.1
 * @date 2021.02.14
 * @since 0.0.1
 */
public interface MarkdownContents {
    /** markdown 文件后缀 */
    String MARKDOWN_FILE_SUFIX = ".md";
    /** Markdown 文件类型，必须安装了 markdown 插件才能识别为此类型 */
    String MARKDOWN_TYPE_NAME = "Markdown";
}
