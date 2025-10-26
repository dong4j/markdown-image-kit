package info.dong4j.idea.plugin.chain;

import info.dong4j.idea.plugin.entity.EventData;
import info.dong4j.idea.plugin.settings.MikPersistenComponent;
import info.dong4j.idea.plugin.settings.MikState;

/**
 * 事件处理接口
 * <p>
 * 定义事件处理类的核心方法，用于判断当前处理类是否适用于给定的数据，并执行具体的处理逻辑。
 * 该接口支持链式处理模式，允许多个处理类按顺序执行，其中任何一个处理类返回 false 时，整个链式处理将中断。
 * </p>
 *
 * @author dong4j
 * @version 0.0.1
 * @date 2021.02.14
 * @since 0.0.1
 */
public interface IActionHandler {
    /** 当前组件状态常量 */
    MikState STATE = MikPersistenComponent.getInstance().getState();

    /**
     * 获取名称
     *
     * @return 名称
     * @since 0.0.1
     */
    String getName();

    /**
     * 判断当前处理器是否适用于给定的数据
     * <p>
     * 该方法用于确定事件数据是否在当前处理器的处理范围内，若返回 false，则表示当前处理器不执行。
     *
     * @param data 事件数据对象
     * @return 如果数据符合处理范围则返回 true，否则返回 false
     * @since 0.0.1
     */
    boolean isEnabled(EventData data);

    /**
     * 执行具体的处理逻辑
     * <p>
     * 该方法用于处理传入的事件数据，返回值表示是否阻止系统的事件传递。若返回 false，则后续的 handler 不会执行，整个链式处理中断。
     *
     * @param data 事件数据
     * @return 是否阻止系统的事件传递。若为 false，后续 handler 不执行，整个 chain 中断
     * @since 0.0.1
     */
    boolean execute(EventData data);
}
