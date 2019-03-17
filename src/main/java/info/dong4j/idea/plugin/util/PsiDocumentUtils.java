package info.dong4j.idea.plugin.util;

import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDocumentManager;

import info.dong4j.idea.plugin.entity.MarkdownImage;
import info.dong4j.idea.plugin.enums.ImageLocationEnum;
import info.dong4j.idea.plugin.settings.OssPersistenConfig;
import info.dong4j.idea.plugin.settings.OssState;

/**
 * <p>Company: 科大讯飞股份有限公司-四川分公司</p>
 * <p>Description: 操作文档</p>
 *
 * @author dong4j
 * @date 2019 -03-12 22:20
 * @email sjdong3 @iflytek.com
 */
public class PsiDocumentUtils {
    private static OssState state = OssPersistenConfig.getInstance().getState();

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
        // 只处理开启了替换开关且现有的标签格式与配置不一样, 或者未上传(未上传的必须处理)
        boolean isLocal = markdownImage.getLocation().equals(ImageLocationEnum.LOCAL);
        boolean changeHtmlTag = !state.getTagTypeCode().equals(markdownImage.getImageMarkType().code)
                                && state.isChangeToHtmlTag();
        if (isLocal || changeHtmlTag) {
            String newLineText = UploadUtils.getFinalImageMark(markdownImage.getTitle(),
                                                               markdownImage.getUploadedUrl(),
                                                               markdownImage.getPath(),
                                                               "");

            WriteCommandAction.runWriteCommandAction(project, () -> document
                .replaceString(document.getLineStartOffset(markdownImage.getLineNumber()),
                               document.getLineEndOffset(markdownImage.getLineNumber()),
                               newLineText));
        }
    }
}