package info.dong4j.idea.plugin.settings.panel;

import com.intellij.ui.components.JBLabel;
import com.intellij.util.ui.JBUI;

import info.dong4j.idea.plugin.enums.InsertImageActionEnum;
import info.dong4j.idea.plugin.settings.MikState;

import org.jetbrains.annotations.NotNull;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JTextField;

import lombok.Getter;

/**
 *
 *
 * @author dong4j
 * @version 1.0.0
 * @email "mailto:dong4j@gmail.com"
 * @date 2025.10.31 22:35
 * @since x.x.x
 */
@SuppressWarnings( {"DuplicatedCode", "D"})
@Getter
public class ImageProcessingPanel {
    // ========== 图片处理区域 ==========
    /**
     * 图片处理面板
     */
    @Getter
    private JPanel content;
    /**
     * 插入图片时的操作下拉框
     * <p>
     * 【新功能】对应老页面的部分逻辑：
     * - 选项4"上传图片"对应老页面的 uploadAndReplaceCheckBox 功能
     * - 选项2/3对应老页面的 copyToDirCheckBox + whereToCopyTextField 功能
     */
    private JComboBox<String> insertImageComboBox;
    /**
     * 自定义路径输入框
     * <p>
     * 【新功能】对应老页面的 whereToCopyTextField，但新增了占位符支持
     */
    private JTextField customPathTextField;
    /**
     * 自定义路径提示标签
     * <p>
     * 【新功能】用于显示自定义路径的说明信息
     */
    private JBLabel customPathHintLabel;
    /**
     * 对本地位置的图片应用上述规则复选框
     * <p>
     * 【新功能】
     */
    private JCheckBox applyToLocalImagesCheckBox;
    /**
     * 对网络位置的图片应用上述规则复选框
     * <p>
     * 【新功能】
     */
    private JCheckBox applyToNetworkImagesCheckBox;
    /**
     * 优先使用相对路径复选框
     * <p>
     * 【新功能】
     */
    private JCheckBox preferRelativePathCheckBox;
    /**
     * 为相对路径添加 ./ 复选框
     * <p>
     * 【新功能】
     */
    private JCheckBox addDotSlashCheckBox;
    /**
     * 插入时自动转义图片 URL 复选框
     * <p>
     * 【新功能】
     */
    private JCheckBox autoEscapeImageUrlCheckBox;
    /**
     * 当前状态对象的引用，用于在 ActionListener 中访问保存的自定义路径值
     */
    private MikState currentState;

    public ImageProcessingPanel() {
        createImageProcessingPanel();
    }

    /**
     * 创建图片处理区域面板
     */
    @SuppressWarnings("DialogTitleCapitalization")
    private void createImageProcessingPanel() {
        content = new JPanel();
        content.setLayout(new GridBagLayout());
        content.setBorder(BorderFactory.createTitledBorder("插入图片时..."));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = JBUI.insets(5, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // 下拉选项
        gbc.gridx = 0;
        gbc.gridy = 0;
        content.add(new JBLabel("插入图片时的操作:"), gbc);

        gbc.gridx = 1;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        insertImageComboBox = new com.intellij.openapi.ui.ComboBox<>(InsertImageActionEnum.getDescriptions());

        final JBLabel jbLabel = new JBLabel("自定义路径:");

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
        customPathHintLabel = new JBLabel("""
                                              <html>
                                                  输入相对路径 (以 ./ 或 ../ 开头) 或绝对路径.<br/>
                                                  <b>${project}</b>: 当前项目路径<br/>
                                                  <b>${filename}</b>: 当前文件名<br/>
                                                  示例: ./assets, ./images, ../images, ./${filename}.assets, ${project}/images
                                              </html>
                                              """);
        customPathHintLabel.setVisible(false);
        customPathHintLabel.setEnabled(false);
        content.add(customPathHintLabel, gbc);

        // 复选框：对本地位置的图片应用上述规则
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 3;
        gbc.weightx = 0;
        applyToLocalImagesCheckBox = new JCheckBox("对本地位置的图片应用上述规则");
        content.add(applyToLocalImagesCheckBox, gbc);

        // 复选框：对网络位置的图片应用上述规则
        gbc.gridy = 4;
        applyToNetworkImagesCheckBox = new JCheckBox("对网络位置的图片应用上述规则");
        applyToNetworkImagesCheckBox.setToolTipText("在合法的 markdown 图片标签中，如果粘贴的是网络图片，则会直接下载到指定的目录中");
        content.add(applyToNetworkImagesCheckBox, gbc);

        // 图片语法偏好
        gbc.gridy = 5;
        content.add(new JPanel(), gbc); // 分隔线占位

        gbc.gridy = 6;
        gbc.gridwidth = 3;
        content.add(new JBLabel("图片语法偏好:"), gbc);

        gbc.gridy = 7;
        preferRelativePathCheckBox = new JCheckBox("优先使用相对路径");
        preferRelativePathCheckBox.setToolTipText("复制到绝对路径时，自动转换为相对路径");
        content.add(preferRelativePathCheckBox, gbc);

        gbc.gridy = 8;
        addDotSlashCheckBox = new JCheckBox("为相对路径添加 ./");
        addDotSlashCheckBox.setToolTipText("开启后，相对路径会自动添加 ./ 前缀");
        addDotSlashCheckBox.setEnabled(false);
        preferRelativePathCheckBox.addActionListener(e -> {
            addDotSlashCheckBox.setEnabled(preferRelativePathCheckBox.isSelected());
        });
        content.add(addDotSlashCheckBox, gbc);

        gbc.gridy = 9;
        autoEscapeImageUrlCheckBox = new JCheckBox("插入时自动转义图片 URL");
        autoEscapeImageUrlCheckBox.setToolTipText("开启后，插入图片 URL 时会自动进行转义处理");
        content.add(autoEscapeImageUrlCheckBox, gbc);
    }

    /**
     * 判断图片处理设置是否被修改
     * <p>
     * 检查传入的MikState对象中图片处理相关设置是否发生改变
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
        return autoEscapeImageUrlCheckBox.isSelected() != state.isAutoEscapeImageUrl();
    }

    /**
     * 应用图片处理配置到状态对象
     * <p>
     * 将面板中各个控件的值保存到传入的 MikState 对象中
     *
     * @param state 状态对象
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
    }

    /**
     * 初始化图片处理面板
     * <p>
     * 根据传入的 MikState 对象初始化面板中各个控件的值
     *
     * @param state 当前状态对象
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
    }

}
