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

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

/**
 * <p>Company: 成都返空汇网络技术有限公司 </p>
 * <p>Description: </p>
 * https://cloud.baidu.com/doc/BOS/s/Ikc5nv3wc
 *
 * @author dong4j
 * @version 0.0.1
 * @email "mailto:dong4j@gmail.com"
 * @date 2020.04.21 23:29
 * @since 0.0.1
 */
public class BaiduBosUtils {

    /**
     * Put oss obj string
     *
     * @param key              key
     * @param content          content
     * @param ossBucket        oss bucket
     * @param endpoint         endpoint
     * @param accessKeyId      access key id
     * @param secretAccessKey  secret access key
     * @param isCustomEndpoint is custom endpoint
     * @param customEndpoint   custom endpoint
     * @return the string
     * @throws IOException io exception
     * @since 0.0.1
     */
    public static String putObject(String key,
                                   InputStream content,
                                   String ossBucket,
                                   String endpoint,
                                   String accessKeyId,
                                   String secretAccessKey,
                                   boolean isCustomEndpoint,
                                   String customEndpoint) throws IOException {
        return AliyunOssUtils.putObject(key, content, ossBucket, endpoint, accessKeyId, secretAccessKey, isCustomEndpoint, customEndpoint);
        // // 1. UTC 时间
        // String date = getUTCDate();
        // // 2. 前缀字符串: bce-auth-v1/{accessKeyId}/{timestamp}/{expirationPeriodInSeconds }
        // String expirationPeriodInSeconds = 3600L * 1000 * 24 * 365 * 10 + "";
        // String authStringPrefix = "bce-auth-v1/" + accessKeyId + "/" + date + "/" + expirationPeriodInSeconds;
        // // 3. 计算 signingKey
        // String signingKey = HmacUtils.hmacSha256Hex(secretAccessKey, authStringPrefix);
        // // 4. 生成 CanonicalRequest:  HTTP Method + "\n" + CanonicalURI + "\n" + CanonicalQueryString + "\n" + CanonicalHeaders
        // String canonicalQueryString = "";
        // String canonicalHeaders = "";
        // String canonicalRequest = "PUT\n" + key + "\n" + canonicalQueryString + "\n" + canonicalHeaders;
        // // 5. 计算 signature
        // String signature = HmacUtils.hmacSha256Hex(signingKey, canonicalRequest);
        // // 6. 生成 authorization: bce-auth-v1/{accessKeyId}/{timestamp}/{expirationPeriodInSeconds }/{signedHeaders}/{signature}
        // String authorization = authStringPrefix + "//" + signature;
        // // formatter:off
        // // bce-auth-v1/aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa/2015-04-27T08:23:49Z/1800//d74a04362e6a848f5b39b15421cb449427f419c95a480fd6b8cf9fc783e2999e
        // // formatter:on
        // System.out.println(authorization);
        //
        // String connectUrl = "https://" + ossBucket + "." + endpoint;
        // if (isCustomEndpoint) {
        //     connectUrl = "http://" + customEndpoint;
        // }
        //
        // URL putUrl = new URL(connectUrl + key);
        // HttpURLConnection connection;
        // StringBuffer sbuffer;
        //
        // //添加 请求内容
        // connection = (HttpURLConnection) putUrl.openConnection();
        // //设置http连接属性
        // connection.setDoOutput(true);
        // connection.setRequestMethod("PUT");
        // //设置请求头
        // connection.setRequestProperty("Date", date);
        // connection.setRequestProperty("Authorization", authorization);
        // connection.setRequestProperty("Host", isCustomEndpoint ? customEndpoint : endpoint);
        //
        // connection.setReadTimeout(5000);
        // connection.setConnectTimeout(3000);
        // connection.connect();
        //
        // try (OutputStream out = connection.getOutputStream()) {
        //     IOUtils.copy(content, out);
        //     // 读取响应
        //     if (connection.getResponseCode() == 200) {
        //         try (InputStreamReader inputStream = new InputStreamReader(connection.getInputStream());
        //              BufferedReader reader = new BufferedReader(inputStream)) {
        //             String lines;
        //             sbuffer = new StringBuffer();
        //
        //             while ((lines = reader.readLine()) != null) {
        //                 lines = new String(lines.getBytes(), StandardCharsets.UTF_8);
        //                 sbuffer.append(lines);
        //             }
        //             System.out.println(sbuffer);
        //         }
        //     } else {
        //         throw new RuntimeException(connection.getResponseCode() + " " + connection.getResponseMessage());
        //     }
        // } finally {
        //     // 断开连接
        //     connection.disconnect();
        // }
        // return key;
    }

    /**
     * 生成签名的 UTC 时间，格式为 yyyy-mm-ddThh:mm:ssZ，例如：2015-04-27T08:23:49Z
     *
     * @return the gmt date
     * @since 1.1.0
     */
    public static String getUTCDate() {
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        return now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'"));
    }
}
