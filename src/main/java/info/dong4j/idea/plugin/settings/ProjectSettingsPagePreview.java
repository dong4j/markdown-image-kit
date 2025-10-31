package info.dong4j.idea.plugin.settings;

import com.intellij.openapi.options.SearchableConfigurable;
import com.intellij.ui.components.JBLabel;
import com.intellij.util.ui.JBUI;

import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;

/**
 * 设置页面预览 - 使用 DLS 方式构建 UI
 * <p>
 * 这是一个预览版本，按照 todo.md 的需求重新组织布局：
 * 1. 图片处理（插入图片时的选项）
 * 2. 图片增强处理（压缩、WebP、重命名、水印等）
 * 3. 上传服务设定
 * <p>
 * 注意：此版本仅用于 UI 预览，不含具体逻辑实现，不影响现有代码
 *
 * @author dong4j
 * @version 2.0.0-preview
 * @date 2025.11.01
 * @since 2.0.0-preview
 */
public class ProjectSettingsPagePreview implements SearchableConfigurable {
    private final JPanel myMainPanel;

    // ========== 图片处理区域 ==========
    private JPanel imageProcessingPanel;
    private JComboBox<String> insertImageComboBox;
    private JTextField customPathTextField;
    private JCheckBox applyToLocalImagesCheckBox;
    private JCheckBox applyToNetworkImagesCheckBox;
    private JCheckBox preferRelativePathCheckBox;
    private JCheckBox addDotSlashCheckBox;
    private JCheckBox autoEscapeImageUrlCheckBox;

    // ========== 图片增强处理区域 ==========
    private JPanel imageEnhancementPanel;
    private JCheckBox replaceToHtmlTagCheckBox;
    private JRadioButton normalHtmlTagRadioButton;
    private JRadioButton clickToZoomHtmlTagRadioButton;
    private JRadioButton customHtmlTagRadioButton;
    private JTextField customHtmlTagTextField;
    private JCheckBox compressCheckBox;
    private JSpinner compressSpinner;
    private JCheckBox convertToWebpCheckBox;
    private JSpinner webpQualitySpinner;
    private JCheckBox renameCheckBox;
    private JComboBox<String> renamePatternComboBox;
    private JTextField renamePatternTextField;
    private JLabel renamePlaceholderHintLabel;
    private JCheckBox watermarkCheckBox;
    private JTextField watermarkTextTextField;

    // ========== 上传服务设定区域 ==========
    private JPanel uploadServicePanel;
    private JComboBox<String> cloudServiceComboBox;
    private JPanel cloudServiceConfigContainer;
    private JCheckBox setAsDefaultCloudCheckBox;
    private JComboBox<String> defaultCloudComboBox;
    private JButton testUploadButton;
    private JButton helpButton;

    // ========== 各个云服务商的配置面板 ==========
    // Sm.ms
    private JPanel smmsConfigPanel;
    private JTextField smmsUrlTextField;
    private JPasswordField smmsTokenTextField;

    // 阿里云 OSS
    private JPanel aliyunOssConfigPanel;
    private JTextField aliyunBucketNameTextField;
    private JTextField aliyunAccessKeyTextField;
    private JPasswordField aliyunAccessSecretTextField;
    private JTextField aliyunEndpointTextField;
    private JTextField aliyunFileDirTextField;
    private JCheckBox aliyunCustomEndpointCheckBox;
    private JTextField aliyunCustomEndpointTextField;

    // 其他云服务商的配置面板...
    // (这里为了简洁，只展示几个主要服务商的结构)

    /**
     * 构造函数
     */
    public ProjectSettingsPagePreview() {
        this.myMainPanel = buildMainPanel();
    }

