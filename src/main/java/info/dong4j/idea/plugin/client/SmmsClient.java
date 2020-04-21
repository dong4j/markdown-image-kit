/*
 * MIT License
 *
 * Copyright (c) 2020 dong4j <dong4j@gmail.com>
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

package info.dong4j.idea.plugin.client;

import com.google.gson.Gson;

import info.dong4j.idea.plugin.entity.SmmsResult;
import info.dong4j.idea.plugin.enums.CloudEnum;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.jetbrains.annotations.Contract;

import java.io.*;
import java.nio.charset.StandardCharsets;

import javax.swing.JPanel;

import lombok.extern.slf4j.Slf4j;

/**
 * <p>Company: no company</p>
 * <p>Description: </p>
 *
 * @author dong4j
 * @email dong4j@gmail.com
 * @since 2019 -04-01 09:21
 */
@Slf4j
@Client(CloudEnum.SM_MS_CLOUD)
public class SmmsClient implements OssClient {
    private static final String UPLOAD_URL = "https://sm.ms/api/v2/upload";
    private static Client client;

    @Contract(pure = true)
    private SmmsClient() {

    }

    @Override
    public CloudEnum getCloudType() {
        return CloudEnum.SM_MS_CLOUD;
    }

    /**
     * Gets instance.
     *
     * @return the instance
     */
    @Contract(pure = true)
    public static SmmsClient getInstance() {
        SmmsClient client = (SmmsClient)OssClient.INSTANCES.get(CloudEnum.SM_MS_CLOUD);
        if(client == null){
            client = SingletonHandler.SINGLETON;
            OssClient.INSTANCES.put(CloudEnum.SM_MS_CLOUD, client);
        }
        return client;
    }

    private static class SingletonHandler {
        private static final SmmsClient SINGLETON = new SmmsClient();
    }

    /**
     * Upload string.
     *
     * @param inputStream the input stream
     * @param fileName    the file name
     * @return the string
     */
    @Override
    public String upload(InputStream inputStream, String fileName) {
        if (client == null) {
            client = new Client();
        }
        return client.upload(inputStream, fileName);
    }

    /**
     * sm.ms 不需要测试
     * {@link info.dong4j.idea.plugin.settings.ProjectSettingsPage#testAndHelpListener()}
     *
     * @param inputStream the input stream
     * @param fileName    the file name
     * @param jPanel      the j panel
     * @return the string
     */
    @Override
    public String upload(InputStream inputStream, String fileName, JPanel jPanel) {
        return "";
    }

    private static class Client {
        private final CloseableHttpClient client;

        /**
         * Instantiates a new Client.
         *
         * @throws IOException the io exception
         */
        Client() {
            HttpClientBuilder builder = HttpClients.custom();
            // 必须设置 UA, 不然会报 403
            builder.setUserAgent("Mozilla/5.0 (Windows; U; Windows NT 6.1; zh-CN; rv:1.9.2.6)");
            this.client = builder.build();
        }

        /**
         * 直接使用 http 接口上传
         *
         * @param inputStream the input stream
         * @param fileName    the file name
         * @return the string
         */
        public String upload(InputStream inputStream, String fileName) {
            try {
                HttpEntity reqEntity = MultipartEntityBuilder.create()
                    .addBinaryBody("smfile", inputStream, ContentType.DEFAULT_BINARY, fileName)
                    .build();

                HttpPost post = new HttpPost(UPLOAD_URL);
                post.setHeader("Content-Type", "multipart/form-data");
                post.setEntity(reqEntity);

                HttpResponse response = this.client.execute(post);

                if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                    byte[] res = EntityUtils.toByteArray(response.getEntity());
                    String result = IOUtils.toString(res, StandardCharsets.UTF_8.name());
                    SmmsResult smmsResult = new Gson().fromJson(result, SmmsResult.class);
                    log.trace("{}", smmsResult);
                    return smmsResult.getData().getUrl();
                }
            } catch (Exception e) {
                log.trace("", e);
            }
            return "";
        }
    }
}
