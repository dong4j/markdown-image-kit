package info.dong4j.idea.plugin.settings;

import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.SearchableConfigurable;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.ui.TextComponentAccessor;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;

import info.dong4j.idea.plugin.MikBundle;
import info.dong4j.idea.plugin.client.OssClient;
import info.dong4j.idea.plugin.enums.CloudEnum;
import info.dong4j.idea.plugin.enums.HelpType;
import info.dong4j.idea.plugin.enums.ImageMarkEnum;
import info.dong4j.idea.plugin.notify.MikNotification;
import info.dong4j.idea.plugin.settings.oss.AliyunOssSetting;
import info.dong4j.idea.plugin.settings.oss.BaiduBosSetting;
import info.dong4j.idea.plugin.settings.oss.CustomOssSetting;
import info.dong4j.idea.plugin.settings.oss.GiteeSetting;
import info.dong4j.idea.plugin.settings.oss.GithubSetting;
import info.dong4j.idea.plugin.settings.oss.PicListOssSetting;
import info.dong4j.idea.plugin.settings.oss.QiniuOssSetting;
import info.dong4j.idea.plugin.settings.oss.TencentOssSetting;
import info.dong4j.idea.plugin.swing.JTextFieldHintListener;
import info.dong4j.idea.plugin.util.ClientUtils;
import info.dong4j.idea.plugin.util.StringUtils;

import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

import java.awt.event.ActionListener;
import java.io.InputStream;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JRadioButton;
import javax.swing.JSlider;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;

import lombok.extern.slf4j.Slf4j;

/**
 * 项目设置页面类
 * <p>
 * 该类用于实现 Markdown 图床工具的设置界面，支持多种云存储服务（如微博、阿里云、百度云、GitHub、Gitee、七牛云、腾讯云等）的配置和管理。
 * 提供了上传设置、图片压缩、水印、文件重命名、自定义域名等高级功能，并支持测试上传和帮助文档跳转。
 * 该类实现了 SearchableConfigurable 接口，用于在插件系统中注册设置页面。
 *
 * @author dong4j
 * @version 1.0.0
 * @date 2025.10.24
 * @since 1.0.0
 */
@SuppressWarnings("D")
@Slf4j
public class ProjectSettingsPage implements SearchableConfigurable, Configurable.NoMargin {
    /** 测试文件名，用于测试场景中的文件标识 */
    public static final String TEST_FILE_NAME = "mik.png";
    /**
     * 配置信息
     * <p>
     * 用于存储和管理持久化组件的配置参数
     *
     * @see MikPersistenComponent
     */
    private final MikPersistenComponent config;
    /** 主面板，用于显示主要界面内容 todo-dong4j : (2025.10.27 01:24) [初始化大小, 添加滑动] */
    private JPanel myMainPanel;

    //region authorizationPanel
    /** 授权面板，用于显示和管理授权相关配置和操作 */
    private JPanel authorizationPanel;
    /** 授权选项卡面板，用于展示不同的授权配置选项 */
    private JTabbedPane authorizationTabbedPanel;


    //region aliyunOss
    /** aliyunOssAuthorizationPanel 组 */
    private JPanel aliyunOssAuthorizationPanel;
    /** 阿里云 OSS 存储桶名称文本输入框 */
    private JTextField aliyunOssBucketNameTextField;
    /** 阿里云 OSS 访问密钥文本输入框 */
    private JTextField aliyunOssAccessKeyTextField;
    /** 阿里云 OSS 访问密钥字段，用于输入和显示密钥内容 */
    private JPasswordField aliyunOssAccessSecretKeyTextField;
    /** 阿里云 OSS 的访问端点文本输入框 */
    private JTextField aliyunOssEndpointTextField;
    /** 阿里云 OSS 文件目录输入框，用于输入或选择 OSS 文件存储目录路径 */
    private JTextField aliyunOssFileDirTextField;
    /** 是否启用自定义域名的复选框组件 */
    private JCheckBox aliyunOssCustomEndpointCheckBox;
    /** 自定义域名输入框，用于输入阿里云 OSS 的自定义域名配置 */
    private JTextField aliyunOssCustomEndpointTextField;
    /** 自定义域名帮助文档 */
    private JLabel aliyunOssCustomEndpointHelper;
    /** 示例文本字段，用于展示阿里云 OSS 相关示例内容 */
    private JTextField aliyunOssExampleTextField;
    //endregion

    //region baiduBos
    /** 百度 Bos 授权面板 */
    private JPanel baiduBosAuthorizationPanel;
    /** 百度 Bos 存储桶名称文本框 */
    private JTextField baiduBosBucketNameTextField;
    /** 百度 Bos 访问密钥文本框，用于输入或显示访问密钥信息 */
    private JTextField baiduBosAccessKeyTextField;
    /** 百度 Bos 访问密钥字段，用于输入和显示百度 Bos 访问密钥信息 */
    private JPasswordField baiduBosAccessSecretKeyTextField;
    /** 百度 Bos 服务端点文本输入框 */
    private JTextField baiduBosEndpointTextField;
    /** 百度 Bos 文件目录文本字段 */
    private JTextField baiduBosFileDirTextField;
    /** 百度 Bos 自定义端点检查框 */
    private JCheckBox baiduBosCustomEndpointCheckBox;
    /** 百度 Bos 自定义端点文本输入框 */
    private JTextField baiduBosCustomEndpointTextField;
    /** 百度 Bos 自定义端点辅助标签 */
    private JLabel baiduBosCustomEndpointHelper;
    /** 百度 Bos 示例文本字段 */
    private JTextField baiduBosExampleTextField;
    //endregion

