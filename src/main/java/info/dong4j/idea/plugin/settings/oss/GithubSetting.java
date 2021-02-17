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

import com.intellij.ui.DocumentAdapter;
import com.intellij.ui.JBColor;

import info.dong4j.idea.plugin.client.AbstractOssClient;
import info.dong4j.idea.plugin.settings.MikState;
import info.dong4j.idea.plugin.settings.OssState;
import info.dong4j.idea.plugin.settings.ProjectSettingsPage;
import info.dong4j.idea.plugin.swing.JTextFieldHintListener;
import info.dong4j.idea.plugin.util.DES;
import info.dong4j.idea.plugin.util.StringUtils;

import org.jetbrains.annotations.NotNull;

import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URI;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;

/**
 * <p>Company: 成都返空汇网络技术有限公司</p>
 * <p>Description:  </p>
 *
 * @author dong4j
 * @version 1.0.0
 * @email "mailto:dong4j@fkhwl.com"
 * @date 2021.02.16 00:32
 * @since 1.3.0
 */
public class GithubSetting implements OssSetting<GithubOssState> {
    /** BAIDU_HELPER_DOC formatter:off */
    private static final String HELPER_DOC = "https://docs.github.com/en/github/working-with-github-pages/configuring-a-custom-domain-for-your-github-pages-site";
    /** formatter:on GITHUB_API */
    private static final String GITHUB_API = "https://api.github.com";
    /** REPOS_HINT */
    private static final String REPOS_HINT = "格式: owner/repos";
    /** BRANCH_HINT */
    private static final String BRANCH_HINT = "使用 main 代替 master";

    /** Repos text field */
    private final JTextField reposTextField;
    /** Branch text field */
    private final JTextField branchTextField;
    /** Token text field */
    private final JPasswordField tokenTextField;
    /** File dir text field */
    private final JTextField fileDirTextField;
    /** Custom endpoint check box */
    private final JCheckBox customEndpointCheckBox;
    /** Custom endpoint text field */
    private final JTextField customEndpointTextField;
    /** Custom endpoint helper */
    private final JLabel customEndpointHelper;
    /** Example text field */
    private final JTextField exampleTextField;

    /**
     * Baidu bos setting
     *
     * @param reposTextField          repos text field
     * @param branchTextField         branch text field
     * @param tokenTextField          token text field
     * @param fileDirTextField        file dir text field
     * @param customEndpointCheckBox  custom endpoint check box
     * @param customEndpointTextField custom endpoint text field
     * @param customEndpointHelper    custom endpoint helper
     * @param exampleTextField        example text field
     * @since 1.3.0
     */
    public GithubSetting(JTextField reposTextField,
                         JTextField branchTextField,
                         JPasswordField tokenTextField,
                         JTextField fileDirTextField,
                         JCheckBox customEndpointCheckBox,
                         JTextField customEndpointTextField,
                         JLabel customEndpointHelper,
                         JTextField exampleTextField) {

        this.reposTextField = reposTextField;
        this.branchTextField = branchTextField;
        this.tokenTextField = tokenTextField;
        this.fileDirTextField = fileDirTextField;
        this.customEndpointCheckBox = customEndpointCheckBox;
        this.customEndpointTextField = customEndpointTextField;
        this.customEndpointHelper = customEndpointHelper;
        this.exampleTextField = exampleTextField;

    }

    /**
     * Gets help doc *
     *
     * @return the help doc
     * @since 1.3.0
     */
    protected String getHelpDoc() {
        return HELPER_DOC;
    }

    /**
     * Gets key *
     *
     * @return the key
     * @since 1.3.0
     */
    protected String getKey() {
        return MikState.GITHUB;
    }

    /**
     * 初始化 oss 认证相关设置
     *
     * @param state state
     * @since 1.3.0
     */
    @Override
    public void init(GithubOssState state) {
        String token = state.getToken();
        this.tokenTextField.setText(DES.decrypt(token, this.getKey()));

        this.setExampleText(false);

        DocumentAdapter documentAdapter = new DocumentAdapter() {
            @Override
            protected void textChanged(@NotNull DocumentEvent e) {
                GithubSetting.this.setExampleText(false);
            }
        };
        DocumentAdapter customDocumentAdapter = new DocumentAdapter() {
            @Override
            protected void textChanged(@NotNull DocumentEvent e) {
                GithubSetting.this.setExampleText(true);
            }
        };

        this.reposTextField.addFocusListener(new JTextFieldHintListener(this.reposTextField, REPOS_HINT));
        this.branchTextField.addFocusListener(new JTextFieldHintListener(this.branchTextField, BRANCH_HINT));

        this.reposTextField.getDocument().addDocumentListener(documentAdapter);
        this.fileDirTextField.getDocument().addDocumentListener(documentAdapter);

        this.change(customDocumentAdapter, state.getIsCustomEndpoint());

        // 设置 customEndpointCheckBox 监听
        this.customEndpointCheckBox.addChangeListener(e -> {
            JCheckBox checkBox = (JCheckBox) e.getSource();
            this.change(customDocumentAdapter, checkBox.isSelected());
        });
    }

