/*
 * MIT License
 *
 * Copyright (c) 2022 dong4j <dong4j@gmail.com>
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

package info.dong4j.idea.plugin.settings;

import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.SearchableConfigurable;
import com.intellij.openapi.ui.Messages;

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
import info.dong4j.idea.plugin.settings.oss.QiniuOssSetting;
import info.dong4j.idea.plugin.settings.oss.TencentOssSetting;
import info.dong4j.idea.plugin.settings.oss.WeiboOssSetting;
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
 * <p>Description: </p>
 *
 * @author dong4j
 * @version 0.0.1
 * @email "mailto:dong4j@gmail.com"
 * @date 2019.03.13 12:19
 * @since 0.0.1
 */
@Slf4j
public class ProjectSettingsPage implements SearchableConfigurable, Configurable.NoMargin {
    /** TEST_FILE_NAME */
    public static final String TEST_FILE_NAME = "mik.png";

    /** Config */
    private final MikPersistenComponent config;

    /** My main panel */
    private JPanel myMainPanel;

    //region authorizationPanel
    private JPanel authorizationPanel;
    /** Authorization tabbed panel */
    private JTabbedPane authorizationTabbedPanel;

    //region weiboOss
    /** weiboOssAuthorizationPanel group */
    private JPanel weiboOssAuthorizationPanel;
    /** Weibo user name text field */
    private JTextField weiboUserNameTextField;
    /** Weibo password field */
    private JPasswordField weiboPasswordField;
    /** User name label */
    private JLabel userNameLabel;
    /** Password label */
    private JLabel passwordLabel;
    //endregion

    //region aliyunOss
    /** aliyunOssAuthorizationPanel group */
    private JPanel aliyunOssAuthorizationPanel;
    /** Aliyun oss bucket name text field */
    private JTextField aliyunOssBucketNameTextField;
    /** Aliyun oss access key text field */
    private JTextField aliyunOssAccessKeyTextField;
    /** Aliyun oss access secret key text field */
    private JPasswordField aliyunOssAccessSecretKeyTextField;
    /** Aliyun oss endpoint text field */
    private JTextField aliyunOssEndpointTextField;
    /** Aliyun oss file dir text field */
    private JTextField aliyunOssFileDirTextField;
    /** 是否启用自定义域名 */
    private JCheckBox aliyunOssCustomEndpointCheckBox;
    /** 自定义域名 */
    private JTextField aliyunOssCustomEndpointTextField;
    /** 自定义域名帮助文档 */
    private JLabel aliyunOssCustomEndpointHelper;
    /** Example text field */
    private JTextField aliyunOssExampleTextField;
    //endregion

    //region baiduBos
    /** Baidu bos authorization panel */
    private JPanel baiduBosAuthorizationPanel;
    /** Baidu bos bucket name text field */
    private JTextField baiduBosBucketNameTextField;
    /** Baidu bos access key text field */
    private JTextField baiduBosAccessKeyTextField;
    /** Baidu bos access secret key text field */
    private JPasswordField baiduBosAccessSecretKeyTextField;
    /** Baidu bos endpoint text field */
    private JTextField baiduBosEndpointTextField;
    /** Baidu bos file dir text field */
    private JTextField baiduBosFileDirTextField;
    /** Baidu bos custom endpoint check box */
    private JCheckBox baiduBosCustomEndpointCheckBox;
    /** Baidu bos custom endpoint text field */
    private JTextField baiduBosCustomEndpointTextField;
    /** Baidu bos custom endpoint helper */
    private JLabel baiduBosCustomEndpointHelper;
    /** Baidu bosxample text field */
    private JTextField baiduBosExampleTextField;
    //endregion

    //region qiniuOss
    /** qiniuOssAuthorizationPanel group */
    private JPanel qiniuOssAuthorizationPanel;
    /** Qiniu oss bucket name text field */
    private JTextField qiniuOssBucketNameTextField;
    /** Qiniu oss access key text field */
    private JTextField qiniuOssAccessKeyTextField;
    /** Qiniu oss access secret key text field */
    private JPasswordField qiniuOssAccessSecretKeyTextField;
    /** Qiniu oss up host text field */
    private JTextField qiniuOssUpHostTextField;
    /** Qiniu oss east china radio button */
    private JRadioButton qiniuOssEastChinaRadioButton;
    /** Qiniu oss nort china radio button */
    private JRadioButton qiniuOssNortChinaRadioButton;
    /** Qiniu oss south china radio button */
    private JRadioButton qiniuOssSouthChinaRadioButton;
    /** Qiniu oss north ameria radio button */
    private JRadioButton qiniuOssNorthAmeriaRadioButton;
    /** Zone index text filed */
    private JTextField zoneIndexTextFiled;
    //endregion

