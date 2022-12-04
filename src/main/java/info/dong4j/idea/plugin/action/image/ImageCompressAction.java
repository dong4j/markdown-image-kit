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

package info.dong4j.idea.plugin.action.image;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.externalSystem.task.TaskCallbackAdapter;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.VirtualFileManager;

import info.dong4j.idea.plugin.MikBundle;
import info.dong4j.idea.plugin.chain.ActionHandlerAdapter;
import info.dong4j.idea.plugin.chain.ActionManager;
import info.dong4j.idea.plugin.chain.FinalChainHandler;
import info.dong4j.idea.plugin.chain.ImageCompressionHandler;
import info.dong4j.idea.plugin.chain.ImageRenameHandler;
import info.dong4j.idea.plugin.entity.EventData;
import info.dong4j.idea.plugin.entity.MarkdownImage;
import info.dong4j.idea.plugin.task.ActionTask;

import org.jetbrains.annotations.Contract;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.Icon;

import lombok.extern.slf4j.Slf4j;

/**
 * <p>Description: 图像压缩 </p>
 *
 * @author dong4j
 * @version 0.0.1
 * @email "mailto:dong4j@gmail.com"
 * @date 2021.02.14 18:40
 * @since 0.0.1
 */
@Slf4j
public final class ImageCompressAction extends ImageActionBase {

    /**
     * Gets icon *
     *
     * @return the icon
     * @since 0.0.1
     */
    @Contract(pure = true)
    @Override
    protected Icon getIcon() {
        return AllIcons.Debugger.ShowCurrentFrame;
    }

    /**
     * Build chain
     *
     * @param event             event
     * @param waitingProcessMap waiting process map
     * @since 0.0.1
     */
    @Override
    protected void buildChain(AnActionEvent event, Map<Document, List<MarkdownImage>> waitingProcessMap) {
        EventData data = new EventData()
            .setActionEvent(event)
            .setProject(event.getProject())
            .setWaitingProcessMap(waitingProcessMap);

        ActionManager manager = new ActionManager(data)
            // 图片压缩
            .addHandler(new ImageCompressionHandler())
            // 图片重命名
            .addHandler(new ImageRenameHandler())
            // 替换
            .addHandler(new ActionHandlerAdapter() {
                @Override
                public String getName() {
                    return "替换原图";
                }

                @Override
                public void invoke(EventData data, Iterator<MarkdownImage> imageIterator, MarkdownImage markdownImage) {
                    InputStream inputStream = markdownImage.getInputStream();
                    try {
                        FileUtil.copy(inputStream, new FileOutputStream(new File(markdownImage.getPath())));
                    } catch (IOException e) {
                        log.trace("", e);
                    }
                }
            })
            .addHandler(new FinalChainHandler())
            // 处理完成后刷新 VFS
            .addCallback(new TaskCallbackAdapter() {
                @Override
                public void onSuccess() {
                    log.trace("Success callback");
                    // 刷新 VFS, 避免新增的图片很久才显示出来
                    ApplicationManager.getApplication().runWriteAction(() -> {
                        VirtualFileManager.getInstance().syncRefresh();
                    });
                }
            });

        // 开启后台任务
        new ActionTask(event.getProject(), MikBundle.message("mik.action.compress.progress"), manager).queue();
    }
}
