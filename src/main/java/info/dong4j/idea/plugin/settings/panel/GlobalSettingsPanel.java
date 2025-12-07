package info.dong4j.idea.plugin.settings.panel;

import com.intellij.util.ui.JBUI;

import info.dong4j.idea.plugin.MikBundle;
import info.dong4j.idea.plugin.settings.MikState;

import org.jetbrains.annotations.NotNull;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JPanel;

import lombok.Getter;

/**
 * 全局设置面板
 * <p>
 * 该类用于构建和管理插件的全局设置，包括插件启用/禁用的主开关。
 * 提供了全局开关的 UI 组件及其状态管理功能。
 *
 * @author dong4j
 * @version 1.0.0
 * @date 2025.12.07
 * @since 2.2.0
 */
@Getter
public class GlobalSettingsPanel {
    /** 全局设置面板，用于显示插件的全局配置选项 */
    @Getter
    private JPanel content;
    /** 启用插件功能的全局开关复选框 */
    private JCheckBox enablePluginCheckBox;

    /**
     * 初始化全局设置面板
     * <p>
     * 构造函数用于初始化全局设置面板，调用创建面板的方法
     */
    public GlobalSettingsPanel() {
        createGlobalSettingsPanel();
    }

    /**
     * 创建全局设置面板
     * <p>
     * 初始化并构建全局设置相关的 UI 组件，包括插件启用开关等配置项。
     *
     * @since 2.2.0
     */
    private void createGlobalSettingsPanel() {
        content = new JPanel();
        content.setLayout(new GridBagLayout());
        content.setBorder(BorderFactory.createTitledBorder(MikBundle.message("panel.global.title")));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = JBUI.insets(5, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // 全局开关（使用粗体字体使其更醒目）
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 3;
        gbc.weightx = 1.0;
        enablePluginCheckBox = new JCheckBox(MikBundle.message("panel.global.enable.plugin"));
        enablePluginCheckBox.setToolTipText(MikBundle.message("panel.global.enable.plugin.tooltip"));
        // 使用粗体字体使其更醒目
        java.awt.Font currentFont = enablePluginCheckBox.getFont();
        enablePluginCheckBox.setFont(currentFont.deriveFont(java.awt.Font.BOLD));
        content.add(enablePluginCheckBox, gbc);
    }

    /**
     * 判断全局设置是否被修改
     * <p>
     * 比较当前全局设置与传入的 MikState 对象中的设置，判断是否有变化。
     *
     * @param state MikState 对象，用于获取全局设置状态
     * @return 如果全局设置被修改，返回 true；否则返回 false
     */
    public boolean isGlobalSettingsModified(@NotNull MikState state) {
        return enablePluginCheckBox.isSelected() != state.isEnablePlugin();
    }

    /**
     * 应用全局设置配置到状态对象
     * <p>
     * 将面板中的全局开关状态保存到传入的 MikState 对象中。
     *
     * @param state 状态对象，用于保存配置信息
     */
    public void applyGlobalSettingsConfigs(@NotNull MikState state) {
        state.setEnablePlugin(enablePluginCheckBox.isSelected());
    }

    /**
     * 初始化全局设置面板
     * <p>
     * 根据传入的 MikState 对象初始化面板中的控件状态。
     *
     * @param state 当前状态对象，用于获取全局设置相关的配置信息
     */
    public void initGlobalSettingsPanel(@NotNull MikState state) {
        enablePluginCheckBox.setSelected(state.isEnablePlugin());
    }

    /**
     * 获取全局开关的选中状态
     *
     * @return 如果全局开关被选中返回 true，否则返回 false
     */
    public boolean isPluginEnabled() {
        return enablePluginCheckBox.isSelected();
    }

    /**
     * 添加全局开关状态改变监听器
     * <p>
     * 当全局开关状态改变时，调用传入的 Runnable 对象。
     *
     * @param listener 监听器，当开关状态改变时执行
     */
    public void addEnablePluginListener(Runnable listener) {
        enablePluginCheckBox.addActionListener(e -> listener.run());
    }
}

