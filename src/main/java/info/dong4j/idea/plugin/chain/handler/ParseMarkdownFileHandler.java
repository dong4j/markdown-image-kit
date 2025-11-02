package info.dong4j.idea.plugin.chain.handler;

import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.editor.Document;

import info.dong4j.idea.plugin.MikBundle;
import info.dong4j.idea.plugin.chain.MarkdownFileFilter;
import info.dong4j.idea.plugin.entity.EventData;
import info.dong4j.idea.plugin.entity.MarkdownImage;
import info.dong4j.idea.plugin.util.MarkdownUtils;

import java.util.List;
import java.util.Map;

import lombok.Setter;

/**
 * 解析 Markdown 文件的处理器类
 * <p>
 * 用于处理 Markdown 文件的解析逻辑，支持从事件数据中获取待处理文件信息，若无则通过工具类解析并设置。同时提供文件过滤功能，可根据配置过滤特定内容。
 * <p>
 * 该类继承自 ActionHandlerAdapter，用于适配插件系统中的动作处理机制。
 *
 * @author dong4j
 * @version 0.0.1
 * @date 2021.02.14
 * @since 0.0.1
 */
@Setter
public class ParseMarkdownFileHandler extends ActionHandlerAdapter {
    /** Markdown 文件过滤器，用于筛选符合要求的 Markdown 文件 */
    private MarkdownFileFilter fileFilter;

    /**
     * 获取文件标题名称
     * <p>
     * 返回与Markdown文件解析相关的标题名称，用于界面展示或操作提示
     *
     * @return 文件标题名称
     * @since 0.0.1
     */
    @Override
    public String getName() {
        return MikBundle.message("mik.action.resolve.markdown.file.title");
    }

    /**
     * 执行事件处理逻辑，优先使用 EventData 中的数据，若无则解析 Markdown 文件
     * <p>
     * 该方法首先尝试从 EventData 中获取等待处理的 Markdown 图片信息，若为空则解析当前文档或文件树中的 Markdown 文件。
     * 若配置了文件过滤器，则进行过滤处理。最后根据是否有数据决定是否继续执行后续处理逻辑。
     *
     * @param data 事件数据对象，包含处理所需的信息
     * @return 若存在等待处理的数据则返回 true，否则返回 false
     */
    @Override
    public boolean execute(EventData data) {
        // 优先处理设置的数据, 用于 ImageMoveIntentionAction 和 ImageUploadIntentionAction
        Map<Document, List<MarkdownImage>> waitingProcessMap = data.getWaitingProcessMap();
        if (waitingProcessMap == null || waitingProcessMap.isEmpty()) {
            // 解析当前文档或者选择的文件树中的所有 markdown 文件.
            waitingProcessMap = MarkdownUtils.getProcessMarkdownInfo(data.getActionEvent(), data.getProject());
            data.setWaitingProcessMap(waitingProcessMap);
        }

        if (this.fileFilter != null) {
            PropertiesComponent propComp = PropertiesComponent.getInstance();
            this.fileFilter.filter(waitingProcessMap, propComp.getValue(MarkdownFileFilter.FILTER_KEY));
        }

        // 有数据才执行后面的 handler
        return !waitingProcessMap.isEmpty();
    }
}
