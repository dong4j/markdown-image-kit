package info.dong4j.idea.plugin.chain;

import com.intellij.openapi.editor.Document;

import info.dong4j.idea.plugin.entity.MarkdownImage;

import java.util.List;
import java.util.Map;

/**
 * Markdown 文件过滤接口
 * <p>
 * 用于定义对 Markdown 文档中图片进行过滤的逻辑，支持根据指定的过滤字符串排除不符合条件的图片。
 * 该接口提供了一个常量 FILTER_KEY，用于标识过滤器的唯一标识符。
 * <p>
 * 实现该接口的类需要提供具体的过滤规则，例如根据图片的 URL、文件名或内容匹配过滤条件。
 *
 * @author dong4j
 * @version 1.0.0
 * @date 2025.10.24
 * @since 0.0.1
 */
public interface MarkdownFileFilter {
    /** 过滤器键名，用于标识过滤器的唯一标识符 */
    String FILTER_KEY = "filter_key";

    /**
     * 按要求排除不需要的 MarkdownImage
     * <p>
     * 根据过滤字符串对等待处理的 MarkdownImage 进行过滤，移除不符合条件的图像。
     *
     * @param waitingProcessMap 等待处理的文档与 MarkdownImage 列表的映射
     * @param filterString      用于过滤的字符串
     */
    void filter(Map<Document, List<MarkdownImage>> waitingProcessMap, String filterString);
}