    /**
     * Change
     *
     * @param customDocumentAdapter custom document adapter
     * @param isSelected            is selected
     * @since 1.3.0
     */
    private void change(DocumentAdapter customDocumentAdapter, boolean isSelected) {
        this.customEndpointTextField.setEnabled(isSelected);
        this.customEndpointHelper.setEnabled(isSelected);
        this.fileDirTextField.setEnabled(!isSelected);
        this.showCustomEndpointHelper();

        if (isSelected) {
            this.customEndpointTextField.getDocument().addDocumentListener(customDocumentAdapter);
        }

        // 重置 example
        this.setExampleText(isSelected);
    }

    /**
     * 实时更新此字段
     *
     * @param isCustom is custom
     * @since 1.3.0
     */
    private void setExampleText(boolean isCustom) {
        String fileDir;
        String url;
        if (isCustom) {
            fileDir = StringUtils.isBlank(this.fileDirTextField.getText().trim()) ? "" :
                      "/" + this.fileDirTextField.getText().trim();
            url = AbstractOssClient.URL_PROTOCOL_HTTPS + "://" + this.customEndpointTextField.getText();

        } else {
            fileDir = StringUtils.isBlank(this.fileDirTextField.getText().trim()) ? "" :
                      "/" + this.fileDirTextField.getText().trim();
            String repos = JTextFieldHintListener.getRealText(this.reposTextField, REPOS_HINT);
            url = GITHUB_API + "/repos/" + repos + "/contents";
            this.exampleTextField.setText(url + fileDir + "/" + ProjectSettingsPage.TEST_FILE_NAME);
        }
        this.exampleTextField.setText(url + fileDir + "/" + ProjectSettingsPage.TEST_FILE_NAME);
    }

    /**
     * Show custom endpoint helper
     *
     * @since 1.3.0
     */
    private void showCustomEndpointHelper() {
        // 设置帮助文档链接
        this.customEndpointHelper.setText("<html><a href='" + this.getHelpDoc() + "'>自定义 Endpoint 帮助文档</a></html>");
        // 设置链接颜色
        this.customEndpointHelper.setForeground(JBColor.WHITE);
        // 设置鼠标样式
        this.customEndpointHelper.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        // 设置提示文字
        this.customEndpointHelper.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                try {
                    Desktop.getDesktop().browse(new URI(GithubSetting.this.getHelpDoc()));
                } catch (Exception ignored) {
                }
            }
        });
    }

    /**
     * Is modified
     *
     * @return the boolean
     * @since 1.3.0
     */
    @Override
    public boolean isModified(@NotNull GithubOssState state) {
        String repos = JTextFieldHintListener.getRealText(this.reposTextField, REPOS_HINT);
        String branch = JTextFieldHintListener.getRealText(this.branchTextField, BRANCH_HINT);
        String token = new String(this.tokenTextField.getPassword());
        if (StringUtils.isNotBlank(token)) {
            token = DES.encrypt(token, this.getKey());
        }
        String filedir = this.fileDirTextField.getText().trim();
        String customEndpoint = this.customEndpointTextField.getText().trim();
        boolean isCustomEndpoint = this.customEndpointCheckBox.isSelected();

        return repos.equals(state.getRepos())
               && branch.equals(state.getBranch())
               && token.equals(state.getToken())
               && filedir.equals(state.getFiledir())
               && state.getIsCustomEndpoint() == isCustomEndpoint
               && customEndpoint.equals(state.getCustomEndpoint());
    }

    /**
     * Apply
     *
     * @since 1.3.0
     */
    @Override
    public void apply(@NotNull GithubOssState state) {
        String repos = JTextFieldHintListener.getRealText(this.reposTextField, REPOS_HINT);
        String branch = JTextFieldHintListener.getRealText(this.branchTextField, BRANCH_HINT);
        String token = new String(this.tokenTextField.getPassword());
        String customEndpoint = this.customEndpointTextField.getText().trim();
        boolean isCustomEndpoint = this.customEndpointCheckBox.isSelected();

        // 需要在加密之前计算 hashcode
        int hashcode = repos.hashCode() +
                       token.hashCode() +
                       branch.hashCode() +
                       (customEndpoint + isCustomEndpoint).hashCode();

        OssState.saveStatus(state, hashcode, MikState.NEW_HASH_KEY);

        if (StringUtils.isNotBlank(token)) {
            token = DES.encrypt(token, this.getKey());
        }

        state.setRepos(repos);
        state.setBranch(branch);
        state.setToken(token);
        state.setCustomEndpoint(customEndpoint);
        state.setIsCustomEndpoint(isCustomEndpoint);
        state.setFiledir(this.fileDirTextField.getText().trim());
    }

    /**
     * Reset
     *
     * @param state state
     * @since 1.3.0
     */
    @Override
    public void reset(GithubOssState state) {
        this.reposTextField.setText(state.getRepos());
        JTextFieldHintListener.init(this.reposTextField, REPOS_HINT);
        this.branchTextField.setText(state.getBranch());
        JTextFieldHintListener.init(this.branchTextField, BRANCH_HINT);

        String token = state.getToken();
        this.tokenTextField.setText(DES.decrypt(token, this.getKey()));
        this.fileDirTextField.setText(state.getFiledir());

        this.customEndpointCheckBox.setSelected(state.getIsCustomEndpoint());
        this.customEndpointTextField.setText(state.getCustomEndpoint());
    }
}
