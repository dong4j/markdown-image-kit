package info.dong4j.test;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.psi.PsiFile;

import org.jetbrains.annotations.NotNull;

/**
 * <p>Company: 科大讯飞股份有限公司-四川分公司</p>
 * <p>Description: ${description}</p>
 *
 * @author dong4j
 * @date 2019-03-12 09:31
 * @email sjdong3@iflytek.com
 */
public class AliyunOssUpload extends AnAction {

    @Override
    public void actionPerformed(@NotNull AnActionEvent actionEvent) {
        // 获取当前在操作的工程上下文
        Project project1 = actionEvent.getProject();
        Project project2 = actionEvent.getData(PlatformDataKeys.PROJECT);
        // 获取当前操作的类文件
        PsiFile psiFile = actionEvent.getData(CommonDataKeys.PSI_FILE);
        // 获取当前类文件路径
        String classPath = "";
        if (psiFile != null) {
            classPath = psiFile.getVirtualFile().getPath();
        }
        String title = "hello world啊啊啊啊";

        // 显示对话框
        Messages.showMessageDialog(project1, classPath, title, Messages.getInformationIcon());
        Messages.showMessageDialog(project2, classPath, title, Messages.getInformationIcon());
    }
}
