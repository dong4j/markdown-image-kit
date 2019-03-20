package info.dong4j.idea.plugin.action;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DefaultActionGroup;

import org.jetbrains.annotations.NotNull;

/**
 * <p>Company: 科大讯飞股份有限公司-四川分公司</p>
 * <p>Description: </p>
 * http://www.jetbrains.org/intellij/sdk/docs/tutorials/action_system/grouping_action.html #Providing specific behaviour for the group
 * # Registering a variable action group
 *
 * @author dong4j
 * @email sjdong3@iflytek.com
 * @since 2019-03-20 06:16
 */
public class UploadGroupAction extends DefaultActionGroup {
    @Override
    public void update(@NotNull AnActionEvent event) {
        event.getPresentation().setIcon(AllIcons.Gutter.Colors);
    }
}
