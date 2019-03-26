/*
 * MIT License
 *
 * Copyright (c) 2019 dong4j <dong4j@gmail.com>
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
 *
 */

package info.dong4j.idea.plugin.settings;

import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.SearchableConfigurable;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.ui.ComponentValidator;
import com.intellij.openapi.ui.ValidationInfo;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.ui.DocumentAdapter;
import com.intellij.ui.JBColor;

import info.dong4j.idea.plugin.client.AliyunOssClient;
import info.dong4j.idea.plugin.client.OssClient;
import info.dong4j.idea.plugin.enums.CloudEnum;
import info.dong4j.idea.plugin.enums.HelpType;
import info.dong4j.idea.plugin.enums.ImageMarkEnum;
import info.dong4j.idea.plugin.enums.ZoneEnum;
import info.dong4j.idea.plugin.strategy.UploadFromTest;
import info.dong4j.idea.plugin.strategy.Uploader;
import info.dong4j.idea.plugin.util.ClientUtils;
import info.dong4j.idea.plugin.util.DES;
import info.dong4j.idea.plugin.util.UploadNotification;

import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

import java.awt.event.ActionListener;
import java.io.*;

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
import javax.swing.event.DocumentEvent;

import lombok.extern.slf4j.Slf4j;

/**
 * <p>Company: 科大讯飞股份有限公司-四川分公司</p>
 * <p>Description: </p>
 *
 * @author dong4j
 * @date 2019 -03-13 12:19
 * @email sjdong3 @iflytek.com
 */
@Slf4j
public class ProjectSettingsPage implements SearchableConfigurable, Configurable.NoScroll {
    private static final String TEST_FILE_NAME = "mik.png";
    private ImageManagerPersistenComponent config;
    private JPanel myMainPanel;

    private JTabbedPane authorizationTabbedPanel;
    /** weiboOssAuthorizationPanel group */
    private JPanel weiboOssAuthorizationPanel;
    private JTextField weiboUserNameTextField;
    private JPasswordField weiboPasswordField;
    private JLabel userNameLabel;
    private JLabel passwordLabel;
    /** aliyunOssAuthorizationPanel group */
    private JPanel aliyunOssAuthorizationPanel;
    private JTextField aliyunOssBucketNameTextField;
    private JTextField aliyunOssAccessKeyTextField;
    private JPasswordField aliyunOssAccessSecretKeyTextField;
    private JTextField aliyunOssEndpointTextField;
    private JTextField aliyunOssFileDirTextField;
    private JTextField exampleTextField;
    /** qiniuOssAuthorizationPanel group */
    private JPanel qiniuOssAuthorizationPanel;
    private JTextField qiniuOssBucketNameTextField;
    private JTextField qiniuOssAccessKeyTextField;
    private JPasswordField qiniuOssAccessSecretKeyTextField;
    private JTextField qiniuOssUpHostTextField;
    private JRadioButton qiniuOssEastChinaRadioButton;
    private JRadioButton qiniuOssNortChinaRadioButton;
    private JRadioButton qiniuOssSouthChinaRadioButton;
    private JRadioButton qiniuOssNorthAmeriaRadioButton;
    private JTextField zoneIndexTextFiled;

    /** globalUploadPanel group */
    private JPanel globalUploadPanel;
    private JCheckBox changeToHtmlTagCheckBox;
    private JRadioButton largePictureRadioButton;
    private JRadioButton commonRadioButton;
    private JLabel commonLabel;
    private JRadioButton customRadioButton;
    private JLabel customLabel;
    private JTextField customHtmlTypeTextField;
    private JCheckBox compressCheckBox;
    private JCheckBox compressBeforeUploadCheckBox;
    private JCheckBox compressAtLookupCheckBox;
    private JSlider compressSlider;
    private JLabel compressLabel;
    private JTextField styleNameTextField;
    private JCheckBox transportCheckBox;
    private JCheckBox backupCheckBox;
    private JCheckBox renameCheckBox;
    private JComboBox fileNameSuffixBoxField;

    /** 按钮 group */
    private JButton testButton;
    private JLabel testMessage;
    private JButton helpButton;

    private JPanel clipboardPanel;
    /** clipboard group */
    private JCheckBox clipboardControlCheckBox;
    private JCheckBox copyToDirCheckBox;
    private JTextField whereToCopyTextField;
    private JCheckBox uploadAndReplaceCheckBox;
    private JComboBox defaultCloudComboBox;

    /** todo-dong4j : (2019年03月20日 13:25) [测试输入验证用] */
    private JTextField myPort;

    public ProjectSettingsPage() {
        log.trace("ProjectSettingsPage Constructor invoke");
        config = ImageManagerPersistenComponent.getInstance();
        if (config != null) {
            reset();
        }
    }

