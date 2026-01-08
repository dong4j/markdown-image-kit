package info.dong4j.idea.plugin.chain.handler;

import com.intellij.openapi.editor.Document;

import info.dong4j.idea.plugin.chain.BaseActionHandler;
import info.dong4j.idea.plugin.chain.ProgressTracker;
import info.dong4j.idea.plugin.console.MikConsoleView;
import info.dong4j.idea.plugin.entity.EventData;
import info.dong4j.idea.plugin.entity.MarkdownImage;

import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;

/**
 * ActionHandlerAdapter
 * <p>
 * 该类是一个动作处理器的适配器，用于处理特定的事件数据，执行相关操作。
 * 主要用于扩展和自定义动作处理逻辑，支持通过继承并重写方法来实现具体业务。
 * 在执行过程中，会更新进度指示器，提供处理进度反馈。
 *
 * @author dong4j
 * @version 0.0.1
 * @date 2021.02.14
 * @since 0.0.1
 */
@Slf4j
public class ActionHandlerAdapter extends BaseActionHandler {
    /**
     * 获取名称
     * <p>
     * 返回当前对象的名称信息
     *
     * @return 名称字符串
     * @since 0.0.1
     */
    @Override
    public String getName() {
        return "";
    }

    /**
     * 过滤文件
     * <p>
     * 判断给定的文件是否应该被处理。子类可以重写此方法实现自定义的过滤逻辑。
     * <p>
     * 例如：
     * <ul>
     *   <li>图片压缩时过滤掉 svg 和 gif 格式</li>
     *   <li>图片上传时过滤掉小于 1KB 或大于 5MB 的文件</li>
     * </ul>
     *
     * @param markdownImage Markdown图片对象
     * @return 如果文件应该被处理返回 true，否则返回 false
     * @since 2.2.0
     */
    protected boolean shouldProcess(@NotNull MarkdownImage markdownImage) {
        // 默认实现：处理所有文件
        return true;
    }

    /**
     * 执行处理操作，遍历等待处理的图片数据并逐个处理
     * <p>
     * 该方法从EventData中获取进度跟踪器，然后遍历等待处理的图片集合，对每张图片进行处理，并更新进度信息。
     * 使用统一的进度跟踪机制，提供更准确的进度展示。
     *
     * @param data 包含处理所需数据的EventData对象
     * @return 始终返回true，表示处理成功完成
     * @since 0.0.1
     */
    @Override
    public boolean execute(EventData data) {
        ProgressTracker progressTracker = data.getProgressTracker();
        int stepIndex = data.getIndex();

        // 统计总图片数
        int totalCount = data.getWaitingProcessMap().values().stream()
            .mapToInt(List::size)
            .sum();

        if (totalCount == 0) {
            log.trace("没有待处理的图片数据");
            return true;
        }

        // 遍历处理每张图片
        int processedCount = 0;
        for (Map.Entry<Document, List<MarkdownImage>> imageEntry : data.getWaitingProcessMap().entrySet()) {
            Iterator<MarkdownImage> imageIterator = imageEntry.getValue().iterator();
            while (imageIterator.hasNext()) {
                MarkdownImage markdownImage = imageIterator.next();
                processedCount++;

                // 使用 ProgressTracker 更新进度
                if (progressTracker != null) {
                    progressTracker.updateItemProgress(stepIndex, markdownImage.getImageName(), processedCount, totalCount);
                }

                if (!this.shouldProcess(markdownImage)) {
                    log.trace("[{}:{}] 跳过图片: {}", data.getAction(), getName(), markdownImage.getImageName());
                    continue;
                }

                try {
                    this.invoke(data, imageIterator, markdownImage);
                } catch (Exception e) {
                    log.debug("处理图片失败: {}", markdownImage.getImageName(), e);
                    MikConsoleView.printErrorMessage(data.getProject(),
                                                     "[✗] 处理图片失败: " + markdownImage.getImageName() + " (" + e.getMessage() + ")");
                }
            }
        }

        return true;
    }

    /**
     * 执行特定逻辑
     * <p>
     * 该方法用于执行与事件数据、图片迭代器和Markdown图片相关的特定处理逻辑。
     *
     * @param data          事件数据对象
     * @param imageIterator 图片迭代器，用于遍历Markdown图片
     * @param markdownImage Markdown图片对象
     */
    @Override
    public void invoke(EventData data, Iterator<MarkdownImage> imageIterator, MarkdownImage markdownImage) {
        log.trace("执行特定逻辑");
    }
}
