package info.dong4j.idea.plugin.client;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import com.intellij.openapi.ui.DialogBuilder;
import com.intellij.openapi.ui.DialogWrapper;

import info.dong4j.idea.plugin.MikBundle;
import info.dong4j.idea.plugin.enums.CloudEnum;
import info.dong4j.idea.plugin.settings.CustomUploadErrorDialog;
import info.dong4j.idea.plugin.settings.MikPersistenComponent;
import info.dong4j.idea.plugin.settings.MikState;
import info.dong4j.idea.plugin.settings.OssState;
import info.dong4j.idea.plugin.settings.oss.CustomOssSetting;
import info.dong4j.idea.plugin.settings.oss.CustomOssState;
import info.dong4j.idea.plugin.util.CustomOssUtils;
import info.dong4j.idea.plugin.util.StringUtils;

import org.apache.http.util.Asserts;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.InputStream;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;

/**
 * 自定义 OSS 客户端实现类
 * <p>
 * 该类实现了 OssClient 接口，用于处理自定义 OSS 服务的上传操作。支持从持久化配置中初始化客户端配置，并提供单例模式确保全局唯一实例。支持通过文件流上传文件，并解析返回结果获取文件 URL。同时支持测试按钮点击事件的上传逻辑，用于验证配置并保存状态。
 *
 * @author dong4j
 * @version 1.0.0
 * @date 2020.04.22
 * @since 1.5.0
 */
@Slf4j
@Client(CloudEnum.CUSTOMIZE)
public class CustomOssClient implements OssClient {
    /** API 的名称或标识，用于指定调用的具体服务接口 */
    private String api;
    /** 请求标识符，用于标识不同的请求区域或来源 */
    private String requestKey;
    /** 响应的 URL 路径 */
    private String responseUrlPath;
    /** HTTP 请求方法 */
    private String httpMethod;

    /**
     * 获取 CustomOssClient 实例
     * <p>
     * 该方法用于获取自定义 OSS 客户端实例，若实例不存在则创建并缓存。
     *
     * @return 自定义 OSS 客户端实例
     * @since 1.5.0
     */
    @Contract(pure = true)
    public static CustomOssClient getInstance() {
        CustomOssClient client = (CustomOssClient) OssClient.INSTANCES.get(CloudEnum.CUSTOMIZE);
        if (client == null) {
            client = SingletonHandler.SINGLETON;
            OssClient.INSTANCES.put(CloudEnum.CUSTOMIZE, client);
        }
        return client;
    }

    /**
     * 单例处理类
     * <p>
     * 用于管理 CustomOssClient 的单例实例，通过缓存的 map 映射获取已初始化的 client，避免创建多个实例，确保全局唯一性。
     *
     * @author dong4j
     * @version 1.5.0
     * @date 2020.04.22
     * @since 1.5.0
     */
    private static class SingletonHandler {
        /** 单例模式实例，用于提供全局唯一的 CustomOssClient 实例 */
        private static final CustomOssClient SINGLETON = new CustomOssClient();
    }

    /**
     * 初始化OSS客户端相关配置
     * <p>
     * 如果是第一次使用，ossClient为null，此时会通过持久化配置进行初始化。
     * 若是第一次设置，获取的持久化配置为null，则初始化ossClient失败。
     *
     * @since 1.5.0
     */
    private void init() {
        CustomOssState state = MikPersistenComponent.getInstance().getState().getCustomOssState();
        this.api = state.getApi();
        this.requestKey = state.getRequestKey();
        this.responseUrlPath = state.getResponseUrlPath();
        this.httpMethod = state.getHttpMethod();
    }

    /**
     * 实现接口，获取当前客户端类型
     * <p>
     * 返回当前客户端所对应的云类型枚举值
     *
     * @return 云类型枚举值
     * @since 1.5.0
     */
    @Override
    public CloudEnum getCloudType() {
        return CloudEnum.CUSTOMIZE;
    }

    /**
     * 通过文件流上传文件
     * <p>
     * 使用输入流将文件上传至对象存储服务，并解析返回结果获取文件访问URL。
     *
     * @param inputStream 文件输入流，用于读取上传文件的内容
     * @param filename    文件名，用于标识上传的文件
     * @return 上传成功后返回的文件访问URL
     * @throws Exception 上传过程中发生异常时抛出
     * @since 1.5.0
     */
    @Override
    public String upload(InputStream inputStream, String filename) throws Exception {
        // 确保配置已初始化
        if (StringUtils.isBlank(this.api)) {
            init();
        }

        Map<String, String> result = CustomOssUtils.putObject(this.api,
                                                              this.requestKey,
                                                              this.httpMethod.toUpperCase(),
                                                              filename,
                                                              inputStream,
                                                              null,
                                                              null);

        String json = result.get("json");
        if (StringUtils.isBlank(json)) {
            showDialog(result);
            throw new RuntimeException("服务器返回空响应");
        }

        try {
            JsonElement parse = JsonParser.parseString(json);
            String url = this.extractUrlFromJson(parse, this.responseUrlPath);

            if (StringUtils.isNotBlank(url)) {
                return url;
            }
        } catch (Exception ignored) {
        }
        showDialog(result);
        throw new RuntimeException("无法从响应中提取URL，请检查'响应URL路径'配置是否正确: " + responseUrlPath + "\n响应内容: " + json);
    }