    private void resetAliyunConfigs(@NotNull ImageManagerState state) {
        AliyunOssState aliyunOssState = state.getAliyunOssState();
        this.aliyunOssBucketNameTextField.setText(aliyunOssState.getBucketName());
        this.aliyunOssAccessKeyTextField.setText(aliyunOssState.getAccessKey());
        String aliyunOssAccessSecreKey = aliyunOssState.getAccessSecretKey();
        this.aliyunOssAccessSecretKeyTextField.setText(DES.decrypt(aliyunOssAccessSecreKey, ImageManagerState.ALIYUN));
        this.aliyunOssEndpointTextField.setText(aliyunOssState.getEndpoint());
        this.aliyunOssFileDirTextField.setText(aliyunOssState.getFiledir());
    }

    private void resetQiniuunConfigs(ImageManagerState state) {
        QiniuOssState qiniuOssState = state.getQiniuOssState();
        this.qiniuOssBucketNameTextField.setText(qiniuOssState.getBucketName());
        this.qiniuOssAccessKeyTextField.setText(qiniuOssState.getAccessKey());
        String accessSecretKey = qiniuOssState.getAccessSecretKey();
        this.qiniuOssAccessSecretKeyTextField.setText(DES.decrypt(accessSecretKey, ImageManagerState.QINIU));
        this.qiniuOssUpHostTextField.setText(qiniuOssState.getEndpoint());
        this.zoneIndexTextFiled.setText(String.valueOf(qiniuOssState.getZoneIndex()));
    }

    private void resetWeiboConfigs(@NotNull ImageManagerState state) {
        WeiboOssState weiboOssState = state.getWeiboOssState();
        this.weiboUserNameTextField.setText(weiboOssState.getUserName());
        this.weiboPasswordField.setText(DES.decrypt(weiboOssState.getPassword(), ImageManagerState.WEIBOKEY));
    }

    private void resetGeneralCOnfigs(ImageManagerState state) {
        this.changeToHtmlTagCheckBox.setSelected(state.isChangeToHtmlTag());
        this.largePictureRadioButton.setSelected(state.getTagType().equals(ImageMarkEnum.LARGE_PICTURE.text));
        this.commonRadioButton.setSelected(state.getTagType().equals(ImageMarkEnum.CUSTOM.text));
        this.customRadioButton.setSelected(state.getTagType().equals(ImageMarkEnum.CUSTOM.text));
        this.customHtmlTypeTextField.setText(state.getTagTypeCode());
        this.compressCheckBox.setSelected(state.isCompress());
        this.compressBeforeUploadCheckBox.setSelected(state.isCompressBeforeUpload());
        this.compressAtLookupCheckBox.setSelected(state.isCompressAtLookup());
        this.compressSlider.setValue(state.getCompressBeforeUploadOfPercent());
        this.compressLabel.setText(String.valueOf(compressSlider.getValue()));
        this.styleNameTextField.setText(state.getStyleName());
        this.transportCheckBox.setSelected(state.isTransport());
        this.backupCheckBox.setSelected(state.isBackup());
        this.renameCheckBox.setSelected(state.isRename());
        this.fileNameSuffixBoxField.setSelectedIndex(state.getSuffixIndex());
    }

    private void resetClipboardConfigs(ImageManagerState state) {
        this.clipboardControlCheckBox.setSelected(state.isClipboardControl());
        this.copyToDirCheckBox.setSelected(state.isCopyToDir());
        this.whereToCopyTextField.setText(state.getImageSavePath());
        this.uploadAndReplaceCheckBox.setSelected(state.isUploadAndReplace());
        this.defaultCloudComboBox.setSelectedIndex(state.getCloudType());
    }

    @NotNull
    @Override
    public String getId() {
        return getDisplayName();
    }

    @Nls
    @Override
    public String getDisplayName() {
        return "Markdown Image Kit";
    }

    @Override
    public JComponent createComponent() {
        initFromSettings();
        return myMainPanel;
    }

    /**
     * 每次打开设置面板时执行
     */
    private void initFromSettings() {
        ImageManagerState state = config.getState();
        initAuthorizationTabbedPanel(state);
        initGlobalPanel(state);
        initClipboardControl();

        String MESSAGE = "The port number should be between 0 and 65535.";

        // Components initialization
        new ComponentValidator(ProjectManager.getInstance().getDefaultProject()).withValidator(v -> {
            String pt = myPort.getText();
            if (StringUtil.isNotEmpty(pt)) {
                try {
                    int portValue = Integer.parseInt(pt);
                    if (portValue >= 0 && portValue <= 65535) {
                        v.updateInfo(null);
                    } else {
                        v.updateInfo(new ValidationInfo(MESSAGE, myPort));
                    }
                } catch (NumberFormatException nfe) {
                    v.updateInfo(new ValidationInfo(MESSAGE, myPort));
                }
            } else {
                v.updateInfo(null);
            }
        }).installOn(myPort);

        myPort.getDocument().addDocumentListener(new DocumentAdapter() {
            @Override
            protected void textChanged(@NotNull DocumentEvent e) {
                ComponentValidator.getInstance(myPort).ifPresent(ComponentValidator::revalidate);
            }
        });
    }

