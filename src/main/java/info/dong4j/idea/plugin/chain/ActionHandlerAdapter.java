package info.dong4j.idea.plugin.chain;

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.progress.ProgressIndicator;

import info.dong4j.idea.plugin.MikBundle;
import info.dong4j.idea.plugin.entity.EventData;
import info.dong4j.idea.plugin.entity.MarkdownImage;

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
     * 执行处理操作，遍历等待处理的图片数据并逐个处理
     * <p>
     * 该方法从EventData中获取进度指示器和数据大小，然后遍历等待处理的图片集合，对每张图片进行处理，并更新进度信息。
     *
     * @param data 包含处理所需数据的EventData对象
     * @return 始终返回true，表示处理成功完成
     * @since 0.0.1
     */
    @Override
    public boolean execute(EventData data) {
        ProgressIndicator indicator = data.getIndicator();
        int size = data.getSize();
        int totalProcessed = 0;

        for (Map.Entry<Document, List<MarkdownImage>> imageEntry : data.getWaitingProcessMap().entrySet()) {
            int totalCount = imageEntry.getValue().size();
            Iterator<MarkdownImage> imageIterator = imageEntry.getValue().iterator();
            while (imageIterator.hasNext()) {
                MarkdownImage markdownImage = imageIterator.next();

                indicator.setText2(MikBundle.message("mik.action.processing.title", markdownImage.getImageName()));
                indicator.setFraction(((++totalProcessed * 1.0) + data.getIndex() * size) / totalCount * size);

                this.invoke(data, imageIterator, markdownImage);
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
    protected void invoke(EventData data, Iterator<MarkdownImage> imageIterator, MarkdownImage markdownImage) {
        log.trace("执行特定逻辑");
    }
}