    /**
     * 从JSON响应中提取URL
     * <p>
     * 根据指定的路径表达式从JSON数据中提取URL值
     *
     * @param json JSON数据
     * @param path URL路径表达式，如 "url" 或 "data.url"
     * @return 提取的URL字符串，若未找到则返回空字符串
     * @since 1.5.0
     */
    private String extractUrlFromJson(JsonElement json, String path) {
        if (json == null || StringUtils.isBlank(path)) {
            return "";
        }

        String[] parts = path.split("\\.");
        JsonElement current = json;

        for (String part : parts) {
            if (!current.isJsonObject()) {
                return "";
            }

            JsonObject obj = current.getAsJsonObject();
            current = obj.get(part);

            if (current == null) {
                return "";
            }
        }

        return current.isJsonPrimitive() ? current.getAsString() : "";
    }

    /**
     * 显示上传错误对话框
     * <p>
     * 根据传入的 result 参数构建并显示一个自定义的上传错误对话框，展示请求头信息、参数、文件部分、响应内容和 JSON 数据。
     * 窗口大小会根据文本内容自动调整。
     *
     * @param result 包含上传相关信息的 Map 对象，包含 "headerInfo"、"params"、"filePart"、"response" 和 "json" 键
     * @since 1.5.0
     */
    private static void showDialog(Map<String, String> result) {
        DialogBuilder builder = new DialogBuilder();
        CustomUploadErrorDialog dialog = new CustomUploadErrorDialog();

        // 设置文本内容
        String text = result.get("headerInfo") + "\n"
                      + result.get("params") + "\n"
                      + result.get("filePart") + "\n"
                      + result.get("response") + "\n"
                      + result.get("json") + "\n";
        dialog.getResponse().setText(text);

        // 根据文本内容计算合适的窗口大小
        int preferredWidth = calculatePreferredWidth(dialog.getResponse(), text);
        int preferredHeight = calculatePreferredHeight(dialog.getResponse(), text);

        // 设置最小和最大尺寸限制
        int minWidth = 400;
        int minHeight = 200;
        int maxWidth = 1200;
        int maxHeight = 800;

        int finalWidth = Math.max(minWidth, Math.min(maxWidth, preferredWidth));
        int finalHeight = Math.max(minHeight, Math.min(maxHeight, preferredHeight));

        // 设置内容面板的推荐大小，以便 DialogBuilder 自动调整窗口大小
        dialog.getContentPane().setPreferredSize(new java.awt.Dimension(finalWidth, finalHeight));
        
        builder.setOkActionEnabled(true);
        builder.setCenterPanel(dialog.getContentPane());
        builder.setTitle(MikBundle.message("custom.oss.error.dialog.title"));
        builder.removeAllActions();
        builder.addOkAction();

        DialogWrapper dialogWrapper = builder.getDialogWrapper();
        if (dialogWrapper != null) {
            builder.setOkOperation((() -> {
                dialogWrapper.close(DialogWrapper.OK_EXIT_CODE);
            }));

            // 显示对话框
            builder.show();

            // 显示后设置对话框大小（确保窗口大小正确）
            if (dialogWrapper.getPeer() != null) {
                java.awt.Window window = dialogWrapper.getPeer().getWindow();
                if (window != null) {
                    window.setSize(finalWidth, finalHeight);
                    // 确保窗口居中并自适应内容
                    window.setLocationRelativeTo(null);
                }
            }
        } else {
            // 如果无法获取 DialogWrapper，则直接显示（使用默认大小）
            builder.show();
        }
    }

    /**
     * 计算文本区域的最佳宽度
     * <p>
     * 根据文本内容和字体信息，计算文本区域的最佳显示宽度。
     *
     * @param textArea 文本区域组件
     * @param text     文本内容
     * @return 推荐宽度（像素）
     */
    private static int calculatePreferredWidth(javax.swing.JTextArea textArea, String text) {
        if (text == null || text.isEmpty()) {
            return 400;
        }

        // 使用 FontMetrics 计算文本宽度
        java.awt.FontMetrics fm = textArea.getFontMetrics(textArea.getFont());
        String[] lines = text.split("\n");

        int maxLineWidth = 0;
        for (String line : lines) {
            int lineWidth = fm.stringWidth(line);
            maxLineWidth = Math.max(maxLineWidth, lineWidth);
        }

        // 添加一些边距和滚动条宽度（约 50 像素）
        int padding = 80;
        return maxLineWidth + padding;
    }

