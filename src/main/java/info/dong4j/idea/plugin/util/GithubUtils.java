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

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import lombok.Builder;
import lombok.Data;

/**
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

        String url = GITHUB_API + "/repos/" + repos + "/contents" + key;
        new GithubOpenAPI().create(url, content, token, branch);
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
    private static class GithubOpenAPI implements OpenAPI {

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
            return new Gson().toJson(GithubRequest.builder()
                                         .message("markdown-image-kit uploaded")
                                         .branch(branch)
                                         .content(content)
                                         .build());
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
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(10000);
            connection.setRequestMethod("PUT");
            connection.setRequestProperty("Content-Type", "application/vnd.github.v3+json");
            connection.setRequestProperty("Authorization", "token " + token);
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
