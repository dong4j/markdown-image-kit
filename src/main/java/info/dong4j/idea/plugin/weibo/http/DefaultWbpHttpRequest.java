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

package info.dong4j.idea.plugin.weibo.http;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.net.HttpURLConnection.HTTP_OK;

/**
 * <p>Description: </p>
 *
 * @author echisan
 * @version 0.0.1
 * @email "mailto:dong4j@gmail.com"
 * @date 2018.06.14 22:31
 * @update dong4j
 * @since 0.0.1
 */
public class DefaultWbpHttpRequest implements WbpHttpRequest {
    /** Header */
    private Map<String, String> header;

    /**
     * Instantiates a new Default wbp http request.
     *
     * @since 0.0.1
     */
    public DefaultWbpHttpRequest() {
    }

    /**
     * 初始化header
     *
     * @param header the header
     * @since 0.0.1
     */
    public DefaultWbpHttpRequest(Map<String, String> header) {
        this.header = header;
    }

    /**
     * Gets header *
     *
     * @return the header
     * @since 0.0.1
     */
    @Override
    public Map<String, String> getHeader() {
        return this.header;
    }

    /**
     * 增添header，新的header将替换旧的
     *
     * @param header the header
     * @since 0.0.1
     */
    @Override
    public void setHeader(Map<String, String> header) {
        Set<Map.Entry<String, String>> entries = header.entrySet();
        entries.forEach(stringStringEntry -> {
            this.header.put(stringStringEntry.getKey(), stringStringEntry.getValue());
        });
    }

    /**
     * Do get
     *
     * @param url    url
     * @param header header
     * @param params params
     * @return the wbp http response
     * @throws IOException io exception
     * @since 0.0.1
     */
    @Override
    public WbpHttpResponse doGet(String url, Map<String, String> header, Map<String, String> params) throws IOException {
        if (params != null) {
            url = url + "?" + this.convertParams(params);
        }

        URL u = new URL(url);
        HttpURLConnection connection = (HttpURLConnection) u.openConnection();
        connection.setRequestMethod("GET");

        if (header != null) {
            header.forEach(connection::setRequestProperty);
        }

        connection.connect();
        return new DefaultWbpHttpResponse(
            connection.getResponseCode(),
            this.getHeaderFromConnection(connection),
            this.getBodyFromConnection(connection)
        );
    }

    /**
     * Do get
     *
     * @param url    url
     * @param params params
     * @return the wbp http response
     * @throws IOException io exception
     * @since 0.0.1
     */
    @Override
    public WbpHttpResponse doGet(String url, Map<String, String> params) throws IOException {
        return this.doGet(url, this.header, params);
    }

    /**
     * Do get
     *
     * @param url url
     * @return the wbp http response
     * @throws IOException io exception
     * @since 0.0.1
     */
    @Override
    public WbpHttpResponse doGet(String url) throws IOException {
        return this.doGet(url, this.header, null);
    }

