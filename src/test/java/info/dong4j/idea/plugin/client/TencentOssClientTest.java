package info.dong4j.idea.plugin.client;


import info.dong4j.idea.plugin.enums.CloudEnum;
import info.dong4j.idea.plugin.util.IOUtils;
import info.dong4j.idea.plugin.util.TencentCosUtils;
import info.dong4j.idea.plugin.util.digest.DigestUtils;
import info.dong4j.idea.plugin.util.digest.Hex;
import info.dong4j.idea.plugin.util.digest.HmacUtils;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.TimeZone;
import java.util.TreeMap;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import lombok.extern.slf4j.Slf4j;

/**
 * 腾讯云对象存储服务（COS）客户端测试类
 * <p>
 * 该类主要用于测试腾讯云COS服务的简单文件上传和对象获取功能，包含上传文件、获取对象、构建授权签名等核心逻辑。
 * 支持通过构造函数动态创建OssClient实例，并提供基于HTTP的上传和下载接口，适用于开发和测试环境。
 *
 * @author dong4j
 * @version 1.0.0
 * @date 2021.02.14
 * @since 1.1.0
 */
@Slf4j
public class TencentOssClientTest {
    /** secretId 从系统属性中获取的密钥标识符 */
    private static final String secretId = System.getProperty("secretId");
    /** secretKey 用于加密和解密数据的密钥，从系统属性中获取 */
    private static final String secretKey = System.getProperty("secretKey");
    // bucket名需包含appid
    /** bucket 名，需包含 appid 信息 */
    private static final String bucketName = System.getProperty("bucketName");

    /**
     * 测试方法的通用描述
     * <p>
     * 测试目标：验证方法在不同场景下的行为
     * 测试场景：未指定具体测试场景，需根据实际实现补充
     * 预期结果：方法应按照预期逻辑执行
     */
    @Test
    public void test() {
    }

    /**
     * 测试 OssClient 的实例化与注入功能
     * <p>
     * 测试目标：验证通过反射实例化被 @Client 标识的 OssClient 类，并将其存入 INSTANCES 集合中
     * 测试场景：当 CloudEnum.TENCENT_CLOUD 对应的类存在且可被实例化时
     * 预期结果：成功创建 OssClient 实例并注入到 INSTANCES 中，后续调用 upload 方法应正常执行
     * <p>
     * 注意：该测试依赖于 CloudEnum.TENCENT_CLOUD 的 feature 字段指向一个有效的类名，且该类需有无参构造函数
     */
    @Test
    public void test2() {
        // 实例化被 @Client 标识的 client, 存入到 map 中
        Class<?> clz = null;
        try {
            clz = Class.forName(CloudEnum.TENCENT_CLOUD.feature);
        } catch (ClassNotFoundException ignored) {
        }
        try {
            Constructor<?> constructor = Objects.requireNonNull(clz).getDeclaredConstructor();
            constructor.setAccessible(true);
            OssClient uploader = (OssClient) constructor.newInstance();

            OssClient.INSTANCES.put(CloudEnum.TENCENT_CLOUD, uploader);

            this.upload();
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
            log.trace("", e);
        } catch (Exception ignored) {
        }
    }

    /**
     * 上传文件到对象存储服务
     * <p>
     * 通过腾讯云对象存储客户端上传指定图片文件，并记录上传后的URL
     *
     * @throws Exception 上传过程中发生异常时抛出
     */
    private void upload() throws Exception {
        OssClient uploader = OssClient.INSTANCES.get(CloudEnum.TENCENT_CLOUD);
        log.trace("{}", uploader.getName());
        String url = uploader.upload(new FileInputStream("/Users/dong4j/Downloads/mik.webp"), "x2.png");
        log.trace("url = {}", url);
    }

    /**
     * 测试 Web API 的上传功能
     * <p>
     * 测试场景：验证使用 TencentCosUtils 工具类上传文件到腾讯云对象存储（COS）的接口
     * 预期结果：上传操作应成功执行，返回正确的文件路径或状态信息
     * <p>
     * 注意：测试中使用的文件路径为本地路径，需确保文件存在且路径正确
     * 上传文件的 key 必须以 / 为前缀，否则可能上传失败
     * <p>
     * 该测试未实际验证文件是否成功上传至 COS，仅验证接口调用是否正常
     */
    @Test
    public void test_web_api() throws Exception {

        // key 必须使用 / 为前缀
        String putResult = TencentCosUtils.putObject("/yguy.jpg",
                                                     new FileInputStream("/Users/dong4j/Downloads/mik.webp"),
                                                     bucketName,
                                                     "ap-chengdu",
                                                     secretId,
                                                     secretKey);
        System.out.println("putResult:" + putResult);

        // String getResult = QcloudCosUtils.getUrl(bucketName, "ap-chengdu", putResult);
        // System.out.println("getResult:" + getResult);
    }

