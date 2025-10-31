package info.dong4j.idea.plugin.settings.panel;

import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
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

import org.jetbrains.annotations.NotNull;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.io.InputStream;

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

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 *
 *
 * @author dong4j
 * @version 1.0.0
 * @email "mailto:dong4j@gmail.com"
 * @date 2025.10.31 22:48
 * @since x.x.x
 */
@Slf4j
@SuppressWarnings( {"D", "DuplicatedCode", "DialogTitleCapitalization"})
public class UploadServicePanel {
    /** 测试文件名，用于测试场景中的文件标识 */
    public static final String TEST_FILE_NAME = "mik.webp";

    // ========== 上传服务设定区域 ==========
    /**
     * 上传服务设定面板
     */
    @Getter
    private JPanel content;
    /**
     * 图床服务商下拉框
     * <p>
     * 【字段映射】对应老页面的 authorizationTabbedPanel (JTabbedPane)，新页面改为下拉框切换配置面板
     */
    private JComboBox<String> cloudServiceComboBox;
    /**
     * 云服务商配置容器
     * <p>
     * 【新功能】用于动态切换不同服务商的配置面板
     */
    private JPanel cloudServiceConfigContainer;
    /**
     * 设为默认图床复选框
     * <p>
     * 【字段映射】对应老页面的 defaultCloudCheckBox
     */
    private JCheckBox setAsDefaultCloudCheckBox;
    /**
     * 默认图床下拉框
     * <p>
     * 【字段映射】对应老页面的 defaultCloudComboBox
     */
    private JComboBox<String> defaultCloudComboBox;
    /** 自定义消息标签，用于显示用户自定义的提示信息 */
    private JLabel cloudServerAvailableMessage;
    /**
     * 验证图片上传按钮
     * <p>
     * 【字段映射】对应老页面的 testButton
     */
    private JButton testUploadButton;
    /**
     * 帮助按钮
     * <p>
     * 【字段映射】对应老页面的 helpButton
     */
    private JButton helpButton;

    // ========== 各个云服务商的配置面板 ==========
    //region Sm.ms
    /**
     * Sm.ms 配置面板
     */
    private JPanel smmsConfigPanel;
    /**
     * Sm.ms API URL 输入框
     * <p>
     * 【字段映射】对应老页面的 smmsUrlTextField
     */
    private JTextField smmsUrlTextField;
    /**
     * Sm.ms Token 输入框
     * <p>
     * 【字段映射】对应老页面的 smmsTokenTextField
     */
    private JPasswordField smmsTokenTextField;
    //endregion

    // region 阿里云 OSS
    /**
     * 阿里云 OSS 配置面板
     */
    private JPanel aliyunOssConfigPanel;
    /**
     * 阿里云 Bucket 名称输入框
     * <p>
     * 【字段映射】对应老页面的 aliyunOssBucketNameTextField
     */
    private JTextField aliyunBucketNameTextField;
    /**
     * 阿里云 Access Key 输入框
     * <p>
     * 【字段映射】对应老页面的 aliyunOssAccessKeyTextField
     */
    private JTextField aliyunAccessKeyTextField;
    /**
     * 阿里云 Access Secret 输入框
     * <p>
     * 【字段映射】对应老页面的 aliyunOssAccessSecretKeyTextField
     */
    private JPasswordField aliyunAccessSecretTextField;
    /**
     * 阿里云 Endpoint 输入框
     * <p>
     * 【字段映射】对应老页面的 aliyunOssEndpointTextField
     */
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
    /**
     * 阿里云自定义域名输入框
     * <p>
     * 【字段映射】对应老页面的 aliyunOssCustomEndpointTextField
     */
    private JTextField aliyunCustomEndpointTextField;
    //endregion

    //region 百度云BOS
    /**
     * 百度云 BOS 配置面板
     */
    private JPanel baiduBosConfigPanel;
    /**
     * 百度云 Bucket 名称输入框
     * <p>
     * 【字段映射】对应老页面的 baiduBosBucketNameTextField
     */
    private JTextField baiduBosBucketNameTextField;
    /**
     * 百度云 Access Key 输入框
     * <p>
     * 【字段映射】对应老页面的 baiduBosAccessKeyTextField
     */
    private JTextField baiduBosAccessKeyTextField;
    /**
     * 百度云 Access Secret 输入框
     * <p>
     * 【字段映射】对应老页面的 baiduBosAccessSecretKeyTextField
     */
    private JPasswordField baiduBosAccessSecretTextField;
    /**
     * 百度云 Endpoint 输入框
     * <p>
     * 【字段映射】对应老页面的 baiduBosEndpointTextField
     */
    private JTextField baiduBosEndpointTextField;
    /**
     * 百度云文件目录输入框
     * <p>
     * 【字段映射】对应老页面的 baiduBosFileDirTextField
     */
    private JTextField baiduBosFileDirTextField;
    /**
     * 百度云自定义域名复选框
     * <p>
     * 【字段映射】对应老页面的 baiduBosCustomEndpointCheckBox
     */
    private JCheckBox baiduBosCustomEndpointCheckBox;
    /**
     * 百度云自定义域名输入框
     * <p>
     * 【字段映射】对应老页面的 baiduBosCustomEndpointTextField
     */
    private JTextField baiduBosCustomEndpointTextField;
    //endregion

