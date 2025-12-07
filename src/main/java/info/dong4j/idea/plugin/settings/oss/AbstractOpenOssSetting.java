package info.dong4j.idea.plugin.settings.oss;

import com.intellij.credentialStore.CredentialAttributes;
import com.intellij.ui.DocumentAdapter;

import info.dong4j.idea.plugin.settings.MikState;
import info.dong4j.idea.plugin.settings.OssState;
import info.dong4j.idea.plugin.swing.JTextFieldHintListener;
import info.dong4j.idea.plugin.util.PasswordManager;

import org.jetbrains.annotations.NotNull;

import javax.swing.JCheckBox;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;

/**
 * 抽象的 OSS 设置类
 * <p>
 * 该类用于抽象 OSS（对象存储服务）相关设置的通用逻辑，提供初始化、修改检测、应用和重置等基础功能。支持自定义 Endpoint、文件目录、分支等配置项，并提供帮助文档链接和示例路径展示。
 * <p>
 * 该类作为抽象类，定义了 OSS 设置的通用行为，具体实现由子类完成。主要包含初始化设置、监听字段变化、更新示例路径、判断是否修改、应用配置和重置配置等功能。
 *
 * @author dong4j
 * @version 1.0.0
 * @date 2021.02.17
 * @since 1.3.0
 */
public abstract class AbstractOpenOssSetting<T extends AbstractOpenOssState> implements OssSetting<T> {
    /** REPOS_HINT 用于指定仓库格式，格式为 owner/repos */
    public static final String REPOS_HINT = "格式: owner/repos";
    /** 仓库名称输入框 */
    private final JTextField reposTextField;
    /** 分支文本输入框 */
    private final JTextField branchTextField;
    /** Token 输入字段 */
    private final JPasswordField tokenTextField;
    /** 文件目录文本字段 */
    private final JTextField fileDirTextField;
    /** 自定义端点检查框，用于启用或禁用自定义端点配置 */
    private final JCheckBox customEndpointCheckBox;
    /** 自定义端点文本字段 */
    private final JTextField customEndpointTextField;
    /** 示例文本字段，用于展示文本输入功能 */
    private final JTextField exampleTextField;

    /**
     * 初始化百度BOS设置组件
     * <p>
     * 通过传入的各个文本框和复选框等组件，初始化百度BOS相关的设置界面元素
     *
     * @param reposTextField          仓库文本框
     * @param branchTextField         分支文本框
     * @param tokenTextField          密码文本框
     * @param fileDirTextField        文件目录文本框
     * @param customEndpointCheckBox  自定义端点复选框
     * @param customEndpointTextField 自定义端点文本框
     * @param exampleTextField        示例文本框
     * @since 1.3.0
     */
    public AbstractOpenOssSetting(JTextField reposTextField,
                                  JTextField branchTextField,
                                  JPasswordField tokenTextField,
                                  JTextField fileDirTextField,
                                  JCheckBox customEndpointCheckBox,
                                  JTextField customEndpointTextField,
                                  JTextField exampleTextField) {

        this.reposTextField = reposTextField;
        this.branchTextField = branchTextField;
        this.tokenTextField = tokenTextField;
        this.fileDirTextField = fileDirTextField;
        this.customEndpointCheckBox = customEndpointCheckBox;
        this.customEndpointTextField = customEndpointTextField;
        this.exampleTextField = exampleTextField;

    }

    /**
     * 获取帮助文档内容
     * <p>
     * 用于获取系统或模块的帮助文档字符串，通常用于显示帮助信息或文档说明
     *
     * @return 帮助文档字符串
     * @since 1.3.0
     */
    protected abstract String getHelpDoc();

    /**
     * Api
     * <p>
     * 提供一个抽象方法，用于定义具体的 API 接口逻辑，子类需实现该方法。
     *
     * @return 返回字符串类型的 API 响应内容
     * @since 1.4.0
     */
    protected abstract String api();

    /**
     * 获取凭证属性
     * <p>
     * 返回当前凭证的属性信息，具体实现由子类提供
     *
     * @return 凭证属性
     * @since 1.6.0
     */
    protected abstract CredentialAttributes credentialAttributes();

