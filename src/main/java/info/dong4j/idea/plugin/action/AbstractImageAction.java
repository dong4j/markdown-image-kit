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
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiUtilBase;

import info.dong4j.idea.plugin.chain.ActionManager;
import info.dong4j.idea.plugin.chain.ImageCompressHandler;
import info.dong4j.idea.plugin.chain.ImageLabelChangeHandler;
import info.dong4j.idea.plugin.chain.ImageLabelInsertHandler;
import info.dong4j.idea.plugin.chain.paste.ImageUploadHandler;
import info.dong4j.idea.plugin.content.ImageContents;
import info.dong4j.idea.plugin.content.MarkdownContents;
import info.dong4j.idea.plugin.entity.EventData;
import info.dong4j.idea.plugin.enums.InsertEnum;
import info.dong4j.idea.plugin.task.ChainBackgroupTask;
import info.dong4j.idea.plugin.util.ImageUtils;

import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.Icon;

import lombok.extern.slf4j.Slf4j;

/**
 * <p>Company: 科大讯飞股份有限公司-四川分公司</p>
 * <p>Description: 图片处理</p>
 *
 * @author dong4j
 * @email sjdong3 @iflytek.com
 * @since 2019 -03-26 15:32
 */
@Slf4j
public abstract class AbstractImageAction extends AnAction {
    /**
     * Gets icon.
     *
     * @return the icon
     */
    abstract protected Icon getIcon();

    @Override
    public void update(@NotNull AnActionEvent event) {
        final Presentation presentation = event.getPresentation();
        final DataContext dataContext = event.getDataContext();
        presentation.setVisible(true);
        presentation.setIcon(getIcon());

        // 未打开 project 时, 不可用
        final Project project = event.getProject();
        if (project == null) {
            presentation.setEnabled(false);
            return;
        }

        // 如果光标选中了编辑器
        final Editor editor = PlatformDataKeys.EDITOR.getData(dataContext);
        if (null != editor) {
            final PsiFile file = PsiUtilBase.getPsiFileInEditor(editor, project);
            // 是 图片 时可用
            presentation.setEnabled(file != null && ImageContents.IMAGE_TYPE_NAME.equals(file.getFileType().getName()));
            return;
        }

        // 没有选中编辑器时, 如果是文件夹
        boolean isValid = false;
        // 如果光标选择了文件树(可多选)
        final VirtualFile[] files = PlatformDataKeys.VIRTUAL_FILE_ARRAY.getData(dataContext);
        // 如果不是空文件夹
        if (null != files) {
            for (VirtualFile file : files) {
                // 只要有一个是文件夹且不是 node_modules 文件夹 , 则可用
                if (file.isDirectory() && !MarkdownContents.NODE_MODULES_FILE.equals(file.getName())) {
                    // 文件夹可用
                    isValid = true;
                    break;
                }
                // 只要其中一个是 markdown 文件, 则可用
                if (ImageContents.IMAGE_TYPE_NAME.equals(file.getFileType().getName())) {
                    isValid = true;
                    break;
                }
            }
        }
        presentation.setEnabled(isValid);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        // 获取光标所选的所有图片文件
        Map<String, File> imageMap = new HashMap<>(32);

        final Project project = event.getProject();
        if (project != null) {

            log.trace("project's base path = {}", project.getBasePath());
            // 如果选中编辑器
            final DataContext dataContext = event.getDataContext();

            final Editor editor = PlatformDataKeys.EDITOR.getData(dataContext);
            if (null != editor) {
                VirtualFile virtualFile = event.getData(PlatformDataKeys.VIRTUAL_FILE);
                assert virtualFile != null;
                transform(imageMap, virtualFile, virtualFile.getName());
            } else {
                // 获取被选中的有文件和目录
                final VirtualFile[] files = PlatformDataKeys.VIRTUAL_FILE_ARRAY.getData(dataContext);
                if (null != files) {
                    for (VirtualFile file : files) {
                        if (ImageContents.IMAGE_TYPE_NAME.equals(file.getFileType().getName())) {
                            transform(imageMap, file, file.getName());

                        }
                        // 如果是目录, 则递归获取所有 image 文件
                        if (file.isDirectory()) {
                            List<VirtualFile> imageFiles = ImageUtils.recursivelyImageFile(file);
                            for (VirtualFile imageFile : imageFiles) {
                                transform(imageMap, imageFile, file.getName());
                            }
                        }
                    }
                }
            }

            if(imageMap.size() > 0){
                EventData data = new EventData()
                    .setProject(project)
                    .setImageMap(imageMap)
                    .setInsertType(InsertEnum.CLIPBOADR);

                ActionManager manager = new ActionManager(data)
                    .addHandler(new ImageCompressHandler())
                    .addHandler(new ImageUploadHandler())
                    .addHandler(new ImageLabelChangeHandler())
                    .addHandler(new ImageLabelInsertHandler());

                new ChainBackgroupTask(project,
                                       "Image Upload Task",
                                       manager).queue();
            }
        }
    }

    /**
     * 将 VirtualFile 转换为 File
     *
     * @param imageMap    the image map
     * @param virtualFile the virtual file
     * @param name        the name
     */
    private void transform(Map<String, File> imageMap, VirtualFile virtualFile, String name) {
        File out = ImageUtils.buildTempFile(virtualFile.getName());
        try {
            FileUtils.copyToFile(virtualFile.getInputStream(), out);
            imageMap.put(name, out);
        } catch (IOException e) {
            log.trace("", e);
        }
    }
}
