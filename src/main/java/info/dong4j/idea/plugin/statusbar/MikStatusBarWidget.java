package info.dong4j.idea.plugin.statusbar;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.actionSystem.Separator;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.ui.popup.ListPopup;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.StatusBarWidget;
import com.intellij.openapi.wm.impl.status.EditorBasedStatusBarPopup;
import com.intellij.util.IconUtil;

import info.dong4j.idea.plugin.MikBundle;
import info.dong4j.idea.plugin.enums.CloudEnum;
import info.dong4j.idea.plugin.settings.MikPersistenComponent;
import info.dong4j.idea.plugin.settings.MikState;
import info.dong4j.idea.plugin.settings.OssState;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.Icon;

import icons.MikIcons;

/**
 * MIK 插件状态栏组件
 * <p>
 * 该组件用于在 IDE 状态栏中显示 MIK 插件的状态，并提供快速设置功能。
 * 包括全局开关、图片语法偏好、默认图床选择等功能。
 *
 * @author dong4j
 * @version 1.0.0
 * @date 2025.12.07
 * @since 2.2.0
 */
public class MikStatusBarWidget extends EditorBasedStatusBarPopup {
    /** Widget ID，用于唯一标识该组件 */
    public static final String ID = "MikStatusBarWidget";

    /**
     * 构造函数
     *
     * @param project 当前项目
     */
    public MikStatusBarWidget(@NotNull Project project) {
        super(project, false);
    }

    /**
     * 创建状态栏组件实例
     *
     * @param project 项目实例
     * @return 状态栏组件实例
     */
    @Override
    protected @NotNull StatusBarWidget createInstance(@NotNull Project project) {
        return new MikStatusBarWidget(project);
    }

    /**
     * 获取 Widget ID
     *
     * @return Widget 唯一标识符
     */
    @Override
    public @NotNull String ID() {
        return ID;
    }

    /**
     * 获取小部件状态
     * <p>
     * 根据插件启用状态显示不同的文本和图标
     *
     * @param file 虚拟文件，可为 null
     * @return 小部件状态对象
     */
    @Override
    protected @NotNull WidgetState getWidgetState(@Nullable VirtualFile file) {
        MikState state = MikPersistenComponent.getInstance().getState();

        String displayText;
        String tooltip;

        if (state.isEnablePlugin()) {
            displayText = " MIK: ON";
            tooltip = MikBundle.message("statusbar.tooltip.enabled");
        } else {
            displayText = " MIK: OFF";
            tooltip = MikBundle.message("statusbar.tooltip.disabled");
        }

        WidgetState widgetState = new WidgetState(tooltip, displayText, true);
        // 设置状态栏图标（缩放到适合状态栏的大小）
        widgetState.setIcon(scaleIconForStatusBar(MikIcons.MIK));
        return widgetState;
    }

    /**
     * 为状态栏缩放图标
     * <p>
     * 将图标缩放到适合状态栏显示的大小（13x13）
     *
     * @param icon 原始图标
     * @return 缩放后的图标
     */
    @Nullable
    private Icon scaleIconForStatusBar(@Nullable Icon icon) {
        if (icon == null) {
            return null;
        }
        // 状态栏图标通常使用 13x13 尺寸，将 16x16 的图标缩放到 13x13
        return IconUtil.scale(icon, null, 0.8125f);
    }

