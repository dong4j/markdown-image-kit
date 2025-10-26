package info.dong4j.idea.plugin.util;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import lombok.Builder;
import lombok.Data;

/**
 * Gitee 工具类
 * <p>
 * 提供与 Gitee 平台相关的操作工具，包括文件上传、HTTP 连接管理等功能。主要用于与 Gitee API 交互，实现代码仓库内容的管理。
 * <p>
 * 该类封装了 Gitee API 的基础地址和文件下载地址，并通过内部类实现与 API 的具体请求逻辑。
 *
 * @author dong4j
 * @version 1.0.0
 * @date 2025.10.24
 * @since 1.4.0
 */
public class GiteeUtils {
    /** GITEE_API 是 Gitee 平台的 API 基础地址 */
    private static final String GITEE_API = "https://gitee.com/api/v5";
    /** 下载资源的 URL 模板，用于构建具体的下载地址 */
    private static final String DOWNLOAD_URL = "https://gitee.com/{owner}/{repos}/raw/{branch}{path}";

    /**
     * 将对象内容上传至对象存储服务
     * <p>
     * 通过指定的仓库、分支和凭证，将输入流中的内容以指定的键存储到对象存储服务中
     *
     * @param key     存储对象的键（Key）
     * @param content 要存储的内容输入流
     * @param repos   仓库路径，格式为 {owner}/{repo}
     * @param branch  仓库分支
     * @param token   认证令牌
     * @throws Exception 上传过程中发生异常时抛出
     * @since 1.3.0
     */
    public static void putObject(String key,
                                 InputStream content,
                                 String repos,
                                 String branch,
                                 String token) throws Exception {

        String url = GITEE_API + "/repos/" + repos + "/contents" + key;
        new GiteeOpenAPI().create(url, content, token, branch);
    }

    /**
     * GiteeOpenAPI 类
     * <p>
     * 用于与 Gitee 平台进行交互的 OpenAPI 实现类，主要提供构建请求和获取 HTTP 连接的接口实现。
     * 该类封装了与 Gitee API 通信所需的请求构建和网络连接操作，支持通过 JSON 格式发送请求数据。
     *
     * @author dong4j
     * @version 1.0.0
     * @date 2021.02.17
     * @since 1.4.0
     */
    private static class GiteeOpenAPI implements OpenAPI {
        /**
         * 构建请求内容
         * <p>
         * 根据传入的分支、内容和令牌信息，构建一个 Gitee 请求的 JSON 字符串。
         *
         * @param branch  分支名称
         * @param content 内容数据
         * @param token   认证令牌
         * @return 构建后的 JSON 字符串
         * @since 1.4.0
         */
        @Override
        public String buildRequest(String branch, String content, String token) {
            return new Gson().toJson((GiteeRequest.builder()
                .message("markdown-image-kit uploaded")
                .branch(branch)
                .content(content)
                .token(token)
                .build()));
        }

        /**
         * 创建并配置 HTTP URL 连接对象
         * <p>
         * 根据提供的 URL 和 Token 创建 HTTP 连接，并设置连接超时、读取超时、请求方法和请求头信息
         *
         * @param url   要请求的 URL 地址
         * @param token 请求时使用的 Token
         * @return 配置完成的 HTTP URL 连接对象
         * @throws IOException 如果 URL 无效或连接过程中发生异常
         * @since 1.4.0
         */
        @Override
        public HttpURLConnection getHttpURLConnection(String url, String token) throws IOException {
            URL realUrl = new URL(url);
            HttpURLConnection connection = (HttpURLConnection) realUrl.openConnection();
            connection.setConnectTimeout(3000);
            connection.setReadTimeout(5000);
            // 设置
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
            connection.setRequestProperty("User-Agent", "markdown-image-kit");
            return connection;
        }
    }

    /**
     * Gitee请求类
     * <p>
     * 用于封装向 Gitee 平台发送请求所需的数据参数，包括提交信息、分支、内容、SHA 值和访问令牌等信息。
     * 该类使用 Lombok 的 @Data 和 @Builder 注解简化了属性的定义和对象的构建过程。
     *
     * @author dong4j
     * @version 1.0.0
     * @email "mailto:dong4j@fkhwl.com"
     * @date 2021.02.17
     * @since 1.4.0
     */
    @Data
    @Builder
    private static class GiteeRequest {
        /** 消息内容 */
        private String message;
        /** 分支名称 */
        private String branch;
        /** 内容信息 */
        private String content;
        /** SHA 值 */
        private String sha;
        /** 访问令牌，用于身份验证和授权 */
        @SerializedName(value = "access_token")
        private String token;
    }


}
