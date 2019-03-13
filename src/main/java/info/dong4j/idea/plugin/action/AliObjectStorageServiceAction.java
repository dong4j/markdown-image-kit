package info.dong4j.idea.plugin.action;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiUtilBase;

import info.dong4j.idea.plugin.util.ParserUtils;
import info.dong4j.idea.plugin.util.PsiDocumentUtils;
import info.dong4j.idea.plugin.util.UploadUtils;

import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.util.Map;
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
    private static final String HTML_TAG_EXTEND = "<a data-fancybox title='${}' href='${}' >![${}](${})</a>";
    private static final String HTML_TAG = "<a title='${}' href='${}' >![${}](${})</a>";

    /**
     * 更新 "upload to Aliyun OSS" 按钮是否可用
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


    /**
     * 处理点击 "upload to Aliyun OSS" 按钮的逻辑
     *
     * @param anActionEvent the an action event
     */
    @Override
    public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
        Project project = anActionEvent.getProject();
        if (project != null) {
            log.trace("project's base path = {}", project.getBasePath());
            // 获取当前操作的文件
            PsiFile psiFile = anActionEvent.getData(CommonDataKeys.PSI_FILE);
            if (psiFile != null && isMardownFile(psiFile.getOriginalFile().getName())) {
                // 解析 markdown 中的图片标签
                Document document = psiFile.getViewProvider().getDocument();
                String text = Objects.requireNonNull(document).getText();

                // 用回车键来分隔几个元素
                String[] textArray = text.split("\n");
                String url = "";
                for (int i = 0; i < textArray.length; i++) {
                    // todo-dong4j : (2019年03月12日 19:02) [要求只是是图片表示, 则肯定是以 ![]() 的形式出现]
                    if (StringUtils.isNotBlank(textArray[i]) && textArray[i].trim().startsWith("![") && textArray[i].trim().endsWith(")")) {
                        log.trace(textArray[i]);
                        // 替换字符串
                        Map<String, String> map = ParserUtils.parseImageTag(textArray[i]);
                        for (Map.Entry<String, String> result : map.entrySet()) {
                            log.trace("key = {}, value = {}", result.getKey(), result.getValue());
                            // 上传到 OSS
                            url = upload(anActionEvent, result.getValue());
                            textArray[i] = ParserUtils.parse0(HTML_TAG_EXTEND, result.getKey(), url, result.getKey(), url);
                        }
                    }
                }

                // 替换全部字符串
                StringBuilder stringBuilder = new StringBuilder();
                for (String string : textArray) {
                    stringBuilder.append(string).append("\n");
                }
                PsiDocumentUtils.commitAndSaveDocument(project, document, stringBuilder.toString());
                showHintDialog(url);
            }
        } else {
            log.trace("project is null");
        }
    }

    @Contract(pure = true)
    private boolean isMardownFile(String name) {
        return name.endsWith(MARKDOWN_FILE_TYPE);
    }

    @Contract("null -> false")
    private boolean isMardownFile(PsiFile psiFile) {
        return psiFile != null && isMardownFile(psiFile.getOriginalFile().getName());
    }

    /**
     * 显示提示对话框
     *
     * @param text          the text
     */
    private void showHintDialog(String text) {
        Notification notification = new Notification("Upload Aliyun OSS", null, NotificationType.INFORMATION);
        // 可使用 HTML 标签
        notification.setContent(text);
        notification.setTitle("title");
        notification.setSubtitle("subTitle");
        notification.setImportant(true);
        Notifications.Bus.notify(notification);
    }

    private String upload( AnActionEvent anActionEvent, String filePath){
        final Project project = anActionEvent.getProject();
        PsiFile psiFile = findImageResource(project, filePath);
        if (psiFile != null) {
            filePath = psiFile.getVirtualFile().getPath();
            String name = UploadUtils.uploadImg2Oss(new File(filePath));
            return UploadUtils.getUrl(name);
        }
        return filePath;
    }

    @Nullable
    private static PsiFile findImageResource(Project project, String filePath) {

        // ./imgs/1eefcf26.png
        String fileName = filePath.substring(filePath.lastIndexOf("/") + 1);

        PsiFile[] foundFiles = FilenameIndex.getFilesByName(project, fileName, GlobalSearchScope.allScope(project));
        if (foundFiles.length <= 0) {
            return null;
        }

        return foundFiles[0];
    }
}
