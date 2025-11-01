package info.dong4j.idea.plugin.settings;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.ui.JBUI;

import info.dong4j.idea.plugin.MikBundle;
import info.dong4j.idea.plugin.enums.CloudEnum;
import info.dong4j.idea.plugin.util.StringUtils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ItemEvent;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import lombok.Getter;

/**
 * 移动到其他 OSS 设置对话框（Java 版本）
 * <p>
 * 该类用于创建一个图形化界面对话框，允许用户设置目标 OSS 的相关信息，如域名、云服务选择等。
 * 支持选择"本地存储"选项，此时只下载图片到本地而不上传。
 *
 * @author dong4j
 * @version 2.0.0
 * @date 2025.11.01
 * @since 2.0.0
 */
public class MoveToOtherOssSettingsDialog extends DialogWrapper {
    /** 移动所有图片的标识 */
    public static final String MOVE_ALL = "ALL";

    /** 默认域名提示信息 */
    private static final String DOMAIN_DEFAULT_MESSAGE = MikBundle.message("mik.panel.message.domain-field");
    
    /** 域名输入框 */
    @Getter
    private final JBTextField domainTextField;
    
    /** 云服务下拉框 */
    private final ComboBox<CloudOption> cloudComboBox;

    /** 消息标签 */
    private final JBLabel messageLabel;
    
    /**
     * 云服务选项包装类
     * 用于在下拉框中显示云服务类型，支持 null 表示本地存储
     */
    public static class CloudOption {
        @Getter
        private final CloudEnum cloudEnum;
        private final String displayName;

        public CloudOption(@Nullable CloudEnum cloudEnum, @NotNull String displayName) {
            this.cloudEnum = cloudEnum;
            this.displayName = displayName;
        }

        @Override
        public String toString() {
            return displayName;
        }
    }

    /**
     * 构造函数
     * <p>
     * 初始化对话框，设置标题和布局
     *
     * @since 2.0.0
     */
    public MoveToOtherOssSettingsDialog() {
        super((Project) null);

        // 初始化组件
        domainTextField = new JBTextField();
        cloudComboBox = new ComboBox<>();
        messageLabel = new JBLabel();

        setTitle(MikBundle.message("picture.migration.plan.title"));

        // 初始化下拉框选项
        initCloudOptions();

        // 添加监听器
        addListeners();

        init();

        // 初始化 OK 按钮状态
        updateOkButton();
        updateMessage();
    }

    /**
     * 初始化云服务下拉框选项
     *
     * @since 2.0.0
     */
    private void initCloudOptions() {
        // 添加所有云服务枚举
        for (CloudEnum cloudEnum : CloudEnum.values()) {
            cloudComboBox.addItem(new CloudOption(cloudEnum, cloudEnum.title));
        }

        // 添加本地存储选项
        cloudComboBox.addItem(new CloudOption(null, MikBundle.message("oss.title.local")));

        // 设置自定义渲染器
        cloudComboBox.setRenderer(new DefaultListCellRenderer() {
            @Override
            public java.awt.Component getListCellRendererComponent(
                JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {

                JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof CloudOption) {
                    label.setText(((CloudOption) value).displayName);
                }
                return label;
            }
        });
    }