    /**
     * 创建弹出菜单
     * <p>
     * 当用户点击状态栏图标时显示的弹出菜单
     *
     * @param context 数据上下文
     * @return 弹出菜单
     */
    @Override
    protected @Nullable ListPopup createPopup(@NotNull DataContext context) {
        DefaultActionGroup group = new DefaultActionGroup();

        // 全局开关
        group.add(new TogglePluginAction());
        group.add(new Separator());

        // 图片语法偏好
        group.add(new ToggleRelativePathAction());
        group.add(new ToggleAddDotSlashAction());
        group.add(new ToggleAutoEscapeAction());
        group.add(new Separator());

        // 其他选项
        group.add(new ToggleConsoleLogAction());
        group.add(new TogglePastePlainTextAction());
        group.add(new Separator());

        // 默认图床选择
        group.add(new DefaultActionGroup("☁️ " + MikBundle.message("statusbar.default.cloud"), true) {
            {
                for (CloudEnum cloudEnum : CloudEnum.values()) {
                    add(new SelectDefaultCloudAction(cloudEnum));
                }
            }
        });

        group.add(new Separator());

        // 打开设置页面
        group.add(new OpenSettingsAction());

        return JBPopupFactory.getInstance().createActionGroupPopup(
            MikBundle.message("statusbar.popup.title"),
            group,
            context,
            JBPopupFactory.ActionSelectionAid.SPEEDSEARCH,
            true
                                                                  );
    }

    /**
     * 切换插件启用状态的 Action
     */
    private static class TogglePluginAction extends AnAction {
        public TogglePluginAction() {
            super(MikBundle.message("statusbar.toggle.plugin"), "", MikIcons.MIK);
        }

        @Override
        public void actionPerformed(@NotNull AnActionEvent e) {
            MikState state = MikPersistenComponent.getInstance().getState();
            state.setEnablePlugin(!state.isEnablePlugin());
            // 更新状态栏显示
            updateStatusBar(e.getProject());
        }

        @Override
        public void update(@NotNull AnActionEvent e) {
            MikState state = MikPersistenComponent.getInstance().getState();
            String text = state.isEnablePlugin()
                          ? MikBundle.message("statusbar.toggle.plugin.disable")
                          : MikBundle.message("statusbar.toggle.plugin.enable");
            e.getPresentation().setText(
                (state.isEnablePlugin() ? "🎉 " : "💤 ") + text
                                       );
        }

        @Override
        public @NotNull com.intellij.openapi.actionSystem.ActionUpdateThread getActionUpdateThread() {
            return com.intellij.openapi.actionSystem.ActionUpdateThread.BGT;
        }
    }

    /**
     * 切换相对路径偏好的 Action
     */
    private static class ToggleRelativePathAction extends AnAction {
        public ToggleRelativePathAction() {
            super(MikBundle.message("statusbar.toggle.relative.path"));
        }

        @Override
        public void actionPerformed(@NotNull AnActionEvent e) {
            MikState state = MikPersistenComponent.getInstance().getState();
            state.setPreferRelativePath(!state.isPreferRelativePath());
        }

        @Override
        public void update(@NotNull AnActionEvent e) {
            MikState state = MikPersistenComponent.getInstance().getState();
            e.getPresentation().setEnabled(state.isEnablePlugin());
            String text = MikBundle.message("statusbar.toggle.relative.path");
            if (state.isPreferRelativePath()) {
                text = "📂 ✓ " + text;
            } else {
                text = "📂 " + text;
            }
            e.getPresentation().setText(text);
        }

        @Override
        public @NotNull com.intellij.openapi.actionSystem.ActionUpdateThread getActionUpdateThread() {
            return com.intellij.openapi.actionSystem.ActionUpdateThread.BGT;
        }
    }

    /**
     * 切换添加 ./ 前缀的 Action
     */
    private static class ToggleAddDotSlashAction extends AnAction {
        public ToggleAddDotSlashAction() {
            super(MikBundle.message("statusbar.toggle.add.dot.slash"));
        }

        @Override
        public void actionPerformed(@NotNull AnActionEvent e) {
            MikState state = MikPersistenComponent.getInstance().getState();
            state.setAddDotSlash(!state.isAddDotSlash());
        }

        @Override
        public void update(@NotNull AnActionEvent e) {
            MikState state = MikPersistenComponent.getInstance().getState();
            // 只有在启用相对路径时才能启用
            e.getPresentation().setEnabled(state.isEnablePlugin() && state.isPreferRelativePath());
            String text = MikBundle.message("statusbar.toggle.add.dot.slash");
            if (state.isAddDotSlash()) {
                text = "🔗 ✓ " + text;
            } else {
                text = "🔗 " + text;
            }
            e.getPresentation().setText(text);
        }

