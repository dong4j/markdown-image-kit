package info.dong4j.idea.plugin.settings.oss;

import com.intellij.credentialStore.CredentialAttributes;

import info.dong4j.idea.plugin.settings.MikState;
import info.dong4j.idea.plugin.settings.OssState;
import info.dong4j.idea.plugin.util.PasswordManager;

import org.jetbrains.annotations.NotNull;

import javax.swing.JPasswordField;
import javax.swing.JTextField;

/**
 * 腾讯云对象存储（OSS）设置类
 * <p>
 * 该类用于封装腾讯云OSS相关的配置信息，包括存储桶名称、访问密钥、秘密密钥和区域名称等参数的管理。
 * 实现了 OssSetting 接口，用于在配置界面中初始化、判断是否修改、应用和重置腾讯OSS的设置信息。
 * 支持与状态对象的同步，确保配置信息的正确性和一致性。
 *
 * @author dong4j
 * @version 1.0.0
 * @date 2025.10.24
 * @since 1.4.0
 */
public class TencentOssSetting implements OssSetting<TencentOssState> {
    /** CREDENTIAL_ATTRIBUTES 用于标识腾讯云对象存储服务的凭证属性 */
    public static final CredentialAttributes CREDENTIAL_ATTRIBUTES =
        PasswordManager.buildCredentialAttributes(TencentOssSetting.class.getName(),
                                                  "TENCENTOSS_SETTINGS_PASSWORD_KEY");
    /** 腾讯云存储桶名称文本框 */
    private final JTextField tencentBacketNameTextField;
    /** 腾讯访问密钥文本框 */
    private final JTextField tencentAccessKeyTextField;
    /** 腾讯密钥字段文本框，用于输入腾讯密钥信息 */
    private final JPasswordField tencentSecretKeyTextField;
    /** 腾讯区域名称文本框，用于输入或显示腾讯云服务的区域名称 */
    private final JTextField tencentRegionNameTextField;

    /**
     * 初始化腾讯云OSS配置信息
     * <p>
     * 通过传入的文本字段设置腾讯云OSS的相关配置参数，包括存储桶名称、访问密钥、秘密密钥和区域名称。
     *
     * @param tencentBacketNameTextField 存储桶名称输入框
     * @param tencentAccessKeyTextField  访问密钥输入框
     * @param tencentSecretKeyTextField  秘密密钥输入框
     * @param tencentRegionNameTextField 区域名称输入框
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
     * 初始化组件状态
     * <p>
     * 根据传入的 TencentOssState 对象初始化相关文本字段的值
     *
     * @param state 用于初始化的 TencentOssState 对象
     * @since 0.0.1
     */
    @Override
    public void init(TencentOssState state) {
        reset(state);
    }

    /**
     * 判断当前配置是否与给定的腾讯云OSS状态对象一致
     * <p>
     * 通过比较当前输入的Bucket名称、Access Key、Secret Key和Region名称
     * 与给定状态对象中的对应值，判断配置是否已修改。
     *
     * @param state 要比较的腾讯OSS状态对象
     * @return 如果配置一致返回true，否则返回false
     * @since 1.4.0
     */
    @Override
    public boolean isModified(@NotNull TencentOssState state) {
        String bucketName = this.tencentBacketNameTextField.getText().trim();
        String accessKey = this.tencentAccessKeyTextField.getText().trim();
        String regionName = this.tencentRegionNameTextField.getText().trim();

        // 只比较非敏感字段，避免在 EDT 上调用 PasswordManager.getPassword()（慢操作）
        // 密码字段的修改会在 apply() 时保存
        return !(bucketName.equals(state.getBucketName())
                 && accessKey.equals(state.getAccessKey())
                 && regionName.equals(state.getRegionName()));
    }

    /**
     * 应用腾讯云OSS配置信息
     * <p>
     * 从界面获取腾讯云OSS的访问密钥、秘密密钥、区域名称和存储桶名称，并计算哈希值以保存状态。
     * 哈希值用于验证配置信息的一致性。
     *
     * @param state 腾讯OSS状态对象，用于存储配置信息
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
     * 重置腾讯云OSS配置信息
     * <p>
     * 根据传入的腾讯云OSS状态对象，更新界面上的各个配置字段内容
     *
     * @param state 腾讯云OSS状态对象，包含访问密钥、区域名称、存储桶名称等信息
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
