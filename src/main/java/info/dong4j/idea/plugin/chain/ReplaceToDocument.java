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
 * <p>Description: 替换原有标签</p>
 *
 * @author dong4j
 * @version 0.0.1
 * @email "mailto:dong4j@gmail.com"
 * @date 2021.02.14 18:40
 * @since 0.0.1
 */
public class ReplaceToDocument extends ActionHandlerAdapter {
    /**
     * Gets name *
     *
     * @return the name
     * @since 0.0.1
     */
    @Override
    public String getName() {
        return MikBundle.message("mik.action.replace.old.title");
    }

    /**
     * Execute
     *
     * @param data data
     * @return the boolean
     * @since 0.0.1
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
