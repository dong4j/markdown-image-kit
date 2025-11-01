package info.dong4j.idea.plugin.settings.oss;

import com.intellij.openapi.ui.TextFieldWithBrowseButton;

import info.dong4j.idea.plugin.settings.MikState;
import info.dong4j.idea.plugin.settings.OssState;
import info.dong4j.idea.plugin.swing.JTextFieldHintListener;

import org.jetbrains.annotations.NotNull;

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
public class PicListOssSetting implements OssSetting<PicListOssState> {
    /** API 接口提示信息 */
    public final static String API_HINT = "http://127.0.0.1:36677/upload";
    /** 图床类型提示信息 */
    public final static String PICBED_HINT = "PicList 参数:图床类型（如 aws-s3, qiniu 等）";
    /** 配置名称提示信息 */
    public final static String CONFIG_NAME_HINT = "PicList 参数: 配置文件名称";
    /** 密钥提示信息 */
    public final static String KEY_HINT = "PicList 参数: 接口密钥（用于鉴权）";
    /** 命令行路径提示信息 */
    public final static String EXE_PATH_HINT = "PicList/PicGo 可执行文件路径（留空则使用 API 上传）";

    /** API 接口文本输入框 */
    private final JTextField apiTextField;
    /** 图床类型文本输入框 */
    private final JTextField picbedTextField;
    /** 配置名称文本输入框 */
    private final JTextField configNameTextField;
    /** 密钥文本输入框 */
    private final JTextField keyTextField;
    /** 命令行路径文本输入框（带浏览按钮） */
    private final TextFieldWithBrowseButton exePathTextField;

    /**
     * 初始化 PicList 图床设置
     *
     * @param apiTextField        API 接口文本字段
     * @param picbedTextField     图床类型文本字段
     * @param configNameTextField 配置名称文本字段
     * @param keyTextField        密钥文本字段
     * @param exePathTextField    命令行路径文本字段（带浏览按钮）
     */
    public PicListOssSetting(JTextField apiTextField,
                             JTextField picbedTextField,
                             JTextField configNameTextField,
                             JTextField keyTextField,
                             TextFieldWithBrowseButton exePathTextField) {
        this.apiTextField = apiTextField;
        this.picbedTextField = picbedTextField;
        this.configNameTextField = configNameTextField;
        this.keyTextField = keyTextField;
        this.exePathTextField = exePathTextField;
    }

    /**
     * 初始化组件，添加焦点监听器以显示提示信息
     *
     * @param state PicList 图床状态对象，用于配置提示信息
     */
    @Override
    public void init(PicListOssState state) {
        reset(state);

        this.picbedTextField.addFocusListener(new JTextFieldHintListener(this.picbedTextField, PICBED_HINT));
        this.configNameTextField.addFocusListener(new JTextFieldHintListener(this.configNameTextField, CONFIG_NAME_HINT));
        this.keyTextField.addFocusListener(new JTextFieldHintListener(this.keyTextField, KEY_HINT));

        // TextFieldWithBrowseButton 的文本字段需要通过 getTextField() 获取
        if (this.exePathTextField != null) {
            this.exePathTextField.getTextField()
                .addFocusListener(new JTextFieldHintListener(this.exePathTextField.getTextField(), EXE_PATH_HINT));
        }
    }

    /**
     * 判断当前状态是否已修改
     *
     * @param state 要比较的状态对象
     * @return 如果当前状态与传入状态一致，返回 false；否则返回 true
     */
    @Override
    public boolean isModified(@NotNull PicListOssState state) {
        String api = this.apiTextField.getText().trim();
        String picbed = JTextFieldHintListener.getRealText(this.picbedTextField, PICBED_HINT);
        String configName = JTextFieldHintListener.getRealText(this.configNameTextField, CONFIG_NAME_HINT);
        String key = JTextFieldHintListener.getRealText(this.keyTextField, KEY_HINT);
        String exePath = this.exePathTextField != null
                         ? JTextFieldHintListener.getRealText(this.exePathTextField.getTextField(), EXE_PATH_HINT)
                         : "";

        return !(api.equals(state.getApi()) &&
                 picbed.equals(state.getPicbed()) &&
                 configName.equals(state.getConfigName()) &&
                 key.equals(state.getKey()) &&
                 exePath.equals(state.getExePath()));
    }

    /**
     * 将当前界面输入的参数应用到 PicListOssState 对象中
     *
     * @param state 要应用参数的 PicListOssState 对象
     */
    @Override
    public void apply(@NotNull PicListOssState state) {
        String api = this.apiTextField.getText().trim();
        String picbed = JTextFieldHintListener.getRealText(this.picbedTextField, PICBED_HINT);
        String configName = JTextFieldHintListener.getRealText(this.configNameTextField, CONFIG_NAME_HINT);
        String key = JTextFieldHintListener.getRealText(this.keyTextField, KEY_HINT);
        String exePath = this.exePathTextField != null
                         ? JTextFieldHintListener.getRealText(this.exePathTextField.getTextField(), EXE_PATH_HINT)
                         : "";

        // 计算 hashcode
        int hashcode = api.hashCode() +
                       (picbed.isEmpty() ? 0 : picbed.hashCode()) +
                       (configName.isEmpty() ? 0 : configName.hashCode()) +
                       (key.isEmpty() ? 0 : key.hashCode()) +
                       (exePath.isEmpty() ? 0 : exePath.hashCode());

        OssState.saveStatus(state, hashcode, MikState.NEW_HASH_KEY);

        state.setApi(api);
        state.setPicbed(picbed);
        state.setConfigName(configName);
        state.setKey(key);
        state.setExePath(exePath);
    }

    /**
     * 重置表单字段为指定状态下的值
     *
     * @param state 包含需要设置的字段值的 PicListOssState 对象
     */
    @Override
    public void reset(PicListOssState state) {
        this.apiTextField.setText(state.getApi());

        this.picbedTextField.setText(state.getPicbed());
        JTextFieldHintListener.init(this.picbedTextField, PICBED_HINT);

        this.configNameTextField.setText(state.getConfigName());
        JTextFieldHintListener.init(this.configNameTextField, CONFIG_NAME_HINT);

        this.keyTextField.setText(state.getKey());
        JTextFieldHintListener.init(this.keyTextField, KEY_HINT);

        if (this.exePathTextField != null) {
            this.exePathTextField.setText(state.getExePath());
            JTextFieldHintListener.init(this.exePathTextField.getTextField(), EXE_PATH_HINT);
        }
    }
}