    //region tencent
    /** Tencent oss authorization panel */
    private JPanel tencentOssAuthorizationPanel;
    /** Tencent backet name text field */
    private JTextField tencentBacketNameTextField;
    /** Tencent access key text field */
    private JTextField tencentAccessKeyTextField;
    /** Tencent secret key text field */
    private JPasswordField tencentSecretKeyTextField;
    /** Tencent region name text field */
    private JTextField tencentRegionNameTextField;
    //endregion

    //region gitHub
    /** Git hub authorization panel */
    private JPanel githubAuthorizationPanel;
    /** Git hub repos text field */
    private JTextField githubReposTextField;
    /** Git hub branch text field */
    private JTextField githubBranchTextField;
    /** Git hub token text field */
    private JPasswordField githubTokenTextField;
    /** Git hub file dir text field */
    private JTextField githubFileDirTextField;
    /** Git hub custom endpoint check box */
    private JCheckBox githubCustomEndpointCheckBox;
    /** Git hubs custom endpoint text field */
    private JTextField githubCustomEndpointTextField;
    /** Git hub custom endpoint helper */
    private JLabel githubCustomEndpointHelper;
    /** Git hub example text field */
    private JTextField githubExampleTextField;
    //endregion

    //region custom
    private JPanel customAuthorizationPanel;
    private JTextField customApiTextField;
    private JTextField requestKeyTextField;
    private JTextField responseUrlPathTextField;
    private JTextField httpMethodTextField;
    //endregion

    //region gitee
    /** Git hub authorization panel */
    private JPanel giteeAuthorizationPanel;
    /** Git hub repos text field */
    private JTextField giteeReposTextField;
    /** Git hub branch text field */
    private JTextField giteeBranchTextField;
    /** Git hub token text field */
    private JPasswordField giteeTokenTextField;
    /** Git hub file dir text field */
    private JTextField giteeFileDirTextField;
    /** Git hub custom endpoint check box */
    private JCheckBox giteeCustomEndpointCheckBox;
    /** Git hubs custom endpoint text field */
    private JTextField giteeCustomEndpointTextField;
    /** Git hub custom endpoint helper */
    private JLabel giteeCustomEndpointHelper;
    /** Git hub example text field */
    private JTextField giteeExampleTextField;
    //endregion

    /** Test button */
    private JButton testButton;
    /** Help button */
    private JButton helpButton;
    //endregion

    //region globalUploadPanel
    /** globalUploadPanel group */
    private JPanel globalUploadPanel;
    /** Default cloud combo box */
    private JComboBox<?> defaultCloudComboBox;

    /** Change to html tag check box */
    private JCheckBox changeToHtmlTagCheckBox;
    /** Large picture radio button */
    private JRadioButton largePictureRadioButton;
    /** Common radio button */
    private JRadioButton commonRadioButton;
    /** Common label */
    private JLabel commonLabel;
    /** Custom radio button */
    private JRadioButton customRadioButton;
    /** Custom label */
    private JLabel customLabel;
    /** Custom html type text field */
    private JTextField customHtmlTypeTextField;
    /** Compress check box */
    private JCheckBox compressCheckBox;
    /** Compress slider */
    private JSlider compressSlider;
    /** Compress label */
    private JLabel compressLabel;
    /** Rename check box */
    private JCheckBox renameCheckBox;
    /** File name suffix box field */
    private JComboBox<?> fileNameSuffixBoxField;
    /** 水印复选框 */
    private JCheckBox watermarkCheckBox;
    /** 水印文字 */
    private JTextField watermarkTextField;
    /** Custom message */
    private JLabel customMessage;
    //endregion

