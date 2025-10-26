package info.dong4j.idea.plugin.action.image;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.editor.Document;

import info.dong4j.idea.plugin.MikBundle;
import info.dong4j.idea.plugin.chain.ActionManager;
import info.dong4j.idea.plugin.chain.FinalChainHandler;
import info.dong4j.idea.plugin.chain.ImageCompressionHandler;
import info.dong4j.idea.plugin.chain.ImageLabelChangeHandler;
import info.dong4j.idea.plugin.chain.ImageRenameHandler;
import info.dong4j.idea.plugin.chain.ImageUploadHandler;
import info.dong4j.idea.plugin.chain.InsertToClipboardHandler;
import info.dong4j.idea.plugin.chain.OptionClientHandler;
import info.dong4j.idea.plugin.client.OssClient;
import info.dong4j.idea.plugin.entity.EventData;
import info.dong4j.idea.plugin.entity.MarkdownImage;
import info.dong4j.idea.plugin.enums.CloudEnum;
import info.dong4j.idea.plugin.settings.OssState;
import info.dong4j.idea.plugin.task.ActionTask;
import info.dong4j.idea.plugin.util.ClientUtils;

import org.jetbrains.annotations.Contract;

import java.util.List;
import java.util.Map;

import javax.swing.Icon;

import lombok.extern.slf4j.Slf4j;

/**
 * 图片右键直接上传功能类
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
    protected Icon getIcon() {
        return AllIcons.Debugger.Overhead;
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
        CloudEnum cloudEnum = OssState.getCloudType(STATE.getCloudType());
        OssClient client = ClientUtils.getClient(cloudEnum);

        EventData data = new EventData()
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
            .addHandler(new OptionClientHandler())
            // 图片上传
            .addHandler(new ImageUploadHandler())
            // 标签转换
            .addHandler(new ImageLabelChangeHandler())
            // 写到 clipboard
            .addHandler(new InsertToClipboardHandler())
            .addHandler(new FinalChainHandler());

        // 开启后台任务
        new ActionTask(event.getProject(), MikBundle.message("mik.action.upload.process", cloudEnum.title), manager).queue();
    }
}
