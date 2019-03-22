package info.dong4j.idea.plugin.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;

import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.util.Objects;

/**
 * <p>Company: 科大讯飞股份有限公司-四川分公司</p>
 * <p>Description: open api 测试类</p>
 *
 * @author dong4j
 * @email sjdong3@iflytek.com
 * @since 2019-03-21 21:42
 */
public class TestAction extends AnAction {
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        // PsiFile file = PsiFileFactory.getInstance(e.getProject()).createFileFromText()
        // VirtualFile virtualFile = e.getData(PlatformDataKeys.VIRTUAL_FILE);
        // PsiElementFactory factory = virtualFile.getManager().getElementFactory();
        // PsiComment comment = factory.createCommentFromText(msg, file);
        // file.addBefore(comment, file.getFirstChild());
        // 编辑器事件传递
        // EditorActionManager.getInstance().getActionHandler(IdeActions.ACTION_EDITOR_ENTER).execute(editor, caret, dataContext);
    }

    /**
     * 获取 VirtualFile 的几种方式
     *
     * @param e the e
     */
    private void getVirtualFile(AnActionEvent e) {
        // 获取 VirtualFile 方式一:
        VirtualFile virtualFile = e.getData(PlatformDataKeys.VIRTUAL_FILE);
        // 获取多个 VirtualFile
        VirtualFile[] virtualFiles = e.getData(PlatformDataKeys.VIRTUAL_FILE_ARRAY);
        // 方式二: 从本地文件系统路径获取
        VirtualFile virtualFileFromLocalFileSystem = LocalFileSystem.getInstance().findFileByIoFile(new File("path"));
        // 方式三: 从 PSI 文件 (如果 PSI 文件仅存在内存中, 则可能返回 null)
        PsiFile psiFile = e.getData(CommonDataKeys.PSI_FILE);
        if (psiFile != null) {
            psiFile.getVirtualFile();
        }
        // 方式四: 从 document 中
        Document document = Objects.requireNonNull(e.getData(PlatformDataKeys.EDITOR)).getDocument();
        VirtualFile virtualFileFromDocument = FileDocumentManager.getInstance().getFile(document);

        // 获取 document
        getDocument(e);
    }

    /**
     * 获取 document 的几种方式
     *
     * @param e the e
     */
    private void getDocument(AnActionEvent e) {
        // 从当前编辑器中获取
        Document documentFromEditor = Objects.requireNonNull(e.getData(PlatformDataKeys.EDITOR)).getDocument();
        // 从 VirtualFile 获取 (如果之前未加载文档内容，则此调用会强制从磁盘加载文档内容)
        VirtualFile virtualFile = e.getData(PlatformDataKeys.VIRTUAL_FILE);
        if (virtualFile != null) {
            Document documentFromVirtualFile = FileDocumentManager.getInstance().getDocument(virtualFile);
            // 从缓存中获取
            Document documentFromVirtualFileCache = FileDocumentManager.getInstance().getCachedDocument(virtualFile);

            // 从 PSI 中获取
            Project project = e.getProject();
            if (project != null) {
                // 获取 PSI (一)
                PsiFile psiFile = PsiManager.getInstance(project).findFile(virtualFile);
                // 获取 PSI (二)
                psiFile = e.getData(CommonDataKeys.PSI_FILE);
                if (psiFile != null) {
                    Document documentFromPsi = PsiDocumentManager.getInstance(project).getDocument(psiFile);
                    // 从缓存中获取
                    Document documentFromPsiCache = PsiDocumentManager.getInstance(project).getCachedDocument(psiFile);
                }
            }
        }
    }

    /**
     * 获取 PSI 的几种方式
     *
     * @param e the e
     */
    private void getPsiFile(AnActionEvent e) {
        // 从 action 中获取
        PsiFile psiFileFromAction = e.getData(LangDataKeys.PSI_FILE);
        Project project = e.getProject();
        if (project != null) {
            VirtualFile virtualFile = e.getData(PlatformDataKeys.VIRTUAL_FILE);
            if (virtualFile != null) {
                // 从 VirtualFile 获取
                PsiFile psiFileFromVirtualFile = PsiManager.getInstance(project).findFile(virtualFile);

                // 从 document
                Document documentFromEditor = Objects.requireNonNull(e.getData(PlatformDataKeys.EDITOR)).getDocument();
                PsiFile psiFileFromDocument = PsiDocumentManager.getInstance(project).getPsiFile(documentFromEditor);

                // 在 project 范围内查找特定 PsiFile
                FilenameIndex.getFilesByName(project, "fileName", GlobalSearchScope.projectScope(project));
            }
        }

        // 找到特定 PSI 元素的使用位置
        // ReferencesSearch.search();
    }
}
