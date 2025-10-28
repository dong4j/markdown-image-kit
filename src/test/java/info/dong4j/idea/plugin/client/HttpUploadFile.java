package info.dong4j.idea.plugin.client;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.activation.MimetypesFileTypeMap;

/**
 * HTTP文件上传工具类
 * <p>
 * 提供通过模拟POST方式提交表单实现图片或文件上传的功能，支持文本参数和文件参数的上传，可指定文件类型Content-Type。
 * 支持自动识别文件类型，适用于需要通过HTTP协议上传文件的场景。
 *
 * @author zdz8207
 * @version 1.0
 * @date 2021.02.14
 * @since 1.1.0
 */
@SuppressWarnings("D")
public class HttpUploadFile {
    /**
     * 主程序入口，用于执行测试方法 testUploadImage
     * <p>
     * 该方法在程序启动时调用，主要作用是运行图像上传功能的测试用例
     *
     * @param args 命令行参数，通常用于传递运行时配置信息
     * @since 1.1.0
     */
    public static void main(String[] args) {
        testUploadImage();
    }

    /**
     * 测试上传PNG图片功能
     * <p>
     * 该方法用于演示如何通过表单方式上传PNG格式图片，并输出上传结果。
     *
     * @since 1.1.0
     */
    public static void testUploadImage() {
        String url = "https://sm.ms/api/upload";
        String fileName = "/Users/dong4j/Downloads/mik.png";
        Map<String, String> textMap = new HashMap<String, String>();
        //可以设置多个input的name，value
        textMap.put("name", "testname");
        textMap.put("type", "2");
        //设置file的name，路径
        Map<String, String> fileMap = new HashMap<>();
        fileMap.put("smfile", fileName);
        String contentType = "";//image/png
        String ret = formUpload(url, textMap, fileMap,contentType);
        System.out.println(ret);
        //{"status":"0","message":"add succeed","baking_url":"group1\/M00\/00\/A8\/CgACJ1Zo-LuAN207AAQA3nlGY5k151.png"}
    }

    /**
     * 用于执行图片上传操作，发送multipart/form-data格式的POST请求
     * <p>
     * 根据提供的URL、文本参数、文件参数和内容类型，构建并发送上传请求，最后返回服务器响应数据
     *
     * @param urlStr      上传的目标URL
     * @param textMap     需要上传的文本参数，键值对形式
     * @param fileMap     需要上传的文件参数，键值对形式，值为文件路径
     * @param contentType 上传文件的内容类型，若未传入则根据文件名自动判断，默认为application/octet-stream
     * @return 返回服务器响应的字符串数据
     * @since 1.1.0
     */
    @SuppressWarnings("rawtypes")
    public static String formUpload(String urlStr, Map<String, String> textMap,
            Map<String, String> fileMap,String contentType) {
        String res = "";
        HttpURLConnection conn = null;
        // boundary就是request头和上传文件内容的分隔符
        String BOUNDARY = "---------------------------123821742118716";
        try {
            URL url = new URL(urlStr);
            conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(3000);
            conn.setReadTimeout(5000);
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setUseCaches(false);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Connection", "Keep-Alive");
            conn.setRequestProperty("User-Agent",
                                    "Mozilla/5.0 (Windows; U; Windows NT 6.1; zh-CN; rv:1.9.2.6)");
            conn.setRequestProperty("Content-Type",
                                    "multipart/form-data; boundary=" + BOUNDARY);
            OutputStream out = new DataOutputStream(conn.getOutputStream());
            // text
            if (textMap != null) {
                StringBuilder strBuf = new StringBuilder();
                for (Map.Entry<String, String> stringStringEntry : textMap.entrySet()) {
                    String inputName = (String) ((Map.Entry) stringStringEntry).getKey();
                    String inputValue = (String) ((Map.Entry) stringStringEntry).getValue();
                    if (inputValue == null) {
                        continue;
                    }
                    strBuf.append("\r\n").append("--").append(BOUNDARY)
                        .append("\r\n");
                    strBuf.append("Content-Disposition: form-data; name=\"").append(inputName).append("\"\r\n\r\n");
                    strBuf.append(inputValue);
                }
                out.write(strBuf.toString().getBytes());
            }
            // file
            if (fileMap != null) {
                for (Map.Entry<String, String> stringStringEntry : fileMap.entrySet()) {
                    String inputName = (String) ((Map.Entry) stringStringEntry).getKey();
                    String inputValue = (String) ((Map.Entry) stringStringEntry).getValue();
                    if (inputValue == null) {
                        continue;
                    }
                    File file = new File(inputValue);
                    String filename = file.getName();

                    //没有传入文件类型，同时根据文件获取不到类型，默认采用application/octet-stream
                    contentType = new MimetypesFileTypeMap().getContentType(file);
                    //contentType非空采用filename匹配默认的图片类型
                    if (!"".equals(contentType)) {
                        if (filename.endsWith(".png")) {
                            contentType = "image/png";
                        } else if (filename.endsWith(".jpg") || filename.endsWith(".jpeg") || filename.endsWith(".jpe")) {
                            contentType = "image/jpeg";
                        } else if (filename.endsWith(".gif")) {
                            contentType = "image/gif";
                        } else if (filename.endsWith(".ico")) {
                            contentType = "image/image/x-icon";
                        }
                    }
                    if (contentType == null || contentType.isEmpty()) {
                        contentType = "application/octet-stream";
                    }
                    String strBuf = "\r\n" + "--" + BOUNDARY +
                                    "\r\n" +
                                    "Content-Disposition: form-data; name=\""
                                    + inputName + "\"; filename=\"" + filename
                                    + "\"\r\n" +
                                    "Content-Type:" + contentType + "\r\n\r\n";
                    out.write(strBuf.getBytes());
                    DataInputStream in = new DataInputStream(new FileInputStream(file));
                    int bytes;
                    byte[] bufferOut = new byte[1024];
                    while ((bytes = in.read(bufferOut)) != -1) {
                        out.write(bufferOut, 0, bytes);
                    }
                    in.close();
                }
            }
            byte[] endData = ("\r\n--" + BOUNDARY + "--\r\n").getBytes();
            out.write(endData);
            out.flush();
            out.close();
            // 读取返回数据
            StringBuilder strBuf = new StringBuilder();
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    conn.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                strBuf.append(line).append("\n");
            }
            res = strBuf.toString();
            reader.close();
            reader = null;
        } catch (Exception e) {
            System.out.println("发送POST请求出错。" + urlStr);
        } finally {
            if (conn != null) {
                conn.disconnect();
                conn = null;
            }
        }
        return res;
    }
}
