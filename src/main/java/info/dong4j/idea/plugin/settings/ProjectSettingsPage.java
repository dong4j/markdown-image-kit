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

package info.dong4j.idea.plugin.settings;

import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.SearchableConfigurable;
import com.intellij.ui.JBColor;

import info.dong4j.idea.plugin.MikBundle;
import info.dong4j.idea.plugin.client.OssClient;
import info.dong4j.idea.plugin.enums.CloudEnum;
import info.dong4j.idea.plugin.enums.HelpType;
import info.dong4j.idea.plugin.enums.ImageMarkEnum;
import info.dong4j.idea.plugin.enums.ZoneEnum;
import info.dong4j.idea.plugin.notify.MikNotification;
import info.dong4j.idea.plugin.util.ClientUtils;
import info.dong4j.idea.plugin.util.DES;
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
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;

import lombok.extern.slf4j.Slf4j;

/**
 * <p>Company: no company</p>
 * <p>Description: </p>
 *
 * @author dong4j
 * @version 0.0.1
 * @email "mailto:dong4j@gmail.com"
 * @date 2019.03.13 12:19
 * @since 0.0.1
 */
@Slf4j
public class ProjectSettingsPage implements SearchableConfigurable, Configurable.NoScroll {
    /** TEST_FILE_NAME */
    static final String TEST_FILE_NAME = "mik.png";

    /** Config */
    private final MikPersistenComponent config;

    private JScrollPane jScrollPane;
    /** My main panel */
    private JPanel myMainPanel;

    //region authorizationPanel
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

    /** 按钮 group */
    private JButton testButton;
    /** Test message */
    private JLabel testMessage;
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
    /** 自定义默认图床 */
    private JCheckBox defaultCloudCheckBox;
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
        this.jScrollPane = new JScrollPane(this.myMainPanel);
        return this.jScrollPane;
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
        // 打开设置页时默认选中默认上传图床
        this.authorizationTabbedPanel.setSelectedIndex(state.getCloudType() == CloudEnum.SM_MS_CLOUD.index ? CloudEnum.WEIBO_CLOUD.index
                                                                                                           : state.getCloudType());
        this.authorizationTabbedPanel.addChangeListener(e -> {
            // 清理 test 信息
            this.testMessage.setText("");
            this.testButton.setText("Test Upload");
            // 获得指定索引的选项卡标签
            log.trace("change {}", this.authorizationTabbedPanel.getTitleAt(this.authorizationTabbedPanel.getSelectedIndex()));
        });

        this.aliyunOssSetting.init(this.config.getState().getAliyunOssState());

        this.baiduBosSetting.init(this.config.getState().getBaiduBosState());

