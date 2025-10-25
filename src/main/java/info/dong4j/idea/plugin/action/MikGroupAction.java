package info.dong4j.idea.plugin.action;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DefaultActionGroup;

import org.jetbrains.annotations.NotNull;

/**
 * <p>Description: 通过代码的方式添加 Group</p>
 * <a href="http://www.jetbrains.org/intellij/sdk/docs/tutorials/action_system/grouping_action.html">...</a>
 * Providing specific behaviour for the group
 * Registering a variable action group
 *
 * @author dong4j
 * @version 0.0.1
 * @email "mailto:dong4j@gmail.com"
 * @date 2021.02.14 18:40
 * @since 0.0.1
 */
public final class MikGroupAction extends DefaultActionGroup {
    /**
     * Update
     *
     * @param event event
     * @since 0.0.1
     */
    @Override
    public void update(@NotNull AnActionEvent event) {
        event.getPresentation().setIcon(AllIcons.Gutter.Colors);
    }
}
