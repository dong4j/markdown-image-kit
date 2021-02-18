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

import info.dong4j.idea.plugin.enums.ZoneEnum;
import info.dong4j.idea.plugin.settings.MikState;
import info.dong4j.idea.plugin.settings.OssState;
import info.dong4j.idea.plugin.swing.JTextFieldHintListener;
import info.dong4j.idea.plugin.util.PasswordManager;

import org.jetbrains.annotations.NotNull;

import java.awt.event.ActionListener;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPasswordField;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

/**
 * <p>Company: 成都返空汇网络技术有限公司</p>
 * <p>Description:  </p>
 *
 * @author dong4j
 * @version 1.0.0
 * @email "mailto:dong4j@fkhwl.com"
 * @date 2021.02.17 13:48
 * @since 1.4.0
 */
public class QiniuOssSetting implements OssSetting<QiniuOssState> {
    /** CREDENTIAL_ATTRIBUTES */
    public static final CredentialAttributes CREDENTIAL_ATTRIBUTES =
        PasswordManager.buildCredentialAttributes(QiniuOssSetting.class.getName(),
                                                  "QINIUOSS_SETTINGS_PASSWORD_KEY",
                                                  QiniuOssSetting.class);

    /** DOMAIN_HINT */
    private static final String DOMAIN_HINT = "http(s)://domain/";

    /** Qiniu oss bucket name text field */
    private final JTextField qiniuOssBucketNameTextField;
    /** Qiniu oss access key text field */
    private final JTextField qiniuOssAccessKeyTextField;
    /** Qiniu oss access secret key text field */
    private final JPasswordField qiniuOssAccessSecretKeyTextField;
    /** Qiniu oss up host text field */
    private final JTextField qiniuOssUpHostTextField;
    /** Qiniu oss east china radio button */
    private final JRadioButton qiniuOssEastChinaRadioButton;
    /** Qiniu oss nort china radio button */
    private final JRadioButton qiniuOssNortChinaRadioButton;
    /** Qiniu oss south china radio button */
    private final JRadioButton qiniuOssSouthChinaRadioButton;
    /** Qiniu oss north ameria radio button */
    private final JRadioButton qiniuOssNorthAmeriaRadioButton;
    /** Zone index text filed */
    private final JTextField zoneIndexTextFiled;
    /** 按钮 group */
    private final JButton testButton;
    /** Test message */
    private final JLabel testMessage;

    /**
     * Qiniu oss setting
     *
     * @param qiniuOssBucketNameTextField      qiniu oss bucket name text field
     * @param qiniuOssAccessKeyTextField       qiniu oss access key text field
     * @param qiniuOssAccessSecretKeyTextField qiniu oss access secret key text field
     * @param qiniuOssUpHostTextField          qiniu oss up host text field
     * @param qiniuOssEastChinaRadioButton     qiniu oss east china radio button
     * @param qiniuOssNortChinaRadioButton     qiniu oss nort china radio button
     * @param qiniuOssSouthChinaRadioButton    qiniu oss south china radio button
     * @param qiniuOssNorthAmeriaRadioButton   qiniu oss north ameria radio button
     * @param zoneIndexTextFiled               zone index text filed
     * @param testButton                       test button
     * @param testMessage                      test message
     * @since 1.4.0
     */
    public QiniuOssSetting(JTextField qiniuOssBucketNameTextField,
                           JTextField qiniuOssAccessKeyTextField,
                           JPasswordField qiniuOssAccessSecretKeyTextField,
                           JTextField qiniuOssUpHostTextField,
                           JRadioButton qiniuOssEastChinaRadioButton,
                           JRadioButton qiniuOssNortChinaRadioButton,
                           JRadioButton qiniuOssSouthChinaRadioButton,
                           JRadioButton qiniuOssNorthAmeriaRadioButton,
                           JTextField zoneIndexTextFiled,
                           JButton testButton,
                           JLabel testMessage) {

        this.qiniuOssBucketNameTextField = qiniuOssBucketNameTextField;
        this.qiniuOssAccessKeyTextField = qiniuOssAccessKeyTextField;
        this.qiniuOssAccessSecretKeyTextField = qiniuOssAccessSecretKeyTextField;
        this.qiniuOssUpHostTextField = qiniuOssUpHostTextField;
        this.qiniuOssEastChinaRadioButton = qiniuOssEastChinaRadioButton;
        this.qiniuOssNortChinaRadioButton = qiniuOssNortChinaRadioButton;
        this.qiniuOssSouthChinaRadioButton = qiniuOssSouthChinaRadioButton;
        this.qiniuOssNorthAmeriaRadioButton = qiniuOssNorthAmeriaRadioButton;
        this.zoneIndexTextFiled = zoneIndexTextFiled;
        this.testButton = testButton;
        this.testMessage = testMessage;

    }

