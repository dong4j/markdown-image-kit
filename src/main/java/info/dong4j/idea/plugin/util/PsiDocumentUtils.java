package info.dong4j.idea.plugin.util;

import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorModificationUtil;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDocumentManager;

import info.dong4j.idea.plugin.entity.MarkdownImage;
import info.dong4j.idea.plugin.enums.ImageLocationEnum;
import info.dong4j.idea.plugin.enums.ImageMarkEnum;
import info.dong4j.idea.plugin.settings.MikPersistenComponent;
import info.dong4j.idea.plugin.settings.MikState;

/**
 * 操作文档工具类
 * <p>
 * 提供与文档操作相关的实用方法，包括全文替换、特定字符串替换以及在光标位置插入字符串等功能。该类主要用于在 IDE 中对文档内容进行修改和保存操作。
 *
 * @author dong4j
 * @version 0.0.1
 * @date 2019.03.12
 * @since 0.0.1
 */
public final class PsiDocumentUtils {
    /** 系统状态信息，用于表示当前组件的运行状态 */
    private static final MikState state = MikPersistenComponent.getInstance().getState();

    /**
     * 对指定文档执行全文替换操作，包括更新文档内容并保存
     * <p>
     * 该方法会检查文档是否为空，若不为空则使用 PsiDocumentManager 执行文档内容的更新和提交操作，并最终保存文档
     *
     * @param project  项目对象，用于获取 PsiDocumentManager 实例
     * @param document 需要修改的文档对象
     * @param string   要替换到文档中的新内容字符串
     */
    public static void commitAndSaveDocument(Project project, Document document, String string) {
        if (document != null) {
            PsiDocumentManager psiDocumentManager = PsiDocumentManager.getInstance(project);
            WriteCommandAction.runWriteCommandAction(project, () -> {
                document.setText(string);
                psiDocumentManager.doPostponedOperationsAndUnblockDocument(document);
                psiDocumentManager.commitDocument(document);
                FileDocumentManager.getInstance().saveDocument(document);
            });
        }
    }

    /**
     * 执行文档的提交与保存操作，仅在特定条件下替换字符串内容
     * <p>
     * 该方法会根据是否为本地图片以及是否需要转换为HTML标签，决定是否替换文档中的字符串。
     * 如果满足任一条件，则使用指定的图片标题和路径生成新的字符串，并执行替换操作。
     *
     * @param project       项目对象，用于操作项目相关的资源
     * @param document      文档对象，用于操作文档内容
     * @param markdownImage Markdown图片对象，包含图片相关信息
     * @since 0.0.1
     */
    public static void commitAndSaveDocument(Project project,
                                             Document document,
                                             MarkdownImage markdownImage) {
        // 只处理开启了替换开关且现有的标签格式与配置不一样, 或者未上传(未上传的必须处理)
        boolean isLocal = markdownImage.getLocation().equals(ImageLocationEnum.LOCAL);

        // 获取配置中的标签代码
        ImageMarkEnum tagEnum = state.getImageMarkEnum();
        String configTagCode = tagEnum == ImageMarkEnum.CUSTOM
                               ? state.getCustomTagCode()
                               : (tagEnum != null ? tagEnum.getCode() : "");

        boolean changeHtmlTag = !configTagCode.equals(markdownImage.getImageMarkType().code)
                                && state.isChangeToHtmlTag();
        if (isLocal || changeHtmlTag) {
            String newLineText = UploadUtils.getFinalImageMark(markdownImage.getTitle(),
                                                               markdownImage.getPath(),
                                                               markdownImage.getPath(),
                                                               "");

            WriteCommandAction.runWriteCommandAction(project, () -> document
                .replaceString(document.getLineStartOffset(markdownImage.getLineNumber()),
                               document.getLineEndOffset(markdownImage.getLineNumber()),
                               newLineText));
        }
    }

    /**
     * 将指定字符串插入到编辑器的光标位置
     * <p>
     * 该方法通过执行插入操作，将传入的字符串插入到指定编辑器的光标位置。
     *
     * @param marks  要插入的字符串内容
     * @param editor 目标编辑器对象
     */
    public static void insertDocument(String marks, Editor editor){
        Runnable r = () -> EditorModificationUtil.insertStringAtCaret(editor, marks);
        WriteCommandAction.runWriteCommandAction(editor.getProject(), r);
    }
}