    /**
     * 初始化 authorizationTabbedPanel group
     */
    private void initAuthorizationTabbedPanel(ImageManagerState state) {
        // 打开设置页时默认选中默认上传图床
        authorizationTabbedPanel.setSelectedIndex(state.getCloudType());
        authorizationTabbedPanel.addChangeListener(e -> {
            // 清理 test 信息
            testMessage.setText("");
            testButton.setText("Test Upload");
            // 获得指定索引的选项卡标签
            log.trace("change {}", authorizationTabbedPanel.getTitleAt(authorizationTabbedPanel.getSelectedIndex()));
        });

        initAliyunOssAuthenticationPanel();
        initWeiboOssAuthenticationPanel();
        initQiniuOssAuthenticationPanel(state);
        testAndHelpListener();
    }

    /**
     * 初始化 upload 配置组
     */
    private void initGlobalPanel(@NotNull ImageManagerState state) {
        initChangeToHtmlGroup();
        initCompressGroup();
        initExpandGroup();

        // 初始化上传图片的后缀
        renameCheckBox.setSelected(config.getState().isRename());
        fileNameSuffixBoxField.setSelectedIndex(state.getSuffixIndex());
        fileNameSuffixBoxField.setEnabled(config.getState().isRename());
        renameCheckBox.addChangeListener(e -> {
            JCheckBox checkBox = (JCheckBox) e.getSource();
            fileNameSuffixBoxField.setEnabled(checkBox.isSelected());
        });
    }

    /**
     * 初始化 clipboard group
     */
    private void initClipboardControl() {
        // 设置是否勾选
        boolean isClipboardControl = config.getState().isClipboardControl();
        boolean isCopyToDir = config.getState().isCopyToDir();
        boolean isUploadAndReplace = config.getState().isUploadAndReplace();
        this.clipboardControlCheckBox.setSelected(isClipboardControl);
        this.copyToDirCheckBox.setSelected(isCopyToDir);
        this.uploadAndReplaceCheckBox.setSelected(isUploadAndReplace);

        // 设置是否可用
        this.copyToDirCheckBox.setEnabled(isClipboardControl);
        this.uploadAndReplaceCheckBox.setEnabled(isClipboardControl);
        // 设置 copy 位置
        this.whereToCopyTextField.setText(config.getState().getImageSavePath());
        this.whereToCopyTextField.setEnabled(isClipboardControl && isCopyToDir);
        // 默认上传图床
        this.defaultCloudComboBox.setEnabled(isUploadAndReplace && isClipboardControl);
        this.defaultCloudComboBox.setSelectedIndex(config.getState().getCloudType());

        // 设置 clipboardControlCheckBox 监听
        clipboardControlCheckBox.addChangeListener(e -> {
            JCheckBox checkBox = (JCheckBox) e.getSource();
            copyToDirCheckBox.setEnabled(checkBox.isSelected());
            uploadAndReplaceCheckBox.setEnabled(checkBox.isSelected());
            // 如果都被选中才设置为可用
            whereToCopyTextField.setEnabled(copyToDirCheckBox.isSelected() && checkBox.isSelected());
            defaultCloudComboBox.setEnabled(uploadAndReplaceCheckBox.isSelected() && checkBox.isSelected());
        });

        // 设置 copyToDirCheckBox 监听
        copyToDirCheckBox.addChangeListener(e -> {
            JCheckBox checkBox = (JCheckBox) e.getSource();
            whereToCopyTextField.setEnabled(checkBox.isSelected());
        });

        // 设置 uploadAndReplaceCheckBox 监听
        uploadAndReplaceCheckBox.addChangeListener(e -> {
            JCheckBox checkBox = (JCheckBox) e.getSource();
            defaultCloudComboBox.setEnabled(checkBox.isSelected());
        });
    }

    /**
     * 初始化 aliyun oss 认证相关设置
     */
    private void initAliyunOssAuthenticationPanel() {
        String aliyunOssAccessSecretKey = config.getState().getAliyunOssState().getAccessSecretKey();
        aliyunOssAccessSecretKeyTextField.setText(DES.decrypt(aliyunOssAccessSecretKey, ImageManagerState.ALIYUN));

        // 处理当 aliyunOssFileDirTextField.getText() 为 空字符时, 不拼接 "/
        setExampleText();

        // 监听 aliyunOssBucketNameTextField
        aliyunOssBucketNameTextField.getDocument().addDocumentListener(new DocumentAdapter() {
            @Override
            protected void textChanged(@NotNull DocumentEvent e) {
                setExampleText();
            }
        });

        // 监听 aliyunOssEndpointTextField
        aliyunOssEndpointTextField.getDocument().addDocumentListener(new DocumentAdapter() {
            @Override
            protected void textChanged(@NotNull DocumentEvent e) {
                setExampleText();
            }
        });

        // 设置 aliyunOssFileDirTextField 输入的监听
        aliyunOssFileDirTextField.getDocument().addDocumentListener(new DocumentAdapter() {
            @Override
            protected void textChanged(@NotNull DocumentEvent e) {
                setExampleText();
            }
        });
    }

