package info.dong4j.idea.plugin.settings.oss;

import com.intellij.credentialStore.CredentialAttributes;

import info.dong4j.idea.plugin.enums.ZoneEnum;
import info.dong4j.idea.plugin.settings.MikState;
import info.dong4j.idea.plugin.settings.OssState;
import info.dong4j.idea.plugin.swing.JTextFieldHintListener;
import info.dong4j.idea.plugin.util.PasswordManager;

import org.jetbrains.annotations.NotNull;

import java.awt.event.ActionListener;

import javax.swing.ButtonGroup;
import javax.swing.JPasswordField;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

/**
 * 七牛OSS设置类
 * <p>
 * 用于管理七牛OSS相关的配置信息，包括Bucket名称、Access Key、Access Secret Key、上传主机地址、区域选择等。该类实现了OssSetting接口，提供初始化、判断是否修改、应用设置和重置设置等核心功能。
 * <p>
 * 该类通过与{@link QiniuOssState}状态对象交互，实现配置信息的持久化和同步。同时，支持区域选择的单选按钮组，并与区域索引文本框联动，实现区域选择的可视化反馈。
 *
 * @author dong4j
 * @version 1.0.0
 * @date 2025.10.24
 * @since 1.4.0
 */
public class QiniuOssSetting implements OssSetting<QiniuOssState> {
    /** 用于存储七牛OSS设置相关的凭证属性 */
    public static final CredentialAttributes CREDENTIAL_ATTRIBUTES =
        PasswordManager.buildCredentialAttributes(QiniuOssSetting.class.getName(),
                                                  "QINIUOSS_SETTINGS_PASSWORD_KEY",
                                                  QiniuOssSetting.class);
    /** 域名提示，用于构建完整的请求地址 */
    private static final String DOMAIN_HINT = "http(s)://domain/";
    /** 七牛 oss 存储桶名称文本字段 */
    private final JTextField qiniuOssBucketNameTextField;
    /** 七牛 OSS 访问密钥输入框 */
    private final JTextField qiniuOssAccessKeyTextField;
    /** 七牛OSS访问密钥字段 */
    private final JPasswordField qiniuOssAccessSecretKeyTextField;
    /** 七牛 OSS 上传地址输入框 */
    private final JTextField qiniuOssUpHostTextField;
    /** 七牛 oss 东部中国区域选择按钮 */
    private final JRadioButton qiniuOssEastChinaRadioButton;
    /** 七牛 OSS 北中国区域选择按钮 */
    private final JRadioButton qiniuOssNortChinaRadioButton;
    /** 七牛 OSS 南中国区域选择按钮 */
    private final JRadioButton qiniuOssSouthChinaRadioButton;
    /** Qiniu OSS 北美区域选择按钮 */
    private final JRadioButton qiniuOssNorthAmeriaRadioButton;
    /** Zone 索引文本字段 */
    private final JTextField zoneIndexTextFiled;

    /**
     * 初始化七牛OSS设置相关的UI组件
     * <p>
     * 该构造函数用于初始化七牛OSS设置界面所需的各个文本框和单选按钮组件，用于后续的配置操作。
     *
     * @param qiniuOssBucketNameTextField      七牛OSS存储桶名称文本框
     * @param qiniuOssAccessKeyTextField       七牛OSS访问密钥文本框
     * @param qiniuOssAccessSecretKeyTextField 七牛OSS访问密钥秘密文本框
     * @param qiniuOssUpHostTextField          七牛OSS上传主机文本框
     * @param qiniuOssEastChinaRadioButton     七牛OSS华东地区单选按钮
     * @param qiniuOssNortChinaRadioButton     七牛OSS华北地区单选按钮
     * @param qiniuOssSouthChinaRadioButton    七牛OSS华南地区单选按钮
     * @param qiniuOssNorthAmeriaRadioButton   七牛OSS北美地区单选按钮
     * @param zoneIndexTextFiled               区域索引文本框
     * @since 1.4.0
     */
    public QiniuOssSetting(JTextField qiniuOssBucketNameTextField,
                           JTextField qiniuOssAccessKeyTextField,
                           JPasswordField qiniuOssAccessSecretKeyTextField,
                           JTextField qiniuOssUpHostTextField,
                           JRadioButton qiniuOssEastChinaRadioButton,
                           JRadioButton qiniuOssNortChinaRadioButton,
                           JRadioButton qiniuOssSouthChinaRadioButton,
                           JRadioButton qiniuOssNorthAmeriaRadioButton,
                           JTextField zoneIndexTextFiled) {

        this.qiniuOssBucketNameTextField = qiniuOssBucketNameTextField;
        this.qiniuOssAccessKeyTextField = qiniuOssAccessKeyTextField;
        this.qiniuOssAccessSecretKeyTextField = qiniuOssAccessSecretKeyTextField;
        this.qiniuOssUpHostTextField = qiniuOssUpHostTextField;
        this.qiniuOssEastChinaRadioButton = qiniuOssEastChinaRadioButton;
        this.qiniuOssNortChinaRadioButton = qiniuOssNortChinaRadioButton;
        this.qiniuOssSouthChinaRadioButton = qiniuOssSouthChinaRadioButton;
        this.qiniuOssNorthAmeriaRadioButton = qiniuOssNorthAmeriaRadioButton;
        this.zoneIndexTextFiled = zoneIndexTextFiled;

    }