    //region qiniuOss
    /** qiniuOssAuthorizationPanel 组 */
    private JPanel qiniuOssAuthorizationPanel;
    /** 七牛 oss 存储桶名称文本框 */
    private JTextField qiniuOssBucketNameTextField;
    /** 七牛云 OSS 访问密钥输入框 */
    private JTextField qiniuOssAccessKeyTextField;
    /** 七牛OSS访问密钥的文本输入框 */
    private JPasswordField qiniuOssAccessSecretKeyTextField;
    /** 七牛云 OSS 上传地址输入框 */
    private JTextField qiniuOssUpHostTextField;
    /** 七牛 OSS 东中国区域选择按钮 */
    private JRadioButton qiniuOssEastChinaRadioButton;
    /** 七牛OSS北中国区域选择按钮 */
    private JRadioButton qiniuOssNortChinaRadioButton;
    /** 七牛 OSS 南中国区域选择按钮 */
    private JRadioButton qiniuOssSouthChinaRadioButton;
    /** 七牛OSS北美区域的单选按钮 */
    private JRadioButton qiniuOssNorthAmeriaRadioButton;
    /** 区域索引文本字段 */
    private JTextField zoneIndexTextFiled;
    //endregion

    //region tencent
    /** 腾讯云OSS授权面板 */
    private JPanel tencentOssAuthorizationPanel;
    /** 腾讯云存储桶名称文本框 */
    private JTextField tencentBacketNameTextField;
    /** 腾讯访问密钥文本框，用于输入或显示腾讯访问密钥信息 */
    private JTextField tencentAccessKeyTextField;
    /** 腾讯密钥字段文本框，用于输入腾讯密钥信息 */
    private JPasswordField tencentSecretKeyTextField;
    /** 腾讯区域名称文本框，用于输入或显示腾讯云服务的区域名称 */
    private JTextField tencentRegionNameTextField;
    //endregion

    //region gitHub
    /** GitHub 授权面板，用于展示与 GitHub 的授权相关配置和操作项 */
    private JPanel githubAuthorizationPanel;
    /** GitHub 仓库文本字段，用于输入或显示仓库名称等信息 */
    private JTextField githubReposTextField;
    /** GitHub 分支文本输入框 */
    private JTextField githubBranchTextField;
    /** GitHub token 输入框，用于输入用户的 GitHub 认证令牌 */
    private JPasswordField githubTokenTextField;
    /** GitHub 文件目录文本输入框 */
    private JTextField githubFileDirTextField;
    /** GitHub 自定义端点复选框，用于控制是否启用自定义的 GitHub 端点配置 */
    private JCheckBox githubCustomEndpointCheckBox;
    /** GitHub 自定义端点文本输入框 */
    private JTextField githubCustomEndpointTextField;
    /** GitHub 自定义端点帮助标签，用于显示或设置自定义端点信息 */
    private JLabel githubCustomEndpointHelper;
    /** GitHub 示例文本字段，用于展示或输入示例文本内容 */
    private JTextField githubExampleTextField;
    //endregion

    //region custom
    /** 自定义授权面板 */
    private JPanel customAuthorizationPanel;
    /** 自定义 API 地址输入框 */
    private JTextField customApiTextField;
    /** 请求密钥输入框 */
    private JTextField requestKeyTextField;
    /** 响应URL路径输入框，用于输入或编辑响应URL路径配置 */
    private JTextField responseUrlPathTextField;
    /** HTTP 请求方法输入框，用于用户选择或输入请求方法，如 GET、POST 等 */
    private JTextField httpMethodTextField;
    //endregion

    //region piclist
    /** PicList 授权面板 */
    private JPanel picListAuthorizationPanel;
    /** PicList API 接口地址输入框 */
    private JTextField picListApiTextField;
    /** PicList 图床类型输入框 */
    private JTextField picListPicbedTextField;
    /** PicList 配置名称输入框 */
    private JTextField picListConfigNameTextField;
    /** PicList 密钥输入框 */
    private JTextField picListKeyTextField;
    /** PicList 命令行路径输入框（带浏览按钮） */
    private TextFieldWithBrowseButton picListExeTextField;
    //endregion

    //region gitee
    /** GitHub 授权面板，用于展示和管理 GitHub 授权相关配置 */
    private JPanel giteeAuthorizationPanel;
    /** GitHub 仓库文本输入框 */
    private JTextField giteeReposTextField;
    /** GitHub 分支文本输入框 */
    private JTextField giteeBranchTextField;
    /** GitHub Token 输入框，用于输入用户的 GitHub Token */
    private JPasswordField giteeTokenTextField;
    /** GitHub文件目录文本字段，用于输入或显示文件存储路径 */
    private JTextField giteeFileDirTextField;
    /** GitHub 自定义端点检查框 */
    private JCheckBox giteeCustomEndpointCheckBox;
    /** GitHub 自定义端点文本输入框 */
    private JTextField giteeCustomEndpointTextField;
    /** GitHub 自定义端点帮助标签 */
    private JLabel giteeCustomEndpointHelper;
    /** giteeExampleTextField 用于展示或输入 GitHub 示例文本 */
    private JTextField giteeExampleTextField;
    //endregion
    /** 测试按钮 */
    private JButton testButton;
    /** 帮助按钮 */
    private JButton helpButton;
    //endregion