    /**
     * 初始化 weibo oss 认证相关设置
     */
    private void initWeiboOssAuthenticationPanel() {
        weiboUserNameTextField.setText(config.getState().getWeiboOssState().getUserName());
        weiboPasswordField.setText(DES.decrypt(config.getState().getWeiboOssState().getPassword(), ImageManagerState.WEIBOKEY));
    }

    /**
     * 初始化 qiniu oss 认证相关设置
     */
    private void initQiniuOssAuthenticationPanel(ImageManagerState state) {
        QiniuOssState qiniuOssState = state.getQiniuOssState();
        qiniuOssAccessSecretKeyTextField.setText(DES.decrypt(qiniuOssState.getAccessSecretKey(), ImageManagerState.QINIU));

        qiniuOssUpHostTextField.addFocusListener(new JTextFieldHintListener(qiniuOssUpHostTextField, "http(s)://domain/"));

        ButtonGroup group = new ButtonGroup();
        qiniuOssEastChinaRadioButton.setMnemonic(ZoneEnum.EAST_CHINA.index);
        qiniuOssNortChinaRadioButton.setMnemonic(ZoneEnum.NORT_CHINA.index);
        qiniuOssSouthChinaRadioButton.setMnemonic(ZoneEnum.SOUTH_CHINA.index);
        qiniuOssNorthAmeriaRadioButton.setMnemonic(ZoneEnum.NORTH_AMERIA.index);
        addZoneRadioButton(group, qiniuOssEastChinaRadioButton);
        addZoneRadioButton(group, qiniuOssNortChinaRadioButton);
        addZoneRadioButton(group, qiniuOssSouthChinaRadioButton);
        addZoneRadioButton(group, qiniuOssNorthAmeriaRadioButton);

        qiniuOssEastChinaRadioButton.setSelected(qiniuOssState.getZoneIndex() == qiniuOssEastChinaRadioButton.getMnemonic());
        qiniuOssNortChinaRadioButton.setSelected(qiniuOssState.getZoneIndex() == qiniuOssNortChinaRadioButton.getMnemonic());
        qiniuOssSouthChinaRadioButton.setSelected(qiniuOssState.getZoneIndex() == qiniuOssSouthChinaRadioButton.getMnemonic());
        qiniuOssNorthAmeriaRadioButton.setSelected(qiniuOssState.getZoneIndex() == qiniuOssNorthAmeriaRadioButton.getMnemonic());
    }

    /**
     * 添加 test 和 help 按钮监听, 根据选中的图床进行测试
     */
    public void testAndHelpListener() {
        // "Test" 按钮点击事件处理
        testButton.addActionListener(e -> {
            int index = authorizationTabbedPanel.getSelectedIndex();
            InputStream inputStream = this.getClass().getResourceAsStream("/" + TEST_FILE_NAME);
            CloudEnum cloudEnum = OssState.getCloudType(index);
            OssClient client = ClientUtils.getInstance(cloudEnum);
            if(client != null){
                String url = Uploader.getInstance().setUploadWay(new UploadFromTest(client,
                                                                                    inputStream,
                                                                                    TEST_FILE_NAME,
                                                                                    (JPanel) authorizationTabbedPanel.getComponentAt(index))).upload();
                if (StringUtils.isNotBlank(url)) {
                    testMessage.setForeground(JBColor.GREEN);
                    testMessage.setText("Upload Succeed");
                    testButton.setText("Test Upload");
                    if (log.isTraceEnabled()) {
                        BrowserUtil.browse(url);
                    }
                } else {
                    testButton.setText("Try Again");
                    testMessage.setForeground(JBColor.RED);
                    testMessage.setText("Upload Failed, Please Check The Configuration");
                }
            }
        });

        // help button 监听
        helpButton.addActionListener(e -> {
            // 打开浏览器到帮助页面
            String url = UploadNotification.helpUrl(HelpType.SETTING.where);
            BrowserUtil.browse(url);
        });
    }

