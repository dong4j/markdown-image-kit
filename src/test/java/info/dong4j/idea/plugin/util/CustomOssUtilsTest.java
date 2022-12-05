package info.dong4j.idea.plugin.util;


import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.junit.Test;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

import lombok.extern.slf4j.Slf4j;

import static org.junit.Assert.assertEquals;

/**
 * <p>Description:  </p>
 *
 * @author dong4j
 * @version 1.0.0
 * @email "mailto:dong4j@gmail.com"
 * @date 2020.04.25 16:36
 * @since 1.5.0
 */
@Slf4j
public class CustomOssUtilsTest {
    /** API */
    private static final String API = "http://127.0.0.1:8080/upload";
    /** 边界标识 */
    private final static String BOUNDARY = UUID.randomUUID().toString().toLowerCase().replaceAll("-", "");
    /** PREFIX */
    private final static String PREFIX = "--";// 必须存在
    /** LINE_END */
    private final static String LINE_END = "\r\n";

    /**
     * POST Multipart Request
     *
     * @param requestUrl  请求url
     * @param requestText 请求参数(字符串键值对map)
     * @param requestFile 请求上传的文件(File)
     * @param header      header
     * @return string
     * @throws Exception exception
     * @Description:
     * @since 1.5.0
     */
    public static String sendRequest(String requestUrl,
                                     Map<String, String> requestText,
                                     Map<String, File> requestFile,
                                     Map<String, String> header) throws Exception {

        URL url = new URL(requestUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        connection.setDoOutput(true);
        connection.setDoInput(true);
        connection.setUseCaches(false);
        connection.setConnectTimeout(1000 * 10);
        connection.setReadTimeout(1000 * 10);
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Accept", "*/*");
        connection.setRequestProperty("Connection", "keep-alive");
        connection.setRequestProperty("User-Agent", "markdown-image-kit");
        connection.setRequestProperty("Charset", "UTF-8");
        connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + BOUNDARY);

        if (header != null && !header.isEmpty()) {
            header.forEach(connection::setRequestProperty);
        }

        connection.connect();

        // 往服务器端写内容 也就是发起http请求需要带的参数

        StringBuffer buffer;

        try (OutputStream os = new DataOutputStream(connection.getOutputStream())) {

            // 请求参数部分
            writeParams(requestText, os);
            // 请求上传文件部分
            writeFile(requestFile, os);
            // 请求结束标志
            String endTarget = PREFIX + BOUNDARY + PREFIX + LINE_END;
            os.write(endTarget.getBytes());
            os.flush();

            // 读取服务器端返回的内容
            log.info("======================响应体=========================");
            log.info("ResponseCode:" + connection.getResponseCode() + ",ResponseMessage:" + connection.getResponseMessage());

            InputStream input;
            if (connection.getResponseCode() == 200) {
                input = connection.getInputStream();
            } else {
                input = connection.getErrorStream();
            }

            BufferedReader br = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8));
            buffer = new StringBuffer();
            String line;
            while ((line = br.readLine()) != null) {
                buffer.append(line);
            }
            log.info("返回报文:" + buffer.toString());

        } catch (Exception e) {
            throw new Exception(e);
        } finally {
            // 断开连接
            connection.disconnect();
        }
        return buffer.toString();
    }

    /**
     * 对post参数进行编码处理并写入数据流中
     *
     * @param requestText request text
     * @param os          os
     * @throws Exception exception
     * @since 1.5.0
     */
    private static String writeParams(Map<String, String> requestText,
                                      OutputStream os) throws Exception {
        String msg = "请求参数部分:\n";
        try {
            if (requestText == null || requestText.isEmpty()) {
                msg += "空";
            } else {
                StringBuilder requestParams = new StringBuilder();
                Set<Map.Entry<String, String>> set = requestText.entrySet();
                for (Map.Entry<String, String> entry : set) {
                    requestParams.append(PREFIX).append(BOUNDARY).append(LINE_END);
                    requestParams.append("Content-Disposition: form-data; name=\"").append(entry.getKey()).append("\"").append(LINE_END);
                    requestParams.append("Content-Type: text/plain; charset=utf-8").append(LINE_END);
                    requestParams.append(LINE_END);// 参数头设置完以后需要两个换行，然后才是参数内容
                    requestParams.append(entry.getValue());
                    requestParams.append(LINE_END);
                }
                os.write(requestParams.toString().getBytes());
                os.flush();

                msg += requestParams.toString();
            }

            log.info(msg);
        } catch (Exception e) {
            throw new Exception(e);
        }

        return msg;
    }

    /**
     * 对post上传的文件进行编码处理并写入数据流中
     *
     * @param requestFile request file
     * @param os          os
     * @throws Exception exception
     * @since 1.5.0
     */
    private static void writeFile(Map<String, File> requestFile,
                                  OutputStream os) throws Exception {
        try {
            StringBuilder msg = new StringBuilder("请求上传文件部分:\n");
            if (requestFile == null || requestFile.isEmpty()) {
                msg.append("空");
            } else {
                StringBuilder requestParams = new StringBuilder();
                Set<Map.Entry<String, File>> set = requestFile.entrySet();
                for (Map.Entry<String, File> entry : set) {
                    requestParams.append(PREFIX).append(BOUNDARY).append(LINE_END);
                    requestParams.append("Content-Disposition: form-data; name=\"")
                        .append(entry.getKey()).append("\"; filename=\"")
                        .append(entry.getValue().getName()).append("\"")
                        .append(LINE_END);
                    requestParams.append("Content-Type:")
                        .append(getContentType(entry.getValue()))
                        .append(LINE_END);

                    // 参数头设置完以后需要两个换行，然后才是参数内容
                    requestParams.append(LINE_END);

                    os.write(requestParams.toString().getBytes());

                    try (InputStream is = new FileInputStream(entry.getValue())) {
                        byte[] buffer = new byte[1024 * 1024];
                        int len;
                        while ((len = is.read(buffer)) != -1) {
                            os.write(buffer, 0, len);
                        }
                        os.write(LINE_END.getBytes());
                        os.flush();
                    }
                    msg.append(requestParams.toString());
                }
            }
            log.info(msg.toString());
        } catch (Exception e) {
            throw new Exception(e);
        }
    }


    /**
     * ContentType
     *
     * @param file file
     * @return content type
     * @Description:
     * @since 1.5.0
     */
    public static String getContentType(File file) {
        String streamContentType = "application/octet-stream";
        String imageContentType;

        try (ImageInputStream image = ImageIO.createImageInputStream(file)) {
            if (image == null) {
                return streamContentType;
            }
            Iterator<ImageReader> it = ImageIO.getImageReaders(image);
            if (it.hasNext()) {
                imageContentType = "image/" + it.next().getFormatName();
                return imageContentType;
            }
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }
        return streamContentType;
    }

    /**
     * Test 1
     *
     * @throws Exception exception
     * @since 1.5.0
     */
    @Test
    public void test_1() throws Exception {

        Map<String, String> requestText = new HashMap<>();
        requestText.put("xxx", "xxx");
        requestText.put("yyy", "yyy");
        Map<String, File> requestFile = new HashMap<>();
        requestFile.put("fileName", new File("/Users/dong4j/Downloads/xu.png"));

        Map<String, String> header = new HashMap<>();
        header.put("x-header", new Date().getTime() + "");
        String request = CustomOssUtilsTest.sendRequest(API, requestText, requestFile, header);
        log.info(request);
    }

    @Test
    public void test_2() throws Exception {
        String upload1 = this.upload("{\"data\": {\"url\": \"可访问的图片地址\"}}", "data.url");
        assertEquals(upload1, "可访问的图片地址");

        String upload2 = this.upload("{\"url\": \"可访问的图片地址\"}", "url");
        assertEquals(upload2, "可访问的图片地址");

    }

    @Test(expected = RuntimeException.class)
    public void test_3() throws Exception {
        this.upload("{\"data1\": {\"url\": \"可访问的图片地址\"}}", "data.url");
    }

    @Test(expected = RuntimeException.class)
    public void test_4() throws Exception {
        this.upload("{\"data\": {\"url1\": \"可访问的图片地址\"}}", "data.url");
    }

    @Test(expected = RuntimeException.class)
    public void test_5() throws Exception {
        this.upload("{\"data\": {\"url1\": {\"xxx\":\"可访问的图片地址\"}}}", "data.url");
    }

    public String upload(String json, String path) throws Exception {
        String[] split = path.split("\\.");

        JsonParser parser = new JsonParser();
        JsonElement parse = parser.parse(json);

        String url = this.getUrl(parse, split, split[0], 0);
        if (StringUtils.isNotBlank(url)) {
            return url;
        }

        throw new RuntimeException(json);
    }

    private String getUrl(JsonElement data, String[] split, String path, int index) {
        if (data != null) {
            if (data.isJsonObject()) {
                JsonObject asJsonObject1 = data.getAsJsonObject();
                JsonElement url = asJsonObject1.get(split[index]);
                index++;
                if (index == split.length) {
                    if (url == null) {
                        return "";
                    } else {
                        return url.getAsString();
                    }
                }
                return this.getUrl(url, split, split[index], index);
            } else {
                return data.getAsString();
            }
        }
        return "";
    }


}
