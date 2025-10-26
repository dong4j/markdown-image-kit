package info.dong4j.idea.plugin.chain;

import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.EditorModificationUtil;

import info.dong4j.idea.plugin.MikBundle;
import info.dong4j.idea.plugin.content.ImageContents;
import info.dong4j.idea.plugin.entity.EventData;
import info.dong4j.idea.plugin.entity.MarkdownImage;

import java.util.Iterator;

/**
 * 文档插入文本行处理器
 * <p>
 * 用于在文档中插入新的文本行，主要处理与文本插入相关的操作，如获取操作名称、执行插入逻辑等。
 * 该处理器基于 WriteCommandAction 实现，确保在安全的写入上下文中执行插入操作。
 * </p>
 *
 * @author dong4j
 * @version 1.0.0
 * @date 2025.10.24
 * @since 1.0.0
 */
public class InsertToDocumentHandler extends ActionHandlerAdapter {
    /**
     * 获取文档标题名称
     * <p>
     * 返回与 "mik.action.write.document.title" 关键字关联的国际化消息，通常用于界面显示的文档标题
     *
     * @return 文档标题名称
     * @since 0.0.1
     */
    @Override
    public String getName() {
        return MikBundle.message("mik.action.write.document.title");
    }

    /**
     * 执行插入Markdown图片的操作
     * <p>
     * 该方法用于在指定编辑器中插入Markdown图片内容，通过WriteCommandAction确保操作在写入线程中执行。
     *
     * @param data          事件数据，包含项目和编辑器信息
     * @param imageIterator 图片迭代器，用于遍历图片数据
     * @param markdownImage Markdown图片对象，包含最终的图片标记内容
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
