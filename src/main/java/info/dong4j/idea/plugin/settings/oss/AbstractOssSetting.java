package info.dong4j.idea.plugin.settings.oss;

import com.intellij.credentialStore.CredentialAttributes;
import com.intellij.ui.DocumentAdapter;

import info.dong4j.idea.plugin.settings.MikState;
import info.dong4j.idea.plugin.settings.OssState;
import info.dong4j.idea.plugin.util.PasswordManager;

import org.jetbrains.annotations.NotNull;

import javax.swing.JCheckBox;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;

/**
 * 抽象的 OSS 设置类
 * <p>
 * 该类用于抽象化 OSS（对象存储服务）相关配置的管理，提供初始化、应用、重置等通用操作，并支持自定义 Endpoint 的切换和帮助文档的展示。
 * 主要用于封装 OSS 配置界面的通用逻辑，包括 Bucket 名、Access Key、Secret Key、Endpoint、文件目录等字段的管理。
 * 同时支持根据是否启用自定义域名来动态控制相关字段的可用性，并提供示例路径的实时更新功能。
 *
 * @author dong4j
 * @version 1.0.0
 * @date 2021.02.16
 * @since 1.1.0
 */
public abstract class AbstractOssSetting<T extends AbstractExtendOssState> implements OssSetting<T> {
    /** Aliyun OSS 存储桶名称文本输入框 */
    private final JTextField bucketNameTextField;
    /** Aliyun OSS 访问密钥输入框 */
    private final JTextField accessKeyTextField;
    /** Aliyun OSS 访问密钥字段，用于输入和显示访问密钥信息 */
    private final JPasswordField accessSecretKeyTextField;
    /** Aliyun OSS 服务端点文本输入框 */
    private final JTextField endpointTextField;
    /** Aliyun OSS 文件目录文本输入框 */
    private final JTextField fileDirTextField;
    /** 是否启用自定义域名的复选框 */
    private final JCheckBox customEndpointCheckBox;
    /** 自定义域名输入框，用于输入用户配置的自定义域名地址 */
    private final JTextField customEndpointTextField;
    /** 示例文本字段 */
    private final JTextField exampleTextField;

    /**
     * 初始化OSS设置组件
     * <p>
     * 该构造函数用于初始化OSS设置相关的文本框和复选框等UI组件，以便后续配置和操作。
     *
     * @param bucketNameTextField      存储桶名称输入框
     * @param accessKeyTextField       访问密钥输入框
     * @param accessSecretKeyTextField 访问密钥密码输入框
     * @param endpointTextField        端点输入框
     * @param fileDirTextField         文件目录输入框
     * @param customEndpointCheckBox   自定义端点复选框
     * @param customEndpointTextField  自定义端点输入框
     * @param exampleTextField         示例输入框
     * @since 1.1.0
     */
    public AbstractOssSetting(JTextField bucketNameTextField,
                              JTextField accessKeyTextField,
                              JPasswordField accessSecretKeyTextField,
                              JTextField endpointTextField,
                              JTextField fileDirTextField,
                              JCheckBox customEndpointCheckBox,
                              JTextField customEndpointTextField,
                              JTextField exampleTextField) {

        this.bucketNameTextField = bucketNameTextField;
        this.accessKeyTextField = accessKeyTextField;
        this.accessSecretKeyTextField = accessSecretKeyTextField;
        this.endpointTextField = endpointTextField;
        this.fileDirTextField = fileDirTextField;
        this.customEndpointCheckBox = customEndpointCheckBox;
        this.customEndpointTextField = customEndpointTextField;
        this.exampleTextField = exampleTextField;
    }

