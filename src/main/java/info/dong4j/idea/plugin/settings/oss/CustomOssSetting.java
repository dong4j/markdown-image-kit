/*
 * MIT License
 *
 * Copyright (c) 2021 dong4j <dong4j@gmail.com>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package info.dong4j.idea.plugin.settings.oss;

import info.dong4j.idea.plugin.settings.MikState;
import info.dong4j.idea.plugin.settings.OssState;
import info.dong4j.idea.plugin.swing.JTextFieldHintListener;

import org.jetbrains.annotations.NotNull;

import javax.swing.JTextField;

/**
 * <p>Company: 成都返空汇网络技术有限公司 </p>
 * <p>Description: </p>
 *
 * @author dong4j
 * @version 1.0.0
 * @email "mailto:dong4j@fkhwl.com"
 * @date 2021.02.17 21:47
 * @since 1.5.0
 */
public class CustomOssSetting implements OssSetting<CustomOssState> {
    /** REQUES_TKEY_HINT */
    private final static String REQUES_TKEY_HINT = "@RequestParam(\"{key}\") MultipartFile file";
    /** HTTP_METHOD_HINT */
    private final static String HTTP_METHOD_HINT = "POST or PUT, 具体请求方式请根据上传接口确定";
    /** RESPONSE_URL_PATH_HINT */
    private final static String RESPONSE_URL_PATH_HINT = "{\"data\": {\"url\": \"可访问的图片地址\"}} --> data.url";
    /** Custom api text field */
    private final JTextField customApiTextField;
    /** Request key text field */
    private final JTextField requestKeyTextField;
    /** Response url path text field */
    private final JTextField responseUrlPathTextField;
    /** Http method text field */
    private final JTextField httpMethodTextField;

    /**
     * Custom oss setting
     *
     * @param customApiTextField       custom api text field
     * @param requestKeyTextField      request key text field
     * @param responseUrlPathTextField response url path text field
     * @param httpMethodTextField      http method text field
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
     * Init
     *
     * @param state state
     * @since 1.5.0
     */
    @Override
    public void init(CustomOssState state) {
        this.requestKeyTextField.addFocusListener(new JTextFieldHintListener(this.requestKeyTextField, REQUES_TKEY_HINT));
        this.responseUrlPathTextField.addFocusListener(new JTextFieldHintListener(this.responseUrlPathTextField, RESPONSE_URL_PATH_HINT));
        this.httpMethodTextField.addFocusListener(new JTextFieldHintListener(this.httpMethodTextField, HTTP_METHOD_HINT));
    }

    /**
     * Is modified
     *
     * @param state state
     * @return the boolean
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
     * Apply
     *
     * @param state state
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
     * Reset
     *
     * @param state state
     * @since 1.5.0
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
