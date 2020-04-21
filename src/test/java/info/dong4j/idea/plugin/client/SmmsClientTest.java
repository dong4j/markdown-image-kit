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
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;

import java.io.*;
import java.nio.charset.StandardCharsets;

import lombok.extern.slf4j.Slf4j;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

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
     * Test *
     *
     * @throws Exception exception
     */
    @Test
    public void test() throws Exception {
    }

    /**
     * Upload *
     *
     * @param fileName file name
     * @throws IOException io exception
     */
    public static void upload(String fileName) throws IOException {
        OkHttpClient client = new OkHttpClient();

        MediaType mediaType = MediaType.parse("multipart/form-data; boundary=----WebKitFormBoundary7MA4YWxkTrZu0gW");
        RequestBody body = RequestBody.create(mediaType, "------WebKitFormBoundary7MA4YWxkTrZu0gW\r\nContent-Disposition: form-data; name=\"smfile\"; filename=\"mik.png\"\r\nContent-Type: image/png\r\n\r\n\r\n------WebKitFormBoundary7MA4YWxkTrZu0gW--");
        Request request = new Request.Builder()
            .url("https://sm.ms/api/upload")
            .post(body)
            .addHeader("content-type", "multipart/form-data; boundary=----WebKitFormBoundary7MA4YWxkTrZu0gW")
            .addHeader("Content-Type", "multipart/form-data")
            .addHeader("cache-control", "no-cache")
            .addHeader("Postman-Token", "a415f3eb-cd9e-4c67-a828-6cf59ca81c10")
            .build();

        Response response = client.newCall(request).execute();
        log.info("{}", response);
    }

    /**
     * Upload 1 *
     *
     * @throws IOException io exception
     */
    @Test
    public void upload1() throws IOException {
        OkHttpClient client = new OkHttpClient();

        MediaType mediaType = MediaType.parse("multipart/form-data; boundary=----WebKitFormBoundary7MA4YWxkTrZu0gW");
        RequestBody body = RequestBody.create(mediaType, "------WebKitFormBoundary7MA4YWxkTrZu0gW\r\nContent-Disposition: form-data; name=\"smfile\"; filename=\"mik.png\"\r\nContent-Type: image/png\r\n\r\n\r\n------WebKitFormBoundary7MA4YWxkTrZu0gW--");
        Request request = new Request.Builder()
            .url("https://sm.ms/api/upload")
            .post(body)
            .addHeader("content-type", "multipart/form-data; boundary=----WebKitFormBoundary7MA4YWxkTrZu0gW")
            .addHeader("cache-control", "no-cache")
            .addHeader("User-Agent", "Mozilla/5.0 (Windows; U; Windows NT 6.1; zh-CN; rv:1.9.2.6)")
            .build();

        Response response = client.newCall(request).execute();
        log.info("{}", response);
    }

    /**
     * Upload 3
     */
    @Test
    public void upload3() {
        File file = new File("/Users/dong4j/Downloads/mik.png");
        RequestBody fileBody = RequestBody.create(MediaType.parse("multipart/form-data"), file);
        RequestBody requestBody = new MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("smfile", file.getName(), fileBody)
            .build();

        Request request = new Request.Builder()
            .url("http://sm.ms/api/v2/upload")
            .addHeader("Content-Type", "multipart/form-data")
            .addHeader("User-Agent", "Mozilla/5.0 (Windows; U; Windows NT 6.1; zh-CN; rv:1.9.2.6)")
            .post(requestBody)
            .build();

        Response response;
        OkHttpClient client = new OkHttpClient();
        try {
            response = client.newCall(request).execute();
            String jsonString = response.body().string();
            log.trace(" upload jsonString =" + jsonString);

            if (!response.isSuccessful()) {
                throw new RuntimeException("upload error code " + response);
            } else {
                JSONObject jsonObject = new JSONObject(jsonString);
                log.info("{}", jsonObject);
                int errorCode = jsonObject.getInt("errorCode");
                if (errorCode == 0) {
                    log.trace(" upload data =" + jsonObject.getString("data"));
                    jsonObject.getString("data");
                } else {
                    throw new RuntimeException("upload error code " + errorCode + ",errorInfo=" + jsonObject.getString("errorInfo"));
                }
            }

        } catch (IOException e) {
            log.trace("upload IOException ", e);
        } catch (JSONException e) {
            log.trace("upload JSONException ", e);
        }
    }

    /**
     * Upload 4 *
     *
     * @throws Exception exception
     */
    @Test
    public void upload4() throws Exception {
        String fileName = "mik1.png";
        String filePath = "/Users/dong4j/Downloads/mik.png";
        String url = "https://sm.ms/api/v2/upload";
        OkHttpClient client = new OkHttpClient();
        RequestBody requestBody = new MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("smfile", fileName, RequestBody.create(MediaType.parse("multipart/form-data"), new File(filePath)))
            .build();

        Request request = new Request.Builder()
            .url(url)
            .addHeader("Content-Type", "multipart/form-data")
            .addHeader("User-Agent", "Mozilla/5.0 (Windows; U; Windows NT 6.1; zh-CN; rv:1.9.2.6)")
            .post(requestBody)
            .build();

        Response response = client.newCall(request).execute();
        if (!response.isSuccessful()) {
            throw new IOException("Unexpected code " + response);
        }
        if (response.body() != null) {
            String result = response.body().string();
            SmmsResult smmsResult = new Gson().fromJson(result, SmmsResult.class);
            log.info("{}", smmsResult);
        }
    }


    /**
     * Upload *
     *
     * @throws InterruptedException interrupted exception
     */
    public static void upload() throws InterruptedException {
        OkHttpClient okHttpClient = new OkHttpClient();
        MediaType parse = MediaType.parse("text/x-markdown;charset=utf-8");
        File file = new File("/Users/dong4j/Downloads/xx.md");
        Request builder = new Request.Builder()
            .post(RequestBody.create(parse, file))
            .url("https://api.github.com/markdown/raw")
            .build();
        Call call = okHttpClient.newCall(builder);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                log.info("=======1111", response.body().string());
            }
        });

        Thread.currentThread().join();
    }

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
}
