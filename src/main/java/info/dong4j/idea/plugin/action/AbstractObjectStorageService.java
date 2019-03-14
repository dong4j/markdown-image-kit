package info.dong4j.idea.plugin.action;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
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

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.*;

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
public abstract class AbstractObjectStorageService extends AnAction {
    private static final String MARKDOWN_FILE_TYPE = ".md";
    private static final String NODE_MODULES_FILE = "node_modules";
    /**
     * The Html tag extend.
     */
    static final String HTML_TAG_EXTEND = "<a data-fancybox title='${}' href='${}' >![${}](${})</a>";
    /**
     * The constant HTML_TAG.
     */
    public static final String HTML_TAG = "<a title='${}' href='${}' >![${}](${})</a>";

    /**
     * 检查 "upload to XXX OSS" 按钮是否可用
     *
     * @param event the event
     */
    @Override
    public void update(@NotNull AnActionEvent event) {
        final Presentation presentation = event.getPresentation();
        final DataContext dataContext = event.getDataContext();

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
            presentation.setEnabled(file != null && isValidForFile(file) && isPassedTest());
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
        presentation.setEnabled(isValid && isPassedTest());
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

    /**
     * Is mardown file boolean.
     *
     * @param name the name
     * @return the boolean
     */
    @Contract(pure = true)
    boolean isMardownFile(String name) {
        return name.endsWith(MARKDOWN_FILE_TYPE);
    }

    /**
     * Is mardown file boolean.
     *
     * @param psiFile the psi file
     * @return the boolean
     */
    @Contract("null -> false")
    private boolean isMardownFile(PsiFile psiFile) {
        return psiFile != null && isMardownFile(psiFile.getOriginalFile().getName());
    }

    /**
     * 是否通过测试
     *
     * @return the boolean
     */
    abstract boolean isPassedTest();

    /**
     * 显示提示对话框
     *
     * @param text the text
     */
    void notifucation(String text) {
        Notification notification = new Notification("Upload to OSS", null, NotificationType.INFORMATION);
        // 可使用 HTML 标签
        notification.setContent(text);
        notification.setTitle("title");
        notification.setSubtitle("subTitle");
        notification.setImportant(true);
        Notifications.Bus.notify(notification);
    }

    /**
     * 由子类实现具体上传方式
     *
     * @param file the file 待上传的文件
     * @return the string   url
     */
    abstract String upload(File file);

    /**
     * 处理事件
     *
     * @param anActionEvent the an action event
     */
    @Override
    public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
        Project project = anActionEvent.getProject();
        if (project != null) {
            log.trace("project's base path = {}", project.getBasePath());
            // 1. 获取当前被选中的文件或目录
            //  1.1 如果是 markdown 文件, 开始解析
            //  1.2 如果是目录, 遍历目录(排除 node_modules), 获取所有 markdown 文件, 然后开始解析
            // 2. 开线程来
        }
    }
}
