package info.dong4j.idea.plugin.chain;

import info.dong4j.idea.plugin.entity.EventData;
import info.dong4j.idea.plugin.entity.MarkdownImage;

import java.util.Iterator;

/**
 * 基础动作处理器类
 * <p>
 * 该类作为所有具体动作处理器的抽象基类，定义了动作处理器的基本行为和接口。
 * 提供了判断是否启用处理、执行处理逻辑以及子类实现具体处理逻辑的抽象方法。
 * 适用于需要统一处理事件数据的场景，支持扩展和自定义处理逻辑。
 *
 * @author dong4j
 * @version 0.0.1
 * @date 2021.02.14
 * @since 0.0.1
 */
public abstract class BaseActionHandler implements IActionHandler {
    /**
     * 判断该处理类是否适用于给定的事件数据
     * <p>
     * 该方法用于确定当前处理类是否能够处理传入的事件数据。默认返回 true，表示可以处理。
     *
     * @param data 事件数据对象
     * @return 如果符合处理范围则返回 true，否则返回 false
     */
    @Override
    public boolean isEnabled(EventData data){
        return true;
    }

    /**
     * 执行具体的处理逻辑
     * <p>
     * 该方法用于执行事件处理的核心逻辑，返回是否阻止系统的事件传递。
     *
     * @param data 事件数据对象，包含事件相关的信息
     * @return 是否阻止系统的事件传递，返回 true 表示阻止，false 表示允许传递
     */
    @Override
    public boolean execute(EventData data){
        return true;
    }

    /**
     * 子类实现具体逻辑
     * <p>
     * 此方法为抽象方法，要求子类实现具体的业务逻辑。
     *
     * @param data          事件数据对象
     * @param imageIterator 图片迭代器，用于遍历Markdown图片集合
     * @param markdownImage 当前处理的Markdown图片对象
     * @since 0.0.1
     */
    abstract void invoke(EventData data, Iterator<MarkdownImage> imageIterator, MarkdownImage markdownImage);
}
