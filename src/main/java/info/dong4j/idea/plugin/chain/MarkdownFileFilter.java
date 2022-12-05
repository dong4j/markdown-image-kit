package info.dong4j.idea.plugin.chain;

import com.intellij.openapi.editor.Document;

import info.dong4j.idea.plugin.entity.MarkdownImage;

import java.util.List;
import java.util.Map;

/**
 * <p>Description: </p>
 *
 * @author dong4j
 * @version 0.0.1
 * @email "mailto:dong4j@gmail.com"
 * @date 2021.02.14 18:40
 * @since 0.0.1
 */
public interface MarkdownFileFilter {
    /** FILTER_KEY */
    String FILTER_KEY = "filter_key";

    /**
     * 按要求排除不需要的 MarkdownImage
     *
     * @param waitingProcessMap the waiting process map
     * @param filterString      the filter string
     * @since 0.0.1
     */
    void filter(Map<Document, List<MarkdownImage>> waitingProcessMap, String filterString);
}
