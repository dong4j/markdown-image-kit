package info.dong4j.idea.plugin.settings.oss;

import com.intellij.credentialStore.CredentialAttributes;

import info.dong4j.idea.plugin.util.PasswordManager;

import javax.swing.JCheckBox;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

/**
 * GitHub 设置类
 * <p>
 * 用于配置和管理 GitHub 相关的存储设置，包括获取帮助文档、API 地址以及凭证属性。
 * 该类继承自 AbstractOpenOssSetting，用于封装 GitHub 存储服务的通用配置逻辑。
 * <p>
 * 主要提供 GitHub 存储服务的 API 地址、帮助文档链接以及凭证属性的获取方法。
 *
 * @author dong4j
 * @version 1.0.0
 * @date 2021.02.16
 * @since 1.3.0
 */
public class GithubSetting extends AbstractOpenOssSetting<GithubOssState> {
    /** CREDENTIAL_ATTRIBUTES 是用于存储 GitHub 身份凭证属性的常量，包含密码键信息 */
    public static final CredentialAttributes CREDENTIAL_ATTRIBUTES =
        PasswordManager.buildCredentialAttributes(GithubSetting.class.getName(),
                                                  "GITHUB_SETTINGS_PASSWORD_KEY");
    /** GitHub 页面帮助文档地址 */
    private static final String HELPER_DOC = "https://docs.github.com/en/github/working-with-github-pages/configuring-a-custom-domain-for-your-github-pages-site";
    /** GitHub API 基础地址 */
    private static final String GITHUB_API = "https://api.github.com";

    /**
     * 初始化 GitHub 设置组件
     * <p>
     * 通过传入的文本字段和复选框等 UI 元素，构建 GitHub 设置界面。
     *
     * @param reposTextField          仓库名称输入框
     * @param branchTextField         分支名称输入框
     * @param tokenTextField          认证令牌输入框
     * @param fileDirTextField        文件目录输入框
     * @param customEndpointCheckBox  自定义端点复选框
     * @param customEndpointTextField 自定义端点输入框
     * @param exampleTextField        示例输入框
     * @since 1.3.0
     */
    public GithubSetting(JTextField reposTextField,
                         JTextField branchTextField,
                         JPasswordField tokenTextField,
                         JTextField fileDirTextField,
                         JCheckBox customEndpointCheckBox,
                         JTextField customEndpointTextField,
                         JTextField exampleTextField) {

        super(reposTextField,
              branchTextField,
              tokenTextField,
              fileDirTextField,
              customEndpointCheckBox,
              customEndpointTextField,
              exampleTextField);

    }

    /**
     * 获取帮助文档内容
     * <p>
     * 返回系统预定义的帮助文档字符串，用于展示给用户操作指引或使用说明
     *
     * @return 帮助文档内容字符串
     * @since 1.3.0
     */
    @Override
    protected String getHelpDoc() {
        return HELPER_DOC;
    }

    /**
     * 调用GitHub API的接口方法
     * <p>
     * 返回预定义的GitHub API地址字符串
     *
     * @return GitHub API地址字符串
     * @since 1.4.0
     */
    @Override
    protected String api() {
        return GITHUB_API;
    }

    /**
     * 获取凭证属性信息
     * <p>
     * 返回系统预定义的凭证属性配置
     *
     * @return 凭证属性信息
     * @since 1.6.0
     */
    @Override
    protected CredentialAttributes credentialAttributes() {
        return CREDENTIAL_ATTRIBUTES;
    }
}
