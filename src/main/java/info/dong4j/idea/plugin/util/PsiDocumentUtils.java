package info.dong4j.idea.plugin.util;

import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDocumentManager;

import info.dong4j.idea.plugin.entity.MarkdownImage;

/**
 * <p>Company: 科大讯飞股份有限公司-四川分公司</p>
 * <p>Description: 操作文档</p>
 *
 * @author dong4j
 * @date 2019 -03-12 22:20
 * @email sjdong3 @iflytek.com
 */
public class PsiDocumentUtils {
    /**
     * Commit and save document.
     *
     * @param project  the project
     * @param document the document
     * @param string   the string
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
     * Commit and save document.
     *
     * @param project       the project
     * @param document      the document
     * @param markdownImage the markdown image
     */
    public static void commitAndSaveDocument(Project project,
                                             Document document,
                                             MarkdownImage markdownImage) {
        String newLineText = UploadUtils.getFinalImageMark(markdownImage.getTitle(),
                                                           markdownImage.getUploadedUrl(),
                                                           markdownImage.getPath());
        WriteCommandAction.runWriteCommandAction(project, () -> document
            .replaceString(document.getLineStartOffset(markdownImage.getLineNumber()),
                           document.getLineEndOffset(markdownImage.getLineNumber()),
                           newLineText));
    }
}