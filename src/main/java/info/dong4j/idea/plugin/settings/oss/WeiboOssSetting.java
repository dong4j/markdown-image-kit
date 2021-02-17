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
import info.dong4j.idea.plugin.util.DES;
import info.dong4j.idea.plugin.util.StringUtils;

import org.jetbrains.annotations.NotNull;

import javax.swing.JPasswordField;
import javax.swing.JTextField;

/**
 * <p>Company: 成都返空汇网络技术有限公司</p>
 * <p>Description:  </p>
 *
 * @author dong4j
 * @version 1.0.0
 * @email "mailto:dong4j@fkhwl.com"
 * @date 2021.02.17 13:31
 * @since 1.4.0
 */
public class WeiboOssSetting implements OssSetting<WeiboOssState> {
    /** Weibo user name text field */
    private final JTextField weiboUserNameTextField;
    /** Weibo password field */
    private final JPasswordField weiboPasswordField;

    /**
     * Weibo oss setting
     *
     * @param weiboUserNameTextField weibo user name text field
     * @param weiboPasswordField     weibo password field
     * @since 1.4.0
     */
    public WeiboOssSetting(JTextField weiboUserNameTextField,
                           JPasswordField weiboPasswordField) {
        this.weiboUserNameTextField = weiboUserNameTextField;
        this.weiboPasswordField = weiboPasswordField;

    }

    /**
     * 初始化 weibo oss 认证相关设置
     *
     * @param weiboOssState weibo oss state
     * @since 0.0.1
     */
    @Override
    public void init(WeiboOssState weiboOssState) {
        this.weiboUserNameTextField.setText(weiboOssState.getUserName());
        this.weiboPasswordField.setText(DES.decrypt(weiboOssState.getPassword(), MikState.WEIBOKEY));
    }

    /**
     * Is modified
     *
     * @param state state
     * @return the boolean
     * @since 1.4.0
     */
    @Override
    public boolean isModified(@NotNull WeiboOssState state) {
        String weiboUsername = this.weiboUserNameTextField.getText().trim();
        String weiboPassword = new String(this.weiboPasswordField.getPassword());
        if (StringUtils.isNotBlank(weiboPassword)) {
            weiboPassword = DES.encrypt(weiboPassword, MikState.WEIBOKEY);
        }
        return weiboUsername.equals(state.getUserName())
               && weiboPassword.equals(state.getPassword());
    }

    /**
     * Apply weibo auth configs
     *
     * @param state state
     * @since 0.0.1
     */
    @Override
    public void apply(@NotNull WeiboOssState state) {
        // 处理 weibo 保存时的逻辑 (保存之前必须通过测试, 右键菜单才可用)
        String username = this.weiboUserNameTextField.getText().trim();
        String password = new String(this.weiboPasswordField.getPassword());
        // 需要在加密之前计算 hashcode
        int hashcode = username.hashCode() + password.hashCode();
        OssState.saveStatus(state, hashcode, MikState.NEW_HASH_KEY);

        if (StringUtils.isNotBlank(password)) {
            password = DES.encrypt(password, MikState.WEIBOKEY);
        }

        state.setUserName(username);
        state.setPassword(password);
    }

    /**
     * Reset
     *
     * @param state state
     * @since 1.4.0
     */
    @Override
    public void reset(WeiboOssState state) {
        this.weiboUserNameTextField.setText(state.getUserName());
        this.weiboPasswordField.setText(DES.decrypt(state.getPassword(), MikState.WEIBOKEY));
    }
}