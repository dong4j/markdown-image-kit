/*
 * MIT License
 *
 * Copyright (c) 2022 dong4j <dong4j@gmail.com>
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
 */

package info.dong4j.idea.plugin.action.intention;

import com.intellij.codeInsight.intention.PsiElementBaseIntentionAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;

import info.dong4j.idea.plugin.client.OssClient;
import info.dong4j.idea.plugin.entity.MarkdownImage;
import info.dong4j.idea.plugin.enums.CloudEnum;
import info.dong4j.idea.plugin.settings.MikPersistenComponent;
import info.dong4j.idea.plugin.settings.MikState;
import info.dong4j.idea.plugin.settings.OssState;
import info.dong4j.idea.plugin.util.ClientUtils;
import info.dong4j.idea.plugin.util.MarkdownUtils;

import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import lombok.extern.slf4j.Slf4j;

/**
 * <p>Description: alt + enter </p>
 * 使用设置后的默认 OSS 客户端
 *
 * @author dong4j
 * @version 0.0.1
 * @email "mailto:dong4j@gmail.com"
 * @date 2021.02.14 18:40
 * @since 0.0.1
 */
@Slf4j
public abstract class IntentionActionBase extends PsiElementBaseIntentionAction {
    /**
     * The State.
     */
    static final MikState STATE = MikPersistenComponent.getInstance().getState();

    /**
     * Gets message.
     *
     * @param clientName the client name
     * @return the message
     * @since 0.0.1
     */
    abstract String getMessage(String clientName);

    /**
     * 使用设置的默认 client
     *
     * @return the oss client
     * @since 0.0.1
     */
    protected OssClient getClient() {
        return ClientUtils.getClient(this.getCloudType());
    }

    /**
     * 获取设置的默认客户端名称
     *
     * @return the string
     * @since 0.0.1
     */
    protected String getName() {
        return this.getCloudType().title;
    }

    /**
     * 获取设置的默认客户端类型, 如果设置的不可用, 则使用 sm.ms
     *
     * @return the cloud enum
     * @since 0.0.1
     */
    protected CloudEnum getCloudType() {
        CloudEnum cloudEnum = OssState.getCloudType(STATE.getCloudType());
        return OssState.getStatus(cloudEnum) ? cloudEnum : CloudEnum.SM_MS_CLOUD;
    }

    /**
     * Gets text *
     *
     * @return the text
     * @since 0.0.1
     */
    @Nls
    @NotNull
    @Override
    public String getText() {
        return this.getMessage(this.getCloudType().title);
    }

    /**
     * Gets family name *
     *
     * @return the family name
     * @since 0.0.1
     */
    @Nls
    @NotNull
    @Override
    public String getFamilyName() {
        return this.getText();
    }

    /**
     * Is available
     *
     * @param project project
     * @param editor  editor
     * @param element element
     * @return the boolean
     * @since 0.0.1
     */
    @Override
    public boolean isAvailable(@NotNull Project project, Editor editor,
                               @NotNull PsiElement element) {


        if (MarkdownUtils.illegalImageMark(project, this.getLineText(editor))) {
            return false;
        }

        VirtualFile virtualFile = FileDocumentManager.getInstance().getFile(editor.getDocument());
        if (virtualFile == null) {
            return false;
        }

        return MarkdownUtils.isMardownFile(virtualFile);
    }

    /**
     * 获取光标所在行文本
     *
     * @param editor the editor
     * @return the string
     * @since 0.0.1
     */
    private String getLineText(@NotNull Editor editor) {
        int documentLine = editor.getDocument().getLineNumber(editor.getCaretModel().getOffset());
        int linestartoffset = editor.getDocument().getLineStartOffset(documentLine);
        int lineendoffset = editor.getDocument().getLineEndOffset(documentLine);

        log.trace("documentLine = {}, linestartoffset = {}, lineendoffset = {}", documentLine, linestartoffset, lineendoffset);

        String text = editor.getDocument().getText(new TextRange(linestartoffset, lineendoffset));
        log.trace("text = {}", text);
        return text;
    }

    /**
     * Gets markdown image *
     *
     * @param editor editor
     * @return the markdown image
     * @since 0.0.1
     */
    @Nullable
    MarkdownImage getMarkdownImage(Editor editor) {
        Document document = editor.getDocument();
        int documentLine = document.getLineNumber(editor.getCaretModel().getOffset());
        VirtualFile virtualFile = FileDocumentManager.getInstance().getFile(document);

        return MarkdownUtils.analysisImageMark(virtualFile, this.getLineText(editor), documentLine);
    }
}
