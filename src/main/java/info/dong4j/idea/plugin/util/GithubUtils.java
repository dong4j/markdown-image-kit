package info.dong4j.idea.plugin.util;

import com.google.gson.Gson;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;

import lombok.Builder;
import lombok.Data;

/**
 * GitHub 工具类
 * <p>
 * 提供与 GitHub API 相关的工具方法，用于操作仓库内容，如上传文件到指定分支。
 * 该类封装了 GitHub API 的请求构建和网络连接处理逻辑，简化了与 GitHub 交互的流程。
 * </p>
 *
 * @author dong4j
 * @version 1.0.0
 * @date 2025.10.24
 * @since 1.3.0
 */
public class GithubUtils {
    /** GitHub API 的基础地址 */
    private static final String GITHUB_API = "https://api.github.com";
    /** 下载资源的 URL 地址，用于从 GitHub 获取文件 */
    private static final String DOWNLOAD_URL = "https://raw.githubusercontent.com/{owner}/{repos}/{branch}{path}";

    /**
     * 将对象上传到 GitHub 的 OSS 存储中
     * <p>
     * 该方法用于将指定的输入流内容以指定的 key 存储到 GitHub 的指定仓库和分支下。
     *
     * @param key     要存储的对象的键（Key）
     * @param content 要存储的对象内容，以 InputStream 形式提供
     * @param repos   仓库路径，格式为 {owner}/{repo}
     * @param branch  存储的目标分支名称
     * @param token   GitHub 认证的 token
     * @throws Exception 如果上传过程中发生异常
     * @since 1.3.0
     */
    public static void putObject(String key,
                                 InputStream content,
                                 String repos,
                                 String branch,
                                 String token) throws Exception {

        String url = GITHUB_API + "/repos/" + repos + "/contents" + key;
        new GithubOpenAPI().create(url, content, token, branch);
    }

    /**
     * GitHub 开放 API 工具类
     * <p>
     * 用于构建 GitHub API 请求内容以及获取 HTTP 连接对象，支持与 GitHub 服务进行交互。
     * 提供了构建 JSON 请求体和设置 HTTP 请求头的通用方法，适用于需要调用 GitHub API 的场景。
     * </p>
     *
     * @author dong4j
     * @version 1.0.0
     * @date 2021.02.17
     * @since 1.4.0
     */
    private static class GithubOpenAPI implements OpenAPI {
        /**
         * 构建请求内容
         * <p>
         * 根据传入的分支、内容和令牌信息，构建并返回一个 JSON 格式的请求字符串。
         *
         * @param branch  分支名称
         * @param content 内容信息
         * @param token   认证令牌
         * @return 构建后的 JSON 请求字符串
         * @since 1.4.0
         */
        @Override
        public String buildRequest(String branch, String content, String token) {
            return new Gson().toJson(GithubRequest.builder()
                                         .message("markdown-image-kit uploaded")
                                         .branch(branch)
                                         .content(content)
                                         .build());
        }

        /**
         * 获取 HTTP URL 连接
         * <p>
         * 根据提供的 URL 和 token 创建并配置 HTTP URL 连接对象，设置超时时间、请求方法和请求头信息。
         *
         * @param url   请求的 URL 地址
         * @param token 认证使用的 token
         * @return 配置好的 HTTP URL 连接对象
         * @throws IOException 如果 URL 连接过程中发生 I/O 异常
         * @since 1.4.0
         */
        @Override
        public HttpURLConnection getHttpURLConnection(String url, String token) throws IOException {
            HttpURLConnection connection = OssUtils.connect(url, "PUT");
            connection.setRequestProperty("Content-Type", "application/vnd.github.v3+json");
            connection.setRequestProperty("Authorization", "token " + token);
            connection.setRequestProperty("User-Agent", "markdown-image-kit");
            return connection;
        }
    }

    /**
     * GitHub 请求类
     * <p>
     * 用于封装向 GitHub 发起请求时所需的参数信息，包括提交信息、分支名称、内容和 SHA 值等。
     * 该类使用 Lombok 的 @Data 和 @Builder 注解简化了属性的定义和对象的构建过程。
     *
     * @author dong4j
     * @version 1.0.0
     * @email mailto:dong4j@gmail.com
     * @date 2021.02.16
     * @since 1.3.0
     */
    @Data
    @Builder
    private static class GithubRequest {
        /** 消息内容 */
        private String message;
        /** 分支名称 */
        private String branch;
        /** 内容文本 */
        private String content;
        /** SHA 值 */
        private String sha;
    }

}