    /**
     * CosWebApi 类
     * <p>
     * 提供腾讯云对象存储服务（COS）的 Web API 接口封装，包括获取授权信息、构建请求 URL、上传和下载对象等功能。
     * 该类主要用于简化与 COS 服务的交互，支持 GET 和 PUT 请求方法，并实现了签名和时间戳的生成逻辑。
     * </p>
     *
     * @author dong4j
     * @version 1.0.0
     * @date 2025.10.24
     * @since 1.1.0
     */
    public static class CosWebApi {

        // private static final String bucket = bucketName;
        // private static final String SecretId = secretId;
        // private static final String SecretKey = secretKey;
        // private static final String host = ".cos.ap-chengdu.myqcloud.com";
        //资源授权有效期(分钟)
        /** 资源授权的有效时间，单位为分钟 */
        private static final int effectiveMinu = 10;
        /** 换行分隔符，用于表示换行符 */
        public static final String LINE_SEPARATOR = "\n";
        /** Q_SIGN_ALGORITHM_KEY 表示请求签名算法的参数键，值为 "q-sign-algorithm" */
        public static final String Q_SIGN_ALGORITHM_KEY = "q-sign-algorithm";
        /** Q_SIGN_ALGORITHM_VALUE 表示使用的签名算法类型，值为 "sha1" */
        public static final String Q_SIGN_ALGORITHM_VALUE = "sha1";
        /** Q_AK 是用于标识某个特定配置项的常量，通常用于权限或认证相关场景 */
        public static final String Q_AK = "q-ak";
        /** Q_SIGN_TIME */
        public static final String Q_SIGN_TIME = "q-sign-time";
        /** q-key-time 字段用于标识请求头中的时间戳参数 */
        public static final String Q_KEY_TIME = "q-key-time";
        /** 请求头列表参数名 */
        public static final String Q_HEADER_LIST = "q-header-list";
        /** q-url-param-list 参数名，用于传递查询参数列表 */
        public static final String Q_URL_PARAM_LIST = "q-url-param-list";
        /** Q_SIGNATURE 用于标识请求中的签名参数名 */
        public static final String Q_SIGNATURE = "q-signature";
        /** HTTP GET 方法 */
        public static final String GET = "get";
        /** HTTP PUT 方法 */
        public static final String PUT = "put";

        /**
         * 获取当前的GMT时间字符串
         * <p>
         * 返回格式为 "EEE, dd MMM yyyy HH:mm:ss GMT" 的GMT时间字符串
         *
         * @return 当前GMT时间字符串
         * @since 1.1.0
         */
        public static String getGMTDate() {
            Calendar cd = Calendar.getInstance();
            SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss 'GMT'", Locale.US);
            sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
            return sdf.format(cd.getTime());
        }

        /**
         * 生成授权信息
         * <p>
         * 根据请求头、参数、HTTP方法、URI路径名、密钥和密钥ID生成用于鉴权的授权字符串。
         * 该授权字符串包含签名算法、时间戳、签名等信息，用于请求的身份验证。
         *
         * @param headers     请求头信息，包含需要参与签名的头字段
         * @param params      请求参数信息，包含需要参与签名的查询参数
         * @param httpMethod  HTTP请求方法，如GET、POST等
         * @param UriPathname 请求的URI路径名
         * @param SecretKey   密钥，用于生成签名
         * @param SecretId    密钥ID，用于标识密钥
         * @return 生成的授权字符串
         * @since 1.1.0
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
            Date expiredTime = new Date(System.currentTimeMillis() + effectiveMinu * 60 * 1000);
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
        //http get请求

        /**
         * 发送 HTTP GET 请求并获取响应内容
         * <p>
         * 通过指定的 URL 和请求头获取服务器返回的字符串数据
         *
         * @param url  请求的目标 URL
         * @param head 请求头信息，包含键值对
         * @return 服务器返回的字符串内容
         * @throws IOException 如果网络请求过程中发生异常
         * @since 1.1.0
         */
        public static String get(String url, Map<String, String> head) throws IOException {
            HttpClient client = HttpClients.createDefault();
            HttpGet httpGet = new HttpGet(url);
            for (String key : head.keySet()) {
                httpGet.setHeader(key, head.get(key));
            }
            HttpResponse response = client.execute(httpGet);
            response.getEntity().getContent();
            HttpEntity entity = response.getEntity();
            return EntityUtils.toString(entity, StandardCharsets.UTF_8);
        }

