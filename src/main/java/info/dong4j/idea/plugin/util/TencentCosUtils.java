package info.dong4j.idea.plugin.util;

import info.dong4j.idea.plugin.util.digest.DigestUtils;
import info.dong4j.idea.plugin.util.digest.Hex;
import info.dong4j.idea.plugin.util.digest.HmacUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.TreeMap;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import lombok.extern.slf4j.Slf4j;

/**
 * 腾讯云对象存储（COS）工具类
 * <p>
 * 提供腾讯云 COS 相关的工具方法，包括签名生成、请求授权、文件上传和下载等操作，支持基于 HTTP 的 COS 服务调用。
 * 该类封装了腾讯云 COS 的签名生成逻辑，用于构建合法的请求头和参数，实现对 COS 服务的访问控制。
 *
 * @author dong4j
 * @version 0.0.1
 * @date 2020.04.21
 * @since 0.0.1
 */
@Slf4j
public class TencentCosUtils {
    /** 换行分隔符，用于表示换行符 */
    public static final String LINE_SEPARATOR = "\n";
    /** Q_SIGN_ALGORITHM_KEY 表示请求签名算法的参数键，用于标识使用的签名算法类型 */
    public static final String Q_SIGN_ALGORITHM_KEY = "q-sign-algorithm";
    /** Q_SIGN_ALGORITHM_VALUE 表示使用的签名算法类型，值为 "sha1" */
    public static final String Q_SIGN_ALGORITHM_VALUE = "sha1";
    /** Q_AK 是用于标识某个特定配置项的常量，通常用于权限或认证场景 */
    public static final String Q_AK = "q-ak";
    /** 请求签名时间参数名 */
    public static final String Q_SIGN_TIME = "q-sign-time";
    /** q-key-time 字段用于标识请求头中的时间戳参数 */
    public static final String Q_KEY_TIME = "q-key-time";
    /** 请求头列表标识符 */
    public static final String Q_HEADER_LIST = "q-header-list";
    /** q-url-param-list 参数名，用于传递查询参数列表 */
    public static final String Q_URL_PARAM_LIST = "q-url-param-list";
    /** 用于标识请求头中的签名字段 */
    public static final String Q_SIGNATURE = "q-signature";
    /** HTTP GET 方法 */
    public static final String GET = "get";
    /** PUT 请求方法标识 */
    public static final String PUT = "put";

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

    /**
     * 构建授权头信息
     * <p>
     * 根据传入的请求头、参数、HTTP方法、URI路径名、密钥和密钥ID，生成用于请求的授权头信息。
     * 该方法会处理请求头和参数的排序与格式化，并计算签名和时间戳，最终返回完整的授权字符串。
     *
     * @param headers     请求头信息
     * @param params      请求参数信息
     * @param httpMethod  HTTP请求方法
     * @param UriPathname URI路径名
     * @param SecretKey   密钥
     * @param SecretId    密钥ID
     * @return 完整的授权头字符串
     * @since 0.0.1
     */
    public static String getAuthorization(Map<String, String> headers,
                                          Map<String, String> params,
                                          String httpMethod,
                                          String UriPathname,
                                          String SecretKey,
                                          String SecretId) {

        Map<String, String> signHeaders = buildSignHeaders(headers);
        TreeMap<String, String> sortedSignHeaders = new TreeMap<>();
        TreeMap<String, String> sortedParams = new TreeMap<>();

        String qHeaderListStr = buildSignMemberStr(sortedSignHeaders);
        String qUrlParamListStr = buildSignMemberStr(sortedParams);

        sortedSignHeaders.putAll(signHeaders);
        sortedParams.putAll(params);
        String formatParameters = formatMapToStr(sortedParams);
        String formatHeaders = formatMapToStr(sortedSignHeaders);

        String formatStr = httpMethod + LINE_SEPARATOR +
                           UriPathname + LINE_SEPARATOR + formatParameters +
                           LINE_SEPARATOR + formatHeaders + LINE_SEPARATOR;

        //增加
        Date expiredTime = new Date(System.currentTimeMillis() + 3600L * 1000 * 24 * 365 * 10);
        String qKeyTimeStr, qSignTimeStr;
        qKeyTimeStr = qSignTimeStr = buildTimeStr(expiredTime);
        String hashFormatStr = DigestUtils.sha1Hex(formatStr);
        String stringToSign = Q_SIGN_ALGORITHM_VALUE +
                              LINE_SEPARATOR + qSignTimeStr + LINE_SEPARATOR +
                              hashFormatStr + LINE_SEPARATOR;

        String signKey = HmacUtils.hmacSha1Hex(SecretKey, qKeyTimeStr);
        String signature = HmacUtils.hmacSha1Hex(signKey, stringToSign);

        return Q_SIGN_ALGORITHM_KEY + "=" +
               Q_SIGN_ALGORITHM_VALUE + "&" + Q_AK + "=" +
               SecretId + "&" + Q_SIGN_TIME + "=" +
               qSignTimeStr + "&" + Q_KEY_TIME + "=" + qKeyTimeStr +
               "&" + Q_HEADER_LIST + "=" + qHeaderListStr + "&" +
               Q_URL_PARAM_LIST + "=" + qUrlParamListStr + "&" +
               Q_SIGNATURE + "=" + signature;
    }

