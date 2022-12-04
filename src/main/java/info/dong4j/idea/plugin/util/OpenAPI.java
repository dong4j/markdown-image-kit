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

import com.intellij.openapi.util.io.FileUtil;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;

/**
 * <p>Description:  </p>
 *
 * @author dong4j
 * @version 1.0.0
 * @email "mailto:dong4j@fkhwl.com"
 * @date 2021.02.17 15:38
 * @since 1.4.0
 */
public interface OpenAPI {

    /**
     * Create
     *
     * @param url        url
     * @param fileStream file stream
     * @param token      token
     * @param branch     branch
     * @return the boolean
     * @throws Exception exception
     * @since 1.4.0
     */
    default boolean create(String url,
                           InputStream fileStream,
                           String token,
                           String branch) throws Exception {
        HttpURLConnection connection = this.getHttpURLConnection(url, token);
        connection.setDoOutput(true);
        connection.setUseCaches(false);
        connection.connect();

        try (DataOutputStream dos = new DataOutputStream(connection.getOutputStream())) {
            String content = Base64Utils.encodeToString(FileUtil.adaptiveLoadBytes(fileStream));

            dos.writeBytes(this.buildRequest(branch, content, token));

            int responseCode = connection.getResponseCode();
            // 读取响应
            if (responseCode == 200 || responseCode == 201) {
                return true;
            } else if (responseCode == 422 || responseCode == 400) {
                // 已存在相同文件, 这里直接返回
                return true;
            } else if (responseCode == 404) {
                throw new RuntimeException(responseCode + " " + connection.getResponseMessage()
                                           + " :The branch (" + branch + ") may not be created");
            } else {
                throw new RuntimeException(responseCode + " " + connection.getResponseMessage());
            }
        } finally {
            // 断开连接
            connection.disconnect();
        }
    }

    /**
     * Build request
     *
     * @param branch  branch
     * @param content content
     * @param token   token
     * @return the string
     * @since 1.4.0
     */
    String buildRequest(String branch, String content, String token);

    /**
     * Gets http url connection *
     *
     * @param url   url
     * @param token token
     * @return the http url connection
     * @throws IOException io exception
     * @since 1.4.0
     */
    HttpURLConnection getHttpURLConnection(String url, String token) throws IOException;
}
