package info.dong4j.idea.plugin.settings.oss;

import com.intellij.credentialStore.CredentialAttributes;

import info.dong4j.idea.plugin.util.PasswordManager;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

/**
 * Gitee 设置类
 * <p>
 * 该类用于封装与 Gitee 相关的配置信息，继承自 AbstractOpenOssSetting，提供 Gitee 服务的凭证属性、帮助文档和 API 地址等配置项。
 * <p>
 * 主要功能包括：构建凭证属性、获取帮助文档链接、获取 API 接口地址等。
 * <p>
 * 适用于需要与 Gitee 服务进行交互的应用场景，如代码托管、API 调用等。
 *
 * @author dong4j
 * @version 1.0.0
 * @date 2025.10.24
 * @since 1.3.0
 */
public class GiteeSetting extends AbstractOpenOssSetting<GiteeOssState> {
    /** 用于存储 Gitee 身份凭证属性的常量，包含密码键等配置信息 */
    public static final CredentialAttributes CREDENTIAL_ATTRIBUTES =
        PasswordManager.buildCredentialAttributes(GiteeSetting.class.getName(),
                                                  "GITEE_SETTINGS_PASSWORD_KEY",
                                                  GiteeSetting.class);
    /** 项目帮助文档地址 */
    private static final String HELPER_DOC = "https://gitee.com/help/articles/4228";
    /** GITEE_API 是 Gitee 平台的 API 地址 */
    private static final String GITEE_API = "https://gitee.com/api/v5";

    /**
     * 初始化 Gitee 设置组件
     * <p>
     * 通过传入的各个文本字段和组件，初始化 Gitee 设置界面。
     *
     * @param reposTextField          仓库文本字段
     * @param branchTextField         分支文本字段
     * @param tokenTextField          认证令牌文本字段
     * @param fileDirTextField        文件目录文本字段
     * @param customEndpointCheckBox  自定义端点复选框
     * @param customEndpointTextField 自定义端点文本字段
     * @param customEndpointHelper    自定义端点帮助标签
     * @param exampleTextField        示例文本字段
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
     * 获取帮助文档内容
     * <p>
     * 返回系统预设的帮助文档字符串，用于展示给用户或开发者参考。
     *
     * @return 帮助文档内容字符串
     * @since 1.3.0
     */
    @Override
    protected String getHelpDoc() {
        return HELPER_DOC;
    }

    /**
     * 调用GITEE API接口
     * <p>
     * 返回预定义的GITEE API常量字符串
     *
     * @return GITEE API常量字符串
     * @since 1.4.0
     */
    @Override
    protected String api() {
        return GITEE_API;
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