    /**
     * 计算文本区域的最佳高度
     * <p>
     * 根据文本内容的行数和字体信息，计算文本区域的最佳显示高度。
     *
     * @param textArea 文本区域组件
     * @param text     文本内容
     * @return 推荐高度（像素）
     */
    private static int calculatePreferredHeight(javax.swing.JTextArea textArea, String text) {
        if (text == null || text.isEmpty()) {
            return 200;
        }

        // 计算行数
        String[] lines = text.split("\n");
        int lineCount = lines.length;

        // 如果文本很长，限制显示的行数（每行约 20 像素，最多显示 30 行）
        int maxVisibleLines = 30;
        int displayLines = Math.min(lineCount, maxVisibleLines);

        // 使用 FontMetrics 计算行高
        java.awt.FontMetrics fm = textArea.getFontMetrics(textArea.getFont());
        int lineHeight = fm.getHeight();

        // 添加一些边距（约 100 像素用于标题、按钮等）
        int padding = 100;
        return displayLines * lineHeight + padding;
    }

    /**
     * 在设置界面点击 'Test' 按钮上传时调用，用于获取当前配置并执行上传操作（新接口）
     * <p>
     * 该方法通过传入的 MikState 获取配置信息，包括 API 地址、请求 key、响应 URL 路径和 HTTP 方法，然后调用 upload 方法执行上传逻辑。
     *
     * @param inputStream 输入流，用于读取上传文件的内容
     * @param filename    文件名，表示上传的文件名称
     * @param state       MikState 对象，用于获取当前配置状态信息
     * @return 上传操作的结果字符串
     * @throws Exception 上传过程中发生异常时抛出
     * @since 2.0.0
     */
    @Override
    public String upload(InputStream inputStream, String filename, MikState state) throws Exception {
        CustomOssState customOssState = state.getCustomOssState();

        this.api = customOssState.getApi();
        this.requestKey = customOssState.getRequestKey();
        // 处理提示文本（如果值是 HINT 则视为空字符串）
        this.requestKey = CustomOssSetting.REQUES_TKEY_HINT.equals(requestKey) ? "" : requestKey;
        this.responseUrlPath = customOssState.getResponseUrlPath();
        this.responseUrlPath = CustomOssSetting.RESPONSE_URL_PATH_HINT.equals(responseUrlPath) ? "" : responseUrlPath;
        this.httpMethod = customOssState.getHttpMethod();
        this.httpMethod = CustomOssSetting.HTTP_METHOD_HINT.equals(httpMethod) ? "" : httpMethod;

        Asserts.notBlank(api, "api");
        Asserts.notBlank(requestKey, "发送到服务器的文件 key");
        Asserts.notBlank(responseUrlPath, "返回结果中的 url 路径");
        Asserts.notBlank(httpMethod, "请求方式");

        return this.upload(inputStream,
                           filename,
                           api,
                           requestKey,
                           responseUrlPath,
                           httpMethod);
    }

    /**
     * 处理测试按钮点击事件后的上传请求，成功后保留 client 信息，用于 paste 或右键上传时使用
     * <p>
     * 该方法接收上传所需的输入流、文件名、API、请求键、响应URL路径和HTTP方法，设置 CustomOssClient 相关参数，并执行上传操作。
     * 若上传成功且返回的URL不为空，则根据传入的API、请求键、响应URL路径和HTTP方法计算哈希值，并更新OSS状态。
     *
     * @param inputStream     上传的输入流
     * @param filename        文件名
     * @param api             使用的API名称
     * @param requestKey      请求键
     * @param responseUrlPath 响应URL路径
     * @param httpMethod      HTTP请求方法
     * @return 上传成功后的URL字符串，若上传失败或无返回则可能为空
     * @throws Exception 上传过程中发生异常时抛出
     * @since 1.5.0
     */
    @NotNull
    @Contract(pure = true)
    public String upload(InputStream inputStream,
                         String filename,
                         String api,
                         String requestKey,
                         String responseUrlPath,
                         String httpMethod) throws Exception {

        // 临时设置配置用于测试
        this.api = api;
        this.requestKey = requestKey;
        this.responseUrlPath = responseUrlPath;
        this.httpMethod = httpMethod;

        String url = this.upload(inputStream, filename);

        if (StringUtils.isNotBlank(url)) {
            int hashcode = api.hashCode() +
                           requestKey.hashCode() +
                           responseUrlPath.hashCode() +
                           httpMethod.hashCode();
            // 更新可用状态
            OssState.saveStatus(MikPersistenComponent.getInstance().getState().getCustomOssState(),
                                hashcode,
                                MikState.OLD_HASH_KEY);
        }
        return url;
    }
}