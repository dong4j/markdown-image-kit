package info.dong4j.idea.plugin.action.menu.image;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.vfs.VirtualFile;

import info.dong4j.idea.plugin.MikBundle;
import info.dong4j.idea.plugin.action.intention.IntentionActionBase;
import info.dong4j.idea.plugin.chain.ActionManager;
import info.dong4j.idea.plugin.chain.handler.CheckAvailableClientHandler;
import info.dong4j.idea.plugin.chain.handler.FinalChainHandler;
import info.dong4j.idea.plugin.chain.handler.ImageCompressionHandler;
import info.dong4j.idea.plugin.chain.handler.ImageLabelChangeHandler;
import info.dong4j.idea.plugin.chain.handler.ImageRenameHandler;
import info.dong4j.idea.plugin.chain.handler.ImageUploadHandler;
import info.dong4j.idea.plugin.chain.handler.RefreshFileSystemHandler;
import info.dong4j.idea.plugin.chain.handler.WriteToClipboardHandler;
import info.dong4j.idea.plugin.client.OssClient;
import info.dong4j.idea.plugin.entity.EventData;
import info.dong4j.idea.plugin.entity.MarkdownImage;
import info.dong4j.idea.plugin.enums.CloudEnum;
import info.dong4j.idea.plugin.settings.OssState;
import info.dong4j.idea.plugin.task.ActionTask;
import info.dong4j.idea.plugin.util.ClientUtils;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

import javax.swing.Icon;

import icons.MikIcons;
import lombok.extern.slf4j.Slf4j;

/**
 * 图片右键直接上传功能类
 * 1. 只会处理图片
 * 2. 在目录上执行(右键菜单), 会遍历查找所有的图片并上传
 * 3. 在图片文件上执行(右键菜单), 只会处理当前图片
 * <p>
 * 该类用于实现图片右键直接上传的业务逻辑，继承自 ImageActionBase，主要负责构建上传流程链，处理图片压缩、重命名、选择存储客户端、上传、添加标签、复制到剪贴板等操作，并最终将任务提交到队列中执行。
 * <p>
 * 使用该类时，通常通过其继承的基类方法触发，例如在用户右键点击图片时调用相关方法，从而启动上传流程。
 *
 * @author dong4j
 * @version 1.0.0
 * @date 2025.10.24
 * @since 1.0.0
 */
@Slf4j
public final class ImageUploadAction extends ImageActionBase {
    /** 最小文件大小限制：1KB */
    private static final long MIN_FILE_SIZE = 1024;
    /** 最大文件大小限制：5MB */
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;

    /**
     * 获取图标
     * <p>
     * 返回一个预定义的图标对象，用于表示调试器的悬停状态。
     *
     * @return 图标对象
     * @since 0.0.1
     */
    @Contract(pure = true)
    @Override
    public Icon getIcon() {
        return MikIcons.MIK;
    }

    /**
     * 过滤文件
     * <p>
     * 图片上传时过滤掉不符合大小要求的文件：
     * <ul>
     *   <li>小于 1KB 的文件：可能是损坏或无效的图片</li>
     *   <li>大于 5MB 的文件：文件过大，上传可能失败或耗时过长</li>
     * </ul>
     *
     * @param virtualFile 虚拟文件对象
     * @return 如果文件应该被处理返回 true，否则返回 false
     * @since 2.2.0
     */
    @Override
    protected boolean shouldProcessFile(@NotNull VirtualFile virtualFile) {
        long fileSize = virtualFile.getLength();

        // 过滤掉小于 1KB 的文件
        if (fileSize < MIN_FILE_SIZE) {
            log.debug("图片上传跳过小文件 ({}字节 < 1KB): {}", fileSize, virtualFile.getName());
            return false;
        }

        // 过滤掉大于 5MB 的文件
        if (fileSize > MAX_FILE_SIZE) {
            log.debug("图片上传跳过大文件 ({}字节 > 10MB): {}", fileSize, virtualFile.getName());
            return false;
        }

        return true;
    }

    /**
     * 构建处理链用于执行图片处理任务
     * <p>
     * 根据传入的事件和等待处理的图片信息，构建一个处理链，依次执行图片压缩、重命名、客户端处理、上传、标签转换、复制到剪贴板等操作，并将整个处理过程作为后台任务执行。
     *
     * @param event             触发事件对象，包含操作上下文信息
     * @param waitingProcessMap 等待处理的图片信息映射，键为文档对象，值为图片列表
     */
    @Override
    protected void buildChain(AnActionEvent event, Map<Document, List<MarkdownImage>> waitingProcessMap) {
        // 使用默认 client
        CloudEnum cloudEnum = OssState.getCloudType(IntentionActionBase.getState().getDefaultCloudType());
        OssClient client = ClientUtils.getClient(cloudEnum);

        EventData data = new EventData()
            .setAction("ImageUploadAction")
            .setActionEvent(event)
            .setProject(event.getProject())
            .setClient(client)
            .setClientName(cloudEnum.title)
            .setWaitingProcessMap(waitingProcessMap);

        ActionManager manager = new ActionManager(data)
            // 图片压缩
            .addHandler(new ImageCompressionHandler())
            // 图片重命名
            .addHandler(new ImageRenameHandler())
            // 处理 client
            .addHandler(new CheckAvailableClientHandler())
            // 图片上传
            .addHandler(new ImageUploadHandler())
            // 标签转换
            .addHandler(new ImageLabelChangeHandler())
            // 写到 clipboard
            .addHandler(new WriteToClipboardHandler())
            // 刷新文件系统
            .addHandler(new RefreshFileSystemHandler())
            // 回收资源
            .addHandler(new FinalChainHandler());

        // 开启后台任务
        new ActionTask(event.getProject(), MikBundle.message("mik.action.upload.process", cloudEnum.title), manager).queue();
    }
}