        @Override
        public @NotNull com.intellij.openapi.actionSystem.ActionUpdateThread getActionUpdateThread() {
            return com.intellij.openapi.actionSystem.ActionUpdateThread.BGT;
        }
    }

    /**
     * 切换自动转义图片 URL 的 Action
     */
    private static class ToggleAutoEscapeAction extends AnAction {
        public ToggleAutoEscapeAction() {
            super(MikBundle.message("statusbar.toggle.auto.escape"));
        }

        @Override
        public void actionPerformed(@NotNull AnActionEvent e) {
            MikState state = MikPersistenComponent.getInstance().getState();
            state.setAutoEscapeImageUrl(!state.isAutoEscapeImageUrl());
        }

        @Override
        public void update(@NotNull AnActionEvent e) {
            MikState state = MikPersistenComponent.getInstance().getState();
            e.getPresentation().setEnabled(state.isEnablePlugin());
            String text = MikBundle.message("statusbar.toggle.auto.escape");
            if (state.isAutoEscapeImageUrl()) {
                text = "🔐 ✓ " + text;
            } else {
                text = "🔐 " + text;
            }
            e.getPresentation().setText(text);
        }

        @Override
        public @NotNull com.intellij.openapi.actionSystem.ActionUpdateThread getActionUpdateThread() {
            return com.intellij.openapi.actionSystem.ActionUpdateThread.BGT;
        }
    }

    /**
     * 选择默认图床的 Action
     */
    private static class SelectDefaultCloudAction extends AnAction {
        private final CloudEnum cloudEnum;

        public SelectDefaultCloudAction(CloudEnum cloudEnum) {
            super(cloudEnum.getTitle(), "", getCloudIcon(cloudEnum));
            this.cloudEnum = cloudEnum;
        }

        @Override
        public void actionPerformed(@NotNull AnActionEvent e) {
            MikState state = MikPersistenComponent.getInstance().getState();
            state.setDefaultCloudType(cloudEnum.getIndex());
            state.setDefaultCloudCheck(true);
        }

        @Override
        public void update(@NotNull AnActionEvent e) {
            MikState state = MikPersistenComponent.getInstance().getState();
            e.getPresentation().setEnabled(state.isEnablePlugin());

            // 检查是否是当前选中的图床
            CloudEnum currentCloud = OssState.getCloudType(state.getDefaultCloudType());
            if (currentCloud == cloudEnum) {
                e.getPresentation().setText("✓ " + cloudEnum.getTitle());
            } else {
                e.getPresentation().setText(cloudEnum.getTitle());
            }
        }

        @Override
        public @NotNull com.intellij.openapi.actionSystem.ActionUpdateThread getActionUpdateThread() {
            return com.intellij.openapi.actionSystem.ActionUpdateThread.BGT;
        }
    }

    /**
     * 切换 Console 日志输出的 Action
     */
    private static class ToggleConsoleLogAction extends AnAction {
        public ToggleConsoleLogAction() {
            super(MikBundle.message("statusbar.toggle.console.log"));
        }

        @Override
        public void actionPerformed(@NotNull AnActionEvent e) {
            MikState state = MikPersistenComponent.getInstance().getState();
            state.setEnableConsoleLog(!state.isEnableConsoleLog());
        }

        @Override
        public void update(@NotNull AnActionEvent e) {
            MikState state = MikPersistenComponent.getInstance().getState();
            e.getPresentation().setEnabled(state.isEnablePlugin());
            String text = MikBundle.message("statusbar.toggle.console.log");
            if (state.isEnableConsoleLog()) {
                text = "📝 ✓ " + text;
            } else {
                text = "📝 " + text;
            }
            e.getPresentation().setText(text);
        }

        @Override
        public @NotNull com.intellij.openapi.actionSystem.ActionUpdateThread getActionUpdateThread() {
            return com.intellij.openapi.actionSystem.ActionUpdateThread.BGT;
        }
    }

