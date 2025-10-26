package info.dong4j.idea.plugin.client;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import com.intellij.openapi.ui.TextFieldWithBrowseButton;

import info.dong4j.idea.plugin.enums.CloudEnum;
import info.dong4j.idea.plugin.settings.MikPersistenComponent;
import info.dong4j.idea.plugin.settings.MikState;
import info.dong4j.idea.plugin.settings.OssState;
import info.dong4j.idea.plugin.settings.oss.PicListOssSetting;
import info.dong4j.idea.plugin.settings.oss.PicListOssState;
import info.dong4j.idea.plugin.util.CustomOssUtils;
import info.dong4j.idea.plugin.util.StringUtils;

import org.apache.http.util.TextUtils;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.awt.Component;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JTextField;

import lombok.extern.slf4j.Slf4j;

/**
 * PicList 图床客户端实现类
 * <p>
 * 实现 PicList 图床的上传功能，支持通过 API 接口上传图片到 PicList 服务。
 * 支持自定义图床类型、配置名称和密钥等参数。
 *
 * @author dong4j
 * @version 1.0.0
 * @date 2025.10.26
 * @since 1.0.0
 */
@SuppressWarnings("D")
@Slf4j
@Client(CloudEnum.PICLIST)
public class PicListClient implements OssClient {
    /** PicList API 接口地址 */
    private static String api;
    /** 图床类型 */
    private static String picbed;
    /** 配置文件名称 */
    private static String configName;
    /** 请求密钥 */
    private static String key;
    /** PicList 可执行文件路径 */
    private static String exePath;

    static {
        init();
    }

    /**
     * 初始化 PicList 客户端配置
     * <p>
     * 从持久化配置中加载 PicList 的配置信息，包括 API 地址、图床类型、配置名称和密钥等。
     *
     * @since 1.0.0
     */
    private static void init() {
        PicListOssState state = MikPersistenComponent.getInstance().getState().getPicListOssState();
        api = state.getApi();
        picbed = state.getPicbed();
        configName = state.getConfigName();
        key = state.getKey();
        exePath = state.getExePath();
    }

    /**
     * 获取 PicListClient 实例
     * <p>
     * 返回 PicList 客户端的单例实例，若不存在则创建并缓存。
     *
     * @return PicList 客户端实例
     * @since 1.0.0
     */
    @Contract(pure = true)
    public static PicListClient getInstance() {
        PicListClient client = (PicListClient) OssClient.INSTANCES.get(CloudEnum.PICLIST);
        if (client == null) {
            client = SingletonHandler.SINGLETON;
            OssClient.INSTANCES.put(CloudEnum.PICLIST, client);
        }
        return client;
    }

    /**
     * 单例处理类
     * <p>
     * 用于管理 PicListClient 的单例实例，确保全局唯一性。
     *
     * @author dong4j
     * @version 1.0.0
     * @date 2025.10.26
     * @since 1.0.0
     */
    private static class SingletonHandler {
        /** 单例模式实例，用于提供全局唯一的 PicListClient 实例 */
        private static final PicListClient SINGLETON = new PicListClient();
    }

    /**
     * 获取当前客户端类型
     * <p>
     * 返回当前客户端对应的云类型枚举值。
     *
     * @return 云类型枚举值
     * @since 1.0.0
     */
    @Override
    public CloudEnum getCloudType() {
        return CloudEnum.PICLIST;
    }

    /**
     * 构建上传 URL，包含查询参数
     * <p>
     * 根据配置的参数构建包含查询参数的完整 URL。
     *
     * @param baseApi         基础 API 地址
     * @param picbedValue     图床类型参数值
     * @param configNameValue 配置名称参数值
     * @param keyValue        密钥参数值
     * @return 包含查询参数的完整 URL
     */
    private String buildUrl(String baseApi, String picbedValue, String configNameValue, String keyValue) {
        StringBuilder urlBuilder = new StringBuilder(baseApi);
        boolean hasParam = false;

        if (StringUtils.isNotEmpty(picbedValue)) {
            urlBuilder.append(hasParam ? "&" : "?")
                .append("picbed=")
                .append(URLEncoder.encode(picbedValue, StandardCharsets.UTF_8));
            hasParam = true;
        }

        if (StringUtils.isNotEmpty(configNameValue)) {
            urlBuilder.append(hasParam ? "&" : "?")
                .append("configName=")
                .append(URLEncoder.encode(configNameValue, StandardCharsets.UTF_8));
            hasParam = true;
        }

        if (StringUtils.isNotEmpty(keyValue)) {
            urlBuilder.append(hasParam ? "&" : "?")
                .append("key=")
                .append(URLEncoder.encode(keyValue, StandardCharsets.UTF_8));
            hasParam = true;
        }

        return urlBuilder.toString();
    }

