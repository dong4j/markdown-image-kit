package info.dong4j.idea.plugin.util;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Serial;
import java.net.HttpURLConnection;
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
 * 七牛云对象存储（OSS）工具类
 * <p>
 * 提供与七牛云对象存储服务相关的操作，包括上传文件、生成上传Token、处理上传请求参数等核心功能。支持多部分上传、文件流处理以及上传策略配置。
 * <p>
 * 该工具类封装了与七牛云API交互的细节，简化了上传文件到OSS的流程，适用于需要将文件上传至七牛云存储的业务场景。
 *
 * @author dong4j
 * @version 1.0.0
 * @date 2021.02.18
 * @since 1.6.1
 */
@UtilityClass
@Slf4j
public class QiniuOssUtils {
    /** 边界标识，用于区分不同请求的唯一标识符 */
    private final static String BOUNDARY = UUID.randomUUID().toString().toLowerCase().replaceAll("-", "");
    /** 前缀标识符，用于区分不同类型的参数或标识 */
    private final static String PREFIX = "--";
    /** 换行符，用于表示行结束符 */
    private final static String LINE_END = "\r\n";

    /**
     * 向七牛云OSS上传文件
     * <p>
     * 该方法用于将指定的文件内容上传到七牛云OSS服务。需要提供文件名、文件内容、OSS存储桶名称、主机地址、Access Key ID和Secret Access Key等参数。
     * 上传过程中会构建相应的HTTP请求，并处理上传结果。如果上传失败，会记录相关信息并抛出运行时异常。
     *
     * @param fileName        要上传的文件名
     * @param content         要上传的文件内容流
     * @param ossBucket       OSS存储桶名称
     * @param host            上传服务的主机地址
     * @param accessKeyId     Access Key ID
     * @param secretAccessKey Secret Access Key
     * @throws Exception 上传过程中发生异常时抛出
     * @since 1.6.1
     */
    public static void putObject(String fileName,
                                 InputStream content,
                                 String ossBucket,
                                 String host,
                                 String accessKeyId,
                                 String secretAccessKey) throws Exception {
        String token = uploadToken(ossBucket, fileName, 3600L * 1000 * 24 * 365 * 10, null, accessKeyId, secretAccessKey);

        HttpURLConnection connection = OssUtils.connect("http://upload.qiniu.com", "POST");

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

                Map<String, String> map = new HashMap<>() {
                    /** 序列化版本号，用于确保类的兼容性 */
                    @Serial
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
     * 显示输入流中的响应内容
     * <p>
     * 读取输入流中的数据，并将内容追加到 StringBuilder 中，最后将结果记录到日志中
     *
     * @param input 输入流对象，用于读取响应数据
     * @throws IOException 如果读取输入流时发生异常
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
     * 将文件内容写入输出流，并构建包含请求参数的字符串消息
     * <p>
     * 该方法用于处理文件上传请求，将指定的输入流内容写入输出流，并构建包含请求参数的字符串消息，用于后续的HTTP请求体构造。
     *
     * @param requestKey  请求键，用于标识请求参数的名称
     * @param fileName    文件名，用于构造Content-Disposition头信息
     * @param inputStream 文件内容的输入流
     * @param os          输出流，用于写入文件内容和请求参数
     * @return 构建的包含请求参数的字符串消息
     * @throws Exception 如果在写入过程中发生异常
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
        msg.append(requestParams);
        return msg.toString();
    }

    /**
     * 对post参数进行编码处理并写入数据流中
     * <p>
     * 将请求参数按照指定格式编码，并写入给定的输出流中。如果参数为空，则返回"空"的提示信息。
     *
     * @param requestText 请求参数的键值对集合
     * @param os          输出流，用于写入编码后的参数数据
     * @return 编码后的参数信息字符串
     * @throws Exception 如果写入过程中发生异常
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
     * 生成用于上传的临时访问凭证（token）
     * <p>
     * 根据提供的参数生成一个用于对象存储服务的上传token，该token包含用于访问存储服务的临时凭证信息。
     *
     * @param bucket          存储空间名称
     * @param key             对象的唯一标识，可以为 null
     * @param expires         上传token的有效时长，单位为秒，默认为3600秒
     * @param policy          上传策略的其他参数，如设置用户标识、返回体等。bucket和key参数会通过该策略间接设置，deadline通过expires间接设置
     * @param accessKey       临时访问密钥
     * @param secretAccessKey 临时访问密钥对应的密钥
     * @return 生成的上传token字符串
     * @since 1.6.1
     */
    public static String uploadToken(String bucket, String key, long expires, StringMap policy, String accessKey, String secretAccessKey) {
        long deadline = System.currentTimeMillis() / 1000 + expires;
        return uploadTokenWithDeadline(bucket, key, deadline, policy, true, accessKey, secretAccessKey);
    }

    /**
     * 根据指定参数生成带截止时间的上传令牌
     * <p>
     * 该方法用于创建一个包含bucket、key、截止时间、策略等信息的上传令牌，并进行签名处理。
     *
     * @param bucket          上传的bucket名称
     * @param key             上传的文件key
     * @param deadline        截止时间，单位为毫秒
     * @param policy          策略配置，用于上传操作
     * @param strict          是否严格模式，影响策略复制行为
     * @param accessKey       用于签名的Access Key
     * @param secretAccessKey 用于签名的Secret Access Key
     * @return 生成并签名后的上传令牌字符串
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
     * 使用数据和密钥生成签名字符串
     * <p>
     * 将数据进行编码后，使用访问密钥和秘密访问密钥生成签名，并将签名与编码后的数据拼接返回。
     *
     * @param data            数据字节数组
     * @param accessKey       访问密钥
     * @param secretAccessKey 秘密访问密钥
     * @return 签名字符串，格式为 "签名:编码后的数据"
     * @since 1.6.1
     */
    public static String signWithData(byte[] data, String accessKey, String secretAccessKey) {
        String s = UrlSafeBase64.encodeToString(data);
        return sign(StringUtils.utf8Bytes(s), accessKey, secretAccessKey) + ":" + s;
    }

    /**
     * 对数据进行签名操作，生成包含访问密钥和签名的字符串
     * <p>
     * 使用 HMAC-SHA256 算法对数据进行签名，并将访问密钥与签名结果拼接返回
     *
     * @param data            需要签名的数据
     * @param accessKey       访问密钥
     * @param secretAccessKey 秘密访问密钥
     * @return 包含访问密钥和签名的字符串，格式为 "accessKey:signature"
     * @since 1.6.1
     */
    public static String sign(byte[] data, String accessKey, String secretAccessKey) {
        Mac mac = createMac(secretAccessKey);
        String encodedSign = UrlSafeBase64.encodeToString(mac.doFinal(data));
        return accessKey + ":" + encodedSign;
    }

    /**
     * 根据密钥创建HmacSHA1的MAC对象
     * <p>
     * 使用指定的密钥初始化一个HmacSHA1算法的MAC对象，用于生成消息认证码
     *
     * @param secretAccessKey 密钥，用于初始化MAC对象
     * @return 创建并初始化后的MAC对象
     * @throws IllegalArgumentException 如果初始化过程中发生安全异常
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
            throw new IllegalArgumentException(e);
        }
        return mac;
    }

    /**
     * 复制策略配置信息到目标策略对象
     * <p>
     * 该方法用于将源策略对象中的字段复制到目标策略对象中。在复制过程中会检查字段是否为已弃用字段，如果是则抛出异常。如果严格模式开启，则只复制目标策略对象中定义的字段。
     *
     * @param policy       目标策略对象，用于存储复制后的字段
     * @param originPolicy 源策略对象，包含需要复制的字段
     * @param strict       是否启用严格模式，若为true则仅复制目标策略中定义的字段
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

    /** 已弃用的策略字段列表 */
    private static final String[] deprecatedPolicyFields = new String[] {
        "asyncOps",
        };
    /** 用于存储策略配置字段的数组，包含回调地址、文件大小限制、持久化操作等关键参数 */
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
