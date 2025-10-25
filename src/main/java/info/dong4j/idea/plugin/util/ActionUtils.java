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
 * <p>Description: </p>
 *
 * @author dong4j
 * @version 0.0.1
 * @email "mailto:dong4j@gmail.com"
 * @date 2021.02.14 18:40
 * @since 0.0.1
 */
public final class ActionUtils {

    /**
     * Is available.
     *
     * @param event the event
     * @param icon  the icon
     * @param type  the type
     * @since 0.0.1
     */
    public static void isAvailable(boolean isAvailable,
                                   @NotNull AnActionEvent event,
                                   Icon icon,
                                   String type) {
        Presentation presentation = event.getPresentation();
        DataContext dataContext = event.getDataContext();
        presentation.setVisible(true);
        presentation.setIcon(icon);

        // 未打开 project 时, 不可用
        Project project = event.getProject();
        if (project == null) {
            presentation.setEnabled(false);
            return;
        }

        // 如果光标选中了编辑器
        Editor editor = PlatformDataKeys.EDITOR.getData(dataContext);
        if (null != editor) {
            PsiFile file = PsiUtilBase.getPsiFileInEditor(editor, project);

            boolean isImageType = type.equals(ImageContents.IMAGE_TYPE_NAME)
                                  ? ImageUtils.isValidForFile(file)
                                  : MarkdownUtils.isValidForFile(file);

            presentation.setEnabled(isImageType);
            return;
        }

        // 没有选中编辑器时, 如果是文件夹
        boolean isValid = false;
        // 如果光标选择了文件树(可多选)
        VirtualFile[] files = PlatformDataKeys.VIRTUAL_FILE_ARRAY.getData(dataContext);
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
                boolean isImageType = type.equals(ImageContents.IMAGE_TYPE_NAME)
                                      ? ImageUtils.isImageFile(file)
                                      : MarkdownUtils.isMardownFile(file);
                if (isImageType) {
                    isValid = true;
                    break;
                }
            }
        }
        presentation.setEnabled(isAvailable && isValid);
    }
}
