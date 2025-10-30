package info.dong4j.idea.plugin.action.image;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.impl.DocumentImpl;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;

import info.dong4j.idea.plugin.content.ImageContents;
import info.dong4j.idea.plugin.entity.MarkdownImage;
import info.dong4j.idea.plugin.enums.ImageLocationEnum;
import info.dong4j.idea.plugin.enums.ImageMarkEnum;
import info.dong4j.idea.plugin.util.ActionUtils;
import info.dong4j.idea.plugin.util.ImageUtils;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.Icon;

import lombok.extern.slf4j.Slf4j;

/**
 * 图片操作基础类
 * <p>
 * 该类作为图片处理操作的基类，提供图片压缩、上传及 URL 处理的基础功能。主要用于支持图片相关操作的扩展，如图片压缩、上传至服务器并保存 URL 到剪贴板等。该类通过继承 AnAction 实现了 IntelliJ IDEA 的插件动作逻辑，支持在编辑器中触发图片处理操作。
 * <p>
 * 该类包含构建图片处理链、更新动作状态、执行图片处理动作等核心方法，并通过抽象方法定义了图标获取和链构建的接口，便于子类扩展。
 *
 * @author dong4j
 * @version 0.0.1
 * @date 2021.02.14
 * @since 0.0.1
 */
@Slf4j
public abstract class ImageActionBase extends AnAction {
    /**
     * 获取图标
     * <p>
     * 返回当前对象对应的图标
     *
     * @return 图标对象
     * @since 0.0.1
     */
    abstract protected Icon getIcon();

    /**
     * 构建处理链
     * <p>
     * 根据给定的事件和等待处理的流程映射，构建相应的处理链
     *
     * @param event             事件对象，包含操作上下文信息
     * @param waitingProcessMap 等待处理的流程映射，键为文档对象，值为Markdown图片列表
     * @since 0.0.1
     */
    abstract void buildChain(AnActionEvent event, Map<Document, List<MarkdownImage>> waitingProcessMap);

    /**
     * 更新操作，用于启用或禁用该动作
     * <p>
     * 该方法通过设置动作可用状态为 true，使动作在 UI 中显示并可执行
     *
     * @param event 动作事件对象，包含执行动作所需的信息
     * @since 0.0.1
     */
    @Override
    public void update(@NotNull AnActionEvent event) {
        ActionUtils.isAvailable(true, event, this.getIcon(), ImageContents.IMAGE_TYPE_NAME);
    }

    /**
     * 处理用户触发的Action事件，用于构建Markdown图片处理链
     * <p>
     * 该方法根据当前选中的编辑器或文件夹，收集所有Markdown图片文件，并构建处理链
     *
     * @param event 事件对象，包含当前操作上下文信息
     */
    @SuppressWarnings("D")
    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        Map<Document, List<MarkdownImage>> waitingProcessMap = new HashMap<>(64);

        Project project = event.getProject();
        if (project != null) {
            log.trace("project's base path = {}", project.getBasePath());

            // 如果选中编辑器
            DataContext dataContext = event.getDataContext();

            Editor editor = PlatformDataKeys.EDITOR.getData(dataContext);
            if (null != editor) {
                VirtualFile virtualFile = event.getData(PlatformDataKeys.VIRTUAL_FILE);
                assert virtualFile != null;
                this.buildWaitingProcessMap(waitingProcessMap, virtualFile);
            } else {
                // 获取被选中的有文件和目录
                VirtualFile[] virtualFiles = PlatformDataKeys.VIRTUAL_FILE_ARRAY.getData(dataContext);
                if (null != virtualFiles) {
                    for (VirtualFile rootFile : virtualFiles) {
                        if (ImageContents.IMAGE_TYPE_NAME.equals(rootFile.getFileType().getName())) {
                            this.buildWaitingProcessMap(waitingProcessMap, rootFile);

                        }
                        // 如果是目录, 则递归获取所有 image 文件
                        if (rootFile.isDirectory()) {
                            List<VirtualFile> imageFiles = ImageUtils.recursivelyImageFile(rootFile);
                            for (VirtualFile subFile : imageFiles) {
                                this.buildWaitingProcessMap(waitingProcessMap, subFile);
                            }
                        }
                    }
                }
            }

            if (!waitingProcessMap.isEmpty()) {
                this.buildChain(event, waitingProcessMap);
            }
        }
    }

    /**
     * 构建等待处理的流程映射
     * <p>
     * 将给定的虚拟文件转换为 MarkdownImage 对象，并将其添加到等待处理的流程映射中。
     *
     * @param waitingProcessMap 等待处理的流程映射
     * @param virtualFile       虚拟文件对象
     */
    private void buildWaitingProcessMap(@NotNull Map<Document, List<MarkdownImage>> waitingProcessMap,
                                        @NotNull VirtualFile virtualFile) {
        MarkdownImage markdownImage = new MarkdownImage();
        markdownImage.setVirtualFile(virtualFile);
        markdownImage.setImageName(virtualFile.getName());
        markdownImage.setPath(virtualFile.getPath());
        try {
            markdownImage.setInputStream(virtualFile.getInputStream());
        } catch (IOException e) {
            return;
        }
        markdownImage.setFileName(virtualFile.getName());
        markdownImage.setExtension(virtualFile.getExtension());
        markdownImage.setLocation(ImageLocationEnum.LOCAL);
        markdownImage.setImageMarkType(ImageMarkEnum.ORIGINAL);

        waitingProcessMap.put(new DocumentImpl(""), new ArrayList<>() {
            /** 序列化版本号，用于确保类的兼容性 */
            @java.io.Serial
            private static final long serialVersionUID = 5838886826856938689L;

            {
                this.add(markdownImage);
            }
        });
    }
}
