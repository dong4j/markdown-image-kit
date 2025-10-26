package info.dong4j.idea.plugin.client;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import com.intellij.openapi.ui.DialogBuilder;
import com.intellij.openapi.ui.DialogWrapper;

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

import javax.swing.JPanel;

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
    private static String api;
    /** 请求标识符，用于标识不同的请求区域或来源 */
    private static String requestKey;
    /** 响应的 URL 路径 */
    private static String responseUrlPath;
    /** HTTP 请求方法 */
    private static String httpMethod;

    static {
        init();
    }

    /**
     * 初始化OSS客户端相关配置
     * <p>
     * 如果是第一次使用，ossClient为null，此时会通过持久化配置进行初始化。
     * 若是第一次设置，获取的持久化配置为null，则初始化ossClient失败。
     *
     * @since 1.5.0
     */
    private static void init() {
        CustomOssState state = MikPersistenComponent.getInstance().getState().getCustomOssState();
        api = state.getApi();
        requestKey = state.getRequestKey();
        responseUrlPath = state.getResponseUrlPath();
        httpMethod = state.getHttpMethod();

    }

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
     * @param fileName    文件名，用于标识上传的文件
     * @return 上传成功后返回的文件访问URL
     * @throws Exception 上传过程中发生异常时抛出
     * @since 1.5.0
     */
    @Override
    public String upload(InputStream inputStream, String fileName) throws Exception {

        Map<String, String> result = CustomOssUtils.putObject(api,
                                                              requestKey,
                                                              httpMethod.toUpperCase(),
                                                              fileName,
                                                              inputStream,
                                                              null,
                                                              null);

        String[] split = responseUrlPath.split("\\.");

        String json = result.get("json");
        JsonElement parse = JsonParser.parseString(json);

        String url = this.getUrl(parse, split, split[0], 0);
        if (StringUtils.isNotBlank(url)) {
            return url;
        }

        showDialog(result);
        throw new RuntimeException("url 路径解析错误: " + responseUrlPath);
    }

    /**
     * 显示上传错误对话框
     * <p>
     * 根据传入的 result 参数构建并显示一个自定义的上传错误对话框，展示请求头信息、参数、文件部分、响应内容和 JSON 数据。
     *
     * @param result 包含上传相关信息的 Map 对象，包含 "headerInfo"、"params"、"filePart"、"response" 和 "json" 键
     * @since 1.5.0
     */
    private static void showDialog(Map<String, String> result) {
        DialogBuilder builder = new DialogBuilder();
        CustomUploadErrorDialog dialog = new CustomUploadErrorDialog();
        dialog.getResponse().setText(result.get("headerInfo") + "\n"
                                     + result.get("params") + "\n"
                                     + result.get("filePart") + "\n"
                                     + result.get("response") + "\n"
                                     + result.get("json") + "\n");

        builder.setOkActionEnabled(true);
        builder.setCenterPanel(dialog.getContentPane());
        builder.setTitle("Response");
        builder.removeAllActions();
        builder.addOkAction();
        builder.setOkOperation((() -> {
            builder.getDialogWrapper().close(DialogWrapper.OK_EXIT_CODE);
        }));

        builder.show();
    }

    /**
     * 根据数据、分割路径和索引生成对应的URL
     * <p>
     * 递归解析JSON数据，根据指定的分割路径获取对应的URL值
     *
     * @param data  需要解析的JSON数据
     * @param split 分割路径数组，用于定位URL字段
     * @param path  当前路径片段
     * @param index 当前路径片段在分割数组中的索引
     * @return 生成的URL字符串，若未找到则返回空字符串
     * @since 1.5.0
     */
    private String getUrl(JsonElement data, String[] split, String path, int index) {
        if (data != null) {
            if (data.isJsonObject()) {
                JsonObject asJsonObject1 = data.getAsJsonObject();
                JsonElement url = asJsonObject1.get(split[index]);
                index++;
                if (index == split.length) {
                    if (url == null) {
                        return "";
                    } else {
                        return url.getAsString();
                    }
                }
                return this.getUrl(url, split, split[index], index);
            } else {
                return data.getAsString();
            }
        }
        return "";
    }

    /**
     * 在设置界面点击 'Test' 按钮上传时调用，用于获取当前配置并执行上传操作
     * <p>
     * 该方法通过传入的 JPanel 获取配置信息，包括 API 地址、请求 key、响应 URL 路径和 HTTP 方法，然后调用 upload 方法执行上传逻辑。
     *
     * @param inputStream 输入流，用于读取上传文件的内容
     * @param fileName    文件名，表示上传的文件名称
     * @param jPanel      JPanel 对象，用于获取当前界面配置信息
     * @return 上传操作的结果字符串
     * @throws Exception 上传过程中发生异常时抛出
     * @since 1.5.0
     */
    @Override
    public String upload(InputStream inputStream, String fileName, JPanel jPanel) throws Exception {
        Map<String, String> map = this.getTestFieldText(jPanel);
        String api = map.get("api");
        String requestKey = map.get("requestKey");
        requestKey = CustomOssSetting.REQUES_TKEY_HINT.equals(requestKey) ? "" : requestKey;
        String responseUrlPath = map.get("responseUrlPath");
        responseUrlPath = CustomOssSetting.RESPONSE_URL_PATH_HINT.equals(responseUrlPath) ? "" : responseUrlPath;
        String httpMethod = map.get("httpMethod");
        httpMethod = CustomOssSetting.HTTP_METHOD_HINT.equals(httpMethod) ? "" : httpMethod;

        Asserts.notBlank(api, "api");
        Asserts.notBlank(requestKey, "发送到服务器的文件 key");
        Asserts.notBlank(responseUrlPath, "返回结果中的 url 路径");
        Asserts.notBlank(httpMethod, "请求方式");

        return this.upload(inputStream,
                           fileName,
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
     * @param fileName        文件名
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
                         String fileName,
                         String api,
                         String requestKey,
                         String responseUrlPath,
                         String httpMethod) throws Exception {

        CustomOssClient.api = api;
        CustomOssClient.requestKey = requestKey;
        CustomOssClient.responseUrlPath = responseUrlPath;
        CustomOssClient.httpMethod = httpMethod;

        CustomOssClient client = CustomOssClient.getInstance();

        String url = client.upload(inputStream, fileName);

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
