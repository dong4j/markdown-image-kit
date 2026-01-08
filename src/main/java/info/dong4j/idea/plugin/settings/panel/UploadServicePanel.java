package info.dong4j.idea.plugin.settings.panel;

import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.ui.TextComponentAccessor;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.ui.components.JBLabel;
import com.intellij.util.ui.JBUI;

import info.dong4j.idea.plugin.MikBundle;
import info.dong4j.idea.plugin.client.OssClient;
import info.dong4j.idea.plugin.enums.CloudEnum;
import info.dong4j.idea.plugin.enums.HelpType;
import info.dong4j.idea.plugin.notify.MikNotification;
import info.dong4j.idea.plugin.settings.MikPersistenComponent;
import info.dong4j.idea.plugin.settings.MikState;
import info.dong4j.idea.plugin.settings.OssState;
import info.dong4j.idea.plugin.settings.oss.AliyunOssSetting;
import info.dong4j.idea.plugin.settings.oss.BaiduBosSetting;
import info.dong4j.idea.plugin.settings.oss.CustomOssSetting;
import info.dong4j.idea.plugin.settings.oss.GiteeSetting;
import info.dong4j.idea.plugin.settings.oss.GithubSetting;
import info.dong4j.idea.plugin.settings.oss.PicListOssSetting;
import info.dong4j.idea.plugin.settings.oss.QiniuOssSetting;
import info.dong4j.idea.plugin.settings.oss.SmmsOssSetting;
import info.dong4j.idea.plugin.settings.oss.TencentOssSetting;
import info.dong4j.idea.plugin.util.ClientUtils;
import info.dong4j.idea.plugin.util.StringUtils;
import info.dong4j.idea.plugin.util.SwingUtils;

import org.jetbrains.annotations.NotNull;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * 上传服务设定面板
 * <p>
 * 该类用于构建和管理上传服务的配置界面，支持多种云服务商（如 Sm.ms、阿里云 OSS、百度云 BOS、GitHub、Gitee、七牛云、腾讯云 COS、自定义 OSS 和 PicList）的配置面板。
 * 用户可以通过下拉框选择不同的云服务商，并动态切换对应的配置面板，实现灵活的上传服务配置。
 *
 * @author dong4j
 * @version 1.0.0
 * @date 2025.10.31
 * @since 1.0.0
 */
@Slf4j
@SuppressWarnings( {"D", "DuplicatedCode"})
public class UploadServicePanel {
    /** 测试文件名，用于测试场景中的文件标识 */
    public static final String TEST_FILE_NAME = "mik.webp";

    // ========== 上传服务设定区域 ==========
    /** 上传服务的配置面板，用于展示和修改上传服务相关设置 */
    @Getter
    private JPanel content;
    /**
     * 图床服务商下拉框
     * <p>
     * 【字段映射】对应老页面的 authorizationTabbedPanel (JTabbedPane)，新页面改为下拉框切换配置面板
     */
    private JComboBox<String> cloudServiceComboBox;
    /** 云服务商配置容器 <br> 用于动态切换不同服务商的配置面板 */
    private JPanel cloudServiceConfigContainer;
    /** 设为默认图床复选框，用于标识是否将当前图床设为默认图床 */
    private JCheckBox setAsDefaultCloudCheckBox;

    /** 自定义消息标签，用于显示用户自定义的提示信息 */
    private JLabel cloudServerAvailableMessage;
    /** 验证图片上传按钮，【字段映射】对应老页面的 testButton */
    private JButton testUploadButton;
    /**
     * 帮助按钮
     * <p>
     * 【字段映射】对应老页面的 helpButton
     */
    private JButton helpButton;

    // ========== 各个云服务商的配置面板 ==========
    //region Sm.ms
    /** Sm.ms 配置面板，用于展示和设置 Sm.ms 相关的配置信息 */
    private JPanel smmsConfigPanel;
    /**
     * Sm.ms API URL 输入框
     * <p>
     * 【字段映射】对应老页面的 smmsUrlTextField
     */
    private JTextField smmsUrlTextField;
    /** Sm.ms Token 输入框，用于输入用户Token值，【字段映射】对应老页面的 smmsTokenTextField */
    private JPasswordField smmsTokenTextField;
    //endregion

    // region 阿里云 OSS
    /** 阿里云 OSS 配置面板，用于展示和管理 OSS 相关配置项 */
    private JPanel aliyunOssConfigPanel;
    /** 阿里云 Bucket 名称输入框，对应老页面的 aliyunOssBucketNameTextField 字段 */
    private JTextField aliyunBucketNameTextField;
    /** 阿里云 Access Key 输入框 【字段映射】对应老页面的 aliyunOssAccessKeyTextField */
    private JTextField aliyunAccessKeyTextField;
    /**
     * 阿里云 Access Secret 输入框
     * <p>
     * 【字段映射】对应老页面的 aliyunOssAccessSecretKeyTextField
     */
    private JPasswordField aliyunAccessSecretTextField;
    /** 阿里云 Endpoint 输入框，对应老页面的 aliyunOssEndpointTextField 字段 */
    private JTextField aliyunEndpointTextField;
    /**
     * 阿里云文件目录输入框
     * <p>
     * 【字段映射】对应老页面的 aliyunOssFileDirTextField
     */
    private JTextField aliyunFileDirTextField;
    /**
     * 阿里云自定义域名复选框
     * <p>
     * 【字段映射】对应老页面的 aliyunOssCustomEndpointCheckBox
     */
    private JCheckBox aliyunCustomEndpointCheckBox;
    /** 阿里云自定义域名输入框，用于输入自定义的 OSS 域名地址 */
    private JTextField aliyunCustomEndpointTextField;
    //endregion

    //region 百度云BOS
    /** 百度云 BOS 配置面板，用于展示和管理百度云对象存储服务的相关配置信息 */
    private JPanel baiduBosConfigPanel;
    /** 百度云 Bucket 名称输入框，对应老页面的 baiduBosBucketNameTextField 字段 */
    private JTextField baiduBosBucketNameTextField;
    /** 百度云 Access Key 输入框 【字段映射】对应老页面的 baiduBosAccessKeyTextField */
    private JTextField baiduBosAccessKeyTextField;
    /** 百度云 Access Secret 输入框，对应老页面的 baiduBosAccessSecretKeyTextField 字段 */
    private JPasswordField baiduBosAccessSecretTextField;
    /** 百度云 Endpoint 输入框，对应老页面的 baiduBosEndpointTextField 字段 */
    private JTextField baiduBosEndpointTextField;
    /** 百度云文件目录输入框，用于输入百度云文件存储目录路径 */
    private JTextField baiduBosFileDirTextField;
    /**
     * 百度云自定义域名复选框
     * <p>
     * 【字段映射】对应老页面的 baiduBosCustomEndpointCheckBox
     */
    private JCheckBox baiduBosCustomEndpointCheckBox;
    /** 百度云自定义域名输入框 【字段映射】对应老页面的 baiduBosCustomEndpointTextField */
    private JTextField baiduBosCustomEndpointTextField;
    //endregion

    //region GitHub
    /** GitHub 配置面板，用于展示和管理 GitHub 相关的配置信息 */
    private JPanel githubConfigPanel;
    /**
     * GitHub 仓库输入框
     * <p>
     * 【字段映射】对应老页面的 githubReposTextField
     */
    private JTextField githubReposTextField;
    /** GitHub 分支输入框 */
    private JTextField githubBranchTextField;
    /**
     * GitHub Token 输入框
     * <p>
     * 【字段映射】对应老页面的 githubTokenTextField
     */
    private JPasswordField githubTokenTextField;
    /**
     * GitHub 文件目录输入框
     * <p>
     * 【字段映射】对应老页面的 githubFileDirTextField
     */
    private JTextField githubFileDirTextField;
    /**
     * GitHub 自定义域名复选框
     * <p>
     * 【字段映射】对应老页面的 githubCustomEndpointCheckBox
     */
    private JCheckBox githubCustomEndpointCheckBox;
    /**
     * GitHub 自定义域名输入框
     * <p>
     * 【字段映射】对应老页面的 githubCustomEndpointTextField
     */
    private JTextField githubCustomEndpointTextField;
    //endregion

