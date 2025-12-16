package info.dong4j.idea.plugin.chain.handler;

import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.progress.ProgressIndicator;

import info.dong4j.idea.plugin.MikBundle;
import info.dong4j.idea.plugin.console.MikConsoleView;
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
public class WriteToDocumentHandler extends ActionHandlerAdapter {
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

        // 输出写入文档的开始日志
        int totalImages = data.getWaitingProcessMap().values().stream().mapToInt(List::size).sum();
        MikConsoleView.printMessage(data.getProject(), String.format("  [写入文档] 共 %d 张图片需要写入", totalImages));
        
        for (Map.Entry<Document, List<MarkdownImage>> imageEntry : data.getWaitingProcessMap().entrySet()) {
            Document document = imageEntry.getKey();
            int totalCount = imageEntry.getValue().size();
            int currentFileProcessed = 0;
            
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

                currentFileProcessed++;
                // 输出每张图片的写入日志
                MikConsoleView.printMessage(data.getProject(),
                                            String.format("         [%d/%d] 图片: %s | 行号: %d",
                                                          currentFileProcessed, totalCount, imageName, markdownImage.getLineNumber() + 1));
            }
        }

        MikConsoleView.printSuccessMessage(data.getProject(), "  [✓] 所有图片标签已写入文档");
        // 触发 Code Vision 重算，确保最新标签对应的入口刷新
        DaemonCodeAnalyzer.getInstance(data.getProject()).restart();
        return true;
    }
}
