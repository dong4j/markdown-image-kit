package info.dong4j.idea.plugin.settings.panel;

import com.intellij.ui.components.JBLabel;
import com.intellij.util.ui.JBUI;

import info.dong4j.idea.plugin.MikBundle;
import info.dong4j.idea.plugin.enums.InsertImageActionEnum;
import info.dong4j.idea.plugin.settings.MikState;
import info.dong4j.idea.plugin.util.SwingUtils;

import org.jetbrains.annotations.NotNull;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JTextField;

import lombok.Getter;

/**
 * 图片处理面板类
 * <p>
 * 该类用于构建和管理图片处理相关的 UI 面板，提供图片插入方式的选择、自定义路径设置、本地和网络图片处理规则配置等功能。支持与 MikState 状态对象进行数据绑定，实现配置的保存、加载和修改检测。
 *
 * @author dong4j
 * @version 1.0.0
 * @date 2025.10.31
 * @since 1.0.0
 */
@SuppressWarnings( {"DuplicatedCode"})
@Getter
public class ImageProcessingPanel {
    // ========== 图片处理区域 ==========
    /** 图片处理面板，用于显示和操作图片处理相关功能的界面组件 */
    @Getter
    private JPanel content;
    /** 插入图片时的操作下拉框，用于选择不同的插入方式，如上传图片或复制到指定目录 */
    private JComboBox<String> insertImageComboBox;
    /** 自定义路径输入框，用于输入自定义路径，支持占位符显示 */
    private JTextField customPathTextField;
    /** 自定义路径提示标签，用于显示自定义路径的说明信息 */
    private JBLabel customPathHintLabel;
    /** 对本地位置的图片应用上述规则的复选框 */
    private JCheckBox applyToLocalImagesCheckBox;
    /** 对网络位置的图片应用上述规则的复选框 */
    private JCheckBox applyToNetworkImagesCheckBox;
    /** 优先使用相对路径复选框 */
    private JCheckBox preferRelativePathCheckBox;
    /** 为相对路径添加 ./ 复选框 */
    private JCheckBox addDotSlashCheckBox;
    /** 插入时自动转义图片 URL 的复选框 */
    private JCheckBox autoEscapeImageUrlCheckBox;
    /** 启用控制台日志的复选框 */
    private JCheckBox enableConsoleLogCheckBox;
    /** 粘贴文件/目录时使用纯文本格式的复选框 */
    private JCheckBox pasteFileAsPlainTextCheckBox;
    /** 当前状态对象的引用，用于在 ActionListener 中访问保存的自定义路径值 */
    private MikState currentState;

    /**
     * 初始化图像处理面板
     * <p>
     * 构造函数用于初始化图像处理面板，调用创建图像处理面板的方法
     */
    public ImageProcessingPanel() {
        createImageProcessingPanel();
    }