        this.initWeiboOssAuthenticationPanel();
        this.initQiniuOssAuthenticationPanel(state);
        this.initTencentOssAuthorizationPanelPanel(state);
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
                    this.testMessage.setForeground(JBColor.RED);
                    this.testMessage.setText("Error: " + exception.getMessage());
                    return;
                }
                if (StringUtils.isNotBlank(url)) {
                    this.testMessage.setForeground(JBColor.GREEN);
                    this.testMessage.setText("Upload Succeed");
                    this.testButton.setText("Test Upload");
                    // 测试通过了, 则判断是否勾选设置默认图床, 若勾选则刷新可用状态
                    boolean isDefaultCheckBox = this.defaultCloudCheckBox.isSelected();
                    if (isDefaultCheckBox) {
                        int cloudTypeIndex = this.defaultCloudComboBox.getSelectedIndex();
                        if (index == cloudTypeIndex) {
                            this.customMessage.setText("");
                        }
                    }
                    if (log.isTraceEnabled()) {
                        BrowserUtil.browse(url);
                    }
                } else {
                    this.testButton.setText("Try Again");
                    this.testMessage.setForeground(JBColor.RED);
                    this.testMessage.setText("Upload Failed, Please Check The Configuration");
                }
            } else {
                this.testButton.setText("Try Again");
                this.testMessage.setForeground(JBColor.RED);
                this.testMessage.setText("Upload Failed, Please Check The Configuration");
            }
        });

        // help button 监听
        this.helpButton.addActionListener(e -> {
            // 打开浏览器到帮助页面
            String url = MikNotification.helpUrl(HelpType.SETTING.where);
            if (!url.equals(MikNotification.ABOUT_BLANK)) {
                BrowserUtil.browse(url);
            }
        });
    }


    /**
     * 初始化 weibo oss 认证相关设置
     *
     * @since 0.0.1
     */
    private void initWeiboOssAuthenticationPanel() {
        this.weiboUserNameTextField.setText(this.config.getState().getWeiboOssState().getUserName());
        this.weiboPasswordField.setText(DES.decrypt(this.config.getState().getWeiboOssState().getPassword(), MikState.WEIBOKEY));
    }

    /**
     * 初始化 qiniu oss 认证相关设置
     *
     * @param state state
     * @since 0.0.1
     */
    private void initQiniuOssAuthenticationPanel(@NotNull MikState state) {
        QiniuOssState qiniuOssState = state.getQiniuOssState();
        this.qiniuOssAccessSecretKeyTextField.setText(DES.decrypt(qiniuOssState.getAccessSecretKey(), MikState.QINIU));

        this.qiniuOssUpHostTextField.addFocusListener(new JTextFieldHintListener(this.qiniuOssUpHostTextField, "http(s)://domain/"));

        ButtonGroup group = new ButtonGroup();
        this.qiniuOssEastChinaRadioButton.setMnemonic(ZoneEnum.EAST_CHINA.index);
        this.qiniuOssNortChinaRadioButton.setMnemonic(ZoneEnum.NORT_CHINA.index);
        this.qiniuOssSouthChinaRadioButton.setMnemonic(ZoneEnum.SOUTH_CHINA.index);
        this.qiniuOssNorthAmeriaRadioButton.setMnemonic(ZoneEnum.NORTH_AMERIA.index);
        this.addZoneRadioButton(group, this.qiniuOssEastChinaRadioButton);
        this.addZoneRadioButton(group, this.qiniuOssNortChinaRadioButton);
        this.addZoneRadioButton(group, this.qiniuOssSouthChinaRadioButton);
        this.addZoneRadioButton(group, this.qiniuOssNorthAmeriaRadioButton);

        this.qiniuOssEastChinaRadioButton.setSelected(qiniuOssState.getZoneIndex() == this.qiniuOssEastChinaRadioButton.getMnemonic());
        this.qiniuOssNortChinaRadioButton.setSelected(qiniuOssState.getZoneIndex() == this.qiniuOssNortChinaRadioButton.getMnemonic());
        this.qiniuOssSouthChinaRadioButton.setSelected(qiniuOssState.getZoneIndex() == this.qiniuOssSouthChinaRadioButton.getMnemonic());
        this.qiniuOssNorthAmeriaRadioButton.setSelected(qiniuOssState.getZoneIndex() == this.qiniuOssNorthAmeriaRadioButton.getMnemonic());
    }

    /**
     * 处理被选中的 zone 单选框
     *
     * @param group  the group
     * @param button the button
     * @since 0.0.1
     */
    private void addZoneRadioButton(@NotNull ButtonGroup group, JRadioButton button) {
        group.add(button);
        ActionListener actionListener = e -> {
            Object sourceObject = e.getSource();
            if (sourceObject instanceof JRadioButton) {
                JRadioButton sourceButton = (JRadioButton) sourceObject;
                this.zoneIndexTextFiled.setText(String.valueOf(sourceButton.getMnemonic()));
                this.testMessage.setText("");
                this.testButton.setText("Test Upload");
            }
        };
        button.addActionListener(actionListener);
    }

    /**
     * 初始化 tencent oss 认证相关设置
     *
     * @param state state
     * @since 0.0.1
     */
    private void initTencentOssAuthorizationPanelPanel(MikState state) {
        TencentOssState tencentOssState = state.getTencentOssState();
        this.tencentSecretKeyTextField.setText(DES.decrypt(tencentOssState.getSecretKey(), MikState.TENCENT));
        this.tencentAccessKeyTextField.setText(tencentOssState.getAccessKey());
        this.tencentRegionNameTextField.setText(tencentOssState.getRegionName());
        this.tencentBacketNameTextField.setText(tencentOssState.getBucketName());
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
            this.customMessage.setText("未设置默认图床时, 将使用 sm.ms 作为默认图床");
        } else {
            this.customMessage.setText(OssState.getStatus(state.getCloudType()) ? "" : "当前 OSS 不可用, 将使用 sm.ms 作为默认图床");
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
            this.customMessage.setText(isClientEnable ? "" : "当前 OSS 不可用, 将使用 sm.ms 作为默认图床");
            this.customMessage.setForeground(isClientEnable ? JBColor.WHITE : JBColor.RED);
        } else {
            this.customMessage.setText("未设置默认图床时, 将使用 sm.ms 作为默认图床");
            this.customMessage.setForeground(JBColor.WHITE);
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

        return !(this.aliyunOssSetting.isModified()
                 && this.baiduBosSetting.isModified()
                 && this.isWeiboAuthModified(state)
                 && this.isQiniuAuthModified(state)
                 && this.isTencentAuthModified(state)
                 && this.isGeneralModified(state)
                 && this.isClipboardModified(state)
        );
    }

    /**
     * Is weibo auth modified
     *
     * @param state state
     * @return the boolean
     * @since 0.0.1
     */
    private boolean isWeiboAuthModified(@NotNull MikState state) {
        WeiboOssState weiboOssState = state.getWeiboOssState();
        String weiboUsername = this.weiboUserNameTextField.getText().trim();
        String weiboPassword = new String(this.weiboPasswordField.getPassword());
        if (StringUtils.isNotBlank(weiboPassword)) {
            weiboPassword = DES.encrypt(weiboPassword, MikState.WEIBOKEY);
        }
        return weiboUsername.equals(weiboOssState.getUserName())
               && weiboPassword.equals(weiboOssState.getPassword());
    }

    /**
     * Is qiniu auth modified
     *
     * @param state state
     * @return the boolean
     * @since 0.0.1
     */
    private boolean isQiniuAuthModified(@NotNull MikState state) {
        QiniuOssState qiniuOssState = state.getQiniuOssState();
        String bucketName = this.qiniuOssBucketNameTextField.getText().trim();
        String accessKey = this.qiniuOssAccessKeyTextField.getText().trim();
        String secretKey = new String(this.qiniuOssAccessSecretKeyTextField.getPassword());
        if (StringUtils.isNotBlank(secretKey)) {
            secretKey = DES.encrypt(secretKey, MikState.QINIU);
        }
        // todo-dong4j : (2019年03月19日 21:01) [重构为 domain]
        String endpoint = this.qiniuOssUpHostTextField.getText().trim();
        // todo-dong4j : (2019年03月19日 21:13) [zone]
        int zoneIndex = Integer.parseInt(this.zoneIndexTextFiled.getText());

        return bucketName.equals(qiniuOssState.getBucketName())
               && accessKey.equals(qiniuOssState.getAccessKey())
               && secretKey.equals(qiniuOssState.getAccessSecretKey())
               && zoneIndex == qiniuOssState.getZoneIndex()
               && endpoint.equals(qiniuOssState.getEndpoint());
    }

    /**
     * Is tencent auth modified
     *
     * @param state state
     * @return the boolean
     * @since 0.0.1
     */
    private boolean isTencentAuthModified(@NotNull MikState state) {
        TencentOssState tencentOssState = state.getTencentOssState();
        String secretKey = new String(this.tencentSecretKeyTextField.getPassword());

        if (StringUtils.isNotBlank(secretKey)) {
            secretKey = DES.encrypt(secretKey, MikState.QINIU);
        }

        String bucketName = this.tencentBacketNameTextField.getText().trim();
        String accessKey = this.tencentAccessKeyTextField.getText().trim();

        String regionName = this.tencentRegionNameTextField.getText().trim();

        return bucketName.equals(tencentOssState.getBucketName())
               && accessKey.equals(tencentOssState.getAccessKey())
               && secretKey.equals(tencentOssState.getSecretKey())
               && regionName.equals(tencentOssState.getRegionName());
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

        return changeToHtmlTag == state.isChangeToHtmlTag()
               && tagType.equals(state.getTagType())
               && tagTypeCode.equals(state.getTagTypeCode())
               && compress == state.isCompress()
               && compressBeforeUploadOfPercent == state.getCompressBeforeUploadOfPercent()
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

        this.aliyunOssSetting.apply();
        this.baiduBosSetting.apply();
        this.applyQiniuAuthConfigs(state);
        this.applyTencentAuthConfigs(state);
        this.applyGeneralConfigs(state);
        this.applyClipboardConfigs(state);
        this.applyWeiboAuthConfigs(state);
    }

    /**
     * Apply qiniu auth configs
     *
     * @param state state
     * @since 0.0.1
     */
    private void applyQiniuAuthConfigs(@NotNull MikState state) {
        QiniuOssState qiniuOssState = state.getQiniuOssState();
        // todo-dong4j : (2019年03月19日 21:01) [重构为 domain]
        String endpoint = this.qiniuOssUpHostTextField.getText().trim();
        // todo-dong4j : (2019年03月19日 21:13) [zone]
        String bucketName = this.qiniuOssBucketNameTextField.getText().trim();
        String accessKey = this.qiniuOssAccessKeyTextField.getText().trim();
        String secretKey = new String(this.qiniuOssAccessSecretKeyTextField.getPassword());
        int zoneIndex = Integer.parseInt(this.zoneIndexTextFiled.getText());
        // 需要在加密之前计算 hashcode
        int hashcode = bucketName.hashCode() +
                       accessKey.hashCode() +
                       secretKey.hashCode() +
                       zoneIndex +
                       endpoint.hashCode();
        OssState.saveStatus(qiniuOssState, hashcode, MikState.NEW_HASH_KEY);

        if (StringUtils.isNotBlank(secretKey)) {
            secretKey = DES.encrypt(secretKey, MikState.QINIU);
        }
        qiniuOssState.setBucketName(bucketName);
        qiniuOssState.setAccessKey(accessKey);
        qiniuOssState.setAccessSecretKey(secretKey);
        qiniuOssState.setEndpoint(endpoint);
        qiniuOssState.setZoneIndex(zoneIndex);
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
     * Apply weibo auth configs
     *
     * @param state state
     * @since 0.0.1
     */
    private void applyWeiboAuthConfigs(@NotNull MikState state) {
        WeiboOssState weiboOssState = state.getWeiboOssState();
        // 处理 weibo 保存时的逻辑 (保存之前必须通过测试, 右键菜单才可用)
        String username = this.weiboUserNameTextField.getText().trim();
        String password = new String(this.weiboPasswordField.getPassword());
        // 需要在加密之前计算 hashcode
        int hashcode = username.hashCode() + password.hashCode();
        OssState.saveStatus(weiboOssState, hashcode, MikState.NEW_HASH_KEY);

        if (StringUtils.isNotBlank(password)) {
            password = DES.encrypt(password, MikState.WEIBOKEY);
        }

        weiboOssState.setUserName(username);
        weiboOssState.setPassword(password);
    }

    /**
     * Apply tencent auth configs
     *
     * @param state state
     * @since 0.0.1
     */
    private void applyTencentAuthConfigs(@NotNull MikState state) {
        TencentOssState tencentOssState = state.getTencentOssState();

        String accessKey = this.tencentAccessKeyTextField.getText().trim();
        String secretKey = new String(this.tencentSecretKeyTextField.getPassword());
        String regionName = this.tencentRegionNameTextField.getText().trim();
        String bucketName = this.tencentBacketNameTextField.getText().trim();
        // 需要在加密之前计算 hashcode
        int hashcode = bucketName.hashCode() +
                       accessKey.hashCode() +
                       secretKey.hashCode() +
                       regionName.hashCode();

        OssState.saveStatus(tencentOssState, hashcode, MikState.NEW_HASH_KEY);

        if (StringUtils.isNotBlank(secretKey)) {
            secretKey = DES.encrypt(secretKey, MikState.TENCENT);
        }

        tencentOssState.setAccessKey(accessKey);
        tencentOssState.setSecretKey(secretKey);
        tencentOssState.setRegionName(regionName);
        tencentOssState.setBucketName(bucketName);
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

        this.resetQiniuunConfigs(state);
        this.resetWeiboConfigs(state);
        this.resetTencentConfigs(state);
        this.resetGeneralCOnfigs(state);
        this.resetClipboardConfigs(state);
    }

    /**
     * Reset qiniuun configs
     *
     * @param state state
     * @since 0.0.1
     */
    private void resetQiniuunConfigs(@NotNull MikState state) {
        QiniuOssState qiniuOssState = state.getQiniuOssState();
        this.qiniuOssBucketNameTextField.setText(qiniuOssState.getBucketName());
        this.qiniuOssAccessKeyTextField.setText(qiniuOssState.getAccessKey());
        String accessSecretKey = qiniuOssState.getAccessSecretKey();
        this.qiniuOssAccessSecretKeyTextField.setText(DES.decrypt(accessSecretKey, MikState.QINIU));
        this.qiniuOssUpHostTextField.setText(qiniuOssState.getEndpoint());
        this.zoneIndexTextFiled.setText(String.valueOf(qiniuOssState.getZoneIndex()));
    }

    /**
     * Reset weibo configs
     *
     * @param state state
     * @since 0.0.1
     */
    private void resetWeiboConfigs(@NotNull MikState state) {
        WeiboOssState weiboOssState = state.getWeiboOssState();
        this.weiboUserNameTextField.setText(weiboOssState.getUserName());
        this.weiboPasswordField.setText(DES.decrypt(weiboOssState.getPassword(), MikState.WEIBOKEY));
    }

    /**
     * Reset tencent configs
     *
     * @param state state
     * @since 0.0.1
     */
    private void resetTencentConfigs(@NotNull MikState state) {
        TencentOssState tencentOssState = state.getTencentOssState();
        this.tencentAccessKeyTextField.setText(tencentOssState.getAccessKey());
        this.tencentRegionNameTextField.setText(tencentOssState.getRegionName());
        this.tencentBacketNameTextField.setText(tencentOssState.getBucketName());
        this.tencentSecretKeyTextField.setText(DES.decrypt(tencentOssState.getSecretKey(), MikState.TENCENT));
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
