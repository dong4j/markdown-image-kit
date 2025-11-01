package info.dong4j.idea.plugin.settings.oss;

import info.dong4j.idea.plugin.settings.MikState;
import info.dong4j.idea.plugin.settings.OssState;
import info.dong4j.idea.plugin.swing.JTextFieldHintListener;

import org.jetbrains.annotations.NotNull;

import javax.swing.JTextField;

/**
 * 自定义OSS设置类
 * <p>
 * 用于封装和管理自定义OSS配置信息，包括API地址、请求键、HTTP方法和响应URL路径等参数的设置与维护。
 * 支持初始化、判断是否修改、应用配置和重置配置等操作，适用于需要动态配置OSS上传参数的场景。
 * <p>
 * 该类通过监听文本字段的焦点事件，实现提示信息的显示与隐藏，提升用户交互体验。
 *
 * @author dong4j
 * @version 1.0.0
 * @date 2025.10.24
 * @since 1.5.0
 */
public class CustomOssSetting implements OssSetting<CustomOssState> {
    /** 请求参数中 key 的提示信息，用于标识上传文件的参数名 */
    public final static String REQUES_TKEY_HINT = "@RequestParam(\"{key}\") MultipartFile file";
    /** HTTP_METHOD_HINT 用于指示 HTTP 请求方法，具体请求方式请根据上传接口确定 */
    public final static String HTTP_METHOD_HINT = "POST or PUT, 具体请求方式请根据上传接口确定";
    /** 响应 URL 路径提示信息，包含可访问的图片地址 */
    public final static String RESPONSE_URL_PATH_HINT = "{\"data\": {\"url\": \"可访问的图片地址\"}} --> data.url";
    /** 自定义 API 文本输入框 */
    private final JTextField customApiTextField;
    /** 请求密钥文本输入框 */
    private final JTextField requestKeyTextField;
    /** 响应 URL 路径文本字段 */
    private final JTextField responseUrlPathTextField;
    /** HTTP 方法文本输入框，用于显示或选择 HTTP 请求方法 */
    private final JTextField httpMethodTextField;

    /**
     * 初始化自定义OSS设置，绑定各个文本字段
     * <p>
     * 通过传入的文本字段初始化自定义OSS设置的相关组件
     *
     * @param customApiTextField       自定义API文本字段
     * @param requestKeyTextField      请求密钥文本字段
     * @param responseUrlPathTextField 响应URL路径文本字段
     * @param httpMethodTextField      HTTP方法文本字段
     * @since 1.5.0
     */
    public CustomOssSetting(JTextField customApiTextField,
                            JTextField requestKeyTextField,
                            JTextField responseUrlPathTextField,
                            JTextField httpMethodTextField) {

        this.customApiTextField = customApiTextField;
        this.requestKeyTextField = requestKeyTextField;
        this.responseUrlPathTextField = responseUrlPathTextField;
        this.httpMethodTextField = httpMethodTextField;
    }

    /**
     * 初始化组件，添加焦点监听器以显示提示信息
     * <p>
     * 该方法用于在组件初始化时，为文本字段添加焦点监听器，用于显示相应的提示文本
     *
     * @param state 自定义OSS状态对象，用于配置提示信息
     * @since 1.5.0
     */
    @Override
    public void init(CustomOssState state) {
        reset(state);
        this.requestKeyTextField.addFocusListener(new JTextFieldHintListener(this.requestKeyTextField, REQUES_TKEY_HINT));
        this.responseUrlPathTextField.addFocusListener(new JTextFieldHintListener(this.responseUrlPathTextField, RESPONSE_URL_PATH_HINT));
        this.httpMethodTextField.addFocusListener(new JTextFieldHintListener(this.httpMethodTextField, HTTP_METHOD_HINT));
    }

    /**
     * 判断当前状态是否已修改
     * <p>
     * 通过比较当前界面输入的请求密钥、响应URL路径、API和HTTP方法与传入状态对象的对应值，判断是否发生修改。
     *
     * @param state 要比较的状态对象
     * @return 如果当前状态与传入状态一致，返回true；否则返回false
     * @since 1.5.0
     */
    @Override
    public boolean isModified(@NotNull CustomOssState state) {
        String requestKey = JTextFieldHintListener.getRealText(this.requestKeyTextField, REQUES_TKEY_HINT);
        String responseUrlPath = JTextFieldHintListener.getRealText(this.responseUrlPathTextField, RESPONSE_URL_PATH_HINT);
        String httpMethod = JTextFieldHintListener.getRealText(this.httpMethodTextField, HTTP_METHOD_HINT);

        String api = this.customApiTextField.getText().trim();

        return requestKey.equals(state.getRequestKey())
               && responseUrlPath.equals(state.getResponseUrlPath())
               && api.equals(state.getApi())
               && httpMethod.equals(state.getHttpMethod());
    }

    /**
     * 将当前界面输入的参数应用到 CustomOssState 对象中
     * <p>
     * 该方法用于获取各个文本字段的真实值，并计算 API 字符串的哈希码，最后将这些信息保存到状态对象中
     *
     * @param state 要应用参数的 CustomOssState 对象
     * @since 1.5.0
     */
    @Override
    public void apply(@NotNull CustomOssState state) {
        String requestKey = JTextFieldHintListener.getRealText(this.requestKeyTextField, REQUES_TKEY_HINT);
        String httpMethod = JTextFieldHintListener.getRealText(this.httpMethodTextField, HTTP_METHOD_HINT);
        String responseUrlPath = JTextFieldHintListener.getRealText(this.responseUrlPathTextField,
                                                                    RESPONSE_URL_PATH_HINT);


        String api = this.customApiTextField.getText().trim();

        // 需要在加密之前计算 hashcode
        int hashcode = api.hashCode() +
                       requestKey.hashCode() +
                       responseUrlPath.hashCode() +
                       httpMethod.hashCode();

        OssState.saveStatus(state, hashcode, MikState.NEW_HASH_KEY);

        state.setApi(api);
        state.setHttpMethod(httpMethod);
        state.setRequestKey(requestKey);
        state.setResponseUrlPath(responseUrlPath);
    }

    /**
     * 重置表单字段为指定状态下的值
     * <p>
     * 根据传入的 CustomOssState 对象，将请求密钥、响应URL路径、HTTP方法和自定义API字段设置为对应状态的值
     *
     * @param state 包含需要设置的字段值的 CustomOssState 对象
     */
    @Override
    public void reset(CustomOssState state) {
        this.requestKeyTextField.setText(state.getRequestKey());
        JTextFieldHintListener.init(this.requestKeyTextField, REQUES_TKEY_HINT);
        this.responseUrlPathTextField.setText(state.getResponseUrlPath());
        JTextFieldHintListener.init(this.responseUrlPathTextField, RESPONSE_URL_PATH_HINT);
        this.httpMethodTextField.setText(state.getHttpMethod());
        JTextFieldHintListener.init(this.httpMethodTextField, HTTP_METHOD_HINT);

        this.customApiTextField.setText(state.getApi());
    }
}
