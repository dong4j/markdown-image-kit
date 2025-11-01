package info.dong4j.idea.plugin.settings.panel;

import com.intellij.ui.components.JBLabel;
import com.intellij.util.ui.JBUI;

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
 *
 *
 * @author dong4j
 * @version 1.0.0
 * @email "mailto:dong4j@gmail.com"
 * @date 2025.10.31 22:42
 * @since x.x.x
 */
@SuppressWarnings("ALL")
public class ImageEnhancementPanel {

    // ========== 图片增强处理区域 ==========
    /**
     * 图片增强处理面板
     */
    @Getter
    private JPanel content;
    /**
     * 替换为 <a> 标签复选框
     * <p>
     * 【字段映射】对应老页面的 changeToHtmlTagCheckBox
     */
    private JCheckBox replaceToHtmlTagCheckBox;
    /**
     * 正常 HTML 标签单选按钮
     * <p>
     * 【字段映射】对应老页面的 commonRadioButton
     */
    private JRadioButton normalHtmlTagRadioButton;
    /**
     * 点击放大 HTML 标签单选按钮
     * <p>
     * 【字段映射】对应老页面的 largePictureRadioButton
     */
    private JRadioButton clickToZoomHtmlTagRadioButton;
    /**
     * 自定义 HTML 标签单选按钮
     * <p>
     * 【字段映射】对应老页面的 customRadioButton
     */
    private JRadioButton customHtmlTagRadioButton;
    /**
     * 自定义 HTML 标签输入框
     * <p>
     * 【字段映射】对应老页面的 customHtmlTypeTextField
     */
    private JTextField customHtmlTagTextField;
    /**
     * 图片压缩复选框
     * <p>
     * 【字段映射】对应老页面的 compressCheckBox
     */
    private JCheckBox compressCheckBox;
    /**
     * 压缩比例微调器
     * <p>
     * 【字段映射】对应老页面的 compressSlider (JSlider)，新页面改为 JSpinner 以便更精确控制
     */
    private JSpinner compressSpinner;
    /**
     * 转为 WebP 复选框
     * <p>
     * 【字段映射】对应老页面的 convertToWebpCheckBox
     */
    private JCheckBox convertToWebpCheckBox;
    /**
     * WebP 质量微调器
     * <p>
     * 【新功能】老页面没有单独的质量控制，新页面增加了 WebP 质量设置
     */
    private JSpinner webpQualitySpinner;
    /**
     * 图片重命名复选框
     * <p>
     * 【字段映射】对应老页面的 renameCheckBox
     */
    private JCheckBox renameCheckBox;
    /**
     * 重命名模式输入框（支持占位符）
     * <p>
     * 【字段映射】对应老页面的 fileNameSuffixBoxField，但新页面改为文本输入框，支持更灵活的占位符格式
     * <p>
     * 注意：老页面使用 ComboBox 选择固定模式（文件名、日期-文件名、随机），新页面改为文本输入框支持自定义占位符
     */
    private JTextField renamePatternTextField;
    /**
     * 重命名占位符提示标签
     * <p>
     * 【新功能】
     */
    private JLabel renamePlaceholderHintLabel;
    /**
     * 添加水印复选框
     * <p>
     * 【字段映射】对应老页面的 watermarkCheckBox
     */
    private JCheckBox watermarkCheckBox;
    /**
     * 水印文本输入框
     * <p>
     * 【字段映射】对应老页面的 watermarkTextField
     */
    private JTextField watermarkTextTextField;

    public ImageEnhancementPanel() {
        createImageEnhancementPanel();
    }

