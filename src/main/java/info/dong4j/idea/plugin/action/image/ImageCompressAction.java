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
 * <p>Description: 图像压缩 </p>
 *
 * @author dong4j
 * @version 0.0.1
 * @email "mailto:dong4j@gmail.com"
 * @date 2021.02.14 18:40
 * @since 0.0.1
 */
@Slf4j
public final class ImageCompressAction extends ImageActionBase {

    /**
     * Gets icon *
     *
     * @return the icon
     * @since 0.0.1
     */
    @Contract(pure = true)
    @Override
    protected Icon getIcon() {
        return AllIcons.Debugger.ShowCurrentFrame;
    }

    /**
     * Build chain
     *
     * @param event             event
     * @param waitingProcessMap waiting process map
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
                @Override
                public String getName() {
                    return "替换原图";
                }

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