    //region globalUploadPanel
    /** 全局上传面板，用于展示和管理文件上传功能 */
    private JPanel globalUploadPanel;
    /** 默认云服务商下拉框，用于展示和选择默认的云服务提供商 */
    private JComboBox<?> defaultCloudComboBox;
    /** 转换为 HTML 标签的复选框，用于控制是否将内容转换为 HTML 标签格式 */
    private JCheckBox changeToHtmlTagCheckBox;
    /** 大图模式单选按钮 */
    private JRadioButton largePictureRadioButton;
    /** 常用单选按钮 */
    private JRadioButton commonRadioButton;
    /** 公共标签，用于显示通用信息或提示内容 */
    private JLabel commonLabel;
    /** 自定义单选按钮 */
    private JRadioButton customRadioButton;
    /** 自定义标签，用于显示或输入特定内容 */
    private JLabel customLabel;
    /** 自定义 HTML 类型文本字段 */
    private JTextField customHtmlTypeTextField;
    /** 压缩选项复选框 */
    private JCheckBox compressCheckBox;
    /** 压缩比例滑块 */
    private JSlider compressSlider;
    /** 压缩标签 */
    private JLabel compressLabel;
    /** 重命名复选框 */
    private JCheckBox renameCheckBox;
    /** 文件名后缀选择框字段 */
    private JComboBox<?> fileNameSuffixBoxField;
    /** 水印复选框 */
    private JCheckBox watermarkCheckBox;
    /** 水印文字输入框，用于显示或编辑水印内容 */
    private JTextField watermarkTextField;
    /** 自定义消息标签，用于显示用户自定义的提示信息 */
    private JLabel customMessage;
    //endregion

    //region clipboardPanel
    /** 剪贴板面板，用于显示和操作剪贴板内容 */
    private JPanel clipboardPanel;
    /** 复制到目录的复选框 */
    private JCheckBox copyToDirCheckBox;
    /** whereToCopyTextField 用于输入用户指定的复制目标位置 */
    private JTextField whereToCopyTextField;
    /** 上传并替换检查框 */
    private JCheckBox uploadAndReplaceCheckBox;
    /** 默认云配置复选框 */
    private JCheckBox defaultCloudCheckBox;
    // endregion


    // region AliyunOssSetting
    /** 阿里云OSS配置信息，用于存储和管理OSS相关设置参数 */
    private final AliyunOssSetting aliyunOssSetting = new AliyunOssSetting(this.aliyunOssBucketNameTextField,
                                                                           this.aliyunOssAccessKeyTextField,
                                                                           this.aliyunOssAccessSecretKeyTextField,
                                                                           this.aliyunOssEndpointTextField,
                                                                           this.aliyunOssFileDirTextField,
                                                                           this.aliyunOssCustomEndpointCheckBox,
                                                                           this.aliyunOssCustomEndpointTextField,
                                                                           this.aliyunOssCustomEndpointHelper,
                                                                           this.aliyunOssExampleTextField);
    //endregion

    //region BaiduBosSetting
    /** 百度对象存储（BOS）配置信息，用于初始化和管理 BOS 相关参数 */
    private final BaiduBosSetting baiduBosSetting = new BaiduBosSetting(this.baiduBosBucketNameTextField,
                                                                        this.baiduBosAccessKeyTextField,
                                                                        this.baiduBosAccessSecretKeyTextField,
                                                                        this.baiduBosEndpointTextField,
                                                                        this.baiduBosFileDirTextField,
                                                                        this.baiduBosCustomEndpointCheckBox,
                                                                        this.baiduBosCustomEndpointTextField,
                                                                        this.baiduBosCustomEndpointHelper,
                                                                        this.baiduBosExampleTextField);
    //endregion

    //region GitHubSetting
    /** GitHub 设置对象，用于管理 GitHub 相关配置和操作 */
    private final GithubSetting githubSetting = new GithubSetting(this.githubReposTextField,
                                                                  this.githubBranchTextField,
                                                                  this.githubTokenTextField,
                                                                  this.githubFileDirTextField,
                                                                  this.githubCustomEndpointCheckBox,
                                                                  this.githubCustomEndpointTextField,
                                                                  this.githubCustomEndpointHelper,
                                                                  this.githubExampleTextField);
    //endregion

    //region GiteeSetting
    /** Gitee 设置信息，用于存储和管理 Gitee 相关配置参数 */
    private final GiteeSetting giteeSetting = new GiteeSetting(this.giteeReposTextField,
                                                               this.giteeBranchTextField,
                                                               this.giteeTokenTextField,
                                                               this.giteeFileDirTextField,
                                                               this.giteeCustomEndpointCheckBox,
                                                               this.giteeCustomEndpointTextField,
                                                               this.giteeCustomEndpointHelper,
                                                               this.giteeExampleTextField);
    //endregion

    //region QiniuOssSetting
    /** 七牛云 OSS 配置信息，用于存储和管理七牛云对象存储的相关设置 */
    private final QiniuOssSetting qiniuOssSetting = new QiniuOssSetting(this.qiniuOssBucketNameTextField,
                                                                        this.qiniuOssAccessKeyTextField,
                                                                        this.qiniuOssAccessSecretKeyTextField,
                                                                        this.qiniuOssUpHostTextField,
                                                                        this.qiniuOssEastChinaRadioButton,
                                                                        this.qiniuOssNortChinaRadioButton,
                                                                        this.qiniuOssSouthChinaRadioButton,
                                                                        this.qiniuOssNorthAmeriaRadioButton,
                                                                        this.zoneIndexTextFiled);
    //endregion

    //region TencentOssSetting
    /** TencentOssSetting 对象，用于配置腾讯云对象存储服务相关参数 */
    private final TencentOssSetting tencentOssSetting = new TencentOssSetting(this.tencentBacketNameTextField,
                                                                              this.tencentAccessKeyTextField,
                                                                              this.tencentSecretKeyTextField,
                                                                              this.tencentRegionNameTextField);
    //endregion

    //region CustomOssSetting
    /** 自定义 OSS 配置信息实例，用于存储和管理 OSS 相关的配置参数 */
    private final CustomOssSetting customOssSetting = new CustomOssSetting(this.customApiTextField,
                                                                           this.requestKeyTextField,
                                                                           this.responseUrlPathTextField,
                                                                           this.httpMethodTextField);
    //endregion

    //region PicListOssSetting
    /** PicList 图床配置信息实例，用于存储和管理 PicList 相关的配置参数 */
    private final PicListOssSetting picListOssSetting = new PicListOssSetting(this.picListApiTextField,
                                                                              this.picListPicbedTextField,
                                                                              this.picListConfigNameTextField,
                                                                              this.picListKeyTextField,
                                                                              this.picListExeTextField);
    //endregion
    /** 用于输入验证测试的端口号输入框 */
    private JTextField myPort;

