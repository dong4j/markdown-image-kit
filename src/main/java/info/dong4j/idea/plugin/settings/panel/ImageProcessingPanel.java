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
@SuppressWarnings("DuplicatedCode")
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
        customPathTextField.setToolTipText("请输入相对路径（以 ./ 或 ../ 开头）或绝对路径。支持 ${filename} 和 ${project} 占位符");
        customPathTextField.setVisible(false);
        customPathTextField.setEnabled(false);
        content.add(customPathTextField, gbc);

        // 复选框：对本地位置的图片应用上述规则
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 3;
        gbc.weightx = 0;
        applyToLocalImagesCheckBox = new JCheckBox("对本地位置的图片应用上述规则");
        content.add(applyToLocalImagesCheckBox, gbc);

        // 复选框：对网络位置的图片应用上述规则
        gbc.gridy = 3;
        applyToNetworkImagesCheckBox = new JCheckBox("对网络位置的图片应用上述规则");
        applyToNetworkImagesCheckBox.setToolTipText("在合法的 markdown 图片标签中，如果粘贴的是网络图片，则会直接下载到指定的目录中");
        content.add(applyToNetworkImagesCheckBox, gbc);

        // 图片语法偏好
        gbc.gridy = 4;
        content.add(new JPanel(), gbc); // 分隔线占位

        gbc.gridy = 5;
        gbc.gridwidth = 3;
        content.add(new JBLabel("图片语法偏好:"), gbc);

        gbc.gridy = 6;
        preferRelativePathCheckBox = new JCheckBox("优先使用相对路径");
        preferRelativePathCheckBox.setToolTipText("复制到绝对路径时，自动转换为相对路径");
        content.add(preferRelativePathCheckBox, gbc);

        gbc.gridy = 7;
        addDotSlashCheckBox = new JCheckBox("为相对路径添加 ./");
        addDotSlashCheckBox.setToolTipText("开启后，相对路径会自动添加 ./ 前缀");
        addDotSlashCheckBox.setEnabled(false);
        preferRelativePathCheckBox.addActionListener(e -> {
            addDotSlashCheckBox.setEnabled(preferRelativePathCheckBox.isSelected());
        });
        content.add(addDotSlashCheckBox, gbc);

        gbc.gridy = 8;
        autoEscapeImageUrlCheckBox = new JCheckBox("插入时自动转义图片 URL");
        autoEscapeImageUrlCheckBox.setToolTipText("开启后，插入图片 URL 时会自动进行转义处理");
        content.add(autoEscapeImageUrlCheckBox, gbc);
    }

    // TODO: 检查图片处理设置是否修改（需要在 MikState 中添加对应字段）
    public boolean isImageProcessingModified(@NotNull MikState state) {
        return true;
    }

    // TODO: 应用图片处理设置（需要在 MikState 中添加对应字段）

    public void applyImageProcessingConfigs(@NotNull MikState state) {

    }

    // TODO: 重置图片处理设置（需要在 MikState 中添加对应字段）
    public void initImageProcessingPanel(@NotNull MikState state) {

    }

}
