package info.dong4j.idea.plugin.action.menu.image;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.VirtualFile;

import info.dong4j.idea.plugin.MikBundle;
import info.dong4j.idea.plugin.action.intention.IntentionActionBase;
import info.dong4j.idea.plugin.chain.ActionManager;
import info.dong4j.idea.plugin.chain.handler.ActionHandlerAdapter;
import info.dong4j.idea.plugin.chain.handler.FinalChainHandler;
import info.dong4j.idea.plugin.chain.handler.ImageCompressionHandler;
import info.dong4j.idea.plugin.chain.handler.ImageRenameHandler;
import info.dong4j.idea.plugin.chain.handler.RefreshFileSystemHandler;
import info.dong4j.idea.plugin.entity.EventData;
import info.dong4j.idea.plugin.entity.MarkdownImage;
import info.dong4j.idea.plugin.task.ActionTask;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.Icon;

import icons.MikIcons;
import lombok.extern.slf4j.Slf4j;

/**
 * 图像压缩操作类
 * 1. 只会处理图片
 * 2. 在目录上执行(右键菜单), 会遍历查找所有的图片并上传
 * 3. 在图片文件上执行(右键菜单), 只会处理当前图片
 * <p>
 * 该类用于处理图像压缩相关的操作，继承自 ImageActionBase，主要负责构建图像压缩处理链，包括压缩、重命名、替换原图等操作，并在处理完成后触发回调。
 * <p>
 * 支持通过事件驱动的方式执行图像处理任务，适用于在 IDE 中对 Markdown 文件中的图像进行批量处理的场景。
 *
 * @author dong4j
 * @version 0.0.1
 * @date 2021.02.14
 * @since 0.0.1
 */
@Slf4j
public final class ImageCompressAction extends ImageActionBase {
    /**
     * 获取图标
     * <p>
     * 返回图片压缩的图标
     *
     * @return 图标对象
     * @since 0.0.1
     */
    @Contract(pure = true)
    @Override
    public Icon getIcon() {
        return MikIcons.COMPRESS;
    }

    /**
     * 更新 Action 的展示状态
     * <p>
     * 动态设置菜单标题，根据是否启用压缩功能显示对应的状态。
     * 如果启用了普通压缩或 WebP 转换任意一个，显示"图片压缩: 已启用"，否则显示"图片压缩: 未启用"
     *
     * @param event Action事件对象
     * @since 2.2.0
     */
    @Override
    public void update(@NotNull AnActionEvent event) {
        // 调用父类的 update 方法，处理基础的可用性检查
        super.update(event);

        // 检查是否启用了压缩功能（普通压缩或 WebP 转换）
        boolean compressEnabled = IntentionActionBase.getState().isCompress();
        boolean webpEnabled = IntentionActionBase.getState().isConvertToWebp();
        boolean anyCompressEnabled = compressEnabled || webpEnabled;

        // 动态设置菜单标题
        Presentation presentation = event.getPresentation();
        if (anyCompressEnabled) {
            presentation.setText(MikBundle.message("mik.action.menu.compress.enabled"));
            
            // 构建详细的描述信息
            StringBuilder description = new StringBuilder();
            if (compressEnabled) {
                int compressPercent = IntentionActionBase.getState().getCompressBeforeUploadOfPercent();
                description.append(MikBundle.message("mik.action.menu.compress.to", compressPercent));
            }
            if (webpEnabled) {
                if (compressEnabled) {
                    description.append(", ");
                }
                int webpQuality = IntentionActionBase.getState().getWebpQuality();
                description.append(MikBundle.message("mik.action.menu.compress.webp", webpQuality));
            }
            presentation.setDescription(MikBundle.message("mik.action.menu.compress.description.enabled", description.toString()));
        } else {
            presentation.setText(MikBundle.message("mik.action.menu.compress.disabled"));
            presentation.setDescription(MikBundle.message("mik.action.menu.compress.description.disabled"));
        }
    }