    /**
     * 创建图片增强处理区域面板
     */
    private void createImageEnhancementPanel() {
        content = new JPanel();
        content.setLayout(new GridBagLayout());
        content.setBorder(BorderFactory.createTitledBorder("图片处理"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = JBUI.insets(5, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // 替换为 <a> 标签
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 3;
        replaceToHtmlTagCheckBox = new JCheckBox("替换为 <a> 标签");
        content.add(replaceToHtmlTagCheckBox, gbc);

        ButtonGroup htmlTagGroup = new ButtonGroup();

        // 正常 - 相对复选框缩进2个空格宽度
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.gridx = 0;
        gbc.insets = JBUI.insets(5, 30, 5, 10); // 左边距30像素（约2个空格）
        normalHtmlTagRadioButton = new JRadioButton("正常");
        normalHtmlTagRadioButton.setEnabled(false);
        htmlTagGroup.add(normalHtmlTagRadioButton);
        content.add(normalHtmlTagRadioButton, gbc);

        // 点击放大
        gbc.gridy = 2;
        clickToZoomHtmlTagRadioButton = new JRadioButton("点击放大");
        clickToZoomHtmlTagRadioButton.setEnabled(false);
        htmlTagGroup.add(clickToZoomHtmlTagRadioButton);
        content.add(clickToZoomHtmlTagRadioButton, gbc);

        // 自定义
        gbc.gridy = 3;
        customHtmlTagRadioButton = new JRadioButton("自定义:");
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
        customHtmlTagRadioButton.addActionListener(e -> {
            customHtmlTagTextField.setEnabled(replaceToHtmlTagCheckBox.isSelected() && customHtmlTagRadioButton.isSelected());
        });

        // 图片压缩
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 1;
        gbc.weightx = 0;
        gbc.insets = JBUI.insets(5, 10); // 确保复选框使用统一的间距设置
        compressCheckBox = new JCheckBox("图片压缩");
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

        compressCheckBox.addActionListener(e -> {
            compressSpinner.setEnabled(compressCheckBox.isSelected());
        });

        // 转为 WebP
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 1;
        gbc.weightx = 0;
        gbc.insets = JBUI.insets(5, 10); // 确保复选框使用统一的间距设置
        convertToWebpCheckBox = new JCheckBox("转为 WebP");
        convertToWebpCheckBox.setToolTipText("将图片转换为 WebP 格式以减小文件大小");
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

        convertToWebpCheckBox.addActionListener(e -> {
            webpQualitySpinner.setEnabled(convertToWebpCheckBox.isSelected());
        });

        // 图片重命名 - 复选框和输入框同一行
        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.gridwidth = 1;
        gbc.weightx = 0;
        gbc.insets = JBUI.insets(5, 10); // 确保复选框使用统一的间距设置
        renameCheckBox = new JCheckBox("图片重命名");
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
        gbc.gridy = 7;
        gbc.gridwidth = 3;
        gbc.weightx = 0;
        renamePlaceholderHintLabel = new JBLabel("""
                                                     <html>
                                                         <b>${datetime:format}</b>: 日期时间，如 ${datetime:yyyyMMdd}<br/>
                                                         <b>${string:length}</b>: 随机字符串，如 ${string:6}<br/>
                                                         <b>${number:length}</b>: 随机数字，如 ${number:6}<br/>
                                                         <b>${filename}</b>: 原文件名<br/>
                                                         示例: ${datetime:yyyyMMdd}_${string:6}, ${datetime:yyyy-MM-dd}_${filename}
                                                     </html>
                                                     """);
        renamePlaceholderHintLabel.setEnabled(true);
        content.add(renamePlaceholderHintLabel, gbc);

        renameCheckBox.addActionListener(e -> {
            boolean enabled = renameCheckBox.isSelected();
            renamePatternTextField.setEnabled(enabled);
            renamePlaceholderHintLabel.setEnabled(enabled);
        });

        // 添加水印
        gbc.gridx = 0;
        gbc.gridy = 10;
        gbc.gridwidth = 1;
        gbc.weightx = 0;
        gbc.insets = JBUI.insets(5, 10); // 确保复选框使用统一的间距设置
        watermarkCheckBox = new JCheckBox("添加水印");
        content.add(watermarkCheckBox, gbc);

        gbc.gridy = 10;
        gbc.gridx = 1;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        watermarkTextTextField = new JTextField();
        watermarkTextTextField.setEnabled(false);
        watermarkTextTextField.setToolTipText("请输入水印文本");
        content.add(watermarkTextTextField, gbc);

        watermarkCheckBox.addActionListener(e -> {
            watermarkTextTextField.setEnabled(watermarkCheckBox.isSelected());
        });

    }

    /**
     * 初始化图片增强处理面板
     *
     * @param state 当前状态对象
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
     *
     * @param state 要比较的状态对象
     * @return 如果当前状态与给定状态一致，返回 true；否则返回 false
     */
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
     *
     * @param state 状态对象
     */
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

        //
        state.setConvertToWebp(this.convertToWebpCheckBox.isSelected());

        // TODO: 在 MikState 中添加 webpQuality 字段
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
