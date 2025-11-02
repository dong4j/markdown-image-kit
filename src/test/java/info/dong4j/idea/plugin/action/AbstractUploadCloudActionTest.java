package info.dong4j.idea.plugin.action;

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

import java.io.File;
import java.util.Objects;

/**
 * 抽象上传云操作测试类
 * <p>
 * 该类主要用于测试与云上传操作相关的抽象行为，包括对 Action 的执行、VirtualFile 的获取以及 PSI 文件的获取等逻辑。
 * 提供了多种方式来获取文档和 PSI 文件对象，适用于需要验证不同数据源和上下文的上传操作测试场景。
 *
 * @author dong4j
 * @version 1.0.0
 * @date 2025.10.24
 * @since 1.1.0
 */
public class AbstractUploadCloudActionTest {
    /**
     * 处理用户触发的编辑器动作事件
     * <p>
     * 该方法用于响应用户在编辑器中执行的动作，例如输入、选择等。
     * 内部逻辑涉及获取项目、虚拟文件、Psi元素工厂等，用于创建和插入注释。
     * 该方法在版本 1.1.0 中引入。
     *
     * @param e 动作事件对象，包含执行动作所需的信息
     * @since 1.1.0
     */
    public void actionPerformed(@NotNull AnActionEvent e) {
        // PsiFile file = PsiFileFactory.getClient(e.getProject()).createFileFromText()
        // VirtualFile virtualFile = e.getData(PlatformDataKeys.VIRTUAL_FILE);
        // PsiElementFactory factory = virtualFile.getManager().getElementFactory();
        // PsiComment comment = factory.createCommentFromText(msg, file);
        // file.addBefore(comment, file.getFirstChild());
        // 编辑器事件传递
        // EditorActionManager.getClient().getActionHandler(IdeActions.ACTION_EDITOR_ENTER).execute(editor, caret, dataContext);
    }

    /**
     * 从 AnActionEvent 获取 VirtualFile 的多种方式，包括通过事件数据、本地文件系统路径、PSI 文件以及 Document 获取。
     * <p>
     * 该方法演示了获取 VirtualFile 的不同途径，适用于插件开发中与文件系统交互的场景。
     *
     * @param e 事件对象，用于获取与 VirtualFile 相关的数据
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
        this.getDocument(e);
    }

    /**
     * 从不同来源尝试获取当前文档对象
     * <p>
     * 该方法通过多种方式获取文档对象，包括从编辑器、VirtualFile、缓存以及 PSI 获取。
     * 如果其中任意一种方式成功，则使用该文档对象。
     *
     * @param e 事件对象，包含获取文档所需的数据
     * @since 1.1.0
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
     * 获取 PSI 文件的几种方式
     * <p>
     * 通过 AnActionEvent 获取 PSI 文件，支持从 Action、VirtualFile、Document 等多种方式获取 PSI 文件。
     *
     * @param e 动作事件对象，包含获取 PSI 文件所需的数据
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
                FilenameIndex.getFilesByName(project, "filename", GlobalSearchScope.projectScope(project));
            }
        }

        // 找到特定 PSI 元素的使用位置
        // ReferencesSearch.search();
    }

}
