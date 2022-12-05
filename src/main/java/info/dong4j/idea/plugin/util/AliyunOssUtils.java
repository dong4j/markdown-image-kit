package info.dong4j.idea.plugin.util;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
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
 * <p>Description: todo-dong4j : (2020年04月22日 1:41 上午) [未完成]</p>
 * https://helpcdn.aliyun.com/document_detail/31947.html
 *
 * @author dong4j
 * @version 0.0.1
 * @email "mailto:dong4j@gmail.com"
 * @date 2020.04.21 23:29
 * @since 0.0.1
 */
public class AliyunOssUtils {
    /** ALGORITHM */
    private final static String ALGORITHM = "HmacSHA1";

    /**
     * Gets oss obj *
     *
     * @param key             key
     * @param ossBucket       oss bucket
     * @param endpoint        endpoint
     * @param accessKeyId     access key id
     * @param secretAccessKey secret access key
     * @return the oss obj
     * @throws IOException io exception
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
                                   String customEndpoint) throws Exception {
        String date = getGMTDate();
        String signResourcePath = "/" + ossBucket + key;
        String signature = (hmacSha1(buildPutSignData(date, signResourcePath), secretAccessKey));
        String authorization = "OSS " + accessKeyId + ":" + signature;

        String connectUrl = "https://" + ossBucket + "." + endpoint;
        if (isCustomEndpoint) {
            connectUrl = "http://" + customEndpoint;
        }

        URL putUrl = new URL(connectUrl + key);
        HttpURLConnection connection;
        StringBuffer sbuffer;

        //添加 请求内容
        connection = (HttpURLConnection) putUrl.openConnection();
        //设置http连接属性
        connection.setDoOutput(true);
        connection.setRequestMethod("PUT");
        //设置请求头
        connection.setRequestProperty("Date", date);
        connection.setRequestProperty("Authorization", authorization);

        connection.setReadTimeout(5000);
        connection.setConnectTimeout(3000);
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
     * Get string
     *
     * @param url  url
     * @param head head
     * @return the string
     * @throws IOException io exception
     * @since 0.0.1
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
        return EntityUtils.toString(entity, "utf-8");
    }

    /**
     * Hmac sha 1 string
     *
     * @param data data
     * @param key  key
     * @return the string
     * @since 0.0.1
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
     * Build get sign data string
     *
     * @param date                  date
     * @param canonicalizedResource canonicalized resource
     * @return the string
     * @since 0.0.1
     */
    public static String buildGetSignData(String date, String canonicalizedResource) {
        return "GET" + "\n" + "\n" + "\n"
               + date + "\n"
               + canonicalizedResource;
    }

    /**
     * Build put sign data string
     *
     * @param date                  date
     * @param canonicalizedResource canonicalized resource
     * @return the string
     * @since 0.0.1
     */
    public static String buildPutSignData(String date, String canonicalizedResource) {
        return "PUT" + "\n" + "\n" + "\n"
               + date + "\n"
               + canonicalizedResource;
    }

    /**
     * Gets gmt date *
     *
     * @return the gmt date
     * @since 0.0.1
     */
    public static String getGMTDate() {
        Calendar cd = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss 'GMT'", Locale.US);
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
        return sdf.format(cd.getTime());
    }
}