    //region clipboardPanel
    /** Clipboard panel */
    private JPanel clipboardPanel;
    /** Copy to dir check box */
    private JCheckBox copyToDirCheckBox;
    /** Where to copy text field */
    private JTextField whereToCopyTextField;
    /** Upload and replace check box */
    private JCheckBox uploadAndReplaceCheckBox;
    /** Default cloud check box */
    private JCheckBox defaultCloudCheckBox;
    //endregion

    //region WeiboOssSetting
    private final WeiboOssSetting weiboOssSetting = new WeiboOssSetting(this.weiboUserNameTextField, this.weiboPasswordField);
    //endregion

    //region AliyunOssSetting
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
    private final TencentOssSetting tencentOssSetting = new TencentOssSetting(this.tencentBacketNameTextField,
                                                                              this.tencentAccessKeyTextField,
                                                                              this.tencentSecretKeyTextField,
                                                                              this.tencentRegionNameTextField);
    //endregion

    //region CustomOssSetting
    private final CustomOssSetting customOssSetting = new CustomOssSetting(this.customApiTextField,
                                                                           this.requestKeyTextField,
                                                                           this.responseUrlPathTextField,
                                                                           this.httpMethodTextField);
    //endregion

    /** todo-dong4j : (2019年03月20日 13:25) [测试输入验证用] */
    private JTextField myPort;

    /**
     * Project settings page
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
     * Create component
     *
     * @return the j component
     * @since 0.0.1
     */
    @Override
    public JComponent createComponent() {
        this.initFromSettings();
        return this.myMainPanel;
    }

    /**
     * Gets id *
     *
     * @return the id
     * @since 0.0.1
     */
    @NotNull
    @Override
    public String getId() {
        return this.getDisplayName();
    }

    /**
     * Gets display name *
     *
     * @return the display name
     * @since 0.0.1
     */
    @Nls
    @Override
    public String getDisplayName() {
        return "Markdown Image Kit";
    }

    /**
     * 每次打开设置面板时执行
     *
     * @since 0.0.1
     */
    private void initFromSettings() {
        MikState state = this.config.getState();
        this.initAuthorizationTabbedPanel(state);
        this.initGlobalPanel(state);
        this.initClipboardControl();

        // String MESSAGE = "The port number should be between 0 and 65535.";
        //
        // // Components initialization
        // new ComponentValidator(ProjectManager.getInstance().getDefaultProject()).withValidator(v -> {
        //     String pt = myPort.getText();
        //     if (StringUtil.isNotEmpty(pt)) {
        //         try {
        //             int portValue = Integer.parseInt(pt);
        //             if (portValue >= 0 && portValue <= 65535) {
        //                 v.updateInfo(null);
        //             } else {
        //                 v.updateInfo(new ValidationInfo(MESSAGE, myPort));
        //             }
        //         } catch (NumberFormatException nfe) {
        //             v.updateInfo(new ValidationInfo(MESSAGE, myPort));
        //         }
        //     } else {
        //         v.updateInfo(null);
        //     }
        // }).installOn(myPort);
        //
        // myPort.getDocument().addDocumentListener(new DocumentAdapter() {
        //     @Override
        //     protected void textChanged(@NotNull DocumentEvent e) {
        //         ComponentValidator.getInstance(myPort).ifPresent(ComponentValidator::revalidate);
        //     }
        // });
    }

    /**
     * 初始化 authorizationTabbedPanel group
     *
     * @param state state
     * @since 0.0.1
     */
    private void initAuthorizationTabbedPanel(@NotNull MikState state) {
        int defaultCloudIndex = state.getCloudType() == CloudEnum.SM_MS_CLOUD.index
                                ? CloudEnum.WEIBO_CLOUD.index
                                : state.getCloudType();
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

        this.weiboOssSetting.init(this.config.getState().getWeiboOssState());
        this.aliyunOssSetting.init(this.config.getState().getAliyunOssState());
        this.baiduBosSetting.init(this.config.getState().getBaiduBosState());
        this.githubSetting.init(this.config.getState().getGithubOssState());
        this.giteeSetting.init(this.config.getState().getGiteeOssState());
        this.qiniuOssSetting.init(this.config.getState().getQiniuOssState());
        this.tencentOssSetting.init(this.config.getState().getTencentOssState());
        this.customOssSetting.init(this.config.getState().getCustomOssState());

        this.testAndHelpListener();
    }