    /**
     * ProjectSettingsPage 构造函数
     * <p>
     * 初始化 ProjectSettingsPage 对象，加载配置信息并重置页面状态。
     *
     * @since 0.0.1
     */
    public ProjectSettingsPage() {
        log.trace("ProjectSettingsPage Constructor invoke");
        this.config = MikPersistenComponent.getInstance();
        if (this.config != null) {
            this.reset();
        }
    }

    /**
     * 创建组件
     * <p>
     * 初始化组件设置并返回主面板组件
     *
     * @return 主面板组件
     * @since 0.0.1
     */
    @Override
    public JComponent createComponent() {
        this.initFromSettings();
        return this.myMainPanel;
    }

    /**
     * 获取唯一标识符
     * <p>
     * 返回该对象的唯一标识符，实际通过获取显示名称实现
     *
     * @return 唯一标识符
     * @since 0.0.1
     */
    @NotNull
    @Override
    public String getId() {
        return this.getDisplayName();
    }

    /**
     * 获取显示名称
     * <p>
     * 返回用于显示的名称，通常用于界面展示或标识用途
     *
     * @return 显示名称，固定为 "Markdown Image Kit"
     * @since 0.0.1
     */
    @Nls
    @Override
    public String getDisplayName() {
        return "Markdown Image Kit";
    }

    /**
     * 每次打开设置面板时执行初始化操作
     * <p>
     * 该方法用于从配置中读取状态信息，并初始化授权面板、全局面板和剪贴板控制相关组件。
     *
     * @since 0.0.1
     */
    private void initFromSettings() {
        MikState state = this.config.getState();
        this.initAuthorizationTabbedPanel(state);
        this.initGlobalPanel(state);
        this.initClipboardControl();

        // todo-dong4j : (2025.10.27 00:47) [暂时不开放, 需要解决并发问题]
        picListExeTextField.setVisible(false);
    }

    /**
     * 初始化 authorizationTabbedPanel 的 group 配置
     * <p>
     * 根据当前状态初始化默认选中的图床类型，并设置 help 按钮的文本内容。同时为 authorizationTabbedPanel 添加切换监听器，用于更新选中标签和 help 按钮显示信息。最后初始化各个图床设置组件。
     *
     * @param state 当前状态对象，用于获取云类型信息
     */
    private void initAuthorizationTabbedPanel(@NotNull MikState state) {
        int defaultCloudIndex = state.getCloudType();
        // 打开设置页时默认选中默认上传图床
        this.authorizationTabbedPanel.setSelectedIndex(defaultCloudIndex);

        // 处理 help 按钮
        this.helpButton.setText("Help & " + OssState.getCloudType(defaultCloudIndex).getTitle());

        this.authorizationTabbedPanel.addChangeListener(e -> {
            // 获得指定索引的选项卡标签
            int selectedIndex = this.authorizationTabbedPanel.getSelectedIndex();
            log.trace("change {}", this.authorizationTabbedPanel.getTitleAt(selectedIndex));
            CloudEnum cloudType = OssState.getCloudType(selectedIndex);
            this.helpButton.setText("Help & " + cloudType.getTitle());
        });

        this.aliyunOssSetting.init(this.config.getState().getAliyunOssState());
        this.baiduBosSetting.init(this.config.getState().getBaiduBosState());
        this.githubSetting.init(this.config.getState().getGithubOssState());
        this.giteeSetting.init(this.config.getState().getGiteeOssState());
        this.qiniuOssSetting.init(this.config.getState().getQiniuOssState());
        this.tencentOssSetting.init(this.config.getState().getTencentOssState());
        this.customOssSetting.init(this.config.getState().getCustomOssState());
        this.picListOssSetting.init(this.config.getState().getPicListOssState());

        // 初始化 PicList 命令行文件选择器
        this.initPicListExeBrowser();

        this.testAndHelpListener();
    }

    /**
     * 初始化 PicList 可执行文件选择器
     * <p>
     * 为 PicList 命令行路径输入框添加浏览按钮功能，使用户可以方便地选择可执行文件。
     */
    private void initPicListExeBrowser() {
        if (this.picListExeTextField == null) {
            log.warn("picListExeTextField 未初始化");
            return;
        }

        // 创建文件选择描述符
        FileChooserDescriptor descriptor = new FileChooserDescriptor(
            true,  // 是否可以选择文件
            true, // 是否可以选择目录
            false, // 是否可以多选
            false,  // 文件是否必须存在
            false,
            false
        );

        // 设置标题和描述
        descriptor.setTitle("选择 PicList/PicGo 可执行文件");
        descriptor.setDescription("macOS: 选择 .app 目录\nWindows: 选择 .exe 文件\nLinux: 选择 .AppImage 文件");

        // 设置文件过滤器（允许选择可执行文件和 .app 目录）
        descriptor.withFileFilter(virtualFile -> {
            String name = virtualFile.getName().toLowerCase();
            // Windows: .exe 文件
            if (name.endsWith(".exe")) {
                return true;
            }
            // macOS: .app 目录
            if (name.endsWith(".app") && virtualFile.isDirectory()) {
                return true;
            }
            // Linux: .AppImage 文件
            return name.endsWith(".appimage");
            // 其他平台的直接可执行文件
        });

        // 添加浏览文件夹监听器
        this.picListExeTextField.addBrowseFolderListener(
            null,  // 项目，可以为 null
            descriptor,
            TextComponentAccessor.TEXT_FIELD_WHOLE_TEXT
                                                        );

        log.debug("PicList/PicGo 可执行文件选择器初始化完成");
    }

