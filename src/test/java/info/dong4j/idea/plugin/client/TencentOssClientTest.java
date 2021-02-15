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

package info.dong4j.idea.plugin.client;


import info.dong4j.idea.plugin.enums.CloudEnum;
import info.dong4j.idea.plugin.util.IOUtils;
import info.dong4j.idea.plugin.util.QcloudCosUtils;
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
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
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
 * <p>Company: no company</p>
 * <p>Description: 腾讯与 COS 简单上传文件 </p>
 *
 * @author dong4j
 * @version 0.0.1
 * @email "mailto:dong4j@gmail.com"
 * @date 2019.04.16 20:11
 * @since 1.1.0
 */
@Slf4j
public class TencentOssClientTest {
    /** secretId */
    private static final String secretId = System.getProperty("secretId");
    /** secretKey */
    private static final String secretKey = System.getProperty("secretKey");
    /** bucketName */
    // bucket名需包含appid
    private static final String bucketName = System.getProperty("bucketName");

    /**
     * Test
     *
     * @since 1.1.0
     */
    @Test
    public void test() {
    }

    /**
     * Test 2
     *
     * @since 1.1.0
     */
    @Test
    public void test2() {
        // 实例化被 @Client 标识的 client, 存入到 map 中
        Class<?> clz = null;
        try {
            clz = Class.forName(CloudEnum.TENCENT_CLOUD.feature);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        try {
            Constructor<?> constructor = Objects.requireNonNull(clz).getDeclaredConstructor();
            constructor.setAccessible(true);
            OssClient uploader = (OssClient) constructor.newInstance();

            OssClient.INSTANCES.put(CloudEnum.TENCENT_CLOUD, uploader);

            this.upload();
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
            log.trace("", e);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Upload *
     *
     * @throws FileNotFoundException file not found exception
     * @since 1.1.0
     */
    private void upload() throws Exception {
        OssClient uploader = OssClient.INSTANCES.get(CloudEnum.TENCENT_CLOUD);
        log.info("{}", uploader.getName());
        String url = uploader.upload(new FileInputStream(new File("/Users/dong4j/Downloads/xu.png")), "x2.png");
        log.info("url = {}", url);
    }

    /**
     * Test web api *
     *
     * @throws FileNotFoundException file not found exception
     * @since 1.1.0
     */
    @Test
    public void test_web_api() throws Exception {

        // key 必须使用 / 为前缀
        String putResult = QcloudCosUtils.putObject("/yguy.jpg",
                                                    new FileInputStream(new File("/Users/dong4j/Downloads/mik.png")),
                                                    bucketName,
                                                    "ap-chengdu",
                                                    secretId,
                                                    secretKey);
        System.out.println("putResult:" + putResult);

        // String getResult = QcloudCosUtils.getUrl(bucketName, "ap-chengdu", putResult);
        // System.out.println("getResult:" + getResult);
    }

    /**
     * <p>Company: 成都返空汇网络技术有限公司 </p>
     * <p>Description: </p>
     *
     * @author dong4j
     * @version 1.0.0
     * @email "mailto:dong4j@gmail.com"
     * @date 2021.02.14 22:44
     * @since 1.1.0
     */
    public static class CosWebApi {

        // private static final String bucket = bucketName;
        // private static final String SecretId = secretId;
        // private static final String SecretKey = secretKey;
        // private static final String host = ".cos.ap-chengdu.myqcloud.com";

        /** effectiveMinu */
        //资源授权有效期(分钟)
        private static final int effectiveMinu = 10;

        /** LINE_SEPARATOR */
        public static final String LINE_SEPARATOR = "\n";
        /** Q_SIGN_ALGORITHM_KEY */
        public static final String Q_SIGN_ALGORITHM_KEY = "q-sign-algorithm";
        /** Q_SIGN_ALGORITHM_VALUE */
        public static final String Q_SIGN_ALGORITHM_VALUE = "sha1";
        /** Q_AK */
        public static final String Q_AK = "q-ak";
        /** Q_SIGN_TIME */
        public static final String Q_SIGN_TIME = "q-sign-time";
        /** Q_KEY_TIME */
        public static final String Q_KEY_TIME = "q-key-time";
        /** Q_HEADER_LIST */
        public static final String Q_HEADER_LIST = "q-header-list";
        /** Q_URL_PARAM_LIST */
        public static final String Q_URL_PARAM_LIST = "q-url-param-list";
        /** Q_SIGNATURE */
        public static final String Q_SIGNATURE = "q-signature";
        /** GET */
        public static final String GET = "get";
        /** PUT */
        public static final String PUT = "put";


        /**
         * Gets gmt date *
         *
         * @return the gmt date
         * @since 1.1.0
         */
        public static String getGMTDate() {
            Calendar cd = Calendar.getInstance();
            SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss 'GMT'", Locale.US);
            sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
            return sdf.format(cd.getTime());
        }

        /**
         * Gets authorization *
         *
         * @param headers     headers
         * @param params      params
         * @param httpMethod  http method
         * @param UriPathname uri pathname
         * @param SecretKey   secret key
         * @param SecretId    secret id
         * @return the authorization
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

        /**
         * Get
         *
         * @param url  url
         * @param head head
         * @return the string
         * @throws IOException io exception
         * @since 1.1.0
         */
        //http get请求
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
         * Build time str
         *
         * @param expiredTime expired time
         * @return the string
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
         * Format map to str
         *
         * @param kVMap k v map
         * @return the string
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
         * Build sign headers
         *
         * @param originHeaders origin headers
         * @return the map
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
         * Build sign member str
         *
         * @param signHeaders sign headers
         * @return the string
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
         * Encode
         *
         * @param originUrl origin url
         * @return the string
         * @since 1.1.0
         */
        public static String encode(String originUrl) {
            try {
                return URLEncoder.encode(originUrl, "UTF-8").replace("+", "%20").replace("*", "%2A").replace("%7E", "~");
            } catch (UnsupportedEncodingException var2) {
                return null;
            }
        }

        /**
         * Gets url *
         *
         * @param backet     backet
         * @param regionName region name
         * @param key        key
         * @return the url
         * @since 1.1.0
         */
        public static String getUrl(String backet, String regionName, String key) {
            if (!key.startsWith("/")) {
                key += "/";
            }
            return "https://" + backet + ".cos." + regionName + ".myqcloud.com" + key;
        }

        /**
         * Gets obj *
         *
         * @param key        key
         * @param backet     backet
         * @param regionName region name
         * @param SecretKey  secret key
         * @param SecretId   secret id
         * @return the obj
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
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            return null;
        }


        /**
         * Sha encode
         *
         * @param inStr in str
         * @return the string
         * @throws Exception exception
         * @since 1.1.0
         */
        ////////////////////////////////////////////////////////////////////
        public static String shaEncode(String inStr) throws Exception {
            MessageDigest sha;
            try {
                sha = MessageDigest.getInstance("SHA");
            } catch (Exception e) {
                e.printStackTrace();
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
         * Gets second timestamp *
         *
         * @param date date
         * @return the second timestamp
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
         * Gen hmac
         *
         * @param key key
         * @param src src
         * @return the string
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
         * Gets upload information *
         *
         * @param path    path
         * @param content content
         * @throws Exception exception
         * @since 1.1.0
         */
        public static void getUploadInformation(String path, InputStream content) throws Exception {
            //创建连接
            URL url = new URL(path);
            HttpURLConnection connection;
            StringBuffer sbuffer;
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
                    sbuffer = new StringBuffer("");

                    while ((lines = reader.readLine()) != null) {

                        lines = new String(lines.getBytes(), StandardCharsets.UTF_8);
                        sbuffer.append(lines);
                    }
                    reader.close();
                }
                //断开连接
                connection.disconnect();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        /**
         * Put obj
         *
         * @param key        key
         * @param content    content
         * @param backet     backet
         * @param regionName region name
         * @param secretKey  secret key
         * @param secretId   secret id
         * @return the string
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
         * Main
         *
         * @param args args
         * @throws FileNotFoundException file not found exception
         * @since 1.1.0
         */
        public static void main(String[] args) throws FileNotFoundException {

            String putResult = putObj("/10A914D0CC18.jpg",
                                      new FileInputStream(new File("/Users/dong4j/Downloads/05B3AB1C-BBA9-4113-B212" +
                                                                   "-10A914D0CC18.jpg")),
                                      bucketName, "ap-chengdu", secretKey, secretId
                                     );
            System.out.println("putResult:" + putResult);

            String getResult = getObj(putResult, bucketName, "ap-chengdu", secretKey, secretId);
            System.out.println("getResult:" + getResult);
        }
    }
}