    /**
     * 通过文件流上传文件
     * <p>
     * 使用输入流将文件上传至 PicList 服务，并解析返回结果获取文件访问 URL。
     * 如果配置了可执行文件路径，则使用命令行方式上传；否则使用 API 方式上传。
     *
     * @param inputStream 文件输入流，用于读取上传文件的内容
     * @param fileName    文件名，用于标识上传的文件
     * @return 上传成功后返回的文件访问 URL
     * @throws Exception 上传过程中发生异常时抛出
     * @since 1.0.0
     */
    @Override
    public String upload(InputStream inputStream, String fileName) throws Exception {
        // 如果配置了可执行文件路径，使用命令行方式上传
        if (StringUtils.isNotEmpty(exePath)) {
            return uploadViaCommandLine(inputStream, fileName);
        }

        // 否则使用 API 方式上传
        return uploadViaApi(inputStream, fileName);
    }

    /**
     * 通过 API 方式上传文件
     *
     * @param inputStream 文件输入流
     * @param fileName    文件名
     * @return 上传后的 URL
     * @throws Exception 上传失败时抛出
     */
    private String uploadViaApi(InputStream inputStream, String fileName) throws Exception {
        // 构建包含查询参数的 URL
        String uploadUrl = buildUrl(api, picbed, configName, key);

        log.debug("开始上传文件到 PicList API: {}", uploadUrl);

        // 使用 CustomOssUtils 发送 multipart/form-data 请求
        // PicList 接受任意字段名的文件，这里使用 "image" 作为字段名
        Map<String, String> result = CustomOssUtils.putObject(uploadUrl,
                                                              "image",
                                                              "POST",
                                                              fileName,
                                                              inputStream,
                                                              null,
                                                              null);

        String jsonResponse = result.get("json");
        log.debug("PicList 响应: {}", jsonResponse);

        // 解析 JSON 响应
        JsonElement jsonElement = JsonParser.parseString(jsonResponse);
        JsonObject jsonObject = jsonElement.getAsJsonObject();

        // 检查是否成功
        boolean success = jsonObject.get("success").getAsBoolean();
        if (!success) {
            String message = jsonObject.has("message") ? jsonObject.get("message").getAsString() : "上传失败";
            throw new RuntimeException("PicList 上传失败: " + message);
        }

        // 获取 result 数组，取第一个元素作为返回 URL
        JsonArray resultArray = jsonObject.getAsJsonArray("result");
        if (resultArray == null || resultArray.isEmpty()) {
            throw new RuntimeException("PicList 返回结果为空");
        }

        String url = resultArray.get(0).getAsString();
        log.info("上传成功: {} -> {}", fileName, url);
        return url;
    }

    /**
     * 通过命令行方式上传文件
     * <p>
     * 使用 PicList 命令行工具上传文件，上传成功后从系统剪贴板获取 URL。
     *
     * @param inputStream 文件输入流
     * @param fileName    文件名
     * @return 上传后的 URL
     * @throws Exception 上传失败时抛出
     */
    private String uploadViaCommandLine(InputStream inputStream, String fileName) throws Exception {
        log.debug("使用命令行上传文件: {}", fileName);

        // 保存输入流到临时文件
        File tempFile = File.createTempFile("piclist-upload-", fileName);
        try {
            // 将输入流内容写入临时文件
            try (FileOutputStream fos = new FileOutputStream(tempFile)) {
                byte[] buffer = new byte[1024];
                int len;
                while ((len = inputStream.read(buffer)) != -1) {
                    fos.write(buffer, 0, len);
                }
            }

            log.debug("临时文件创建成功: {}", tempFile.getAbsolutePath());

            // 构建命令，处理 macOS 的 .app 目录结构
            String actualExePath = resolveExecutablePath(exePath);
            String[] command = {actualExePath, "upload", tempFile.getAbsolutePath()};

            log.debug("执行命令: {}", String.join(" ", command));

            // 执行命令
            ProcessBuilder processBuilder = new ProcessBuilder(command);
            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();

            // 等待命令执行完成
            boolean finished = process.waitFor(10, TimeUnit.SECONDS);
            if (!finished) {
                process.destroyForcibly();
                throw new RuntimeException("命令行执行超时");
            }

            int exitCode = process.exitValue();
            if (exitCode != 0) {
                throw new RuntimeException("命令行执行失败，退出码: " + exitCode);
            }

            log.debug("命令执行成功");

            // 从剪贴板获取 URL（带重试机制）
            String url = getUrlFromClipboardWithRetry();
            if (StringUtils.isBlank(url)) {
                throw new RuntimeException("未能从剪贴板获取上传结果。" +
                                           "\n提示：请确保 PicList 已正确上传图片并将 URL 复制到剪贴板。");
            }

            log.info("上传成功: {} -> {}", fileName, url);
            return url;

        } finally {
            // 删除临时文件
            if (tempFile.exists()) {
                boolean deleted = tempFile.delete();
                if (!deleted) {
                    log.warn("临时文件删除失败: {}", tempFile.getAbsolutePath());
                }
            }
        }
    }