    /**
     * 初始化替换标签设置组
     */
    private void initChangeToHtmlGroup() {
        customHtmlTypeTextField.addFocusListener(new JTextFieldHintListener(customHtmlTypeTextField, "格式: <a title='${}' href='${}' >![${}](${})</a>"));

        // 初始化 changeToHtmlTagCheckBox 选中状态
        // 设置被选中
        boolean changeToHtmlTagCheckBoxStatus = config.getState().isChangeToHtmlTag();

        this.changeToHtmlTagCheckBox.setSelected(changeToHtmlTagCheckBoxStatus);
        // 设置组下单选框可用
        this.largePictureRadioButton.setEnabled(changeToHtmlTagCheckBoxStatus);
        this.commonRadioButton.setEnabled(changeToHtmlTagCheckBoxStatus);
        this.customRadioButton.setEnabled(changeToHtmlTagCheckBoxStatus);

        // 初始化 changeToHtmlTagCheckBox 组下单选框状态
        if (ImageMarkEnum.COMMON_PICTURE.text.equals(config.getState().getTagType())) {
            commonRadioButton.setSelected(true);
        } else if (ImageMarkEnum.LARGE_PICTURE.text.equals(config.getState().getTagType())) {
            largePictureRadioButton.setSelected(true);
        } else if (ImageMarkEnum.CUSTOM.text.equals(config.getState().getTagType())) {
            customRadioButton.setSelected(true);
            customHtmlTypeTextField.setEnabled(changeToHtmlTagCheckBoxStatus);
            customHtmlTypeTextField.setText(config.getState().getTagTypeCode());
        } else {
            commonRadioButton.setSelected(true);
        }

        // changeToHtmlTagCheckBox 监听, 修改组下组件状态
        changeToHtmlTagCheckBox.addChangeListener(e -> {
            JCheckBox checkBox = (JCheckBox) e.getSource();
            largePictureRadioButton.setEnabled(checkBox.isSelected());
            commonRadioButton.setEnabled(checkBox.isSelected());
            customRadioButton.setEnabled(checkBox.isSelected());
            // 如果原来自定义选项被选中, 则将输入框设置为可用
            if (customRadioButton.isSelected() && checkBox.isSelected()) {
                customHtmlTypeTextField.setEnabled(true);
            }
        });
        ButtonGroup group = new ButtonGroup();
        addRadioButton(group, largePictureRadioButton);
        addRadioButton(group, commonRadioButton);
        addRadioButton(group, customRadioButton);
    }

    /**
     * 初始化图片压缩配置组
     */
    private void initCompressGroup() {
        styleNameTextField.addFocusListener(new JTextFieldHintListener(styleNameTextField, "请提前在 Aliyun OSS 控制台设置"));

        boolean compressStatus = config.getState().isCompress();
        boolean beforeCompressStatus = config.getState().isCompressBeforeUpload();
        boolean lookUpCompressStatus = config.getState().isCompressAtLookup();
        // 设置被选中
        this.compressCheckBox.setSelected(compressStatus);
        // 设置组下多选框状态
        this.compressBeforeUploadCheckBox.setEnabled(compressStatus);
        this.compressBeforeUploadCheckBox.setSelected(beforeCompressStatus);
        this.compressAtLookupCheckBox.setEnabled(compressStatus);
        this.compressAtLookupCheckBox.setSelected(lookUpCompressStatus);

        this.compressSlider.setEnabled(compressStatus && beforeCompressStatus);
        this.compressSlider.setValue(config.getState().getCompressBeforeUploadOfPercent());

        // 设置主刻度间隔
        compressSlider.setMajorTickSpacing(10);
        // 设置次刻度间隔
        compressSlider.setMinorTickSpacing(2);
        // 绘制 刻度 和 标签
        compressSlider.setPaintTicks(true);
        compressSlider.setPaintLabels(true);
        compressSlider.addChangeListener(e -> compressLabel.setText(String.valueOf(compressSlider.getValue())));

        this.compressLabel.setText(String.valueOf(compressSlider.getValue()));
        this.styleNameTextField.setEnabled(compressStatus && lookUpCompressStatus);
        this.styleNameTextField.setText(config.getState().getStyleName());

        // compressCheckBox 监听, 修改组下组件状态
        compressCheckBox.addChangeListener(e -> {
            JCheckBox checkBox = (JCheckBox) e.getSource();
            if (checkBox.isSelected()) {
                compressBeforeUploadCheckBox.setEnabled(true);
                compressAtLookupCheckBox.setEnabled(true);
            } else {
                compressBeforeUploadCheckBox.setEnabled(false);
                compressAtLookupCheckBox.setEnabled(false);
                compressSlider.setEnabled(false);
                styleNameTextField.setEnabled(false);
            }
        });

        compressBeforeUploadCheckBox.addChangeListener(e -> {
            compressSlider.setEnabled(compressBeforeUploadCheckBox.isSelected());
        });
        compressAtLookupCheckBox.addChangeListener(e -> {
            styleNameTextField.setEnabled(compressAtLookupCheckBox.isSelected());
        });
    }

    /**
     * 初始化图片备份和图床迁移
     */
    private void initExpandGroup() {
        // todo-dong4j : (2019年03月15日 20:52) [删除此设置, 使用 MoveToOtherStorageAction 替代]
        this.transportCheckBox.setSelected(config.getState().isTransport());
        this.backupCheckBox.setSelected(config.getState().isBackup());
    }