    //region GitHub
    /**
     * GitHub 配置面板
     */
    private JPanel githubConfigPanel;
    /**
     * GitHub 仓库输入框
     * <p>
     * 【字段映射】对应老页面的 githubReposTextField
     */
    private JTextField githubReposTextField;
    /**
     * GitHub 分支输入框
     * <p>
     * 【字段映射】对应老页面的 githubBranchTextField
     */
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
    /**
     * Gitee 配置面板
     */
    private JPanel giteeConfigPanel;
    /**
     * Gitee 仓库输入框
     * <p>
     * 【字段映射】对应老页面的 giteeReposTextField
     */
    private JTextField giteeReposTextField;
    /**
     * Gitee 分支输入框
     * <p>
     * 【字段映射】对应老页面的 giteeBranchTextField
     */
    private JTextField giteeBranchTextField;
    /**
     * Gitee Token 输入框
     * <p>
     * 【字段映射】对应老页面的 giteeTokenTextField
     */
    private JPasswordField giteeTokenTextField;
    /**
     * Gitee 文件目录输入框
     * <p>
     * 【字段映射】对应老页面的 giteeFileDirTextField
     */
    private JTextField giteeFileDirTextField;
    /**
     * Gitee 自定义域名复选框
     * <p>
     * 【字段映射】对应老页面的 giteeCustomEndpointCheckBox
     */
    private JCheckBox giteeCustomEndpointCheckBox;
    /**
     * Gitee 自定义域名输入框
     * <p>
     * 【字段映射】对应老页面的 giteeCustomEndpointTextField
     */
    private JTextField giteeCustomEndpointTextField;
    //endregion

    //region 七牛云
    /**
     * 七牛云配置面板
     */
    private JPanel qiniuOssConfigPanel;
    /**
     * 七牛云 Bucket 名称输入框
     * <p>
     * 【字段映射】对应老页面的 qiniuOssBucketNameTextField
     */
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
    /**
     * 七牛云 Domain 输入框
     * <p>
     * 【字段映射】对应老页面的 qiniuOssUpHostTextField
     */
    private JTextField qiniuOssDomainTextField;
    /**
     * 七牛云区域 - 华东单选按钮
     * <p>
     * 【字段映射】对应老页面的 qiniuOssEastChinaRadioButton
     */
    private JRadioButton qiniuOssEastChinaRadioButton;
    /**
     * 七牛云区域 - 华北单选按钮
     * <p>
     * 【字段映射】对应老页面的 qiniuOssNortChinaRadioButton
     */
    private JRadioButton qiniuOssNortChinaRadioButton;
    /**
     * 七牛云区域 - 华南单选按钮
     * <p>
     * 【字段映射】对应老页面的 qiniuOssSouthChinaRadioButton
     */
    private JRadioButton qiniuOssSouthChinaRadioButton;
    /**
     * 七牛云区域 - 北美单选按钮
     * <p>
     * 【字段映射】对应老页面的 qiniuOssNorthAmeriaRadioButton
     */
    private JRadioButton qiniuOssNorthAmeriaRadioButton;
    /**
     * 七牛云区域索引隐藏字段
     * <p>
     * 【字段映射】对应老页面的 zoneIndexTextFiled
     */
    private JTextField qiniuZoneIndexTextField;
    //endregion

    //region 腾讯云 COS
    /**
     * 腾讯云 COS 配置面板
     */
    private JPanel tencentOssConfigPanel;
    /**
     * 腾讯云 Bucket 名称输入框
     * <p>
     * 【字段映射】对应老页面的 tencentBacketNameTextField
     */
    private JTextField tencentBucketNameTextField;
    /**
     * 腾讯云 Access Key 输入框
     * <p>
     * 【字段映射】对应老页面的 tencentAccessKeyTextField
     */
    private JTextField tencentAccessKeyTextField;
    /**
     * 腾讯云 Secret Key 输入框
     * <p>
     * 【字段映射】对应老页面的 tencentSecretKeyTextField
     */
    private JPasswordField tencentSecretKeyTextField;
    /**
     * 腾讯云 Region Name 输入框
     * <p>
     * 【字段映射】对应老页面的 tencentRegionNameTextField
     */
    private JTextField tencentRegionNameTextField;
    //endregion

    //region 自定义 OSS
    /**
     * 自定义 OSS 配置面板
     */
    private JPanel customOssConfigPanel;
    /**
     * 自定义 OSS API 输入框
     * <p>
     * 【字段映射】对应老页面的 customApiTextField
     */
    private JTextField customApiTextField;
    /**
     * 自定义 OSS 请求 Key 输入框
     * <p>
     * 【字段映射】对应老页面的 requestKeyTextField
     */
    private JTextField customRequestKeyTextField;
    /**
     * 自定义 OSS 响应 URL 路径输入框
     * <p>
     * 【字段映射】对应老页面的 responseUrlPathTextField
     */
    private JTextField customResponseUrlPathTextField;
    /**
     * 自定义 OSS HTTP 方法输入框
     * <p>
     * 【字段映射】对应老页面的 httpMethodTextField
     */
    private JTextField customHttpMethodTextField;
    //endregion

