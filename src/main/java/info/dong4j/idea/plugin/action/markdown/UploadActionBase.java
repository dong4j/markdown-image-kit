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

package info.dong4j.idea.plugin.action.markdown;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFileManager;

import info.dong4j.idea.plugin.MikBundle;
import info.dong4j.idea.plugin.chain.ActionManager;
import info.dong4j.idea.plugin.client.OssClient;
import info.dong4j.idea.plugin.content.MarkdownContents;
import info.dong4j.idea.plugin.entity.EventData;
import info.dong4j.idea.plugin.task.ActionTask;
import info.dong4j.idea.plugin.util.ActionUtils;

import org.jetbrains.annotations.NotNull;

import javax.swing.Icon;

import lombok.extern.slf4j.Slf4j;

/**
 * <p>Company: no company</p>
 * <p>Description: 右键上传到 OSS </p>
 *
 * @author dong4j
 * @version 0.0.1
 * @email "mailto:dong4j@gmail.com"
 * @date 2019.03.14 17:15
 * @since 0.0.1
 */
@Slf4j
public abstract class UploadActionBase extends AnAction {
    /**
     * Gets icon.
     *
     * @return the icon
     * @since 0.0.1
     */
    abstract protected Icon getIcon();

    /**
     * 当前图床是否完成
     *
     * @return the boolean
     * @since 0.0.1
     */
    boolean available() {
        return true;
    }

    /**
     * 获取 action name
     *
     * @return the name
     * @since 0.0.1
     */
    abstract String getName();

    /**
     * 检查 "upload to XXX OSS" 按钮是否可用
     * 1. 相关 test 通过后
     * a. 如果全是目录则可用
     * b. 如果文件是 markdown 才可用
     *
     * @param event the event
     * @since 0.0.1
     */
    @Override
    public void update(@NotNull AnActionEvent event) {
        ActionUtils.isAvailable(this.available(), event, this.getIcon(), MarkdownContents.MARKDOWN_TYPE_NAME);
    }

    /**
     * 所有子类都走这个逻辑, 做一些前置判断和解析 markdown image mark
     *
     * @param event the an action event
     * @since 0.0.1
     */
    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        // 先刷新一次, 避免才添加的文件未被添加的 VFS 中, 导致找不到文件的问题
        VirtualFileManager.getInstance().syncRefresh();

        Project project = event.getProject();
        if (project != null) {
            EventData data = new EventData()
                .setActionEvent(event)
                .setProject(project)
                // 使用子类的具体 client
                .setClient(this.getClient())
                .setClientName(this.getName());

            // 开启后台任务
            new ActionTask(project, MikBundle.message("mik.action.upload.process", this.getName()), ActionManager.buildUploadChain(data)).queue();
        }
    }

    /**
     * 获取具体上传的客户端, 委托给后台任务执行
     *
     * @return the oss client
     * @since 0.0.1
     */
    abstract OssClient getClient();
}