    /**
     * Init
     *
     * @param state state
     * @since 0.0.1
     */
    @Override
    public void init(QiniuOssState state) {
        this.qiniuOssAccessSecretKeyTextField.setText(PasswordManager.getPassword(CREDENTIAL_ATTRIBUTES));

        this.qiniuOssUpHostTextField.addFocusListener(new JTextFieldHintListener(this.qiniuOssUpHostTextField, DOMAIN_HINT));

        ButtonGroup group = new ButtonGroup();
        this.qiniuOssEastChinaRadioButton.setMnemonic(ZoneEnum.EAST_CHINA.index);
        this.qiniuOssNortChinaRadioButton.setMnemonic(ZoneEnum.NORT_CHINA.index);
        this.qiniuOssSouthChinaRadioButton.setMnemonic(ZoneEnum.SOUTH_CHINA.index);
        this.qiniuOssNorthAmeriaRadioButton.setMnemonic(ZoneEnum.NORTH_AMERIA.index);

        this.addZoneRadioButton(group, this.qiniuOssEastChinaRadioButton);
        this.addZoneRadioButton(group, this.qiniuOssNortChinaRadioButton);
        this.addZoneRadioButton(group, this.qiniuOssSouthChinaRadioButton);
        this.addZoneRadioButton(group, this.qiniuOssNorthAmeriaRadioButton);

        this.qiniuOssEastChinaRadioButton.setSelected(state.getZoneIndex() == this.qiniuOssEastChinaRadioButton.getMnemonic());
        this.qiniuOssNortChinaRadioButton.setSelected(state.getZoneIndex() == this.qiniuOssNortChinaRadioButton.getMnemonic());
        this.qiniuOssSouthChinaRadioButton.setSelected(state.getZoneIndex() == this.qiniuOssSouthChinaRadioButton.getMnemonic());
        this.qiniuOssNorthAmeriaRadioButton.setSelected(state.getZoneIndex() == this.qiniuOssNorthAmeriaRadioButton.getMnemonic());
    }

    /**
     * 处理被选中的 zone 单选框
     *
     * @param group  the group
     * @param button the button
     * @since 0.0.1
     */
    private void addZoneRadioButton(@NotNull ButtonGroup group, JRadioButton button) {
        group.add(button);
        ActionListener actionListener = e -> {
            Object sourceObject = e.getSource();
            if (sourceObject instanceof JRadioButton) {
                JRadioButton sourceButton = (JRadioButton) sourceObject;
                this.zoneIndexTextFiled.setText(String.valueOf(sourceButton.getMnemonic()));
                this.testMessage.setText("");
                this.testButton.setText("Test Upload");
            }
        };
        button.addActionListener(actionListener);
    }

    /**
     * Is modified
     *
     * @param state state
     * @return the boolean
     * @since 1.4.0
     */
    @Override
    public boolean isModified(@NotNull QiniuOssState state) {
        String bucketName = this.qiniuOssBucketNameTextField.getText().trim();
        String accessKey = this.qiniuOssAccessKeyTextField.getText().trim();
        String secretKey = new String(this.qiniuOssAccessSecretKeyTextField.getPassword());

        // todo-dong4j : (2019年03月19日 21:01) [重构为 domain]
        String endpoint = JTextFieldHintListener.getRealText(this.qiniuOssUpHostTextField, DOMAIN_HINT);
        // todo-dong4j : (2019年03月19日 21:13) [zone]
        int zoneIndex = Integer.parseInt(this.zoneIndexTextFiled.getText());

        return bucketName.equals(state.getBucketName())
               && accessKey.equals(state.getAccessKey())
               && secretKey.equals(PasswordManager.getPassword(CREDENTIAL_ATTRIBUTES))
               && zoneIndex == state.getZoneIndex()
               && endpoint.equals(state.getEndpoint());
    }

    /**
     * Apply
     *
     * @param state state
     * @since 0.0.1
     */
    @Override
    public void apply(@NotNull QiniuOssState state) {
        // todo-dong4j : (2019年03月19日 21:01) [重构为 domain]
        String endpoint = JTextFieldHintListener.getRealText(this.qiniuOssUpHostTextField, DOMAIN_HINT);
        // todo-dong4j : (2019年03月19日 21:13) [zone]
        String bucketName = this.qiniuOssBucketNameTextField.getText().trim();
        String accessKey = this.qiniuOssAccessKeyTextField.getText().trim();
        String secretKey = new String(this.qiniuOssAccessSecretKeyTextField.getPassword());
        int zoneIndex = Integer.parseInt(this.zoneIndexTextFiled.getText());
        // 需要在加密之前计算 hashcode
        int hashcode = bucketName.hashCode() +
                       accessKey.hashCode() +
                       secretKey.hashCode() +
                       zoneIndex +
                       endpoint.hashCode();
        OssState.saveStatus(state, hashcode, MikState.NEW_HASH_KEY);

        state.setBucketName(bucketName);
        state.setAccessKey(accessKey);
        PasswordManager.setPassword(CREDENTIAL_ATTRIBUTES, secretKey);
        state.setEndpoint(endpoint);
        state.setZoneIndex(zoneIndex);
    }

    /**
     * Reset
     *
     * @param state state
     * @since 1.4.0
     */
    @Override
    public void reset(QiniuOssState state) {
        this.qiniuOssBucketNameTextField.setText(state.getBucketName());
        this.qiniuOssAccessKeyTextField.setText(state.getAccessKey());
        this.qiniuOssAccessSecretKeyTextField.setText(PasswordManager.getPassword(CREDENTIAL_ATTRIBUTES));
        this.qiniuOssUpHostTextField.setText(state.getEndpoint());
        JTextFieldHintListener.init(this.qiniuOssUpHostTextField, DOMAIN_HINT);
        this.zoneIndexTextFiled.setText(String.valueOf(state.getZoneIndex()));
    }
}
