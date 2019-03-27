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

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;

import info.dong4j.idea.plugin.client.OssClient;
import info.dong4j.idea.plugin.content.MarkdownContents;
import info.dong4j.idea.plugin.entity.MarkdownImage;
import info.dong4j.idea.plugin.notify.UploadNotification;
import info.dong4j.idea.plugin.strategy.UploadFromAction;
import info.dong4j.idea.plugin.strategy.Uploader;
import info.dong4j.idea.plugin.util.ActionUtils;
import info.dong4j.idea.plugin.util.MarkdownUtils;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.Icon;

import lombok.extern.slf4j.Slf4j;

/**
 * <p>Company: 科大讯飞股份有限公司-四川分公司</p>
 * <p>Description: ${description}</p>
 *
 * @author dong4j
 * @date 2019 -03-14 17:15
 * @email sjdong3 @iflytek.com
 */
@Slf4j
public abstract class UploadActionBase extends AnAction {
    /**
     * Gets icon.
     *
     * @return the icon
     */
    abstract protected Icon getIcon();

    /**
     * action 是否为可用状态
     *
     * @return the boolean
     */
    abstract boolean isAvailable();

    /**
     * 获取 action name
     *
     * @return the name
     */
    abstract String getName();

    /**
     * 检查 "upload to XXX OSS" 按钮是否可用
     * 1. 相关 test 通过后
     * a. 如果全是目录则可用
     * b. 如果文件是 markdown 才可用
     *
     * @param event the event
     */
    @Override
    public void update(@NotNull AnActionEvent event) {
        ActionUtils.isAvailable(event, getIcon(), MarkdownContents.MARKDOWN_TYPE_NAME);
    }

    /**
     * 所有子类都走这个逻辑, 做一些前置判断和解析 markdown image mark
     *
     * @param event the an action event
     */
    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        Map<Document, List<MarkdownImage>> waitingForUploadImages = new HashMap<>(20);

        final Project project = event.getProject();
        if (project != null) {

            // 如果账号未设置或者设置后未测试, 则给出提示
            if (!isAvailable()) {
                log.trace("No account set or setting error. action = {}", getName());
                UploadNotification.notifyConfigurableError(project, getName());
                return;
            }

            log.trace("project's base path = {}", project.getBasePath());
            // 如果选中编辑器
            final DataContext dataContext = event.getDataContext();

            final Editor editor = PlatformDataKeys.EDITOR.getData(dataContext);
            // todo-dong4j : (2019年03月15日 09:41) [如果光标选中了编辑器, upload()已经判断过是否为 markdown 文件, 此处不需再判断]
            if (null != editor) {
                // 解析此文件中所有的图片标签
                Document documentFromEditor = editor.getDocument();
                VirtualFile virtualFile = FileDocumentManager.getInstance().getFile(documentFromEditor);
                waitingForUploadImages.put(documentFromEditor, MarkdownUtils.getImageInfoFromFiles(virtualFile));
            } else {
                // 获取被选中的有文件和目录
                final VirtualFile[] files = PlatformDataKeys.VIRTUAL_FILE_ARRAY.getData(dataContext);
                if (null != files) {
                    // fixme-dong4j : (2019年03月26日 16:52 [逻辑有问题])
                    for (VirtualFile file : files) {
                        if (MarkdownUtils.isMardownFile(file)) {
                            // 解析此文件中所有的图片标签
                            Document documentFromVirtualFile = FileDocumentManager.getInstance().getDocument(file);
                            waitingForUploadImages.put(documentFromVirtualFile, MarkdownUtils.getImageInfoFromFiles(file));
                        }
                        // 如果是目录, 则递归获取所有 markdown 文件
                        if (file.isDirectory()) {
                            List<VirtualFile> markdownFiles = MarkdownUtils.recursivelyMarkdownFile(file);
                            for (VirtualFile virtualFile : markdownFiles) {
                                Document documentFromVirtualFile = FileDocumentManager.getInstance().getDocument(virtualFile);
                                waitingForUploadImages.put(documentFromVirtualFile, MarkdownUtils.getImageInfoFromFiles(virtualFile));
                            }
                        }
                    }
                }
            }
            execute(event, waitingForUploadImages);
        }
    }

    /**
     * 新建后台任务, 调用 upload() 执行上传逻辑
     *
     * @param event                  the event
     * @param waitingForUploadImages the waiting for upload images
     * @return the string   url      上传成功后返回的 url
     */
    @Contract(pure = true)
    private void execute(@NotNull AnActionEvent event, Map<Document, List<MarkdownImage>> waitingForUploadImages) {
        final Project project = event.getProject();
        if (project != null) {
            if (waitingForUploadImages.size() > 0) {
                // 先刷新一次, 避免才添加的文件未被添加的 VFS 中, 导致找不到文件的问题
                VirtualFileManager.getInstance().syncRefresh();
                // 获取对应的 client
                OssClient ossClient = getOssClient();
                Uploader.getInstance()
                    .setUploadWay(new UploadFromAction(project, ossClient, waitingForUploadImages))
                    .upload();
            }
        }
    }

    /**
     * 获取具体上传的客户端, 委托给后台任务执行
     *
     * @return the oss client
     */
    abstract OssClient getOssClient();
}
