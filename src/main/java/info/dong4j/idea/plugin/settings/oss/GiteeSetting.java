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
 * @date 2021.02.16 00:32
 * @since 1.4.0
 */
public class GiteeSetting extends AbstractOpenOssSetting<GiteeOssState> {
    /** CREDENTIAL_ATTRIBUTES */
    public static final CredentialAttributes CREDENTIAL_ATTRIBUTES =
        PasswordManager.buildCredentialAttributes(GiteeSetting.class.getName(),
                                                  "GITEE_SETTINGS_PASSWORD_KEY",
                                                  GiteeSetting.class);
    /** BAIDU_HELPER_DOC formatter:off */
private static final String HELPER_DOC = "https://gitee.com/help/articles/4228";
    /** formatter:on GITHUB_API */
    private static final String GITEE_API = "https://gitee.com/api/v5";
    /** REPOS_HINT */
    private static final String REPOS_HINT = "格式: owner/repos";
    /** BRANCH_HINT */
    private static final String BRANCH_HINT = "与 GitHub 不同, master 就是 master";

    /**
     * Baidu bos setting
     *
     * @param reposTextField          repos text field
     * @param branchTextField         branch text field
     * @param tokenTextField          token text field
     * @param fileDirTextField        file dir text field
     * @param customEndpointCheckBox  custom endpoint check box
     * @param customEndpointTextField custom endpoint text field
     * @param customEndpointHelper    custom endpoint helper
     * @param exampleTextField        example text field
     * @since 1.3.0
     */
    public GiteeSetting(JTextField reposTextField,
                        JTextField branchTextField,
                        JPasswordField tokenTextField,
                        JTextField fileDirTextField,
                        JCheckBox customEndpointCheckBox,
                        JTextField customEndpointTextField,
                        JLabel customEndpointHelper,
                        JTextField exampleTextField) {

        super(reposTextField,
              branchTextField,
              tokenTextField,
              fileDirTextField,
              customEndpointCheckBox,
              customEndpointTextField,
              customEndpointHelper,
              exampleTextField,
              REPOS_HINT,
              BRANCH_HINT);

    }

    /**
     * Gets help doc *
     *
     * @return the help doc
     * @since 1.3.0
     */
    @Override
    protected String getHelpDoc() {
        return HELPER_DOC;
    }

    /**
     * Api
     *
     * @return the string
     * @since 1.4.0
     */
    @Override
    protected String api() {
        return GITEE_API;
    }

    /**
     * Credential attributes
     *
     * @return the string
     * @since 1.6.0
     */
    @Override
    protected CredentialAttributes credentialAttributes() {
        return CREDENTIAL_ATTRIBUTES;
    }
}