    /**
     * 过滤文件
     * <p>
     * 图片压缩时过滤掉 SVG 和 GIF 格式，因为：
     * <ul>
     *   <li>SVG 是矢量图，压缩会导致质量损失且无实际意义</li>
     *   <li>GIF 是动图，压缩可能破坏动画效果</li>
     * </ul>
     *
     * @param virtualFile 虚拟文件对象
     * @return 如果文件应该被处理返回 true，否则返回 false
     * @since 2.2.0
     */
    @Override
    protected boolean shouldProcessFile(@NotNull VirtualFile virtualFile) {
        String fileName = virtualFile.getName().toLowerCase();
        // 过滤掉 svg 和 gif 格式
        if (fileName.endsWith(".svg") || fileName.endsWith(".gif")) {
            log.debug("图片压缩跳过 SVG/GIF 格式: {}", virtualFile.getName());
            return false;
        }
        return true;
    }

    /**
     * 构建处理链，用于执行图片相关的处理任务
     * <p>
     * 该方法根据给定的事件和等待处理的图片映射关系，创建一个包含多个处理步骤的链式结构。
     * 处理步骤包括图片压缩、图片重命名、替换原图以及处理完成后的 VFS 刷新操作。
     * 最后将构建好的处理链作为后台任务提交执行。
     *
     * @param event             事件对象，包含触发处理的上下文信息
     * @param waitingProcessMap 等待处理的图片映射关系，键为文档对象，值为图片列表
     * @since 0.0.1
     */
    @Override
    protected void buildChain(AnActionEvent event, Map<Document, List<MarkdownImage>> waitingProcessMap) {
        EventData data = new EventData()
            .setAction("ImageCompressAction")
            .setActionEvent(event)
            .setProject(event.getProject())
            .setWaitingProcessMap(waitingProcessMap);

        ActionManager manager = new ActionManager(data)
            // 图片压缩
            .addHandler(new ImageCompressionHandler())
            // 图片重命名
            .addHandler(new ImageRenameHandler())
            // 删除并替换
            .addHandler(new DeleteFileHandler())
            // 刷新文件系统
            .addHandler(new RefreshFileSystemHandler())
            // 回收资源
            .addHandler(new FinalChainHandler());

        // 开启后台任务
        new ActionTask(event.getProject(), MikBundle.message("mik.action.compress.progress"), manager).queue();
    }


    /**
     * 删除文件处理类
     * <p>
     * 用于处理Markdown图片替换操作，负责获取操作名称并执行图片流写入及原始文件删除逻辑。
     * 该类继承自ActionHandlerAdapter，主要用于在图片替换过程中完成文件的存储与清理工作。
     *
     * @author 未知
     * @version 1.0.0
     * @date 2025.10.24
     * @since 1.0.0
     */
    private static final class DeleteFileHandler extends ActionHandlerAdapter {
        /**
         * 获取名称信息
         * <p>
         * 返回一个固定的名称字符串"替换原图"
         *
         * @return 名称字符串
         */
        @Override
        public String getName() {
            return MikBundle.message("mik.action.replace.original");
        }

        /**
         * 处理Markdown图片的回调方法，将图片流写入指定路径
         * <p>
         * 该方法接收事件数据、图片迭代器和具体的Markdown图片对象，通过读取图片流并写入文件系统实现图片存储。若图片路径已更新为webp格式，则会将新文件写入指定路径，并删除原始文件。
         *
         * @param data          事件数据
         * @param imageIterator 图片迭代器
         * @param markdownImage 具体的Markdown图片对象
         */
        @Override
        public void invoke(EventData data, Iterator<MarkdownImage> imageIterator, MarkdownImage markdownImage) {
            InputStream inputStream = markdownImage.getInputStream();
            String newPath = markdownImage.getPath();
            String originalPath = null;

            // 保存原始文件路径（如果存在）
            if (markdownImage.getVirtualFile() != null) {
                originalPath = markdownImage.getVirtualFile().getPath();
            }

            try {
                // 写入新文件（如果路径已更新为 webp，会写入到新位置）
                FileUtil.copy(inputStream, new FileOutputStream(newPath));

                // 如果原始文件存在且路径与新路径不同（说明已转换为 webp），则删除原始文件
                if (originalPath != null && !originalPath.equals(newPath)) {
                    File originalFile = new File(originalPath);
                    if (originalFile.exists() && originalFile.isFile()) {
                        boolean deleted = originalFile.delete();
                        if (deleted) {
                            log.debug("已删除原始文件: {}", originalPath);
                        } else {
                            log.debug("删除原始文件失败: {}", originalPath);
                        }
                    }
                }
            } catch (IOException e) {
                log.debug("", e);
            }
        }
    }

}
