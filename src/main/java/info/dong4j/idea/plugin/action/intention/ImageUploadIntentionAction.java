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

package info.dong4j.idea.plugin.action.intention;

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.util.IncorrectOperationException;

import info.dong4j.idea.plugin.MikBundle;
import info.dong4j.idea.plugin.chain.ActionManager;
import info.dong4j.idea.plugin.chain.ImageCompressionHandler;
import info.dong4j.idea.plugin.chain.ImageLabelChangeHandler;
import info.dong4j.idea.plugin.chain.ImageLabelJoinHandler;
import info.dong4j.idea.plugin.chain.ImageRenameHandler;
import info.dong4j.idea.plugin.chain.ImageUploadHandler;
import info.dong4j.idea.plugin.chain.InsertToClipboardHandler;
import info.dong4j.idea.plugin.chain.OptionClientHandler;
import info.dong4j.idea.plugin.chain.ResolveImageFileHandler;
import info.dong4j.idea.plugin.entity.EventData;
import info.dong4j.idea.plugin.entity.MarkdownImage;
import info.dong4j.idea.plugin.enums.ImageLocationEnum;
import info.dong4j.idea.plugin.task.ActionTask;
import info.dong4j.idea.plugin.util.MarkdownUtils;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;

/**
 * <p>Company: 科大讯飞股份有限公司-四川分公司</p>
 * <p>Description: alt + enter 处理单个标签上传 </p>
 *
 * @author dong4j
 * @email sjdong3 @iflytek.com
 * @see com.intellij.testIntegration.createTest.CreateTestAction
 * @since 2019 -03-27 09:34
 */
@Slf4j
public final class ImageUploadIntentionAction extends IntentionActionBase {

    @NotNull
    @Override
    String getMessage(String clientName) {
        return MikBundle.message("mik.intention.upload.message", clientName);
    }

    @Override
    public void invoke(@NotNull Project project,
                       Editor editor,
                       @NotNull PsiElement element) throws IncorrectOperationException {

        Document document = editor.getDocument();
        int documentLine = document.getLineNumber(editor.getCaretModel().getOffset());
        VirtualFile virtualFile = FileDocumentManager.getInstance().getFile(document);
        MarkdownImage matchImageMark = MarkdownUtils.matchImageMark(virtualFile, getLineText(editor), documentLine);
        if(matchImageMark == null){
            return;
        }

        if(ImageLocationEnum.LOCAL != matchImageMark.getLocation()){
            return;
        }

        Map<Document, List<MarkdownImage>> waitingForMoveMap = new HashMap<Document, List<MarkdownImage>>(1) {
            {
                put(editor.getDocument(), new ArrayList<MarkdownImage>(1) {
                    {
                        add(matchImageMark);
                    }
                });
            }
        };

        EventData data = new EventData()
            .setProject(project)
            .setClientName(getName())
            .setClient(getClient())
            .setWaitingProcessMap(waitingForMoveMap);

        ActionManager manager = new ActionManager(data)
            // 解析 image 文件
            .addHandler(new ResolveImageFileHandler())
            // 处理 client
            .addHandler(new OptionClientHandler())
            // 图片压缩
            .addHandler(new ImageCompressionHandler())
            // 图片重命名
            .addHandler(new ImageRenameHandler())
            // 图片上传
            .addHandler(new ImageUploadHandler())
            // 拼接标签
            .addHandler(new ImageLabelJoinHandler())
            // 标签转换
            .addHandler(new ImageLabelChangeHandler())
            // 写到 clipboard
            .addHandler(new InsertToClipboardHandler());

        // 开启后台任务
        new ActionTask(project, MikBundle.message("mik.action.upload.process", getName()), manager).queue();
    }
}