    /**
     * 添加事件监听器
     *
     * @since 2.0.0
     */
    private void addListeners() {
        // 域名输入框监听
        domainTextField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                updateOkButton();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                updateOkButton();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                updateOkButton();
            }
        });

        // 云服务下拉框监听
        cloudComboBox.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                updateOkButton();
                updateMessage();
            }
        });
    }

    /**
     * 创建对话框中心面板
     *
     * @return 中心面板组件
     * @since 2.0.0
     */
    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = JBUI.insets(5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // 第一行：域名标签和输入框
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.0;
        panel.add(new JBLabel(MikBundle.message("picture.migration.plan.domain")), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        panel.add(domainTextField, gbc);

        // 第二行：提示信息
        gbc.gridx = 1;
        gbc.gridy = 1;
        JBLabel hintLabel = new JBLabel(MikBundle.message("picture.migration.plan.hit"));
        hintLabel.setForeground(JBColor.GRAY);
        panel.add(hintLabel, gbc);

        // 第三行：云服务标签和下拉框
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 0.0;
        panel.add(new JBLabel(MikBundle.message("picture.migration.plan.move")), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        panel.add(cloudComboBox, gbc);

        // 第四行：消息标签
        gbc.gridx = 1;
        gbc.gridy = 3;
        messageLabel.setForeground(JBColor.RED);
        panel.add(messageLabel, gbc);

        return panel;
    }

    /**
     * 更新 OK 按钮状态
     * <p>
     * 根据输入有效性和云服务可用性来控制 OK 按钮的启用状态。
     * 不管本地存储还是云存储，都需要输入域名或 "ALL"；
     * 对于云存储，还需要额外检查图床是否可用。
     *
     * @since 2.0.0
     */
    private void updateOkButton() {
        boolean hasValidInput = isValidInput();

        if (isLocalStorage()) {
            // 本地存储：只需要域名有效
            setOKActionEnabled(hasValidInput);
        } else {
            // 云存储：域名有效 + 图床可用
            setOKActionEnabled(hasValidInput && isCloudAvailable());
        }
    }

    /**
     * 更新消息提示
     * <p>
     * 根据云服务可用性更新消息标签的文本和颜色。
     * 只有云存储才需要显示图床状态。
     *
     * @since 2.0.0
     */
    private void updateMessage() {
        if (isLocalStorage()) {
            // 本地存储不显示图床状态
            messageLabel.setText("");
        } else {
            // 云存储显示图床是否可用
            if (isCloudAvailable()) {
                messageLabel.setText("");
            } else {
                messageLabel.setText(MikBundle.message("oss.not.available"));
                messageLabel.setForeground(JBColor.RED);
            }
        }
    }

    /**
     * 检查输入是否有效
     * <p>
     * 验证域名输入是否非空且不是默认提示信息。
     * 接受 "ALL" 或其他有效域名。
     *
     * @return 输入是否有效
     * @since 2.0.0
     */
    private boolean isValidInput() {
        String text = domainTextField.getText().trim();
        return StringUtils.isNotBlank(text) && !text.equals(DOMAIN_DEFAULT_MESSAGE);
    }

    /**
     * 检查云服务是否可用
     * <p>
     * 对于本地存储（cloudEnum 为 null），始终返回 true；
     * 对于云存储，检查对应的图床客户端是否已配置可用。
     *
     * @return 云服务是否可用
     * @since 2.0.0
     */
    private boolean isCloudAvailable() {
        CloudOption selectedOption = getSelectedOption();
        if (selectedOption == null || selectedOption.cloudEnum == null) {
            // 本地存储始终可用
            return true;
        }
        // 检查图床是否已配置
        return OssState.getStatus(selectedOption.cloudEnum.index);
    }

    /**
     * 判断是否选择了本地存储
     *
     * @return 是否为本地存储
     * @since 2.0.0
     */
    public boolean isLocalStorage() {
        CloudOption selectedOption = getSelectedOption();
        return selectedOption != null && selectedOption.cloudEnum == null;
    }

    /**
     * 获取选择的云服务类型
     *
     * @return 选择的云服务类型，如果是本地存储则返回 null
     * @since 2.0.0
     */
    @Nullable
    public CloudEnum getSelectedCloudEnum() {
        CloudOption selectedOption = getSelectedOption();
        return selectedOption != null ? selectedOption.cloudEnum : null;
    }

    /**
     * 获取当前选择的选项
     *
     * @return 当前选择的选项
     * @since 2.0.0
     */
    @Nullable
    private CloudOption getSelectedOption() {
        return (CloudOption) cloudComboBox.getSelectedItem();
    }

    /**
     * 获取域名输入文本
     *
     * @return 域名文本
     * @since 2.0.0
     */
    @NotNull
    public String getDomainText() {
        return domainTextField.getText();
    }

    /**
     * OK 按钮点击处理
     * <p>
     * 验证输入有效性和云服务可用性后执行关闭操作。
     * 不管本地存储还是云存储，都需要输入域名或 "ALL"。
     *
     * @since 2.0.0
     */
    @Override
    protected void doOKAction() {
        boolean hasValidInput = isValidInput();

        if (isLocalStorage()) {
            // 本地存储：只需要域名有效
            if (hasValidInput) {
                super.doOKAction();
            }
        } else {
            // 云存储：域名有效 + 图床可用
            if (hasValidInput && isCloudAvailable()) {
                super.doOKAction();
            }
        }
    }
}