    //region Gitee
    /** Gitee 配置面板，用于展示和管理 Gitee 相关的配置项 */
    private JPanel giteeConfigPanel;
    /**
     * Gitee 仓库输入框
     * <p>
     * 【字段映射】对应老页面的 giteeReposTextField
     */
    private JTextField giteeReposTextField;
    /** Gitee 分支输入框 */
    private JTextField giteeBranchTextField;
    /** Gitee Token 输入框，用于输入用户的 Gitee Token，对应老页面的 giteeTokenTextField 字段 */
    private JPasswordField giteeTokenTextField;
    /** Gitee 文件目录输入框，用于接收用户输入的文件目录路径，【字段映射】对应老页面的 giteeFileDirTextField */
    private JTextField giteeFileDirTextField;
    /** Gitee 自定义域名复选框，用于标识是否启用自定义域名配置 */
    private JCheckBox giteeCustomEndpointCheckBox;
    /** Gitee 自定义域名输入框，用于输入自定义的 Gitee 域名地址，【字段映射】对应老页面的 giteeCustomEndpointTextField */
    private JTextField giteeCustomEndpointTextField;
    //endregion

    //region 七牛云
    /** 七牛云配置面板，用于展示和管理七牛云对象存储的相关配置项 */
    private JPanel qiniuOssConfigPanel;
    /** 七牛云 Bucket 名称输入框，对应老页面的 qiniuOssBucketNameTextField 字段 */
    private JTextField qiniuOssBucketNameTextField;
    /**
     * 七牛云 Access Key 输入框
     * <p>
     * 【字段映射】对应老页面的 qiniuOssAccessKeyTextField
     */
    private JTextField qiniuOssAccessKeyTextField;
    /**
     * 七牛云 Access Secret 输入框
     * <p>
     * 【字段映射】对应老页面的 qiniuOssAccessSecretKeyTextField
     */
    private JPasswordField qiniuOssAccessSecretTextField;
    /** 七牛云 Domain 输入框，对应老页面的 qiniuOssUpHostTextField 字段 */
    private JTextField qiniuOssDomainTextField;
    /** 七牛云区域 - 华东单选按钮，对应老页面的 qiniuOssEastChinaRadioButton 字段映射 */
    private JRadioButton qiniuOssEastChinaRadioButton;
    /** 七牛云区域 - 华北单选按钮，对应老页面的 qiniuOssNortChinaRadioButton */
    private JRadioButton qiniuOssNortChinaRadioButton;
    /** 七牛云区域 - 华南单选按钮，对应老页面的 qiniuOssSouthChinaRadioButton 字段映射 */
    private JRadioButton qiniuOssSouthChinaRadioButton;
    /** 七牛云区域 - 北美单选按钮，对应老页面的 qiniuOssNorthAmeriaRadioButton 字段 */
    private JRadioButton qiniuOssNorthAmeriaRadioButton;
    /** 七牛云 OSS 东南亚区域选择单选按钮 */
    private JRadioButton qiniuOssSoutheastAsiaRadioButton;
    /** 七牛云区域索引隐藏字段，对应老页面的 zoneIndexTextFiled */
    private JTextField qiniuZoneIndexTextField;
    //endregion

    //region 腾讯云 COS
    /** 腾讯云 COS 配置面板，用于展示和管理腾讯云对象存储服务的相关配置信息 */
    private JPanel tencentOssConfigPanel;
    /** 腾讯云 Bucket 名称输入框，对应老页面的 tencentBacketNameTextField 字段 */
    private JTextField tencentBucketNameTextField;
    /** 腾讯云 Access Key 输入框，对应老页面的 tencentAccessKeyTextField 字段 */
    private JTextField tencentAccessKeyTextField;
    /** 腾讯云 Secret Key 输入框，用于输入腾讯云的密钥信息，对应老页面的 tencentSecretKeyTextField 字段 */
    private JPasswordField tencentSecretKeyTextField;
    /** 腾讯云 Region Name 输入框，对应老页面的 tencentRegionNameTextField 字段 */
    private JTextField tencentRegionNameTextField;
    //endregion

    //region 自定义 OSS
    /** 自定义 OSS 配置面板，用于展示和修改 OSS 相关配置项 */
    private JPanel customOssConfigPanel;
    /** 自定义 OSS API 输入框，用于输入自定义 API 地址 */
    private JTextField customApiTextField;
    /** 自定义 OSS 请求 Key 输入框，用于输入用户自定义的请求 Key 值 */
    private JTextField customRequestKeyTextField;
    /** 自定义 OSS 响应 URL 路径输入框，用于输入自定义的 URL 路径 */
    private JTextField customResponseUrlPathTextField;
    /** 自定义 OSS HTTP 方法输入框，用于输入自定义的 HTTP 方法，对应老页面的 httpMethodTextField */
    private JTextField customHttpMethodTextField;
    //endregion

    //region PicList
    /** PicList 配置面板，用于展示和设置图片列表相关配置项 */
    private JPanel picListConfigPanel;
    /** PicList API 输入框，对应老页面的 picListApiTextField 字段 */
    private JTextField picListApiTextField;
    /**
     * PicList 图床类型输入框
     * <p>
     * 【字段映射】对应老页面的 picListPicbedTextField
     */
    private JTextField picListPicbedTextField;
    /** PicList 配置名称输入框，用于输入配置名称，对应老页面的 picListConfigNameTextField */
    private JTextField picListConfigNameTextField;
    /** PicList 密钥输入框，用于输入 PicList 的密钥值，对应老页面的 picListKeyTextField 字段 */
    private JTextField picListKeyTextField;
    /**
     * PicList 命令行路径输入框（带浏览按钮）
     * <p>
     * 【字段映射】对应老页面的 picListExeTextField (TextFieldWithBrowseButton)
     */
    private TextFieldWithBrowseButton picListExeTextField;
    //endregion

    // ========== OSS Setting 对象 ==========
    /** Sm.ms OSS 配置信息 */
    private SmmsOssSetting smmsOssSetting;
    /** 阿里云 OSS 配置信息 */
    private AliyunOssSetting aliyunOssSetting;
    /** 百度对象存储（BOS）配置信息 */
    private BaiduBosSetting baiduBosSetting;
    /** GitHub 设置对象 */
    private GithubSetting githubSetting;
    /** Gitee 设置信息 */
    private GiteeSetting giteeSetting;
    /** 七牛云 OSS 配置信息 */
    private QiniuOssSetting qiniuOssSetting;
    /** TencentOssSetting 对象，用于存储腾讯云对象存储服务相关配置信息 */
    private TencentOssSetting tencentOssSetting;
    /** 自定义 OSS 配置信息实例 */
    private CustomOssSetting customOssSetting;
    /** PicList 图床配置信息实例 */
    private PicListOssSetting picListOssSetting;

    /**
     * 构造函数，初始化上传服务面板
     * <p>
     * 调用 createUploadServicePanel 方法创建上传服务面板组件
     */
    public UploadServicePanel() {
        createUploadServicePanel();
    }