    /**
     * 初始化 OSS 认证相关设置
     * <p>
     * 该方法用于初始化 OSS 认证所需的字段和监听器，包括设置访问密钥、监听文本框内容变化、
     * 处理自定义端点的切换逻辑等。
     *
     * @param state 用于初始化的配置状态对象
     * @since 0.0.1
     */
    @Override
    public void init(T state) {
        reset(state);

        // 处理当 aliyunOssFileDirTextField.getText() 为 空字符时, 不拼接 "/
        this.setExampleText(false);

        DocumentAdapter documentAdapter = new DocumentAdapter() {
            /**
             * 处理文本内容变化事件
             * <p>
             * 当文本内容发生变化时，调用该方法更新示例文本状态
             *
             * @param e 文本变化事件对象
             */
            @Override
            protected void textChanged(@NotNull DocumentEvent e) {
                AbstractOssSetting.this.setExampleText(false);
            }
        };
        DocumentAdapter customDocumentAdapter = new DocumentAdapter() {
            /**
             * 当文本内容发生变化时触发的方法
             * <p>
             * 该方法在文本内容发生改变时被调用，用于更新示例文本状态
             *
             * @param e 文档事件对象，包含文本变化的相关信息
             */
            @Override
            protected void textChanged(@NotNull DocumentEvent e) {
                AbstractOssSetting.this.setExampleText(true);
            }
        };

        // 监听 aliyunOssBucketNameTextField
        this.bucketNameTextField.getDocument().addDocumentListener(documentAdapter);
        // 监听 aliyunOssEndpointTextField
        this.endpointTextField.getDocument().addDocumentListener(documentAdapter);
        // 设置 aliyunOssFileDirTextField 输入的监听
        this.fileDirTextField.getDocument().addDocumentListener(documentAdapter);

        this.change(customDocumentAdapter, state.getIsCustomEndpoint());

        // 设置 customEndpointCheckBox 监听
        this.customEndpointCheckBox.addChangeListener(e -> {
            JCheckBox checkBox = (JCheckBox) e.getSource();
            this.change(customDocumentAdapter, checkBox.isSelected());
        });
    }

    /**
     * 根据是否选中状态更新相关控件的启用状态，并处理示例文本的显示
     * <p>
     * 当选中状态为 true 时，启用自定义 endpoint 相关控件，并添加文档监听器；否则，禁用这些控件。
     * 同时根据选中状态重置示例文本内容。
     *
     * @param customDocumentAdapter 自定义文档监听器，用于监听文本变化
     * @param isSelected            是否选中状态，true 表示选中，false 表示未选中
     * @since 1.1.0
     */
    private void change(DocumentAdapter customDocumentAdapter, boolean isSelected) {
        this.customEndpointTextField.setEnabled(isSelected);
        // this.endpointTextField.setEnabled(!isSelected);
        this.bucketNameTextField.setEnabled(!isSelected);
        this.fileDirTextField.setEnabled(!isSelected);

        if (isSelected) {
            // 开启自定义 endpoint 时, example 修改为自定义 endpoint
            this.customEndpointTextField.getDocument().addDocumentListener(customDocumentAdapter);
        }

        // 重置 example
        // this.setExampleText(isSelected);
    }

    /**
     * 根据是否为自定义配置更新示例文本字段的内容
     * <p>
     * 根据传入的 isCustom 参数判断使用自定义配置还是默认配置，构建对应的 URL 和文件路径，并更新示例文本字段。
     *
     * @param isCustom 是否为自定义配置
     */
    private void setExampleText(boolean isCustom) {
        // String fileDir;
        // String url;
        // if (isCustom) {
        //     fileDir = StringUtils.isBlank(this.fileDirTextField.getText().trim()) ? "" :
        //               "/" + this.fileDirTextField.getText().trim();
        //     url = AbstractOssClient.URL_PROTOCOL_HTTPS + "://" + this.customEndpointTextField.getText();
        //
        // } else {
        //     fileDir = StringUtils.isBlank(this.fileDirTextField.getText().trim()) ? "" :
        //               "/" + this.fileDirTextField.getText().trim();
        //     String endpoint = this.endpointTextField.getText().trim();
        //     String backetName = this.bucketNameTextField.getText().trim();
        //     url = AbstractOssClient.URL_PROTOCOL_HTTPS + "://" + backetName + "." + endpoint;
        //     this.exampleTextField.setText(url + fileDir + "/" + ProjectSettingsPage.TEST_FILE_NAME);
        // }
        // this.exampleTextField.setText(url + fileDir + "/" + ProjectSettingsPage.TEST_FILE_NAME);
    }

