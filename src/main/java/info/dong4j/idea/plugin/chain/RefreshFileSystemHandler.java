package info.dong4j.idea.plugin.chain;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;

import info.dong4j.idea.plugin.MikBundle;
import info.dong4j.idea.plugin.entity.EventData;
import info.dong4j.idea.plugin.entity.MarkdownImage;

import java.io.File;
import java.util.Iterator;

import lombok.extern.slf4j.Slf4j;

/**
 * 刷新文件系统处理器
 * <p>
 * 用于在处理完成后刷新虚拟文件系统（VFS），确保新增的图片文件能够及时显示在文件树中。
 * 该处理器继承自 ActionHandlerAdapter，通常在处理器链的最后执行。
 * 优先刷新特定目录，如果无法获取目录信息，则刷新整个文件系统。
 *
 * @author dong4j
 * @version 1.0.0
 * @date 2025.11.01
 * @since 1.0.0
 */
@SuppressWarnings("D")
@Slf4j
public class RefreshFileSystemHandler extends ActionHandlerAdapter {
    /**
     * 获取名称
     * <p>
     * 返回预定义的名称字符串，用于表示操作进度标题
     *
     * @return 名称字符串
     * @since 1.0.0
     */
    @Override
    public String getName() {
        return MikBundle.message("mik.action.refresh.filesystem.title");
    }

    /**
     * 执行刷新文件系统操作
     * <p>
     * 该方法用于在处理完成后刷新虚拟文件系统（VFS），确保新增的图片文件能够及时显示。
     * 优先刷新特定目录（如果可以从 markdownImage 中获取路径），否则刷新整个文件系统。
     * 由于处理器可能在后台线程中执行，使用 invokeLater 确保在 EDT 线程中执行刷新操作。
     *
     * @param data          事件数据对象
     * @param imageIterator 图片迭代器，用于遍历图片数据
     * @param markdownImage Markdown图片对象
     * @since 1.0.0
     */
    @Override
    public void invoke(EventData data, Iterator<MarkdownImage> imageIterator, MarkdownImage markdownImage) {
        // 刷新 VFS, 避免新增的图片很久才显示出来
        // 使用 invokeLater 确保在 EDT 线程中执行，避免线程访问异常
        ApplicationManager.getApplication().invokeLater(() -> {
            // 优先刷新特定目录
            if (markdownImage != null && markdownImage.getPath() != null && !markdownImage.getPath().isEmpty()) {
                refreshSpecificDirectory(markdownImage.getPath(), data);
            } else {
                // 如果无法获取特定目录，则刷新整个文件系统
                VirtualFileManager.getInstance().syncRefresh();
            }
        });
        log.trace("文件系统刷新完成");
    }

    /**
     * 刷新特定目录
     * <p>
     * 根据图片路径刷新对应的目录，确保新文件能够立即显示。
     * 如果路径是相对路径，则相对于当前文档所在目录进行刷新。
     *
     * @param imagePath 图片路径（可能是相对路径或绝对路径）
     * @param data      事件数据，用于获取项目和文档信息
     * @since 1.0.0
     */
    private void refreshSpecificDirectory(String imagePath, EventData data) {
        try {
            File imageFile = new File(imagePath);

            // 如果是相对路径，尝试从当前文档所在目录解析
            if (!imageFile.isAbsolute() && data.getEditor() != null) {
                try {
                    com.intellij.openapi.editor.Document document = data.getEditor().getDocument();
                    com.intellij.openapi.fileEditor.FileDocumentManager fileDocMgr =
                        com.intellij.openapi.fileEditor.FileDocumentManager.getInstance();
                    VirtualFile currentFile = fileDocMgr.getFile(document);

                    if (currentFile != null) {
                        // 从当前文档所在目录解析相对路径
                        File currentDocFile = new File(currentFile.getPath());
                        File currentDocDir = currentDocFile.getParentFile();
                        if (currentDocDir != null) {
                            imageFile = new File(currentDocDir, imagePath);
                        }
                    }
                } catch (Exception e) {
                    log.trace("从当前文档解析路径失败", e);
                }
            }

            // 如果仍然是相对路径，尝试从项目根目录解析
            if (!imageFile.isAbsolute() && data.getProject() != null) {
                String projectBasePath = data.getProject().getBasePath();
                if (projectBasePath != null) {
                    imageFile = new File(projectBasePath, imagePath);
                }
            }

            // 刷新文件所在目录
            File parentDir = imageFile.getParentFile();
            if (parentDir != null && parentDir.exists()) {
                VirtualFile virtualDir = LocalFileSystem.getInstance().refreshAndFindFileByIoFile(parentDir);
                if (virtualDir != null) {
                    // 刷新目录，确保新文件显示
                    virtualDir.refresh(false, false);
                    log.trace("已刷新目录: {}", parentDir.getAbsolutePath());
                    return;
                }
            }
        } catch (Exception e) {
            log.trace("刷新特定目录失败，将刷新整个文件系统", e);
        }

        // 如果刷新特定目录失败，则刷新整个文件系统
        VirtualFileManager.getInstance().syncRefresh();
    }
}