        /**
         * 构建时间字符串
         * <p>
         * 将当前时间戳和指定的过期时间戳拼接成一个字符串，格式为"startTime;endTime"
         *
         * @param expiredTime 过期时间对象
         * @return 拼接后的时间字符串
         * @since 1.1.0
         */
        public static String buildTimeStr(Date expiredTime) {
            StringBuilder strBuilder = new StringBuilder();
            long startTime = System.currentTimeMillis() / 1000;
            long endTime = expiredTime.getTime() / 1000;
            strBuilder.append(startTime).append(";").append(endTime);
            return strBuilder.toString();
        }

        /**
         * 将键值对映射转换为字符串格式
         * <p>
         * 遍历传入的键值对映射，将每个键值对按照 key=value 的格式拼接成一个字符串，键名转换为小写并进行编码，值也进行编码处理。
         *
         * @param kVMap 要转换的键值对映射
         * @return 拼接后的字符串
         * @since 1.1.0
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
         * 构建用于签名的请求头信息
         * <p>
         * 根据原始请求头信息，筛选并构建用于签名的头信息。仅包含特定类型的头字段，如内容类型、内容长度、内容MD5以及以"x"开头的自定义头。
         *
         * @param originHeaders 原始请求头信息
         * @return 包含签名所需头信息的Map
         * @since 1.1.0
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
         * 将传入的签名头信息中的键转换为小写，并用分号分隔拼接成一个字符串。
         *
         * @param signHeaders 签名头信息，键值对集合
         * @return 拼接后的签名成员字符串
         * @since 1.1.0
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
         * 对原始URL进行编码处理
         * <p>
         * 将原始URL使用UTF-8编码格式进行URL编码，并对特殊字符进行替换处理
         *
         * @param originUrl 原始URL字符串
         * @return 编码后的URL字符串
         * @since 1.1.0
         */
        public static String encode(String originUrl) {
            return URLEncoder.encode(originUrl, StandardCharsets.UTF_8).replace("+", "%20").replace("*", "%2A").replace("%7E", "~");
        }

        /**
         * 构建 COS 服务的访问 URL
         * <p>
         * 根据指定的存储桶名称、区域名称和对象键生成对应的 COS 访问 URL。
         *
         * @param backet     存储桶名称
         * @param regionName 区域名称
         * @param key        对象键，若不以斜杠开头则自动补上
         * @return 构建完成的 COS 访问 URL
         * @since 1.1.0
         */
        public static String getUrl(String backet, String regionName, String key) {
            if (!key.startsWith("/")) {
                key += "/";
            }
            return "https://" + backet + ".cos." + regionName + ".myqcloud.com" + key;
        }

