package info.dong4j.idea.plugin.util;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.codec.digest.HmacUtils;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
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
 * <p>Company: 成都返空汇网络技术有限公司 </p>
 * <p>Description: </p>
 * https://cloud.tencent.com/document/product/436/7751
 *
 * @author dong4j
 * @version x.x.x
 * @email "mailto:dongshijie@fkhwl.com"
 * @date 2020.04.21 23:29
 */
@Slf4j
public class QcloudCosUtils {
    /** EFFECTIVE_MINU */
    //资源授权有效期(分钟)
    private static final long EFFECTIVE_MINU = 3600L * 1000 * 24 * 365 * 10;
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
     */
    @NotNull
    public static String getGMTDate() {
        Calendar cd = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss 'GMT'", Locale.US);
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
        return sdf.format(cd.getTime());
    }

    /**
     * Gets authorization *
     *
     * @param headers    headers
     * @param params     params
     * @param httpMethod http method
     * @param key        uri pathname
     * @param secretKey  secret key
     * @param secretId   secret id
     * @return the authorization
     */
    @NotNull
    public static String getAuthorization(Map<String, String> headers,
                                          Map<String, String> params,
                                          String httpMethod,
                                          String key,
                                          String secretKey,
                                          String secretId) {

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
                           key + LINE_SEPARATOR + formatParameters +
                           LINE_SEPARATOR + formatHeaders + LINE_SEPARATOR;

        //增加
        Date expiredTime = new Date(System.currentTimeMillis() + EFFECTIVE_MINU);
        String qKeyTimeStr, qSignTimeStr;
        qKeyTimeStr = qSignTimeStr = buildTimeStr(expiredTime);
        String hashFormatStr = DigestUtils.sha1Hex(formatStr);
        String stringToSign = Q_SIGN_ALGORITHM_VALUE +
                              LINE_SEPARATOR + qSignTimeStr + LINE_SEPARATOR +
                              hashFormatStr + LINE_SEPARATOR;

        String signKey = HmacUtils.hmacSha1Hex(secretKey, qKeyTimeStr);
        String signature = HmacUtils.hmacSha1Hex(signKey, stringToSign);

        return Q_SIGN_ALGORITHM_KEY + "=" +
               Q_SIGN_ALGORITHM_VALUE + "&" + Q_AK + "=" +
               secretId + "&" + Q_SIGN_TIME + "=" +
               qSignTimeStr + "&" + Q_KEY_TIME + "=" + qKeyTimeStr +
               "&" + Q_HEADER_LIST + "=" + qHeaderListStr + "&" +
               Q_URL_PARAM_LIST + "=" + qUrlParamListStr + "&" +
               Q_SIGNATURE + "=" + signature;
    }