    /**
     * 实时更新此字段
     */
    private void setExampleText() {
        String fileDir = StringUtils.isBlank(aliyunOssFileDirTextField.getText().trim()) ? "" : "/" + aliyunOssFileDirTextField.getText().trim();
        String endpoint = aliyunOssEndpointTextField.getText().trim();
        String backetName = aliyunOssBucketNameTextField.getText().trim();
        String url = AliyunOssClient.URL_PROTOCOL_HTTPS + "://" + backetName + "." + endpoint;
        exampleTextField.setText(url + fileDir + "/" + TEST_FILE_NAME);
    }

    /**
     * 处理被选中的 zone 单选框
     *
     * @param group  the group
     * @param button the button
     */
    private void addZoneRadioButton(ButtonGroup group, JRadioButton button) {
        group.add(button);
        ActionListener actionListener = e -> {
            Object sourceObject = e.getSource();
            if (sourceObject instanceof JRadioButton) {
                JRadioButton sourceButton = (JRadioButton) sourceObject;
                zoneIndexTextFiled.setText(String.valueOf(sourceButton.getMnemonic()));
                testMessage.setText("");
                testButton.setText("Test Upload");
            }
        };
        button.addActionListener(actionListener);
    }

    /**
     * 处理被选中的单选框
     *
     * @param group  the group
     * @param button the button
     */
    private void addRadioButton(ButtonGroup group, JRadioButton button) {
        group.add(button);
        // 构造一个监听器，响应checkBox事件
        ActionListener actionListener = e -> {
            Object sourceObject = e.getSource();
            if (sourceObject instanceof JRadioButton) {
                JRadioButton sourceButton = (JRadioButton) sourceObject;
                if (ImageMarkEnum.CUSTOM.text.equals(sourceButton.getText())) {
                    customHtmlTypeTextField.setEnabled(true);
                } else {
                    customHtmlTypeTextField.setEnabled(false);
                }
            }
        };
        button.addActionListener(actionListener);
    }

    /**
     * 判断 GUI 是否有变化
     *
     * @return the boolean
     */
    @Override
    public boolean isModified() {
        log.trace("isModified invoke");
        ImageManagerState state = config.getState();
        return !(isAliyunAuthModified(state)
                 && isWeiboAuthModified(state)
                 && isQiniuAuthModified(state)
                 && isGeneralModified(state)
                 && isClipboardModified(state)
        );
    }

    private boolean isAliyunAuthModified(@NotNull ImageManagerState state) {
        AliyunOssState aliyunOssState = state.getAliyunOssState();
        String bucketName = aliyunOssBucketNameTextField.getText().trim();
        String accessKey = aliyunOssAccessKeyTextField.getText().trim();
        String secretKey = new String(aliyunOssAccessSecretKeyTextField.getPassword());
        if (StringUtils.isNotBlank(secretKey)) {
            secretKey = DES.encrypt(secretKey, ImageManagerState.ALIYUN);
        }
        String endpoint = aliyunOssEndpointTextField.getText().trim();
        String filedir = aliyunOssFileDirTextField.getText().trim();

        return bucketName.equals(aliyunOssState.getBucketName())
               && accessKey.equals(aliyunOssState.getAccessKey())
               && secretKey.equals(aliyunOssState.getAccessSecretKey())
               && endpoint.equals(aliyunOssState.getEndpoint())
               && filedir.equals(aliyunOssState.getFiledir());
    }

    private boolean isWeiboAuthModified(@NotNull ImageManagerState state) {
        WeiboOssState weiboOssState = state.getWeiboOssState();
        String weiboUsername = weiboUserNameTextField.getText().trim();
        String weiboPassword = new String(weiboPasswordField.getPassword());
        if (StringUtils.isNotBlank(weiboPassword)) {
            weiboPassword = DES.encrypt(weiboPassword, ImageManagerState.WEIBOKEY);
        }
        return weiboUsername.equals(weiboOssState.getUserName())
               && weiboPassword.equals(weiboOssState.getPassword());
    }

    private boolean isQiniuAuthModified(ImageManagerState state) {
        QiniuOssState qiniuOssState = state.getQiniuOssState();
        String bucketName = qiniuOssBucketNameTextField.getText().trim();
        String accessKey = qiniuOssAccessKeyTextField.getText().trim();
        String secretKey = new String(qiniuOssAccessSecretKeyTextField.getPassword());
        if (StringUtils.isNotBlank(secretKey)) {
            secretKey = DES.encrypt(secretKey, ImageManagerState.QINIU);
        }
        // todo-dong4j : (2019年03月19日 21:01) [重构为 domain]
        String endpoint = qiniuOssUpHostTextField.getText().trim();
        // todo-dong4j : (2019年03月19日 21:13) [zone]
        int zoneIndex = Integer.parseInt(zoneIndexTextFiled.getText());

        return bucketName.equals(qiniuOssState.getBucketName())
               && accessKey.equals(qiniuOssState.getAccessKey())
               && secretKey.equals(qiniuOssState.getAccessSecretKey())
               && zoneIndex == qiniuOssState.getZoneIndex()
               && endpoint.equals(qiniuOssState.getEndpoint());
    }

