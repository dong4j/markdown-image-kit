/*
 * MIT License
 *
 * Copyright (c) 2019 dong4j <dong4j@gmail.com>
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

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.junit.Test;

import java.io.*;

import lombok.extern.slf4j.Slf4j;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * <p>Company: 科大讯飞股份有限公司-四川分公司</p>
 * <p>Description: ${description}</p>
 *
 * @author dong4j
 * @date 2019-04-02 14:23
 * @email sjdong3@iflytek.com
 */
@Slf4j
public class SmmsClientTest {
    @Test
    public void test() throws Exception {
        upload("");
        // upload("", new File("/Users/dong4j/Downloads/mik.png"));
        // log.info("{}", upload("https://sm.ms/api/upload", "/Users/dong4j/Downloads/mik.png", "mik.png"));
        // upload();
        doPostupload(new File("/Users/dong4j/Downloads/mik.png"), 0, "https://sm.ms/api/upload/");
    }

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

    public String upload(String imageType, File file) {
        RequestBody fileBody = RequestBody.create(MediaType.parse("image/png"), file);
        RequestBody requestBody = new MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("smfile", file.getName(), fileBody)
            .build();

        Request request = new Request.Builder()
            .url("http://sm.ms/api/upload")
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
                int errorCode = jsonObject.getInt("errorCode");
                if (errorCode == 0) {
                    log.trace(" upload data =" + jsonObject.getString("data"));
                    return jsonObject.getString("data");
                } else {
                    throw new RuntimeException("upload error code " + errorCode + ",errorInfo=" + jsonObject.getString("errorInfo"));
                }
            }

        } catch (IOException e) {
            log.trace("upload IOException ", e);
        } catch (JSONException e) {
            log.trace("upload JSONException ", e);
        }
        return null;
    }

    public static ResponseBody upload(String url, String filePath, String fileName) throws Exception {
        OkHttpClient client = new OkHttpClient();
        RequestBody requestBody = new MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            // .addFormDataPart("smfile", fileName, RequestBody.create(MediaType.parse("multipart/form-data"), new File(filePath)))
            .addFormDataPart("ssl", "false")
            .addFormDataPart("name", "smfile")
            .addFormDataPart("filename", "fileName")
            .build();

        Request request = new Request.Builder()
            .url(url)
            .addHeader("Content-Type", "multipart/form-data")
            .addHeader("cache-control", "no-cache")
            .post(requestBody)
            .build();

        Response response = client.newCall(request).execute();
        if (!response.isSuccessful()) {
            throw new IOException("Unexpected code " + response);
        }

        return response.body();
    }

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


    public static String doPostupload(File file, int type, String url) {
        HttpClient httpClient = new DefaultHttpClient();
        HttpPost httpPost = null;

        // 把文件转换成流对象FileBody
        FileBody bin = new FileBody(file);
        try {
            //***************注意这里的代码******
            // httpClient = new SSLClient();
            httpPost = new HttpPost(url);
            MultipartEntity reqEntity = new MultipartEntity();
            //封装其他参数到Stringbody（需要把int转成String再放入）
            StringBody username = new StringBody("张三");
            StringBody password = new StringBody("123456");
            // StringBody type1 = new StringBody(String.valueOf(type));//type为int
            //参数放入请求实体（包括文件和其他参数）
            reqEntity.addPart("smfile", bin);
            // reqEntity.addPart("type", type1);
            httpPost.setEntity(reqEntity);

            HttpResponse httpResponse = httpClient.execute(httpPost);
            log.info("{}", httpResponse);

            //String body = result.getResponseString(); // body即为服务器返回的内容

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return "";
    }

    @Test
    public void test1(){
    }
}