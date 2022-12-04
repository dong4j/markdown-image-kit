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

package info.dong4j.idea.plugin.action.markdown;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;

import info.dong4j.idea.plugin.MikBundle;
import info.dong4j.idea.plugin.chain.ActionHandlerAdapter;
import info.dong4j.idea.plugin.chain.ActionManager;
import info.dong4j.idea.plugin.chain.FinalChainHandler;
import info.dong4j.idea.plugin.chain.ImageLabelChangeHandler;
import info.dong4j.idea.plugin.chain.ReplaceToDocument;
import info.dong4j.idea.plugin.chain.ResolveMarkdownFileHandler;
import info.dong4j.idea.plugin.content.MarkdownContents;
import info.dong4j.idea.plugin.entity.EventData;
import info.dong4j.idea.plugin.entity.MarkdownImage;
import info.dong4j.idea.plugin.enums.ImageLocationEnum;
import info.dong4j.idea.plugin.enums.ImageMarkEnum;
import info.dong4j.idea.plugin.task.ActionTask;
import info.dong4j.idea.plugin.util.ActionUtils;
import info.dong4j.idea.plugin.util.ParserUtils;

import org.jetbrains.annotations.NotNull;

import java.util.Iterator;

import lombok.extern.slf4j.Slf4j;

/**
 * <p>Description: 全局替换标签 </p>
 *
 * @author dong4j
 * @version 0.0.1
 * @email "mailto:dong4j@gmail.com"
 * @date 2021.02.14 18:40
 * @since 0.0.1
 */
@Slf4j
public final class ChangeLabelAction extends AnAction {
    /**
     * Update
     *
     * @param event event
     * @since 0.0.1
     */
    @Override
    public void update(@NotNull AnActionEvent event) {
        ActionUtils.isAvailable(true, event, AllIcons.Actions.ListChanges, MarkdownContents.MARKDOWN_TYPE_NAME);
    }

    /**
     * Action performed
     *
     * @param event event
     * @since 0.0.1
     */
    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {

        Project project = event.getProject();
        if (project != null) {

            EventData data = new EventData()
                .setActionEvent(event)
                .setProject(project);

            ActionManager actionManager = new ActionManager(data)
                // 解析 markdown 文件
                .addHandler(new ResolveMarkdownFileHandler())
                // 全部标签转换
                .addHandler(new ActionHandlerAdapter() {
                    @Override
                    public String getName() {
                        return MikBundle.message("mik.action.replace.label");
                    }

                    @Override
                    public void invoke(EventData data, Iterator<MarkdownImage> imageIterator, MarkdownImage markdownImage) {
                        // 如果是本地类型, 则不替换
                        if (markdownImage.getLocation().equals(ImageLocationEnum.LOCAL)) {
                            return;
                        }

                        // 如果没有勾选 标签替换开关, 则全部替换为原始标签
                        if (!STATE.isChangeToHtmlTag()) {
                            markdownImage.setFinalMark(ParserUtils.parse2(ImageMarkEnum.ORIGINAL.code,
                                                                          markdownImage.getTitle(),
                                                                          markdownImage.getPath()));
                            return;
                        }

                        ImageMarkEnum currentMarkType = markdownImage.getImageMarkType();
                        if (!STATE.getTagType().equals(currentMarkType.text)) {
                            ImageLabelChangeHandler.change(markdownImage);
                        }
                    }
                })
                // 写入标签
                .addHandler(new ReplaceToDocument())
                .addHandler(new FinalChainHandler());

            new ActionTask(project,
                           MikBundle.message("mik.action.change.process"),
                           actionManager).queue();
        }
    }
}
