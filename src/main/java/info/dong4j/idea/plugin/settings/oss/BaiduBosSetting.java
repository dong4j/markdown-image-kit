package info.dong4j.idea.plugin.settings.oss;

import com.intellij.credentialStore.CredentialAttributes;

import info.dong4j.idea.plugin.util.PasswordManager;

import javax.swing.JCheckBox;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

/**
 * 百度对象存储服务（BOS）设置类
 * <p>
 * 该类用于配置和管理百度对象存储服务的相关参数，包括存储桶名称、访问密钥、访问密钥密文、端点、文件目录等信息。继承自抽象类 AbstractOssSetting，提供了通用的 OSS 设置功能，并针对百度 BOS 进行了定制化实现。
 * <p>
 * 该类还包含帮助文档链接，用于指导用户如何正确配置百度 BOS 服务。
 *
 * @author dong4j
 * @version 1.0.0
 * @date 2021.02.16
 * @since 1.1.0
 */
public class BaiduBosSetting extends AbstractOssSetting<BaiduBosState> {
    /** CREDENTIAL_ATTRIBUTES 用于存储与百度云存储凭证相关的属性配置 */
    public static final CredentialAttributes CREDENTIAL_ATTRIBUTES =
        PasswordManager.buildCredentialAttributes(BaiduBosSetting.class.getName(),
                                                  "BAIDUBOS_SETTINGS_PASSWORD_KEY");
    /** 百度云对象存储服务（BOS）帮助文档地址 */
    private static final String BAIDU_HELPER_DOC = "https://cloud.baidu.com/doc/BOS/s/ckaqihkra";

    /**
     * 初始化百度BOS设置组件
     * <p>
     * 通过传入的各个文本字段和组件，初始化百度BOS相关的配置界面元素。
     *
     * @param bucketNameTextField      存储桶名称文本框
     * @param accessKeyTextField       访问密钥文本框
     * @param accessSecretKeyTextField 访问密钥密码文本框
     * @param endpointTextField        终端点文本框
     * @param fileDirTextField         文件目录文本框
     * @param customEndpointCheckBox   自定义终端点复选框
     * @param customEndpointTextField  自定义终端点文本框
     * @param exampleTextField         示例文本框
     * @since 1.1.0
     */
    public BaiduBosSetting(JTextField bucketNameTextField,
                           JTextField accessKeyTextField,
                           JPasswordField accessSecretKeyTextField,
                           JTextField endpointTextField,
                           JTextField fileDirTextField,
                           JCheckBox customEndpointCheckBox,
                           JTextField customEndpointTextField,
                           JTextField exampleTextField) {

        super(bucketNameTextField,
              accessKeyTextField,
              accessSecretKeyTextField,
              endpointTextField,
              fileDirTextField,
              customEndpointCheckBox,
              customEndpointTextField,
              exampleTextField);
    }

    /**
     * 获取帮助文档内容
     * <p>
     * 返回系统预设的帮助文档字符串
     *
     * @return 帮助文档内容
     * @since 1.1.0
     */
    @Override
    protected String getHelpDoc() {
        return BAIDU_HELPER_DOC;
    }

    /**
     * 获取凭证属性信息
     * <p>
     * 返回系统预定义的凭证属性配置
     *
     * @return 凭证属性对象
     * @since 1.6.0
     */
    @Override
    protected CredentialAttributes credentialAttributes() {
        return CREDENTIAL_ATTRIBUTES;
    }
}
