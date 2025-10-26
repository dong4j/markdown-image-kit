package info.dong4j.idea.plugin.action.image;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.externalSystem.task.TaskCallbackAdapter;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.VirtualFileManager;

import info.dong4j.idea.plugin.MikBundle;
import info.dong4j.idea.plugin.chain.ActionHandlerAdapter;
import info.dong4j.idea.plugin.chain.ActionManager;
import info.dong4j.idea.plugin.chain.FinalChainHandler;
import info.dong4j.idea.plugin.chain.ImageCompressionHandler;
import info.dong4j.idea.plugin.chain.ImageRenameHandler;
import info.dong4j.idea.plugin.entity.EventData;
import info.dong4j.idea.plugin.entity.MarkdownImage;
import info.dong4j.idea.plugin.task.ActionTask;

import org.jetbrains.annotations.Contract;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.Icon;

import lombok.extern.slf4j.Slf4j;

/**
 * 图像压缩操作类
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
     * 返回当前类所使用的图标
     *
     * @return 图标对象
     * @since 0.0.1
     */
    @Contract(pure = true)
    @Override
    protected Icon getIcon() {
        return AllIcons.Debugger.ShowCurrentFrame;
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
            .setActionEvent(event)
            .setProject(event.getProject())
            .setWaitingProcessMap(waitingProcessMap);

        ActionManager manager = new ActionManager(data)
            // 图片压缩
            .addHandler(new ImageCompressionHandler())
            // 图片重命名
            .addHandler(new ImageRenameHandler())
            // 替换
            .addHandler(new ActionHandlerAdapter() {
                /**
                 * 获取名称信息
                 * <p>
                 * 返回一个固定的名称字符串"替换原图"
                 *
                 * @return 名称字符串
                 */
                @Override
                public String getName() {
                    return "替换原图";
                }

                /**
                 * 处理Markdown图片的回调方法，将图片流写入指定路径
                 * <p>
                 * 该方法接收事件数据、图片迭代器和具体的Markdown图片对象，通过读取图片流并写入文件系统实现图片存储
                 *
                 * @param data          事件数据
                 * @param imageIterator 图片迭代器
                 * @param markdownImage 具体的Markdown图片对象
                 */
                @Override
                public void invoke(EventData data, Iterator<MarkdownImage> imageIterator, MarkdownImage markdownImage) {
                    InputStream inputStream = markdownImage.getInputStream();
                    try {
                        FileUtil.copy(inputStream, new FileOutputStream(markdownImage.getPath()));
                    } catch (IOException e) {
                        log.trace("", e);
                    }
                }
            })
            .addHandler(new FinalChainHandler())
            // 处理完成后刷新 VFS
            .addCallback(new TaskCallbackAdapter() {
                /**
                 * 处理成功回调
                 * <p>
                 * 在异步操作成功时执行，用于记录日志并刷新 VFS，确保新增的图片能够及时显示
                 *
                 * @author [作者姓名]
                 * @since 1.0
                 */
                @Override
                public void onSuccess() {
                    log.trace("Success callback");
                    // 刷新 VFS, 避免新增的图片很久才显示出来
                    ApplicationManager.getApplication().runWriteAction(() -> {
                        VirtualFileManager.getInstance().syncRefresh();
                    });
                }
            });

        // 开启后台任务
        new ActionTask(event.getProject(), MikBundle.message("mik.action.compress.progress"), manager).queue();
    }
}
