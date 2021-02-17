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

package info.dong4j.idea.plugin.util;


import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import com.intellij.openapi.util.io.FileUtil;

import org.junit.Test;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import lombok.Builder;
import lombok.Data;

/**
 * <p>Company: 成都返空汇网络技术有限公司</p>
 * <p>Description:  </p>
 *
 * @author dong4j
 * @version 1.0.0
 * @email "mailto:dong4j@gmail.com"
 * @date 2020.04.25 16:36
 * @since 1.1.0
 */
public class GiteeUtilsTest {

    private static final String GITEE_API = "https://gitee.com/api/v5";
    private static final String DOWNLOAD_URL = "https://gitee.com/{owner}/{repos}/raw/{branch}{path}";
    /** token */
    private static final String token = System.getProperty("token");
    private static final String repos = "markdown-image-kit-test";
    private static final String owner = "dong4j";
    private static final String path = "/xx.png";

    /**
     * https://gitee.com/api/v5/swagger#/postV5ReposOwnerRepoContentsPath
     *
     * @throws IOException io exception
     * @since 1.1.0
     */
    @Test
    public void test() throws Exception {
        // https://gitee.com/api/v5/repos/{owner}/{repo}/contents/{path}
        String url = GITEE_API + "/repos/" + owner + "/" + repos + "/contents" + path;
        File file = new File("/Users/dong4j/Downloads/xu.png");
        boolean result = create(url, file, token);
        System.out.println(result);
    }

    /**
     * 由于没有考虑 sha，故而只能新建文件，而不能更新文件 (更新文件需要先 get 访问得到 sha，然后再 put)
     *
     * @param url   https://api.github.com/repos/:owner/:repo/contents/:path
     * @param file  需确保文件存在
     * @param token 用于鉴权
     * @return
     */
    public static boolean create(String url, File file, String token) throws Exception {
        URL realUrl = new URL(url);
        HttpURLConnection connection = (HttpURLConnection) realUrl.openConnection();
        connection.setConnectTimeout(120000);
        connection.setReadTimeout(120000);
        // 设置
        connection.setDoOutput(true); // 需要输出
        connection.setDoInput(true); // 需要输入
        connection.setUseCaches(false); // 不允许缓存
        connection.setRequestMethod("POST"); // 设置 PUT 方式连接
        connection.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
        connection.setRequestProperty("User-Agent", "markdown-image-kit");
        connection.connect();

        StringBuffer sbuffer;
        try (DataOutputStream dos = new DataOutputStream(connection.getOutputStream())) {
            String content = Base64Utils.encodeToString(FileUtil.loadFileBytes(file));
            GithubRequest request = GithubRequest.builder()
                .message("markdown-image-kit uploaded")
                .branch("master")
                .content(content)
                .token(token)
                .build();

            dos.writeBytes(new Gson().toJson(request));

            int responseCode = connection.getResponseCode();
            // 读取响应
            if (responseCode == 200 || responseCode == 201) {
                try (InputStreamReader inputStream = new InputStreamReader(connection.getInputStream());
                     BufferedReader reader = new BufferedReader(inputStream)) {
                    String lines;
                    sbuffer = new StringBuffer();

                    while ((lines = reader.readLine()) != null) {
                        lines = new String(lines.getBytes(), StandardCharsets.UTF_8);
                        sbuffer.append(lines);
                    }
                    System.out.println(sbuffer);
                }
                return true;
            } else if (responseCode == 422) {
                return true;
            } else if (responseCode == 400) {
                throw new RuntimeException(responseCode + " " + connection.getResponseMessage()
                                           + " :" + path + " 可能已存在");
            } else {
                throw new RuntimeException(responseCode + " " + connection.getResponseMessage());
            }
        } finally {
            // 断开连接
            connection.disconnect();
        }
    }

    @Data
    @Builder
    private static class GithubRequest {
        private String message;
        private String branch;
        private String content;
        private String sha;
        @SerializedName(value = "access_token")
        private String token;
    }

}
