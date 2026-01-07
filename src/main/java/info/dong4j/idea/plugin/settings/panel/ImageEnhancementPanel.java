package info.dong4j.idea.plugin.settings.panel;

import com.intellij.openapi.ui.ComboBox;
import com.intellij.ui.components.JBLabel;
import com.intellij.util.ui.JBUI;

import info.dong4j.idea.plugin.MikBundle;
import info.dong4j.idea.plugin.enums.ImageEditorEnum;
import info.dong4j.idea.plugin.enums.ImageMarkEnum;
import info.dong4j.idea.plugin.settings.MikState;
import info.dong4j.idea.plugin.util.SwingUtils;

import org.jetbrains.annotations.NotNull;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.Objects;

import javax.swing.DefaultListCellRenderer;
import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;

import icons.MikIcons;
import lombok.Getter;

/**
 * 图片增强处理面板类
 * <p>
 * 该类用于构建和管理图片增强处理的图形界面面板，包含图片格式转换、压缩设置、重命名模板、水印添加等功能组件。支持与状态对象的初始化、修改检测和配置应用操作，便于在图像处理流程中进行配置管理和界面交互。
 *
 * @author dong4j
 * @version 1.0.0
 * @date 2025.10.31
 * @since 1.0.0
 */
public class ImageEnhancementPanel {

    // ========== 图片增强处理区域 ==========
    private static final ImageEditorEnum[] IMAGE_EDITOR_OPTIONS = new ImageEditorEnum[] {
        ImageEditorEnum.CLEANSHOT_X,
        ImageEditorEnum.SHOTTR
    };

    /** 图片增强处理面板 */
    @Getter
    private JPanel content;
    /**
     * 图片重命名复选框
     * <p>
     * 【字段映射】对应老页面的 renameCheckBox
     */
    private JCheckBox renameCheckBox;
    /** 重命名模式输入框，支持占位符格式，用于新页面中自定义文件重命名模式 */
    private JTextField renamePatternTextField;
    /** 重命名占位符提示标签 */
    private JLabel renamePlaceholderHintLabel;

    /**
     * 压缩比例微调器
     * <p>
     * 【字段映射】对应老页面的 compressSlider (JSlider)，新页面改为 JSpinner 以便更精确控制
     */
    private JSpinner compressSpinner;
    /** 转为 WebP 复选框 【字段映射】对应老页面的 convertToWebpCheckBox */
    private JCheckBox convertToWebpCheckBox;
    /** WebP 质量微调器，用于调整 WebP 图像的压缩质量，新页面新增功能，老页面未包含此设置 */
    private JSpinner webpQualitySpinner;
    /** 添加水印的复选框，用于控制是否显示水印功能 */
    private JCheckBox watermarkCheckBox;
    /** 水印文本输入框，用于输入水印内容，【字段映射】对应老页面的 watermarkTextField */
    private JTextField watermarkTextTextField;

    /** 替换为 <a> 标签的复选框，用于控制是否将选项转换为 HTML 标签 */
    private JCheckBox replaceToHtmlTagCheckBox;
    /** HTML 标签类型下拉框，用于选择标签类型（正常、点击放大、自定义） */
    private JComboBox<String> htmlTagTypeComboBox;
    /** 自定义 HTML 标签输入框，用于输入自定义的 HTML 标签内容 */
    private JTextField customHtmlTagTextField;

    /** 图片压缩复选框 【字段映射】对应老页面的 compressCheckBox */
    private JCheckBox compressCheckBox;
    /** 启用图片编辑器的复选框 */
    private JCheckBox enableImageEditorCheckBox;
    /** 图片编辑器下拉列表 */
    private JComboBox<String> imageEditorComboBox;
    /** 删除图片复选框 */
    private JCheckBox deleteImageCheckBox;
    /** 删除时是否二次确认复选框 */
    private JCheckBox deleteImageWithConfirmCheckBox;
    /** 当前状态对象的引用，用于在 ActionListener 中访问保存的自定义标签代码 */
    private MikState currentState;

    /**
     * 构造函数，初始化图像增强面板
     * <p>
     * 调用 createImageEnhancementPanel 方法创建图像增强面板
     */
    public ImageEnhancementPanel() {
        createImageEnhancementPanel();
    }

