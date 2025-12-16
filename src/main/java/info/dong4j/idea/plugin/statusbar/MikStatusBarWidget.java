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
import info.dong4j.idea.plugin.enums.ImageEditorEnum;
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

        // 图片编辑器选择
        group.add(new DefaultActionGroup("🖼️ " + MikBundle.message("panel.image.processing.enable.image.editor"), true) {
            {
                for (ImageEditorEnum editorEnum : ImageEditorEnum.values()) {
                    add(new SelectImageEditorAction(editorEnum));
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
        /**
         * 构造函数, 用于初始化 TogglePluginAction 实例
         * <p> 设置该操作的名称, 描述和图标, 用于在状态栏中显示插件切换功能
         *
         */
        public TogglePluginAction() {
            super(MikBundle.message("statusbar.toggle.plugin"), "", MikIcons.MIK);
        }

        /**
         * 执行切换插件状态的操作
         * <p> 获取当前插件状态, 将其取反并更新状态栏显示
         *
         * @param e Action 事件对象
         */
        @Override
        public void actionPerformed(@NotNull AnActionEvent e) {
            MikState state = MikPersistenComponent.getInstance().getState();
            state.setEnablePlugin(!state.isEnablePlugin());
            // 更新状态栏显示
            updateStatusBar(e.getProject());
        }

        /**
         * 更新操作界面的显示状态
         * <p> 根据插件的启用状态更新操作按钮的文本和图标, 用于在状态栏中显示插件的启用或禁用状态.
         *
         * @param e ActionEvent 对象, 包含操作事件的相关信息
         */
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

        /**
         * 指定此操作的更新线程
         * <p> 返回用于更新操作状态的线程类型, 此处指定为后台线程 (BGT).
         *
         * @return 返回 {@link com.intellij.openapi.actionSystem.ActionUpdateThread#BGT} 表示后台线程
         */
        @Override
        public @NotNull com.intellij.openapi.actionSystem.ActionUpdateThread getActionUpdateThread() {
            return com.intellij.openapi.actionSystem.ActionUpdateThread.BGT;
        }
    }

    /**
     * 切换相对路径偏好的 Action
     */
    private static class ToggleRelativePathAction extends AnAction {
        /**
         * 构造函数, 用于初始化 ToggleRelativePathAction 实例
         * <p> 设置该操作的显示名称, 名称来源于资源文件中的对应键值
         *
         */
        public ToggleRelativePathAction() {
            super(MikBundle.message("statusbar.toggle.relative.path"));
        }

        /**
         * 执行切换相对路径偏好的操作
         * <p> 获取插件状态并切换用户是否偏好使用相对路径的设置
         *
         * @param e Action 事件对象
         */
        @Override
        public void actionPerformed(@NotNull AnActionEvent e) {
            MikState state = MikPersistenComponent.getInstance().getState();
            state.setPreferRelativePath(!state.isPreferRelativePath());
        }

        /**
         * 更新操作按钮的状态和显示文本
         * <p> 根据插件状态启用情况设置按钮是否可用, 并根据相对路径偏好设置修改按钮显示文本.
         *
         * @param e ActionEvent 对象, 包含操作事件信息
         */
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

        /**
         * 指定此操作的更新线程
         * <p> 返回用于更新操作界面的线程类型, 此处指定为后台线程 (BGT).
         *
         * @return 返回 {@link com.intellij.openapi.actionSystem.ActionUpdateThread#BGT} 表示在后台线程中更新
         */
        @Override
        public @NotNull com.intellij.openapi.actionSystem.ActionUpdateThread getActionUpdateThread() {
            return com.intellij.openapi.actionSystem.ActionUpdateThread.BGT;
        }
    }

    /**
     * 切换添加 ./ 前缀的 Action
     */
    private static class ToggleAddDotSlashAction extends AnAction {
        /**
         * 构造函数, 用于初始化 ToggleAddDotSlashAction 实例
         * <p> 使用指定的文本初始化动作, 该文本来自资源文件中的国际化消息
         *
         */
        public ToggleAddDotSlashAction() {
            super(MikBundle.message("statusbar.toggle.add.dot.slash"));
        }

        /**
         * 切换添加点斜线的功能状态
         * <p> 在动作触发时, 获取当前状态并翻转添加点斜线的布尔值, 从而切换该功能的启用状态.
         *
         * @param e 动作事件对象, 包含触发此动作的相关信息
         */
        @Override
        public void actionPerformed(@NotNull AnActionEvent e) {
            MikState state = MikPersistenComponent.getInstance().getState();
            state.setAddDotSlash(!state.isAddDotSlash());
        }

        /**
         * 更新操作按钮的状态和显示文本
         * <p> 根据插件状态和路径偏好设置, 启用或禁用按钮, 并根据是否添加了点斜线前缀来更新按钮文本.
         *
         * @param e ActionEvent 对象, 包含操作事件信息
         */
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

        /**
         * 指定此操作的更新线程
         * <p> 返回用于更新操作界面的线程类型, 此处指定为后台线程 (BGT).
         *
         * @return 返回 {@link com.intellij.openapi.actionSystem.ActionUpdateThread#BGT} 表示在后台线程中更新
         */
        @Override
        public @NotNull com.intellij.openapi.actionSystem.ActionUpdateThread getActionUpdateThread() {
            return com.intellij.openapi.actionSystem.ActionUpdateThread.BGT;
        }
    }

    /**
     * 切换自动转义图片 URL 的 Action
     */
    private static class ToggleAutoEscapeAction extends AnAction {
        /**
         * 构造函数, 用于初始化 ToggleAutoEscapeAction 实例
         * <p> 使用指定的文本初始化该操作, 该文本通常用于状态栏显示
         *
         */
        public ToggleAutoEscapeAction() {
            super(MikBundle.message("statusbar.toggle.auto.escape"));
        }

        /**
         * 执行切换自动转义图片 URL 的功能
         * <p> 获取插件状态并切换自动转义图片 URL 的设置值
         *
         * @param e Action 事件对象
         */
        @Override
        public void actionPerformed(@NotNull AnActionEvent e) {
            MikState state = MikPersistenComponent.getInstance().getState();
            state.setAutoEscapeImageUrl(!state.isAutoEscapeImageUrl());
        }

        /**
         * 更新操作按钮的状态和显示文本
         * <p> 根据插件状态和自动转义图片 URL 的设置, 启用或禁用按钮, 并更新按钮显示文本以反映当前设置.
         *
         * @param e ActionEvent 对象, 包含与操作相关的事件数据
         */
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

        /**
         * 指定此操作的更新线程
         * <p> 返回用于更新操作界面的线程类型, 此处指定为后台线程 (BGT).
         *
         * @return 返回 {@link com.intellij.openapi.actionSystem.ActionUpdateThread#BGT} 表示在后台线程中更新
         */
        @Override
        public @NotNull com.intellij.openapi.actionSystem.ActionUpdateThread getActionUpdateThread() {
            return com.intellij.openapi.actionSystem.ActionUpdateThread.BGT;
        }
    }

    /**
     * 选择默认图床的 Action
     */
    private static class SelectDefaultCloudAction extends AnAction {
        /**
         * 表示当前操作的云服务类型
         * <p> 用于标识该动作对应的云平台, 如阿里云, 腾讯云等
         */
        private final CloudEnum cloudEnum;

        /**
         * 构造一个用于选择默认云服务的操作
         * <p> 初始化该操作时设置其显示名称, 描述和图标, 并保存对应的云服务枚举值
         *
         * @param cloudEnum 对应的云服务枚举值
         */
        public SelectDefaultCloudAction(CloudEnum cloudEnum) {
            super(cloudEnum.getTitle(), "", getCloudIcon(cloudEnum));
            this.cloudEnum = cloudEnum;
        }

        /**
         * 执行设置默认云类型的动作
         * <p> 获取当前状态并设置默认云类型及默认云检查标志为 true.
         *
         * @param e ActionEvent 对象, 包含触发此动作的事件信息
         */
        @Override
        public void actionPerformed(@NotNull AnActionEvent e) {
            MikState state = MikPersistenComponent.getInstance().getState();
            state.setDefaultCloudType(cloudEnum.getIndex());
            state.setDefaultCloudCheck(true);
        }

        /**
         * 更新操作的呈现状态
         * <p> 根据插件是否启用和当前默认云类型, 设置操作的文本和启用状态.
         *
         * @param e ActionEvent 对象, 包含操作的上下文信息
         */
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

        /**
         * 指定此操作的更新线程
         * <p> 返回用于更新操作界面的线程类型, 此处指定为后台线程 (BGT).
         *
         * @return 返回 {@link com.intellij.openapi.actionSystem.ActionUpdateThread#BGT} 表示后台线程
         */
        @Override
        public @NotNull com.intellij.openapi.actionSystem.ActionUpdateThread getActionUpdateThread() {
            return com.intellij.openapi.actionSystem.ActionUpdateThread.BGT;
        }
    }

    /**
     * 切换 Console 日志输出的 Action
     */
    private static class ToggleConsoleLogAction extends AnAction {
        /**
         * 构造函数, 用于初始化 ToggleConsoleLogAction 实例
         * <p> 使用指定的国际化消息作为操作名称初始化该动作
         *
         */
        public ToggleConsoleLogAction() {
            super(MikBundle.message("statusbar.toggle.console.log"));
        }

        /**
         * 切换控制台日志功能的状态
         * <p> 根据当前状态, 将控制台日志功能启用或禁用进行取反操作.
         *
         * @param e Action 事件对象
         */
        @Override
        public void actionPerformed(@NotNull AnActionEvent e) {
            MikState state = MikPersistenComponent.getInstance().getState();
            state.setEnableConsoleLog(!state.isEnableConsoleLog());
        }

        /**
         * 更新操作按钮的状态和显示文本
         * <p> 根据插件状态启用情况设置按钮是否可用, 并根据控制台日志是否启用设置显示文本
         *
         * @param e ActionEvent 对象, 包含操作事件信息
         */
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

        /**
         * 指定此操作的更新线程
         * <p> 返回用于更新操作界面的线程类型, 此处指定为后台线程 (BGT).
         *
         * @return 返回 {@link com.intellij.openapi.actionSystem.ActionUpdateThread#BGT} 表示在后台线程中更新
         */
        @Override
        public @NotNull com.intellij.openapi.actionSystem.ActionUpdateThread getActionUpdateThread() {
            return com.intellij.openapi.actionSystem.ActionUpdateThread.BGT;
        }
    }

    /**
     * 切换粘贴文件为纯文本的 Action
     */
    private static class TogglePastePlainTextAction extends AnAction {
        /**
         * 构造函数, 用于初始化 TogglePastePlainTextAction 实例
         * <p> 使用指定的国际化消息作为操作名称初始化该动作
         *
         */
        public TogglePastePlainTextAction() {
            super(MikBundle.message("statusbar.toggle.paste.plain.text"));
        }

        /**
         * 执行切换粘贴纯文本模式的操作
         * <p> 获取插件状态并切换粘贴文件为纯文本的设置值
         *
         * @param e Action 事件对象
         */
        @Override
        public void actionPerformed(@NotNull AnActionEvent e) {
            MikState state = MikPersistenComponent.getInstance().getState();
            state.setPasteFileAsPlainText(!state.isPasteFileAsPlainText());
        }

        /**
         * 更新操作按钮的状态和显示文本
         * <p> 根据插件状态启用或禁用按钮, 并根据当前“粘贴为纯文本”设置修改按钮显示文本.
         *
         * @param e ActionEvent 对象, 包含操作事件信息
         */
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

        /**
         * 指定此操作的更新线程
         * <p> 返回用于更新操作状态的线程类型, 此处指定为后台线程 (BGT).
         *
         * @return 返回 {@link com.intellij.openapi.actionSystem.ActionUpdateThread#BGT} 表示使用后台线程进行更新
         */
        @Override
        public @NotNull com.intellij.openapi.actionSystem.ActionUpdateThread getActionUpdateThread() {
            return com.intellij.openapi.actionSystem.ActionUpdateThread.BGT;
        }
    }

    /**
     * 打开 MIK 设置页面的 Action
     */
    private static class OpenSettingsAction extends AnAction {
        /**
         * 构造一个 OpenSettingsAction 实例
         * <p> 初始化动作并设置其显示名称, 名称由资源文件中的 "statusbar.open.settings" 键对应的值构成
         */
        public OpenSettingsAction() {
            super("⚙️ " + MikBundle.message("statusbar.open.settings"));
        }

        /**
         * 执行打开设置对话框的操作
         * <p> 根据传入的动作事件获取项目, 并显示指定的设置对话框.
         *
         * @param e 动作事件对象, 用于获取项目信息
         */
        @Override
        public void actionPerformed(@NotNull AnActionEvent e) {
            Project project = e.getProject();
            if (project != null) {
                com.intellij.openapi.options.ShowSettingsUtil.getInstance()
                    .showSettingsDialog(project, "Markdown Image Kit");
            }
        }

        /**
         * 更新操作的可用性状态
         * <p> 根据当前项目是否存在, 启用或禁用该操作的界面展示.
         *
         * @param e 动作事件对象, 包含与操作相关的上下文信息
         */
        @Override
        public void update(@NotNull AnActionEvent e) {
            e.getPresentation().setEnabled(e.getProject() != null);
        }

        /**
         * 指定此操作的更新线程
         * <p> 返回用于更新操作界面的线程类型, 此处指定为后台线程.
         *
         * @return 返回 {@link com.intellij.openapi.actionSystem.ActionUpdateThread#BGT} 表示后台线程
         */
        @Override
        public @NotNull com.intellij.openapi.actionSystem.ActionUpdateThread getActionUpdateThread() {
            return com.intellij.openapi.actionSystem.ActionUpdateThread.BGT;
        }
    }

    /**
     * 选择图片编辑器的 Action
     */
    private static class SelectImageEditorAction extends AnAction {
        /**
         * 表示图像编辑器的枚举类型
         * <p> 用于标识当前选择的图像编辑器
         */
        private final ImageEditorEnum editorEnum;

        /**
         * 构造一个用于选择图像编辑器的操作
         * <p> 初始化操作并设置其名称, 描述和图标, 同时保存传入的图像编辑器类型
         *
         * @param editorEnum 要设置的图像编辑器类型
         */
        public SelectImageEditorAction(ImageEditorEnum editorEnum) {
            super(editorEnum.getName(), "", getEditorIcon(editorEnum));
            this.editorEnum = editorEnum;
        }

        /**
         * 执行选择图像编辑器的操作
         * <p> 获取当前状态并设置所选的图像编辑器以及启用图像编辑器功能
         *
         * @param e Action 事件对象
         */
        @Override
        public void actionPerformed(@NotNull AnActionEvent e) {
            MikState state = MikPersistenComponent.getInstance().getState();
            state.setImageEditor(editorEnum);
            state.setEnableImageEditor(true);
        }

        /**
         * 更新操作按钮的状态和显示文本
         * <p> 根据当前插件状态和所选图像编辑器, 启用或禁用按钮, 并设置相应的显示文本.
         *
         * @param e 操作事件对象, 用于获取和设置按钮的呈现状态
         */
        @Override
        public void update(@NotNull AnActionEvent e) {
            MikState state = MikPersistenComponent.getInstance().getState();
            e.getPresentation().setEnabled(state.isEnablePlugin());

            // 检查是否是当前选中的编辑器
            ImageEditorEnum currentEditor = state.getImageEditor();
            if (currentEditor == null) {
                currentEditor = ImageEditorEnum.SHOTTR;
            }
            if (currentEditor == editorEnum && state.isEnableImageEditor()) {
                e.getPresentation().setText("✓ " + editorEnum.getName());
            } else {
                e.getPresentation().setText(editorEnum.getName());
            }
        }

        /**
         * 指定该操作的更新线程
         * <p> 返回用于更新操作界面的线程类型, 此处指定为后台线程 (BGT).
         *
         * @return 返回 {@link com.intellij.openapi.actionSystem.ActionUpdateThread#BGT} 表示后台线程
         */
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
     * 根据图片编辑器类型获取对应的图标
     *
     * @param editorEnum 图片编辑器类型
     * @return 对应的图标（经过缩放处理）
     */
    private static Icon getEditorIcon(ImageEditorEnum editorEnum) {
        Icon icon = switch (editorEnum) {
            case SHOTTR -> MikIcons.SHOTTR;
            case CLEANSHOT_X -> MikIcons.CLEANSHOTX;
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

