/*
 * MIT License
 *
 * Copyright (c) 2021 dong4j <dong4j@gmail.com>
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

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.util.IncorrectOperationException;

import info.dong4j.idea.plugin.MikBundle;
import info.dong4j.idea.plugin.chain.ActionManager;
import info.dong4j.idea.plugin.chain.FinalChainHandler;
import info.dong4j.idea.plugin.chain.ImageLabelChangeHandler;
import info.dong4j.idea.plugin.chain.ReplaceToDocument;
import info.dong4j.idea.plugin.entity.EventData;
import info.dong4j.idea.plugin.entity.MarkdownImage;
import info.dong4j.idea.plugin.enums.ImageLocationEnum;
import info.dong4j.idea.plugin.enums.ImageMarkEnum;
import info.dong4j.idea.plugin.task.ActionTask;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>Company: no company</p>
 * <p>Description: 替换标签意图</p>
 *
 * @author dong4j
 * @version 0.0.1
 * @email "mailto:dong4j@gmail.com"
 * @date 2021.02.14 18:40
 * @since 0.0.1
 */
public class ImageLabelChangeIntetionAction extends IntentionActionBase {
    /**
     * Gets message *
     *
     * @param clientName client name
     * @return the message
     * @since 0.0.1
     */
    @NotNull
    @Override
    String getMessage(String clientName) {
        return MikBundle.message("mik.intention.change.message");
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

        return super.isAvailable(project, editor, element) && STATE.isChangeToHtmlTag();
    }

    /**
     * Invoke
     *
     * @param project project
     * @param editor  editor
     * @param element element
     * @throws IncorrectOperationException incorrect operation exception
     * @since 0.0.1
     */
    @Override
    public void invoke(@NotNull Project project, Editor editor, @NotNull PsiElement element)
        throws IncorrectOperationException {
        MarkdownImage markdownImage = this.getMarkdownImage(editor);
        if (markdownImage != null) {
            if (markdownImage.getLocation().name().equals(ImageLocationEnum.LOCAL.name())) {
                return;
            }

            Map<Document, List<MarkdownImage>> waitingForMoveMap = new HashMap<Document, List<MarkdownImage>>(1) {
                private static final long serialVersionUID = 2431958015276934209L;

                {
                    this.put(editor.getDocument(), new ArrayList<MarkdownImage>(1) {
                        private static final long serialVersionUID = -9013015357454667709L;

                        {
                            // 手动设置为原始类型, 后面才能替换
                            markdownImage.setImageMarkType(ImageMarkEnum.ORIGINAL);
                            this.add(markdownImage);
                        }
                    });
                }
            };

            EventData data = new EventData()
                .setProject(project)
                .setWaitingProcessMap(waitingForMoveMap);

            ActionManager actionManager = new ActionManager(data)
                .addHandler(new ImageLabelChangeHandler())
                // 写入标签
                .addHandler(new ReplaceToDocument())
                .addHandler(new FinalChainHandler());

            // 开启后台任务
            new ActionTask(project, MikBundle.message("mik.action.move.process", this.getName()),
                           actionManager).queue();
        }
    }
}