    /**
     * 创建上传服务设定区域面板
     * <p>
     * 初始化并构建上传服务配置面板，包含云服务商选择、默认图床设置、配置容器以及测试和帮助按钮。
     * 面板使用网格布局进行组件排列，支持响应式布局调整。
     */
    private void createUploadServicePanel() {
        content = new JPanel();
        content.setLayout(new GridBagLayout());
        content.setBorder(SwingUtils.configureTitledBorder(MikBundle.message("panel.upload.service.title")));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = JBUI.insets(5, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // 云服务商下拉选择 - 放在最前面，与配置面板中的标签和输入框对齐
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.weightx = 0;
        content.add(new JBLabel(MikBundle.message("panel.upload.service.cloud.provider")), gbc);

        gbc.gridx = 1;
        gbc.gridwidth = 1;
        gbc.weightx = 1.0; // 设置较大的权重，占比更大
        cloudServiceComboBox = new com.intellij.openapi.ui.ComboBox<>(createCloudServiceOptions());
        content.add(cloudServiceComboBox, gbc);

        // 默认图床复选框和下拉框 - 与选择服务商在同一行
        gbc.gridx = 2;
        gbc.gridwidth = 1;
        gbc.weightx = 0;
        setAsDefaultCloudCheckBox = new JCheckBox(MikBundle.message("panel.upload.service.set.as.default"));
        content.add(setAsDefaultCloudCheckBox, gbc);

        // 配置消息字段 cloudServerAvailableMessage
        gbc.gridx = 4;
        gbc.gridwidth = 1;
        gbc.weightx = 0;
        cloudServerAvailableMessage = new JBLabel("");
        cloudServerAvailableMessage.setHorizontalAlignment(SwingConstants.RIGHT); // 文字右对齐
        content.add(cloudServerAvailableMessage, gbc);

        // 云服务商配置容器
        gbc.gridx = 0;           // 组件在网格中的列位置（起始列为第0列）
        gbc.gridy = 1;           // 组件在网格中的行位置（位于第1行，在服务商选择行下方）
        gbc.gridwidth = 5;       // 组件横向跨越的列数（跨越所有6列，gridx 0-5）
        gbc.weightx = 1.0;       // 横向权重：当窗口横向扩展时，该组件获得额外空间的权重（1.0表示会按比例分配）
        gbc.weighty = 1.0;       // 纵向权重：当窗口纵向扩展时，该组件获得额外空间的权重（1.0表示会按比例分配，占满剩余垂直空间）
        gbc.fill = GridBagConstraints.BOTH;  // 填充方式：当组件尺寸小于分配空间时，同时在横向和纵向方向填充空间
        cloudServiceConfigContainer = new JPanel(new BorderLayout());
        cloudServiceConfigContainer.setBorder(BorderFactory.createEtchedBorder()); // 添加蚀刻边框，使容器区域更清晰
        cloudServiceConfigContainer.add(createSmmsConfigPanel(), BorderLayout.CENTER);
        content.add(cloudServiceConfigContainer, gbc);

        // 测试按钮和帮助按钮容器 - 创建一个单独的容器来放置按钮
        JPanel buttonPanel = new JPanel(new GridBagLayout());
        GridBagConstraints buttonGbc = new GridBagConstraints();
        buttonGbc.insets = JBUI.insets(5, 10);
        buttonGbc.fill = GridBagConstraints.NONE;
        buttonGbc.anchor = GridBagConstraints.CENTER;

        // 第0列 - 空白（占位）
        buttonGbc.gridx = 0;
        buttonGbc.weightx = 1.0;
        buttonPanel.add(new JPanel(), buttonGbc);

        // 第1列 - 帮助按钮
        buttonGbc.gridx = 1;
        buttonGbc.weightx = 0;
        CloudEnum currentCloudType = OssState.getCloudType(cloudServiceComboBox.getSelectedIndex());
        helpButton = new JButton(MikBundle.message("panel.upload.service.help.button", currentCloudType.getTitle()));
        buttonPanel.add(helpButton, buttonGbc);

        // 第2列 - 测试按钮
        buttonGbc.gridx = 2;
        String testButtonText = MikBundle.message("panel.upload.service.test.button.text");
        testUploadButton = new JButton("<html><span style='color:red'>●</span> " + testButtonText + "</html>");
        testUploadButton.setToolTipText(MikBundle.message("panel.upload.service.test.button.tooltip"));
        buttonPanel.add(testUploadButton, buttonGbc);

        // 第3列 - 空白（占位）
        buttonGbc.gridx = 3;
        buttonGbc.weightx = 1.0;
        buttonPanel.add(new JBLabel(""), buttonGbc);

        // 将按钮容器添加到主面板
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 5; // 跨越所有列
        gbc.weightx = 1.0;
        gbc.weighty = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        content.add(buttonPanel, gbc);

        this.cloudServiceConboBoxListener();
        this.setAsDefaultCloudCheckBoxListener();
        // 初始化测试按钮和帮助按钮
        this.testAndHelpListener();

    }

    /**
     * 从 CloudEnum 枚举生成下拉框选项数组
     * <p>
     * 按照 CloudEnum 的 index 顺序生成标题数组，用于填充下拉框。
     * 确保选项顺序与枚举的 index 一致。
     *
     * @return 云服务商标题数组，按 index 顺序排列
     */
    @NotNull
    private String[] createCloudServiceOptions() {
        CloudEnum[] values = CloudEnum.values();
        String[] options = new String[values.length];
        // 按照 index 排序，确保顺序正确
        for (CloudEnum cloudEnum : values) {
            options[cloudEnum.getIndex()] = cloudEnum.getTitle();
        }
        return options;
    }

    /**
     * 为“设置为默认图床”复选框添加监听器
     * <p>
     * 当复选框状态发生变化时，根据其选中状态更新默认图床配置。
     * 若勾选，则将当前选中的图床服务设置为默认图床；
     * 若取消勾选，则恢复默认图床配置。
     *
     * @since 1.0
     */
    private void setAsDefaultCloudCheckBoxListener() {
        // 勾选后将 cloudServiceComboBox 选择的图床设置为默认图床
        setAsDefaultCloudCheckBox.addActionListener(e -> {
            if (setAsDefaultCloudCheckBox.isSelected()) {
                int cloudTypeIndex = this.cloudServiceComboBox.getSelectedIndex();
                MikPersistenComponent.getInstance().getState().setDefaultCloudType(cloudTypeIndex);
            } else {
                // 如果取消勾选，则将默认图床设置为默认图床
                MikPersistenComponent.getInstance().getState().setDefaultCloudType(MikState.DEFAULT_CLOUD.getIndex());
            }
        });
    }

    /**
     * 监听云服务下拉框的选项变化事件
     * <p>
     * 当用户选择不同的云服务时，动态更新图床配置面板和帮助按钮的文字内容
     */
    private void cloudServiceConboBoxListener() {
        this.cloudServiceComboBox.addActionListener(e -> {
            // 动态设置图床配置面板
            setCloudServiceConfigContainer(cloudServiceComboBox.getSelectedIndex());

            // 动态设置帮助按钮的文字
            int selectedIndex = this.cloudServiceComboBox.getSelectedIndex();
            CloudEnum cloudType = OssState.getCloudType(selectedIndex);
            this.helpButton.setText(MikBundle.message("panel.upload.service.help.button", cloudType.getTitle()));
        });
    }

    //region 图床 panel 初始化

    /**
     * 创建 Sm.ms 配置面板
     * <p>
     * 初始化并返回一个用于配置 Sm.ms 服务的面板，包含 API 地址和 Token 输入框。
     *
     * @return 配置面板对象
     */
    @NotNull
    private JPanel createSmmsConfigPanel() {
        smmsConfigPanel = new JPanel();
        smmsConfigPanel.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = JBUI.insets(2, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0;
        gbc.gridy = 0;
        smmsConfigPanel.add(new JBLabel(MikBundle.message("oss.field.api.url")), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        smmsUrlTextField = new JTextField();
        smmsConfigPanel.add(smmsUrlTextField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0;
        smmsConfigPanel.add(new JBLabel(MikBundle.message("oss.field.token")), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        smmsTokenTextField = new JPasswordField();
        smmsConfigPanel.add(smmsTokenTextField, gbc);

        return smmsConfigPanel;
    }

    /**
     * 创建阿里云 OSS 配置面板
     * <p>
     * 用于构建阿里云 OSS 的配置界面，包含存储桶名称、访问密钥、访问密钥密文、端点、文件目录等配置项。
     *
     * @return 阿里云 OSS 配置面板对象
     */
    @NotNull
    private JPanel createAliyunOssConfigPanel() {
        aliyunOssConfigPanel = new JPanel();
        aliyunOssConfigPanel.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = JBUI.insets(5, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0;
        gbc.gridy = 0;
        aliyunOssConfigPanel.add(new JBLabel(MikBundle.message("oss.field.bucket.name")), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        aliyunBucketNameTextField = new JTextField();
        aliyunOssConfigPanel.add(aliyunBucketNameTextField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0;
        aliyunOssConfigPanel.add(new JBLabel(MikBundle.message("oss.field.access.key")), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        aliyunAccessKeyTextField = new JTextField();
        aliyunOssConfigPanel.add(aliyunAccessKeyTextField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 0;
        aliyunOssConfigPanel.add(new JBLabel(MikBundle.message("oss.field.access.secret")), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        aliyunAccessSecretTextField = new JPasswordField();
        aliyunOssConfigPanel.add(aliyunAccessSecretTextField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.weightx = 0;
        aliyunOssConfigPanel.add(new JBLabel(MikBundle.message("oss.field.endpoint")), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        aliyunEndpointTextField = new JTextField();
        aliyunOssConfigPanel.add(aliyunEndpointTextField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.weightx = 0;
        aliyunOssConfigPanel.add(new JBLabel(MikBundle.message("oss.field.file.dir")), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        aliyunFileDirTextField = new JTextField();
        aliyunOssConfigPanel.add(aliyunFileDirTextField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.weightx = 0;
        aliyunCustomEndpointCheckBox = new JCheckBox(MikBundle.message("oss.field.custom.domain"));
        aliyunOssConfigPanel.add(aliyunCustomEndpointCheckBox, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        aliyunCustomEndpointTextField = new JTextField();
        aliyunCustomEndpointTextField.setEnabled(false);
        aliyunOssConfigPanel.add(aliyunCustomEndpointTextField, gbc);

        aliyunCustomEndpointCheckBox.addActionListener(e -> aliyunCustomEndpointTextField.setEnabled(aliyunCustomEndpointCheckBox.isSelected()));

        return aliyunOssConfigPanel;
    }

    /**
     * 创建百度云 BOS 配置面板
     * <p>
     * 用于构建百度云 BOS 的配置界面，包含存储桶名称、访问密钥、访问密钥密文、端点、文件目录以及自定义域名相关配置项。
     * 面板使用 GridBagLayout 布局，各配置项通过 GridBagConstraints 进行精确定位和排列。
     *
     * @return 百度云 BOS 配置面板
     */
    @NotNull
    private JPanel createBaiduBosConfigPanel() {
        baiduBosConfigPanel = new JPanel();
        baiduBosConfigPanel.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = JBUI.insets(5, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0;
        baiduBosConfigPanel.add(new JBLabel(MikBundle.message("oss.field.bucket.name")), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        baiduBosBucketNameTextField = new JTextField();
        baiduBosConfigPanel.add(baiduBosBucketNameTextField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0;
        baiduBosConfigPanel.add(new JBLabel(MikBundle.message("oss.field.access.key")), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        baiduBosAccessKeyTextField = new JTextField();
        baiduBosConfigPanel.add(baiduBosAccessKeyTextField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 0;
        baiduBosConfigPanel.add(new JBLabel(MikBundle.message("oss.field.access.secret")), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        baiduBosAccessSecretTextField = new JPasswordField();
        baiduBosConfigPanel.add(baiduBosAccessSecretTextField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.weightx = 0;
        baiduBosConfigPanel.add(new JBLabel(MikBundle.message("oss.field.endpoint")), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        baiduBosEndpointTextField = new JTextField();
        baiduBosEndpointTextField.setText("cd.bcebos.com");
        baiduBosConfigPanel.add(baiduBosEndpointTextField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.weightx = 0;
        baiduBosConfigPanel.add(new JBLabel(MikBundle.message("oss.field.file.dir")), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        baiduBosFileDirTextField = new JTextField();
        baiduBosFileDirTextField.setToolTipText(MikBundle.message("oss.field.directory.optional"));
        baiduBosConfigPanel.add(baiduBosFileDirTextField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.weightx = 0;
        baiduBosCustomEndpointCheckBox = new JCheckBox(MikBundle.message("oss.field.custom.domain.colon"));
        baiduBosConfigPanel.add(baiduBosCustomEndpointCheckBox, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        baiduBosCustomEndpointTextField = new JTextField();
        baiduBosCustomEndpointTextField.setEnabled(false);
        baiduBosConfigPanel.add(baiduBosCustomEndpointTextField, gbc);

        baiduBosCustomEndpointCheckBox.addActionListener(e -> baiduBosCustomEndpointTextField.setEnabled(baiduBosCustomEndpointCheckBox.isSelected()));

        return baiduBosConfigPanel;
    }

    /**
     * 创建 GitHub 配置面板
     * <p>
     * 用于构建包含 GitHub 相关配置项的图形界面面板，包括仓库地址、分支、访问令牌、文件目录以及自定义域名端点选项。
     *
     * @return 创建的 GitHub 配置面板
     */
    @NotNull
    private JPanel createGithubConfigPanel() {
        githubConfigPanel = new JPanel();
        githubConfigPanel.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = JBUI.insets(5, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0;
        githubConfigPanel.add(new JBLabel(MikBundle.message("oss.field.repository")), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        githubReposTextField = new JTextField();
        githubReposTextField.setToolTipText(MikBundle.message("oss.field.repository.example"));
        githubConfigPanel.add(githubReposTextField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0;
        githubConfigPanel.add(new JBLabel(MikBundle.message("oss.field.branch")), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        githubBranchTextField = new JTextField();
        githubBranchTextField.setText("main");
        githubConfigPanel.add(githubBranchTextField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 0;
        githubConfigPanel.add(new JBLabel(MikBundle.message("oss.field.token")), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        githubTokenTextField = new JPasswordField();
        githubConfigPanel.add(githubTokenTextField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.weightx = 0;
        githubConfigPanel.add(new JBLabel(MikBundle.message("oss.field.file.dir")), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        githubFileDirTextField = new JTextField();
        githubFileDirTextField.setToolTipText(MikBundle.message("oss.field.directory.optional"));
        githubConfigPanel.add(githubFileDirTextField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.weightx = 0;
        githubCustomEndpointCheckBox = new JCheckBox(MikBundle.message("oss.field.custom.domain.colon"));
        githubConfigPanel.add(githubCustomEndpointCheckBox, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        githubCustomEndpointTextField = new JTextField();
        githubCustomEndpointTextField.setEnabled(false);
        githubConfigPanel.add(githubCustomEndpointTextField, gbc);

        githubCustomEndpointCheckBox.addActionListener(e -> githubCustomEndpointTextField.setEnabled(githubCustomEndpointCheckBox.isSelected()));

        return githubConfigPanel;
    }

    /**
     * 创建 Gitee 配置面板
     * <p>
     * 用于生成包含 Gitee 相关配置项的面板，包括仓库地址、分支、Token、文件目录以及自定义域名设置。
     *
     * @return 返回创建的 Gitee 配置面板对象
     */
    @NotNull
    private JPanel createGiteeConfigPanel() {
        giteeConfigPanel = new JPanel();
        giteeConfigPanel.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = JBUI.insets(5, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0;
        giteeConfigPanel.add(new JBLabel(MikBundle.message("oss.field.repository")), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        giteeReposTextField = new JTextField();
        giteeReposTextField.setToolTipText(MikBundle.message("oss.field.repository.example"));
        giteeConfigPanel.add(giteeReposTextField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0;
        giteeConfigPanel.add(new JBLabel(MikBundle.message("oss.field.branch")), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        giteeBranchTextField = new JTextField();
        giteeBranchTextField.setText("master");
        giteeConfigPanel.add(giteeBranchTextField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 0;
        giteeConfigPanel.add(new JBLabel(MikBundle.message("oss.field.token")), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        giteeTokenTextField = new JPasswordField();
        giteeConfigPanel.add(giteeTokenTextField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.weightx = 0;
        giteeConfigPanel.add(new JBLabel(MikBundle.message("oss.field.file.dir")), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        giteeFileDirTextField = new JTextField();
        giteeFileDirTextField.setToolTipText(MikBundle.message("oss.field.directory.optional"));
        giteeConfigPanel.add(giteeFileDirTextField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.weightx = 0;
        giteeCustomEndpointCheckBox = new JCheckBox(MikBundle.message("oss.field.custom.domain"));
        giteeConfigPanel.add(giteeCustomEndpointCheckBox, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        giteeCustomEndpointTextField = new JTextField();
        giteeCustomEndpointTextField.setEnabled(false);
        giteeConfigPanel.add(giteeCustomEndpointTextField, gbc);

        giteeCustomEndpointCheckBox.addActionListener(e -> giteeCustomEndpointTextField.setEnabled(giteeCustomEndpointCheckBox.isSelected()));

        return giteeConfigPanel;
    }

    /**
     * 创建七牛云 OSS 配置面板
     * <p>
     * 用于构建七牛云对象存储服务的配置界面，包含 Bucket 名称、Access Key、Access Secret、Domain 和区域选择等配置项。
     *
     * @return 七牛云 OSS 配置面板
     */
    @NotNull
    private JPanel createQiniuOssConfigPanel() {
        qiniuOssConfigPanel = new JPanel();
        qiniuOssConfigPanel.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = JBUI.insets(5, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Bucket 名称
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.weightx = 0;
        qiniuOssConfigPanel.add(new JBLabel(MikBundle.message("oss.field.bucket.name")), gbc);

        gbc.gridx = 1;
        gbc.gridwidth = 6;
        gbc.weightx = 1.0;
        qiniuOssBucketNameTextField = new JTextField();
        qiniuOssConfigPanel.add(qiniuOssBucketNameTextField, gbc);

        // Access Key
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.weightx = 0;
        qiniuOssConfigPanel.add(new JBLabel(MikBundle.message("oss.field.access.key")), gbc);

        gbc.gridx = 1;
        gbc.gridwidth = 6;
        gbc.weightx = 1.0;
        qiniuOssAccessKeyTextField = new JTextField();
        qiniuOssConfigPanel.add(qiniuOssAccessKeyTextField, gbc);

        // Access Secret
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 1;
        gbc.weightx = 0;
        qiniuOssConfigPanel.add(new JBLabel(MikBundle.message("oss.field.access.secret")), gbc);

        gbc.gridx = 1;
        gbc.gridwidth = 6;
        gbc.weightx = 1.0;
        qiniuOssAccessSecretTextField = new JPasswordField();
        qiniuOssConfigPanel.add(qiniuOssAccessSecretTextField, gbc);

        // Domain
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 1;
        gbc.weightx = 0;
        qiniuOssConfigPanel.add(new JBLabel(MikBundle.message("oss.field.domain")), gbc);

        gbc.gridx = 1;
        gbc.gridwidth = 6;
        gbc.weightx = 1.0;
        qiniuOssDomainTextField = new JTextField();
        qiniuOssConfigPanel.add(qiniuOssDomainTextField, gbc);

        // 区域选择 - 4个单选按钮在同一行
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 1;
        gbc.weightx = 0;
        qiniuOssConfigPanel.add(new JBLabel(MikBundle.message("oss.field.region")), gbc);

        ButtonGroup zoneGroup = new ButtonGroup();

        // 华东
        gbc.gridx = 1;
        gbc.gridwidth = 1;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        qiniuOssEastChinaRadioButton = new JRadioButton(MikBundle.message("oss.qiniu.region.east.china"));
        zoneGroup.add(qiniuOssEastChinaRadioButton);
        qiniuOssConfigPanel.add(qiniuOssEastChinaRadioButton, gbc);

        // 华北
        gbc.gridx = 2;
        gbc.gridwidth = 1;
        gbc.weightx = 0;
        qiniuOssNortChinaRadioButton = new JRadioButton(MikBundle.message("oss.qiniu.region.north.china"));
        zoneGroup.add(qiniuOssNortChinaRadioButton);
        qiniuOssConfigPanel.add(qiniuOssNortChinaRadioButton, gbc);

        // 华南
        gbc.gridx = 3;
        gbc.gridwidth = 1;
        gbc.weightx = 0;
        qiniuOssSouthChinaRadioButton = new JRadioButton(MikBundle.message("oss.qiniu.region.south.china"));
        zoneGroup.add(qiniuOssSouthChinaRadioButton);
        qiniuOssConfigPanel.add(qiniuOssSouthChinaRadioButton, gbc);

        // 北美
        gbc.gridx = 4;
        gbc.gridwidth = 1;
        gbc.weightx = 0;
        qiniuOssNorthAmeriaRadioButton = new JRadioButton(MikBundle.message("oss.qiniu.region.north.america"));
        zoneGroup.add(qiniuOssNorthAmeriaRadioButton);
        qiniuOssConfigPanel.add(qiniuOssNorthAmeriaRadioButton, gbc);

        gbc.gridx = 5;
        gbc.gridwidth = 1;
        gbc.weightx = 0;
        qiniuOssSoutheastAsiaRadioButton = new JRadioButton(MikBundle.message("oss.qiniu.region.southeast.asia"));
        zoneGroup.add(qiniuOssSoutheastAsiaRadioButton);
        qiniuOssConfigPanel.add(qiniuOssSoutheastAsiaRadioButton, gbc);

        // 恢复 fill 设置
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // 隐藏的区域索引字段（用于存储区域索引值）
        qiniuZoneIndexTextField = new JTextField();
        qiniuZoneIndexTextField.setVisible(false);

        return qiniuOssConfigPanel;
    }

    /**
     * 创建腾讯云 COS 配置面板
     * <p>
     * 用于生成腾讯云对象存储服务（COS）的配置界面，包含存储桶名称、访问密钥、密钥和区域等输入字段。
     *
     * @return 腾讯云 COS 配置面板
     */
    @NotNull
    private JPanel createTencentOssConfigPanel() {
        tencentOssConfigPanel = new JPanel();
        tencentOssConfigPanel.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = JBUI.insets(5, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0;
        tencentOssConfigPanel.add(new JBLabel(MikBundle.message("oss.field.bucket.name")), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        tencentBucketNameTextField = new JTextField();
        tencentOssConfigPanel.add(tencentBucketNameTextField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0;
        tencentOssConfigPanel.add(new JBLabel(MikBundle.message("oss.field.access.key")), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        tencentAccessKeyTextField = new JTextField();
        tencentOssConfigPanel.add(tencentAccessKeyTextField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 0;
        tencentOssConfigPanel.add(new JBLabel(MikBundle.message("oss.field.secret.key")), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        tencentSecretKeyTextField = new JPasswordField();
        tencentOssConfigPanel.add(tencentSecretKeyTextField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.weightx = 0;
        tencentOssConfigPanel.add(new JBLabel(MikBundle.message("oss.field.region.name")), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        tencentRegionNameTextField = new JTextField();
        tencentRegionNameTextField.setText("ap-chengdu");
        tencentOssConfigPanel.add(tencentRegionNameTextField, gbc);

        return tencentOssConfigPanel;
    }

    /**
     * 创建自定义 OSS 配置面板
     * <p>
     * 用于生成包含 OSS 配置相关输入字段的面板，包括 API 地址、请求密钥、响应 URL 路径和 HTTP 方法等。
     *
     * @return 自定义 OSS 配置面板
     */
    @NotNull
    private JPanel createCustomOssConfigPanel() {
        customOssConfigPanel = new JPanel();
        customOssConfigPanel.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = JBUI.insets(5, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0;
        customOssConfigPanel.add(new JBLabel(MikBundle.message("oss.field.api.address")), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        customApiTextField = new JTextField();
        customOssConfigPanel.add(customApiTextField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0;
        customOssConfigPanel.add(new JBLabel(MikBundle.message("oss.field.request.key")), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        customRequestKeyTextField = new JTextField();
        customOssConfigPanel.add(customRequestKeyTextField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 0;
        customOssConfigPanel.add(new JBLabel(MikBundle.message("oss.field.response.url.path")), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        customResponseUrlPathTextField = new JTextField();
        customOssConfigPanel.add(customResponseUrlPathTextField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.weightx = 0;
        customOssConfigPanel.add(new JBLabel(MikBundle.message("oss.field.http.method")), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        customHttpMethodTextField = new JTextField();
        customHttpMethodTextField.setText("POST");
        customOssConfigPanel.add(customHttpMethodTextField, gbc);

        return customOssConfigPanel;
    }

    /**
     * 创建 PicList 配置面板，用于展示和编辑 PicList 相关配置信息
     * <p>
     * 该方法初始化一个 JPanel，使用 GridBagLayout 布局，并添加多个标签和文本框控件，用于配置 PicList 的 API 地址、图片床类型、配置文件名、API 密钥以及 CLI 路径。
     *
     * @return 创建好的 PicList 配置面板
     */
    @NotNull
    private JPanel createPicListConfigPanel() {
        picListConfigPanel = new JPanel();
        picListConfigPanel.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = JBUI.insets(5, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0;
        picListConfigPanel.add(new JBLabel(MikBundle.message("oss.field.api.address")), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        picListApiTextField = new JTextField();
        picListApiTextField.setToolTipText(MikBundle.message("oss.field.complete.api.address"));
        picListConfigPanel.add(picListApiTextField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0;
        picListConfigPanel.add(new JBLabel(MikBundle.message("oss.field.picbed.type")), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        picListPicbedTextField = new JTextField();
        picListPicbedTextField.setToolTipText(MikBundle.message("oss.field.picbed.type.example"));
        picListConfigPanel.add(picListPicbedTextField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 0;
        picListConfigPanel.add(new JBLabel(MikBundle.message("oss.field.config.name")), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        picListConfigNameTextField = new JTextField();
        picListConfigNameTextField.setToolTipText(MikBundle.message("oss.field.config.file.name"));
        picListConfigPanel.add(picListConfigNameTextField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.weightx = 0;
        picListConfigPanel.add(new JBLabel(MikBundle.message("oss.field.api.key")), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        picListKeyTextField = new JTextField();
        picListKeyTextField.setToolTipText(MikBundle.message("oss.field.api.key.auth"));
        picListConfigPanel.add(picListKeyTextField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.weightx = 0;
        picListConfigPanel.add(new JBLabel(MikBundle.message("oss.field.cli.path")), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        picListExeTextField = new TextFieldWithBrowseButton();
        picListExeTextField.setToolTipText(MikBundle.message("oss.field.select.cli.file"));

        // 初始化文件选择器（参考老页面的实现）
        initPicListExeBrowser();

        picListConfigPanel.add(picListExeTextField, gbc);

        return picListConfigPanel;
    }
    //endregion

    /**
     * 初始化 PicList 可执行文件选择器
     * <p>
     * 用于配置文件选择器，允许用户选择可执行文件或特定格式的目录，如 .exe、.app 或 .appimage 文件。
     */
    private void initPicListExeBrowser() {
        if (this.picListExeTextField == null) {
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
        });

        // 设置标题和描述
        String title = MikBundle.message("panel.upload.service.file.chooser.title");
        String description = MikBundle.message("panel.upload.service.file.chooser.description");
        descriptor.withTitle(title).withDescription(description);

        // 兼容新旧 API：先尝试新 API（2024.2+），失败则回退到旧 API（2022.3-2024.1）
        addBrowseFolderListenerCompat(
            this.picListExeTextField,
            title,
            description,
            descriptor,
            TextComponentAccessor.TEXT_FIELD_WHOLE_TEXT
                                     );
    }

    /**
     * 兼容新旧 API 的 addBrowseFolderListener 方法
     * <p>
     * 该方法使用反射自动检测当前 IntelliJ Platform 版本，优先使用新 API（2024.2+），
     * 如果新 API 不可用则回退到旧 API（2022.3-2024.1）。
     * <p>
     * 新 API: addBrowseFolderListener(Project, FileChooserDescriptor, TextComponentAccessor)
     * 旧 API: addBrowseFolderListener(String, String, Project, FileChooserDescriptor, TextComponentAccessor)
     * <p>
     * 使用反射可以避免编译时的版本兼容性问题。
     *
     * @param textField   文本字段组件
     * @param title       对话框标题
     * @param description 对话框描述
     * @param descriptor  文件选择描述符
     * @param accessor    文本组件访问器
     */
    private void addBrowseFolderListenerCompat(
        @NotNull TextFieldWithBrowseButton textField,
        @NotNull String title,
        @NotNull String description,
        @NotNull FileChooserDescriptor descriptor,
        @NotNull TextComponentAccessor<? super JTextField> accessor
                                              ) {
        try {
            // 尝试使用新 API（2024.2+）
            // 新 API: addBrowseFolderListener(Project, FileChooserDescriptor, TextComponentAccessor)
            Method newMethod = textField.getClass().getMethod(
                "addBrowseFolderListener",
                Project.class,
                FileChooserDescriptor.class,
                TextComponentAccessor.class
                                                             );
            newMethod.invoke(textField, null, descriptor, accessor);
            log.debug("Using new API for addBrowseFolderListener (2024.2+)");
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            // 如果新 API 不可用或调用失败，回退到旧 API（2022.3-2024.1）
            log.debug("New API not available or failed, falling back to old API", e);
            fallbackToOldBrowseFolderListenerAPI(textField, title, description, descriptor, accessor);
        }
    }

    /**
     * 回退到旧版本的 addBrowseFolderListener API
     * <p>
     * 旧 API: addBrowseFolderListener(String, String, Project, FileChooserDescriptor, TextComponentAccessor)
     *
     * @param textField   文本字段组件
     * @param title       对话框标题
     * @param description 对话框描述
     * @param descriptor  文件选择描述符
     * @param accessor    文本组件访问器
     */
    private void fallbackToOldBrowseFolderListenerAPI(
        @NotNull TextFieldWithBrowseButton textField,
        @NotNull String title,
        @NotNull String description,
        @NotNull FileChooserDescriptor descriptor,
        @NotNull TextComponentAccessor<? super JTextField> accessor
                                                     ) {
        try {
            // 旧 API: addBrowseFolderListener(String, String, Project, FileChooserDescriptor, TextComponentAccessor)
            Method oldMethod = textField.getClass().getMethod(
                "addBrowseFolderListener",
                String.class,
                String.class,
                Project.class,
                FileChooserDescriptor.class,
                TextComponentAccessor.class
                                                             );
            oldMethod.invoke(textField, title, description, null, descriptor, accessor);
            log.debug("Using old API for addBrowseFolderListener (2022.3-2024.1)");
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException ex) {
            log.debug("Failed to add browse folder listener using both new and old API", ex);
        }
    }

    /**
     * 根据索引获取对应的配置面板
     * <p>
     * 根据传入的索引值获取对应的云服务商配置面板，若对应面板尚未创建，则先创建再返回。
     *
     * @param index 服务商索引，用于确定需要返回的配置面板类型
     * @return 对应服务商的配置面板
     */
    @NotNull
    private JPanel getConfigPanelForIndex(int index) {
        final CloudEnum cloudEnum = CloudEnum.of(index);
        return switch (cloudEnum) {
            case SM_MS_CLOUD -> {
                if (this.smmsConfigPanel == null) {
                    yield createSmmsConfigPanel();
                }
                yield this.smmsConfigPanel;
            }
            case ALIYUN_CLOUD -> {
                if (this.aliyunOssConfigPanel == null) {
                    yield createAliyunOssConfigPanel();
                }
                yield this.aliyunOssConfigPanel;
            }
            case QINIU_CLOUD -> {
                if (this.qiniuOssConfigPanel == null) {
                    yield createQiniuOssConfigPanel();
                }
                yield this.qiniuOssConfigPanel;
            }
            case TENCENT_CLOUD -> {
                if (this.tencentOssConfigPanel == null) {
                    yield createTencentOssConfigPanel();
                }
                yield this.tencentOssConfigPanel;
            }
            case BAIDU_CLOUD -> {
                if (this.baiduBosConfigPanel == null) {
                    yield createBaiduBosConfigPanel();
                }
                yield this.baiduBosConfigPanel;
            }
            case GITHUB -> {
                if (this.githubConfigPanel == null) {
                    yield createGithubConfigPanel();
                }
                yield this.githubConfigPanel;
            }
            case GITEE -> {
                if (this.giteeConfigPanel == null) {
                    yield createGiteeConfigPanel();
                }
                yield this.giteeConfigPanel;
            }
            case CUSTOMIZE -> {
                if (this.customOssConfigPanel == null) {
                    yield createCustomOssConfigPanel();
                }
                yield this.customOssConfigPanel;
            }
            case PICLIST -> {
                if (this.picListConfigPanel == null) {
                    yield createPicListConfigPanel();
                }
                yield this.picListConfigPanel;
            }
        };
    }

    /**
     * 初始化上传服务设定面板
     * <p>
     * 根据当前状态对象初始化上传服务相关的配置项，包括设置默认图床服务商和对应的配置面板。
     *
     * @param state 当前状态对象，用于获取默认图床服务商和配置信息
     */
    public void initUploadServicePanel(@NotNull MikState state) {
        // 从持久化配置中获取选中的图床服务商
        int currentCloudIndex = state.getDefaultCloudType();

        // 默认图床设置
        this.setAsDefaultCloudCheckBox.setSelected(state.isDefaultCloudCheck());
        this.cloudServiceComboBox.setSelectedIndex(currentCloudIndex);

        // 手动触发切换以显示对应的配置面板
        if (this.cloudServiceConfigContainer != null) {
            setCloudServiceConfigContainer(currentCloudIndex);
        }

        resetOssState(state);
    }

    /**
     * 重置状态相关的 UI 显示内容
     * <p>
     * 根据传入的云服务器索引，更新状态提示信息和测试上传按钮的显示文本。
     *
     * @param currentCloudIndex 当前选择的云服务器索引
     */
    private void resetAvailableStatus(int currentCloudIndex) {
        // 加载设置页面时从配置中读取配置并设置 cloudServerAvailableMessage 的 text
        if (this.cloudServerAvailableMessage != null) {
            this.cloudServerAvailableMessage.setText(
                OssState.getStatus(currentCloudIndex)
                ? MikBundle.message("oss.available")
                : MikBundle.message("oss.not.available"));
        }
        String color = OssState.getStatus(currentCloudIndex) ? "green" : "red";
        String testButtonText = MikBundle.message("panel.upload.service.test.button.text");
        this.testUploadButton.setText("<html><span style='color:" + color + "'>●</span> " + testButtonText + "</html>");
    }

    /**
     * 设置指定云服务索引对应的配置容器内容
     * <p>
     * 清除当前配置容器中的所有组件，根据传入的云服务索引创建新的配置面板并添加到容器中。
     * 同时重新验证和重绘容器。接着更新状态中的云服务类型，并初始化对应的OSS设置。
     * 最后重置该云服务索引的可用状态。
     *
     * @param cloudIndex 云服务索引
     */
    private void setCloudServiceConfigContainer(int cloudIndex) {
        // 获取图床设置面板, 如果未创建则创建
        JPanel configPanel = getConfigPanelForIndex(cloudIndex);
        final MikState state = MikPersistenComponent.getInstance().getState();
        initOssSettings(cloudIndex, state);
        resetAvailableStatus(cloudIndex);

        this.cloudServiceConfigContainer.removeAll();
        this.cloudServiceConfigContainer.add(configPanel, BorderLayout.CENTER);
        this.cloudServiceConfigContainer.revalidate();
        this.cloudServiceConfigContainer.repaint();

        // 如果默认图床和当前选择的图床不一致, 取消勾选 设置默认
        this.setAsDefaultCloudCheckBox.setSelected(state.getDefaultCloudType() == cloudIndex);
    }

    /**
     * 判断上传服务设定是否已修改
     * <p>
     * 比较当前上传服务的状态与传入的状态对象，判断是否发生修改。
     *
     * @param state 要比较的状态对象
     * @return 如果当前状态与给定状态一致，返回 true；否则返回 false
     */
    public boolean isUploadServiceModified(@NotNull MikState state) {
        // 检查 OSS 配置设定是否修改
        return isOssStateModified(state);
    }

    /**
     * 将应用上传服务的配置应用到状态对象中
     * <p>
     * 通过指定的状态对象，将上传服务的相关配置设置到状态中，用于后续操作使用。
     *
     * @param state 状态对象，用于承载上传服务的配置信息
     */
    public void applyUploadServiceConfigs(@NotNull MikState state) {
        // 保存当前选中的服务商的配置
        applyOssState(state);
    }

    /**
     * 初始化各个 OSS 设置对象
     * <p>
     * 根据传入的云服务商索引，创建对应的 OSS 设置对象，并调用 init 方法进行初始化配置。
     * 该方法仅在初始化时调用一次，用于创建默认选中的图床设置对象。
     *
     * @param cloudIndex 云服务商的索引，用于确定具体要初始化的 OSS 服务类型
     * @param state      当前状态对象，用于获取对应 OSS 服务的配置状态
     */
    public void initOssSettings(int cloudIndex, MikState state) {
        // 初始化默认选中的图床 Setting 对象
        CloudEnum cloudEnum = CloudEnum.of(cloudIndex);
        switch (cloudEnum) {
            case SM_MS_CLOUD:
                if (this.smmsOssSetting == null && this.smmsUrlTextField != null) {
                    this.smmsOssSetting = new SmmsOssSetting(this.smmsUrlTextField, this.smmsTokenTextField);
                    this.smmsOssSetting.init(state.getSmmsOssState());
                }
                break;
            case ALIYUN_CLOUD:
                if (this.aliyunOssSetting == null && this.aliyunBucketNameTextField != null) {
                    this.aliyunOssSetting = new AliyunOssSetting(
                        this.aliyunBucketNameTextField, this.aliyunAccessKeyTextField,
                        this.aliyunAccessSecretTextField, this.aliyunEndpointTextField,
                        this.aliyunFileDirTextField, this.aliyunCustomEndpointCheckBox,
                        this.aliyunCustomEndpointTextField, null);
                    this.aliyunOssSetting.init(state.getAliyunOssState());
                }
                break;
            case BAIDU_CLOUD:
                if (this.baiduBosSetting == null && this.baiduBosBucketNameTextField != null) {
                    this.baiduBosSetting = new BaiduBosSetting(
                        this.baiduBosBucketNameTextField, this.baiduBosAccessKeyTextField,
                        this.baiduBosAccessSecretTextField, this.baiduBosEndpointTextField,
                        this.baiduBosFileDirTextField, this.baiduBosCustomEndpointCheckBox,
                        this.baiduBosCustomEndpointTextField, null);
                    this.baiduBosSetting.init(state.getBaiduBosState());
                }
                break;
            case GITHUB:
                if (this.githubSetting == null && this.githubReposTextField != null) {
                    this.githubSetting = new GithubSetting(
                        this.githubReposTextField, this.githubBranchTextField,
                        this.githubTokenTextField, this.githubFileDirTextField,
                        this.githubCustomEndpointCheckBox, this.githubCustomEndpointTextField,
                        null);
                    this.githubSetting.init(state.getGithubOssState());
                }
                break;
            case GITEE:
                if (this.giteeSetting == null && this.giteeReposTextField != null) {
                    this.giteeSetting = new GiteeSetting(
                        this.giteeReposTextField, this.giteeBranchTextField,
                        this.giteeTokenTextField, this.giteeFileDirTextField,
                        this.giteeCustomEndpointCheckBox, this.giteeCustomEndpointTextField,
                        null);
                    this.giteeSetting.init(state.getGiteeOssState());
                }
                break;
            case QINIU_CLOUD:
                if (this.qiniuOssSetting == null && this.qiniuOssBucketNameTextField != null) {
                    this.qiniuOssSetting = new QiniuOssSetting(
                        this.qiniuOssBucketNameTextField, this.qiniuOssAccessKeyTextField,
                        this.qiniuOssAccessSecretTextField, this.qiniuOssDomainTextField,
                        this.qiniuOssEastChinaRadioButton, this.qiniuOssNortChinaRadioButton,
                        this.qiniuOssSouthChinaRadioButton, this.qiniuOssNorthAmeriaRadioButton, this.qiniuOssSoutheastAsiaRadioButton,
                        this.qiniuZoneIndexTextField);
                    this.qiniuOssSetting.init(state.getQiniuOssState());
                }
                break;
            case TENCENT_CLOUD:
                if (this.tencentOssSetting == null && this.tencentBucketNameTextField != null) {
                    this.tencentOssSetting = new TencentOssSetting(
                        this.tencentBucketNameTextField, this.tencentAccessKeyTextField,
                        this.tencentSecretKeyTextField, this.tencentRegionNameTextField);
                    this.tencentOssSetting.init(state.getTencentOssState());
                }
                break;
            case CUSTOMIZE:
                if (this.customOssSetting == null && this.customApiTextField != null) {
                    this.customOssSetting = new CustomOssSetting(
                        this.customApiTextField, this.customRequestKeyTextField,
                        this.customResponseUrlPathTextField, this.customHttpMethodTextField);
                    this.customOssSetting.init(state.getCustomOssState());
                }
                break;
            case PICLIST:
                if (this.picListOssSetting == null && this.picListApiTextField != null) {
                    this.picListOssSetting = new PicListOssSetting(
                        this.picListApiTextField, this.picListPicbedTextField,
                        this.picListConfigNameTextField, this.picListKeyTextField,
                        this.picListExeTextField);
                    this.picListOssSetting.init(state.getPicListOssState());
                }
                break;
            default:
                break;
        }
    }

    /**
     * 应用OSS状态信息
     * <p>
     * 根据传入的MikState对象和默认云类型，执行OSS状态相关的处理逻辑
     */
    private void applyOssState(MikState state) {
        final int cloudType = state.getDefaultCloudType();
        applyOssState(state, cloudType);
    }

    /**
     * 根据云类型应用对应的OSS状态配置
     * <p>
     * 根据传入的云类型枚举值，调用对应OSS配置对象的apply方法，传入相应的状态对象。
     *
     * @param state     包含各云平台OSS状态的对象
     * @param cloudType 云类型标识，用于确定使用哪个OSS配置
     */
    private void applyOssState(MikState state, int cloudType) {

        final CloudEnum cloudEnum = CloudEnum.of(cloudType);
        switch (cloudEnum) {
            case SM_MS_CLOUD -> this.smmsOssSetting.apply(state.getSmmsOssState());
            case ALIYUN_CLOUD -> this.aliyunOssSetting.apply(state.getAliyunOssState());
            case QINIU_CLOUD -> this.qiniuOssSetting.apply(state.getQiniuOssState());
            case TENCENT_CLOUD -> this.tencentOssSetting.apply(state.getTencentOssState());
            case BAIDU_CLOUD -> this.baiduBosSetting.apply(state.getBaiduBosState());
            case GITHUB -> this.githubSetting.apply(state.getGithubOssState());
            case GITEE -> this.giteeSetting.apply(state.getGiteeOssState());
            case CUSTOMIZE -> this.customOssSetting.apply(state.getCustomOssState());
            case PICLIST -> this.picListOssSetting.apply(state.getPicListOssState());
        }
    }

    /**
     * 重置所有 OSS 配置状态
     * <p>
     * 根据传入的 MikState 对象，重置各个 OSS 配置的状态。
     * 依次调用每个 OSS 配置对象的 reset 方法，传入对应的状态值。
     *
     * @param state 包含各个 OSS 配置状态的 MikState 对象
     */
    public void resetOssState(MikState state) {

        // 重置各个 OSS 配置
        if (this.smmsOssSetting != null) {
            this.smmsOssSetting.reset(state.getSmmsOssState());
        }
        if (this.aliyunOssSetting != null) {
            this.aliyunOssSetting.reset(state.getAliyunOssState());
        }
        if (this.baiduBosSetting != null) {
            this.baiduBosSetting.reset(state.getBaiduBosState());
        }
        if (this.githubSetting != null) {
            this.githubSetting.reset(state.getGithubOssState());
        }
        if (this.giteeSetting != null) {
            this.giteeSetting.reset(state.getGiteeOssState());
        }
        if (this.qiniuOssSetting != null) {
            this.qiniuOssSetting.reset(state.getQiniuOssState());
        }
        if (this.tencentOssSetting != null) {
            this.tencentOssSetting.reset(state.getTencentOssState());
        }
        if (this.customOssSetting != null) {
            this.customOssSetting.reset(state.getCustomOssState());
        }
        if (this.picListOssSetting != null) {
            this.picListOssSetting.reset(state.getPicListOssState());
        }
    }

    /**
     * 判断 OSS 状态是否发生修改
     * <p>
     * 遍历所有 OSS 配置，检查每个配置是否在给定的 MikState 中标记为已修改。只要有一个配置返回 true，整个方法即返回 true。
     * 使用短路逻辑，一旦发现已修改的配置，立即返回，提高效率。
     *
     * @param state 包含各个 OSS 状态信息的对象
     * @return 如果有任何 OSS 配置状态发生修改，返回 true；否则返回 false
     */
    public boolean isOssStateModified(MikState state) {
        // 检查各个 OSS 配置是否修改
        // 只要有一个 OSS 配置返回 true（已修改），整个方法就返回 true
        // 使用短路逻辑，找到第一个已修改的配置就立即返回

        if (this.smmsOssSetting != null && this.smmsOssSetting.isModified(state.getSmmsOssState())) {
            return true;
        }
        if (this.aliyunOssSetting != null && this.aliyunOssSetting.isModified(state.getAliyunOssState())) {
            return true;
        }
        if (this.baiduBosSetting != null && this.baiduBosSetting.isModified(state.getBaiduBosState())) {
            return true;
        }
        if (this.githubSetting != null && this.githubSetting.isModified(state.getGithubOssState())) {
            return true;
        }
        if (this.giteeSetting != null && this.giteeSetting.isModified(state.getGiteeOssState())) {
            return true;
        }
        if (this.qiniuOssSetting != null && this.qiniuOssSetting.isModified(state.getQiniuOssState())) {
            return true;
        }
        if (this.tencentOssSetting != null && this.tencentOssSetting.isModified(state.getTencentOssState())) {
            return true;
        }
        if (this.customOssSetting != null && this.customOssSetting.isModified(state.getCustomOssState())) {
            return true;
        }
        return this.picListOssSetting != null && this.picListOssSetting.isModified(state.getPicListOssState());
    }

    /**
     * 为 "Test" 和 "Help" 按钮添加点击事件监听器，用于执行测试上传或打开帮助页面
     * <p>
     * 该方法为 "Test" 按钮绑定点击事件，用于选择对应的图床并上传测试文件，显示上传结果；同时为 "Help" 按钮绑定点击事件，用于打开对应图床的帮助页面。
     * <p>
     * EDT 处理说明：
     * 按钮点击事件在 EDT 上执行，但 ClientUtils.getClient() 和 client.upload() 中调用密码管理器获取密码是耗时操作，
     * 如果在 EDT 上执行会导致 UI 冻结。因此使用 ApplicationManager.getApplication().executeOnPooledThread()
     * 将耗时操作放到后台线程池执行，所有 UI 更新操作（如显示对话框）使用 SwingUtilities.invokeLater() 切回 EDT 执行。
     * SwingUtilities.invokeLater() 不受模态状态影响，可以在模态对话框中立即执行，而 ApplicationManager.invokeLater()
     * 在模态对话框中可能被延迟到对话框关闭后才执行。
     * <p>
     * 执行流程：EDT (按钮点击) -> 后台线程池 (获取客户端、上传文件) -> EDT (显示结果对话框)
     */
    public void testAndHelpListener() {
        // "Test" 按钮点击事件处理
        this.testUploadButton.addActionListener(e -> {
            int index = this.cloudServiceComboBox.getSelectedIndex();
            CloudEnum cloudEnum = OssState.getCloudType(index);

            // 处理 EDT 问题, ClientUtils.getClient 因为初始化时会调用密码管理器获取密码, 这属于耗时操作, 而且在 upload 也有获取密码逻辑
            ApplicationManager.getApplication().executeOnPooledThread(() -> {
                try {
                    OssClient client = ClientUtils.getClient(cloudEnum);
                    if (client != null) {
                        String url;
                        MikState state = MikPersistenComponent.getInstance().getState();
                        // 主动保存当前配置, 后续测试逻辑可以从 state 获取最新配置
                        this.applyOssState(state, index);

                        InputStream inputStream = this.getClass().getResourceAsStream("/" + TEST_FILE_NAME);
                        url = client.upload(inputStream, TEST_FILE_NAME, state);
                        log.trace("测试按钮上传的图片返回结果: {}", url);

                        if (StringUtils.isNotBlank(url)) {
                            if (log.isTraceEnabled()) {
                                BrowserUtil.browse(url);
                            }
                        } else {
                            SwingUtilities.invokeLater(() -> {
                                Messages.showMessageDialog(this.content,
                                                           MikBundle.message("settings.upload.failed", cloudEnum.getTitle()),
                                                           "Error",
                                                           Messages.getErrorIcon());
                            });

                        }
                    } else {
                        SwingUtilities.invokeLater(() -> {
                            Messages.showMessageDialog(this.content,
                                                       MikBundle.message("settings.upload.not.available", cloudEnum.getTitle()),
                                                       "Error",
                                                       Messages.getErrorIcon());
                        });
                    }
                } catch (Exception exception) {
                    SwingUtilities.invokeLater(() -> {
                        Messages.showMessageDialog(this.content,
                                                   exception.getMessage(),
                                                   "Error",
                                                   Messages.getErrorIcon());
                    });
                } finally {
                    // 重置提示状态
                    resetAvailableStatus(index);
                }
            });
        });

        // help button 监听
        this.helpButton.addActionListener(e -> {
            CloudEnum cloudType = OssState.getCloudType(this.cloudServiceComboBox.getSelectedIndex());
            // https://mik.dong4j.site/api/mik/help/settings/aliyun_cloud
            String url = MikNotification.helpUrl(HelpType.SETTING.where + "/" + cloudType.name().toLowerCase());
            if (!url.equals(MikNotification.ABOUT_BLANK)) {
                BrowserUtil.browse(url);
            }
        });
    }

    /**
     * 设置面板所有组件的启用/禁用状态
     * <p>
     * 当全局开关改变时，联动控制所有子组件的可用状态
     *
     * @param enabled true 启用所有组件，false 禁用所有组件
     */
    public void setAllComponentsEnabled(boolean enabled) {
        cloudServiceComboBox.setEnabled(enabled);
        setAsDefaultCloudCheckBox.setEnabled(enabled);
        testUploadButton.setEnabled(enabled);
        helpButton.setEnabled(enabled);

        // 禁用当前显示的配置面板中的所有输入控件
        java.awt.Component[] components = cloudServiceConfigContainer.getComponents();
        for (java.awt.Component component : components) {
            setComponentEnabled(component, enabled);
        }
    }

    /**
     * 递归设置组件及其子组件的启用/禁用状态
     *
     * @param component 要设置的组件
     * @param enabled   true 启用，false 禁用
     */
    private void setComponentEnabled(java.awt.Component component, boolean enabled) {
        if (component instanceof java.awt.Container) {
            for (java.awt.Component child : ((java.awt.Container) component).getComponents()) {
                setComponentEnabled(child, enabled);
            }
        }
        component.setEnabled(enabled);
    }

}
