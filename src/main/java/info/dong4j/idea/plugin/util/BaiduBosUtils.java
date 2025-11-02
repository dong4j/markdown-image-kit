package info.dong4j.idea.plugin.util;

import info.dong4j.idea.plugin.enums.ImageMediaType;
import info.dong4j.idea.plugin.util.digest.HmacUtils;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import lombok.extern.slf4j.Slf4j;

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
@Slf4j
public class BaiduBosUtils {
    /**
     * 向百度BOS上传对象并返回上传结果
     * <p>
     * 该方法用于将指定的输入流内容上传到百度BOS，支持自定义端点和标准端点。上传完成后，会根据响应结果返回相应的字符串信息。
     *
     * @param key              上传对象的键（Key）
     * @param content          要上传的输入流内容
     * @param ossBucket        BOS存储桶名称
     * @param endpoint         BOS服务端点
     * @param accessKeyId      访问密钥ID
     * @param secretAccessKey  访问密钥
     * @param isCustomEndpoint 是否使用自定义端点
     * @param customEndpoint   自定义端点地址
     * @return 上传结果的字符串表示
     * @throws Exception 上传过程中发生异常
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
        // 1. UTC 时间
        String date = getUTCDate();

        // 2. 前缀字符串: bce-auth-v1/{accessKeyId}/{timestamp}/{expirationPeriodInSeconds}
        String expirationPeriodInSeconds = "1800";
        String authStringPrefix = "bce-auth-v1/" + accessKeyId + "/" + date + "/" + expirationPeriodInSeconds;
        // 3. 计算 signingKey
        String signingKey = HmacUtils.hmacSha256Hex(secretAccessKey, authStringPrefix);
        // 4. 生成 CanonicalRequest
        // HTTP Method + "\n" + CanonicalURI + "\n" + CanonicalQueryString + "\n" + CanonicalHeaders
        String canonicalURI = normalizeURI(key);
        String canonicalQueryString = "";
        // 确定实际使用的host
        String actualHost = isCustomEndpoint ? customEndpoint : endpoint;
        // CanonicalHeaders格式：headerName:headerValue\n（按字典序排列）
        // 注意：值不应该进行编码，直接使用原始值
        String canonicalHeaders = "host:" + ossBucket + "." + actualHost;
        String signedHeaders = "host";
        // 规范请求
        String canonicalRequest = "PUT\n" + canonicalURI + "\n" + canonicalQueryString + "\n" + canonicalHeaders;
        // 5. 计算 signature
        String signature = HmacUtils.hmacSha256Hex(signingKey, canonicalRequest);
        // 6. 生成 authorization: bce-auth-v1/{accessKeyId}/{timestamp}/{expirationPeriodInSeconds}/{signedHeaders}/{signature}
        String authorization = authStringPrefix + "/" + signedHeaders + "/" + signature;
        // 读取 InputStream 到字节数组以获取准确的 Content-Length
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        IOUtils.copy(content, baos);
        byte[] contentBytes = baos.toByteArray();
        int contentLength = contentBytes.length;

        String connectUrl = "https://" + ossBucket + "." + endpoint;
        if (isCustomEndpoint) {
            connectUrl = "http://" + customEndpoint;
        }

        HttpURLConnection connection = OssUtils.connect(connectUrl + key, "PUT");
        StringBuffer sbuffer;
        //设置请求头
        connection.setRequestProperty("Date", getHttpDate());
        connection.setRequestProperty("Authorization", authorization);
        connection.setRequestProperty("Content-Length", String.valueOf(contentLength));
        connection.setRequestProperty("Content-Type", getContentType(key));
        // 注意：Host 头不需要手动设置，HttpURLConnection 会根据 URL 自动设置
        connection.connect();

        try (OutputStream out = connection.getOutputStream();
             ByteArrayInputStream contentStream = new ByteArrayInputStream(contentBytes)) {
            IOUtils.copy(contentStream, out);
            // 读取响应
            if (connection.getResponseCode() == 200) {
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
            } else {
                throw new RuntimeException(connection.getResponseCode() + " " + connection.getResponseMessage());
            }
        } finally {
            // 断开连接
            connection.disconnect();
        }
        return key;
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

    /**
     * 生成HTTP标准的Date头字符串，格式为RFC 1123，例如：Wed, 06 Apr 2016 06:34:40 GMT
     * <p>
     * 该方法使用系统当前时间，并将其转换为UTC时区的时间，按照RFC 1123格式格式化后返回。
     *
     * @return HTTP标准的Date头字符串
     * @since 0.0.1
     */
    private static String getHttpDate() {
        return DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss 'GMT'", Locale.ENGLISH)
            .format(Instant.now().atZone(ZoneOffset.UTC));
    }

    /**
     * URL编码方法
     * <p>
     * 使用Java标准库URLEncoder对字符串进行编码
     * 例如：2025-10-29T14:22:55Z -> 2025-10-29T14%3A22%3A55Z
     *
     * @param str 待编码的字符串
     * @return 编码后的字符串
     * @since 0.0.1
     */
    private static String urlEncode(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return URLEncoder.encode(str, StandardCharsets.UTF_8);
    }

    /**
     * 根据文件名获取Content-Type
     * <p>
     * 根据文件扩展名返回对应的MIME类型，如果无法识别则返回application/octet-stream
     *
     * @param filename 文件名或文件路径
     * @return Content-Type字符串
     * @since 0.0.1
     */
    private static String getContentType(String filename) {
        if (filename == null || filename.isEmpty()) {
            return "application/octet-stream";
        }
        return ImageMediaType.fromFileName(filename, "application/octet-stream");
    }

    /**
     * 规范化URI路径（用于CanonicalURI）
     * <p>
     * 根据百度BOS规范，确保URI路径以/开头并正确编码
     *
     * @param uri 原始URI路径
     * @return 规范化后的URI路径
     * @since 0.0.1
     */
    private static String normalizeURI(String uri) {
        if (uri == null || uri.isEmpty()) {
            return "/";
        }

        // 确保以/开头
        if (!uri.startsWith("/")) {
            uri = "/" + uri;
        }

        // 分段处理，保留斜杠，只编码每个路径段
        String[] parts = uri.split("/", -1);
        StringBuilder result = new StringBuilder("/");
        for (int i = 1; i < parts.length; i++) {
            if (i > 1) {
                result.append("/");
            }
            if (!parts[i].isEmpty()) {
                result.append(urlEncode(parts[i]));
            }
        }

        return result.toString();
    }
}
