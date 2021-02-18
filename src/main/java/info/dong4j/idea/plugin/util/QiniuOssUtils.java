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

package info.dong4j.idea.plugin.util;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

/**
 * <p>Company: 成都返空汇网络技术有限公司</p>
 * <p>Description:  </p>
 * https://developer.qiniu.com/kodo/1208/upload-token
 * https://developer.qiniu.com/kodo/1312/upload
 *
 * @author dong4j
 * @version 1.0.0
 * @email "mailto:dong4j@fkhwl.com"
 * @date 2021.02.18 17:57
 * @since 1.6.1
 */
@UtilityClass
@Slf4j
public class QiniuOssUtils {
    /** 边界标识 */
    private final static String BOUNDARY = UUID.randomUUID().toString().toLowerCase().replaceAll("-", "");
    /** PREFIX */
    private final static String PREFIX = "--";
    /** LINE_END */
    private final static String LINE_END = "\r\n";

    /**
     * Put object
     *
     * @param fileName        file name
     * @param content         content
     * @param ossBucket       oss bucket
     * @param host            host
     * @param accessKeyId     access key id
     * @param secretAccessKey secret access key
     * @throws Exception exception
     * @since 1.6.1
     */
    public static void putObject(String fileName,
                                 InputStream content,
                                 String ossBucket,
                                 String host,
                                 String accessKeyId,
                                 String secretAccessKey) throws Exception {
        String token = uploadToken(ossBucket, fileName, 3600L * 1000 * 24 * 365 * 10, null, accessKeyId, secretAccessKey);

        URL url = new URL("http://upload.qiniu.com");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        connection.setDoOutput(true);
        connection.setDoInput(true);
        connection.setUseCaches(false);
        connection.setConnectTimeout(3000);
        connection.setReadTimeout(5000);
        connection.setRequestMethod("POST");

        connection.setRequestProperty("Host", host);
        connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + BOUNDARY);

        connection.connect();