    /**
     * 发送HTTP GET请求并返回响应内容字符串
     * <p>
     * 通过指定的URL和请求头发送GET请求，获取响应内容并返回字符串形式
     *
     * @param url  请求的URL地址
     * @param head 请求头信息，包含键值对
     * @return 响应内容的字符串形式
     * @throws IOException 如果请求过程中发生IO异常
     * @since 0.0.1
     */
    public static String get(String url, Map<String, String> head) throws IOException {
        return OssUtils.get(url, head);
    }

    /**
     * 构建时间字符串
     * <p>
     * 将当前时间戳和指定的过期时间戳拼接成一个字符串，格式为"startTime;endTime"
     *
     * @param expiredTime 过期时间对象
     * @return 拼接后的时间字符串
     * @since 0.0.1
     */
    public static String buildTimeStr(Date expiredTime) {
        StringBuilder strBuilder = new StringBuilder();
        long startTime = System.currentTimeMillis() / 1000;
        long endTime = expiredTime.getTime() / 1000;
        strBuilder.append(startTime).append(";").append(endTime);
        return strBuilder.toString();
    }

    /**
     * 将 Map 对象格式化为 URL 查询参数字符串
     * <p>
     * 遍历 Map 中的键值对，将每个键值对编码后拼接成 URL 查询字符串格式，如 key1=value1&key2=value2
     *
     * @param kVMap 要格式化的键值对 Map
     * @return 格式化后的 URL 查询字符串
     * @since 0.0.1
     */
    public static String formatMapToStr(Map<String, String> kVMap) {
        StringBuilder strBuilder = new StringBuilder();
        boolean seeOne = false;
        for (String key : kVMap.keySet()) {
            String lowerKey = key.toLowerCase();
            String encodeKey = encode(lowerKey);
            String encodedValue = "";
            if (kVMap.get(key) != null) {
                encodedValue = encode(kVMap.get(key));
            }
            if (!seeOne) {
                seeOne = true;
            } else {
                strBuilder.append("&");
            }
            strBuilder.append(encodeKey).append("=").append(encodedValue);
        }
        return strBuilder.toString();
    }

    /**
     * 构建签名所需的请求头映射
     * <p>
     * 根据原始请求头，筛选并构建用于签名的头信息。忽略某些特定的通用头字段，如
     * "content-type"、"content-length"、"content-md5"，以及以 "x" 或 "X" 开头的扩展头字段。
     * 所有保留的头字段都会转换为小写形式，并放入新的映射中。
     *
     * @param originHeaders 原始请求头映射
     * @return 用于签名的请求头映射
     * @since 0.0.1
     */
    private static Map<String, String> buildSignHeaders(Map<String, String> originHeaders) {
        Map<String, String> signHeaders = new HashMap<>();
        for (String key : originHeaders.keySet()) {

            if (key.equalsIgnoreCase("content-type") || key.equalsIgnoreCase("content-length")
                || key.equalsIgnoreCase("content-md5") || key.startsWith("x")
                || key.startsWith("X")) {
                String lowerKey = key.toLowerCase();
                String value = originHeaders.get(key);
                signHeaders.put(lowerKey, value);
            }
        }
        return signHeaders;
    }

    /**
     * 构建签名成员字符串
     * <p>
     * 将传入的签名头信息按照键名进行拼接，生成一个字符串。键名会转换为小写，并以分号分隔。
     *
     * @param signHeaders 签名头信息，键值对集合
     * @return 拼接后的字符串
     * @since 0.0.1
     */
    public static String buildSignMemberStr(Map<String, String> signHeaders) {
        StringBuilder strBuilder = new StringBuilder();
        boolean seenOne = false;
        for (String key : signHeaders.keySet()) {
            if (!seenOne) {
                seenOne = true;
            } else {
                strBuilder.append(";");
            }
            strBuilder.append(key.toLowerCase());
        }
        return strBuilder.toString();
    }