    /**
     * Do post
     *
     * @param url    url
     * @param header header
     * @param params params
     * @return the wbp http response
     * @throws IOException io exception
     * @since 0.0.1
     */
    @Override
    public WbpHttpResponse doPost(String url, Map<String, String> header, Map<String, String> params) throws IOException {

        URL u = new URL(url);
        HttpURLConnection connection = (HttpURLConnection) u.openConnection();
        connection.setRequestMethod("POST");
        connection.setDoInput(true);
        connection.setDoOutput(true);
        connection.setUseCaches(false);

        if (header != null) {
            header.forEach(connection::setRequestProperty);
        }
        connection.connect();

        if (params != null) {
            String requestBody = this.convertParams(params);
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(connection.getOutputStream()));
            bw.write(requestBody);
            bw.flush();
            bw.close();
        }
        return new DefaultWbpHttpResponse(
            connection.getResponseCode(),
            this.getHeaderFromConnection(connection),
            this.getBodyFromConnection(connection)
        );
    }

    /**
     * Do post
     *
     * @param url    url
     * @param params params
     * @return the wbp http response
     * @throws IOException io exception
     * @since 0.0.1
     */
    @Override
    public WbpHttpResponse doPost(String url, Map<String, String> params) throws IOException {
        return this.doPost(url, this.header, params);
    }

    /**
     * Do post
     *
     * @param url url
     * @return the wbp http response
     * @throws IOException io exception
     * @since 0.0.1
     */
    @Override
    public WbpHttpResponse doPost(String url) throws IOException {
        return this.doPost(url, null);
    }

    /**
     * Do post multi part
     *
     * @param url     url
     * @param header  header
     * @param content content
     * @return the wbp http response
     * @throws IOException io exception
     * @since 0.0.1
     */
    @Override
    public WbpHttpResponse doPostMultiPart(String url, Map<String, String> header, String content) throws IOException {
        URL u = new URL(url);
        HttpURLConnection connection = (HttpURLConnection) u.openConnection();
        connection.setRequestMethod("POST");
        connection.setDoInput(true);
        connection.setDoOutput(true);
        connection.setUseCaches(false);

        if (header != null) {
            header.forEach(connection::setRequestProperty);
        }
        String END_LINE = "\r\n";
        String TWO = "--";
        String boundary = "===" + System.currentTimeMillis() + "===";
        String contentType = "multipart/form-data; boundary=" + boundary;
        connection.setRequestProperty("Content-Type", contentType);
        StringBuilder bodyBulider = new StringBuilder();
        bodyBulider.append(TWO).append(boundary).append(END_LINE)
            .append("Content-Disposition: form-data; name=\"b64_data\"")
            .append(END_LINE).append(END_LINE)
            .append(content)
            .append(END_LINE)
            .append(TWO).append(boundary).append(TWO);
        connection.connect();

        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(connection.getOutputStream()));

        bw.write(bodyBulider.toString());
        bw.flush();
        bw.close();
        return new DefaultWbpHttpResponse(
            connection.getResponseCode(),
            this.getHeaderFromConnection(connection),
            this.getBodyFromConnection(connection)
        );
    }

    /**
     * Gets header from connection *
     *
     * @param connection connection
     * @return the header from connection
     * @since 0.0.1
     */
    private Map<String, String> getHeaderFromConnection(HttpURLConnection connection) {
        Map<String, List<String>> headerFields = connection.getHeaderFields();

        List<String> list = headerFields.get("Set-Cookie");
        String cookie = null;
        if (list != null) {
            StringBuilder sb = new StringBuilder();
            for (String s : list) {
                int i = s.indexOf(";");
                if (i != -1) {
                    String substring = s.substring(0, i);
                    sb.append(substring).append("; ");
                } else {
                    break;
                }
            }
            cookie = sb.toString();
            if (cookie.length() != 0) {
                cookie = cookie.substring(0, cookie.length() - 2);
            }
        }

        Map<String, String> header = new HashMap<>();
        Set<Map.Entry<String, List<String>>> entries = headerFields.entrySet();
        entries.forEach(e -> {
            StringBuilder sb = new StringBuilder();
            List<String> values = e.getValue();
            for (String s : values) {
                sb.append(s);
            }
            header.put(e.getKey(), sb.toString());
        });
        header.put("Set-Cookie", cookie);
        return header;
    }

    /**
     * Gets body from connection *
     *
     * @param connection connection
     * @return the body from connection
     * @throws IOException io exception
     * @since 0.0.1
     */
    private String getBodyFromConnection(HttpURLConnection connection) throws IOException {
        int responseCode = connection.getResponseCode();

        if (responseCode == HTTP_OK) {
            InputStream inputStream = connection.getInputStream();
            return this.readInputStream(inputStream);
        } else {
            InputStream errorStream = connection.getErrorStream();
            return this.readInputStream(errorStream);
        }
    }

    /**
     * Read input stream
     *
     * @param is is
     * @return the string
     * @throws IOException io exception
     * @since 0.0.1
     */
    private String readInputStream(InputStream is) throws IOException {
        String str;
        StringBuilder stringBuilder = new StringBuilder();
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(is));
        while ((str = bufferedReader.readLine()) != null) {
            stringBuilder.append(str);
        }
        return stringBuilder.toString();
    }
}