        // 往服务器端写内容 也就是发起http请求需要带的参数
        StringBuilder buffer;
        String params;
        String filePart;
        String headerInfo;
        StringBuilder result;
        try (OutputStream os = new DataOutputStream(connection.getOutputStream())) {

            Map<String, String> requestText = new HashMap<>();
            requestText.put("key", fileName);
            requestText.put("token", token);

            // 请求参数部分
            params = writeParams(requestText, os);
            // 请求上传文件部分
            filePart = writeFile("file", fileName, content, os);
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

            if (connection.getResponseCode() == 200) {
                showResponse(connection.getInputStream());
            }
            if (connection.getResponseCode() != 200) {
                showResponse(connection.getErrorStream());

                headerInfo = connection.getRequestMethod() + " " + connection.getURL()
                             + "\nContent-Type: " + connection.getContentType() + "\n";

                Map<String, String> map = new HashMap<String, String>() {
                    private static final long serialVersionUID = -5643217270707235408L;

                    {
                        this.put("headerInfo", headerInfo);
                        this.put("params", params);
                        this.put("filePart", filePart);
                        this.put("response", result.toString());
                    }
                };
                log.trace("{}", map);
                throw new RuntimeException(connection.getResponseCode() + " " + connection.getResponseMessage());
            }

        } finally {
            // 断开连接
            connection.disconnect();
        }

    }

    /**
     * Show response
     *
     * @param input input
     * @throws IOException io exception
     * @since 1.6.1
     */
    private static void showResponse(InputStream input) throws IOException {
        StringBuilder buffer;
        BufferedReader br = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8));

        String line;
        buffer = new StringBuilder();
        while ((line = br.readLine()) != null) {
            buffer.append(line);
        }
        log.info("{}", buffer);
    }

    /**
     * Write file
     *
     * @param requestKey  request key
     * @param fileName    file name
     * @param inputStream input stream
     * @param os          os
     * @return the string
     * @throws Exception exception
     * @since 1.6.1
     */
    private static String writeFile(String requestKey, String fileName, InputStream inputStream, OutputStream os) throws Exception {
        StringBuilder msg = new StringBuilder("请求上传文件部分:\n");
        StringBuilder requestParams = new StringBuilder();
        requestParams.append(PREFIX).append(BOUNDARY).append(LINE_END);
        requestParams.append("Content-Disposition: form-data; name=\"")
            .append(requestKey).append("\"; filename=\"")
            .append(fileName).append("\"")
            .append(LINE_END);
        requestParams.append("Content-Type: ")
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
     * @return the string
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
                // requestParams.append("Content-Type: text/plain; charset=utf-8").append(LINE_END);
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

    /**
     * 生成上传token
     *
     * @param bucket          空间名
     * @param key             key，可为 null
     * @param expires         有效时长，单位秒。默认3600s
     * @param policy          上传策略的其它参数，如 new StringMap().put("endUser", "uid").putNotEmpty("returnBody", "")。                scope通过
     *                        bucket、key间接设置，deadline 通过 expires 间接设置
     * @param accessKey       access key
     * @param secretAccessKey secret access key
     * @return 生成的上传token string
     * @since 1.6.1
     */
    public static String uploadToken(String bucket, String key, long expires, StringMap policy, String accessKey, String secretAccessKey) {
        long deadline = System.currentTimeMillis() / 1000 + expires;
        return uploadTokenWithDeadline(bucket, key, deadline, policy, true, accessKey, secretAccessKey);
    }

    /**
     * Upload token with deadline
     *
     * @param bucket          bucket
     * @param key             key
     * @param deadline        deadline
     * @param policy          policy
     * @param strict          strict
     * @param accessKey       access key
     * @param secretAccessKey secret access key
     * @return the string
     * @since 1.6.1
     */
    public static String uploadTokenWithDeadline(String bucket,
                                                 String key,
                                                 long deadline,
                                                 StringMap policy,
                                                 boolean strict,
                                                 String accessKey,
                                                 String secretAccessKey) {
        String scope = bucket;
        if (key != null) {
            scope = bucket + ":" + key;
        }
        StringMap x = new StringMap();
        copyPolicy(x, policy, strict);
        x.put("scope", scope);
        x.put("deadline", deadline);

        String s = Json.encode(x);
        return signWithData(StringUtils.utf8Bytes(s), accessKey, secretAccessKey);
    }

    /**
     * Sign with data
     *
     * @param data            data
     * @param accessKey       access key
     * @param secretAccessKey secret access key
     * @return the string
     * @since 1.6.1
     */
    public static String signWithData(byte[] data, String accessKey, String secretAccessKey) {
        String s = UrlSafeBase64.encodeToString(data);
        return sign(StringUtils.utf8Bytes(s), accessKey, secretAccessKey) + ":" + s;
    }

    /**
     * Sign
     *
     * @param data            data
     * @param accessKey       access key
     * @param secretAccessKey secret access key
     * @return the string
     * @since 1.6.1
     */
    public static String sign(byte[] data, String accessKey, String secretAccessKey) {
        Mac mac = createMac(secretAccessKey);
        String encodedSign = UrlSafeBase64.encodeToString(mac.doFinal(data));
        return accessKey + ":" + encodedSign;
    }

    /**
     * Create mac
     *
     * @param secretAccessKey secret access key
     * @return the mac
     * @since 1.6.1
     */
    private static Mac createMac(String secretAccessKey) {
        Mac mac;
        try {
            mac = javax.crypto.Mac.getInstance("HmacSHA1");
            byte[] sk = StringUtils.utf8Bytes(secretAccessKey);
            SecretKeySpec secretKeySpec = new SecretKeySpec(sk, "HmacSHA1");
            mac.init(secretKeySpec);
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
            throw new IllegalArgumentException(e);
        }
        return mac;
    }


    /**
     * Copy policy
     *
     * @param policy       policy
     * @param originPolicy origin policy
     * @param strict       strict
     * @since 1.6.1
     */
    private static void copyPolicy(StringMap policy, StringMap originPolicy, boolean strict) {
        if (originPolicy == null) {
            return;
        }
        originPolicy.forEach((key, value) -> {
            if (StringUtils.inStringArray(key, deprecatedPolicyFields)) {
                throw new IllegalArgumentException(key + " is deprecated!");
            }
            if (!strict || StringUtils.inStringArray(key, policyFields)) {
                policy.put(key, value);
            }
        });
    }

    /** deprecatedPolicyFields */
    private static final String[] deprecatedPolicyFields = new String[] {
        "asyncOps",
        };

    /** policyFields */
    private static final String[] policyFields = new String[] {
        "callbackUrl",
        "callbackBody",
        "callbackHost",
        "callbackBodyType",
        "callbackFetchKey",

        "returnUrl",
        "returnBody",

        "endUser",
        "saveKey",
        "insertOnly",
        "isPrefixalScope",

        "detectMime",
        "mimeLimit",
        "fsizeLimit",
        "fsizeMin",

        "persistentOps",
        "persistentNotifyUrl",
        "persistentPipeline",

        "deleteAfterDays",
        "fileType",
        };
}