    /**
     * 初始化 OSS 认证相关设置
     * <p>
     * 该方法用于初始化 OSS 认证所需的各项配置，包括设置密码、文本字段监听器、
     * 自定义端点切换逻辑以及帮助文档的鼠标监听器。
     *
     * @param state 用于初始化的配置状态对象
     * @since 1.3.0
     */
    @Override
    public void init(T state) {
        reset(state);

        this.setExampleText(false);

        DocumentAdapter documentAdapter = new DocumentAdapter() {
            /**
             * 文本内容发生变化时触发的方法
             * <p>
             * 当文本内容发生改变时，调用此方法以更新示例文本状态
             *
             * @param e 文档事件对象，包含变更详情
             */
            @Override
            protected void textChanged(@NotNull DocumentEvent e) {
                AbstractOpenOssSetting.this.setExampleText(false);
            }
        };
        DocumentAdapter customDocumentAdapter = new DocumentAdapter() {
            /**
             * 文本内容发生变化时触发的方法
             * <p>
             * 当文本内容发生改变时，调用此方法以更新示例文本状态为已设置
             *
             * @param e 文档事件对象，包含文本变化的相关信息
             */
            @Override
            protected void textChanged(@NotNull DocumentEvent e) {
                AbstractOpenOssSetting.this.setExampleText(true);
            }
        };

        this.reposTextField.addFocusListener(new JTextFieldHintListener(this.reposTextField, REPOS_HINT));

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
     * 根据选中状态更新相关控件的启用状态，并绑定文档监听器
     * <p>
     * 该方法用于切换控件的启用状态，根据传入的 isSelected 参数决定是否启用自定义端点相关控件，同时禁用文件目录控件。如果 isSelected 为 true，则绑定文档监听器以监听文本变化。
     *
     * @param customDocumentAdapter 自定义文档监听器，用于监听文本变化
     * @param isSelected            是否选中状态，true 表示启用相关控件，false 表示禁用
     */
    private void change(DocumentAdapter customDocumentAdapter, boolean isSelected) {
        this.customEndpointTextField.setEnabled(isSelected);
        this.fileDirTextField.setEnabled(!isSelected);

        if (isSelected) {
            this.customEndpointTextField.getDocument().addDocumentListener(customDocumentAdapter);
        }

        // 重置 example
        // this.setExampleText(isSelected);
    }

    /**
     * 根据是否为自定义模式更新示例文本字段的内容
     * <p>
     * 根据传入的 isCustom 参数判断使用自定义端点还是默认 API 路径，构建 URL 并更新示例文本字段。
     *
     * @param isCustom 是否为自定义模式
     */
    private void setExampleText(boolean isCustom) {
        // String fileDir;
        // String url;
        // if (isCustom) {
        //     fileDir = StringUtils.isBlank(this.fileDirTextField.getText().trim()) ? "" :
        //               "/" + this.fileDirTextField.getText().trim();
        //     url = AbstractOssClient.URL_PROTOCOL_HTTPS + "://" + this.customEndpointTextField.getText();
        //
        // } else {
        //     fileDir = StringUtils.isBlank(this.fileDirTextField.getText().trim()) ? "" :
        //               "/" + this.fileDirTextField.getText().trim();
        //     String repos = JTextFieldHintListener.getRealText(this.reposTextField, REPOS_HINT);
        //     url = this.api() + "/repos/" + repos + "/contents";
        //     this.exampleTextField.setText(url + fileDir + "/" + ProjectSettingsPage.TEST_FILE_NAME);
        // }
        // this.exampleTextField.setText(url + fileDir + "/" + ProjectSettingsPage.TEST_FILE_NAME);
    }

    /**
     * 判断当前状态是否与给定状态相同
     * <p>
     * 比较当前对象的各个属性值与传入状态对象的对应属性值，判断是否完全一致。
     *
     * @param state 要比较的状态对象
     * @return 如果所有属性值都相同，返回 true；否则返回 false
     * @since 1.3.0
     */
    @Override
    public boolean isModified(@NotNull T state) {
        String repos = JTextFieldHintListener.getRealText(this.reposTextField, REPOS_HINT);
        String branch = this.branchTextField.getText().trim();

        String filedir = this.fileDirTextField.getText().trim();
        String customEndpoint = this.customEndpointTextField.getText().trim();
        boolean isCustomEndpoint = this.customEndpointCheckBox.isSelected();

        // 只比较非敏感字段，避免在 EDT 上调用 PasswordManager.getPassword()（慢操作）
        // 密码字段的修改会在 apply() 时保存
        return !(repos.equals(state.getRepos())
                 && branch.equals(state.getBranch())
                 && filedir.equals(state.getFiledir())
                 && state.getIsCustomEndpoint() == isCustomEndpoint
                 && customEndpoint.equals(state.getCustomEndpoint()));
    }

    /**
     * 应用配置信息到指定状态对象中
     * <p>
     * 该方法用于将用户输入的仓库地址、分支、访问令牌、自定义端点以及是否启用自定义端点等配置信息应用到给定的状态对象中。在设置之前，会先计算配置信息的哈希值并保存。
     *
     * @param state 要应用配置信息的状态对象
     * @since 1.3.0
     */
    @Override
    public void apply(@NotNull T state) {
        String repos = JTextFieldHintListener.getRealText(this.reposTextField, REPOS_HINT);
        String branch = this.branchTextField.getText().trim();
        String token = new String(this.tokenTextField.getPassword());
        String customEndpoint = this.customEndpointTextField.getText().trim();
        boolean isCustomEndpoint = this.customEndpointCheckBox.isSelected();

        // 需要在加密之前计算 hashcode
        int hashcode = repos.hashCode() +
                       token.hashCode() +
                       branch.hashCode() +
                       (customEndpoint + isCustomEndpoint).hashCode();

        OssState.saveStatus(state, hashcode, MikState.NEW_HASH_KEY);

        state.setRepos(repos);
        state.setBranch(branch);
        PasswordManager.setPassword(this.credentialAttributes(), token);
        state.setCustomEndpoint(customEndpoint);
        state.setIsCustomEndpoint(isCustomEndpoint);
        state.setFiledir(this.fileDirTextField.getText().trim());
    }

    /**
     * 重置表单字段为指定状态
     * <p>
     * 根据传入的状态对象，将各个文本字段和复选框恢复为对应的状态值
     *
     * @param state 要恢复的状态对象
     * @since 1.3.0
     */
    @Override
    public void reset(T state) {
        this.reposTextField.setText(state.getRepos());
        JTextFieldHintListener.init(this.reposTextField, REPOS_HINT);
        this.branchTextField.setText(state.getBranch());

        this.tokenTextField.setText(PasswordManager.getPassword(this.credentialAttributes()));
        this.fileDirTextField.setText(state.getFiledir());

        this.customEndpointCheckBox.setSelected(state.getIsCustomEndpoint());
        this.customEndpointTextField.setText(state.getCustomEndpoint());
    }
}
