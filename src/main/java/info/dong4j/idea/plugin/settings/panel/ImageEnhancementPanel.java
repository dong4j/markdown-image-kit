package info.dong4j.idea.plugin.settings.panel;

import com.intellij.ui.components.JBLabel;
import com.intellij.util.ui.JBUI;

import info.dong4j.idea.plugin.MikBundle;
import info.dong4j.idea.plugin.enums.ImageMarkEnum;
import info.dong4j.idea.plugin.settings.MikState;

import org.jetbrains.annotations.NotNull;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;

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
    /** 图片增强处理面板 */
    @Getter
    private JPanel content;
    /** 替换为 <a> 标签的复选框，用于控制是否将选项转换为 HTML 标签 */
    private JCheckBox replaceToHtmlTagCheckBox;
    /** 正常 HTML 标签单选按钮，对应老页面的 commonRadioButton */
    private JRadioButton normalHtmlTagRadioButton;
    /** 点击放大 HTML 标签单选按钮，用于表示是否启用点击放大 HTML 标签的功能 */
    private JRadioButton clickToZoomHtmlTagRadioButton;
    /** 自定义 HTML 标签单选按钮 【字段映射】对应老页面的 customRadioButton */
    private JRadioButton customHtmlTagRadioButton;
    /** 自定义 HTML 标签输入框，用于输入自定义的 HTML 标签内容 */
    private JTextField customHtmlTagTextField;
    /** 图片压缩复选框 【字段映射】对应老页面的 compressCheckBox */
    private JCheckBox compressCheckBox;
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
    /** 添加水印的复选框，用于控制是否显示水印功能 */
    private JCheckBox watermarkCheckBox;
    /** 水印文本输入框，用于输入水印内容，【字段映射】对应老页面的 watermarkTextField */
    private JTextField watermarkTextTextField;

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
        content.setBorder(BorderFactory.createTitledBorder(MikBundle.message("panel.image.enhancement.title")));

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
        renamePlaceholderHintLabel.setEnabled(true);
        content.add(renamePlaceholderHintLabel, gbc);

        renameCheckBox.addActionListener(e -> {
            boolean enabled = renameCheckBox.isSelected();
            renamePatternTextField.setEnabled(enabled);
            renamePlaceholderHintLabel.setEnabled(enabled);
        });

        // 图片压缩
        gbc.gridx = 0;
        gbc.gridy = 2;
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
        gbc.gridy = 3;
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
        gbc.gridy = 4;
        gbc.gridwidth = 1;
        gbc.weightx = 0;
        gbc.insets = JBUI.insets(5, 10); // 确保复选框使用统一的间距设置
        watermarkCheckBox = new JCheckBox(MikBundle.message("panel.image.enhancement.watermark"));
        content.add(watermarkCheckBox, gbc);

        gbc.gridy = 4;
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
        gbc.gridy = 5;
        gbc.gridwidth = 3;
        replaceToHtmlTagCheckBox = new JCheckBox(MikBundle.message("panel.image.enhancement.replace.html.tag"));
        content.add(replaceToHtmlTagCheckBox, gbc);

        ButtonGroup htmlTagGroup = new ButtonGroup();

        // 正常 - 相对复选框缩进2个空格宽度
        gbc.gridy = 6;
        gbc.gridwidth = 1;
        gbc.gridx = 0;
        gbc.insets = JBUI.insets(5, 30, 5, 10); // 左边距30像素（约2个空格）
        normalHtmlTagRadioButton = new JRadioButton(MikBundle.message("panel.image.enhancement.html.normal"));
        normalHtmlTagRadioButton.setEnabled(false);
        htmlTagGroup.add(normalHtmlTagRadioButton);
        content.add(normalHtmlTagRadioButton, gbc);

        // 点击放大
        gbc.gridy = 7;
        clickToZoomHtmlTagRadioButton = new JRadioButton(MikBundle.message("panel.image.enhancement.html.zoom"));
        clickToZoomHtmlTagRadioButton.setEnabled(false);
        htmlTagGroup.add(clickToZoomHtmlTagRadioButton);
        content.add(clickToZoomHtmlTagRadioButton, gbc);

        // 自定义
        gbc.gridy = 8;
        customHtmlTagRadioButton = new JRadioButton(MikBundle.message("panel.image.enhancement.html.custom"));
        customHtmlTagRadioButton.setEnabled(false);
        htmlTagGroup.add(customHtmlTagRadioButton);
        content.add(customHtmlTagRadioButton, gbc);

        // 自定义输入框
        gbc.gridx = 1;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        gbc.insets = JBUI.insets(5, 10); // 恢复默认边距
        customHtmlTagTextField = new JTextField();
        customHtmlTagTextField.setEnabled(false);
        customHtmlTagTextField.setToolTipText("请输入自定义 HTML 标签模板");
        content.add(customHtmlTagTextField, gbc);

        // 启用/禁用相关控件
        replaceToHtmlTagCheckBox.addActionListener(e -> {
            boolean enabled = replaceToHtmlTagCheckBox.isSelected();
            normalHtmlTagRadioButton.setEnabled(enabled);
            clickToZoomHtmlTagRadioButton.setEnabled(enabled);
            customHtmlTagRadioButton.setEnabled(enabled);
            customHtmlTagTextField.setEnabled(enabled && customHtmlTagRadioButton.isSelected());
        });
        customHtmlTagRadioButton.addActionListener(e -> customHtmlTagTextField.setEnabled(replaceToHtmlTagCheckBox.isSelected() && customHtmlTagRadioButton.isSelected()));

    }

    /**
     * 初始化图片增强处理面板
     * <p>
     * 根据传入的状态对象初始化图片增强相关的 UI 组件，包括 HTML 标签类型、图片压缩、转为 WebP、图片重命名和水印设置。
     *
     * @param state 当前状态对象，包含图片增强处理的各种配置信息
     */
    public void initImageEnhancementPanel(@NotNull MikState state) {
        // 替换为 HTML 标签
        this.replaceToHtmlTagCheckBox.setSelected(state.isChangeToHtmlTag());

        boolean htmlTagEnabled = state.isChangeToHtmlTag();
        this.normalHtmlTagRadioButton.setEnabled(htmlTagEnabled);
        this.clickToZoomHtmlTagRadioButton.setEnabled(htmlTagEnabled);
        this.customHtmlTagRadioButton.setEnabled(htmlTagEnabled);

        // 根据状态设置选中的单选按钮
        String tagType = state.getTagType();
        if (ImageMarkEnum.COMMON_PICTURE.text.equals(tagType)) {
            this.normalHtmlTagRadioButton.setSelected(true);
        } else if (ImageMarkEnum.LARGE_PICTURE.text.equals(tagType)) {
            this.clickToZoomHtmlTagRadioButton.setSelected(true);
        } else if (ImageMarkEnum.CUSTOM.text.equals(tagType)) {
            this.customHtmlTagRadioButton.setSelected(true);
            this.customHtmlTagTextField.setText(state.getTagTypeCode());
            this.customHtmlTagTextField.setEnabled(htmlTagEnabled);
        } else {
            this.normalHtmlTagRadioButton.setSelected(true);
        }

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
                // 如果模板为空，根据旧的 suffixIndex 设置默认模式（兼容性处理）
                if (state.getSuffixIndex() == 0) {
                    this.renamePatternTextField.setText("${filename}");
                } else if (state.getSuffixIndex() == 1) {
                    this.renamePatternTextField.setText("${datetime:yyyy-MM-dd}_${filename}");
                } else {
                    this.renamePatternTextField.setText("MIK-${string:6}");
                }
            }
        }

        // 水印
        this.watermarkCheckBox.setSelected(state.isWatermark());
        this.watermarkTextTextField.setEnabled(state.isWatermark());
        this.watermarkTextTextField.setText(state.getWatermarkText());
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

        String tagType = "";
        String tagTypeCode = "";
        if (changeToHtmlTag) {
            if (this.normalHtmlTagRadioButton.isSelected()) {
                tagType = ImageMarkEnum.COMMON_PICTURE.text;
                tagTypeCode = ImageMarkEnum.COMMON_PICTURE.code;
            } else if (this.clickToZoomHtmlTagRadioButton.isSelected()) {
                tagType = ImageMarkEnum.LARGE_PICTURE.text;
                tagTypeCode = ImageMarkEnum.LARGE_PICTURE.code;
            } else if (this.customHtmlTagRadioButton.isSelected()) {
                tagType = ImageMarkEnum.CUSTOM.text;
                tagTypeCode = this.customHtmlTagTextField.getText().trim();
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
        return !(changeToHtmlTag == state.isChangeToHtmlTag()
                 && tagType.equals(state.getTagType())
                 && tagTypeCode.equals(state.getTagTypeCode())
                 && compress == state.isCompress()
                 && compressPercent == state.getCompressBeforeUploadOfPercent()
                 && webpQuality == state.getWebpQuality()
                 && convertToWebp == state.isConvertToWebp()
                 && rename == state.isRename()
                 && renameTemplate.equals(state.getRenameTemplate() != null ? state.getRenameTemplate() : "${filename}")
                 && watermark == state.isWatermark()
                 && watermarkText.equals(state.getWatermarkText()));
    }

    /**
     * 应用图片增强处理配置到状态对象
     * <p>
     * 将界面中的图片增强相关配置（如HTML标签类型、压缩设置、WebP转换、重命名模板等）应用到指定的状态对象中。
     *
     * @param state 状态对象，用于存储图片增强配置信息
     */
    @SuppressWarnings("D")
    public void applyImageEnhancementConfigs(@NotNull MikState state) {
        state.setChangeToHtmlTag(this.replaceToHtmlTagCheckBox.isSelected());

        if (this.replaceToHtmlTagCheckBox.isSelected()) {
            if (this.normalHtmlTagRadioButton.isSelected()) {
                state.setTagType(ImageMarkEnum.COMMON_PICTURE.text);
                state.setTagTypeCode(ImageMarkEnum.COMMON_PICTURE.code);
            } else if (this.clickToZoomHtmlTagRadioButton.isSelected()) {
                state.setTagType(ImageMarkEnum.LARGE_PICTURE.text);
                state.setTagTypeCode(ImageMarkEnum.LARGE_PICTURE.code);
            } else if (this.customHtmlTagRadioButton.isSelected()) {
                state.setTagType(ImageMarkEnum.CUSTOM.text);
                state.setTagTypeCode(this.customHtmlTagTextField.getText().trim());
            }
        }

        state.setCompress(this.compressCheckBox.isSelected());
        state.setCompressBeforeUploadOfPercent(((Number) this.compressSpinner.getValue()).intValue());

        state.setConvertToWebp(this.convertToWebpCheckBox.isSelected());

        state.setWebpQuality(((Number) this.webpQualitySpinner.getValue()).intValue());

        state.setRename(this.renameCheckBox.isSelected());
        // 保存重命名模板
        state.setRenameTemplate(this.renamePatternTextField.getText().trim());

        // 兼容性处理：根据重命名模式推断 suffixIndex，保持旧版本兼容
        if (this.renamePatternTextField != null && this.renameCheckBox.isSelected()) {
            String pattern = this.renamePatternTextField.getText().trim();
            if (pattern.contains("${datetime:") || pattern.contains("${yyyy") || pattern.contains("${date")) {
                state.setSuffixIndex(1); // 日期-文件名
            } else if (pattern.contains("${random") || pattern.contains("${string:") || pattern.contains("${number:")) {
                state.setSuffixIndex(2); // 随机
            } else {
                state.setSuffixIndex(0); // 文件名
            }
        }

        state.setWatermark(this.watermarkCheckBox.isSelected());
        state.setWatermarkText(this.watermarkTextTextField.getText().trim());
    }
}
