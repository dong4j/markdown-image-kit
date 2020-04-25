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

import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;

import java.io.*;
import java.nio.charset.StandardCharsets;

import lombok.extern.slf4j.Slf4j;

/**
 * <p>Company: no company</p>
 * <p>Description: ${description}</p>
 *
 * @author dong4j
 * @version x.x.x
 * @email dong4j @gmail.com
 * @date 2019 -04-02 14:23
 */
@Slf4j
public class SmmsClientTest {

    /**
     * Test 1 *
     *
     * @throws IOException io exception
     */
    @Test
    public void test1() throws IOException {
        log.info("{}", this.start("https://sm.ms/api/v2/upload", "/Users/dong4j/Downloads/05B3AB1C-BBA9-4113-B212-10A914D0CC18.jpg"));
    }

    /**
     * Start string
     *
     * @param url      url
     * @param filePath file path
     * @return the string
     * @throws IOException io exception
     */
    @NotNull
    private String start(String url, String filePath) throws IOException {
        HttpPost post = new HttpPost(url);
        File imageFile = new File(filePath);
        FileBody imageFileBody = new FileBody(imageFile);

        HttpEntity reqEntity = MultipartEntityBuilder.create()
            .addPart("smfile", imageFileBody)
            .build();

        post.setEntity(reqEntity);

        HttpClientBuilder builder = HttpClients.custom();
        // 必须设置 UA, 不然会报 403
        builder.setUserAgent("Mozilla/5.0 (Windows; U; Windows NT 6.1; zh-CN; rv:1.9.2.6)");
        CloseableHttpClient httpClient = builder.build();

        HttpResponse response = httpClient.execute(post);
        byte[] res;
        if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
            res = EntityUtils.toByteArray(response.getEntity());
            String result = IOUtils.toString(res, StandardCharsets.UTF_8.name());
            SmmsResult smmsResult = new Gson().fromJson(result, SmmsResult.class);
            log.info("{}", smmsResult);
        }

        return "";
    }

    @Test
    public void test_2() throws FileNotFoundException {
        log.info("{}", this.upload(new FileInputStream(new File("/Users/dong4j/Downloads/05B3AB1C-BBA9-4113-B212-10A914D0CC18.jpg")),
                                   "05B3AB1C-BBA9-4113-B212-10A914D0CC18.jpg"));
    }

    public String upload(InputStream inputStream, String fileName) {
        try {
            HttpClientBuilder builder = HttpClients.custom();
            // 必须设置 UA, 不然会报 403
            builder.setUserAgent("Mozilla/5.0 (Windows; U; Windows NT 6.1; zh-CN; rv:1.9.2.6)");
            CloseableHttpClient client = builder.build();


            HttpEntity reqEntity = MultipartEntityBuilder.create()
                .addBinaryBody("smfile", inputStream, ContentType.DEFAULT_BINARY, fileName)
                .build();

            HttpPost post = new HttpPost("https://sm.ms/api/v2/upload");
            post.setEntity(reqEntity);

            HttpResponse response = client.execute(post);

            if (response.getStatusLine().getStatusCode() == org.apache.http.HttpStatus.SC_OK) {
                byte[] res = EntityUtils.toByteArray(response.getEntity());
                String result = IOUtils.toString(res, StandardCharsets.UTF_8.name());
                SmmsResult smmsResult = new Gson().fromJson(result, SmmsResult.class);
                log.trace("{}", smmsResult);
                if (smmsResult.getData() == null) {
                    return smmsResult.getMessage();
                }
                return smmsResult.getData().getUrl();
            }
        } catch (Exception e) {
            log.trace("", e);
        }
        return "";
    }
}
