/*
 * MIT License
 *
 * Copyright (c) 2021 dong4j <dong4j@gmail.com>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package info.dong4j.idea.plugin.settings.oss;

import com.intellij.credentialStore.CredentialAttributes;
import com.intellij.ui.DocumentAdapter;
import com.intellij.ui.JBColor;

import info.dong4j.idea.plugin.client.AbstractOssClient;
import info.dong4j.idea.plugin.settings.MikState;
import info.dong4j.idea.plugin.settings.OssState;
import info.dong4j.idea.plugin.settings.ProjectSettingsPage;
import info.dong4j.idea.plugin.util.PasswordManager;
import info.dong4j.idea.plugin.util.StringUtils;

import org.jetbrains.annotations.NotNull;

import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URI;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;

/**
 * <p>Company: 成都返空汇网络技术有限公司</p>
 * <p>Description:  </p>
 *
 * @param <T> parameter
 * @author dong4j
 * @version 1.0.0
 * @email "mailto:dong4j@fkhwl.com"
 * @date 2021.02.16 13:12
 * @since 1.1.0
 */
public abstract class AbstractOssSetting<T extends AbstractExtendOssState> implements OssSetting<T> {

    /** Aliyun oss bucket name text field */
    private final JTextField bucketNameTextField;
    /** Aliyun oss access key text field */
    private final JTextField accessKeyTextField;
    /** Aliyun oss access secret key text field */
    private final JPasswordField accessSecretKeyTextField;
    /** Aliyun oss endpoint text field */
    private final JTextField endpointTextField;
    /** Aliyun oss file dir text field */
    private final JTextField fileDirTextField;
    /** 是否启用自定义域名 */
    private final JCheckBox customEndpointCheckBox;
    /** 自定义域名 */
    private final JTextField customEndpointTextField;
    /** 自定义域名帮助文档 */
    private final JLabel customEndpointHelper;
    /** Example text field */
    private final JTextField exampleTextField;

    /**
     * Abstract oss setting
     *
     * @param bucketNameTextField      bucket name text field
     * @param accessKeyTextField       access key text field
     * @param accessSecretKeyTextField access secret key text field
     * @param endpointTextField        endpoint text field
     * @param fileDirTextField         file dir text field
     * @param customEndpointCheckBox   custom endpoint check box
     * @param customEndpointTextField  custom endpoint text field
     * @param customEndpointHelper     custom endpoint helper
     * @param exampleTextField         example text field
     * @since 1.1.0
     */
    public AbstractOssSetting(JTextField bucketNameTextField,
                              JTextField accessKeyTextField,
                              JPasswordField accessSecretKeyTextField,
                              JTextField endpointTextField,
                              JTextField fileDirTextField,
                              JCheckBox customEndpointCheckBox,
                              JTextField customEndpointTextField,
                              JLabel customEndpointHelper,
                              JTextField exampleTextField) {

        this.bucketNameTextField = bucketNameTextField;
        this.accessKeyTextField = accessKeyTextField;
        this.accessSecretKeyTextField = accessSecretKeyTextField;
        this.endpointTextField = endpointTextField;
        this.fileDirTextField = fileDirTextField;
        this.customEndpointCheckBox = customEndpointCheckBox;
        this.customEndpointTextField = customEndpointTextField;
        this.customEndpointHelper = customEndpointHelper;
        this.exampleTextField = exampleTextField;
    }

