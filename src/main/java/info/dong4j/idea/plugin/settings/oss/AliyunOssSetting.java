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

import info.dong4j.idea.plugin.util.PasswordManager;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

/**
 * <p>Company: 成都返空汇网络技术有限公司</p>
 * <p>Description:  </p>
 *
 * @author dong4j
 * @version 1.0.0
 * @email "mailto:dong4j@fkhwl.com"
 * @date 2021.02.16 00:28
 * @since 1.1.0
 */
public class AliyunOssSetting extends AbstractOssSetting<AliyunOssState> {
    /** CREDENTIAL_ATTRIBUTES */
    public static final CredentialAttributes CREDENTIAL_ATTRIBUTES =
        PasswordManager.buildCredentialAttributes(AliyunOssSetting.class.getName(),
                                                  "ALIYUNOSS_SETTINGS_PASSWORD_KEY",
                                                  AliyunOssSetting.class);

    /** helperDoc */
    private static final String ALIYUN_HELPER_DOC = "https://help.aliyun.com/document_detail/31836.html";

    /**
     * Aliyun oss setting
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
    public AliyunOssSetting(JTextField bucketNameTextField,
                            JTextField accessKeyTextField,
                            JPasswordField accessSecretKeyTextField,
                            JTextField endpointTextField,
                            JTextField fileDirTextField,
                            JCheckBox customEndpointCheckBox,
                            JTextField customEndpointTextField,
                            JLabel customEndpointHelper,
                            JTextField exampleTextField) {

        super(bucketNameTextField,
              accessKeyTextField,
              accessSecretKeyTextField,
              endpointTextField,
              fileDirTextField,
              customEndpointCheckBox,
              customEndpointTextField,
              customEndpointHelper,
              exampleTextField);
    }

    /**
     * Gets help doc *
     *
     * @return the help doc
     * @since 1.1.0
     */
    @Override
    protected String getHelpDoc() {
        return ALIYUN_HELPER_DOC;
    }

    /**
     * Credential attributes
     *
     * @return the credential attributes
     * @since 1.6.0
     */
    @Override
    protected CredentialAttributes credentialAttributes() {
        return CREDENTIAL_ATTRIBUTES;
    }

}