    /**
     * 为 "Test" 和 "Help" 按钮添加监听器，用于根据选中的图床进行测试或获取帮助信息
     * <p>
     * 该方法为 "Test" 按钮绑定点击事件，用于上传测试文件并显示结果；同时为 "Help" 按钮绑定点击事件，用于打开对应帮助页面。
     *
     * @since 0.0.1
     */
    public void testAndHelpListener() {
        // "Test" 按钮点击事件处理
        this.testButton.addActionListener(e -> {
            int index = this.authorizationTabbedPanel.getSelectedIndex();
            InputStream inputStream = this.getClass().getResourceAsStream("/" + TEST_FILE_NAME);
            CloudEnum cloudEnum = OssState.getCloudType(index);
            OssClient client = ClientUtils.getClient(cloudEnum);
            if (client != null) {
                String url;
                try {
                    url = client.upload(inputStream, TEST_FILE_NAME, (JPanel) this.authorizationTabbedPanel.getComponentAt(index));
                } catch (Exception exception) {
                    //显示对话框
                    Messages.showMessageDialog(this.myMainPanel,
                                               exception.getMessage(),
                                               "Error",
                                               Messages.getErrorIcon());
                    return;
                }
                if (StringUtils.isNotBlank(url)) {
                    // 测试通过了, 则判断是否勾选设置默认图床, 若勾选则刷新可用状态
                    boolean isDefaultCheckBox = this.defaultCloudCheckBox.isSelected();
                    if (isDefaultCheckBox) {
                        int cloudTypeIndex = this.defaultCloudComboBox.getSelectedIndex();
                        if (index == cloudTypeIndex) {
                            this.customMessage.setText("");
                        }
                    }
                    Messages.showMessageDialog(this.myMainPanel,
                                               cloudEnum.getTitle() + " 上传成功",
                                               "Successed",
                                               Messages.getInformationIcon());
                    // 主动保存
                    this.apply();
                    if (log.isTraceEnabled()) {
                        BrowserUtil.browse(url);
                    }
                } else {
                    Messages.showMessageDialog(this.myMainPanel,
                                               cloudEnum.getTitle() + " 上传失败",
                                               "Error",
                                               Messages.getErrorIcon());
                }
            } else {
                Messages.showMessageDialog(this.myMainPanel,
                                           cloudEnum.getTitle() + " 不可用, 请检查配置是否正确",
                                           "Error",
                                           Messages.getErrorIcon());
            }
        });

        // help button 监听
        this.helpButton.addActionListener(e -> {
            String url = MikNotification.helpUrl(HelpType.CUSTOM.where);
            CloudEnum cloudType = OssState.getCloudType(this.authorizationTabbedPanel.getSelectedIndex());
            if (cloudType != CloudEnum.CUSTOMIZE) {
                url = MikNotification.helpUrl(HelpType.SETTING.where);
            }
            if (!url.equals(MikNotification.ABOUT_BLANK)) {
                BrowserUtil.browse(url);
            }
        });
    }

    /**
     * 初始化上传配置组
     * <p>
     * 根据传入的 state 初始化上传相关的配置项，包括默认图床选择、压缩设置、文件重命名和水印配置等。
     *
     * @param state 用于初始化配置的 MikState 对象
     */
    private void initGlobalPanel(@NotNull MikState state) {
        boolean isDefaultCloudCheck = state.isDefaultCloudCheck();
        this.defaultCloudCheckBox.setSelected(isDefaultCloudCheck);
        // 没有设置默认图床, 则显示提示消息
        if (!isDefaultCloudCheck) {
            this.customMessage.setText("sm.ms");
        } else {
            this.customMessage.setText(OssState.getStatus(state.getCloudType()) ? "" : MikBundle.message("oss.not.available"));
        }
        this.defaultCloudComboBox.setEnabled(isDefaultCloudCheck);
        this.defaultCloudComboBox.setSelectedIndex(state.getCloudType());

        this.showSelectCloudMessage(state.getCloudType());

        this.defaultCloudCheckBox.addActionListener(e -> {
            this.showSelectCloudMessage(state.getTempCloudType());
            this.defaultCloudComboBox.setEnabled(this.defaultCloudCheckBox.isSelected());
        });

        this.defaultCloudComboBox.addActionListener(e -> {
            JComboBox<?> jComboBox = (JComboBox<?>) e.getSource();
            int currentSelectIndex = jComboBox.getSelectedIndex();
            this.showSelectCloudMessage(currentSelectIndex);
            if (currentSelectIndex == CloudEnum.SM_MS_CLOUD.index) {
                this.defaultCloudComboBox.setSelectedIndex(state.getTempCloudType());
            }
            state.setTempCloudType(currentSelectIndex);
        });

        this.initChangeToHtmlGroup();
        this.initCompressGroup();

        // 初始化上传图片的后缀
        this.renameCheckBox.setSelected(this.config.getState().isRename());
        this.fileNameSuffixBoxField.setSelectedIndex(state.getSuffixIndex());
        this.fileNameSuffixBoxField.setEnabled(this.config.getState().isRename());
        this.renameCheckBox.addChangeListener(e -> {
            JCheckBox checkBox = (JCheckBox) e.getSource();
            this.fileNameSuffixBoxField.setEnabled(checkBox.isSelected());
        });

        // 水印配置初始化
        this.watermarkCheckBox.setSelected(this.config.getState().isWatermark());
        this.watermarkTextField.setEnabled(this.watermarkCheckBox.isSelected());
        this.watermarkCheckBox.addActionListener(e -> this.watermarkTextField.setEnabled(this.watermarkCheckBox.isSelected()));
    }

    /**
     * 根据云类型显示选择云的提示信息
     * <p>
     * 根据传入的云类型判断是否启用OSS服务，并设置对应的提示信息。
     *
     * @param cloudType 云类型
     */
    private void showSelectCloudMessage(int cloudType) {
        if (this.defaultCloudCheckBox.isSelected()) {
            boolean isClientEnable = OssState.getStatus(cloudType);
            this.customMessage.setText(isClientEnable ? "" : MikBundle.message("oss.not.available"));
        } else {
            this.customMessage.setText("sm.ms");
        }
    }

