package info.dong4j.idea.plugin.chain;

import info.dong4j.idea.plugin.entity.EventData;
import info.dong4j.idea.plugin.settings.MikPersistenComponent;
import info.dong4j.idea.plugin.settings.MikState;

/**
* <p>Description: </p>
 *
 * @author dong4j
 * @version 0.0.1
 * @email "mailto:dong4j@gmail.com"
 * @date 2021.02.14 18:40
 * @since 0.0.1
 */
public interface IActionHandler {
    /**
     * The constant STATE.
     */
    MikState STATE = MikPersistenComponent.getInstance().getState();

    /**
     * Gets name.
     *
     * @return the name
     * @since 0.0.1
     */
    String getName();

    /**
     * 是否符合该处理类的处理范围
     *
     * @param data the data
     * @return 是否符合 boolean false 则当前 handler 不执行
     * @since 0.0.1
     */
    boolean isEnabled(EventData data);

    /**
     * 执行具体的处理逻辑
     *
     * @param data the data
     * @return 是否阻止系统的事件传递 boolean  为 false 时后一个 handler 不执行, 整个 chain 中断
     * @since 0.0.1
     */
    boolean execute(EventData data);
}