    /**
     * 创建图片增强处理区域面板
     * <p>
     * 初始化并构建图片增强处理相关的 UI 控件，包括重命名、图片压缩、转为 WebP、添加水印、替换为 HTML 标签等功能的控件布局。
     * 该面板使用 GridBagLayout 布局管理器进行布局，并通过 GridBagConstraints 控制各组件的位置和大小。
     * 所有控件的启用状态根据对应的复选框状态动态切换。
     */
    private void createImageEnhancementPanel() {
        content = new JPanel();
        content.setLayout(new GridBagLayout());
        content.setBorder(SwingUtils.configureTitledBorder(MikBundle.message("panel.image.enhancement.title")));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = JBUI.insets(5, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // 图片重命名 - 放在最前面
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.weightx = 0;
        gbc.insets = JBUI.insets(5, 10); // 确保复选框使用统一的间距设置
        renameCheckBox = new JCheckBox(MikBundle.message("panel.image.enhancement.rename"));
        content.add(renameCheckBox, gbc);

        gbc.gridx = 1;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        renamePatternTextField = new JTextField("${filename}");
        renamePatternTextField.setEnabled(false);
        renamePatternTextField.setToolTipText("重命名模板，支持占位符");
        content.add(renamePatternTextField, gbc);

        // 占位符提示标签
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.gridwidth = 3;
        gbc.weightx = 0;
        renamePlaceholderHintLabel = new JBLabel(MikBundle.message("panel.image.enhancement.rename.hint"));
        renamePlaceholderHintLabel.setEnabled(false); // 默认禁用，与 renameCheckBox 未勾选状态一致
        content.add(renamePlaceholderHintLabel, gbc);

        renameCheckBox.addActionListener(e -> {
            boolean enabled = renameCheckBox.isSelected();
            renamePatternTextField.setEnabled(enabled);
            renamePlaceholderHintLabel.setEnabled(enabled);
        });

        // 图片编辑器配置
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 1;
        gbc.weightx = 0;
        enableImageEditorCheckBox = new JCheckBox(MikBundle.message("panel.image.processing.enable.image.editor"));
        enableImageEditorCheckBox.setToolTipText(MikBundle.message("panel.image.processing.enable.image.editor.tooltip"));
        enableImageEditorCheckBox.addActionListener(e -> {
            boolean enabled = enableImageEditorCheckBox.isSelected();
            imageEditorComboBox.setEnabled(enabled);
        });
        content.add(enableImageEditorCheckBox, gbc);

        gbc.gridx = 1;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        imageEditorComboBox = new ComboBox<>(getImageEditorOptionNames());
        imageEditorComboBox.setEnabled(false);
        // 设置自定义渲染器，显示图标和文本
        imageEditorComboBox.setRenderer(new DefaultListCellRenderer() {
            /**
             * 重写列表单元格渲染器方法, 用于自定义单元格显示内容和图标
             * <p> 根据当前索引获取对应的 ImageEditorEnum 枚举值, 并设置单元格的图标和文本.
             *
             * @param list         当前列表组件
             * @param value        当前单元格的值
             * @param index        当前单元格的索引
             * @param isSelected   当前单元格是否被选中
             * @param cellHasFocus 当前单元格是否获得焦点
             * @return 渲染后的单元格组件
             */
            @Override
            public java.awt.Component getListCellRendererComponent(
                JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                // 根据索引获取对应的枚举值
                ImageEditorEnum editorEnum = getImageEditorOption(index);
                if (editorEnum != null) {
                    Icon icon = switch (editorEnum) {
                        case SHOTTR -> MikIcons.SHOTTR;
                        case CLEANSHOT_X -> MikIcons.CLEANSHOTX;
                        default -> null;
                    };
                    label.setIcon(icon);
                    label.setText(editorEnum.getName());
                }
                return label;
            }
        });
        content.add(imageEditorComboBox, gbc);

        // 图片压缩
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 1;
        gbc.weightx = 0;
        gbc.insets = JBUI.insets(5, 10); // 确保复选框使用统一的间距设置
        compressCheckBox = new JCheckBox(MikBundle.message("panel.image.enhancement.compress"));
        content.add(compressCheckBox, gbc);

        gbc.gridx = 1;
        gbc.gridwidth = 1; // 改为只占一列
        gbc.weightx = 0; // 不拉伸
        gbc.fill = GridBagConstraints.NONE; // 不填充，保持固定大小
        compressSpinner = new JSpinner(new SpinnerNumberModel(80, 40, 90, 1));
        compressSpinner.setEnabled(false);
        ((JSpinner.DefaultEditor) compressSpinner.getEditor()).getTextField().setColumns(5);
        // 禁止直接在文本框中输入，只能通过上下按钮调节
        ((JSpinner.DefaultEditor) compressSpinner.getEditor()).getTextField().setEditable(false);
        // 设置 JSpinner 的宽度
        compressSpinner.setPreferredSize(new Dimension(100, compressSpinner.getPreferredSize().height));
        compressSpinner.setMaximumSize(new Dimension(100, compressSpinner.getMaximumSize().height));
        content.add(compressSpinner, gbc);
        // 恢复 fill 设置
        gbc.fill = GridBagConstraints.HORIZONTAL;

        compressCheckBox.addActionListener(e -> compressSpinner.setEnabled(compressCheckBox.isSelected()));

        // 转为 WebP
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 1;
        gbc.weightx = 0;
        gbc.insets = JBUI.insets(5, 10); // 确保复选框使用统一的间距设置
        convertToWebpCheckBox = new JCheckBox(MikBundle.message("panel.image.enhancement.webp"));
        content.add(convertToWebpCheckBox, gbc);


        gbc.gridx = 1;
        gbc.gridwidth = 1; // 改为只占一列
        gbc.weightx = 0; // 不拉伸
        gbc.fill = GridBagConstraints.NONE; // 不填充，保持固定大小
        webpQualitySpinner = new JSpinner(new SpinnerNumberModel(80, 40, 90, 1));
        webpQualitySpinner.setEnabled(false);
        ((JSpinner.DefaultEditor) webpQualitySpinner.getEditor()).getTextField().setColumns(5);
        // 禁止直接在文本框中输入，只能通过上下按钮调节
        ((JSpinner.DefaultEditor) webpQualitySpinner.getEditor()).getTextField().setEditable(false);
        // 设置 JSpinner 的宽度
        webpQualitySpinner.setPreferredSize(new Dimension(100, webpQualitySpinner.getPreferredSize().height));
        webpQualitySpinner.setMaximumSize(new Dimension(100, webpQualitySpinner.getMaximumSize().height));
        content.add(webpQualitySpinner, gbc);
        // 恢复 fill 设置
        gbc.fill = GridBagConstraints.HORIZONTAL;

        convertToWebpCheckBox.addActionListener(e -> webpQualitySpinner.setEnabled(convertToWebpCheckBox.isSelected()));

        // 添加水印
        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.gridwidth = 1;
        gbc.weightx = 0;
        gbc.insets = JBUI.insets(5, 10); // 确保复选框使用统一的间距设置
        watermarkCheckBox = new JCheckBox(MikBundle.message("panel.image.enhancement.watermark"));
        content.add(watermarkCheckBox, gbc);

        gbc.gridy = 6;
        gbc.gridx = 1;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        watermarkTextTextField = new JTextField();
        watermarkTextTextField.setEnabled(false);
        watermarkTextTextField.setToolTipText("请输入水印文本");
        content.add(watermarkTextTextField, gbc);

        watermarkCheckBox.addActionListener(e -> watermarkTextTextField.setEnabled(watermarkCheckBox.isSelected()));

        // 替换为 <a> 标签 - 放在水印下面
        gbc.gridx = 0;
        gbc.gridy = 7;
        gbc.gridwidth = 1;
        gbc.weightx = 0;
        gbc.insets = JBUI.insets(5, 10);
        replaceToHtmlTagCheckBox = new JCheckBox(MikBundle.message("panel.image.enhancement.replace.html.tag"));
        content.add(replaceToHtmlTagCheckBox, gbc);

        // HTML 标签类型下拉框
        gbc.gridx = 1;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        htmlTagTypeComboBox = new ComboBox<>(ImageMarkEnum.getDescriptions());
        htmlTagTypeComboBox.setEnabled(false);

        htmlTagTypeComboBox.addActionListener(e -> {
            int selectedIndex = htmlTagTypeComboBox.getSelectedIndex();
            ImageMarkEnum tagEnum = ImageMarkEnum.of(selectedIndex);
            boolean showCustom = tagEnum == ImageMarkEnum.CUSTOM;
            customHtmlTagTextField.setVisible(showCustom);
            customHtmlTagTextField.setEnabled(showCustom && replaceToHtmlTagCheckBox.isSelected());
            // 当选择自定义时，如果输入框为空，则从保存的值中恢复
            if (showCustom && currentState != null) {
                String currentText = customHtmlTagTextField.getText().trim();
                if (currentText.isEmpty()) {
                    String savedCustomCode = currentState.getCustomTagCode();
                    if (savedCustomCode != null && !savedCustomCode.isEmpty()) {
                        customHtmlTagTextField.setText(savedCustomCode);
                    }
                }
            }
        });
        content.add(htmlTagTypeComboBox, gbc);

        // 自定义输入框（占满一行，不需要左侧标签）
        gbc.gridx = 1;
        gbc.gridy = 8;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        customHtmlTagTextField = new JTextField();
        customHtmlTagTextField.setVisible(false);
        customHtmlTagTextField.setEnabled(false);
        customHtmlTagTextField.setToolTipText("请输入自定义 HTML 标签模板");
        content.add(customHtmlTagTextField, gbc);


        // 启用/禁用相关控件
        replaceToHtmlTagCheckBox.addActionListener(e -> {
            boolean enabled = replaceToHtmlTagCheckBox.isSelected();
            htmlTagTypeComboBox.setEnabled(enabled);

            // 触发下拉框的 ActionListener，以更新自定义输入框的显示状态
            int selectedIndex = htmlTagTypeComboBox.getSelectedIndex();
            ImageMarkEnum tagEnum = ImageMarkEnum.of(selectedIndex);
            boolean showCustom = tagEnum == ImageMarkEnum.CUSTOM;

            customHtmlTagTextField.setVisible(enabled && showCustom);
            customHtmlTagTextField.setEnabled(enabled && showCustom);
        });

        // 删除图片配置
        gbc.gridx = 0;
        gbc.gridy = 9;
        gbc.gridwidth = 1;
        gbc.weightx = 0;
        gbc.insets = JBUI.insets(5, 10);
        deleteImageCheckBox = new JCheckBox(MikBundle.message("panel.image.enhancement.delete.image"));
        deleteImageCheckBox.addActionListener(e -> {
            boolean enabled = deleteImageCheckBox.isSelected();
            deleteImageWithConfirmCheckBox.setEnabled(enabled);
        });
        content.add(deleteImageCheckBox, gbc);

        // 删除时是否二次确认复选框 - 放在下一行，前面添加空格表示子选项
        gbc.gridx = 0;
        gbc.gridy = 10;
        gbc.gridwidth = 3;
        gbc.weightx = 1.0;
        gbc.insets = JBUI.insets(5, 30, 5, 10); // 增加左边距，使其看起来像子选项
        deleteImageWithConfirmCheckBox = new JCheckBox("  " + MikBundle.message("panel.image.enhancement.delete.image.with.confirm"));
        deleteImageWithConfirmCheckBox.setEnabled(false);
        content.add(deleteImageWithConfirmCheckBox, gbc);
        // 恢复默认的 insets 设置
        gbc.insets = JBUI.insets(5, 10);

    }

    /**
     * 初始化图片增强处理面板
     * <p>
     * 根据传入的状态对象初始化图片增强相关的 UI 组件，包括 HTML 标签类型、图片压缩、转为 WebP、图片重命名和水印设置。
     *
     * @param state 当前状态对象，包含图片增强处理的各种配置信息
     */
    public void initImageEnhancementPanel(@NotNull MikState state) {
        // 保存 state 引用，以便在 ActionListener 中访问
        this.currentState = state;

        // 替换为 HTML 标签
        this.replaceToHtmlTagCheckBox.setSelected(state.isChangeToHtmlTag());

        boolean htmlTagEnabled = state.isChangeToHtmlTag();

        // 设置下拉框的启用状态
        this.htmlTagTypeComboBox.setEnabled(htmlTagEnabled);

        // 根据状态设置下拉框的选中项
        ImageMarkEnum tagEnum = state.getImageMarkEnum();
        if (tagEnum == null) {
            tagEnum = ImageMarkEnum.ORIGINAL;
        }

        // 确保索引在有效范围内，防止因枚举值变化导致的索引越界
        int index = tagEnum.index;
        if (index < 0 || index >= this.htmlTagTypeComboBox.getItemCount()) {
            // 如果索引无效，尝试通过枚举名称查找
            tagEnum = ImageMarkEnum.ORIGINAL;
            index = tagEnum.index;
        }
        this.htmlTagTypeComboBox.setSelectedIndex(index);

        // 自定义输入框的可见性和启用状态
        boolean customSelected = tagEnum == ImageMarkEnum.CUSTOM;
        if (customSelected) {
            this.customHtmlTagTextField.setText(state.getCustomTagCode());
        }

        boolean customVisible = htmlTagEnabled && customSelected;
        this.customHtmlTagTextField.setVisible(customVisible);
        this.customHtmlTagTextField.setEnabled(customVisible);

        // 图片压缩
        this.compressCheckBox.setSelected(state.isCompress());
        this.compressSpinner.setEnabled(state.isCompress());
        this.compressSpinner.setValue(state.getCompressBeforeUploadOfPercent());

        // 转为 WebP
        this.convertToWebpCheckBox.setSelected(state.isConvertToWebp());
        this.webpQualitySpinner.setValue(state.getWebpQuality());
        this.webpQualitySpinner.setEnabled(state.isConvertToWebp());

        // 图片重命名
        this.renameCheckBox.setSelected(state.isRename());
        if (this.renamePatternTextField != null) {
            this.renamePatternTextField.setEnabled(state.isRename());
            // 使用新的 renameTemplate 字段
            String template = state.getRenameTemplate();
            if (template != null && !template.trim().isEmpty()) {
                this.renamePatternTextField.setText(template);
            } else {
                // 如果模板为空，使用默认模板
                this.renamePatternTextField.setText("${filename}");
            }
        }
        // 设置提示标签的启用状态
        if (this.renamePlaceholderHintLabel != null) {
            this.renamePlaceholderHintLabel.setEnabled(state.isRename());
        }

        // 水印
        this.watermarkCheckBox.setSelected(state.isWatermark());
        this.watermarkTextTextField.setEnabled(state.isWatermark());
        this.watermarkTextTextField.setText(state.getWatermarkText());

        // 图片编辑器
        this.enableImageEditorCheckBox.setSelected(state.isEnableImageEditor());
        ImageEditorEnum editor = state.getImageEditor();
        this.imageEditorComboBox.setSelectedIndex(getImageEditorOptionIndex(editor));
        this.imageEditorComboBox.setEnabled(state.isEnableImageEditor());

        // 删除图片
        this.deleteImageCheckBox.setSelected(state.isDeleteImage());
        this.deleteImageWithConfirmCheckBox.setSelected(state.isDeleteImageWithConfirm());
        this.deleteImageWithConfirmCheckBox.setEnabled(state.isDeleteImage());
    }

    /**
     * 判断图片增强处理设置是否已修改
     * <p>
     * 比较当前图片增强处理配置与给定状态对象的配置，判断是否发生改变。
     * 如果所有配置项均一致，则返回 false；否则返回 true。
     *
     * @param state 要比较的状态对象
     * @return 如果当前状态与给定状态一致，返回 false；否则返回 true
     */
    @SuppressWarnings("D")
    public boolean isImageEnhancementModified(@NotNull MikState state) {
        boolean changeToHtmlTag = this.replaceToHtmlTagCheckBox.isSelected();

        // 从下拉框获取选中的标签类型枚举
        ImageMarkEnum selectedTagEnum = null;
        String customTagTypeCode = "";
        if (changeToHtmlTag) {
            int selectedIndex = this.htmlTagTypeComboBox.getSelectedIndex();
            selectedTagEnum = ImageMarkEnum.of(selectedIndex);
            // 如果是自定义类型，获取自定义代码
            if (selectedTagEnum == ImageMarkEnum.CUSTOM) {
                customTagTypeCode = this.customHtmlTagTextField.getText().trim();
            }
        }

        boolean compress = this.compressCheckBox.isSelected();
        int compressPercent = ((Number) this.compressSpinner.getValue()).intValue();
        boolean convertToWebp = this.convertToWebpCheckBox.isSelected();
        int webpQuality = ((Number) this.webpQualitySpinner.getValue()).intValue();

        boolean rename = this.renameCheckBox.isSelected();
        String renameTemplate = this.renamePatternTextField.getText().trim();

        boolean watermark = this.watermarkCheckBox.isSelected();
        String watermarkText = this.watermarkTextTextField.getText().trim();

        boolean deleteImage = this.deleteImageCheckBox.isSelected();
        boolean deleteImageWithConfirm = this.deleteImageWithConfirmCheckBox.isSelected();

        // 比较枚举值和自定义代码
        boolean tagEnumEquals = (selectedTagEnum == state.getImageMarkEnum());
        boolean customCodeEquals = true;
        if (selectedTagEnum == ImageMarkEnum.CUSTOM) {
            String stateCode = state.getCustomTagCode() != null ? state.getCustomTagCode() : "";
            customCodeEquals = customTagTypeCode.equals(stateCode);
        }

        // 图片编辑器
        boolean enableImageEditor = this.enableImageEditorCheckBox.isSelected();
        int selectedEditorIndex = this.imageEditorComboBox.getSelectedIndex();
        ImageEditorEnum currentEditor = getImageEditorOption(selectedEditorIndex);
        ImageEditorEnum stateEditor = normalizeImageEditor(state.getImageEditor());
        boolean editorEquals = false;
        if (currentEditor == null || stateEditor == null) {
            editorEquals = (currentEditor == stateEditor);
        } else {
            editorEquals = currentEditor.equals(stateEditor);
        }

        return !(changeToHtmlTag == state.isChangeToHtmlTag()
                 && tagEnumEquals
                 && customCodeEquals
                 && compress == state.isCompress()
                 && compressPercent == state.getCompressBeforeUploadOfPercent()
                 && webpQuality == state.getWebpQuality()
                 && convertToWebp == state.isConvertToWebp()
                 && rename == state.isRename()
                 && Objects.equals(renameTemplate, state.getRenameTemplate() != null ? state.getRenameTemplate() : "${filename}")
                 && watermark == state.isWatermark()
                 && Objects.equals(watermarkText, state.getWatermarkText())
                 && enableImageEditor == state.isEnableImageEditor()
                 && editorEquals
                 && deleteImage == state.isDeleteImage()
                 && deleteImageWithConfirm == state.isDeleteImageWithConfirm());
    }

    /**
     * 应用图片增强处理配置到状态对象
     * <p>
     * 将界面中的图片增强相关配置（如HTML标签类型、压缩设置、WebP转换、重命名模板等）应用到指定的状态对象中。
     *
     * @param state 状态对象，用于存储图片增强配置信息
     */
    public void applyImageEnhancementConfigs(@NotNull MikState state) {
        state.setChangeToHtmlTag(this.replaceToHtmlTagCheckBox.isSelected());

        if (this.replaceToHtmlTagCheckBox.isSelected()) {
            int selectedIndex = this.htmlTagTypeComboBox.getSelectedIndex();
            // 将下拉框索引映射回枚举值
            ImageMarkEnum tagEnum = ImageMarkEnum.of(selectedIndex);
            // 设置标签类型枚举
            state.setImageMarkEnum(tagEnum);

            // 如果是自定义类型，保存自定义代码
            if (tagEnum == ImageMarkEnum.CUSTOM) {
                state.setCustomTagCode(this.customHtmlTagTextField.getText().trim());
            } else {
                state.setCustomTagCode(""); // 非自定义类型清空自定义代码
            }
        }

        state.setCompress(this.compressCheckBox.isSelected());
        state.setCompressBeforeUploadOfPercent(((Number) this.compressSpinner.getValue()).intValue());

        state.setConvertToWebp(this.convertToWebpCheckBox.isSelected());

        state.setWebpQuality(((Number) this.webpQualitySpinner.getValue()).intValue());

        state.setRename(this.renameCheckBox.isSelected());
        // 保存重命名模板
        state.setRenameTemplate(this.renamePatternTextField.getText().trim());

        state.setWatermark(this.watermarkCheckBox.isSelected());
        state.setWatermarkText(this.watermarkTextTextField.getText().trim());

        // 图片编辑器
        state.setEnableImageEditor(this.enableImageEditorCheckBox.isSelected());
        int selectedEditorIndex = this.imageEditorComboBox.getSelectedIndex();
        ImageEditorEnum editorEnum = getImageEditorOption(selectedEditorIndex);
        state.setImageEditor(editorEnum != null ? editorEnum : ImageEditorEnum.CLEANSHOT_X);

        // 删除图片
        state.setDeleteImage(this.deleteImageCheckBox.isSelected());
        state.setDeleteImageWithConfirm(this.deleteImageWithConfirmCheckBox.isSelected());
    }

    /**
     * 设置面板所有组件的启用/禁用状态
     * <p>
     * 当全局开关改变时，联动控制所有子组件的可用状态
     *
     * @param enabled true 启用所有组件，false 禁用所有组件
     */
    public void setAllComponentsEnabled(boolean enabled) {
        replaceToHtmlTagCheckBox.setEnabled(enabled);
        htmlTagTypeComboBox.setEnabled(enabled && replaceToHtmlTagCheckBox.isSelected());
        customHtmlTagTextField.setEnabled(enabled && replaceToHtmlTagCheckBox.isSelected()
                                          && htmlTagTypeComboBox.getSelectedIndex() == ImageMarkEnum.CUSTOM.index);

        renameCheckBox.setEnabled(enabled);
        renamePatternTextField.setEnabled(enabled && renameCheckBox.isSelected());
        renamePlaceholderHintLabel.setEnabled(enabled && renameCheckBox.isSelected());

        compressCheckBox.setEnabled(enabled);
        compressSpinner.setEnabled(enabled && compressCheckBox.isSelected());

        convertToWebpCheckBox.setEnabled(enabled);
        webpQualitySpinner.setEnabled(enabled && convertToWebpCheckBox.isSelected());

        watermarkCheckBox.setEnabled(enabled);
        watermarkTextTextField.setEnabled(enabled && watermarkCheckBox.isSelected());

        enableImageEditorCheckBox.setEnabled(enabled);
        imageEditorComboBox.setEnabled(enabled && enableImageEditorCheckBox.isSelected());

        deleteImageCheckBox.setEnabled(enabled);
        deleteImageWithConfirmCheckBox.setEnabled(enabled && deleteImageCheckBox.isSelected());
    }

    private static String[] getImageEditorOptionNames() {
        String[] names = new String[IMAGE_EDITOR_OPTIONS.length];
        for (int i = 0; i < IMAGE_EDITOR_OPTIONS.length; i++) {
            names[i] = IMAGE_EDITOR_OPTIONS[i].getName();
        }
        return names;
    }

    private static int getImageEditorOptionIndex(ImageEditorEnum editor) {
        ImageEditorEnum normalizedEditor = normalizeImageEditor(editor);
        for (int i = 0; i < IMAGE_EDITOR_OPTIONS.length; i++) {
            if (IMAGE_EDITOR_OPTIONS[i] == normalizedEditor) {
                return i;
            }
        }
        for (int i = 0; i < IMAGE_EDITOR_OPTIONS.length; i++) {
            if (IMAGE_EDITOR_OPTIONS[i] == ImageEditorEnum.CLEANSHOT_X) {
                return i;
            }
        }
        return 0;
    }

    private static ImageEditorEnum getImageEditorOption(int index) {
        if (index < 0 || index >= IMAGE_EDITOR_OPTIONS.length) {
            return null;
        }
        return IMAGE_EDITOR_OPTIONS[index];
    }

    private static ImageEditorEnum normalizeImageEditor(ImageEditorEnum editor) {
        if (editor == null || editor == ImageEditorEnum.DRAWIO) {
            return ImageEditorEnum.CLEANSHOT_X;
        }
        return editor;
    }
}