    private boolean isGeneralModified(ImageManagerState state) {
        // 是否替换标签
        boolean changeToHtmlTag = changeToHtmlTagCheckBox.isSelected();
        // 替换的标签类型
        String tagType = "";
        // 替换的标签类型 code
        String tagTypeCode = "";
        if (changeToHtmlTag) {
            // 正常的
            if (commonRadioButton.isSelected()) {
                tagType = ImageMarkEnum.COMMON_PICTURE.text;
                tagTypeCode = ImageMarkEnum.COMMON_PICTURE.code;
            }
            // 点击看大图
            else if (largePictureRadioButton.isSelected()) {
                tagType = ImageMarkEnum.LARGE_PICTURE.text;
                tagTypeCode = ImageMarkEnum.LARGE_PICTURE.code;
            }
            // 自定义
            else if (customRadioButton.isSelected()) {
                tagType = ImageMarkEnum.CUSTOM.text;
                // todo-dong4j : (2019年03月14日 14:30) [格式验证]
                tagTypeCode = customHtmlTypeTextField.getText().trim();
            }
        }

        // 是否压缩图片
        boolean compress = compressCheckBox.isSelected();
        // 上传前压缩
        boolean compressBeforeUpload = compressBeforeUploadCheckBox.isSelected();
        // 压缩比例
        int compressBeforeUploadOfPercent = compressSlider.getValue();
        // 查看时压缩
        boolean compressAtLookup = compressAtLookupCheckBox.isSelected();
        // Aliyun OSS 图片压缩配置
        String styleName = "";
        if (compressAtLookup) {
            styleName = styleNameTextField.getText().trim();
        }
        // 图床迁移
        boolean transport = transportCheckBox.isSelected();
        // 图片备份
        boolean backup = backupCheckBox.isSelected();

        boolean isRename = renameCheckBox.isSelected();
        // 图片后缀
        int index = fileNameSuffixBoxField.getSelectedIndex();

        return changeToHtmlTag == state.isChangeToHtmlTag()
               && tagType.equals(state.getTagType())
               && tagTypeCode.equals(state.getTagTypeCode())
               && compress == state.isCompress()
               && compressBeforeUpload == state.isCompressBeforeUpload()
               && compressBeforeUploadOfPercent == state.getCompressBeforeUploadOfPercent()
               && compressAtLookup == state.isCompressAtLookup()
               && styleName.equals(state.getStyleName())
               && transport == state.isTransport()
               && backup == state.isBackup()
               && isRename == state.isRename()
               && index == state.getSuffixIndex();
    }

    private boolean isClipboardModified(@NotNull ImageManagerState state) {
        boolean clipboardControl = clipboardControlCheckBox.isSelected();
        boolean copyToDir = copyToDirCheckBox.isSelected();
        boolean uploadAndReplace = uploadAndReplaceCheckBox.isSelected();
        String whereToCopy = whereToCopyTextField.getText().trim();

        return clipboardControl == state.isClipboardControl()
               && copyToDir == state.isCopyToDir()
               && uploadAndReplace == state.isUploadAndReplace()
               && whereToCopy.equals(state.getImageSavePath())
               && defaultCloudComboBox.getSelectedIndex() == state.getCloudType();
    }

    /**
     * 配置被修改后时被调用, 修改 state 中的数据
     */
    @Override
    public void apply() {
        log.trace("apply invoke");
        ImageManagerState state = config.getState();
        applyAliyunAuthConfigs(state);
        applyQiniuAuthConfigs(state);
        applyGeneralConfigs(state);
        applyClipboardConfigs(state);
        applyWeiboAuthConfigs(state);
    }

    private void applyAliyunAuthConfigs(ImageManagerState state) {
        AliyunOssState aliyunOssState = state.getAliyunOssState();
        String bucketName = this.aliyunOssBucketNameTextField.getText().trim();
        String accessKey = this.aliyunOssAccessKeyTextField.getText().trim();
        String accessSecretKey = new String(aliyunOssAccessSecretKeyTextField.getPassword());
        String endpoint = this.aliyunOssEndpointTextField.getText().trim();
        // 需要在加密之前计算 hashcode
        int hashcode = bucketName.hashCode() +
                       accessKey.hashCode() +
                       accessSecretKey.hashCode() +
                       endpoint.hashCode();
        OssState.saveStatus(aliyunOssState, hashcode, ImageManagerState.NEW_HASH_KEY);

        if (StringUtils.isNotBlank(accessSecretKey)) {
            accessSecretKey = DES.encrypt(accessSecretKey, ImageManagerState.ALIYUN);
        }

        aliyunOssState.setBucketName(bucketName);
        aliyunOssState.setAccessKey(accessKey);
        aliyunOssState.setAccessSecretKey(accessSecretKey);
        aliyunOssState.setEndpoint(endpoint);
        aliyunOssState.setFiledir(this.aliyunOssFileDirTextField.getText().trim());
    }