    /**
     * 对给定的原始URL进行编码处理
     * <p>
     * 将字符串使用UTF-8编码格式进行URL编码，并对特殊字符进行替换处理：
     * '+' 替换为 '%20'，'*' 替换为 '%2A'，'~' 替换为 '%7E'
     *
     * @param originUrl 原始URL字符串
     * @return 编码后的字符串
     * @since 0.0.1
     */
    public static String encode(String originUrl) {
        return URLEncoder.encode(originUrl, StandardCharsets.UTF_8).replace("+", "%20").replace("*", "%2A").replace("%7E", "~");
    }

    /**
     * 根据存储桶名称、区域名称和对象键生成对应的 COS 服务 URL
     * <p>
     * 该方法用于构造 COS 服务的访问地址，确保对象键以斜杠开头，然后拼接成完整的 URL。
     *
     * @param backet     存储桶名称
     * @param regionName 区域名称
     * @param key        对象键，若不以斜杠开头则自动添加
     * @return 构造完成的 COS 服务 URL
     * @since 0.0.1
     */
    public static String getUrl(String backet, String regionName, String key) {
        if (!key.startsWith("/")) {
            key = "/" + key;
        }
        return "https://" + backet + ".cos." + regionName + ".myqcloud.com" + key;
    }

    /**
     * 根据给定参数获取对象数据
     * <p>
     * 该方法通过构造请求头和授权信息，向指定的 COS 地址发起 HTTP 请求，获取对象数据。
     *
     * @param key        要获取的对象键（Key）
     * @param backet     存储桶名称（Bucket）
     * @param regionName 区域名称（Region）
     * @param SecretKey  密钥（SecretKey）
     * @param SecretId   密钥 ID（SecretId）
     * @return 获取到的对象数据，若请求失败则返回 null
     * @since 0.0.1
     */
    public static String getObj(String key, String backet, String regionName,
                                String SecretKey,
                                String SecretId) {
        String gmtDate = getGMTDate();
        Map<String, String> headers = new HashMap<>();
        headers.put("Host", backet + ".cos." + regionName + ".myqcloud.com");
        headers.put("Date", gmtDate);

        Map<String, String> params = new HashMap<>();
        String authorization = getAuthorization(headers, params, GET, key, SecretKey, SecretId);

        Map<String, String> httpHeader = new HashMap<>();
        httpHeader.put("Host", backet + ".cos." + regionName + ".myqcloud.com");
        httpHeader.put("Date", gmtDate);
        httpHeader.put("Authorization", authorization);

        try {
            return get(getUrl(backet, regionName, key), httpHeader);
        } catch (IOException ex) {
            return null;
        }
    }

    /**
     * 对字符串进行SHA编码处理
     * <p>
     * 将输入的字符串转换为字节数组，使用SHA算法进行哈希计算，并将结果转换为十六进制字符串返回。
     *
     * @param inStr 需要编码的输入字符串
     * @return 编码后的十六进制字符串
     * @since 0.0.1
     */
    public static String shaEncode(String inStr) {
        MessageDigest sha;
        try {
            sha = MessageDigest.getInstance("SHA");
        } catch (Exception e) {
            return "";
        }

        byte[] byteArray = inStr.getBytes(StandardCharsets.UTF_8);
        byte[] md5Bytes = sha.digest(byteArray);
        StringBuilder hexValue = new StringBuilder();
        for (byte md5Byte : md5Bytes) {
            int val = ((int) md5Byte) & 0xff;
            if (val < 16) {
                hexValue.append("0");
            }
            hexValue.append(Integer.toHexString(val));
        }
        return hexValue.toString();
    }

    /**
     * 获取时间戳的前部分（去除最后三位）
     * <p>
     * 将给定的日期对象转换为时间戳字符串，并截取去除最后三位数字的部分。
     *
     * @param date 日期对象
     * @return 截取后的时间戳字符串，若输入为null则返回空字符串
     * @since 0.0.1
     */
    public static String getSecondTimestamp(Date date) {
        if (null == date) {
            return "";
        }
        String timestamp = String.valueOf(date.getTime());
        int length = timestamp.length();
        if (length > 3) {
            return timestamp.substring(0, length - 3);
        } else {
            return "";
        }
    }

