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
 * ActionUtils 工具类
 * <p>
 * 提供与 IDE 中 Action 相关的辅助方法，主要用于判断 Action 是否可用，根据当前上下文环境设置其显示状态和图标。
 * 支持根据文件类型（如图片或 Markdown）动态启用或禁用 Action。
 *
 * @author dong4j
 * @version 0.0.1
 * @date 2021.02.14
 * @since 0.0.1
 */
public final class ActionUtils {
    /**
     * 判断当前操作是否可用
     * <p>
     * 根据传入的参数和当前上下文信息，判断该操作是否可用。若项目未打开，则不可用；若选中了编辑器中的文件，则根据文件类型判断是否可用；若选中了文件树中的文件或文件夹，则根据文件类型或是否为文件夹判断是否可用。
     *
     * @param isAvailable 是否默认可用
     * @param event       事件对象，用于获取当前上下文信息
     * @param icon        图标，用于设置操作的图标
     * @param type        类型标识，用于判断文件类型是否匹配
     * @since 0.0.1
     */
    @SuppressWarnings("D")
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