    /**
     * 初始化替换标签设置组，设置相关组件的初始状态和监听器
     * <p>
     * 该方法用于初始化文本框和单选按钮的焦点监听和状态变化监听，根据配置信息设置默认选中项和组件可用性。
     *
     * @since 0.0.1
     */
    private void initChangeToHtmlGroup() {
        this.customHtmlTypeTextField.addFocusListener(new JTextFieldHintListener(this.customHtmlTypeTextField,
                                                                                 MikBundle.message("mik.change.mark.message")));

        // 初始化 changeToHtmlTagCheckBox 选中状态
        // 设置被选中
        boolean changeToHtmlTagCheckBoxStatus = this.config.getState().isChangeToHtmlTag();

        this.changeToHtmlTagCheckBox.setSelected(changeToHtmlTagCheckBoxStatus);
        // 设置组下单选框可用
        this.largePictureRadioButton.setEnabled(changeToHtmlTagCheckBoxStatus);
        this.commonRadioButton.setEnabled(changeToHtmlTagCheckBoxStatus);
        this.customRadioButton.setEnabled(changeToHtmlTagCheckBoxStatus);

        // 初始化 changeToHtmlTagCheckBox 组下单选框状态
        if (ImageMarkEnum.COMMON_PICTURE.text.equals(this.config.getState().getTagType())) {
            this.commonRadioButton.setSelected(true);
        } else if (ImageMarkEnum.LARGE_PICTURE.text.equals(this.config.getState().getTagType())) {
            this.largePictureRadioButton.setSelected(true);
        } else if (ImageMarkEnum.CUSTOM.text.equals(this.config.getState().getTagType())) {
            this.customRadioButton.setSelected(true);
            this.customHtmlTypeTextField.setEnabled(changeToHtmlTagCheckBoxStatus);
            this.customHtmlTypeTextField.setText(this.config.getState().getTagTypeCode());
        } else {
            this.commonRadioButton.setSelected(true);
        }

        // changeToHtmlTagCheckBox 监听, 修改组下组件状态
        this.changeToHtmlTagCheckBox.addChangeListener(e -> {
            JCheckBox checkBox = (JCheckBox) e.getSource();
            this.largePictureRadioButton.setEnabled(checkBox.isSelected());
            this.commonRadioButton.setEnabled(checkBox.isSelected());
            this.customRadioButton.setEnabled(checkBox.isSelected());
            // 如果原来自定义选项被选中, 则将输入框设置为可用
            if (this.customRadioButton.isSelected() && checkBox.isSelected()) {
                this.customHtmlTypeTextField.setEnabled(true);
            }
        });
        ButtonGroup group = new ButtonGroup();
        this.addChangeTagRadioButton(group, this.largePictureRadioButton);
        this.addChangeTagRadioButton(group, this.commonRadioButton);
        this.addChangeTagRadioButton(group, this.customRadioButton);
    }

    /**
     * 处理被选中的单选框，更新自定义HTML类型文本框的可用状态
     * <p>
     * 将指定的单选框添加到按钮组中，并为其添加监听器。当单选框状态发生变化时，
     * 根据其文本内容决定自定义HTML类型文本框是否可用。
     *
     * @param group  按钮组
     * @param button 被选中的单选框
     * @since 0.0.1
     */
    private void addChangeTagRadioButton(@NotNull ButtonGroup group, JRadioButton button) {
        group.add(button);
        // 构造一个监听器，响应checkBox事件
        ActionListener actionListener = e -> {
            Object sourceObject = e.getSource();
            if (sourceObject instanceof JRadioButton sourceButton) {
                this.customHtmlTypeTextField.setEnabled(ImageMarkEnum.CUSTOM.text.equals(sourceButton.getText()));
            }
        };
        button.addActionListener(actionListener);
    }

    /**
     * 初始化图片压缩配置组
     * <p>
     * 该方法用于初始化与图片压缩相关的配置组件，包括压缩状态的设置、滑块控件的配置以及事件监听的绑定。
     * 主要设置压缩状态是否启用，滑块的取值范围、刻度显示以及与压缩状态的联动逻辑。
     *
     * @since 0.0.1
     */
    private void initCompressGroup() {
        boolean compressStatus = this.config.getState().isCompress();
        // 设置被选中
        this.compressCheckBox.setSelected(compressStatus);

        this.compressSlider.setEnabled(compressStatus);
        this.compressSlider.setValue(this.config.getState().getCompressBeforeUploadOfPercent());

        // 设置主刻度间隔
        this.compressSlider.setMajorTickSpacing(10);
        // 设置次刻度间隔
        this.compressSlider.setMinorTickSpacing(2);
        // 绘制 刻度 和 标签
        this.compressSlider.setPaintTicks(true);
        this.compressSlider.setPaintLabels(true);
        this.compressSlider.addChangeListener(e -> this.compressLabel.setText(String.valueOf(this.compressSlider.getValue())));

        this.compressLabel.setText(String.valueOf(this.compressSlider.getValue()));

        // compressCheckBox 监听, 修改组下组件状态
        this.compressCheckBox.addChangeListener(e -> {
            JCheckBox checkBox = (JCheckBox) e.getSource();
            this.compressSlider.setEnabled(checkBox.isSelected());
        });
    }

