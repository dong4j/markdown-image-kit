package info.dong4j.idea.plugin.statusbar;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.StatusBar;
import com.intellij.openapi.wm.StatusBarWidget;
import com.intellij.openapi.wm.StatusBarWidgetFactory;

import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

/**
 * MIK 插件状态栏组件工厂
 * <p>
 * 用于创建和管理 MIK 插件的状态栏组件
 *
 * @author dong4j
 * @version 1.0.0
 * @date 2025.12.07
 * @since 2.2.0
 */
public class MikStatusBarWidgetFactory implements StatusBarWidgetFactory {

    /**
     * 获取 Widget ID
     *
     * @return Widget 唯一标识符
     */
    @Override
    public @NonNls @NotNull String getId() {
        return MikStatusBarWidget.ID;
    }

    /**
     * 获取显示名称
     *
     * @return 显示名称
     */
    @Override
    public @Nls @NotNull String getDisplayName() {
        return "Markdown Image Kit";
    }

    /**
     * 是否默认可用
     *
     * @return true 表示默认可用
     */
    @Override
    public boolean isAvailable(@NotNull Project project) {
        return true;
    }

    /**
     * 创建 Widget 实例
     *
     * @param project 当前项目
     * @return Widget 实例
     */
    @Override
    public @NotNull StatusBarWidget createWidget(@NotNull Project project) {
        return new MikStatusBarWidget(project);
    }

    /**
     * 释放 Widget 资源
     *
     * @param widget Widget 实例
     */
    @Override
    public void disposeWidget(@NotNull StatusBarWidget widget) {
        widget.dispose();
    }

    /**
     * 是否可以在状态栏中启用
     *
     * @param statusBar 状态栏实例
     * @return true 表示可以启用
     */
    @Override
    public boolean canBeEnabledOn(@NotNull StatusBar statusBar) {
        return true;
    }
}

