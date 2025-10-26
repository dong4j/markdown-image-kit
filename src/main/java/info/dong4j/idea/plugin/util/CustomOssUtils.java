package info.dong4j.idea.plugin.util;


import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Serial;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import lombok.extern.slf4j.Slf4j;

/**
 * 自定义图床上传工具类
 * <p>
 * 提供将文件上传至自定义图床的通用方法，支持多部分表单数据（multipart/form-data）上传，包含文件和参数的处理逻辑。
 * 该工具类封装了HTTP连接的建立、参数和文件的写入、响应结果的解析等功能，适用于需要将图片或文件上传至第三方图床服务的场景。
 * </p>
 *
 * @author dong4j
 * @version 1.0.0
 * @date 2020.04.25
 * @since 1.0.0
 */
@Slf4j
public class CustomOssUtils {
    /** 边界标识，用于标识请求的边界，由 UUID 生成并格式化为小写字符串 */
    private final static String BOUNDARY = UUID.randomUUID().toString().toLowerCase().replaceAll("-", "");
    /** 命令行参数前缀，用于标识参数的开始 */
    private final static String PREFIX = "--";
    /** 换行符，用于表示文本行结束的字符序列 */
    private final static String LINE_END = "\r\n";

    /**
     * 向指定API地址发送HTTP请求并返回响应信息
     * <p>
     * 该方法用于构建并发送HTTP请求，支持上传文件和请求参数，返回包含请求头信息、参数、文件部分、响应码和响应内容的Map对象。
     *
     * @param api         目标API地址
     * @param requestKey  请求参数的键
     * @param httpMethod  HTTP请求方法（如GET、POST等）
     * @param fileName    上传文件的文件名
     * @param inputStream 上传文件的输入流
     * @param requestText 请求参数的键值对Map
     * @param header      请求头信息的键值对Map
     * @return 包含请求和响应信息的Map对象，包含headerInfo、params、filePart、response和json字段
     * @throws Exception 发生异常时抛出
     */
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
            /** 序列化版本号，用于确保类的兼容性 */
            @Serial
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

    /**
     * 将上传文件的请求数据写入输出流
     * <p>
     * 该方法用于构建并写入包含文件上传信息的请求体，包括文件名、内容类型等参数头信息，以及文件内容本身。
     *
     * @param requestKey  请求参数的键名
     * @param fileName    文件名
     * @param inputStream 文件输入流，用于读取文件内容
     * @param os          输出流，用于写入构建好的请求数据
     * @return 构建好的请求数据字符串
     * @throws Exception 如果在写入过程中发生异常
     */
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
        msg.append(requestParams);
        return msg.toString();
    }

    /**
     * 对请求参数进行编码处理并写入数据流
     * <p>
     * 将传入的参数Map编码为HTTP请求参数格式，并写入指定的输出流中。如果参数为空，则返回"空"的提示信息。
     *
     * @param requestText 请求参数的Map集合
     * @param os          输出流对象，用于写入编码后的参数数据
     * @return 编码后的请求参数信息字符串
     * @throws Exception 如果写入过程中发生异常
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