    /**
     * 初始化剪贴板控制相关组件
     * <p>
     * 用于初始化剪贴板操作相关的UI组件，包括设置复选框状态、文本框内容以及添加事件监听器。
     *
     * @since 0.0.1
     */
    private void initClipboardControl() {
        // 设置是否勾选
        boolean isCopyToDir = this.config.getState().isCopyToDir();
        boolean isUploadAndReplace = this.config.getState().isUploadAndReplace();
        this.copyToDirCheckBox.setSelected(isCopyToDir);
        this.uploadAndReplaceCheckBox.setSelected(isUploadAndReplace);

        // 设置 copy 位置
        this.whereToCopyTextField.setText(this.config.getState().getImageSavePath());
        this.whereToCopyTextField.setEnabled(isCopyToDir);

        // 设置 copyToDirCheckBox 监听
        this.copyToDirCheckBox.addChangeListener(e -> {
            JCheckBox checkBox = (JCheckBox) e.getSource();
            this.whereToCopyTextField.setEnabled(checkBox.isSelected());
        });
    }

    /**
     * 判断 GUI 是否有变化
     * <p>
     * 检查各个 OSS 配置项和通用设置是否发生修改，若全部未修改则返回 false，否则返回 true
     *
     * @return 是否有变化
     * @since 0.0.1
     */
    @Override
    public boolean isModified() {
        log.trace("isModified invoke");
        MikState state = this.config.getState();

        return !(this.aliyunOssSetting.isModified(state.getAliyunOssState())
                 && this.baiduBosSetting.isModified(state.getBaiduBosState())
                 && this.githubSetting.isModified(state.getGithubOssState())
                 && this.giteeSetting.isModified(state.getGiteeOssState())
                 && this.qiniuOssSetting.isModified(state.getQiniuOssState())
                 && this.tencentOssSetting.isModified(state.getTencentOssState())
                 && this.customOssSetting.isModified(state.getCustomOssState())
                 && this.picListOssSetting.isModified(state.getPicListOssState())
                 && this.isGeneralModified(state)
                 && this.isClipboardModified(state)
        );
    }

    /**
     * 判断当前状态是否与给定的MikState对象状态一致
     * <p>
     * 比较当前界面配置状态与传入的MikState对象的各个属性是否一致，包括是否替换为HTML标签、标签类型、压缩设置、重命名设置、水印设置等。
     *
     * @param state 要比较的MikState对象
     * @return 如果当前状态与给定状态一致，返回true；否则返回false
     */
    private boolean isGeneralModified(MikState state) {
        // 是否替换标签
        boolean changeToHtmlTag = this.changeToHtmlTagCheckBox.isSelected();
        // 替换的标签类型
        String tagType = "";
        // 替换的标签类型 code
        String tagTypeCode = "";
        if (changeToHtmlTag) {
            // 正常的
            if (this.commonRadioButton.isSelected()) {
                tagType = ImageMarkEnum.COMMON_PICTURE.text;
                tagTypeCode = ImageMarkEnum.COMMON_PICTURE.code;
            }
            // 点击看大图
            else if (this.largePictureRadioButton.isSelected()) {
                tagType = ImageMarkEnum.LARGE_PICTURE.text;
                tagTypeCode = ImageMarkEnum.LARGE_PICTURE.code;
            }
            // 自定义
            else if (this.customRadioButton.isSelected()) {
                tagType = ImageMarkEnum.CUSTOM.text;
                // todo-dong4j : (2019年03月14日 14:30) [格式验证]
                tagTypeCode = this.customHtmlTypeTextField.getText().trim();
            }
        }

        // 是否压缩图片
        boolean compress = this.compressCheckBox.isSelected();
        // 压缩比例
        int compressBeforeUploadOfPercent = this.compressSlider.getValue();

        boolean isRename = this.renameCheckBox.isSelected();
        // 图片后缀
        int index = this.fileNameSuffixBoxField.getSelectedIndex();
        boolean isDefaultCloudCheck = this.defaultCloudCheckBox.isSelected();

        // 是否开启水印
        boolean isWatermark = this.watermarkCheckBox.isSelected();
        String watermarkText = this.watermarkTextField.getText();

        return changeToHtmlTag == state.isChangeToHtmlTag()
               && tagType.equals(state.getTagType())
               && tagTypeCode.equals(state.getTagTypeCode())
               && compress == state.isCompress()
               && compressBeforeUploadOfPercent == state.getCompressBeforeUploadOfPercent()
               && isWatermark == state.isWatermark()
               && watermarkText.equals(state.getWatermarkText())
               && isRename == state.isRename()
               && index == state.getSuffixIndex()
               && isDefaultCloudCheck == state.isDefaultCloudCheck()
               && this.defaultCloudComboBox.getSelectedIndex() == state.getCloudType();

    }

    /**
     * 判断剪贴板内容是否已修改
     * <p>
     * 比较当前剪贴板状态与传入的MikState对象的状态，判断是否发生改变。
     *
     * @param state 要比较的MikState对象
     * @return 如果剪贴板内容与传入状态一致，返回true；否则返回false
     * @since 0.0.1
     */
    private boolean isClipboardModified(@NotNull MikState state) {
        boolean copyToDir = this.copyToDirCheckBox.isSelected();
        boolean uploadAndReplace = this.uploadAndReplaceCheckBox.isSelected();
        String whereToCopy = this.whereToCopyTextField.getText().trim();

        return copyToDir == state.isCopyToDir()
               && uploadAndReplace == state.isUploadAndReplace()
               && whereToCopy.equals(state.getImageSavePath());
    }

    /**
     * 配置被修改后调用，用于更新 state 中的数据
     * <p>
     * 当配置发生变化时，该方法会被调用，用于将配置信息应用到 state 对象中，更新相关存储设置和通用配置。
     */
    @Override
    public void apply() {
        log.trace("apply invoke");
        MikState state = this.config.getState();

        this.aliyunOssSetting.apply(state.getAliyunOssState());
        this.baiduBosSetting.apply(state.getBaiduBosState());
        this.githubSetting.apply(state.getGithubOssState());
        this.giteeSetting.apply(state.getGiteeOssState());
        this.qiniuOssSetting.apply(state.getQiniuOssState());
        this.tencentOssSetting.apply(state.getTencentOssState());
        this.customOssSetting.apply(state.getCustomOssState());
        this.picListOssSetting.apply(state.getPicListOssState());
        this.applyGeneralConfigs(state);
        this.applyClipboardConfigs(state);
    }