    /**
     * 创建图片处理区域面板
     * <p>
     * 初始化并构建图片处理相关的 UI 组件，包括下拉选项、自定义路径输入框、复选框等，用于配置图片插入规则和路径处理方式。
     * 下拉选项用于选择图片插入动作，根据选择显示或隐藏自定义路径输入框及相关提示标签。
     * 复选框用于控制是否对本地或网络图片应用规则，以及图片语法偏好设置。
     *
     * @since 1.0
     */
    private void createImageProcessingPanel() {
        content = new JPanel();
        content.setLayout(new GridBagLayout());
        content.setBorder(SwingUtils.configureTitledBorder(MikBundle.message("panel.image.processing.title")));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = JBUI.insets(5, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // 下拉选项
        gbc.gridx = 0;
        gbc.gridy = 0;
        content.add(new JBLabel(MikBundle.message("panel.image.processing.insert.action")), gbc);

        gbc.gridx = 1;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        insertImageComboBox = new com.intellij.openapi.ui.ComboBox<>(InsertImageActionEnum.getDescriptions());

        final JBLabel jbLabel = new JBLabel(MikBundle.message("panel.image.processing.custom.path"));

        insertImageComboBox.addActionListener(e -> {
            int selectedIndex = insertImageComboBox.getSelectedIndex();
            InsertImageActionEnum actionEnum = InsertImageActionEnum.of(selectedIndex);
            boolean showCustomPath = actionEnum == InsertImageActionEnum.COPY_TO_CUSTOM;
            customPathTextField.setVisible(showCustomPath);
            customPathTextField.setEnabled(showCustomPath);
            jbLabel.setVisible(showCustomPath);
            jbLabel.setEnabled(showCustomPath);
            customPathHintLabel.setVisible(showCustomPath);
            customPathHintLabel.setEnabled(showCustomPath);
            // 当选择自定义路径时，如果输入框为空，则从保存的值中恢复
            if (showCustomPath && currentState != null) {
                String currentText = customPathTextField.getText().trim();
                if (currentText.isEmpty()) {
                    String savedCustomPath = currentState.getSavedCustomInsertPath();
                    if (savedCustomPath != null && !savedCustomPath.isEmpty()) {
                        customPathTextField.setText(savedCustomPath);
                    }
                }
            }
        });
        content.add(insertImageComboBox, gbc);

        // 自定义路径输入框
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.weightx = 0;
        jbLabel.setVisible(false);
        jbLabel.setEnabled(false);
        content.add(jbLabel, gbc);

        gbc.gridx = 1;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        customPathTextField = new JTextField();
        customPathTextField.setVisible(false);
        customPathTextField.setEnabled(false);
        content.add(customPathTextField, gbc);

        // 自定义路径提示标签
        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        customPathHintLabel = new JBLabel(MikBundle.message("panel.image.processing.custom.path.hint"));
        customPathHintLabel.setVisible(false);
        customPathHintLabel.setEnabled(false);
        content.add(customPathHintLabel, gbc);

        // 复选框：对本地位置的图片应用上述规则
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 3;
        gbc.weightx = 0;
        applyToLocalImagesCheckBox = new JCheckBox(MikBundle.message("panel.image.processing.apply.local"));
        content.add(applyToLocalImagesCheckBox, gbc);

        // 复选框：对网络位置的图片应用上述规则
        gbc.gridy = 4;
        applyToNetworkImagesCheckBox = new JCheckBox(MikBundle.message("panel.image.processing.apply.network"));
        applyToNetworkImagesCheckBox.setToolTipText(MikBundle.message("panel.image.processing.apply.network.tooltip"));
        content.add(applyToNetworkImagesCheckBox, gbc);

        // 图片语法偏好
        gbc.gridy = 5;
        content.add(new JPanel(), gbc); // 分隔线占位

        gbc.gridy = 6;
        gbc.gridwidth = 3;
        content.add(new JBLabel(MikBundle.message("panel.image.processing.syntax.preference")), gbc);

        gbc.gridy = 7;
        preferRelativePathCheckBox = new JCheckBox(MikBundle.message("panel.image.processing.prefer.relative"));
        preferRelativePathCheckBox.setToolTipText(MikBundle.message("panel.image.processing.prefer.relative.tooltip"));
        content.add(preferRelativePathCheckBox, gbc);

        gbc.gridy = 8;
        addDotSlashCheckBox = new JCheckBox(MikBundle.message("panel.image.processing.add.dot.slash"));
        addDotSlashCheckBox.setToolTipText(MikBundle.message("panel.image.processing.add.dot.slash.tooltip"));
        addDotSlashCheckBox.setEnabled(false);
        preferRelativePathCheckBox.addActionListener(e -> addDotSlashCheckBox.setEnabled(preferRelativePathCheckBox.isSelected()));
        content.add(addDotSlashCheckBox, gbc);

        gbc.gridy = 9;
        autoEscapeImageUrlCheckBox = new JCheckBox(MikBundle.message("panel.image.processing.auto.escape"));
        autoEscapeImageUrlCheckBox.setToolTipText(MikBundle.message("panel.image.processing.auto.escape.tooltip"));
        content.add(autoEscapeImageUrlCheckBox, gbc);

        gbc.gridy = 10;
        enableConsoleLogCheckBox = new JCheckBox(MikBundle.message("panel.image.processing.enable.console.log"));
        enableConsoleLogCheckBox.setToolTipText(MikBundle.message("panel.image.processing.enable.console.log.tooltip"));
        content.add(enableConsoleLogCheckBox, gbc);

        gbc.gridy = 11;
        pasteFileAsPlainTextCheckBox = new JCheckBox(MikBundle.message("panel.image.processing.paste.file.as.plain.text"));
        pasteFileAsPlainTextCheckBox.setToolTipText(MikBundle.message("panel.image.processing.paste.file.as.plain.text.tooltip"));
        content.add(pasteFileAsPlainTextCheckBox, gbc);
    }

    /**
     * 判断图片处理设置是否被修改
     * <p>
     * 比较当前图片处理设置与传入的MikState对象中的设置，判断是否有变化。
     * 如果有任何设置项不同，则返回true，表示设置被修改；否则返回false。
     *
     * @param state MikState对象，用于获取图片处理设置状态
     * @return 如果图片处理设置被修改，返回true；否则返回false
     */
    public boolean isImageProcessingModified(@NotNull MikState state) {
        // 检查插入图片时的操作
        int selectedIndex = insertImageComboBox.getSelectedIndex();
        InsertImageActionEnum currentAction = InsertImageActionEnum.of(selectedIndex);
        InsertImageActionEnum stateAction = state.getInsertImageAction();
        // 使用 equals 进行比较，避免 null 比较问题
        if (currentAction == null || stateAction == null) {
            if (currentAction != stateAction) {
                return true;
            }
        } else if (!currentAction.equals(stateAction)) {
            return true;
        }

        // 检查 insertPath（根据操作类型确定期望的路径值）
        String customPath = currentAction == InsertImageActionEnum.COPY_TO_CUSTOM
                            ? customPathTextField.getText().trim()
                            : null;
        String expectedPath = InsertImageActionEnum.getPathByAction(currentAction, customPath);

        String statePath = state.getCurrentInsertPath() != null ? state.getCurrentInsertPath() : "";

        if (!expectedPath.equals(statePath)) {
            return true;
        }

        // 检查各个复选框
        if (applyToLocalImagesCheckBox.isSelected() != state.isApplyToLocalImages()) {
            return true;
        }
        if (applyToNetworkImagesCheckBox.isSelected() != state.isApplyToNetworkImages()) {
            return true;
        }
        if (preferRelativePathCheckBox.isSelected() != state.isPreferRelativePath()) {
            return true;
        }
        if (addDotSlashCheckBox.isSelected() != state.isAddDotSlash()) {
            return true;
        }
        if (autoEscapeImageUrlCheckBox.isSelected() != state.isAutoEscapeImageUrl()) {
            return true;
        }
        if (enableConsoleLogCheckBox.isSelected() != state.isEnableConsoleLog()) {
            return true;
        }
        return pasteFileAsPlainTextCheckBox.isSelected() != state.isPasteFileAsPlainText();
    }

    /**
     * 应用图片处理配置到状态对象
     * <p>
     * 将面板中各个控件的值保存到传入的 MikState 对象中，包括插入图片操作类型、路径设置、复选框状态等。
     *
     * @param state 状态对象，用于保存配置信息
     */
    public void applyImageProcessingConfigs(@NotNull MikState state) {
        // 获取选中的操作枚举
        int selectedIndex = insertImageComboBox.getSelectedIndex();
        InsertImageActionEnum actionEnum = InsertImageActionEnum.of(selectedIndex);
        state.setInsertImageAction(actionEnum);

        // 根据操作类型设置 insertPath
        String customPath = actionEnum == InsertImageActionEnum.COPY_TO_CUSTOM
                            ? customPathTextField.getText().trim()
                            : null;
        String path = InsertImageActionEnum.getPathByAction(actionEnum, customPath);
        state.setCurrentInsertPath(path);

        // 如果是自定义路径，同时保存到持久化字段
        if (actionEnum == InsertImageActionEnum.COPY_TO_CUSTOM) {
            state.setSavedCustomInsertPath(customPath);
        }

        // 保存复选框状态
        state.setApplyToLocalImages(applyToLocalImagesCheckBox.isSelected());
        state.setApplyToNetworkImages(applyToNetworkImagesCheckBox.isSelected());
        state.setPreferRelativePath(preferRelativePathCheckBox.isSelected());
        state.setAddDotSlash(addDotSlashCheckBox.isSelected());
        state.setAutoEscapeImageUrl(autoEscapeImageUrlCheckBox.isSelected());
        state.setEnableConsoleLog(enableConsoleLogCheckBox.isSelected());
        state.setPasteFileAsPlainText(pasteFileAsPlainTextCheckBox.isSelected());
    }

    /**
     * 初始化图片处理面板
     * <p>
     * 根据传入的 MikState 对象初始化面板中各个控件的值，包括下拉框、复选框等组件的状态。
     *
     * @param state 当前状态对象，用于获取图片处理相关的配置信息
     */
    public void initImageProcessingPanel(@NotNull MikState state) {
        // 保存 state 引用，以便在 ActionListener 中访问
        this.currentState = state;

        // 优先使用 insertImageAction，如果为 null 则根据 insertPath 反推
        InsertImageActionEnum action = state.getInsertImageAction();
        String insertPath = state.getCurrentInsertPath() != null ? state.getCurrentInsertPath() : "";

        if (action == null) {
            // 根据 insertPath 反推应该选择哪个枚举值
            if (insertPath.equals("./")) {
                action = InsertImageActionEnum.COPY_TO_CURRENT;
            } else if (insertPath.equals("./assets")) {
                action = InsertImageActionEnum.COPY_TO_ASSETS;
            } else if (insertPath.equals("./${filename}.assets")) {
                action = InsertImageActionEnum.COPY_TO_FILENAME_ASSETS;
            } else if (!insertPath.isEmpty()) {
                // 如果 insertPath 不为空但不是已知的模式，则认为是自定义路径
                action = InsertImageActionEnum.COPY_TO_CUSTOM;
            } else {
                action = InsertImageActionEnum.NONE;
            }
        }

        // 设置下拉框选中项
        insertImageComboBox.setSelectedIndex(action.getValue());

        // 根据操作类型设置自定义路径的显示和值
        if (action == InsertImageActionEnum.COPY_TO_CUSTOM) {
            // 如果是自定义路径，显示并设置路径值，优先使用保存的自定义路径值
            customPathTextField.setVisible(true);
            customPathTextField.setEnabled(true);
            String savedCustomPath = state.getSavedCustomInsertPath();
            // 优先使用保存的自定义路径值，如果为空则使用 insertPath
            String pathToUse = (savedCustomPath != null && !savedCustomPath.isEmpty())
                               ? savedCustomPath
                               : insertPath;
            customPathTextField.setText(pathToUse);
            customPathHintLabel.setVisible(true);
            customPathHintLabel.setEnabled(true);
        } else {
            // 其他情况隐藏自定义路径输入框和提示标签
            customPathTextField.setVisible(false);
            customPathTextField.setEnabled(false);
            customPathHintLabel.setVisible(false);
            customPathHintLabel.setEnabled(false);
        }

        // 设置复选框状态
        applyToLocalImagesCheckBox.setSelected(state.isApplyToLocalImages());
        applyToNetworkImagesCheckBox.setSelected(state.isApplyToNetworkImages());
        preferRelativePathCheckBox.setSelected(state.isPreferRelativePath());
        addDotSlashCheckBox.setSelected(state.isAddDotSlash());
        addDotSlashCheckBox.setEnabled(state.isPreferRelativePath());
        autoEscapeImageUrlCheckBox.setSelected(state.isAutoEscapeImageUrl());
        enableConsoleLogCheckBox.setSelected(state.isEnableConsoleLog());
        pasteFileAsPlainTextCheckBox.setSelected(state.isPasteFileAsPlainText());
    }

    /**
     * 设置面板所有组件的启用/禁用状态
     * <p>
     * 当全局开关改变时，联动控制所有子组件的可用状态
     *
     * @param enabled true 启用所有组件，false 禁用所有组件
     */
    public void setAllComponentsEnabled(boolean enabled) {
        insertImageComboBox.setEnabled(enabled);
        customPathTextField.setEnabled(enabled && customPathTextField.isVisible());
        customPathHintLabel.setEnabled(enabled && customPathHintLabel.isVisible());
        applyToLocalImagesCheckBox.setEnabled(enabled);
        applyToNetworkImagesCheckBox.setEnabled(enabled);
        preferRelativePathCheckBox.setEnabled(enabled);
        addDotSlashCheckBox.setEnabled(enabled && preferRelativePathCheckBox.isSelected());
        autoEscapeImageUrlCheckBox.setEnabled(enabled);
        enableConsoleLogCheckBox.setEnabled(enabled);
        pasteFileAsPlainTextCheckBox.setEnabled(enabled);
    }

}
