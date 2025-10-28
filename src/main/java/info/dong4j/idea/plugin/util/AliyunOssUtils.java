package info.dong4j.idea.plugin.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * 阿里云OSS工具类
 * <p>
 * 提供阿里云对象存储服务（OSS）相关的辅助方法，包括对象的获取、上传、签名生成和日期格式化等功能。
 * 该类主要用于构建OSS请求所需的签名和请求头，支持GET和PUT操作，并处理与阿里云API交互的通用逻辑。
 * <p>
 * 该工具类使用HmacSHA1算法生成签名，确保请求的安全性。
 *
 * @author dong4j
 * @version 1.0.0
 * @date 2025.10.24
 * @since 1.0.0
 */
public class AliyunOssUtils {
    /** 加密算法类型，使用 HmacSHA1 算法进行消息认证 */
    private final static String ALGORITHM = "HmacSHA1";

    /**
     * 根据指定参数获取OSS对象内容
     * <p>
     * 使用给定的OSS bucket、endpoint、访问密钥等信息构造请求URL，并通过HTTP请求获取指定key对应的OSS对象内容。
     *
     * @param key             OSS对象的唯一标识符
     * @param ossBucket       OSS存储桶名称
     * @param endpoint        OSS服务端点地址
     * @param accessKeyId     访问密钥ID
     * @param secretAccessKey 访问密钥
     * @return 获取到的OSS对象内容
     * @throws IOException 当HTTP请求过程中发生IO异常时抛出
     * @since 0.0.1
     */
    public static String getObject(String key, String ossBucket, String endpoint, String accessKeyId, String secretAccessKey) throws IOException {
        String signResourcePath = "/" + ossBucket + key;
        String url = "http://" + ossBucket + "." + endpoint;

        String date = getGMTDate();
        String Signature = (hmacSha1(buildGetSignData(date, signResourcePath), secretAccessKey));

        String Authorization = "OSS " + accessKeyId + ":" + Signature;

        Map<String, String> head = new HashMap<String, String>();
        head.put("Date", date);
        head.put("Authorization", Authorization);
        return get(url + key, head);
    }

    /**
     * 向OSS存储对象并返回对象键
     * <p>
     * 该方法用于将输入流中的内容上传到指定的OSS存储桶，并返回对象的键。
     * 支持自定义端点和标准端点，同时处理OSS的签名和授权信息。
     *
     * @param key              要存储的对象键
     * @param content          要上传的内容输入流
     * @param ossBucket        OSS存储桶名称
     * @param endpoint         OSS服务端点
     * @param accessKeyId      访问密钥ID
     * @param secretAccessKey  访问密钥
     * @param isCustomEndpoint 是否使用自定义端点
     * @param customEndpoint   自定义端点地址
     * @return 返回对象键
     * @throws IOException 当上传过程中发生IO异常时抛出
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
        String date = getGMTDate();
        String signResourcePath = "/" + ossBucket + key;
        String signature = (hmacSha1(buildPutSignData(date, signResourcePath), secretAccessKey));
        String authorization = "OSS " + accessKeyId + ":" + signature;

        String connectUrl = "https://" + ossBucket + "." + endpoint;
        if (isCustomEndpoint) {
            connectUrl = "http://" + customEndpoint;
        }

        HttpURLConnection connection = OssUtils.connect(connectUrl + key, "PUT");
        StringBuffer sbuffer;
        //设置请求头
        connection.setRequestProperty("Date", date);
        connection.setRequestProperty("Authorization", authorization);
        connection.connect();

        try (OutputStream out = connection.getOutputStream()) {
            IOUtils.copy(content, out);
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
     * 发送HTTP GET请求并返回响应内容字符串
     * <p>
     * 通过指定的URL和请求头发送GET请求，获取响应内容并转换为字符串返回
     *
     * @param url  请求的URL地址
     * @param head 请求头信息，包含键值对
     * @return 响应内容的字符串形式
     * @throws IOException 如果请求过程中发生IO异常
     */
    public static String get(String url, Map<String, String> head) throws IOException {
        return OssUtils.get(url, head);
    }

    /**
     * 使用 HMAC-SHA1 算法对数据进行加密并返回 Base64 编码的字符串
     * <p>
     * 该方法通过给定的数据和密钥，使用 HMAC-SHA1 算法生成消息认证码，并将结果进行 Base64 编码后返回。
     *
     * @param data 需要加密的数据字符串
     * @param key  加密所使用的密钥字符串
     * @return Base64 编码后的 HMAC-SHA1 认证码字符串
     * @throws RuntimeException 如果加密过程中发生异常
     */
    public static String hmacSha1(String data, String key) {
        try {
            Mac mac = Mac.getInstance("HmacSHA1");
            SecretKeySpec keySpec = new SecretKeySpec(key.getBytes(), ALGORITHM);
            mac.init(keySpec);
            byte[] rawHmac;
            rawHmac = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return new String(Base64.getEncoder().encode(rawHmac));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 构建用于签名的数据字符串
     * <p>
     * 将 HTTP 方法 "GET"、空行、日期和规范化资源拼接成一个用于签名的字符串
     *
     * @param date                  请求的日期
     * @param canonicalizedResource 规范化的资源路径
     * @return 用于签名的数据字符串
     * @since 0.0.1
     */
    public static String buildGetSignData(String date, String canonicalizedResource) {
        return "GET" + "\n" + "\n" + "\n"
               + date + "\n"
               + canonicalizedResource;
    }

    /**
     * 构建用于签名的 PUT 请求数据字符串
     * <p>
     * 将日期和规范化资源组合成用于生成签名的字符串格式
     *
     * @param date                  请求日期，格式为字符串
     * @param canonicalizedResource 规范化的资源路径
     * @return 用于签名的字符串数据
     * @since 0.0.1
     */
    public static String buildPutSignData(String date, String canonicalizedResource) {
        return "PUT" + "\n" + "\n" + "\n"
               + date + "\n"
               + canonicalizedResource;
    }

    /**
     * 获取当前的GMT时间字符串
     * <p>
     * 使用指定格式和时区（GMT）格式化当前时间，并返回字符串形式的时间。
     *
     * @return 当前GMT时间的字符串表示，格式为 "EEE, dd MMM yyyy HH:mm:ss GMT"
     * @since 0.0.1
     */
    public static String getGMTDate() {
        Calendar cd = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss 'GMT'", Locale.US);
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
        return sdf.format(cd.getTime());
    }
}
