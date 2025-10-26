package info.dong4j.idea.plugin.chain;

import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.progress.ProgressIndicator;

import info.dong4j.idea.plugin.MikBundle;
import info.dong4j.idea.plugin.entity.EventData;
import info.dong4j.idea.plugin.entity.MarkdownImage;
import info.dong4j.idea.plugin.util.StringUtils;

import java.util.List;
import java.util.Map;

/**
 * 替换原有标签的处理类
 * <p>
 * 该类用于执行替换文档中旧标签的操作，主要功能是遍历待处理的文档和图片信息，根据指定的替换规则更新文档内容。
 * 支持进度指示和文本更新，适用于需要批量替换文档中特定标记的场景。
 *
 * @author dong4j
 * @version 0.0.1
 * @date 2021.02.14
 * @since 0.0.1
 */
public class ReplaceToDocument extends ActionHandlerAdapter {
    /**
     * 获取名称
     * <p>
     * 返回预定义的名称字符串，用于表示替换旧操作的标题
     *
     * @return 名称字符串
     * @since 0.0.1
     */
    @Override
    public String getName() {
        return MikBundle.message("mik.action.replace.old.title");
    }

    /**
     * 执行处理逻辑，用于处理文档中的 Markdown 图片标记替换
     * <p>
     * 该方法从事件数据中获取待处理的文档和图片信息，逐个处理图片标记，并更新文档内容。
     * 同时通过进度指示器实时反馈处理进度和当前处理的图片名称。
     *
     * @param data 事件数据，包含需要处理的文档、图片信息以及进度指示器等
     * @return 始终返回 true，表示处理成功
     */
    @Override
    public boolean execute(EventData data) {
        ProgressIndicator indicator = data.getIndicator();
        int size = data.getSize();
        int totalProcessed = 0;

        for (Map.Entry<Document, List<MarkdownImage>> imageEntry : data.getWaitingProcessMap().entrySet()) {
            Document document = imageEntry.getKey();
            int totalCount = imageEntry.getValue().size();
            for (MarkdownImage markdownImage : imageEntry.getValue()) {
                String imageName = markdownImage.getImageName();
                indicator.setFraction(((++totalProcessed * 1.0) + data.getIndex() * size) / totalCount * size);
                indicator.setText2("Processing " + imageName);

                String finalMark = markdownImage.getFinalMark();
                if(StringUtils.isBlank(finalMark)){
                    continue;
                }
                String newLineText = markdownImage.getOriginalLineText().replace(markdownImage.getOriginalMark(), finalMark);

                WriteCommandAction.runWriteCommandAction(data.getProject(), () -> document
                    .replaceString(document.getLineStartOffset(markdownImage.getLineNumber()),
                                   document.getLineEndOffset(markdownImage.getLineNumber()),
                                   newLineText));


            }
        }
        return true;
    }
}