    /**
     * 判断当前状态是否与给定状态修改过
     * <p>
     * 比较当前配置状态与传入的 state 对象，判断是否发生修改。比较内容包括存储桶名称、访问密钥、秘密密钥、端点、文件目录、是否使用自定义端点以及自定义端点地址。
     *
     * @param state 要比较的状态对象
     * @return 如果当前状态与给定状态相同，返回 true；否则返回 false
     */
    @Override
    public boolean isModified(@NotNull T state) {
        String bucketName = this.bucketNameTextField.getText().trim();
        String accessKey = this.accessKeyTextField.getText().trim();

        String endpoint = this.endpointTextField.getText().trim();
        String filedir = this.fileDirTextField.getText().trim();
        String customEndpoint = this.customEndpointTextField.getText().trim();
        boolean isCustomEndpoint = this.customEndpointCheckBox.isSelected();

        // 只比较非敏感字段，避免在 EDT 上调用 PasswordManager.getPassword()（慢操作）
        // 密码字段的修改会在 apply() 时保存
        return !(bucketName.equals(state.getBucketName())
                 && accessKey.equals(state.getAccessKey())
                 && endpoint.equals(state.getEndpoint())
                 && filedir.equals(state.getFiledir())
                 && state.getIsCustomEndpoint() == isCustomEndpoint
                 && customEndpoint.equals(state.getCustomEndpoint()));
    }

    /**
     * 应用配置信息到指定状态对象中
     * <p>
     * 从文本字段中获取OSS相关配置参数，计算哈希值并保存状态。同时设置状态对象的各个属性。
     *
     * @param state 要应用配置的状态对象
     */
    @Override
    public void apply(@NotNull T state) {
        String bucketName = this.bucketNameTextField.getText().trim();
        String accessKey = this.accessKeyTextField.getText().trim();
        String accessSecretKey = new String(this.accessSecretKeyTextField.getPassword());
        String endpoint = this.endpointTextField.getText().trim();
        String customEndpoint = this.customEndpointTextField.getText().trim();
        boolean isCustomEndpoint = this.customEndpointCheckBox.isSelected();

        // 需要在加密之前计算 hashcode
        int hashcode = bucketName.hashCode() +
                       accessKey.hashCode() +
                       accessSecretKey.hashCode() +
                       endpoint.hashCode() +
                       (customEndpoint + isCustomEndpoint).hashCode();

        OssState.saveStatus(state, hashcode, MikState.NEW_HASH_KEY);

        state.setBucketName(bucketName);
        state.setAccessKey(accessKey);
        state.setAccessSecretKey(accessSecretKey);
        PasswordManager.setPassword(this.credentialAttributes(), accessSecretKey);
        state.setEndpoint(endpoint);
        state.setCustomEndpoint(customEndpoint);
        state.setIsCustomEndpoint(isCustomEndpoint);
        state.setFiledir(this.fileDirTextField.getText().trim());
    }

    /**
     * 重置表单字段为指定状态的值
     * <p>
     * 根据传入的 state 对象，将各个文本字段和复选框设置为对应的状态值。
     *
     * @param state 要设置的状态对象，包含桶名、访问密钥、秘密密钥、端点、文件目录、是否使用自定义端点及自定义端点信息
     * @since 1.1.0
     */
    @Override
    public void reset(T state) {
        this.bucketNameTextField.setText(state.getBucketName());
        this.accessKeyTextField.setText(state.getAccessKey());
        this.accessSecretKeyTextField.setText(PasswordManager.getPassword(this.credentialAttributes()));
        this.endpointTextField.setText(state.getEndpoint());
        this.fileDirTextField.setText(state.getFiledir());

        this.customEndpointCheckBox.setSelected(state.getIsCustomEndpoint());
        this.customEndpointTextField.setText(state.getCustomEndpoint());
    }

    /**
     * 获取帮助文档内容
     * <p>
     * 返回系统或模块的帮助文档字符串，用于展示给用户或开发者参考
     *
     * @return 帮助文档内容
     * @since 1.1.0
     */
    protected abstract String getHelpDoc();

    /**
     * 获取凭证属性
     * <p>
     * 返回当前凭证的相关属性信息，具体属性由子类实现。
     *
     * @return 凭证属性
     * @since 1.6.0
     */
    protected abstract CredentialAttributes credentialAttributes();

}
