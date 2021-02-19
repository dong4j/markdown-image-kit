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
 * <p>Description: oss client 实现步骤:
 * 1. 初始化配置: 从持久化配置中初始化 client
 * 2. 静态内部类获取 client 单例
 * 3. 实现 OssClient 接口
 * 4. 自定义 upload 逻辑</p>
 *
 * @author dong4j
 * @version 1.0.0
 * @email "mailto:dong4j@gmail.com"
 * @date 2020.04.22 01:17
 * @since 1.5.0
 */
@Slf4j
@Client(CloudEnum.CUSTOMIZE)
public class CustomOssClient implements OssClient {
    /** bucketName */
    private static String api;
    /** regionName */
    private static String requestKey;
    /** accessKey */
    private static String responseUrlPath;
    /** httpMethod */
    private static String httpMethod;

    static {
        init();
    }

    /**
     * 如果是第一次使用, ossClient == null, 使用持久化配置初始化
     * 1. 如果是第一次设置, 获取的持久化配置为 null, 则初始化 ossClient 失败
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
     * Gets instance.
     *
     * @return the instance
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
     * 使用缓存的 map 映射获取已初始化的 client, 避免创建多个实例
     *
     * @author dong4j
     * @version 1.5.0
     * @email "mailto:dong4j@gmail.com"
     * @date 2020.04.22 01:17
     * @since 1.5.0
     */
    private static class SingletonHandler {
        /** SINGLETON */
        private static final CustomOssClient SINGLETON = new CustomOssClient();
    }

    /**
     * 实现接口, 获取当前 client type
     *
     * @return the cloud typed
     * @since 1.5.0
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
     * @throws Exception exception
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

        JsonParser parser = new JsonParser();
        String json = result.get("json");
        JsonElement parse = parser.parse(json);

        String url = this.getUrl(parse, split, split[0], 0);
        if (StringUtils.isNotBlank(url)) {
            return url;
        }

        showDialog(result);
        throw new RuntimeException("url 路径解析错误: " + responseUrlPath);
    }

    /**
     * Show dialog
     *
     * @param result result
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
     * Gets url *
     *
     * @param data  data
     * @param split split
     * @param path  path
     * @param index index
     * @return the url
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
     * 在设置界面点击 'Test' 按钮上传时调用, 通过 JPanel 获取当前配置
     * {@link info.dong4j.idea.plugin.settings.ProjectSettingsPage#testAndHelpListener()}
     *
     * @param inputStream the input stream
     * @param fileName    the file name
     * @param jPanel      the j panel
     * @return the string
     * @throws Exception exception
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
     * test 按钮点击事件后请求, 成功后保留 client, paste 或者 右键 上传时使用
     *
     * @param inputStream     the input stream
     * @param fileName        the file name
     * @param api             api
     * @param requestKey      request key
     * @param responseUrlPath response url path
     * @param httpMethod      http method
     * @return the string
     * @throws Exception exception
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