        /**
         * 根据指定参数获取对象内容
         * <p>
         * 构建请求头和参数，调用获取对象的 HTTP 请求方法，并返回响应结果
         *
         * @param key        对象键名
         * @param backet     存储桶名称
         * @param regionName 区域名称
         * @param SecretKey  密钥
         * @param SecretId   会话密钥
         * @return 获取到的对象内容，若请求失败则返回 null
         * @since 1.1.0
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
            } catch (IOException ignored) {
            }
            return null;
        }
        ////////////////////////////////////////////////////////////////////
        /**
         * 对字符串进行SHA编码处理
         * <p>
         * 将输入的字符串转换为字节数组，使用SHA算法进行哈希处理，并将结果转换为十六进制字符串返回。
         *
         * @param inStr 需要编码的输入字符串
         * @return SHA编码后的十六进制字符串
         * @throws Exception 如果SHA算法初始化失败或发生其他异常
         * @since 1.1.0
         */
        public static String shaEncode(String inStr) throws Exception {
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
         * @since 1.1.0
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
         * 使用HMAC算法生成消息摘要字符串
         * <p>
         * 该方法基于HmacSHA1算法，使用指定的密钥对源字符串进行加密，返回十六进制格式的摘要结果。
         *
         * @param key 密钥，用于生成HMAC的加密密钥
         * @param src 源字符串，需要加密的数据内容
         * @return 返回使用HMAC算法生成的十六进制字符串摘要
         * @throws RuntimeException 如果加密过程中发生异常
         * @since 1.1.0
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
         * 向指定路径发送PUT请求，上传输入流内容，并读取服务器返回的响应信息。
         *
         * @param path    上传的目标路径
         * @param content 要上传的输入流内容
         * @throws Exception 发生异常时抛出
         * @since 1.1.0
         */
        public static void getUploadInformation(String path, InputStream content) throws Exception {
            //创建连接
            URL url = new URL(path);
            HttpURLConnection connection;
            StringBuilder sbuffer;
            try {
                //添加 请求内容
                connection = (HttpURLConnection) url.openConnection();
                //设置http连接属性
                connection.setDoOutput(true);
                connection.setDoInput(true);
                connection.setRequestMethod("PUT");
                connection.setRequestProperty("Content-Length", content.available() + "");

                connection.setReadTimeout(5000);//设置读取超时时间
                connection.setConnectTimeout(3000);//设置连接超时时间
                connection.connect();
                OutputStream out = connection.getOutputStream();
                IOUtils.copy(content, out);

                out.flush();
                out.close();
                //读取响应
                if (connection.getResponseCode() == 200) {
                    // 从服务器获得一个输入流
                    InputStreamReader inputStream = new InputStreamReader(connection.getInputStream());
                    BufferedReader reader = new BufferedReader(inputStream);

                    String lines;
                    sbuffer = new StringBuilder();

                    while ((lines = reader.readLine()) != null) {

                        lines = new String(lines.getBytes(), StandardCharsets.UTF_8);
                        sbuffer.append(lines);
                    }
                    reader.close();
                }
                //断开连接
                connection.disconnect();
            } catch (IOException ignored) {
            }
        }

        /**
         * 上传对象到指定存储桶并生成带签名的URL
         * <p>
         * 该方法用于构建带签名的URL，用于上传对象到指定的存储桶。生成的URL包含签名信息，确保请求的安全性。
         * 上传完成后返回对象的键（key），若上传失败则返回null。
         *
         * @param key        对象的键（key）
         * @param content    要上传的内容流
         * @param backet     存储桶名称
         * @param regionName 区域名称
         * @param secretKey  秘钥
         * @param secretId   秘钥ID
         * @return 对象的键（key），若上传失败则返回null
         * @since 1.1.0
         */
        public static String putObj(String key,
                                    InputStream content,
                                    String backet,
                                    String regionName,
                                    String secretKey,
                                    String secretId) {
            try {
                Date date_s = new Date();
                Date date_e = new Date();
                date_e.setTime(date_s.getTime() + 3600L * 1000 * 24 * 365 * 10);

                String q_sign_algorithm = "sha1";
                String q_sign_time = getSecondTimestamp(date_s) + ";" + getSecondTimestamp(date_e);
                String q_key_time = getSecondTimestamp(date_s) + ";" + getSecondTimestamp(date_e);


                String SignKey = genHMAC(secretKey, q_key_time);
                String HttpString = PUT + "\n" + key + "\n\n\n";
                String StringToSign = q_sign_algorithm + "\n" + q_sign_time + "\n" + shaEncode(HttpString) + "\n";
                String Signature = genHMAC(SignKey, StringToSign);

                String url = getUrl(backet, regionName, key) + "?q-sign-algorithm=" + q_sign_algorithm + "&q-ak=" + secretId +
                             "&q-sign-time=" + q_sign_time + "&q-key-time=" + q_key_time + "&q-header-list=&q-url-param-list=&q-signature" +
                             "=" + Signature;

                getUploadInformation(url, content);
                return key;
            } catch (Exception ex) {
                System.out.println("上传失败:" + ex.getMessage());
                return null;
            }
        }

        /**
         * 主方法，用于测试对象存储功能
         * <p>
         * 该方法用于演示上传和下载对象的操作，通过调用 putObj 和 getObj 方法完成
         *
         * @param args 命令行参数，目前未使用
         * @throws FileNotFoundException 如果文件未找到时抛出
         * @since 1.1.0
         */
        public static void main(String[] args) throws FileNotFoundException {

            String putResult = putObj("/10A914D0CC18.jpg",
                                      new FileInputStream("/Users/dong4j/Downloads/05B3AB1C-BBA9-4113-B212" +
                                                          "-10A914D0CC18.jpg"),
                                      bucketName, "ap-chengdu", secretKey, secretId
                                     );
            System.out.println("putResult:" + putResult);

            String getResult = getObj(putResult, bucketName, "ap-chengdu", secretKey, secretId);
            System.out.println("getResult:" + getResult);
        }
    }
}