    /**
     * 从系统剪贴板获取 URL（带重试机制）
     * <p>
     * 由于 PicList 上传图片需要时间，会多次尝试从剪贴板获取 URL。
     * 最多尝试 10 次，每次间隔 500ms，总等待时间约 5 秒。
     * todo-dong4j : (2025.10.27 00:04) [在并发上传图片时还是有问题]
     *
     * @return URL 字符串，如果没有则返回空字符串
     */
    private String getUrlFromClipboardWithRetry() {
        int maxRetries = 10;
        long delayMs = 500;

        for (int i = 0; i < maxRetries; i++) {
            String url = getUrlFromClipboard();
            if (StringUtils.isNotBlank(url)) {
                log.debug("第 {} 次尝试，成功获取 URL: {}", i + 1, url);
                return url;
            }

            // 如果不是最后一次尝试，等待后重试
            if (i < maxRetries - 1) {
                try {
                    log.debug("第 {} 次尝试，剪贴板中尚未有 URL，等待 {}ms 后重试...", i + 1, delayMs);
                    Thread.sleep(delayMs);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    log.error("等待被中断", e);
                    return "";
                }
            }
        }

        log.warn("尝试 {} 次后仍未从剪贴板获取到 URL", maxRetries);
        return "";
    }

    /**
     * 从系统剪贴板获取 URL
     *
     * @return URL 字符串，如果没有则返回空字符串
     */
    private String getUrlFromClipboard() {
        try {
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            if (clipboard.isDataFlavorAvailable(DataFlavor.stringFlavor)) {
                Object clipboardData = clipboard.getData(DataFlavor.stringFlavor);
                if (clipboardData instanceof String text) {
                    // 检查是否是 URL
                    if (text.startsWith("http://") || text.startsWith("https://")) {
                        return text.trim();
                    }
                }
            }
        } catch (UnsupportedFlavorException e) {
            log.error("不支持的剪贴板数据格式", e);
        } catch (Exception e) {
            log.error("获取剪贴板内容失败", e);
        }
        return "";
    }

    /**
     * 解析可执行文件路径
     * <p>
     * 处理不同操作系统的可执行文件路径，特别是 macOS 的 .app 目录结构。
     *
     * @param exePath 用户输入的可执行文件路径
     * @return 实际可执行文件的路径
     */
    private String resolveExecutablePath(String exePath) {
        String osName = System.getProperty("os.name").toLowerCase();

        // macOS 需要特殊处理 .app 目录
        if (osName.contains("mac")) {
            File file = new File(exePath);

            // 如果是 .app 目录，需要找到内部的可执行文件
            if (file.isDirectory() && exePath.endsWith(".app")) {
                // 首先尝试查找 PicList 可执行文件
                File executableFile = new File(file, "Contents/MacOS/PicList");
                if (executableFile.exists() && executableFile.canExecute()) {
                    log.debug("macOS 检测到 .app 目录，使用可执行文件: {}", executableFile.getAbsolutePath());
                    return executableFile.getAbsolutePath();
                }

                // 如果 PicList 不存在，查找 MacOS 目录中的第一个可执行文件
                File macOsDir = new File(file, "Contents/MacOS");
                if (macOsDir.exists() && macOsDir.isDirectory()) {
                    File[] files = macOsDir.listFiles();
                    if (files != null) {
                        for (File f : files) {
                            if (f.isFile() && f.canExecute()) {
                                log.debug("macOS 在 MacOS 目录中找到可执行文件: {}", f.getAbsolutePath());
                                return f.getAbsolutePath();
                            }
                        }
                    }
                }

                // 无法找到 .app 内的可执行文件
                throw new RuntimeException("无法在 .app 目录中找到可执行文件: " + exePath +
                                           "\n请确保选择了正确的 PicList/PicGo .app 包");
            }
        }

        // 验证文件是否存在且可执行（Windows 和 Linux）
        File file = new File(exePath);
        if (!file.exists()) {
            throw new RuntimeException("可执行文件不存在: " + exePath);
        }
        if (!file.canExecute()) {
            throw new RuntimeException("文件没有执行权限: " + exePath +
                                       "\n请使用 chmod +x 命令添加执行权限");
        }

        return exePath;
    }

