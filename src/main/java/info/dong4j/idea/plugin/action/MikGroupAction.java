package info.dong4j.idea.plugin.action;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DefaultActionGroup;

import org.jetbrains.annotations.NotNull;

/**
 * 用于通过代码方式添加 Group 的动作类
 * <p>
 * 该类继承自 DefaultActionGroup，用于提供特定的行为定义，并注册变量动作组。
 * 主要用于在 IntelliJ IDEA 插件开发中实现自定义动作组的逻辑处理。
 *
 * @author dong4j
 * @version 0.0.1
 * @email mailto:dong4j@gmail.com
 * @date 2021.02.14
 * @since 0.0.1
 */
public final class MikGroupAction extends DefaultActionGroup {
    /**
     * 更新操作，设置动作的图标
     * <p>
     * 该方法用于在更新动作时设置其图标为指定的图标资源。
     *
     * @param event 动作事件对象，包含动作执行的相关信息
     * @since 0.0.1
     */
    @Override
    public void update(@NotNull AnActionEvent event) {
        event.getPresentation().setIcon(AllIcons.Gutter.Colors);
    }
}
