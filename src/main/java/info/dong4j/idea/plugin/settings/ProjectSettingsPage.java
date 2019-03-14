package info.dong4j.idea.plugin.settings;

import com.aliyun.oss.OSSClient;
import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.SearchableConfigurable;
import com.intellij.ui.DocumentAdapter;
import com.intellij.ui.JBColor;

import info.dong4j.idea.plugin.enums.HtmlTagTypeEnum;
import info.dong4j.idea.plugin.enums.SuffixSelectTypeEnum;
import info.dong4j.idea.plugin.util.EnumsUtils;
import info.dong4j.idea.plugin.util.UploadUtils;

import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

import java.awt.event.ActionListener;
import java.io.*;
import java.util.Objects;
import java.util.Optional;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSlider;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.event.ChangeListener;
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

    private JPanel myGeneralPanel;

    private JPanel aliyunOssAuthorizationPanel;
    private JPanel myUploadPanel;

    private JTextField bucketNameTextField;
    private JTextField accessKeyTextField;
    private JTextField accessSecretKeyTextField;
    private JTextField endpointTextField;
    private JTextField fileDirTextField;
    private JComboBox suffixBoxField;
    private JButton testButton;
    private JButton helpButton;

    private JTextField exampleTextField;
    private JLabel message;
    private JCheckBox changeToHtmlTagCheckBox;
    private JRadioButton largePictureRadioButton;
    private JRadioButton commonRadioButton;
    private JRadioButton customRadioButton;
    private JTextField customHtmlTypeTextField;
    private JTextField commontextField;
    private JTextField largePicturetextField;
    private JCheckBox compressCheckBox;
    private JCheckBox compressBeforeUploadCheckBox;
    private JCheckBox compressAtLookupCheckBox;
    private JSlider compressSlider;
    private JTextField styleNameTextField;
    private JCheckBox transportCheckBox;
    private JCheckBox backupCheckBox;
    private JTabbedPane authorizationTabbedPanel;
    private JPanel weiboOssAuthorizationPanel;
    private JPanel qiniuOssAuthorizationPanel;

    private AliyunOssSettings aliyunOssSettings;

    /**
     * Instantiates a new Project settings page.
     */
    public ProjectSettingsPage() {
        log.trace("ProjectSettingsPage Constructor invoke");
        aliyunOssSettings = AliyunOssSettings.getInstance();
        if (aliyunOssSettings != null) {
            reset();
        }
    }

    @Nls
    @Override
    public String getDisplayName() {
        log.trace("get plugin setting DisplayName");
        return "Aliyun OSS Settings";
    }

    @Override
    public JComponent createComponent() {
        log.trace("createComponent");
        initFromSettings();
        return myGeneralPanel;
    }

    /**
     * 每次打开设置面板时执行
     */
    private void initFromSettings() {
        initAuthenticationPanel();
        initUploadPanel();
    }

    /**
     * 初始化认证相关设置
     */
    private void initAuthenticationPanel() {
        // 根据持久化配置设置为被选中的 item
        suffixBoxField.setSelectedItem(aliyunOssSettings.getState().getSuffix());
        // 处理当 fileDirTextField.getText() 为 空字符时, 不拼接 "/
        setExampleText();

        endpointTextField.getDocument().addDocumentListener(new DocumentAdapter() {
            @Override
            protected void textChanged(@NotNull DocumentEvent e) {
                setExampleText();
            }
        });

        // 设置 fileDirTextField 输入的监听
        fileDirTextField.getDocument().addDocumentListener(new DocumentAdapter() {
            @Override
            protected void textChanged(@NotNull DocumentEvent e) {
                setExampleText();
            }
        });

        // 添加监听器, 当选项被修改后, 修改 exampleTextField 中的 text
        suffixBoxField.addItemListener(e -> {
            setExampleText();
        });

        // "Test" 按钮点击事件处理
        testButton.addActionListener(e -> {
            // 获取输入框文本, 进行请求处理
            String tempBucketName = bucketNameTextField.getText().trim();
            String tempAccessKey = accessKeyTextField.getText().trim();
            String tempAccessSecretKey = accessSecretKeyTextField.getText().trim();
            String tempEndpoint = endpointTextField.getText().trim().replace(tempBucketName + ".", "");
            String tempFileDir = fileDirTextField.getText().trim();
            tempFileDir = StringUtils.isBlank(tempFileDir) ? "" : tempFileDir + "/";
            OSSClient ossClient = null;
            try {
                ossClient = new OSSClient(tempEndpoint, tempAccessKey, tempAccessSecretKey);
                // 返回读取指定资源的输入流
                InputStream is = this.getClass().getResourceAsStream("/test.png");
                UploadUtils.uploadFile2OSS(ossClient, is, tempFileDir, "test.png");
                UploadUtils.getUrl(tempFileDir, "test.png");
                message.setText("test succeed");
                message.setForeground(JBColor.GREEN);
            } catch (Exception e1) {
                message.setText(e1.getMessage());
                message.setForeground(JBColor.RED);
            } finally {
                if (ossClient != null) {
                    ossClient.shutdown();
                }
            }
        });

        // help button 监听
        helpButton.addActionListener(e -> {
            // 打开浏览器到帮助页面
            String url = "http://dong4j.info";
            BrowserUtil.browse(url);
        });
    }

    /**
     * 实时更新此字段
     */
    private void setExampleText() {
        String fileDir = StringUtils.isBlank(fileDirTextField.getText()) ? "" : "/" + fileDirTextField.getText();
        exampleTextField.setText(endpointTextField.getText() + fileDir + getSufixString(Objects.requireNonNull(suffixBoxField.getSelectedItem()).toString()));
    }

    /**
     * 初始化 upload 配置组
     */
    private void initUploadPanel() {
        initChangeToHtmlGroup();
        initCompressGroup();
        initExpandGroup();
    }

    /**
     * 初始化图片备份和图床迁移
     */
    private void initExpandGroup() {
        this.transportCheckBox.setSelected(aliyunOssSettings.getState().isTransport());
        this.backupCheckBox.setSelected(aliyunOssSettings.getState().isBackup());
    }

    /**
     * 初始化图片压缩配置组
     */
    private void initCompressGroup() {
        styleNameTextField.addFocusListener(new JTextFieldHintListener(styleNameTextField, "请提前在 Aliyun OSS 控制台设置"));

        boolean compressStatus = aliyunOssSettings.getState().isCompress();
        boolean beforeCompressStatus = aliyunOssSettings.getState().isCompressBeforeUpload();
        boolean lookUpCompressStatus = aliyunOssSettings.getState().isCompressAtLookup();
        // 设置被选中
        this.compressCheckBox.setSelected(compressStatus);
        // 设置组下多选框状态
        this.compressBeforeUploadCheckBox.setEnabled(compressStatus);
        this.compressBeforeUploadCheckBox.setSelected(beforeCompressStatus);
        this.compressAtLookupCheckBox.setEnabled(compressStatus);
        this.compressAtLookupCheckBox.setSelected(lookUpCompressStatus);

        this.compressSlider.setEnabled(compressStatus && beforeCompressStatus);
        this.compressSlider.setValue(aliyunOssSettings.getState().getCompressBeforeUploadOfPercent());
        this.styleNameTextField.setEnabled(compressStatus && lookUpCompressStatus);
        this.styleNameTextField.setText(aliyunOssSettings.getState().getStyleName());

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

        addCheckBoxListener(compressBeforeUploadCheckBox, compressSlider);
        addCheckBoxListener(compressAtLookupCheckBox, styleNameTextField);
    }

    /**
     * 为 checkBox 添加监听器
     *
     * @param master    the master
     * @param component the component
     */
    private void addCheckBoxListener(JCheckBox master, JComponent component) {
        ChangeListener changeListener = e -> {
            JCheckBox checkBox = (JCheckBox) e.getSource();
            if (checkBox.isSelected()) {
                component.setEnabled(true);
            } else {
                component.setEnabled(false);
            }
        };
        master.addChangeListener(changeListener);
    }

    /**
     * 初始化替换标签设置组
     */
    private void initChangeToHtmlGroup() {
        customHtmlTypeTextField.addFocusListener(new JTextFieldHintListener(customHtmlTypeTextField, "格式: <a title='${}' href='${}' >![${}](${})</a>"));

        // 初始化 changeToHtmlTagCheckBox 选中状态
        // 设置被选中
        this.changeToHtmlTagCheckBox.setSelected(aliyunOssSettings.getState().isChangeToHtmlTag());
        // 设置组下单选框可用
        this.largePictureRadioButton.setEnabled(aliyunOssSettings.getState().isChangeToHtmlTag());
        this.commonRadioButton.setEnabled(aliyunOssSettings.getState().isChangeToHtmlTag());
        this.customRadioButton.setEnabled(aliyunOssSettings.getState().isChangeToHtmlTag());

        // 初始化 changeToHtmlTagCheckBox 组下单选框状态
        if (HtmlTagTypeEnum.COMMON_PICTURE.text.equals(aliyunOssSettings.getState().getTagType())) {
            commonRadioButton.setSelected(true);
        } else if (HtmlTagTypeEnum.LARGE_PICTURE.text.equals(aliyunOssSettings.getState().getTagType())) {
            largePictureRadioButton.setSelected(true);
        } else if (HtmlTagTypeEnum.CUSTOM.text.equals(aliyunOssSettings.getState().getTagType())) {
            customRadioButton.setSelected(true);
            customHtmlTypeTextField.setEnabled(true);
            customHtmlTypeTextField.setText(aliyunOssSettings.getState().getTagTypeCode());
        } else {
            commonRadioButton.setSelected(true);
        }

        // changeToHtmlTagCheckBox 监听, 修改组下组件状态
        changeToHtmlTagCheckBox.addChangeListener(e -> {
            JCheckBox checkBox = (JCheckBox) e.getSource();
            if (checkBox.isSelected()) {
                largePictureRadioButton.setEnabled(true);
                commonRadioButton.setEnabled(true);
                customRadioButton.setEnabled(true);
                // 如果原来自定义选项被选中, 则将输入框设置为可用
                if (customRadioButton.isSelected()) {
                    customHtmlTypeTextField.setEnabled(true);
                }
            } else {
                largePictureRadioButton.setEnabled(false);
                commonRadioButton.setEnabled(false);
                customRadioButton.setEnabled(false);
                customHtmlTypeTextField.setEnabled(false);
            }
        });
        ButtonGroup group = new ButtonGroup();
        addRadioButton(group, largePictureRadioButton);
        addRadioButton(group, commonRadioButton);
        addRadioButton(group, customRadioButton);
    }

    /**
     * 处理被选中的单选框
     *
     * @param group  the group
     * @param button the button
     */
    private void addRadioButton(ButtonGroup group, JRadioButton button) {
        // 设置name即为 actionCommand
        group.add(button);
        // 构造一个监听器，响应checkBox事件
        ActionListener actionListener = e -> {
            Object sourceObject = e.getSource();
            if (sourceObject instanceof JRadioButton) {
                JRadioButton sourceButton = (JRadioButton) sourceObject;
                if (HtmlTagTypeEnum.CUSTOM.text.equals(sourceButton.getText())) {
                    customHtmlTypeTextField.setEnabled(true);
                } else {
                    customHtmlTypeTextField.setEnabled(false);
                }
            }
        };
        button.addActionListener(actionListener);
    }

    /**
     * 生成文件名后缀
     *
     * @param name the name
     * @return the sufix string
     */
    @NotNull
    private String getSufixString(String name) {
        if (SuffixSelectTypeEnum.FILE_NAME.name.equals(name)) {
            return "/image.png";
        } else if (SuffixSelectTypeEnum.DATE_FILE_NAME.name.equals(name)) {
            return "/2019-2-30-image.png";
        } else if (SuffixSelectTypeEnum.RANDOM.name.equals(name)) {
            return "/98knb.png";
        } else {
            return "";
        }
    }

    /**
     * 判断 GUI 是否有变化
     *
     * @return the boolean
     */
    @Override
    public boolean isModified() {
        log.trace("isModified invoke");
        String newBucketName = bucketNameTextField.getText().trim();
        String newAccessKey = accessKeyTextField.getText().trim();
        String newAccessSecretKey = accessSecretKeyTextField.getText().trim();
        String newEndpoint = endpointTextField.getText().trim();
        String newFileDir = fileDirTextField.getText().trim();
        String newSuffix = "";

        int index = suffixBoxField.getSelectedIndex();
        Optional<SuffixSelectTypeEnum> type = EnumsUtils.getEnumObject(SuffixSelectTypeEnum.class, e -> e.getIndex() == index);
        if (type.isPresent()) {
            newSuffix = type.get().getName();
        }

        // 是否替换标签
        boolean changeToHtmlTag = changeToHtmlTagCheckBox.isSelected();
        // 替换的标签类型
        String tagType = "";
        // 替换的标签类型 code
        String tagTypeCode = "";
        if (changeToHtmlTag) {
            // 正常的
            if (commonRadioButton.isSelected()) {
                tagType = HtmlTagTypeEnum.COMMON_PICTURE.text;
                tagTypeCode = HtmlTagTypeEnum.COMMON_PICTURE.code;
            }
            // 点击看大图
            else if (largePictureRadioButton.isSelected()) {
                tagType = HtmlTagTypeEnum.LARGE_PICTURE.text;
                tagTypeCode = HtmlTagTypeEnum.LARGE_PICTURE.code;
            }
            // 自定义
            else if (customRadioButton.isSelected()) {
                tagType = HtmlTagTypeEnum.CUSTOM.text;
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

        return !(newBucketName.equals(aliyunOssSettings.getState().getBucketName())
                 && newAccessKey.equals(aliyunOssSettings.getState().getAccessKey())
                 && newAccessSecretKey.equals(aliyunOssSettings.getState().getAccessSecretKey())
                 && newEndpoint.equals(aliyunOssSettings.getState().getEndpoint())
                 && newFileDir.equals(aliyunOssSettings.getState().getFiledir())
                 && newSuffix.equals(aliyunOssSettings.getState().getSuffix())

                 && changeToHtmlTag == aliyunOssSettings.getState().isChangeToHtmlTag()
                 && tagType.equals(aliyunOssSettings.getState().getTagType())
                 && tagTypeCode.equals(aliyunOssSettings.getState().getTagTypeCode())
                 && compress == aliyunOssSettings.getState().isCompress()
                 && compressBeforeUpload == aliyunOssSettings.getState().isCompressBeforeUpload()
                 && compressBeforeUploadOfPercent == aliyunOssSettings.getState().getCompressBeforeUploadOfPercent()
                 && compressAtLookup == aliyunOssSettings.getState().isCompressAtLookup()
                 && styleName.equals(aliyunOssSettings.getState().getStyleName())
                 && transport == aliyunOssSettings.getState().isTransport()
                 && backup == aliyunOssSettings.getState().isBackup()
        );
    }

    /**
     * 配置被修改后时被调用, 修改 state 中的数据
     */
    @Override
    public void apply() {
        log.trace("apply invoke");
        aliyunOssSettings.getState().setBucketName(this.bucketNameTextField.getText().trim());
        aliyunOssSettings.getState().setAccessKey(this.accessKeyTextField.getText().trim());
        aliyunOssSettings.getState().setAccessSecretKey(this.accessSecretKeyTextField.getText().trim());
        aliyunOssSettings.getState().setEndpoint(this.endpointTextField.getText().trim());
        aliyunOssSettings.getState().setFiledir(this.fileDirTextField.getText().trim());
        aliyunOssSettings.getState().setSuffix(Objects.requireNonNull(this.suffixBoxField.getSelectedItem()).toString());

        aliyunOssSettings.getState().setChangeToHtmlTag(this.changeToHtmlTagCheckBox.isSelected());
        if (this.changeToHtmlTagCheckBox.isSelected()) {
            // 正常的
            if (commonRadioButton.isSelected()) {
                aliyunOssSettings.getState().setTagType(HtmlTagTypeEnum.COMMON_PICTURE.text);
                aliyunOssSettings.getState().setTagTypeCode(HtmlTagTypeEnum.COMMON_PICTURE.code);
            }
            // 点击看大图
            else if (largePictureRadioButton.isSelected()) {
                aliyunOssSettings.getState().setTagType(HtmlTagTypeEnum.LARGE_PICTURE.text);
                aliyunOssSettings.getState().setTagTypeCode(HtmlTagTypeEnum.LARGE_PICTURE.code);
            }
            // 自定义
            else if (customRadioButton.isSelected()) {
                aliyunOssSettings.getState().setTagType(HtmlTagTypeEnum.CUSTOM.text);
                // todo-dong4j : (2019年03月14日 14:30) [格式验证]
                aliyunOssSettings.getState().setTagTypeCode(customHtmlTypeTextField.getText().trim());
            }
        }
        aliyunOssSettings.getState().setCompress(this.compressCheckBox.isSelected());
        aliyunOssSettings.getState().setCompressBeforeUpload(this.compressBeforeUploadCheckBox.isSelected());
        aliyunOssSettings.getState().setCompressBeforeUploadOfPercent(this.compressSlider.getValue());
        aliyunOssSettings.getState().setCompressAtLookup(this.compressAtLookupCheckBox.isSelected());
        aliyunOssSettings.getState().setStyleName(this.styleNameTextField.getText().trim());
        aliyunOssSettings.getState().setTransport(this.transportCheckBox.isSelected());
        aliyunOssSettings.getState().setBackup(this.backupCheckBox.isSelected());

        // 重新创建 OSSClient
        UploadUtils.destory();
        UploadUtils.reset();
    }

    /**
     * 撤回是调用
     */
    @Override
    public void reset() {
        log.trace("reset invoke");
        this.bucketNameTextField.setText(aliyunOssSettings.getState().getBucketName());
        this.accessKeyTextField.setText(aliyunOssSettings.getState().getAccessKey());
        this.accessSecretKeyTextField.setText(aliyunOssSettings.getState().getAccessSecretKey());
        this.endpointTextField.setText(aliyunOssSettings.getState().getEndpoint());
        this.fileDirTextField.setText(aliyunOssSettings.getState().getFiledir());
        this.suffixBoxField.setSelectedItem(aliyunOssSettings.getState().getFiledir());
    }

    @NotNull
    @Override
    public String getId() {
        log.trace("getId invoke");
        return getDisplayName();
    }
}