    /**
     * 切换粘贴文件为纯文本的 Action
     */
    private static class TogglePastePlainTextAction extends AnAction {
        public TogglePastePlainTextAction() {
            super(MikBundle.message("statusbar.toggle.paste.plain.text"));
        }

        @Override
        public void actionPerformed(@NotNull AnActionEvent e) {
            MikState state = MikPersistenComponent.getInstance().getState();
            state.setPasteFileAsPlainText(!state.isPasteFileAsPlainText());
        }

        @Override
        public void update(@NotNull AnActionEvent e) {
            MikState state = MikPersistenComponent.getInstance().getState();
            e.getPresentation().setEnabled(state.isEnablePlugin());
            String text = MikBundle.message("statusbar.toggle.paste.plain.text");
            if (state.isPasteFileAsPlainText()) {
                text = "📄 ✓ " + text;
            } else {
                text = "📄 " + text;
            }
            e.getPresentation().setText(text);
        }

        @Override
        public @NotNull com.intellij.openapi.actionSystem.ActionUpdateThread getActionUpdateThread() {
            return com.intellij.openapi.actionSystem.ActionUpdateThread.BGT;
        }
    }

    /**
     * 打开 MIK 设置页面的 Action
     */
    private static class OpenSettingsAction extends AnAction {
        public OpenSettingsAction() {
            super("⚙️ " + MikBundle.message("statusbar.open.settings"));
        }

        @Override
        public void actionPerformed(@NotNull AnActionEvent e) {
            Project project = e.getProject();
            if (project != null) {
                com.intellij.openapi.options.ShowSettingsUtil.getInstance()
                    .showSettingsDialog(project, "Markdown Image Kit");
            }
        }

        @Override
        public void update(@NotNull AnActionEvent e) {
            e.getPresentation().setEnabled(e.getProject() != null);
        }

        @Override
        public @NotNull com.intellij.openapi.actionSystem.ActionUpdateThread getActionUpdateThread() {
            return com.intellij.openapi.actionSystem.ActionUpdateThread.BGT;
        }
    }

    /**
     * 根据云服务类型获取对应的图标
     *
     * @param cloudEnum 云服务类型
     * @return 对应的图标（经过缩放处理）
     */
    private static Icon getCloudIcon(CloudEnum cloudEnum) {
        Icon icon = switch (cloudEnum) {
            case SM_MS_CLOUD -> MikIcons.SM_MS;
            case ALIYUN_CLOUD -> MikIcons.ALIYUN_OSS;
            case QINIU_CLOUD -> MikIcons.QINIU_OSS;
            case TENCENT_CLOUD -> MikIcons.TENCENT;
            case BAIDU_CLOUD -> MikIcons.BAIDU;
            case GITHUB -> MikIcons.GITHUB;
            case GITEE -> MikIcons.GITEE;
            case CUSTOMIZE -> MikIcons.CUSTOM;
            case PICLIST -> MikIcons.PICLIST;
        };

        // 缩放图标，使其适合菜单显示（通常为原大小的 81.25%）
        return IconUtil.scale(icon, null, 0.8125f);
    }

    /**
     * 更新状态栏显示
     * <p>
     * 在设置改变后调用此方法，可以立即更新状态栏的显示内容。
     * 使用 invokeLater 确保更新在状态持久化之后执行。
     *
     * @param project 项目实例，如果为 null 则不执行更新
     */
    private static void updateStatusBar(@Nullable Project project) {
        if (project == null) {
            return;
        }

        // 使用 invokeLater 延迟更新，确保状态已经被持久化
        // 同时确保在正确的模态上下文中执行
        com.intellij.openapi.application.ApplicationManager.getApplication().invokeLater(() -> {
            if (project.isDisposed()) {
                return;
            }

            com.intellij.openapi.wm.StatusBar statusBar =
                com.intellij.openapi.wm.WindowManager.getInstance().getStatusBar(project);

            if (statusBar != null) {
                statusBar.updateWidget(ID);
            }
        }, com.intellij.openapi.application.ModalityState.defaultModalityState());
    }
}

