package info.dong4j.idea.plugin.client;

import com.qcloud.cos.COSClient;

import info.dong4j.idea.plugin.enums.CloudEnum;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.*;

import javax.swing.JPanel;

import lombok.extern.slf4j.Slf4j;

/**
 * <p>Company: 科大讯飞股份有限公司-四川分公司</p>
 * <p>Description: oss client 模板 </p>
 *
 * @author dong4j
 * @email sjdong3 @iflytek.com
 * @since 2019 -07-08 16:39
 */
@Slf4j
public class DemoClient implements OssClient {

    private static final Object LOCK = new Object();
    private static COSClient ossClient = null;

    /////////////////////////////////  1. init start /////////////////////////////////

    /**
     * Instantiates a new Tencent oss client.
     */
    private DemoClient() {
        checkClient();
    }

    /**
     * 在调用 ossClient 之前先检查, 如果为 null 就 init()
     */
    private static void checkClient() {
        synchronized (LOCK) {
            if (ossClient == null) {
                init();
            }
        }
    }

    /**
     * 如果是第一次使用, ossClient == null, 使用持久化配置初始化 SDK client
     */
    private static void init() {
        // 1. 从 state 中获取配置信息
        try {
            // 2. 生成 SDK client
        } catch (Exception ignored) {
        }
    }

    ///////////////////////////////// init end /////////////////////////////////


    ///////////////////////////////// 2. singleton start /////////////////////////////////

    /**
     * Gets instance.
     *
     * @return the instance
     */
    @Contract(pure = true)
    public static DemoClient getInstance() {
        return DemoClient.SingletonHandler.singleton;
    }

    private static class SingletonHandler {
        private static DemoClient singleton = new DemoClient();

        static {
            checkClient();
        }
    }

    /**
     * Set oss client.
     *
     * @param oss the oss
     */
    private void setOssClient(COSClient oss) {
        ossClient = oss;
    }
    ///////////////////////////////// singleton end /////////////////////////////////

    ///////////////////////////////// 3. interface start /////////////////////////////////

    /**
     * 实现接口, 获取当前 client type
     *
     * @return the cloud typed
     */
    @Override
    public CloudEnum getCloudType() {
        return CloudEnum.CUSTOMIZE;
    }

    /**
     * 通过文件流上传文件
     *
     * @param inputStream the input stream
     * @param fileName    the file name
     * @return the string
     */
    @Override
    public String upload(InputStream inputStream, String fileName) {
        return upload(ossClient, inputStream, fileName);
    }

    /**
     * 在设置界面点击 'Test' 按钮上传时调用, 通过 JPanel 获取当前配置
     * {@link info.dong4j.idea.plugin.settings.ProjectSettingsPage#testAndHelpListener()}
     *
     * @param inputStream the input stream
     * @param fileName    the file name
     * @param jPanel      the j panel
     * @return the string
     */
    @Override
    public String upload(InputStream inputStream, String fileName, JPanel jPanel) {
        // 从 JPanel 中获取当前的上传配置
        return upload(inputStream,
                      fileName,
                      "bucketName",
                      "accessKey",
                      "secretKey");
    }

    ///////////////////////////////// interface end /////////////////////////////////


    ///////////////////////////////// 4. custom upload start /////////////////////////////////

    /**
     * test 按钮点击事件后请求, 成功后保留 client, paste 或者 右键 上传时使用
     *
     * @param inputStream the input stream
     * @param fileName    the file name
     * @param bucketName  the bucketName name
     * @param accessKey   the access key
     * @param secretKey   the secret key
     * @return the string
     */
    @NotNull
    @Contract(pure = true)
    public String upload(InputStream inputStream,
                         String fileName,
                         String bucketName,
                         String accessKey,
                         String secretKey) {
        // 1. 获取单例 client
        DemoClient demoClient = DemoClient.getInstance();
        // 2. 使用 SDK 生成 client
        String url = demoClient.upload(ossClient, inputStream, fileName);

        if (org.apache.commons.lang.StringUtils.isNotBlank(url)) {
            // 3. 计算 hashcode
            // 4. 保存经过验证的 client
        }
        return url;
    }

    /**
     * 调用 SDK 上传文件 并返回 rul
     *
     * @param ossClient   the oss client
     * @param inputStream the input stream
     * @param fileName    the file name
     * @return the string
     */
    public String upload(@NotNull COSClient ossClient, InputStream inputStream, String fileName) {
        return "";
    }
    ///////////////////////////////// custom upload end /////////////////////////////////
}