    /**
     * 在设置界面点击 'Test' 按钮上传时调用，用于获取当前配置并执行上传操作
     * <p>
     * 该方法通过传入的 JPanel 获取配置信息，然后调用 upload 方法执行上传逻辑。
     *
     * @param inputStream 输入流，用于读取上传文件的内容
     * @param fileName    文件名，表示上传的文件名称
     * @param jPanel      JPanel 对象，用于获取当前界面配置信息
     * @return 上传操作的结果字符串
     * @throws Exception 上传过程中发生异常时抛出
     * @since 1.0.0
     */
    @Override
    public String upload(InputStream inputStream, String fileName, JPanel jPanel) throws Exception {
        Map<String, String> map = this.getTestFieldTextWithPicListSupport(jPanel);
        String apiValue = map.get("picListApiTextField");
        String picbedValue = map.get("picListPicbedTextField");
        String configNameValue = map.get("picListConfigNameTextField");
        String keyValue = map.get("picListKeyTextField");
        String exePathValue = map.get("picListExeTextField");

        // 移除提示文本
        picbedValue = PicListOssSetting.PICBED_HINT.equals(picbedValue) ? "" : picbedValue;
        configNameValue = PicListOssSetting.CONFIG_NAME_HINT.equals(configNameValue) ? "" : configNameValue;
        keyValue = PicListOssSetting.KEY_HINT.equals(keyValue) ? "" : keyValue;
        exePathValue = PicListOssSetting.EXE_PATH_HINT.equals(exePathValue) ? "" : exePathValue;

        if (TextUtils.isBlank(apiValue) && TextUtils.isBlank(exePathValue)) {
            throw new IllegalStateException("API 地址和 可执行文件路径 必须配置一个");
        }

        return this.upload(inputStream,
                           fileName,
                           apiValue,
                           picbedValue,
                           configNameValue,
                           keyValue,
                           exePathValue);
    }

    /**
     * 处理测试按钮点击事件后的上传请求，成功后保留 client 信息
     * <p>
     * 该方法接收上传所需的输入流、文件名和配置参数，设置 PicListClient 相关参数，并执行上传操作。
     * 若上传成功，则根据传入的参数计算哈希值，并更新 OSS 状态。
     *
     * @param inputStream     上传的输入流
     * @param fileName        文件名
     * @param apiValue        API 地址
     * @param picbedValue     图床类型
     * @param configNameValue 配置名称
     * @param keyValue        密钥
     * @param exePathValue    可执行文件路径
     * @return 上传成功后的 URL 字符串，若上传失败或无返回则可能为空
     * @throws Exception 上传过程中发生异常时抛出
     */
    @NotNull
    @Contract(pure = true)
    public String upload(InputStream inputStream,
                         String fileName,
                         String apiValue,
                         String picbedValue,
                         String configNameValue,
                         String keyValue,
                         String exePathValue) throws Exception {

        PicListClient.api = apiValue;
        PicListClient.picbed = picbedValue;
        PicListClient.configName = configNameValue;
        PicListClient.key = keyValue;
        PicListClient.exePath = exePathValue;

        PicListClient client = PicListClient.getInstance();

        String url = client.upload(inputStream, fileName);

        if (StringUtils.isNotBlank(url)) {
            int hashcode = apiValue.hashCode() +
                           (picbedValue == null ? 0 : picbedValue.hashCode()) +
                           (configNameValue == null ? 0 : configNameValue.hashCode()) +
                           (keyValue == null ? 0 : keyValue.hashCode()) +
                           (exePathValue == null ? 0 : exePathValue.hashCode());
            // 更新可用状态
            OssState.saveStatus(MikPersistenComponent.getInstance().getState().getPicListOssState(),
                                hashcode,
                                MikState.OLD_HASH_KEY);
        }
        return url;
    }

    /**
     * 遍历指定面板中的组件，提取字段值
     * <p>
     * 支持 JTextField、JCheckBox 和 TextFieldWithBrowseButton 组件。
     *
     * @param jPanel 要遍历的面板对象
     * @return 包含组件 name 属性与对应值的 Map
     */
    @NotNull
    private Map<String, String> getTestFieldTextWithPicListSupport(JPanel jPanel) {
        Map<String, String> fieldMap = new HashMap<>(8);
        Component[] components = jPanel.getComponents();
        for (Component c : components) {
            if (c instanceof JTextField textField) {
                fieldMap.put(textField.getName(), textField.getText());
            } else if (c instanceof JCheckBox checkBox) {
                fieldMap.put(checkBox.getName(), checkBox.isSelected() + "");
            } else if (c instanceof TextFieldWithBrowseButton textFieldWithBrowseButton) {
                // 获取 TextFieldWithBrowseButton 的文本字段
                JTextField textField = textFieldWithBrowseButton.getTextField();
                fieldMap.put(textFieldWithBrowseButton.getName(), textField.getText());
            }
        }
        return fieldMap;
    }
}
