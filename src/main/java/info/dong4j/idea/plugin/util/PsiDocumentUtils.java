/*
 * MIT License
 *
 * Copyright (c) 2019 dong4j <dong4j@gmail.com>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */

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
import info.dong4j.idea.plugin.settings.ImageManagerPersistenComponent;
import info.dong4j.idea.plugin.settings.ImageManagerState;

/**
 * <p>Company: 科大讯飞股份有限公司-四川分公司</p>
 * <p>Description: 操作文档</p>
 *
 * @author dong4j
 * @date 2019 -03-12 22:20
 * @email sjdong3 @iflytek.com
 */
public final class PsiDocumentUtils {
    private static ImageManagerState state = ImageManagerPersistenComponent.getInstance().getState();

    /**
     * 全文替换
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
     * 只替换特定字符串
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

    /**
     * 将字符串插入到光标位置
     *
     * @param marks  the marks
     * @param editor the editor
     */
    public static void insertDocument(String marks, Editor editor){
        Runnable r = () -> EditorModificationUtil.insertStringAtCaret(editor, marks);
        WriteCommandAction.runWriteCommandAction(editor.getProject(), r);
    }
}