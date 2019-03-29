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

package info.dong4j.idea.plugin.action.image;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.externalSystem.task.TaskCallbackAdapter;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;

import info.dong4j.idea.plugin.MikBundle;
import info.dong4j.idea.plugin.chain.ActionManager;
import info.dong4j.idea.plugin.chain.BaseActionHandler;
import info.dong4j.idea.plugin.chain.FinalChainHandler;
import info.dong4j.idea.plugin.chain.ImageCompressionHandler;
import info.dong4j.idea.plugin.chain.ImageRenameHandler;
import info.dong4j.idea.plugin.chain.ResolveImageFileHandler;
import info.dong4j.idea.plugin.entity.EventData;
import info.dong4j.idea.plugin.task.ActionTask;

import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.util.Map;

import javax.swing.Icon;

import lombok.extern.slf4j.Slf4j;

/**
 * <p>Company: 科大讯飞股份有限公司-四川分公司</p>
 * <p>Description: 图像压缩 </p>
 *
 * @author dong4j
 * @email sjdong3@iflytek.com
 * @since 2019-03-15 20:43
 */
@Slf4j
public final class ImageCompressAction extends ImageActionBase {

    @Contract(pure = true)
    @Override
    protected Icon getIcon() {
        return AllIcons.Debugger.ShowCurrentFrame;
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        Project project = event.getProject();
        if (project != null) {
            EventData data = new EventData()
                .setActionEvent(event)
                .setProject(event.getProject());

            ActionManager manager = new ActionManager(data)
                // 解析 image 文件
                .addHandler(new ResolveImageFileHandler())
                // 图片压缩
                .addHandler(new ImageCompressionHandler())
                // 图片重命名
                .addHandler(new ImageRenameHandler())
                // 替换
                .addHandler(new BaseActionHandler() {
                    @Override
                    public String getName() {
                        return "替换原图";
                    }

                    @Override
                    public boolean execute(EventData data) {
                        int size = data.getSize();
                        ProgressIndicator indicator = data.getIndicator();
                        indicator.setText2(MikBundle.message("mik.chain.compress.progress"));
                        int totalCount = data.getImageMap().size();
                        int totalProcessed = 0;

                        Map<String, File> imageMap = data.getImageMap();
                        Map<String, VirtualFile> virtualFileMap = data.getVirtualFileMap();

                        for (Map.Entry<String, File> entry : imageMap.entrySet()) {
                            try {
                                FileUtils.copyFile(entry.getValue(), new File(virtualFileMap.get(entry.getKey()).getPath()));
                            } catch (IOException e) {
                                log.trace("", e);
                            }
                            indicator.setFraction(((++totalProcessed * 1.0) + data.getIndex() * size) / totalCount * size);
                        }
                        return true;
                    }
                })
                .addHandler(new FinalChainHandler())
                // 处理完成后刷新 VFS
                .addCallback(new TaskCallbackAdapter(){
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
}
