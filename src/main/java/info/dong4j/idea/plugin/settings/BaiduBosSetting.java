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

package info.dong4j.idea.plugin.settings;

import com.intellij.ui.DocumentAdapter;
import com.intellij.ui.JBColor;

import info.dong4j.idea.plugin.client.BaiduBosClient;
import info.dong4j.idea.plugin.util.DES;
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
 * todo-dong4j : (2021.02.16 01:27) [与 AliyunOssSetting 一起重构]
 *
 * @author dong4j
 * @version 1.0.0
 * @email "mailto:dong4j@fkhwl.com"
 * @date 2021.02.16 00:32
 * @since 1.1.0
 */
public class BaiduBosSetting {
    /** BAIDU_HELPER_DOC */
    private static final String BAIDU_HELPER_DOC = "https://cloud.baidu.com/doc/BOS/s/ckaqihkra";

    private BaiduBosState state;
    private final JTextField bucketNameTextField;
    private final JTextField accessKeyTextField;
    private final JPasswordField accessSecretKeyTextField;
    private final JTextField endpointTextField;
    private final JTextField fileDirTextField;
    private final JCheckBox customEndpointCheckBox;
    private final JTextField customEndpointTextField;
    private final JLabel customEndpointHelper;
    private final JTextField exampleTextField;

    public BaiduBosSetting(JTextField bucketNameTextField,
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
     * 初始化 baidu bos 认证相关设置
     *
     * @since 0.0.1
     */
    void init(BaiduBosState state) {
        this.state = state;
        String accessSecretKey = this.state.getAccessSecretKey();
        this.accessSecretKeyTextField.setText(DES.decrypt(accessSecretKey, MikState.BAIDU));

        // 处理当 fileDirTextField.getText() 为 空字符时, 不拼接 "/
        this.setExampleText(false);

        DocumentAdapter documentAdapter = new DocumentAdapter() {
            @Override
            protected void textChanged(@NotNull DocumentEvent e) {
                BaiduBosSetting.this.setExampleText(false);
            }
        };
        DocumentAdapter customDocumentAdapter = new DocumentAdapter() {
            @Override
            protected void textChanged(@NotNull DocumentEvent e) {
                BaiduBosSetting.this.setExampleText(true);
            }
        };

        // 监听 bucketNameTextField
        this.bucketNameTextField.getDocument().addDocumentListener(documentAdapter);
        // 监听 endpointTextField
        this.endpointTextField.getDocument().addDocumentListener(documentAdapter);
        // 设置 fileDirTextField 输入的监听
        this.fileDirTextField.getDocument().addDocumentListener(documentAdapter);

        this.change(customDocumentAdapter, this.state.getIsCustomEndpoint());

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

        if (isSelected) {
            this.showCustomEndpointHelper();
            // 开启自定义 endpoint 时, example 修改为自定义 endpoint
            this.customEndpointTextField.getDocument().addDocumentListener(customDocumentAdapter);
        } else {
            this.customEndpointHelper.setText("");
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
            url = BaiduBosClient.URL_PROTOCOL_HTTPS + "://" + this.customEndpointTextField.getText();

        } else {
            fileDir = StringUtils.isBlank(this.fileDirTextField.getText().trim()) ? "" :
                      "/" + this.fileDirTextField.getText().trim();
            String endpoint = this.endpointTextField.getText().trim();
            String backetName = this.bucketNameTextField.getText().trim();
            url = BaiduBosClient.URL_PROTOCOL_HTTPS + "://" + backetName + "." + endpoint;
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
        this.customEndpointHelper.setText("<html><a href='" + BAIDU_HELPER_DOC + "'>自定义 Endpoint 帮助文档</a></html>");
        // 设置链接颜色
        this.customEndpointHelper.setForeground(JBColor.WHITE);
        // 设置鼠标样式
        this.customEndpointHelper.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        // 设置提示文字
        this.customEndpointHelper.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                try {
                    Desktop.getDesktop().browse(new URI(BAIDU_HELPER_DOC));
                } catch (Exception ignored) {
                }
            }
        });
    }

    boolean isModified() {
        String bucketName = this.bucketNameTextField.getText().trim();
        String accessKey = this.accessKeyTextField.getText().trim();
        String secretKey = new String(this.accessSecretKeyTextField.getPassword());
        if (StringUtils.isNotBlank(secretKey)) {
            secretKey = DES.encrypt(secretKey, MikState.BAIDU);
        }
        String endpoint = this.endpointTextField.getText().trim();
        String filedir = this.fileDirTextField.getText().trim();
        String customEndpoint = this.customEndpointTextField.getText().trim();
        boolean isCustomEndpoint = this.customEndpointCheckBox.isSelected();

        return bucketName.equals(this.state.getBucketName())
               && accessKey.equals(this.state.getAccessKey())
               && secretKey.equals(this.state.getAccessSecretKey())
               && endpoint.equals(this.state.getEndpoint())
               && filedir.equals(this.state.getFiledir())
               && this.state.getIsCustomEndpoint() == isCustomEndpoint
               && customEndpoint.equals(this.state.getCustomEndpoint());
    }

    void apply() {
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
                       endpoint.hashCode();
        OssState.saveStatus(this.state, hashcode, MikState.NEW_HASH_KEY);

        if (StringUtils.isNotBlank(accessSecretKey)) {
            accessSecretKey = DES.encrypt(accessSecretKey, MikState.BAIDU);
        }

        this.state.setBucketName(bucketName);
        this.state.setAccessKey(accessKey);
        this.state.setAccessSecretKey(accessSecretKey);
        this.state.setEndpoint(endpoint);
        this.state.setCustomEndpoint(customEndpoint);
        this.state.setIsCustomEndpoint(isCustomEndpoint);
        this.state.setFiledir(this.fileDirTextField.getText().trim());
    }

    public void reset(BaiduBosState state) {
        this.state = state;
        this.bucketNameTextField.setText(this.state.getBucketName());
        this.accessKeyTextField.setText(this.state.getAccessKey());
        String accessSecretKey = this.state.getAccessSecretKey();
        this.accessSecretKeyTextField.setText(DES.decrypt(accessSecretKey, MikState.BAIDU));
        this.endpointTextField.setText(this.state.getEndpoint());
        this.fileDirTextField.setText(this.state.getFiledir());

        this.customEndpointCheckBox.setSelected(this.state.getIsCustomEndpoint());
        this.customEndpointTextField.setText(this.state.getCustomEndpoint());
    }
}
