package info.dong4j.idea.plugin.watch;

import info.dong4j.idea.plugin.settings.ImageManagerPersistenComponent;
import info.dong4j.idea.plugin.settings.ImageManagerState;

/**
 * <p>Company: 科大讯飞股份有限公司-四川分公司</p>
 * <p>Description: </p>
 *
 * @author dong4j
 * @email sjdong3 @iflytek.com
 * @since 2019 -03-22 18:35
 */
public interface IActionHandler {
    ImageManagerState STATE = ImageManagerPersistenComponent.getInstance().getState();

    /**
     * 是否符合该处理类的处理范围
     *
     * @return 是否符合
     */
    boolean isEnabled();

    /**
     * 执行具体的处理逻辑
     *
     * @return 是否阻止系统的事件传递 boolean
     */
    boolean execute();
}
