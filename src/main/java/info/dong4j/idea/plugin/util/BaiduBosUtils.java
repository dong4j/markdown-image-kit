package info.dong4j.idea.plugin.util;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

/**
 * 百度对象存储（BOS）工具类
 * <p>
 * 提供与百度对象存储服务相关的操作工具方法，包括对象上传、签名时间生成等功能。
 * 该类封装了百度BOS服务的调用逻辑，简化了开发者在使用BOS服务时的配置和操作。
 *
 * @author dong4j
 * @version 0.0.1
 * @date 2020.04.21
 * @since 0.0.1
 */
public class BaiduBosUtils {
    /**
     * 向阿里云OSS上传对象并返回上传结果
     * <p>
     * 该方法用于将指定的输入流内容上传到阿里云OSS，支持自定义端点和标准端点。上传完成后，会根据响应结果返回相应的字符串信息。
     *
     * @param key              上传对象的键（Key）
     * @param content          要上传的输入流内容
     * @param ossBucket        OSS存储桶名称
     * @param endpoint         OSS服务端点
     * @param accessKeyId      访问密钥ID
     * @param secretAccessKey  访问密钥
     * @param isCustomEndpoint 是否使用自定义端点
     * @param customEndpoint   自定义端点地址
     * @return 上传结果的字符串表示
     * @throws IOException 上传过程中发生I/O异常
     * @since 0.0.1
     */
    public static String putObject(String key,
                                   InputStream content,
                                   String ossBucket,
                                   String endpoint,
                                   String accessKeyId,
                                   String secretAccessKey,
                                   boolean isCustomEndpoint,
                                   String customEndpoint) throws Exception {
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
     * 生成当前 UTC 时间的字符串表示，格式为 yyyy-mm-ddThh:mm:ssZ，例如：2015-04-27T08:23:49Z
     * <p>
     * 该方法使用系统当前时间，并将其转换为 UTC 时区的时间，再按照指定格式格式化为字符串返回。
     *
     * @return 当前 UTC 时间的字符串表示
     * @since 1.1.0
     */
    public static String getUTCDate() {
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        return now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'"));
    }
}
