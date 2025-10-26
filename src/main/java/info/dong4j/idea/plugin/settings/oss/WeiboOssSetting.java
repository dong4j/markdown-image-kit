package info.dong4j.idea.plugin.settings.oss;

import com.intellij.credentialStore.CredentialAttributes;

import info.dong4j.idea.plugin.settings.MikState;
import info.dong4j.idea.plugin.settings.OssState;
import info.dong4j.idea.plugin.util.PasswordManager;

import org.jetbrains.annotations.NotNull;

import javax.swing.JPasswordField;
import javax.swing.JTextField;

/**
 * 微博OSS设置类
 * <p>
 * 用于管理微博OSS相关的认证配置，包括初始化、应用和重置设置功能。该类实现了OssSetting接口，用于处理微博OSS状态与界面组件之间的绑定和同步。
 * <p>
 * 主要功能包括：
 * - 根据状态初始化微博用户名和密码字段
 * - 检查当前设置是否与给定状态有差异
 * - 应用当前设置到状态对象
 * - 重置设置为指定状态的值
 *
 * @author dong4j
 * @version 1.0.0
 * @date 2025.10.24
 * @since 1.4.0
 */
public class WeiboOssSetting implements OssSetting<WeiboOssState> {
    /** CREDENTIAL_ATTRIBUTES 表示微博 OSS 设置的凭证属性，用于标识和管理密码相关配置 */
    public static final CredentialAttributes CREDENTIAL_ATTRIBUTES =
        PasswordManager.buildCredentialAttributes(WeiboOssSetting.class.getName(),
                                                  "WEIBOOSS_SETTINGS_PASSWORD_KEY",
                                                  WeiboOssSetting.class);
    /** 微博用户名输入框 */
    private final JTextField weiboUserNameTextField;
    /** 微博密码字段 */
    private final JPasswordField weiboPasswordField;

    /**
     * 初始化微博OSS设置对象，绑定用户名和密码的文本字段
     * <p>
     * 该构造函数用于设置微博OSS配置所需的用户名和密码输入框，以便后续获取用户输入的值。
     *
     * @param weiboUserNameTextField 微博用户名输入框
     * @param weiboPasswordField     微博密码输入框
     * @since 1.4.0
     */
    public WeiboOssSetting(JTextField weiboUserNameTextField,
                           JPasswordField weiboPasswordField) {
        this.weiboUserNameTextField = weiboUserNameTextField;
        this.weiboPasswordField = weiboPasswordField;

    }

    /**
     * 初始化微博OSS认证相关设置
     * <p>
     * 设置微博用户名和密码字段的值，用于OSS认证配置
     *
     * @param weiboOssState 微博OSS认证状态对象，包含用户名和密码信息
     * @since 0.0.1
     */
    @Override
    public void init(WeiboOssState weiboOssState) {
        this.weiboUserNameTextField.setText(weiboOssState.getUsername());
        this.weiboPasswordField.setText(PasswordManager.getPassword(CREDENTIAL_ATTRIBUTES));
    }

    /**
     * 判断当前微博状态是否已修改
     * <p>
     * 比较当前输入的微博用户名和密码与给定状态中的值，判断是否发生修改
     *
     * @param state 待比较的微博状态对象
     * @return 如果当前状态与给定状态一致，返回 true；否则返回 false
     * @since 1.4.0
     */
    @Override
    public boolean isModified(@NotNull WeiboOssState state) {
        String weiboUsername = this.weiboUserNameTextField.getText().trim();
        String weiboPassword = new String(this.weiboPasswordField.getPassword());
        String oldWeiboPassword = PasswordManager.getPassword(CREDENTIAL_ATTRIBUTES);
        return weiboUsername.equals(state.getUsername())
               && weiboPassword.equals(oldWeiboPassword);
    }

    /**
     * 应用微博授权配置
     * <p>
     * 处理微博授权配置的保存逻辑，包括获取用户名和密码、计算哈希值并保存状态。
     *
     * @param state 微博授权状态对象
     * @since 0.0.1
     */
    @Override
    public void apply(@NotNull WeiboOssState state) {
        // 处理 weibo 保存时的逻辑 (保存之前必须通过测试, 右键菜单才可用)
        String username = this.weiboUserNameTextField.getText().trim();
        String weiboPassword = new String(this.weiboPasswordField.getPassword());
        // 需要在加密之前计算 hashcode
        int hashcode = username.hashCode() + weiboPassword.hashCode();
        OssState.saveStatus(state, hashcode, MikState.NEW_HASH_KEY);

        state.setUsername(username);
        PasswordManager.setPassword(CREDENTIAL_ATTRIBUTES, weiboPassword);
    }

    /**
     * 重置界面状态为指定的微博OSS状态
     * <p>
     * 根据传入的WeiboOssState对象，设置用户名和密码字段的值
     *
     * @param state 微博OSS状态对象，包含用户名和密码信息
     * @since 1.4.0
     */
    @Override
    public void reset(WeiboOssState state) {
        this.weiboUserNameTextField.setText(state.getUsername());
        this.weiboPasswordField.setText(PasswordManager.getPassword(CREDENTIAL_ATTRIBUTES));
    }

}
