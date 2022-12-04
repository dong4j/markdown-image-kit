/*
 * MIT License
 *
 * Copyright (c) 2022 dong4j <dong4j@gmail.com>
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

import info.dong4j.idea.plugin.settings.MikState;
import info.dong4j.idea.plugin.settings.OssState;
import info.dong4j.idea.plugin.util.PasswordManager;

import org.jetbrains.annotations.NotNull;

import javax.swing.JPasswordField;
import javax.swing.JTextField;

/**
 * <p>Description:  </p>
 *
 * @author dong4j
 * @version 1.0.0
 * @email "mailto:dong4j@fkhwl.com"
 * @date 2021.02.17 14:03
 * @since 1.4.0
 */
public class TencentOssSetting implements OssSetting<TencentOssState> {
    /** CREDENTIAL_ATTRIBUTES */
    public static final CredentialAttributes CREDENTIAL_ATTRIBUTES =
        PasswordManager.buildCredentialAttributes(TencentOssSetting.class.getName(),
                                                  "TENCENTOSS_SETTINGS_PASSWORD_KEY",
                                                  TencentOssSetting.class);

    /** Tencent backet name text field */
    private final JTextField tencentBacketNameTextField;
    /** Tencent access key text field */
    private final JTextField tencentAccessKeyTextField;
    /** Tencent secret key text field */
    private final JPasswordField tencentSecretKeyTextField;
    /** Tencent region name text field */
    private final JTextField tencentRegionNameTextField;

    /**
     * Tencent oss setting
     *
     * @param tencentBacketNameTextField tencent backet name text field
     * @param tencentAccessKeyTextField  tencent access key text field
     * @param tencentSecretKeyTextField  tencent secret key text field
     * @param tencentRegionNameTextField tencent region name text field
     * @since 1.4.0
     */
    public TencentOssSetting(JTextField tencentBacketNameTextField,
                             JTextField tencentAccessKeyTextField,
                             JPasswordField tencentSecretKeyTextField,
                             JTextField tencentRegionNameTextField) {

        this.tencentBacketNameTextField = tencentBacketNameTextField;
        this.tencentAccessKeyTextField = tencentAccessKeyTextField;
        this.tencentSecretKeyTextField = tencentSecretKeyTextField;
        this.tencentRegionNameTextField = tencentRegionNameTextField;

    }

    /**
     * Init
     *
     * @param state state
     * @since 0.0.1
     */
    @Override
    public void init(TencentOssState state) {
        this.tencentSecretKeyTextField.setText(PasswordManager.getPassword(CREDENTIAL_ATTRIBUTES));
        this.tencentAccessKeyTextField.setText(state.getAccessKey());
        this.tencentRegionNameTextField.setText(state.getRegionName());
        this.tencentBacketNameTextField.setText(state.getBucketName());
    }

    /**
     * Is modified
     *
     * @param state state
     * @return the boolean
     * @since 1.4.0
     */
    @Override
    public boolean isModified(@NotNull TencentOssState state) {
        String secretKey = new String(this.tencentSecretKeyTextField.getPassword());

        String bucketName = this.tencentBacketNameTextField.getText().trim();
        String accessKey = this.tencentAccessKeyTextField.getText().trim();

        String regionName = this.tencentRegionNameTextField.getText().trim();

        return bucketName.equals(state.getBucketName())
               && accessKey.equals(state.getAccessKey())
               && secretKey.equals(PasswordManager.getPassword(CREDENTIAL_ATTRIBUTES))
               && regionName.equals(state.getRegionName());
    }

    /**
     * Apply
     *
     * @param state state
     * @since 0.0.1
     */
    @Override
    public void apply(@NotNull TencentOssState state) {
        String accessKey = this.tencentAccessKeyTextField.getText().trim();
        String secretKey = new String(this.tencentSecretKeyTextField.getPassword());
        String regionName = this.tencentRegionNameTextField.getText().trim();
        String bucketName = this.tencentBacketNameTextField.getText().trim();
        // 需要在加密之前计算 hashcode
        int hashcode = bucketName.hashCode() +
                       accessKey.hashCode() +
                       secretKey.hashCode() +
                       regionName.hashCode();

        OssState.saveStatus(state, hashcode, MikState.NEW_HASH_KEY);

        state.setAccessKey(accessKey);
        PasswordManager.setPassword(CREDENTIAL_ATTRIBUTES, secretKey);
        state.setRegionName(regionName);
        state.setBucketName(bucketName);
    }

    /**
     * Reset
     *
     * @param state state
     * @since 1.4.0
     */
    @Override
    public void reset(TencentOssState state) {
        this.tencentAccessKeyTextField.setText(state.getAccessKey());
        this.tencentRegionNameTextField.setText(state.getRegionName());
        this.tencentBacketNameTextField.setText(state.getBucketName());
        this.tencentSecretKeyTextField.setText(PasswordManager.getPassword(CREDENTIAL_ATTRIBUTES));
    }
}
