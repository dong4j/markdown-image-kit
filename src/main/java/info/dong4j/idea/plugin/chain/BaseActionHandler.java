package info.dong4j.idea.plugin.chain;

import info.dong4j.idea.plugin.entity.EventData;
import info.dong4j.idea.plugin.entity.MarkdownImage;

import java.util.Iterator;

/**
 * <p>Description: </p>
 *
 * @author dong4j
 * @version 0.0.1
 * @email "mailto:dong4j@gmail.com"
 * @date 2021.02.14 18:40
 * @since 0.0.1
 */
public abstract class BaseActionHandler implements IActionHandler {
    /**
     * 是否符合该处理类的处理范围
     *
     * @param data the data
     * @return 是否符合 boolean
     * @since 0.0.1
     */
    @Override
    public boolean isEnabled(EventData data){
        return true;
    }

    /**
     * 执行具体的处理逻辑
     *
     * @param data the data
     * @return 是否阻止系统的事件传递 boolean
     * @since 0.0.1
     */
    @Override
    public boolean execute(EventData data){
        return true;
    }

    /**
     * 子类实现具体逻辑
     *
     * @param data          the data
     * @param imageIterator the image iterator
     * @param markdownImage the markdown image
     * @since 0.0.1
     */
    abstract void invoke(EventData data, Iterator<MarkdownImage> imageIterator, MarkdownImage markdownImage);
}
