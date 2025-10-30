package info.dong4j.idea.plugin.util;


import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import com.intellij.openapi.util.io.FileUtil;

import org.junit.Test;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import lombok.Builder;
import lombok.Data;

/**
 * Gitee 工具类测试类
 * <p>
 * 用于测试 Gitee 平台的文件上传功能，通过调用 Gitee API 实现文件的创建和上传操作。
 * 该类包含一个测试方法和一个用于构建请求的静态方法，支持通过指定 URL、文件路径和 token 进行文件上传。
 * 注意：由于未处理文件的 SHA 值，当前仅支持新建文件，不支持更新已有文件。
 *
 * @author dong4j
 * @version 1.0.0
 * @date 2020.04.25
 * @since 1.1.0
 */
public class GiteeUtilsTest {

    /** Gitee API 基础地址 */
    private static final String GITEE_API = "https://gitee.com/api/v5";
    /** 下载资源的固定 URL 模板，用于构建具体的下载地址 */
    private static final String DOWNLOAD_URL = "https://gitee.com/{owner}/{repos}/raw/{branch}{path}";
    /** 用于存储从系统属性中获取的 token 值 */
    private static final String token = System.getProperty("token");
    /** 仓库名称，用于标识测试环境的 Markdown 图片插件仓库 */
    private static final String repos = "markdown-image-kit-test";
    /** 系统所有者标识，用于标识系统或模块的拥有者 */
    private static final String owner = "dong4j";
    /** 图片路径，用于指定图片资源的位置 */
    private static final String path = "/xx.png";

    /**
     * 测试创建文件内容的功能
     * <p>
     * 测试场景：向指定仓库的路径上传文件
     * 预期结果：应返回创建操作的成功状态
     * <p>
     * 该测试需要本地存在指定路径的文件（如 /Users/dong4j/Downloads/mik.webp），并确保 token 有效
     */
    @Test
    public void test() throws Exception {
        // https://gitee.com/api/v5/repos/{owner}/{repo}/contents/{path}
        String url = GITEE_API + "/repos/" + owner + "/" + repos + "/contents" + path;
        File file = new File("/Users/dong4j/Downloads/mik.webp");
        boolean result = create(url, file, token);
        System.out.println(result);
    }

    /**
     * 向指定的 GitHub 仓库路径创建文件
     * <p>
     * 该方法用于通过 HTTP POST 请求向 GitHub 仓库的指定路径创建文件。由于未考虑文件的 SHA 值，因此无法更新已有文件，只能新建文件。
     * <p>
     * 创建文件时，会将文件内容进行 Base64 编码，并构建包含文件内容、消息、分支和鉴权信息的 JSON 请求体发送至指定 URL。
     * 根据响应码判断操作是否成功，若响应码为 200 或 201 则返回 true，若为 422 则也返回 true，若为 400 则抛出运行时异常提示文件可能已存在。
     *
     * @param url   GitHub API 的文件创建 URL，格式应为 <a href="https://api.github.com/repos/:owner/:repo/contents/:path">...</a>
     * @param file  要上传的文件对象，需确保文件存在
     * @param token GitHub 鉴权 Token，用于身份验证
     * @return 如果文件创建成功返回 true，若响应码为 422 也返回 true，否则抛出异常
     * @throws Exception 如果请求过程中发生异常，如网络问题或响应处理错误
     */
    public static boolean create(String url, File file, String token) throws Exception {
        URL realUrl = new URI(url).toURL();
        HttpURLConnection connection = (HttpURLConnection) realUrl.openConnection();
        connection.setConnectTimeout(120000);
        connection.setReadTimeout(120000);
        // 设置
        connection.setDoOutput(true); // 需要输出
        connection.setDoInput(true); // 需要输入
        connection.setUseCaches(false); // 不允许缓存
        connection.setRequestMethod("POST"); // 设置 PUT 方式连接
        connection.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
        connection.setRequestProperty("User-Agent", "markdown-image-kit");
        connection.connect();

        StringBuffer sbuffer;
        try (DataOutputStream dos = new DataOutputStream(connection.getOutputStream())) {
            String content = Base64Utils.encodeToString(FileUtil.loadFileBytes(file));
            GithubRequest request = GithubRequest.builder()
                .message("markdown-image-kit uploaded")
                .branch("master")
                .content(content)
                .token(token)
                .build();

            dos.writeBytes(new Gson().toJson(request));

            int responseCode = connection.getResponseCode();
            // 读取响应
            if (responseCode == 200 || responseCode == 201) {
                try (InputStreamReader inputStream = new InputStreamReader(connection.getInputStream());
                     BufferedReader reader = new BufferedReader(inputStream)) {
                    String lines;
                    sbuffer = new StringBuffer();

                    while ((lines = reader.readLine()) != null) {
                        lines = new String(lines.getBytes(), StandardCharsets.UTF_8);
                        sbuffer.append(lines);
                    }
                    System.out.println(sbuffer);
                }
                return true;
            } else if (responseCode == 422) {
                return true;
            } else if (responseCode == 400) {
                throw new RuntimeException(responseCode + " " + connection.getResponseMessage()
                                           + " :" + path + " 可能已存在");
            } else {
                throw new RuntimeException(responseCode + " " + connection.getResponseMessage());
            }
        } finally {
            // 断开连接
            connection.disconnect();
        }
    }

    /**
     * GitHub 请求数据类
     * <p>
     * 用于封装向 GitHub API 发送请求所需的数据参数，包括提交信息、分支名称、内容、SHA 值和访问令牌等信息。
     * 该类使用 Lombok 的 @Data 注解自动生成 Getter、Setter、toString 等方法，使用 @Builder 注解提供构建器模式以方便对象创建。
     *
     * @author 作者名
     * @version 1.0.0
     * @date 2025.10.24
     * @since 1.0.0
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
        /** AccessToken 值 */
        @SerializedName(value = "access_token")
        private String token;
    }

}
