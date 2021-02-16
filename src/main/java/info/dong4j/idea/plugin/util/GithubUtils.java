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

import com.intellij.openapi.util.io.FileUtil;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import lombok.Builder;
import lombok.Data;

/**
 * <p>Company: 成都返空汇网络技术有限公司 </p>
 * <p>Description: </p>
 *
 * @author dong4j
 * @version 1.0.0
 * @email "mailto:dong4j@gmail.com"
 * @date 2020.04.21 23:29
 * @since 1.3.0
 */
public class GithubUtils {
    /** GITHUB_API */
    private static final String GITHUB_API = "https://api.github.com";
    /** DOWNLOAD_URL */
    private static final String DOWNLOAD_URL = "https://raw.githubusercontent.com/{owner}/{repos}/{branch}{path}";

    /**
     * Put oss obj string
     *
     * @param key     key
     * @param content content
     * @param repos   拥有者/仓库名
     * @param branch  branch
     * @param token   token
     * @return the string
     * @throws Exception exception
     * @since 1.3.0
     */
    public static String putObject(String key,
                                   InputStream content,
                                   String repos,
                                   String branch,
                                   String token) throws Exception {

        String url = GITHUB_API + "/repos/" + repos + "/contents" + key;
        create(url, content, token, branch);
        // "https://raw.githubusercontent.com/{owner}/{repos}/{branch}{path}";
        return "https://raw.githubusercontent.com/" + repos + "/" + branch + key;
    }

    /**
     * 由于没有考虑 sha，故而只能新建文件，而不能更新文件 (更新文件需要先 get 访问得到 sha，然后再 put)
     *
     * @param url        https://api.github.com/repos/:owner/:repo/contents/:path
     * @param fileStream file stream
     * @param token      用于鉴权
     * @param branch     branch
     * @return boolean boolean
     * @throws Exception exception
     * @since 1.3.0
     */
    public static boolean create(String url, InputStream fileStream, String token, String branch) throws Exception {
        URL realUrl = new URL(url);
        HttpURLConnection connection = (HttpURLConnection) realUrl.openConnection();
        connection.setConnectTimeout(5000);
        connection.setReadTimeout(10000);
        // 设置
        connection.setDoOutput(true);
        connection.setDoInput(true);
        connection.setUseCaches(false);
        connection.setRequestMethod("PUT");

        connection.setRequestProperty("Content-Type", "application/vnd.github.v3+json");
        connection.setRequestProperty("Authorization", "token " + token);
        connection.setRequestProperty("User-Agent", "markdown-image-kit");
        connection.connect();

        StringBuffer sbuffer;
        try (DataOutputStream dos = new DataOutputStream(connection.getOutputStream())) {
            String content = Base64Utils.encodeToString(FileUtil.adaptiveLoadBytes(fileStream));

            // 主分支兼容处理
            branch = StringUtils.isNotBlank(branch) && branch.equals("master") ? "main" : branch;

            GithubRequest request = GithubRequest.builder()
                .message("markdown-image-kit uploaded")
                .branch(branch)
                .content(content)
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
                }
                return true;
            } else if (responseCode == 422) {
                // 已存在相同文件, 这里直接返回
                return true;
            } else {
                throw new RuntimeException(responseCode + " " + connection.getResponseMessage());
            }
        } finally {
            // 断开连接
            connection.disconnect();
        }
    }

    /**
     * <p>Company: 成都返空汇网络技术有限公司 </p>
     * <p>Description: </p>
     *
     * @author dong4j
     * @version 1.0.0
     * @email "mailto:dong4j@fkhwl.com"
     * @date 2021.02.16 20:17
     * @since 1.3.0
     */
    @Data
    @Builder
    private static class GithubRequest {
        /** Message */
        private String message;
        /** Branch */
        private String branch;
        /** Content */
        private String content;
        /** Sha */
        private String sha;
    }

}