    //region PicList
    /**
     * PicList 配置面板
     */
    private JPanel picListConfigPanel;
    /**
     * PicList API 输入框
     * <p>
     * 【字段映射】对应老页面的 picListApiTextField
     */
    private JTextField picListApiTextField;
    /**
     * PicList 图床类型输入框
     * <p>
     * 【字段映射】对应老页面的 picListPicbedTextField
     */
    private JTextField picListPicbedTextField;
    /**
     * PicList 配置名称输入框
     * <p>
     * 【字段映射】对应老页面的 picListConfigNameTextField
     */
    private JTextField picListConfigNameTextField;
    /**
     * PicList 密钥输入框
     * <p>
     * 【字段映射】对应老页面的 picListKeyTextField
     */
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
    /** TencentOssSetting 对象 */
    private TencentOssSetting tencentOssSetting;
    /** 自定义 OSS 配置信息实例 */
    private CustomOssSetting customOssSetting;
    /** PicList 图床配置信息实例 */
    private PicListOssSetting picListOssSetting;

    public UploadServicePanel() {
        createUploadServicePanel();
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
     * 创建上传服务设定区域面板
     */
    private void createUploadServicePanel() {
        content = new JPanel();
        content.setLayout(new GridBagLayout());
        content.setBorder(BorderFactory.createTitledBorder("上传服务设定"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = JBUI.insets(5, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // 云服务商下拉选择 - 放在最前面，与配置面板中的标签和输入框对齐
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.weightx = 0;
        content.add(new JBLabel("图床服务商:"), gbc);

        gbc.gridx = 1;
        gbc.gridwidth = 1;
        gbc.weightx = 1.0; // 设置较大的权重，占比更大
        cloudServiceComboBox = new com.intellij.openapi.ui.ComboBox<>(createCloudServiceOptions());

        cloudServiceComboBox.addActionListener(e -> {
            setCloudServiceConfigContainer(cloudServiceComboBox.getSelectedIndex());
            // 切换云服务商时，重新显示红点指示器（表示新服务商还未测试）
            if (this.testUploadButton != null) {
                this.testUploadButton.setText("<html><span style='color:red'>●</span> 验证图片上传</html>");
            }
        });

        content.add(cloudServiceComboBox, gbc);

        // 默认图床复选框和下拉框 - 与选择服务商在同一行
        gbc.gridx = 2;
        gbc.gridwidth = 1;
        gbc.weightx = 0;
        setAsDefaultCloudCheckBox = new JCheckBox("设为默认");
        content.add(setAsDefaultCloudCheckBox, gbc);

        gbc.gridx = 3;
        gbc.gridwidth = 1;
        gbc.weightx = 1.0;
        defaultCloudComboBox = new com.intellij.openapi.ui.ComboBox<>(createCloudServiceOptions());
        defaultCloudComboBox.setEnabled(false);
        content.add(defaultCloudComboBox, gbc);

        setAsDefaultCloudCheckBox.addActionListener(e -> {
            defaultCloudComboBox.setEnabled(setAsDefaultCloudCheckBox.isSelected());
        });

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
        helpButton = new JButton("Help & " + OssState.getCloudType(cloudServiceComboBox.getSelectedIndex()).getTitle());
        buttonPanel.add(helpButton, buttonGbc);

        // 第2列 - 测试按钮
        buttonGbc.gridx = 2;
        testUploadButton = new JButton("<html><span style='color:red'>●</span> 验证图片上传</html>");
        testUploadButton.setToolTipText("发起上传测试请求: 切换服务商后需要验证配置是否有误");
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

        this.cloudServiceComboBox.addActionListener(e -> {
            // 获得指定索引的选项卡标签
            int selectedIndex = this.cloudServiceComboBox.getSelectedIndex();
            CloudEnum cloudType = OssState.getCloudType(selectedIndex);
            this.helpButton.setText("Help & " + cloudType.getTitle());
        });

        // 初始化测试按钮和帮助按钮
        this.testAndHelpListener();

    }

    /**
     * 创建 Sm.ms 配置面板
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
        smmsConfigPanel.add(new JBLabel("API URL:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        smmsUrlTextField = new JTextField();
        smmsConfigPanel.add(smmsUrlTextField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0;
        smmsConfigPanel.add(new JBLabel("Token:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        smmsTokenTextField = new JPasswordField();
        smmsConfigPanel.add(smmsTokenTextField, gbc);

        return smmsConfigPanel;
    }

    /**
     * 创建阿里云 OSS 配置面板
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
        aliyunOssConfigPanel.add(new JBLabel("Bucket 名称:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        aliyunBucketNameTextField = new JTextField();
        aliyunOssConfigPanel.add(aliyunBucketNameTextField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0;
        aliyunOssConfigPanel.add(new JBLabel("Access Key:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        aliyunAccessKeyTextField = new JTextField();
        aliyunOssConfigPanel.add(aliyunAccessKeyTextField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 0;
        aliyunOssConfigPanel.add(new JBLabel("Access Secret:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        aliyunAccessSecretTextField = new JPasswordField();
        aliyunOssConfigPanel.add(aliyunAccessSecretTextField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.weightx = 0;
        aliyunOssConfigPanel.add(new JBLabel("Endpoint:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        aliyunEndpointTextField = new JTextField();
        aliyunOssConfigPanel.add(aliyunEndpointTextField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.weightx = 0;
        aliyunOssConfigPanel.add(new JBLabel("文件目录:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        aliyunFileDirTextField = new JTextField();
        aliyunOssConfigPanel.add(aliyunFileDirTextField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.weightx = 0;
        aliyunCustomEndpointCheckBox = new JCheckBox("自定义域名");
        aliyunOssConfigPanel.add(aliyunCustomEndpointCheckBox, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        aliyunCustomEndpointTextField = new JTextField();
        aliyunCustomEndpointTextField.setEnabled(false);
        aliyunOssConfigPanel.add(aliyunCustomEndpointTextField, gbc);

        aliyunCustomEndpointCheckBox.addActionListener(e -> {
            aliyunCustomEndpointTextField.setEnabled(aliyunCustomEndpointCheckBox.isSelected());
        });

        return aliyunOssConfigPanel;
    }

    /**
     * 创建百度云 BOS 配置面板
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
        baiduBosConfigPanel.add(new JBLabel("Bucket 名称:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        baiduBosBucketNameTextField = new JTextField();
        baiduBosConfigPanel.add(baiduBosBucketNameTextField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0;
        baiduBosConfigPanel.add(new JBLabel("Access Key:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        baiduBosAccessKeyTextField = new JTextField();
        baiduBosConfigPanel.add(baiduBosAccessKeyTextField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 0;
        baiduBosConfigPanel.add(new JBLabel("Access Secret:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        baiduBosAccessSecretTextField = new JPasswordField();
        baiduBosConfigPanel.add(baiduBosAccessSecretTextField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.weightx = 0;
        baiduBosConfigPanel.add(new JBLabel("Endpoint:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        baiduBosEndpointTextField = new JTextField();
        baiduBosEndpointTextField.setText("cd.bcebos.com");
        baiduBosConfigPanel.add(baiduBosEndpointTextField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.weightx = 0;
        baiduBosConfigPanel.add(new JBLabel("文件目录:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        baiduBosFileDirTextField = new JTextField();
        baiduBosFileDirTextField.setToolTipText("目录名(可选)");
        baiduBosConfigPanel.add(baiduBosFileDirTextField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.weightx = 0;
        baiduBosCustomEndpointCheckBox = new JCheckBox("自定义域名:");
        baiduBosConfigPanel.add(baiduBosCustomEndpointCheckBox, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        baiduBosCustomEndpointTextField = new JTextField();
        baiduBosCustomEndpointTextField.setEnabled(false);
        baiduBosConfigPanel.add(baiduBosCustomEndpointTextField, gbc);

        baiduBosCustomEndpointCheckBox.addActionListener(e -> {
            baiduBosCustomEndpointTextField.setEnabled(baiduBosCustomEndpointCheckBox.isSelected());
        });

        return baiduBosConfigPanel;
    }

    /**
     * 创建 GitHub 配置面板
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
        githubConfigPanel.add(new JBLabel("仓库:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        githubReposTextField = new JTextField();
        githubReposTextField.setToolTipText("例如: username/repository");
        githubConfigPanel.add(githubReposTextField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0;
        githubConfigPanel.add(new JBLabel("分支:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        githubBranchTextField = new JTextField();
        githubBranchTextField.setText("main");
        githubConfigPanel.add(githubBranchTextField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 0;
        githubConfigPanel.add(new JBLabel("Token:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        githubTokenTextField = new JPasswordField();
        githubConfigPanel.add(githubTokenTextField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.weightx = 0;
        githubConfigPanel.add(new JBLabel("文件目录:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        githubFileDirTextField = new JTextField();
        githubFileDirTextField.setToolTipText("目录名(可选)");
        githubConfigPanel.add(githubFileDirTextField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.weightx = 0;
        githubCustomEndpointCheckBox = new JCheckBox("自定义域名:");
        githubConfigPanel.add(githubCustomEndpointCheckBox, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        githubCustomEndpointTextField = new JTextField();
        githubCustomEndpointTextField.setEnabled(false);
        githubConfigPanel.add(githubCustomEndpointTextField, gbc);

        githubCustomEndpointCheckBox.addActionListener(e -> {
            githubCustomEndpointTextField.setEnabled(githubCustomEndpointCheckBox.isSelected());
        });

        return githubConfigPanel;
    }

    /**
     * 创建 Gitee 配置面板
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
        giteeConfigPanel.add(new JBLabel("仓库:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        giteeReposTextField = new JTextField();
        giteeReposTextField.setToolTipText("例如: username/repository");
        giteeConfigPanel.add(giteeReposTextField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0;
        giteeConfigPanel.add(new JBLabel("分支:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        giteeBranchTextField = new JTextField();
        giteeBranchTextField.setText("master");
        giteeConfigPanel.add(giteeBranchTextField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 0;
        giteeConfigPanel.add(new JBLabel("Token:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        giteeTokenTextField = new JPasswordField();
        giteeConfigPanel.add(giteeTokenTextField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.weightx = 0;
        giteeConfigPanel.add(new JBLabel("文件目录:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        giteeFileDirTextField = new JTextField();
        giteeFileDirTextField.setToolTipText("目录名(可选)");
        giteeConfigPanel.add(giteeFileDirTextField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.weightx = 0;
        giteeCustomEndpointCheckBox = new JCheckBox("自定义域名");
        giteeConfigPanel.add(giteeCustomEndpointCheckBox, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        giteeCustomEndpointTextField = new JTextField();
        giteeCustomEndpointTextField.setEnabled(false);
        giteeConfigPanel.add(giteeCustomEndpointTextField, gbc);

        giteeCustomEndpointCheckBox.addActionListener(e -> {
            giteeCustomEndpointTextField.setEnabled(giteeCustomEndpointCheckBox.isSelected());
        });

        return giteeConfigPanel;
    }

    /**
     * 创建七牛云配置面板
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
        qiniuOssConfigPanel.add(new JBLabel("Bucket 名称:"), gbc);

        gbc.gridx = 1;
        gbc.gridwidth = 5;
        gbc.weightx = 1.0;
        qiniuOssBucketNameTextField = new JTextField();
        qiniuOssConfigPanel.add(qiniuOssBucketNameTextField, gbc);

        // Access Key
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.weightx = 0;
        qiniuOssConfigPanel.add(new JBLabel("Access Key:"), gbc);

        gbc.gridx = 1;
        gbc.gridwidth = 5;
        gbc.weightx = 1.0;
        qiniuOssAccessKeyTextField = new JTextField();
        qiniuOssConfigPanel.add(qiniuOssAccessKeyTextField, gbc);

        // Access Secret
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 1;
        gbc.weightx = 0;
        qiniuOssConfigPanel.add(new JBLabel("Access Secret:"), gbc);

        gbc.gridx = 1;
        gbc.gridwidth = 5;
        gbc.weightx = 1.0;
        qiniuOssAccessSecretTextField = new JPasswordField();
        qiniuOssConfigPanel.add(qiniuOssAccessSecretTextField, gbc);

        // Domain
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 1;
        gbc.weightx = 0;
        qiniuOssConfigPanel.add(new JBLabel("Domain:"), gbc);

        gbc.gridx = 1;
        gbc.gridwidth = 5;
        gbc.weightx = 1.0;
        qiniuOssDomainTextField = new JTextField();
        qiniuOssConfigPanel.add(qiniuOssDomainTextField, gbc);

        // 区域选择 - 4个单选按钮在同一行
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 1;
        gbc.weightx = 0;
        qiniuOssConfigPanel.add(new JBLabel("区域:"), gbc);

        ButtonGroup zoneGroup = new ButtonGroup();

        // 华东
        gbc.gridx = 1;
        gbc.gridwidth = 1;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        qiniuOssEastChinaRadioButton = new JRadioButton("EastChina");
        qiniuOssEastChinaRadioButton.setSelected(true);
        zoneGroup.add(qiniuOssEastChinaRadioButton);
        qiniuOssConfigPanel.add(qiniuOssEastChinaRadioButton, gbc);

        // 华北
        gbc.gridx = 2;
        gbc.gridwidth = 1;
        gbc.weightx = 0;
        qiniuOssNortChinaRadioButton = new JRadioButton("NortChina");
        zoneGroup.add(qiniuOssNortChinaRadioButton);
        qiniuOssConfigPanel.add(qiniuOssNortChinaRadioButton, gbc);

        // 华南
        gbc.gridx = 3;
        gbc.gridwidth = 1;
        gbc.weightx = 0;
        qiniuOssSouthChinaRadioButton = new JRadioButton("SouthChina");
        zoneGroup.add(qiniuOssSouthChinaRadioButton);
        qiniuOssConfigPanel.add(qiniuOssSouthChinaRadioButton, gbc);

        // 北美
        gbc.gridx = 4;
        gbc.gridwidth = 1;
        gbc.weightx = 0;
        qiniuOssNorthAmeriaRadioButton = new JRadioButton("NorthAmeria");
        zoneGroup.add(qiniuOssNorthAmeriaRadioButton);
        qiniuOssConfigPanel.add(qiniuOssNorthAmeriaRadioButton, gbc);

        // 恢复 fill 设置
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // 隐藏的区域索引字段（用于存储区域索引值）
        qiniuZoneIndexTextField = new JTextField();
        qiniuZoneIndexTextField.setVisible(false);

        return qiniuOssConfigPanel;
    }

    /**
     * 创建腾讯云 COS 配置面板
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
        tencentOssConfigPanel.add(new JBLabel("Bucket 名称:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        tencentBucketNameTextField = new JTextField();
        tencentOssConfigPanel.add(tencentBucketNameTextField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0;
        tencentOssConfigPanel.add(new JBLabel("Access Key:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        tencentAccessKeyTextField = new JTextField();
        tencentOssConfigPanel.add(tencentAccessKeyTextField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 0;
        tencentOssConfigPanel.add(new JBLabel("Secret Key:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        tencentSecretKeyTextField = new JPasswordField();
        tencentOssConfigPanel.add(tencentSecretKeyTextField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.weightx = 0;
        tencentOssConfigPanel.add(new JBLabel("Region Name:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        tencentRegionNameTextField = new JTextField();
        tencentRegionNameTextField.setText("ap-chengdu");
        tencentOssConfigPanel.add(tencentRegionNameTextField, gbc);

        return tencentOssConfigPanel;
    }

    /**
     * 创建自定义 OSS 配置面板
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
        customOssConfigPanel.add(new JBLabel("API 地址:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        customApiTextField = new JTextField();
        customOssConfigPanel.add(customApiTextField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0;
        customOssConfigPanel.add(new JBLabel("请求 Key:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        customRequestKeyTextField = new JTextField();
        customOssConfigPanel.add(customRequestKeyTextField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 0;
        customOssConfigPanel.add(new JBLabel("响应 URL 路径:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        customResponseUrlPathTextField = new JTextField();
        customOssConfigPanel.add(customResponseUrlPathTextField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.weightx = 0;
        customOssConfigPanel.add(new JBLabel("HTTP 方法:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        customHttpMethodTextField = new JTextField();
        customHttpMethodTextField.setText("POST");
        customOssConfigPanel.add(customHttpMethodTextField, gbc);

        return customOssConfigPanel;
    }

    /**
     * 创建 PicList 配置面板
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
        picListConfigPanel.add(new JBLabel("API 地址:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        picListApiTextField = new JTextField();
        picListApiTextField.setToolTipText("完成 API 地址, 优先级低于命令行");
        picListConfigPanel.add(picListApiTextField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0;
        picListConfigPanel.add(new JBLabel("图床类型:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        picListPicbedTextField = new JTextField();
        picListPicbedTextField.setToolTipText("图床类型（如 aws-s3, qiniu 等）");
        picListConfigPanel.add(picListPicbedTextField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 0;
        picListConfigPanel.add(new JBLabel("配置名称:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        picListConfigNameTextField = new JTextField();
        picListConfigNameTextField.setToolTipText("配置文件名称");
        picListConfigPanel.add(picListConfigNameTextField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.weightx = 0;
        picListConfigPanel.add(new JBLabel("接口密钥:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        picListKeyTextField = new JTextField();
        picListKeyTextField.setToolTipText("接口密钥（用于鉴权）");
        picListConfigPanel.add(picListKeyTextField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.weightx = 0;
        picListConfigPanel.add(new JBLabel("命令行路径:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        picListExeTextField = new TextFieldWithBrowseButton();
        picListExeTextField.setToolTipText("选择命令行文件, 优先级高于 API");

        // 初始化文件选择器（参考老页面的实现）
        initPicListExeBrowser();

        picListConfigPanel.add(picListExeTextField, gbc);

        return picListConfigPanel;
    }

    /**
     * 初始化 PicList 可执行文件选择器
     * <p>
     * 参考老页面的 initPicListExeBrowser() 方法实现
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

        // 添加浏览文件夹监听器
        this.picListExeTextField.addBrowseFolderListener(
            "选择 PicList 可执行文件",
            "请选择 PicList 命令行可执行文件",
            null,  // 项目，可以为 null
            descriptor,
            TextComponentAccessor.TEXT_FIELD_WHOLE_TEXT
                                                        );
    }

    /**
     * 根据索引获取对应的配置面板
     *
     * @param index 服务商索引
     * @return 配置面板
     */
    @NotNull
    private JPanel getConfigPanelForIndex(int index) {
        final CloudEnum cloudEnum = CloudEnum.of(index);
        return switch (cloudEnum) {
            case SM_MS_CLOUD -> createSmmsConfigPanel();
            case ALIYUN_CLOUD -> createAliyunOssConfigPanel();
            case QINIU_CLOUD -> createQiniuOssConfigPanel();
            case TENCENT_CLOUD -> createTencentOssConfigPanel();
            case BAIDU_CLOUD -> createBaiduBosConfigPanel();
            case GITHUB -> createGithubConfigPanel();
            case GITEE -> createGiteeConfigPanel();
            case CUSTOMIZE -> createCustomOssConfigPanel();
            case PICLIST -> createPicListConfigPanel();
        };
    }

    /**
     * 初始化上传服务设定面板
     *
     * @param state 当前状态对象
     */
    public void initUploadServicePanel(@NotNull MikState state) {
        // 设置默认选中的图床服务商
        int defaultCloudIndex = state.getCloudType();
        if (this.cloudServiceComboBox != null) {
            this.cloudServiceComboBox.setSelectedIndex(defaultCloudIndex);

            // 手动触发切换以显示对应的配置面板
            if (this.cloudServiceConfigContainer != null) {
                setCloudServiceConfigContainer(defaultCloudIndex);
            }
        }

        // 默认图床设置
        this.setAsDefaultCloudCheckBox.setSelected(state.isDefaultCloudCheck());
        this.defaultCloudComboBox.setEnabled(state.isDefaultCloudCheck());
        this.defaultCloudComboBox.setSelectedIndex(state.getCloudType());

        // 加载设置页面时从配置中读取配置并设置 cloudServerAvailableMessage 的 text
        if (this.cloudServerAvailableMessage != null) {
            this.cloudServerAvailableMessage.setText(
                OssState.getStatus(state.getCloudType())
                ? MikBundle.message("oss.available")
                : MikBundle.message("oss.not.available")
                                                    );
        }
    }

    private void setCloudServiceConfigContainer(int defaultCloudIndex) {
        this.cloudServiceConfigContainer.removeAll();
        JPanel configPanel = getConfigPanelForIndex(defaultCloudIndex);
        this.cloudServiceConfigContainer.add(configPanel, BorderLayout.CENTER);
        this.cloudServiceConfigContainer.revalidate();
        this.cloudServiceConfigContainer.repaint();
    }

    /**
     * 判断上传服务设定是否已修改
     *
     * @param state 要比较的状态对象
     * @return 如果当前状态与给定状态一致，返回 true；否则返回 false
     */
    public boolean isUploadServiceModified(@NotNull MikState state) {
        boolean defaultCloudCheck = this.setAsDefaultCloudCheckBox.isSelected();
        int cloudType = this.defaultCloudComboBox.getSelectedIndex();

        return defaultCloudCheck == state.isDefaultCloudCheck()
               && cloudType == state.getCloudType();
    }

    /**
     * 应用上传服务设定配置到状态对象
     *
     * @param state 状态对象
     */
    public void applyUploadServiceConfigs(@NotNull MikState state) {
        state.setDefaultCloudCheck(this.setAsDefaultCloudCheckBox.isSelected());
        state.setCloudType(this.setAsDefaultCloudCheckBox.isSelected()
                           ? this.defaultCloudComboBox.getSelectedIndex()
                           : CloudEnum.SM_MS_CLOUD.getIndex());
        state.setTempCloudType(this.defaultCloudComboBox.getSelectedIndex());
    }

    /**
     * 初始化各个 OSS 设置对象
     * <p>
     * 创建各个图床服务商的 Setting 对象，并调用 init 方法初始化配置
     * <p>
     * 注意：该方法只在初始化时调用一次，创建默认选中的图床 Setting 对象
     *
     * @param state 当前状态对象
     */
    public void initOssSettings(@NotNull MikState state) {
        // 初始化默认选中的图床 Setting 对象
        int index = state.getCloudType();
        switch (index) {
            case 0:
                if (this.smmsOssSetting == null && this.smmsUrlTextField != null) {
                    this.smmsOssSetting = new SmmsOssSetting(this.smmsUrlTextField, this.smmsTokenTextField);
                    this.smmsOssSetting.init(state.getSmmsOssState());
                }
                break;
            case 1:
                if (this.aliyunOssSetting == null && this.aliyunBucketNameTextField != null) {
                    this.aliyunOssSetting = new AliyunOssSetting(
                        this.aliyunBucketNameTextField, this.aliyunAccessKeyTextField,
                        this.aliyunAccessSecretTextField, this.aliyunEndpointTextField,
                        this.aliyunFileDirTextField, this.aliyunCustomEndpointCheckBox,
                        this.aliyunCustomEndpointTextField, null, null);
                    this.aliyunOssSetting.init(state.getAliyunOssState());
                }
                break;
            case 2:
                if (this.baiduBosSetting == null && this.baiduBosBucketNameTextField != null) {
                    this.baiduBosSetting = new BaiduBosSetting(
                        this.baiduBosBucketNameTextField, this.baiduBosAccessKeyTextField,
                        this.baiduBosAccessSecretTextField, this.baiduBosEndpointTextField,
                        this.baiduBosFileDirTextField, this.baiduBosCustomEndpointCheckBox,
                        this.baiduBosCustomEndpointTextField, null, null);
                    this.baiduBosSetting.init(state.getBaiduBosState());
                }
                break;
            case 3:
                if (this.githubSetting == null && this.githubReposTextField != null) {
                    this.githubSetting = new GithubSetting(
                        this.githubReposTextField, this.githubBranchTextField,
                        this.githubTokenTextField, this.githubFileDirTextField,
                        this.githubCustomEndpointCheckBox, this.githubCustomEndpointTextField,
                        null, null);
                    this.githubSetting.init(state.getGithubOssState());
                }
                break;
            case 4:
                if (this.giteeSetting == null && this.giteeReposTextField != null) {
                    this.giteeSetting = new GiteeSetting(
                        this.giteeReposTextField, this.giteeBranchTextField,
                        this.giteeTokenTextField, this.giteeFileDirTextField,
                        this.giteeCustomEndpointCheckBox, this.giteeCustomEndpointTextField,
                        null, null);
                    this.giteeSetting.init(state.getGiteeOssState());
                }
                break;
            case 5:
                if (this.qiniuOssSetting == null && this.qiniuOssBucketNameTextField != null) {
                    this.qiniuOssSetting = new QiniuOssSetting(
                        this.qiniuOssBucketNameTextField, this.qiniuOssAccessKeyTextField,
                        this.qiniuOssAccessSecretTextField, this.qiniuOssDomainTextField,
                        this.qiniuOssEastChinaRadioButton, this.qiniuOssNortChinaRadioButton,
                        this.qiniuOssSouthChinaRadioButton, this.qiniuOssNorthAmeriaRadioButton,
                        this.qiniuZoneIndexTextField);
                    this.qiniuOssSetting.init(state.getQiniuOssState());
                }
                break;
            case 6:
                if (this.tencentOssSetting == null && this.tencentBucketNameTextField != null) {
                    this.tencentOssSetting = new TencentOssSetting(
                        this.tencentBucketNameTextField, this.tencentAccessKeyTextField,
                        this.tencentSecretKeyTextField, this.tencentRegionNameTextField);
                    this.tencentOssSetting.init(state.getTencentOssState());
                }
                break;
            case 7:
                if (this.customOssSetting == null && this.customApiTextField != null) {
                    this.customOssSetting = new CustomOssSetting(
                        this.customApiTextField, this.customRequestKeyTextField,
                        this.customResponseUrlPathTextField, this.customHttpMethodTextField);
                    this.customOssSetting.init(state.getCustomOssState());
                }
                break;
            case 8:
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

    public void apply(MikState state) {

        // 应用各个 OSS 配置
        if (this.smmsOssSetting != null) {
            this.smmsOssSetting.apply(state.getSmmsOssState());
        }
        if (this.aliyunOssSetting != null) {
            this.aliyunOssSetting.apply(state.getAliyunOssState());
        }
        if (this.baiduBosSetting != null) {
            this.baiduBosSetting.apply(state.getBaiduBosState());
        }
        if (this.githubSetting != null) {
            this.githubSetting.apply(state.getGithubOssState());
        }
        if (this.giteeSetting != null) {
            this.giteeSetting.apply(state.getGiteeOssState());
        }
        if (this.qiniuOssSetting != null) {
            this.qiniuOssSetting.apply(state.getQiniuOssState());
        }
        if (this.tencentOssSetting != null) {
            this.tencentOssSetting.apply(state.getTencentOssState());
        }
        if (this.customOssSetting != null) {
            this.customOssSetting.apply(state.getCustomOssState());
        }
        if (this.picListOssSetting != null) {
            this.picListOssSetting.apply(state.getPicListOssState());
        }
    }

    public void reset(MikState state) {

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

    public boolean isModified(MikState state) {

        // 检查各个 OSS 配置是否修改
        boolean ossModified = true;
        if (this.smmsOssSetting != null) {
            ossModified = this.smmsOssSetting.isModified(state.getSmmsOssState());
        }
        if (this.aliyunOssSetting != null) {
            ossModified = ossModified && this.aliyunOssSetting.isModified(state.getAliyunOssState());
        }
        if (this.baiduBosSetting != null) {
            ossModified = ossModified && this.baiduBosSetting.isModified(state.getBaiduBosState());
        }
        if (this.githubSetting != null) {
            ossModified = ossModified && this.githubSetting.isModified(state.getGithubOssState());
        }
        if (this.giteeSetting != null) {
            ossModified = ossModified && this.giteeSetting.isModified(state.getGiteeOssState());
        }
        if (this.qiniuOssSetting != null) {
            ossModified = ossModified && this.qiniuOssSetting.isModified(state.getQiniuOssState());
        }
        if (this.tencentOssSetting != null) {
            ossModified = ossModified && this.tencentOssSetting.isModified(state.getTencentOssState());
        }
        if (this.customOssSetting != null) {
            ossModified = ossModified && this.customOssSetting.isModified(state.getCustomOssState());
        }
        if (this.picListOssSetting != null) {
            ossModified = ossModified && this.picListOssSetting.isModified(state.getPicListOssState());
        }

        return ossModified;
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
        this.testUploadButton.addActionListener(e -> {
            int index = this.cloudServiceComboBox.getSelectedIndex();
            InputStream inputStream = this.getClass().getResourceAsStream("/" + TEST_FILE_NAME);
            CloudEnum cloudEnum = OssState.getCloudType(index);
            OssClient client = ClientUtils.getClient(cloudEnum);
            if (client != null) {
                String url;
                try {
                    url = client.upload(inputStream, TEST_FILE_NAME, this.getConfigPanelForIndex(index));
                    log.info("测试按钮上传的图片返回结果: {}", url);
                } catch (Exception exception) {
                    //显示对话框
                    Messages.showMessageDialog(this.content,
                                               exception.getMessage(),
                                               "Error",
                                               Messages.getErrorIcon());
                    return;
                }
                if (StringUtils.isNotBlank(url)) {
                    // 测试通过了, 移除红点指示器
                    this.testUploadButton.setText("验证图片上传");

                    // 测试通过了, 则判断是否勾选设置默认图床, 若勾选则刷新可用状态
                    boolean isDefaultCheckBox = this.setAsDefaultCloudCheckBox.isSelected();
                    if (isDefaultCheckBox) {
                        int cloudTypeIndex = this.defaultCloudComboBox.getSelectedIndex();
                        if (index == cloudTypeIndex) {
                            this.cloudServerAvailableMessage.setText("<html><span style='color:greed'>●</span> 验证图片上传</html>");
                        }
                    }
                    // 主动保存
                    this.apply(MikPersistenComponent.getInstance().getState());
                    if (log.isTraceEnabled()) {
                        BrowserUtil.browse(url);
                    }
                } else {
                    Messages.showMessageDialog(this.content,
                                               MikBundle.message("settings.upload.failed", cloudEnum.getTitle()),
                                               "Error",
                                               Messages.getErrorIcon());
                }
            } else {
                Messages.showMessageDialog(this.content,
                                           MikBundle.message("settings.upload.not.available", cloudEnum.getTitle()),
                                           "Error",
                                           Messages.getErrorIcon());
            }
        });

        // help button 监听
        this.helpButton.addActionListener(e -> {
            CloudEnum cloudType = OssState.getCloudType(this.cloudServiceComboBox.getSelectedIndex());
            // https://mik.dong4j.site/mik/help/settings/aliyun_cloud
            String url = MikNotification.helpUrl(HelpType.SETTING.where + "/" + cloudType.name().toLowerCase());
            if (!url.equals(MikNotification.ABOUT_BLANK)) {
                BrowserUtil.browse(url);
            }
        });
    }

}
