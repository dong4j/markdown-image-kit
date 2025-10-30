package info.dong4j.idea.plugin.settings.oss;

import com.intellij.credentialStore.CredentialAttributes;

import info.dong4j.idea.plugin.settings.MikState;
import info.dong4j.idea.plugin.settings.OssState;
import info.dong4j.idea.plugin.util.PasswordManager;

import org.jetbrains.annotations.NotNull;

import javax.swing.JPasswordField;
import javax.swing.JTextField;

/**
 * PicList 图床设置类
 * <p>
 * 用于封装和管理 PicList 图床配置信息，包括 API 地址、图床类型、配置名称和密钥等参数的设置与维护。
 * 支持初始化、判断是否修改、应用配置和重置配置等操作。
 *
 * @author dong4j
 * @version 1.0.0
 * @date 2025.10.26
 * @since 1.0.0
 */
public class SmmsOssSetting implements OssSetting<SmmsOssState> {
    public static final CredentialAttributes CREDENTIAL_ATTRIBUTES =
        PasswordManager.buildCredentialAttributes(SmmsOssSetting.class.getName(),
                                                  "SMMSOSS_SETTINGS_TOKEN");

    /** 图床类型文本输入框 */
    private final JPasswordField smmsTokenTextField;

    /**
     * 初始化 PicList 图床设置
     *
     * @param smmsUrlTextField   API 接口文本字段
     * @param smmsTokenTextField 图床类型文本字段
     */
    public SmmsOssSetting(JTextField smmsUrlTextField,
                          JPasswordField smmsTokenTextField) {
        this.smmsTokenTextField = smmsTokenTextField;
    }

    /**
     * 初始化组件，添加焦点监听器以显示提示信息
     *
     * @param state PicList 图床状态对象，用于配置提示信息
     */
    @Override
    public void init(SmmsOssState state) {
        this.smmsTokenTextField.setText(PasswordManager.getPassword(CREDENTIAL_ATTRIBUTES));
    }

    /**
     * 判断当前状态是否已修改
     *
     * @param state 要比较的状态对象
     * @return 如果当前状态与传入状态一致，返回 false；否则返回 true
     */
    @Override
    public boolean isModified(@NotNull SmmsOssState state) {
        String token = new String(this.smmsTokenTextField.getPassword());

        return token.equals(state.getToken());
    }

    /**
     * 将当前界面输入的参数应用到 PicListOssState 对象中
     *
     * @param state 要应用参数的 PicListOssState 对象
     */
    @Override
    public void apply(@NotNull SmmsOssState state) {
        String token = new String(this.smmsTokenTextField.getPassword());
        // 需要在加密之前计算 hashcode
        int hashcode = token.hashCode();
        OssState.saveStatus(state, hashcode, MikState.NEW_HASH_KEY);
        PasswordManager.setPassword(CREDENTIAL_ATTRIBUTES, token);
    }

    /**
     * 重置表单字段为指定状态下的值
     *
     * @param state 包含需要设置的字段值的 PicListOssState 对象
     */
    @Override
    public void reset(SmmsOssState state) {
        this.smmsTokenTextField.setText(PasswordManager.getPassword(CREDENTIAL_ATTRIBUTES));
    }
}
