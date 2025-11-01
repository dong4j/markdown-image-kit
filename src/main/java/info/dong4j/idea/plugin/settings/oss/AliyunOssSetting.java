package info.dong4j.idea.plugin.settings.oss;

import com.intellij.credentialStore.CredentialAttributes;

import info.dong4j.idea.plugin.util.PasswordManager;

import javax.swing.JCheckBox;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

/**
 * 阿里云OSS设置类
 * <p>
 * 用于配置和管理阿里云对象存储服务（OSS）的相关参数，包括Bucket名称、访问密钥、
 * 密钥秘密、端点、文件目录等信息。该类继承自AbstractOssSetting，提供了通用的OSS设置功能。
 * <p>
 * 该类还包含帮助文档链接，用于指导用户正确配置阿里云OSS参数。
 *
 * @author dong4j
 * @version 1.0.0
 * @date 2021.02.16
 * @since 1.1.0
 */
public class AliyunOssSetting extends AbstractOssSetting<AliyunOssState> {
    /** CREDENTIAL_ATTRIBUTES 表示阿里云 OSS 设置的凭证属性，用于密码管理 */
    public static final CredentialAttributes CREDENTIAL_ATTRIBUTES =
        PasswordManager.buildCredentialAttributes(AliyunOssSetting.class.getName(),
                                                  "ALIYUNOSS_SETTINGS_PASSWORD_KEY");
    /** 阿里云帮助文档链接，用于提供相关技术支持和说明 */
    private static final String ALIYUN_HELPER_DOC = "https://help.aliyun.com/document_detail/31836.html";

    /**
     * 初始化阿里云OSS设置组件
     * <p>
     * 通过传入的各个文本框和组件初始化阿里云OSS设置界面，用于配置OSS相关参数。
     *
     * @param bucketNameTextField      存储桶名称文本框
     * @param accessKeyTextField       访问密钥文本框
     * @param accessSecretKeyTextField 访问密钥密码文本框
     * @param endpointTextField        端点文本框
     * @param fileDirTextField         文件目录文本框
     * @param customEndpointCheckBox   自定义端点复选框
     * @param customEndpointTextField  自定义端点文本框
     * @param exampleTextField         示例文本框
     * @since 1.1.0
     */
    public AliyunOssSetting(JTextField bucketNameTextField,
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
     * 返回系统预定义的帮助文档字符串，用于展示给用户操作指引或使用说明
     *
     * @return 帮助文档内容
     * @since 1.1.0
     */
    @Override
    protected String getHelpDoc() {
        return ALIYUN_HELPER_DOC;
    }

    /**
     * 获取凭证属性信息
     * <p>
     * 返回系统预定义的凭证属性配置信息，用于描述凭证相关的元数据。
     *
     * @return 凭证属性对象
     * @since 1.6.0
     */
    @Override
    protected CredentialAttributes credentialAttributes() {
        return CREDENTIAL_ATTRIBUTES;
    }

}
