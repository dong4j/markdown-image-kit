package info.dong4j.idea.plugin.util;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

/**
 * OssUtils 工具类
 * <p>
 * 提供与对象存储服务（OSS）相关的实用方法，包括发送HTTP请求、建立HTTP连接等操作，用于简化OSS相关接口的调用。
 *
 * @author 作者
 * @version 1.0.0
 * @date 2025.10.24
 * @since 1.0.0
 */
@UtilityClass
@Slf4j
public class OssUtils {

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
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpGet httpGet = new HttpGet(url);
            for (String key : head.keySet()) {
                httpGet.setHeader(key, head.get(key));
            }
            HttpResponse response = client.execute(httpGet);
            response.getEntity().getContent();
            HttpEntity entity = response.getEntity();
            return EntityUtils.toString(entity, StandardCharsets.UTF_8);
        }
    }

    /**
     * 建立与指定URL的HTTP连接
     * <p>
     * 根据提供的URL和HTTP方法创建并配置HTTP连接对象，设置连接和读取超时时间，以及请求方法。
     *
     * @param url        要连接的URL地址
     * @param httpMethod 请求使用的HTTP方法（如GET、POST等）
     * @return 配置好的HttpURLConnection对象
     * @throws IOException 如果URL无效或发生其他I/O错误
     */
    public static HttpURLConnection connect(String url, String httpMethod) throws IOException {
        URL putUrl;
        try {
            putUrl = new URI(url).toURL();

            HttpURLConnection connection = (HttpURLConnection) putUrl.openConnection();

            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setUseCaches(false);
            connection.setConnectTimeout(3000);
            connection.setReadTimeout(5000);

            connection.setRequestMethod(httpMethod);
            return connection;
        } catch (MalformedURLException | URISyntaxException e) {
            throw new IOException("Invalid URL: " + url, e);
        }
    }
}