    /**
     * 初始化七牛OSS相关组件
     * <p>
     * 该方法用于设置七牛OSS的访问密钥、上传域名提示以及区域选择按钮的状态。
     * 根据传入的state对象设置默认选中的区域按钮。
     *
     * @param state 七牛OSS状态对象，用于获取当前区域索引
     */
    @Override
    public void init(QiniuOssState state) {
        this.qiniuOssAccessSecretKeyTextField.setText(PasswordManager.getPassword(CREDENTIAL_ATTRIBUTES));

        this.qiniuOssUpHostTextField.addFocusListener(new JTextFieldHintListener(this.qiniuOssUpHostTextField, DOMAIN_HINT));

        ButtonGroup group = new ButtonGroup();
        this.qiniuOssEastChinaRadioButton.setMnemonic(ZoneEnum.EAST_CHINA.index);
        this.qiniuOssNortChinaRadioButton.setMnemonic(ZoneEnum.NORT_CHINA.index);
        this.qiniuOssSouthChinaRadioButton.setMnemonic(ZoneEnum.SOUTH_CHINA.index);
        this.qiniuOssNorthAmeriaRadioButton.setMnemonic(ZoneEnum.NORTH_AMERIA.index);

        this.addZoneRadioButton(group, this.qiniuOssEastChinaRadioButton);
        this.addZoneRadioButton(group, this.qiniuOssNortChinaRadioButton);
        this.addZoneRadioButton(group, this.qiniuOssSouthChinaRadioButton);
        this.addZoneRadioButton(group, this.qiniuOssNorthAmeriaRadioButton);

        this.qiniuOssEastChinaRadioButton.setSelected(state.getZoneIndex() == this.qiniuOssEastChinaRadioButton.getMnemonic());
        this.qiniuOssNortChinaRadioButton.setSelected(state.getZoneIndex() == this.qiniuOssNortChinaRadioButton.getMnemonic());
        this.qiniuOssSouthChinaRadioButton.setSelected(state.getZoneIndex() == this.qiniuOssSouthChinaRadioButton.getMnemonic());
        this.qiniuOssNorthAmeriaRadioButton.setSelected(state.getZoneIndex() == this.qiniuOssNorthAmeriaRadioButton.getMnemonic());
    }

    /**
     * 处理被选中的 zone 单选框
     * <p>
     * 将指定的 JRadioButton 添加到 ButtonGroup 中，并为该按钮绑定一个监听器，用于在按钮被点击时更新 zoneIndexTextFiled 的文本为所选按钮的快捷键值。
     *
     * @param group  要添加按钮的按钮组
     * @param button 要添加的单选按钮
     * @since 0.0.1
     */
    private void addZoneRadioButton(@NotNull ButtonGroup group, JRadioButton button) {
        group.add(button);
        ActionListener actionListener = e -> {
            Object sourceObject = e.getSource();
            if (sourceObject instanceof JRadioButton sourceButton) {
                this.zoneIndexTextFiled.setText(String.valueOf(sourceButton.getMnemonic()));
            }
        };
        button.addActionListener(actionListener);
    }