    /**
     * 应用通用配置到状态对象
     * <p>
     * 根据界面组件的状态，将相关配置应用到传入的MikState对象中，包括标签类型、压缩设置、重命名设置等。
     *
     * @param state 状态对象，用于存储配置信息
     */
    private void applyGeneralConfigs(@NotNull MikState state) {
        state.setChangeToHtmlTag(this.changeToHtmlTagCheckBox.isSelected());
        if (this.changeToHtmlTagCheckBox.isSelected()) {
            // 正常的
            if (this.commonRadioButton.isSelected()) {
                state.setTagType(ImageMarkEnum.COMMON_PICTURE.text);
                state.setTagTypeCode(ImageMarkEnum.COMMON_PICTURE.code);
            }
            // 点击看大图
            else if (this.largePictureRadioButton.isSelected()) {
                state.setTagType(ImageMarkEnum.LARGE_PICTURE.text);
                state.setTagTypeCode(ImageMarkEnum.LARGE_PICTURE.code);
            }
            // 自定义
            else if (this.customRadioButton.isSelected()) {
                state.setTagType(ImageMarkEnum.CUSTOM.text);
                // todo-dong4j : (2019年03月14日 14:30) [格式验证]
                state.setTagTypeCode(this.customHtmlTypeTextField.getText().trim());
            }
        }
        state.setCompress(this.compressCheckBox.isSelected());
        state.setCompressBeforeUploadOfPercent(this.compressSlider.getValue());
        state.setRename(this.renameCheckBox.isSelected());
        state.setSuffixIndex(this.fileNameSuffixBoxField.getSelectedIndex());
        state.setDefaultCloudCheck(this.defaultCloudCheckBox.isSelected());
        state.setCloudType(state.isDefaultCloudCheck() ? this.defaultCloudComboBox.getSelectedIndex() : CloudEnum.SM_MS_CLOUD.index);
        state.setTempCloudType(this.defaultCloudComboBox.getSelectedIndex());
        state.setWatermark(this.watermarkCheckBox.isSelected());
        state.setWatermarkText(this.watermarkTextField.getText());
    }

    /**
     * 应用剪贴板配置
     * <p>
     * 根据界面组件的状态，将剪贴板相关配置应用到MikState对象中
     *
     * @param state 保存剪贴板配置的MikState对象
     * @since 0.0.1
     */
    private void applyClipboardConfigs(@NotNull MikState state) {
        state.setCopyToDir(this.copyToDirCheckBox.isSelected());
        state.setUploadAndReplace(this.uploadAndReplaceCheckBox.isSelected());
        state.setImageSavePath(this.whereToCopyTextField.getText().trim());
    }

    /**
     * 重置配置信息
     * <p>
     * 该方法用于重置所有OSS相关配置和通用配置，通过传入的MikState对象获取配置状态并进行重置。
     */
    @Override
    public void reset() {
        log.trace("reset invoke");
        MikState state = this.config.getState();

        this.aliyunOssSetting.reset(state.getAliyunOssState());
        this.baiduBosSetting.reset(state.getBaiduBosState());
        this.githubSetting.reset(state.getGithubOssState());
        this.giteeSetting.reset(state.getGiteeOssState());
        this.tencentOssSetting.reset(state.getTencentOssState());
        this.qiniuOssSetting.reset(state.getQiniuOssState());
        this.customOssSetting.reset(state.getCustomOssState());
        this.picListOssSetting.reset(state.getPicListOssState());
        this.resetGeneralCOnfigs(state);
        this.resetClipboardConfigs(state);
    }

    /**
     * 重置通用配置项
     * <p>
     * 根据传入的MikState对象，将界面中的各个配置项重置为对应的状态值。
     *
     * @param state 用于重置配置的MikState对象
     */
    private void resetGeneralCOnfigs(@NotNull MikState state) {
        this.changeToHtmlTagCheckBox.setSelected(state.isChangeToHtmlTag());
        this.largePictureRadioButton.setSelected(state.getTagType().equals(ImageMarkEnum.LARGE_PICTURE.text));
        this.commonRadioButton.setSelected(state.getTagType().equals(ImageMarkEnum.CUSTOM.text));
        this.customRadioButton.setSelected(state.getTagType().equals(ImageMarkEnum.CUSTOM.text));
        this.customHtmlTypeTextField.setText(state.getTagTypeCode());
        this.compressCheckBox.setSelected(state.isCompress());
        this.compressSlider.setValue(state.getCompressBeforeUploadOfPercent());
        this.compressLabel.setText(String.valueOf(this.compressSlider.getValue()));
        this.renameCheckBox.setSelected(state.isRename());
        this.fileNameSuffixBoxField.setSelectedIndex(state.getSuffixIndex());
        this.defaultCloudCheckBox.setSelected(state.isDefaultCloudCheck());
        this.defaultCloudComboBox.setSelectedIndex(state.getCloudType());
        this.watermarkCheckBox.setSelected(state.isWatermark());
        this.watermarkTextField.setText(state.getWatermarkText());
    }

    /**
     * 重置剪贴板配置
     * <p>
     * 根据传入的MikState对象，更新剪贴板相关组件的状态，包括复制到目录的勾选状态、保存路径文本以及上传并替换的勾选状态。
     *
     * @param state 用于重置配置的MikState对象
     * @since 0.0.1
     */
    private void resetClipboardConfigs(@NotNull MikState state) {
        this.copyToDirCheckBox.setSelected(state.isCopyToDir());
        this.whereToCopyTextField.setText(state.getImageSavePath());
        this.uploadAndReplaceCheckBox.setSelected(state.isUploadAndReplace());
    }
}
