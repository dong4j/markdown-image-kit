package info.dong4j.idea.plugin.settings;

import com.aliyun.oss.OSSClient;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.SearchableConfigurable;
import com.intellij.ui.DocumentAdapter;
import com.intellij.ui.JBColor;

import info.dong4j.idea.plugin.enums.SuffixSelectTypeEnum;
import info.dong4j.idea.plugin.util.EnumsUtils;
import info.dong4j.idea.plugin.util.UploadUtils;

import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.lang.reflect.Method;
import java.util.Objects;
import java.util.Optional;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
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

    private JPanel myGeneralPanel;

    private JPanel mySettingsPanel;
    private JPanel mySupportPanel;

    private JTextField bucketNameTextField;
    private JTextField accessKeyTextField;
    private JTextField accessSecretKeyTextField;
    private JTextField endpointTextField;
    private JTextField fileDirTextField;
    private JComboBox suffixBoxField;
    private JButton testButton;
    private JButton helpButton;

    private JTextField exampleTextField;
    private JPanel myUploadPanel;
    private JLabel message;

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
        // 根据持久化配置设置为被选中的 item
        suffixBoxField.setSelectedItem(aliyunOssSettings.getState().getSuffix());
        // 处理当 fileDirTextField.getText() 为 空字符时, 不拼接 "/
        String fileDir = StringUtils.isBlank(fileDirTextField.getText()) ? "" : "/" + fileDirTextField.getText();
        // 拼接字符串
        exampleTextField.setText(endpointTextField.getText() + fileDir + getSufixString(suffixBoxField.getSelectedItem().toString()));

        endpointTextField.getDocument().addDocumentListener(new DocumentAdapter() {
            @Override
            protected void textChanged(@NotNull DocumentEvent e) {
                exampleTextField.setText(endpointTextField.getText() + fileDirTextField.getText() + getSufixString(suffixBoxField.getSelectedItem().toString()));
            }
        });

        // 设置 fileDirTextField 输入的监听
        fileDirTextField.getDocument().addDocumentListener(new DocumentAdapter() {
            @Override
            protected void textChanged(@NotNull DocumentEvent e) {
                String fileDirText = StringUtils.isBlank(fileDirTextField.getText()) ? "" : "/" + fileDirTextField.getText();
                exampleTextField.setText(endpointTextField.getText() + fileDirText + getSufixString(suffixBoxField.getSelectedItem().toString()));
            }
        });

        // 添加监听器, 当选项被修改后, 修改 exampleTextField 中的 text
        suffixBoxField.addItemListener(e -> {
            log.trace("itemStateChanged e = {}", e.getItem());
            String fileDirText = StringUtils.isBlank(fileDirTextField.getText()) ? "" : fileDirTextField.getText();
            exampleTextField.setText(endpointTextField.getText() + fileDirText + getSufixString(e.getItem().toString()));
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
            OSSClient ossClient = new OSSClient(tempEndpoint, tempAccessKey, tempAccessSecretKey);
            try {
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
                ossClient.shutdown();
            }
        });

        // help button 监听
        helpButton.addActionListener(e -> {
            // 打开浏览器到帮助页面
            String url = "http://dong4j.info";
            try {
                String osName = System.getProperty("os.name", "");
                if (osName.startsWith("Mac OS")) {
                    // 苹果
                    Class fileMgr = Class.forName("com.apple.eio.FileManager");
                    Method openURL = fileMgr.getDeclaredMethod("openURL", String.class);
                    openURL.invoke(null, url);
                } else if (osName.startsWith("Windows")) {
                    // windows
                    Runtime.getRuntime().exec(
                        "rundll32 url.dll,FileProtocolHandler " + url);
                } else {
                    // Unix or Linux
                    String[] browsers = {"firefox", "opera", "konqueror", "epiphany",
                                         "mozilla", "netscape"};
                    String browser = null;
                    // 执行代码，在brower有值后跳出，
                    // 这里是如果进程创建成功了，==0是表示正常结束。
                    for (int count = 0; count < browsers.length && browser == null; count++) {
                        if (Runtime.getRuntime()
                                .exec(new String[] {"which", browsers[count]})
                                .waitFor() == 0) {
                            browser = browsers[count];
                        }
                    }
                    if (browser == null) {
                        throw new Exception("Could not find web browser");
                    }
                    // 这个值在上面已经成功的得到了一个进程。
                    else {
                        Runtime.getRuntime().exec(new String[] {browser, url});
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
    }

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

        return !(newBucketName.equals(aliyunOssSettings.getState().getBucketName())
                 && newAccessKey.equals(aliyunOssSettings.getState().getAccessKey())
                 && newAccessSecretKey.equals(aliyunOssSettings.getState().getAccessSecretKey())
                 && newEndpoint.equals(aliyunOssSettings.getState().getEndpoint())
                 && newFileDir.equals(aliyunOssSettings.getState().getFiledir())
                 && newSuffix.equals(aliyunOssSettings.getState().getSuffix()));
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

        // 重新创建 OSSClient
        UploadUtils.destory();
        UploadUtils.init();
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