    /**
     * 创建主面板组件
     */
    @NotNull
    private JPanel buildMainPanel() {
        // 使用 BorderLayout 作为主布局
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // 创建左侧内容面板（使用垂直布局）
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));

        // 1. 图片处理区域
        contentPanel.add(createImageProcessingPanel());

        // 添加间距
        contentPanel.add(new JPanel()); // 占位符

        // 2. 图片增强处理区域
        contentPanel.add(createImageEnhancementPanel());

        // 添加间距
        contentPanel.add(new JPanel()); // 占位符

        // 3. 上传服务设定区域
        contentPanel.add(createUploadServicePanel());

        mainPanel.add(contentPanel, BorderLayout.CENTER);

        return mainPanel;
    }

    /**
     * 创建图片处理区域面板
     */
    @NotNull
    private JPanel createImageProcessingPanel() {
        imageProcessingPanel = new JPanel();
        imageProcessingPanel.setLayout(new GridBagLayout());
        imageProcessingPanel.setBorder(BorderFactory.createTitledBorder("插入图片时..."));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = JBUI.insets(5, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // 下拉选项
        gbc.gridx = 0;
        gbc.gridy = 0;
        imageProcessingPanel.add(new JBLabel("插入图片时的操作:"), gbc);

        gbc.gridx = 1;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        insertImageComboBox = new JComboBox<>(new String[] {
            "无特殊操作",
            "复制图片到当前文件夹 (./)",
            "复制图片到 ./assets 文件夹",
            "复制图片到 ./${filename}.assets 文件夹",
            "上传图片",
            "复制到指定路径"
        });
        insertImageComboBox.addActionListener(e -> {
            boolean showCustomPath = insertImageComboBox.getSelectedIndex() == 5;
            customPathTextField.setVisible(showCustomPath);
            customPathTextField.setEnabled(showCustomPath);
        });
        imageProcessingPanel.add(insertImageComboBox, gbc);

        // 自定义路径输入框
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.weightx = 0;
        imageProcessingPanel.add(new JBLabel("自定义路径:"), gbc);

        gbc.gridx = 1;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        customPathTextField = new JTextField();
        customPathTextField.setToolTipText("请输入相对路径（以 ./ 或 ../ 开头）或绝对路径。支持 ${filename} 和 ${project} 占位符");
        customPathTextField.setVisible(false);
        customPathTextField.setEnabled(false);
        imageProcessingPanel.add(customPathTextField, gbc);

        // 复选框：对本地位置的图片应用上述规则
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 3;
        gbc.weightx = 0;
        applyToLocalImagesCheckBox = new JCheckBox("对本地位置的图片应用上述规则");
        imageProcessingPanel.add(applyToLocalImagesCheckBox, gbc);

        // 复选框：对网络位置的图片应用上述规则
        gbc.gridy = 3;
        applyToNetworkImagesCheckBox = new JCheckBox("对网络位置的图片应用上述规则");
        applyToNetworkImagesCheckBox.setToolTipText("在合法的 markdown 图片标签中，如果粘贴的是网络图片，则会直接下载到指定的目录中");
        imageProcessingPanel.add(applyToNetworkImagesCheckBox, gbc);

        // 图片语法偏好
        gbc.gridy = 4;
        imageProcessingPanel.add(new JPanel(), gbc); // 分隔线占位

        gbc.gridy = 5;
        gbc.gridwidth = 3;
        imageProcessingPanel.add(new JBLabel("图片语法偏好:"), gbc);

        gbc.gridy = 6;
        preferRelativePathCheckBox = new JCheckBox("优先使用相对路径");
        preferRelativePathCheckBox.setToolTipText("复制到绝对路径时，自动转换为相对路径");
        imageProcessingPanel.add(preferRelativePathCheckBox, gbc);

        gbc.gridy = 7;
        addDotSlashCheckBox = new JCheckBox("为相对路径添加 ./");
        addDotSlashCheckBox.setToolTipText("开启后，相对路径会自动添加 ./ 前缀");
        addDotSlashCheckBox.setEnabled(false);
        preferRelativePathCheckBox.addActionListener(e -> {
            addDotSlashCheckBox.setEnabled(preferRelativePathCheckBox.isSelected());
        });
        imageProcessingPanel.add(addDotSlashCheckBox, gbc);

        gbc.gridy = 8;
        autoEscapeImageUrlCheckBox = new JCheckBox("插入时自动转义图片 URL");
        autoEscapeImageUrlCheckBox.setToolTipText("开启后，插入图片 URL 时会自动进行转义处理");
        imageProcessingPanel.add(autoEscapeImageUrlCheckBox, gbc);

        return imageProcessingPanel;
    }

    /**
     * 创建图片增强处理区域面板
     */
    @NotNull
    private JPanel createImageEnhancementPanel() {
        imageEnhancementPanel = new JPanel();
        imageEnhancementPanel.setLayout(new GridBagLayout());
        imageEnhancementPanel.setBorder(BorderFactory.createTitledBorder("图片处理"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = JBUI.insets(5, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // 替换为 <a> 标签
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 3;
        replaceToHtmlTagCheckBox = new JCheckBox("替换为 <a> 标签");
        imageEnhancementPanel.add(replaceToHtmlTagCheckBox, gbc);

        ButtonGroup htmlTagGroup = new ButtonGroup();
        // 正常 - 相对复选框缩进2个空格宽度
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.gridx = 0;
        gbc.insets = JBUI.insets(5, 30, 5, 10); // 左边距30像素（约2个空格）
        normalHtmlTagRadioButton = new JRadioButton("正常");
        normalHtmlTagRadioButton.setEnabled(false);
        htmlTagGroup.add(normalHtmlTagRadioButton);
        imageEnhancementPanel.add(normalHtmlTagRadioButton, gbc);

        // 点击放大
        gbc.gridy = 2;
        clickToZoomHtmlTagRadioButton = new JRadioButton("点击放大");
        clickToZoomHtmlTagRadioButton.setEnabled(false);
        htmlTagGroup.add(clickToZoomHtmlTagRadioButton);
        imageEnhancementPanel.add(clickToZoomHtmlTagRadioButton, gbc);

        // 自定义
        gbc.gridy = 3;
        customHtmlTagRadioButton = new JRadioButton("自定义:");
        customHtmlTagRadioButton.setEnabled(false);
        htmlTagGroup.add(customHtmlTagRadioButton);
        imageEnhancementPanel.add(customHtmlTagRadioButton, gbc);

        // 自定义输入框
        gbc.gridx = 1;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        gbc.insets = JBUI.insets(5, 10); // 恢复默认边距
        customHtmlTagTextField = new JTextField();
        customHtmlTagTextField.setEnabled(false);
        customHtmlTagTextField.setToolTipText("请输入自定义 HTML 标签模板");
        imageEnhancementPanel.add(customHtmlTagTextField, gbc);

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
        compressCheckBox = new JCheckBox("图片压缩");
        imageEnhancementPanel.add(compressCheckBox, gbc);

        gbc.gridx = 1;
        gbc.gridwidth = 1; // 改为只占一列
        gbc.weightx = 0; // 不拉伸
        gbc.fill = GridBagConstraints.NONE; // 不填充，保持固定大小
        compressSpinner = new JSpinner(new SpinnerNumberModel(80, 40, 90, 1));
        compressSpinner.setEnabled(false);
        ((JSpinner.DefaultEditor) compressSpinner.getEditor()).getTextField().setColumns(5);
        // 设置 JSpinner 的宽度
        compressSpinner.setPreferredSize(new Dimension(100, compressSpinner.getPreferredSize().height));
        compressSpinner.setMaximumSize(new Dimension(100, compressSpinner.getMaximumSize().height));
        imageEnhancementPanel.add(compressSpinner, gbc);
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
        convertToWebpCheckBox = new JCheckBox("转为 WebP");
        convertToWebpCheckBox.setToolTipText("将图片转换为 WebP 格式以减小文件大小");
        imageEnhancementPanel.add(convertToWebpCheckBox, gbc);

        gbc.gridx = 1;
        gbc.gridwidth = 1; // 改为只占一列
        gbc.weightx = 0; // 不拉伸
        gbc.fill = GridBagConstraints.NONE; // 不填充，保持固定大小
        webpQualitySpinner = new JSpinner(new SpinnerNumberModel(80, 40, 90, 1));
        webpQualitySpinner.setEnabled(false);
        ((JSpinner.DefaultEditor) webpQualitySpinner.getEditor()).getTextField().setColumns(5);
        // 设置 JSpinner 的宽度
        webpQualitySpinner.setPreferredSize(new Dimension(100, webpQualitySpinner.getPreferredSize().height));
        webpQualitySpinner.setMaximumSize(new Dimension(100, webpQualitySpinner.getMaximumSize().height));
        imageEnhancementPanel.add(webpQualitySpinner, gbc);
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
        renameCheckBox = new JCheckBox("图片重命名");
        imageEnhancementPanel.add(renameCheckBox, gbc);

        gbc.gridx = 1;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        renamePatternTextField = new JTextField("${filename}");
        renamePatternTextField.setEnabled(false);
        renamePatternTextField.setToolTipText("命名模板，例如: ${filename}、${yyyy-MM-dd}-${number}-${filename} 等");
        imageEnhancementPanel.add(renamePatternTextField, gbc);

        // 占位符提示标签
        gbc.gridx = 0;
        gbc.gridy = 7;
        gbc.gridwidth = 3;
        gbc.weightx = 0;
        renamePlaceholderHintLabel = new JBLabel("<html><small>支持占位符: ${filename}、${project}、${yyyy-MM-dd}、${number} 等</small></html>");
        renamePlaceholderHintLabel.setEnabled(false);
        imageEnhancementPanel.add(renamePlaceholderHintLabel, gbc);

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
        watermarkCheckBox = new JCheckBox("添加水印");
        imageEnhancementPanel.add(watermarkCheckBox, gbc);

        gbc.gridy = 10;
        gbc.gridx = 1;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        watermarkTextTextField = new JTextField();
        watermarkTextTextField.setEnabled(false);
        watermarkTextTextField.setToolTipText("请输入水印文本");
        imageEnhancementPanel.add(watermarkTextTextField, gbc);

        watermarkCheckBox.addActionListener(e -> {
            watermarkTextTextField.setEnabled(watermarkCheckBox.isSelected());
        });

        return imageEnhancementPanel;
    }

    /**
     * 创建上传服务设定区域面板
     */
    @NotNull
    private JPanel createUploadServicePanel() {
        uploadServicePanel = new JPanel();
        uploadServicePanel.setLayout(new GridBagLayout());
        uploadServicePanel.setBorder(BorderFactory.createTitledBorder("上传服务设定"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = JBUI.insets(5, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // 云服务商下拉选择 - 放在最前面，与配置面板中的标签和输入框对齐
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.weightx = 0;
        uploadServicePanel.add(new JBLabel("图床服务商:"), gbc);

        gbc.gridx = 1;
        gbc.gridwidth = 1;
        gbc.weightx = 1.0; // 设置较大的权重，占比更大
        cloudServiceComboBox = new JComboBox<>(new String[] {
            "Sm.ms",
            "阿里云 OSS",
            "百度云 BOS",
            "GitHub",
            "Gitee",
            "七牛云",
            "腾讯云 COS",
            "自定义 OSS",
            "PicList"
        });
        uploadServicePanel.add(cloudServiceComboBox, gbc);

        // 默认图床复选框和下拉框 - 与选择服务商在同一行
        gbc.gridx = 3;
        gbc.gridwidth = 1;
        gbc.weightx = 0;
        setAsDefaultCloudCheckBox = new JCheckBox("设为默认");
        uploadServicePanel.add(setAsDefaultCloudCheckBox, gbc);

        gbc.gridx = 4;
        gbc.gridwidth = 1;
        gbc.weightx = 1.0;
        defaultCloudComboBox = new JComboBox<>(new String[] {
            "Sm.ms",
            "阿里云 OSS",
            "百度云 BOS",
            "GitHub",
            "Gitee",
            "七牛云",
            "腾讯云 COS",
            "自定义 OSS",
            "PicList"
        });
        defaultCloudComboBox.setEnabled(false);
        uploadServicePanel.add(defaultCloudComboBox, gbc);

        setAsDefaultCloudCheckBox.addActionListener(e -> {
            defaultCloudComboBox.setEnabled(setAsDefaultCloudCheckBox.isSelected());
        });

        // 云服务商配置容器
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 5; // 跨越所有列
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        cloudServiceConfigContainer = new JPanel(new BorderLayout());
        cloudServiceConfigContainer.add(createSmmsConfigPanel(), BorderLayout.CENTER);
        uploadServicePanel.add(cloudServiceConfigContainer, gbc);

        // 切换服务商时更新配置面板
        cloudServiceComboBox.addActionListener(e -> {
            int selectedIndex = cloudServiceComboBox.getSelectedIndex();
            cloudServiceConfigContainer.removeAll();
            JPanel configPanel;
            switch (selectedIndex) {
                case 0:
                    configPanel = createSmmsConfigPanel(); // Sm.ms
                    break;
                case 1:
                    configPanel = createAliyunOssConfigPanel(); // 阿里云 OSS
                    break;
                default:
                    configPanel = new JPanel(); // 其他服务商（占位）
                    break;
            }
            cloudServiceConfigContainer.add(configPanel, BorderLayout.CENTER);
            cloudServiceConfigContainer.revalidate();
            cloudServiceConfigContainer.repaint();
        });

        // 测试按钮和帮助按钮
        gbc.gridy = 2;
        gbc.gridx = 0;
        gbc.gridwidth = 1;
        gbc.weightx = 0;
        gbc.weighty = 0;
        gbc.fill = GridBagConstraints.NONE;
        testUploadButton = new JButton("验证图片上传");
        testUploadButton.setToolTipText("发起上传测试请求");
        uploadServicePanel.add(testUploadButton, gbc);

        gbc.gridx = 1;
        helpButton = new JButton("帮助");
        uploadServicePanel.add(helpButton, gbc);

        return uploadServicePanel;
    }

    /**
     * 创建 Sm.ms 配置面板
     */
    @NotNull
    private JPanel createSmmsConfigPanel() {
        smmsConfigPanel = new JPanel();
        smmsConfigPanel.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = JBUI.insets(5, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0;
        gbc.gridy = 0;
        smmsConfigPanel.add(new JBLabel("API URL:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        smmsUrlTextField = new JTextField();
        smmsConfigPanel.add(smmsUrlTextField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0;
        smmsConfigPanel.add(new JBLabel("Token:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        smmsTokenTextField = new JPasswordField();
        smmsConfigPanel.add(smmsTokenTextField, gbc);

        return smmsConfigPanel;
    }

    /**
     * 创建阿里云 OSS 配置面板
     */
    @NotNull
    private JPanel createAliyunOssConfigPanel() {
        aliyunOssConfigPanel = new JPanel();
        aliyunOssConfigPanel.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = JBUI.insets(5, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0;
        gbc.gridy = 0;
        aliyunOssConfigPanel.add(new JBLabel("Bucket 名称:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        aliyunBucketNameTextField = new JTextField();
        aliyunOssConfigPanel.add(aliyunBucketNameTextField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0;
        aliyunOssConfigPanel.add(new JBLabel("Access Key:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        aliyunAccessKeyTextField = new JTextField();
        aliyunOssConfigPanel.add(aliyunAccessKeyTextField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 0;
        aliyunOssConfigPanel.add(new JBLabel("Access Secret:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        aliyunAccessSecretTextField = new JPasswordField();
        aliyunOssConfigPanel.add(aliyunAccessSecretTextField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.weightx = 0;
        aliyunOssConfigPanel.add(new JBLabel("Endpoint:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        aliyunEndpointTextField = new JTextField();
        aliyunOssConfigPanel.add(aliyunEndpointTextField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.weightx = 0;
        aliyunOssConfigPanel.add(new JBLabel("文件目录:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        aliyunFileDirTextField = new JTextField();
        aliyunOssConfigPanel.add(aliyunFileDirTextField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 2;
        gbc.weightx = 0;
        aliyunCustomEndpointCheckBox = new JCheckBox("启用自定义域名");
        aliyunOssConfigPanel.add(aliyunCustomEndpointCheckBox, gbc);

        gbc.gridy = 6;
        gbc.gridx = 0;
        gbc.gridwidth = 1;
        aliyunOssConfigPanel.add(new JBLabel("自定义域名:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        aliyunCustomEndpointTextField = new JTextField();
        aliyunCustomEndpointTextField.setEnabled(false);
        aliyunOssConfigPanel.add(aliyunCustomEndpointTextField, gbc);

        aliyunCustomEndpointCheckBox.addActionListener(e -> {
            aliyunCustomEndpointTextField.setEnabled(aliyunCustomEndpointCheckBox.isSelected());
        });

        return aliyunOssConfigPanel;
    }

    @Override
    public @Nls(capitalization = Nls.Capitalization.Title) String getDisplayName() {
        return "Markdown Image Kit (Preview)";
    }

    @Override
    public @Nullable JComponent createComponent() {
        return myMainPanel;
    }

    @Override
    public @NotNull String getId() {
        return "markdown.image.kit.preview";
    }

    @Override
    public boolean isModified() {
        // 预览版本，不实现具体逻辑
        return false;
    }

    @Override
    public void apply() {
        // 预览版本，不实现具体逻辑
    }

    @Override
    public void reset() {
        // 预览版本，不实现具体逻辑
    }
}