    private void applyQiniuAuthConfigs(ImageManagerState state) {
        QiniuOssState qiniuOssState = state.getQiniuOssState();
        // todo-dong4j : (2019年03月19日 21:01) [重构为 domain]
        String endpoint = qiniuOssUpHostTextField.getText().trim();
        // todo-dong4j : (2019年03月19日 21:13) [zone]
        String bucketName = qiniuOssBucketNameTextField.getText().trim();
        String accessKey = qiniuOssAccessKeyTextField.getText().trim();
        String secretKey = new String(qiniuOssAccessSecretKeyTextField.getPassword());
        int zoneIndex = Integer.parseInt(zoneIndexTextFiled.getText());
        // 需要在加密之前计算 hashcode
        int hashcode = bucketName.hashCode() +
                       accessKey.hashCode() +
                       secretKey.hashCode() +
                       zoneIndex +
                       endpoint.hashCode();
        OssState.saveStatus(qiniuOssState, hashcode, ImageManagerState.NEW_HASH_KEY);

        if (StringUtils.isNotBlank(secretKey)) {
            secretKey = DES.encrypt(secretKey, ImageManagerState.QINIU);
        }
        qiniuOssState.setBucketName(bucketName);
        qiniuOssState.setAccessKey(accessKey);
        qiniuOssState.setAccessSecretKey(secretKey);
        qiniuOssState.setEndpoint(endpoint);
        qiniuOssState.setZoneIndex(zoneIndex);
    }

    private void applyGeneralConfigs(ImageManagerState state) {
        state.setChangeToHtmlTag(this.changeToHtmlTagCheckBox.isSelected());
        if (this.changeToHtmlTagCheckBox.isSelected()) {
            // 正常的
            if (commonRadioButton.isSelected()) {
                state.setTagType(ImageMarkEnum.COMMON_PICTURE.text);
                state.setTagTypeCode(ImageMarkEnum.COMMON_PICTURE.code);
            }
            // 点击看大图
            else if (largePictureRadioButton.isSelected()) {
                state.setTagType(ImageMarkEnum.LARGE_PICTURE.text);
                state.setTagTypeCode(ImageMarkEnum.LARGE_PICTURE.code);
            }
            // 自定义
            else if (customRadioButton.isSelected()) {
                state.setTagType(ImageMarkEnum.CUSTOM.text);
                // todo-dong4j : (2019年03月14日 14:30) [格式验证]
                state.setTagTypeCode(customHtmlTypeTextField.getText().trim());
            }
        }
        state.setCompress(this.compressCheckBox.isSelected());
        state.setCompressBeforeUpload(this.compressBeforeUploadCheckBox.isSelected());
        state.setCompressBeforeUploadOfPercent(this.compressSlider.getValue());
        state.setCompressAtLookup(this.compressAtLookupCheckBox.isSelected());
        state.setStyleName(this.styleNameTextField.getText().trim());
        state.setTransport(this.transportCheckBox.isSelected());
        state.setBackup(this.backupCheckBox.isSelected());
        state.setRename(renameCheckBox.isSelected());
        state.setSuffixIndex(fileNameSuffixBoxField.getSelectedIndex());
    }

    private void applyClipboardConfigs(ImageManagerState state) {
        state.setClipboardControl(this.clipboardControlCheckBox.isSelected());
        state.setCopyToDir(this.copyToDirCheckBox.isSelected());
        state.setUploadAndReplace(this.uploadAndReplaceCheckBox.isSelected());
        state.setImageSavePath(this.whereToCopyTextField.getText().trim());
        state.setCloudType(this.defaultCloudComboBox.getSelectedIndex());
    }

    private void applyWeiboAuthConfigs(ImageManagerState state) {
        WeiboOssState weiboOssState = state.getWeiboOssState();
        // 处理 weibo 保存时的逻辑 (保存之前必须通过测试, 右键菜单才可用)
        String username = this.weiboUserNameTextField.getText().trim();
        String password = new String(weiboPasswordField.getPassword());
        // 需要在加密之前计算 hashcode
        int hashcode = username.hashCode() + password.hashCode();
        OssState.saveStatus(weiboOssState, hashcode, ImageManagerState.NEW_HASH_KEY);

        if (StringUtils.isNotBlank(password)) {
            password = DES.encrypt(password, ImageManagerState.WEIBOKEY);
        }

        weiboOssState.setUserName(username);
        weiboOssState.setPassword(password);
    }

    /**
     * 撤回是调用
     */
    @Override
    public void reset() {
        log.trace("reset invoke");
        ImageManagerState state = config.getState();
        resetAliyunConfigs(state);
        resetQiniuunConfigs(state);
        resetWeiboConfigs(state);
        resetGeneralCOnfigs(state);
        resetClipboardConfigs(state);
    }
}
