package info.dong4j.idea.plugin.settings.oss;

import com.intellij.credentialStore.CredentialAttributes;

import info.dong4j.idea.plugin.util.PasswordManager;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

/**
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
    /** BAIDU_HELPER_DOC */
    private static final String HELPER_DOC = "https://gitee.com/help/articles/4228";
    /** GITHUB_API */
    private static final String GITEE_API = "https://gitee.com/api/v5";

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
              exampleTextField);

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