    /**
     * 检查当前配置是否与给定的七牛OSS状态对象一致
     * <p>
     * 该方法通过获取当前界面输入的存储桶名称、访问密钥、秘密密钥、区域索引和端点信息，与传入的QiniuOssState对象进行比较，判断配置是否已修改。
     *
     * @param state 要比较的七牛OSS状态对象
     * @return 如果配置一致返回true，否则返回false
     * @since 1.4.0
     */
    @Override
    public boolean isModified(@NotNull QiniuOssState state) {
        String bucketName = this.qiniuOssBucketNameTextField.getText().trim();
        String accessKey = this.qiniuOssAccessKeyTextField.getText().trim();
        String secretKey = new String(this.qiniuOssAccessSecretKeyTextField.getPassword());

        // todo-dong4j : (2019年03月19日 21:01) [重构为 domain]
        String endpoint = JTextFieldHintListener.getRealText(this.qiniuOssUpHostTextField, DOMAIN_HINT);
        // todo-dong4j : (2019年03月19日 21:13) [zone]
        int zoneIndex = Integer.parseInt(this.zoneIndexTextFiled.getText());

        return bucketName.equals(state.getBucketName())
               && accessKey.equals(state.getAccessKey())
               && secretKey.equals(PasswordManager.getPassword(CREDENTIAL_ATTRIBUTES))
               && zoneIndex == state.getZoneIndex()
               && endpoint.equals(state.getEndpoint());
    }

    /**
     * 应用配置信息到指定的 QiniuOssState 对象中
     * <p>
     * 该方法从界面组件中获取七牛云存储的相关配置信息，包括存储桶名称、访问密钥、
     * 秘密密钥、区域索引和端点地址，并将这些信息设置到传入的 QiniuOssState 对象中。
     * 同时，还会计算配置信息的哈希值并保存状态。
     *
     * @param state 要应用配置信息的 QiniuOssState 对象
     */
    @Override
    public void apply(@NotNull QiniuOssState state) {
        // todo-dong4j : (2019年03月19日 21:01) [重构为 domain]
        String endpoint = JTextFieldHintListener.getRealText(this.qiniuOssUpHostTextField, DOMAIN_HINT);
        // todo-dong4j : (2019年03月19日 21:13) [zone]
        String bucketName = this.qiniuOssBucketNameTextField.getText().trim();
        String accessKey = this.qiniuOssAccessKeyTextField.getText().trim();
        String secretKey = new String(this.qiniuOssAccessSecretKeyTextField.getPassword());
        int zoneIndex = Integer.parseInt(this.zoneIndexTextFiled.getText());
        // 需要在加密之前计算 hashcode
        int hashcode = bucketName.hashCode() +
                       accessKey.hashCode() +
                       secretKey.hashCode() +
                       zoneIndex +
                       endpoint.hashCode();
        OssState.saveStatus(state, hashcode, MikState.NEW_HASH_KEY);

        state.setBucketName(bucketName);
        state.setAccessKey(accessKey);
        PasswordManager.setPassword(CREDENTIAL_ATTRIBUTES, secretKey);
        state.setEndpoint(endpoint);
        state.setZoneIndex(zoneIndex);
    }

    /**
     * 重置七牛云 OSS 配置信息
     * <p>
     * 根据传入的 {@link QiniuOssState} 对象，将界面中的相关字段设置为对应的状态值。
     *
     * @param state 包含七牛云 OSS 配置信息的 {@link QiniuOssState} 对象
     * @since 1.4.0
     */
    @Override
    public void reset(QiniuOssState state) {
        this.qiniuOssBucketNameTextField.setText(state.getBucketName());
        this.qiniuOssAccessKeyTextField.setText(state.getAccessKey());
        this.qiniuOssAccessSecretKeyTextField.setText(PasswordManager.getPassword(CREDENTIAL_ATTRIBUTES));
        this.qiniuOssUpHostTextField.setText(state.getEndpoint());
        JTextFieldHintListener.init(this.qiniuOssUpHostTextField, DOMAIN_HINT);
        this.zoneIndexTextFiled.setText(String.valueOf(state.getZoneIndex()));
    }
}
