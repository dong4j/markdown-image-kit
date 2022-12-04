/*
 * MIT License
 *
 * Copyright (c) 2022 dong4j <dong4j@gmail.com>
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

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import lombok.Builder;
import lombok.Data;

/**
 * <p>Description: </p>
 * <a href="https://gitee.com/api/v5/swagger#/postV5ReposOwnerRepoContentsPath">...</a>
 *
 * @author dong4j
 * @version 1.0.0
 * @email "mailto:dong4j@gmail.com"
 * @date 2020.04.21 23:29
 * @since 1.4.0
 */
public class GiteeUtils {
    /** GITEE_API */
    private static final String GITEE_API = "https://gitee.com/api/v5";
    /** DOWNLOAD_URL */
    private static final String DOWNLOAD_URL = "https://gitee.com/{owner}/{repos}/raw/{branch}{path}";

    /**
     * Put oss obj string
     *
     * @param key     key
     * @param content content
     * @param repos   {owner}/{repos}
     * @param branch  branch
     * @param token   token
     * @return the string
     * @throws Exception exception
     * @since 1.3.0
     */
    public static void putObject(String key,
                                 InputStream content,
                                 String repos,
                                 String branch,
                                 String token) throws Exception {

        String url = GITEE_API + "/repos/" + repos + "/contents" + key;
        new GiteeOpenAPI().create(url, content, token, branch);
    }

    /**
     * <p>Description: </p>
     *
     * @author dong4j
     * @version 1.0.0
     * @email "mailto:dong4j@fkhwl.com"
     * @date 2021.02.17 15:42
     * @since 1.4.0
     */
    private static class GiteeOpenAPI implements OpenAPI {

        /**
         * Build request
         *
         * @param branch  branch
         * @param content content
         * @param token   token
         * @return the string
         * @since 1.4.0
         */
        @Override
        public String buildRequest(String branch, String content, String token) {
            return new Gson().toJson((GiteeRequest.builder()
                .message("markdown-image-kit uploaded")
                .branch(branch)
                .content(content)
                .token(token)
                .build()));
        }

        /**
         * Gets http url connection *
         *
         * @param url   url
         * @param token token
         * @return the http url connection
         * @throws IOException io exception
         * @since 1.4.0
         */
        @Override
        public HttpURLConnection getHttpURLConnection(String url, String token) throws IOException {
            URL realUrl = new URL(url);
            HttpURLConnection connection = (HttpURLConnection) realUrl.openConnection();
            connection.setConnectTimeout(3000);
            connection.setReadTimeout(5000);
            // 设置
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
            connection.setRequestProperty("User-Agent", "markdown-image-kit");
            return connection;
        }
    }

    /**
     * <p>Description: </p>
     *
     * @author dong4j
     * @version 1.0.0
     * @email "mailto:dong4j@fkhwl.com"
     * @date 2021.02.17 15:28
     * @since 1.4.0
     */
    @Data
    @Builder
    private static class GiteeRequest {
        /** Message */
        private String message;
        /** Branch */
        private String branch;
        /** Content */
        private String content;
        /** Sha */
        private String sha;
        /** Token */
        @SerializedName(value = "access_token")
        private String token;
    }


}