    /**
     * 根据指定的密钥和源字符串生成HMAC哈希值
     * <p>
     * 使用HmacSHA1算法对输入的字符串进行加密处理，并返回十六进制格式的哈希字符串
     *
     * @param key 密钥，用于加密计算
     * @param src 源字符串，需要进行加密处理的数据
     * @return 生成的HMAC哈希字符串
     * @throws RuntimeException 如果加密过程中发生异常
     */
    public static String genHMAC(String key, String src) {
        try {
            SecretKeySpec signingKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA1");
            Mac mac = Mac.getInstance("HmacSHA1");
            mac.init(signingKey);
            byte[] rawHmac = mac.doFinal(src.getBytes(StandardCharsets.UTF_8));
            return Hex.encodeHexString(rawHmac);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 获取上传信息
     * <p>
     * 根据提供的路径、输入流、密钥、存储桶和区域名称，向指定路径发送PUT请求，并读取响应内容。
     * 若响应状态码为200，则返回构建的上传信息URL。
     *
     * @param path       上传请求的路径
     * @param content    要上传的输入流内容
     * @param key        用于构建URL的密钥参数
     * @param backet     存储桶名称
     * @param regionName 区域名称，用于构建URL
     * @return 上传信息的URL字符串
     * @throws Exception 如果请求过程中发生异常
     * @since 0.0.1
     */
    public static String getUploadInformation(String path,
                                              InputStream content,
                                              String key,
                                              String backet,
                                              String regionName) throws Exception {
        //创建连接
        HttpURLConnection connection = OssUtils.connect(path, "PUT");
        connection.setRequestProperty("Content-Length", content.available() + "");
        connection.connect();

        StringBuilder sbuffer;

        try (OutputStream out = connection.getOutputStream()) {
            IOUtils.copy(content, out);
            //读取响应
            if (connection.getResponseCode() == 200) {
                // 从服务器获得一个输入流
                try (InputStreamReader inputStream = new InputStreamReader(connection.getInputStream());
                     BufferedReader reader = new BufferedReader(inputStream)) {
                    String lines;
                    sbuffer = new StringBuilder();

                    while ((lines = reader.readLine()) != null) {
                        lines = new String(lines.getBytes(), StandardCharsets.UTF_8);
                        sbuffer.append(lines);
                    }
                    return getUrl(backet, regionName, key);
                }
            }
        } finally {
            //断开连接
            connection.disconnect();
        }
        return "";
    }

    /**
     * 将对象以字符串形式上传到指定的存储桶
     * <p>
     * 该方法用于生成带签名的URL，并通过该URL上传对象内容。方法内部会构建签名信息，包括时间戳、签名算法和密钥等，确保请求的安全性。
     *
     * @param key        存储对象的键（Key）
     * @param content    要上传的对象内容流
     * @param backet     存储桶名称
     * @param regionName 区域名称
     * @param secretId   秘密ID（Access Key ID）
     * @param secretKey  秘密密钥（Access Key Secret）
     * @return 上传信息的字符串表示
     * @throws Exception 上传过程中发生异常时抛出
     * @since 0.0.1
     */
    public static String putObject(String key,
                                   InputStream content,
                                   String backet,
                                   String regionName,
                                   String secretId,
                                   String secretKey) throws Exception {
        Date dateS = new Date();
        Date dateE = new Date();
        dateE.setTime(dateS.getTime() + 3600L * 1000 * 24 * 365 * 10);

        String qSignAlgorithm = "sha1";
        String qSignTime = getSecondTimestamp(dateS) + ";" + getSecondTimestamp(dateE);
        String qKeyTime = getSecondTimestamp(dateS) + ";" + getSecondTimestamp(dateE);


        String signKey = genHMAC(secretKey, qKeyTime);
        String httpString = PUT + "\n" + key + "\n\n\n";
        String stringToSign = qSignAlgorithm + "\n" + qSignTime + "\n" + shaEncode(httpString) + "\n";
        String signature = genHMAC(signKey, stringToSign);

        // formatter:off
        String url = MessageFormat.format("{0}?q-sign-algorithm={1}&q-ak={2}&q-sign-time={3}&q-key-time={4}&q-header-list=&q-url-param-list=&q-signature={5}",
                                          getUrl(backet, regionName, key), qSignAlgorithm, secretId, qSignTime, qKeyTime, signature);

        return getUploadInformation(url, content, key, backet, regionName);
    }
}