    /**
     * 初始化 oss 认证相关设置
     *
     * @param state state
     * @since 0.0.1
     */
    @Override
    public void init(T state) {
        this.accessSecretKeyTextField.setText(PasswordManager.getPassword(this.credentialAttributes()));

        // 处理当 aliyunOssFileDirTextField.getText() 为 空字符时, 不拼接 "/
        this.setExampleText(false);

        DocumentAdapter documentAdapter = new DocumentAdapter() {
            @Override
            protected void textChanged(@NotNull DocumentEvent e) {
                AbstractOssSetting.this.setExampleText(false);
            }
        };
        DocumentAdapter customDocumentAdapter = new DocumentAdapter() {
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
     * Change
     *
     * @param customDocumentAdapter custom document adapter
     * @param isSelected            is selected
     * @since 1.1.0
     */
    private void change(DocumentAdapter customDocumentAdapter, boolean isSelected) {
        this.customEndpointTextField.setEnabled(isSelected);
        this.customEndpointHelper.setEnabled(isSelected);
        this.endpointTextField.setEnabled(!isSelected);
        this.bucketNameTextField.setEnabled(!isSelected);
        this.fileDirTextField.setEnabled(!isSelected);
        this.showCustomEndpointHelper();

        if (isSelected) {
            // 开启自定义 endpoint 时, example 修改为自定义 endpoint
            this.customEndpointTextField.getDocument().addDocumentListener(customDocumentAdapter);
        }

        // 重置 example
        this.setExampleText(isSelected);
    }

    /**
     * 实时更新此字段
     *
     * @param isCustom is custom
     * @since 0.0.1
     */
    private void setExampleText(boolean isCustom) {
        String fileDir;
        String url;
        if (isCustom) {
            fileDir = StringUtils.isBlank(this.fileDirTextField.getText().trim()) ? "" :
                      "/" + this.fileDirTextField.getText().trim();
            url = AbstractOssClient.URL_PROTOCOL_HTTPS + "://" + this.customEndpointTextField.getText();

        } else {
            fileDir = StringUtils.isBlank(this.fileDirTextField.getText().trim()) ? "" :
                      "/" + this.fileDirTextField.getText().trim();
            String endpoint = this.endpointTextField.getText().trim();
            String backetName = this.bucketNameTextField.getText().trim();
            url = AbstractOssClient.URL_PROTOCOL_HTTPS + "://" + backetName + "." + endpoint;
            this.exampleTextField.setText(url + fileDir + "/" + ProjectSettingsPage.TEST_FILE_NAME);
        }
        this.exampleTextField.setText(url + fileDir + "/" + ProjectSettingsPage.TEST_FILE_NAME);
    }

    /**
     * Show custom endpoint helper
     *
     * @since 1.1.0
     */
    private void showCustomEndpointHelper() {
        // 设置帮助文档链接
        this.customEndpointHelper.setText("<html><a href='" + this.getHelpDoc() + "'>自定义 Endpoint 帮助文档</a></html>");
        // 设置链接颜色
        this.customEndpointHelper.setForeground(JBColor.WHITE);
        // 设置鼠标样式
        this.customEndpointHelper.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        // 设置提示文字
        this.customEndpointHelper.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                try {
                    Desktop.getDesktop().browse(new URI(AbstractOssSetting.this.getHelpDoc()));
                } catch (Exception ignored) {
                }
            }
        });
    }

    /**
     * Is modified
     *
     * @param state state
     * @return the boolean
     * @since 1.1.0
     */
    @Override
    public boolean isModified(@NotNull T state) {
        String bucketName = this.bucketNameTextField.getText().trim();
        String accessKey = this.accessKeyTextField.getText().trim();
        String secretKey = new String(this.accessSecretKeyTextField.getPassword());

        String endpoint = this.endpointTextField.getText().trim();
        String filedir = this.fileDirTextField.getText().trim();
        String customEndpoint = this.customEndpointTextField.getText().trim();
        boolean isCustomEndpoint = this.customEndpointCheckBox.isSelected();

        return bucketName.equals(state.getBucketName())
               && accessKey.equals(state.getAccessKey())
               && secretKey.equals(PasswordManager.getPassword(this.credentialAttributes()))
               && endpoint.equals(state.getEndpoint())
               && filedir.equals(state.getFiledir())
               && state.getIsCustomEndpoint() == isCustomEndpoint
               && customEndpoint.equals(state.getCustomEndpoint());
    }

    /**
     * Apply
     *
     * @param state state
     * @since 1.1.0
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
     * Reset
     *
     * @param state state
     * @since 1.1.0
     */
    @Override
    public void reset(T state) {
        this.bucketNameTextField.setText(state.getBucketName());
        this.accessKeyTextField.setText(state.getAccessKey());
        this.accessSecretKeyTextField.setText(PasswordManager.getPassword(AliyunOssSetting.CREDENTIAL_ATTRIBUTES));
        this.endpointTextField.setText(state.getEndpoint());
        this.fileDirTextField.setText(state.getFiledir());

        this.customEndpointCheckBox.setSelected(state.getIsCustomEndpoint());
        this.customEndpointTextField.setText(state.getCustomEndpoint());
    }

    /**
     * Gets help doc *
     *
     * @return the help doc
     * @since 1.1.0
     */
    protected abstract String getHelpDoc();

    /**
     * Credential attributes
     *
     * @return the credential attributes
     * @since 1.6.0
     */
    protected abstract CredentialAttributes credentialAttributes();

}
