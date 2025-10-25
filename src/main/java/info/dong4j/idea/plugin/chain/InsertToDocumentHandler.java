package info.dong4j.idea.plugin.chain;

import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.EditorModificationUtil;

import info.dong4j.idea.plugin.MikBundle;
import info.dong4j.idea.plugin.content.ImageContents;
import info.dong4j.idea.plugin.entity.EventData;
import info.dong4j.idea.plugin.entity.MarkdownImage;

import java.util.Iterator;

/**
 * <p>Description: 插入新的文本行</p>
 *
 * @author dong4j
 * @version 0.0.1
 * @email "mailto:dong4j@gmail.com"
 * @date 2021.02.14 18:40
 * @since 0.0.1
 */
public class InsertToDocumentHandler extends ActionHandlerAdapter {

    /**
     * Gets name *
     *
     * @return the name
     * @since 0.0.1
     */
    @Override
    public String getName() {
        return MikBundle.message("mik.action.write.document.title");
    }

    /**
     * Invoke
     *
     * @param data          data
     * @param imageIterator image iterator
     * @param markdownImage markdown image
     * @since 0.0.1
     */
    @Override
    public void invoke(EventData data, Iterator<MarkdownImage> imageIterator, MarkdownImage markdownImage) {
        WriteCommandAction.runWriteCommandAction(data.getProject(),
                                                 () -> EditorModificationUtil
                                                     .insertStringAtCaret(
                                                         data.getEditor(),
                                                         markdownImage.getFinalMark() + ImageContents.LINE_BREAK));
    }
}
