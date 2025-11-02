package info.dong4j.idea.plugin.chain.handler;

import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.EditorModificationUtil;

import info.dong4j.idea.plugin.MikBundle;
import info.dong4j.idea.plugin.content.ImageContents;
import info.dong4j.idea.plugin.entity.EventData;
import info.dong4j.idea.plugin.entity.MarkdownImage;
import info.dong4j.idea.plugin.util.StringUtils;

import java.util.Iterator;

/**
 * 文档插入文本行处理器
 * <p>
 * 用于在文档中插入新的文本行，主要处理与文本插入相关的操作，如获取操作名称、执行插入逻辑等。
 * 该处理器基于 WriteCommandAction 实现，确保在安全的写入上下文中执行插入操作。
 * <p>
 * 当光标在图片路径中时，会只替换路径部分，而不是插入完整的图片标签。
 * </p>
 *
 * @author dong4j
 * @version 1.0.0
 * @date 2025.10.24
 * @since 1.0.0
 */
public class WriteToEditorHandler extends ActionHandlerAdapter {
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
     * 如果光标在图片路径中（originalMark 不为空），则只替换路径部分；否则插入完整的图片标签。
     *
     * @param data          事件数据，包含项目和编辑器信息
     * @param imageIterator 图片迭代器，用于遍历图片数据
     * @param markdownImage Markdown图片对象，包含最终的图片标记内容
     */
    @Override
    public void invoke(EventData data, Iterator<MarkdownImage> imageIterator, MarkdownImage markdownImage) {
        String finalMark = markdownImage.getFinalMark();
        if (StringUtils.isBlank(finalMark)) {
            return;
        }

        WriteCommandAction.runWriteCommandAction(data.getProject(), () -> {
            Document document = data.getEditor().getDocument();

            // 如果 originalMark 不为空且路径偏移量有效，说明需要替换路径部分
            if (StringUtils.isNotBlank(markdownImage.getOriginalMark())
                && markdownImage.getLineStartOffset() > 0
                && markdownImage.getLineEndOffset() >= markdownImage.getLineStartOffset()) {

                // 只替换路径部分
                // 从 finalMark 中提取路径（假设格式为 ![title](path) 或 ![](path)）
                String newPath = extractPathFromFinalMark(finalMark);
                if (newPath != null) {
                    document.replaceString(markdownImage.getLineStartOffset(),
                                           markdownImage.getLineEndOffset(),
                                           newPath);
                } else {
                    // 如果无法提取路径，回退到插入完整标签
                    EditorModificationUtil.insertStringAtCaret(data.getEditor(), finalMark + ImageContents.LINE_BREAK);
                }
            } else {
                // 插入完整的图片标签
                EditorModificationUtil.insertStringAtCaret(data.getEditor(), finalMark + ImageContents.LINE_BREAK);
            }
        });
    }

    /**
     * 从 finalMark 中提取路径部分
     * <p>
     * 从 markdown 图片标签中提取路径，例如从 `![title](path)` 或 `![](path)` 中提取 `path`。
     *
     * @param finalMark 完整的 markdown 图片标签
     * @return 路径部分，如果无法提取则返回 null
     * @since 1.0.0
     */
    private String extractPathFromFinalMark(String finalMark) {
        if (StringUtils.isBlank(finalMark)) {
            return null;
        }

        // 查找 "](" 的位置
        int middleIndex = finalMark.indexOf(ImageContents.IMAGE_MARK_MIDDLE);
        if (middleIndex == -1) {
            return null;
        }

        // 查找 ")" 的位置
        int suffixIndex = finalMark.indexOf(ImageContents.IMAGE_MARK_SUFFIX, middleIndex);
        if (suffixIndex == -1) {
            return null;
        }

        // 提取路径部分
        return finalMark.substring(middleIndex + ImageContents.IMAGE_MARK_MIDDLE.length(), suffixIndex);
    }
}
