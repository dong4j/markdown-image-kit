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

package info.dong4j.idea.plugin.action;

import com.intellij.codeInsight.intention.PsiElementBaseIntentionAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.util.IncorrectOperationException;

import info.dong4j.idea.plugin.MikBundle;
import info.dong4j.idea.plugin.chain.ActionManager;
import info.dong4j.idea.plugin.chain.ImageCompressHandler;
import info.dong4j.idea.plugin.chain.ImageLabelChangeHandler;
import info.dong4j.idea.plugin.chain.ImageLabelInsertHandler;
import info.dong4j.idea.plugin.chain.paste.ImageUploadHandler;
import info.dong4j.idea.plugin.client.OssClient;
import info.dong4j.idea.plugin.content.ImageContents;
import info.dong4j.idea.plugin.entity.EventData;
import info.dong4j.idea.plugin.entity.MarkdownImage;
import info.dong4j.idea.plugin.enums.CloudEnum;
import info.dong4j.idea.plugin.enums.ImageLocationEnum;
import info.dong4j.idea.plugin.enums.InsertEnum;
import info.dong4j.idea.plugin.settings.ImageManagerPersistenComponent;
import info.dong4j.idea.plugin.settings.ImageManagerState;
import info.dong4j.idea.plugin.settings.OssState;
import info.dong4j.idea.plugin.task.ChainBackgroupTask;
import info.dong4j.idea.plugin.util.ClientUtils;
import info.dong4j.idea.plugin.util.MarkdownUtils;
import info.dong4j.idea.plugin.util.UploadUtils;

import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import lombok.extern.slf4j.Slf4j;

/**
 * <p>Company: 科大讯飞股份有限公司-四川分公司</p>
 * <p>Description: alt + enter 处理单个标签 </p>
 *
 * @author dong4j
 * @email sjdong3 @iflytek.com
 * @see com.intellij.testIntegration.createTest.CreateTestAction
 * @since 2019 -03-27 09:34
 */
@Slf4j
public final class ImageIntentionAction extends PsiElementBaseIntentionAction {
    private ImageManagerState state = ImageManagerPersistenComponent.getInstance().getState();
    private MarkdownImage matchImageMark;

    @Override
    public void invoke(@NotNull Project project,
                       Editor editor,
                       @NotNull PsiElement element) throws IncorrectOperationException {

        // 是本地的图片直接上传替换
        if (ImageLocationEnum.LOCAL == matchImageMark.getLocation()) {
            String imageName = matchImageMark.getPath();
            VirtualFile virtualFile = UploadUtils.searchVirtualFileByName(project, imageName);

            if (virtualFile == null) {
                return;
            }

            if (!ImageContents.IMAGE_TYPE_NAME.equals(virtualFile.getFileType().getName())) {
                return;
            }

            File file = new File(virtualFile.getPath());
            Map<String, File> imageMap = new HashMap<String, File>(1) {
                {
                    put(virtualFile.getName(), file);
                }
            };

            EventData data = new EventData()
                .setProject(project)
                .setEditor(editor)
                .setImageMap(imageMap)
                .setMarkdownImage(matchImageMark)
                .setInsertType(InsertEnum.INTENTION);

            ActionManager manager = new ActionManager(data)
                .addHandler(new ImageCompressHandler())
                .addHandler(new ImageUploadHandler())
                .addHandler(new ImageLabelChangeHandler())
                .addHandler(new ImageLabelInsertHandler());

            new ChainBackgroupTask(editor.getProject(), "Intention Task", manager).queue();
        } else {
            // todo-dong4j : (2019年03月27日 10:43) [是网络图片则判断是否添加标签或者图床迁移]
        }
    }


    @Override
    public boolean isAvailable(@NotNull Project project, Editor editor,
                               @NotNull PsiElement element) {

        VirtualFile virtualFile = FileDocumentManager.getInstance().getFile(editor.getDocument());
        if (virtualFile == null) {
            return false;
        }

        if (!MarkdownUtils.isMardownFile(virtualFile)) {
            return false;
        }

        String fileName = Objects.requireNonNull(PsiDocumentManager.getInstance(project).getPsiFile(editor.getDocument())).getName();

        int documentLine = editor.getDocument().getLineNumber(editor.getCaretModel().getOffset());
        int linestartoffset = editor.getDocument().getLineStartOffset(documentLine);
        int lineendoffset = editor.getDocument().getLineEndOffset(documentLine);

        log.trace("documentLine = {}, linestartoffset = {}, lineendoffset = {}", documentLine, linestartoffset, lineendoffset);

        String text = editor.getDocument().getText(new TextRange(linestartoffset, lineendoffset));
        log.trace("text = {}", text);

        matchImageMark = MarkdownUtils.matchImageMark(fileName, text, documentLine);

        return matchImageMark != null;
    }

    @Nls
    @NotNull
    @Override
    public String getText() {
        String clientName;
        CloudEnum cloudEnum = OssState.getCloudType(state.getCloudType());
        OssClient client = ClientUtils.getInstance(cloudEnum);
        if (client != null) {
            clientName = client.getName();
        } else {
            clientName = "OSS";
        }
        return MikBundle.message("mik.intention.upload.message", clientName);
    }

    @Nls
    @NotNull
    @Override
    public String getFamilyName() {
        return getText();
    }
}