    /**
     * 添加 test 和 help 按钮监听, 根据选中的图床进行测试
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
     * 初始化 upload 配置组
     *
     * @param state state
     * @since 0.0.1
     */
    private void initGlobalPanel(@NotNull MikState state) {
        boolean isDefaultCloudCheck = state.isDefaultCloudCheck();
        this.defaultCloudCheckBox.setSelected(isDefaultCloudCheck);
        // 没有设置默认图床, 则显示提示消息
        if (!isDefaultCloudCheck) {
            this.customMessage.setText("sm.ms");
        } else {
            this.customMessage.setText(OssState.getStatus(state.getCloudType()) ? "" : "当前 OSS 不可用!");
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
     * Show select cloud message
     *
     * @param cloudType cloud type
     * @since 0.0.1
     */
    private void showSelectCloudMessage(int cloudType) {
        if (this.defaultCloudCheckBox.isSelected()) {
            boolean isClientEnable = OssState.getStatus(cloudType);
            this.customMessage.setText(isClientEnable ? "" : "当前 OSS 不可用!");
        } else {
            this.customMessage.setText("sm.ms");
        }
    }

    /**
     * 初始化替换标签设置组
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
     * 处理被选中的单选框
     *
     * @param group  the group
     * @param button the button
     * @since 0.0.1
     */
    private void addChangeTagRadioButton(@NotNull ButtonGroup group, JRadioButton button) {
        group.add(button);
        // 构造一个监听器，响应checkBox事件
        ActionListener actionListener = e -> {
            Object sourceObject = e.getSource();
            if (sourceObject instanceof JRadioButton) {
                JRadioButton sourceButton = (JRadioButton) sourceObject;
                this.customHtmlTypeTextField.setEnabled(ImageMarkEnum.CUSTOM.text.equals(sourceButton.getText()));
            }
        };
        button.addActionListener(actionListener);
    }

    /**
     * 初始化图片压缩配置组
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
     * 初始化 clipboard group
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
     *
     * @return the boolean
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
                 && this.weiboOssSetting.isModified(state.getWeiboOssState())
                 && this.qiniuOssSetting.isModified(state.getQiniuOssState())
                 && this.tencentOssSetting.isModified(state.getTencentOssState())
                 && this.customOssSetting.isModified(state.getCustomOssState())
                 && this.isGeneralModified(state)
                 && this.isClipboardModified(state)
        );
    }

    /**
     * Is general modified
     *
     * @param state state
     * @return the boolean
     * @since 0.0.1
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
     * Is clipboard modified
     *
     * @param state state
     * @return the boolean
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
     * 配置被修改后时被调用, 修改 state 中的数据
     *
     * @since 0.0.1
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
        this.weiboOssSetting.apply(state.getWeiboOssState());
        this.customOssSetting.apply(state.getCustomOssState());
        this.applyGeneralConfigs(state);
        this.applyClipboardConfigs(state);
    }

    /**
     * Apply general configs
     *
     * @param state state
     * @since 0.0.1
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
     * Apply clipboard configs
     *
     * @param state state
     * @since 0.0.1
     */
    private void applyClipboardConfigs(@NotNull MikState state) {
        state.setCopyToDir(this.copyToDirCheckBox.isSelected());
        state.setUploadAndReplace(this.uploadAndReplaceCheckBox.isSelected());
        state.setImageSavePath(this.whereToCopyTextField.getText().trim());
    }

    /**
     * 撤回是调用
     *
     * @since 0.0.1
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
        this.weiboOssSetting.reset(state.getWeiboOssState());
        this.customOssSetting.reset(state.getCustomOssState());
        this.resetGeneralCOnfigs(state);
        this.resetClipboardConfigs(state);
    }

    /**
     * Reset general c onfigs
     *
     * @param state state
     * @since 0.0.1
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
     * Reset clipboard configs
     *
     * @param state state
     * @since 0.0.1
     */
    private void resetClipboardConfigs(@NotNull MikState state) {
        this.copyToDirCheckBox.setSelected(state.isCopyToDir());
        this.whereToCopyTextField.setText(state.getImageSavePath());
        this.uploadAndReplaceCheckBox.setSelected(state.isUploadAndReplace());
    }
}
