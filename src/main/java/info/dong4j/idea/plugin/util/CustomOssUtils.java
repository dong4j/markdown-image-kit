package info.dong4j.idea.plugin.util;


import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import lombok.extern.slf4j.Slf4j;

/**
 * <p>Description: 上传到自定义图床 </p>
 *
 * @author dong4j
 * @version 1.0.0
 * @email "mailto:dong4j@gmail.com"
 * @date 2020.04.25 16:36
 * @since 1.1.0
 */
@Slf4j
public class CustomOssUtils {
    /** 边界标识 */
    private final static String BOUNDARY = UUID.randomUUID().toString().toLowerCase().replaceAll("-", "");
    /** PREFIX */
    private final static String PREFIX = "--";
    /** LINE_END */
    private final static String LINE_END = "\r\n";

    public static Map<String, String> putObject(String api,
                                                String requestKey,
                                                String httpMethod,
                                                String fileName,
                                                InputStream inputStream,
                                                Map<String, String> requestText,
                                                Map<String, String> header) throws Exception {
        URL url = new URL(api);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        connection.setDoOutput(true);
        connection.setDoInput(true);
        connection.setUseCaches(false);
        connection.setConnectTimeout(3000);
        connection.setReadTimeout(5000);
        connection.setRequestMethod(httpMethod);
        connection.setRequestProperty("Accept", "application/json");
        connection.setRequestProperty("Connection", "keep-alive");
        connection.setRequestProperty("User-Agent", "markdown-image-kit");
        connection.setRequestProperty("Charset", "UTF-8");
        connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + BOUNDARY);

        if (header != null && !header.isEmpty()) {
            header.forEach(connection::setRequestProperty);
        }

        connection.connect();

        // 往服务器端写内容 也就是发起http请求需要带的参数
        StringBuilder buffer;
        String params;
        String filePart;
        String headerInfo;
        StringBuilder result;
        try (OutputStream os = new DataOutputStream(connection.getOutputStream())) {

            // 请求参数部分
            params = writeParams(requestText, os);
            // 请求上传文件部分
            filePart = writeFile(requestKey, fileName, inputStream, os);
            // 请求结束标志
            String endTarget = PREFIX + BOUNDARY + PREFIX + LINE_END;
            os.write(endTarget.getBytes());
            os.flush();
            result = new StringBuilder();
            // 读取服务器端返回的内容
            result.append("ResponseCode: ")
                .append(connection.getResponseCode())
                .append(", ResponseMessage: ")
                .append(connection.getResponseMessage())
                .append("\n");

            InputStream input;
            if (connection.getResponseCode() == 200) {
                input = connection.getInputStream();
            } else {
                input = connection.getErrorStream();
            }

            BufferedReader br = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8));

            String line;
            buffer = new StringBuilder();
            while ((line = br.readLine()) != null) {
                buffer.append(line);
            }

            headerInfo = connection.getRequestMethod() + " " + connection.getURL()
                         + "\nContent-Type: " + connection.getContentType() + "\n";

        } finally {
            // 断开连接
            connection.disconnect();
        }
        return new HashMap<String, String>() {
            private static final long serialVersionUID = -5643217270707235408L;

            {
                this.put("headerInfo", headerInfo);
                this.put("params", params);
                this.put("filePart", filePart);
                this.put("response", result.toString());
                this.put("json", buffer.toString());
            }
        };
    }

    private static String writeFile(String requestKey, String fileName, InputStream inputStream, OutputStream os) throws Exception {
        StringBuilder msg = new StringBuilder("请求上传文件部分:\n");
        StringBuilder requestParams = new StringBuilder();
        requestParams.append(PREFIX).append(BOUNDARY).append(LINE_END);
        requestParams.append("Content-Disposition: form-data; name=\"")
            .append(requestKey).append("\"; filename=\"")
            .append(fileName).append("\"")
            .append(LINE_END);
        requestParams.append("Content-Type:")
            .append("application/octet-stream")
            .append(LINE_END);

        // 参数头设置完以后需要两个换行，然后才是参数内容
        requestParams.append(LINE_END);

        os.write(requestParams.toString().getBytes());

        try (InputStream is = inputStream) {
            byte[] buffer = new byte[1024 * 1024];
            int len;
            while ((len = is.read(buffer)) != -1) {
                os.write(buffer, 0, len);
            }
            os.write(LINE_END.getBytes());
            os.flush();
        }
        msg.append(requestParams.toString());
        return msg.toString();
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
        if (requestText == null || requestText.isEmpty()) {
            msg += "空\n";
        } else {
            StringBuilder requestParams = new StringBuilder();
            Set<Map.Entry<String, String>> set = requestText.entrySet();
            for (Map.Entry<String, String> entry : set) {
                requestParams.append(PREFIX).append(BOUNDARY).append(LINE_END);
                requestParams.append("Content-Disposition: form-data; name=\"").append(entry.getKey()).append("\"").append(LINE_END);
                requestParams.append("Content-Type: text/plain; charset=utf-8").append(LINE_END);
                // 参数头设置完以后需要两个换行，然后才是参数内容
                requestParams.append(LINE_END);
                requestParams.append(entry.getValue());
                requestParams.append(LINE_END);
            }
            os.write(requestParams.toString().getBytes());
            os.flush();
            msg += requestParams.toString();
        }
        return msg;
    }


}
