package info.dong4j.idea.plugin.util;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * <p>Company: 成都返空汇网络技术有限公司 </p>
 * <p>Description: todo-dong4j : (2020年04月22日 1:41 上午) [未完成]</p>
 * https://helpcdn.aliyun.com/document_detail/31947.html
 *
 * @author dong4j
 * @version x.x.x
 * @email "mailto:dongshijie@fkhwl.com"
 * @date 2020.04.21 23:29
 */
public class AliyunOssUtils {

    private static final String ossBucket = "xxx";
    private static final String accessKeyId = "xxx";
    private static final String secretAccessKey = "xxx";

    //可根据自己的oss产品自行更改域名
    private static final String endpoint = "oss-cn-shanghai.aliyuncs.com/";

    private final static String CHARSET_UTF8 = "utf8";
    private final static String ALGORITHM = "HmacSHA1";

    /**
     * Gets oss obj *
     *
     * @param key key
     * @return the oss obj
     * @throws IOException io exception
     */
    public static String getOssObj(String key) throws IOException {
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
     * @param key     key
     * @param content content
     * @return the string
     * @throws IOException io exception
     */
    public static String putOssObj(String key, String content) throws IOException {
        String date = getGMTDate();

        String signResourcePath = "/" + ossBucket + key;
        String connectUrl = "http://" + ossBucket + "." + endpoint;

        String Signature = (hmacSha1(buildPutSignData(date, signResourcePath), secretAccessKey));
        String Authorization = "OSS " + accessKeyId + ":" + Signature;

        URL putUrl = new URL(connectUrl + key);
        HttpURLConnection connection;
        StringBuffer sbuffer = null;

        try {
            //添加 请求内容
            connection = (HttpURLConnection) putUrl.openConnection();
            //设置http连接属性
            connection.setDoOutput(true);
            connection.setRequestMethod("PUT");
            //设置请求头
            connection.setRequestProperty("Date", date);
            connection.setRequestProperty("Authorization", Authorization);

            connection.setReadTimeout(10000);//设置读取超时时间
            connection.setConnectTimeout(10000);//设置连接超时时间
            connection.connect();
            OutputStream out = connection.getOutputStream();
            out.write(new String(content).getBytes());
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
                    lines = new String(lines.getBytes(), "utf-8");
                    sbuffer.append(lines);
                }
                reader.close();
            } else {
                //连接失败
                return null;
            }
            //断开连接
            connection.disconnect();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return key;
    }

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

    public static String hmacSha1(String data, String key) {
        try {
            Mac mac = Mac.getInstance("HmacSHA1");
            SecretKeySpec keySpec = new SecretKeySpec(key.getBytes(), ALGORITHM);
            mac.init(keySpec);
            byte[] rawHmac;
            rawHmac = mac.doFinal(data.getBytes(CHARSET_UTF8));
            return new String(Base64.encodeBase64(rawHmac));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static String buildGetSignData(String Date, String CanonicalizedResource) {
        return "GET" + "\n" + "\n" + "\n"
               + Date + "\n"
               + CanonicalizedResource;
    }

    public static String buildPutSignData(String Date, String CanonicalizedResource) {
        return "PUT" + "\n" + "\n" + "\n"
               + Date + "\n"
               + CanonicalizedResource;
    }

    public static String getGMTDate() {
        Calendar cd = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss 'GMT'", Locale.US);
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
        return sdf.format(cd.getTime());
    }
}
