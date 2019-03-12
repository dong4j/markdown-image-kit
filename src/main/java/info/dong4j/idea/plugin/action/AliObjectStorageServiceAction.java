package info.dong4j.idea.plugin.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiUtilBase;

import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

import lombok.extern.slf4j.Slf4j;

/**
 * <p>Company: 科大讯飞股份有限公司-四川分公司</p>
 * <p>Description: 上传到阿里 OSS 事件</p>
 *
 * @author dong4j
 * @email sjdong3 @iflytek.com
 * @since 2019 -03-12 17:20
 */
@Slf4j
public final class AliObjectStorageServiceAction extends AnAction {
    private static final String MARKDOWN_FILE_TYPE = ".md";
    private static final String NODE_MODULES_FILE = "node_modules";

    /**
     * 更新 "upload to Aliyun OSS" 按钮是否可用
     *
     * @param event the event
     */
    @Override
    public void update(AnActionEvent event) {
        final Presentation presentation = event.getPresentation();
        final DataContext dataContext = event.getDataContext();

        // 未打开 project 时, 不可用
        final Project project = event.getProject();
        if (project == null) {
            presentation.setEnabled(false);
            return;
        }

        // 如果光标没有选中编辑器
        final Editor editor = PlatformDataKeys.EDITOR.getData(dataContext);
        if (null != editor) {
            final PsiFile file = PsiUtilBase.getPsiFileInEditor(editor, project);
            presentation.setEnabled(file != null && isValidForFile(file));
            return;
        }

        // 如果是文件夹时
        boolean isValid = false;
        // 如果光标选择了文件树(可多选)
        final VirtualFile[] files = PlatformDataKeys.VIRTUAL_FILE_ARRAY.getData(dataContext);
        // 如果不是空文件夹
        if (null != files) {
            for (VirtualFile file : files) {
                // 只要有一个是文件夹且不是 node_modules文件夹 , 则可用
                if (file.isDirectory() && !NODE_MODULES_FILE.equals(file.getName())) {
                    // 文件夹可用
                    isValid = true;
                    break;
                }
                // 只要其中一个是 markdown 文件, 则可用
                if (isMardownFile(file.getName())) {
                    isValid = true;
                    break;
                }
            }
        }
        presentation.setEnabled(isValid);
    }

    /**
     * 通过文件验证是否为 markdown 且是否可写
     *
     * @param file the file
     * @return the boolean
     */
    private boolean isValidForFile(@NotNull PsiFile file) {
        if (!isMardownFile(file)) {
            return false;
        }
        // 不可写时按钮不可用
        return file.isWritable();
    }


    @Override
    public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
        // 获取当前在操作的工程上下文, 第一种方式
        Project project = anActionEvent.getProject();
        // 第二种方式, 其实最终调用的都是 actionEvent.getData(CommonDataKeys.PROJECT);
        // Project project = actionEvent.getData(PlatformDataKeys.PROJECT);
        // 获取当前操作的文件
        PsiFile psiFile = anActionEvent.getData(CommonDataKeys.PSI_FILE);
        if (psiFile != null && isMardownFile(psiFile.getOriginalFile().getName())) {
            // 解析 markdown 中的图片标签
            String text = Objects.requireNonNull(psiFile.getViewProvider().getDocument()).getText();
            Messages.showMessageDialog(project, psiFile.getFileType().getName(), "File Type", null);

            // 用回车键来分隔几个元素
            String[] textArray = text.split("\n");
            for (String string : textArray) {
                // todo-dong4j : (2019年03月12日 19:02) [要求只是是图片表示, 则肯定是以 ![]() 的形式出现]
                if (StringUtils.isNotBlank(string) && string.trim().startsWith("![") && string.trim().endsWith(")")) {
                    log.trace(string);
                    // 替换
                }
            }
        }

        if (project != null) {
            log.trace("project's base path = {}", project.getBasePath());
        } else {
            log.trace("project is null");
        }
    }

    @Contract(pure = true)
    private boolean isMardownFile(String name) {
        return name.endsWith(MARKDOWN_FILE_TYPE);
    }

    private boolean isMardownFile(AnActionEvent anActionEvent) {
        PsiFile psiFile = anActionEvent.getData(CommonDataKeys.PSI_FILE);
        if (psiFile == null) {
            return false;
        } else {
            return isMardownFile(psiFile.getOriginalFile().getName());
        }
    }

    private boolean isMardownFile(PsiFile psiFile) {
        if (psiFile == null) {
            return false;
        } else {
            return isMardownFile(psiFile.getOriginalFile().getName());
        }
    }
}