    /**
     * Get string
     *
     * @param url  url
     * @param head head
     * @return the string
     * @throws IOException io exception
     */
    public static String get(String url, @NotNull Map<String, String> head) throws IOException {
        CloseableHttpClient client = HttpClients.createDefault();
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
     * Build time str string
     *
     * @param expiredTime expired time
     * @return the string
     */
    @NotNull
    public static String buildTimeStr(@NotNull Date expiredTime) {
        StringBuilder strBuilder = new StringBuilder();
        long startTime = System.currentTimeMillis() / 1000;
        long endTime = expiredTime.getTime() / 1000;
        strBuilder.append(startTime).append(";").append(endTime);
        return strBuilder.toString();
    }

    /**
     * Format map to str string
     *
     * @param kVMap k v map
     * @return the string
     */
    @NotNull
    public static String formatMapToStr(@NotNull Map<String, String> kVMap) {
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
     * Build sign headers map
     *
     * @param originHeaders origin headers
     * @return the map
     */
    @NotNull
    private static Map<String, String> buildSignHeaders(@NotNull Map<String, String> originHeaders) {
        Map<String, String> signHeaders = new HashMap<>();
        for (String key : originHeaders.keySet()) {

            if ("content-type" .equalsIgnoreCase(key) || "content-length" .equalsIgnoreCase(key)
                || "content-md5" .equalsIgnoreCase(key) || key.startsWith("x")
                || key.startsWith("X")) {
                String lowerKey = key.toLowerCase();
                String value = originHeaders.get(key);
                signHeaders.put(lowerKey, value);
            }
        }
        return signHeaders;
    }

    /**
     * Build sign member str string
     *
     * @param signHeaders sign headers
     * @return the string
     */
    @NotNull
    public static String buildSignMemberStr(@NotNull Map<String, String> signHeaders) {
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
     * Encode string
     *
     * @param originUrl origin url
     * @return the string
     */
    @Nullable
    public static String encode(String originUrl) {
        try {
            return URLEncoder.encode(originUrl, "UTF-8").replace("+", "%20").replace("*", "%2A").replace("%7E", "~");
        } catch (UnsupportedEncodingException var2) {
            return null;
        }
    }


    /**
     * Gets obj *
     *
     * @param key       key
     * @param backet    backet
     * @param host      host
     * @param secretKey secret key
     * @param secretId  secret id
     * @return the obj
     */
    @Nullable
    public static String getObject(String key,
                                   String backet,
                                   String host,
                                   String secretKey,
                                   String secretId) {
        String gmtDate = getGMTDate();
        Map<String, String> headers = new HashMap<>();
        headers.put("Host", backet + host);
        headers.put("Date", gmtDate);

        Map<String, String> params = new HashMap<>();
        String authorization = getAuthorization(headers, params, GET, key, secretKey, secretId);

        Map<String, String> httpHeader = new HashMap<>();
        httpHeader.put("Host", backet + host);
        httpHeader.put("Date", gmtDate);
        httpHeader.put("Authorization", authorization);

        try {
            return get(getUrl(backet, host, key), httpHeader);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    /**
     * Get url string
     *
     * @param backet     backet
     * @param regionName region name
     * @param key        key
     * @return the string
     */
    @NotNull
    @Contract(pure = true)
    public static String getUrl(String backet, String regionName, String key) {
        return "https://" + backet + ".cos." + regionName + ".myqcloud.com" + key;
    }

    /**
     * Sha encode string
     *
     * @param inStr in str
     * @return the string
     * @throws Exception exception
     */
    @NotNull
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
     */
    @NotNull
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
     * Gen hmac string
     *
     * @param key key
     * @param src src
     * @return the string
     */
    @NotNull
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
     * Put obj string
     *
     * @param content    content
     * @param key        key
     * @param secretKey  secret key
     * @param secretId   secret id
     * @param bucket     bucket
     * @param regionName region name
     * @return the string
     */
    public static String putObject(InputStream content, String key, String secretKey, String secretId, String bucket, String regionName) {
        try {
            Date dateStart = new Date();
            Date dateEnd = new Date();
            dateEnd.setTime(dateStart.getTime() + EFFECTIVE_MINU);

            String sha1 = "sha1";
            String qSignTime = getSecondTimestamp(dateStart) + ";" + getSecondTimestamp(dateEnd);
            String qKeyTime = getSecondTimestamp(dateStart) + ";" + getSecondTimestamp(dateEnd);


            String SignKey = genHMAC(secretKey, qKeyTime);
            String HttpString = PUT + "\n" + key + "\n\n\n";
            String StringToSign = sha1 + "\n" + qSignTime + "\n" + shaEncode(HttpString) + "\n";
            String Signature = genHMAC(SignKey, StringToSign);

            String url = getUrl(bucket, regionName, key) + "?q-sign-algorithm=" + sha1 + "&q-ak=" + secretId +
                         "&q-sign-time=" + qSignTime + "&q-key-time=" + qKeyTime + "&q-header-list=&q-url-param-list=&q-signature=" + Signature;

            getUploadInformation(url, content);
            return getUrl(bucket, regionName, key);
        } catch (Exception ex) {
            return "";
        }
    }

    /**
     * Gets upload information *
     *
     * @param path    path
     * @param content content
     * @throws IOException io exception
     * @throws Exception   exception
     */
    public static void getUploadInformation(String path, @NotNull InputStream content) throws IOException, Exception {
        //创建连接
        URL url = new URL(path);
        HttpURLConnection connection;
        //添加 请求内容
        connection = (HttpURLConnection) url.openConnection();
        //设置http连接属性
        connection.setDoOutput(true);
        connection.setDoInput(true);
        connection.setRequestMethod("PUT");
        connection.setRequestProperty("Content-Length", content.available() + "");

        connection.setReadTimeout(10000);
        connection.connect();

        try (OutputStream out = connection.getOutputStream()) {
            IOUtils.copy(content, out);
            //读取响应
            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                String s = IOUtils.toString(connection.getInputStream(), StandardCharsets.UTF_8);
                log.trace("{}", s);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            connection.disconnect();
        }
    }
}
