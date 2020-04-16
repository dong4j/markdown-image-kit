/*
 * MIT License
 *
 * Copyright (c) 2020 dong4j <dong4j@gmail.com>
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

package info.dong4j.idea.plugin.util;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiUtilBase;

import info.dong4j.idea.plugin.content.ImageContents;
import info.dong4j.idea.plugin.content.MikContents;

import org.jetbrains.annotations.NotNull;

import javax.swing.Icon;

/**
 * <p>Company: no company</p>
 * <p>Description: </p>
 *
 * @author dong4j
 * @email dong4j@gmail.com
 * @since 2019-03-27 12:34
 */
public final class ActionUtils {

    /**
     * Is available.
     *
     * @param event the event
     * @param icon  the icon
     * @param type  the type
     */
    public static void isAvailable(@NotNull AnActionEvent event, Icon icon, String type) {
        final Presentation presentation = event.getPresentation();
        final DataContext dataContext = event.getDataContext();
        presentation.setVisible(true);
        presentation.setIcon(icon);

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

            boolean isImageType = type.equals(ImageContents.IMAGE_TYPE_NAME) ? ImageUtils.isValidForFile(file) : MarkdownUtils.isValidForFile(file);
            presentation.setEnabled(isImageType);
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
                if (file.isDirectory() && !MikContents.NODE_MODULES_FILE.equals(file.getName())) {
                    // 文件夹可用
                    isValid = true;
                    break;
                }
                // 只要其中一个是 markdown 文件, 则可用
                boolean isImageType = type.equals(ImageContents.IMAGE_TYPE_NAME) ? ImageUtils.isImageFile(file) : MarkdownUtils.isMardownFile(file);
                if (isImageType) {
                    isValid = true;
                    break;
                }
            }
        }
        presentation.setEnabled(isValid);
    }
}
