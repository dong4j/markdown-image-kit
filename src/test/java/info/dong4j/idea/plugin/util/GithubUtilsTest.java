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

import com.intellij.openapi.util.io.FileUtil;

import org.junit.Test;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.Builder;
import lombok.Data;

/**
 * <p>Description:  </p>
 *
 * @author dong4j
 * @version 1.0.0
 * @email "mailto:dong4j@gmail.com"
 * @date 2020.04.25 16:36
 * @since 1.1.0
 */
public class GithubUtilsTest {
    private static final String GITHUB_API = "https://api.github.com";
    private static final String DOWNLOAD_URL = "https://raw.githubusercontent.com/{owner}/{repos}/{branch}{path}";
    /** token */
    private static final String token = System.getProperty("token");
    private static final String repos = "markdown-image-kit-test";
    private static final String owner = "dong4j";
    private static final String path = "/xu.png";

    /**
     * https://docs.github.com/en/rest/reference/repos#create-or-update-file-contents
     *
     * @throws IOException io exception
     * @since 1.1.0
     */
    @Test
    public void test() throws Exception {
        String url = GITHUB_API + "/repos/" + owner + "/" + repos + "/contents" + path;
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
        connection.setRequestMethod("PUT"); // 设置 PUT 方式连接

        connection.setRequestProperty("Content-Type", "application/vnd.github.v3+json");
        connection.setRequestProperty("Authorization", "token " + token);
        connection.setRequestProperty("User-Agent", "markdown-image-kit");
        connection.connect();

        StringBuffer sbuffer;
        try (DataOutputStream dos = new DataOutputStream(connection.getOutputStream())) {
            String content = Base64Utils.encodeToString(FileUtil.loadFileBytes(file));
            GithubRequest request = GithubRequest.builder()
                .message("markdown-image-kit uploaded")
                .branch("main")
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
                    System.out.println(sbuffer);
                }
            } else if (responseCode == 422) {
                update(url, file, token);
            } else {
                throw new RuntimeException(responseCode + " " + connection.getResponseMessage());
            }
        } finally {
            // 断开连接
            connection.disconnect();
        }

        return true;
    }

    /**
     * 更新文件(先get访问得到sha，然后再put)
     *
     * @param url   https://api.github.com/repos/:owner/:repo/contents/:path
     * @param file  需确保文件存在
     * @param token 用于鉴权
     * @return
     */
    public static boolean update(String url, File file, String token) {
        long begin = System.currentTimeMillis();
        System.out.println("获取文件SHA...");
        String sha = getSHA(url);
        if (sha == null) {
            return false;
        }
        long end = System.currentTimeMillis();
        System.out.printf("获取文件SHA 耗时 %ds\n", (end - begin) / 1000);
        System.out.println("上传开始...");
        //StringBuffer result = new StringBuffer();
        BufferedReader in = null;
        HttpURLConnection conn = null;
        try {
            URL realUrl = new URL(url);
            conn = (HttpURLConnection) realUrl.openConnection();
            conn.setConnectTimeout(120000);
            conn.setReadTimeout(120000);
            // 设置
            conn.setDoOutput(true); // 需要输出
            conn.setDoInput(true); // 需要输入
            conn.setUseCaches(false); // 不允许缓存
            conn.setRequestMethod("PUT"); // 设置PUT方式连接

            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Authorization", "token " + token);
            conn.setRequestProperty("User-Agent", "Github File Uploader App");
            conn.connect();
            // 传输数据
            DataOutputStream dos = new DataOutputStream(conn.getOutputStream());
            // 传输json头部
            dos.writeBytes("{\"message\":\".\",\"sha\":\"" + sha + "\",\"content\":\"");
            // 传输文件内容
            byte[] buffer = new byte[1024 * 1002]; // 3的倍数
            RandomAccessFile raf = new RandomAccessFile(file, "r");
            long size = raf.read(buffer);
            while (size > -1) {
                if (size == buffer.length) {
                    dos.write(Base64.getEncoder().encode(buffer));
                } else {
                    byte tmp[] = new byte[(int) size];
                    System.arraycopy(buffer, 0, tmp, 0, (int) size);
                    dos.write(Base64.getEncoder().encode(tmp));
                }
                size = raf.read(buffer);
            }
            raf.close();
            // 传输json尾部
            dos.writeBytes("\"}");
            dos.flush();
            dos.close();

            in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line;
            while ((line = in.readLine()) != null) {
                //result.append(line).append("\n");
            }
        } catch (Exception e) {
            System.out.println("发送PUT请求出现异常！");
            e.printStackTrace();
            return false;
        } finally {
            try {
                in.close();
            } catch (Exception e2) {
            }
        }
        end = System.currentTimeMillis();
        System.out.printf("上传结束，耗时 %ds\n", (end - begin) / 1000);
        //result.toString()
        return true;
    }

    /**
     * 获取url 对应的SHA
     *
     * @param url
     * @param token
     * @return
     */
    static Pattern pattern = Pattern.compile("\"sha\": *\"([^\"]+)\"");

    public static String getSHA(String url) {
        StringBuffer result = new StringBuffer();
        BufferedReader in = null;
        HttpURLConnection conn = null;
        try {
            URL realUrl = new URL(url);
            conn = (HttpURLConnection) realUrl.openConnection();
            conn.setConnectTimeout(120000);
            conn.setReadTimeout(120000);
            conn.setRequestMethod("GET");
            conn.setRequestProperty("User-Agent", "Github File Uploader App");
            conn.connect();
            in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line;
            while ((line = in.readLine()) != null) {
                result.append(line).append("\n");
            }
            Matcher matcher = pattern.matcher(result.toString());
            if (matcher.find()) {
                return matcher.group(1);
            }
        } catch (Exception e) {
            System.out.println("请求SHA出现异常！");
            e.printStackTrace();
        } finally {
            try {
                in.close();
            } catch (Exception e2) {
            }
        }
        return null;
    }

    @Data
    @Builder
    private static class GithubRequest {
        private String message;
        private String branch;
        private String content;
        private String sha;
    }

}
