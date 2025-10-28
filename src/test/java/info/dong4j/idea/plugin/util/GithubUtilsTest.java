package info.dong4j.idea.plugin.util;


import com.google.gson.Gson;

import com.intellij.openapi.util.io.FileUtil;

import org.junit.Test;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.Builder;
import lombok.Data;

/**
 * GitHub 工具类测试
 * <p>
 * 该类主要用于测试 GitHub 文件上传和更新功能，包括通过 API 创建或更新文件内容的操作。
 * <p>
 * 由于未考虑文件的 SHA 值，当前只能实现新建文件的功能，若需更新文件，需先获取 SHA 值再进行 PUT 请求。
 * <p>
 * 包含测试方法 test()，用于模拟文件上传流程，并通过 create() 和 update() 方法实现具体的上传和更新逻辑。
 * <p>
 * 使用了 Builder 模式来构建 GitHubRequest 对象，便于配置上传参数。
 *
 * @author dong4j
 * @version 1.0.0
 * @date 2025.10.24
 * @since 1.1.0
 */
public class GithubUtilsTest {
    /** GitHub API 基础地址 */
    private static final String GITHUB_API = "https://api.github.com";
    /** 下载资源的 URL 模板，用于构建 GitHub 上的文件下载地址 */
    private static final String DOWNLOAD_URL = "https://raw.githubusercontent.com/{owner}/{repos}/{branch}{path}";
    /** token 值，从系统属性中获取 */
    private static final String token = System.getProperty("token");
    /** 仓库名称，用于标识测试环境的仓库 */
    private static final String repos = "markdown-image-kit-test";
    /** 所有者标识，固定为 "dong4j" */
    private static final String owner = "dong4j";
    /** 资源文件路径，指向图片文件 xu.png */
    private static final String path = "/xu.png";

    /**
     * 测试创建或更新文件内容功能
     * <p>
     * 测试场景：向 GitHub 仓库的指定路径上传文件
     * 预期结果：应返回操作是否成功的布尔值
     * <p>
     * 说明：该测试需要本地存在指定路径的文件（如 /Users/dong4j/Downloads/xu.png），并确保 GitHub API 地址和认证 token 正确配置
     */
    @Test
    public void test() throws Exception {
        String url = GITHUB_API + "/repos/" + owner + "/" + repos + "/contents" + path;
        File file = new File("/Users/dong4j/Downloads/xu.png");
        boolean result = create(url, file, token);
        System.out.println(result);
    }

    /**
     * 向指定的 GitHub 仓库路径上传文件
     * <p>
     * 该方法用于通过 PUT 请求将文件内容上传到 GitHub 仓库的指定路径。由于未考虑文件的 SHA 值，因此无法更新已有文件，只能新建文件。
     * 如果上传失败并返回 422 状态码，则尝试调用更新方法。其他异常情况则抛出运行时异常。
     *
     * @param url   GitHub API 的文件上传地址，格式为 <a href="https://api.github.com/repos/:owner/:repo/contents/:path">...</a>
     * @param file  要上传的文件对象，需确保文件存在
     * @param token GitHub 鉴权 token，用于验证身份
     * @return 上传操作是否成功，返回 true 表示成功，false 表示失败
     * @throws Exception 如果发生网络或 I/O 异常
     */
    public static boolean create(String url, File file, String token) throws Exception {
        URL realUrl = new URL(url);
        HttpURLConnection connection = (HttpURLConnection) realUrl.openConnection();
        connection.setConnectTimeout(120000);
        connection.setReadTimeout(120000);
        // 设置
        connection.setDoOutput(true); // 需要输出
        connection.setDoInput(true); // 需要输入
        connection.setUseCaches(false); // 不允许缓存
        connection.setRequestMethod("PUT"); // 设置 PUT 方式连接

        connection.setRequestProperty("Content-Type", "application/vnd.github.v3+json");
        connection.setRequestProperty("Authorization", "token " + token);
        connection.setRequestProperty("User-Agent", "markdown-image-kit");
        connection.connect();

        StringBuffer sbuffer;
        try (DataOutputStream dos = new DataOutputStream(connection.getOutputStream())) {
            String content = Base64Utils.encodeToString(FileUtil.loadFileBytes(file));
            GithubRequest request = GithubRequest.builder()
                .message("markdown-image-kit uploaded")
                .branch("main")
                .content(content)
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
            } else if (responseCode == 422) {
                update(url, file, token);
            } else {
                throw new RuntimeException(responseCode + " " + connection.getResponseMessage());
            }
        } finally {
            // 断开连接
            connection.disconnect();
        }

        return true;
    }

    /**
     * 向指定URL上传文件，用于更新GitHub仓库中的文件内容
     * <p>
     * 该方法首先通过GET请求获取目标文件的SHA值，然后使用PUT请求上传新文件内容。
     * 上传过程中会记录并输出耗时信息。
     *
     * @param url   GitHub API的文件更新接口地址，格式为 <a href="https://api.github.com/repos/:owner/:repo/contents/:path">...</a>
     * @param file  需要上传的文件对象，需确保文件已存在
     * @param token GitHub API鉴权使用的访问令牌
     * @return 上传操作是否成功，true表示成功，false表示失败
     */
    public static boolean update(String url, File file, String token) {
        long begin = System.currentTimeMillis();
        System.out.println("获取文件SHA...");
        String sha = getSHA(url);
        if (sha == null) {
            return false;
        }
        long end = System.currentTimeMillis();
        System.out.printf("获取文件SHA 耗时 %ds\n", (end - begin) / 1000);
        System.out.println("上传开始...");
        //StringBuffer result = new StringBuffer();
        BufferedReader in = null;
        HttpURLConnection conn;
        try {
            URL realUrl = new URL(url);
            conn = (HttpURLConnection) realUrl.openConnection();
            conn.setConnectTimeout(120000);
            conn.setReadTimeout(120000);
            // 设置
            conn.setDoOutput(true); // 需要输出
            conn.setDoInput(true); // 需要输入
            conn.setUseCaches(false); // 不允许缓存
            conn.setRequestMethod("PUT"); // 设置PUT方式连接

            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Authorization", "token " + token);
            conn.setRequestProperty("User-Agent", "Github File Uploader App");
            conn.connect();
            // 传输数据
            DataOutputStream dos = new DataOutputStream(conn.getOutputStream());
            // 传输json头部
            dos.writeBytes("{\"message\":\".\",\"sha\":\"" + sha + "\",\"content\":\"");
            // 传输文件内容
            byte[] buffer = new byte[1024 * 1002]; // 3的倍数
            RandomAccessFile raf = new RandomAccessFile(file, "r");
            long size = raf.read(buffer);
            while (size > -1) {
                if (size == buffer.length) {
                    dos.write(Base64.getEncoder().encode(buffer));
                } else {
                    byte[] tmp = new byte[(int) size];
                    System.arraycopy(buffer, 0, tmp, 0, (int) size);
                    dos.write(Base64.getEncoder().encode(tmp));
                }
                size = raf.read(buffer);
            }
            raf.close();
            // 传输json尾部
            dos.writeBytes("\"}");
            dos.flush();
            dos.close();

            in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            while (in.readLine() != null) {
                //result.append(line).append("\n");
            }
        } catch (Exception e) {
            System.out.println("发送PUT请求出现异常！");
            return false;
        } finally {
            try {
                in.close();
            } catch (Exception ignored) {
            }
        }
        end = System.currentTimeMillis();
        System.out.printf("上传结束，耗时 %ds\n", (end - begin) / 1000);
        //result.toString()
        return true;
    }

    /** 匹配 URL 对应的 SHA 值的正则表达式模式 */
    static Pattern pattern = Pattern.compile("\"sha\": *\"([^\"]+)\"");

    /**
     * 从指定URL获取SHA值
     * <p>
     * 通过发送GET请求获取指定URL的内容，并使用正则表达式提取其中的SHA值。
     *
     * @param url 请求的URL地址
     * @return 提取到的SHA值，若未找到或发生异常则返回null
     */
    public static String getSHA(String url) {
        StringBuilder result = new StringBuilder();
        BufferedReader in = null;
        HttpURLConnection conn;
        try {
            URL realUrl = new URL(url);
            conn = (HttpURLConnection) realUrl.openConnection();
            conn.setConnectTimeout(120000);
            conn.setReadTimeout(120000);
            conn.setRequestMethod("GET");
            conn.setRequestProperty("User-Agent", "Github File Uploader App");
            conn.connect();
            in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line;
            while ((line = in.readLine()) != null) {
                result.append(line).append("\n");
            }
            Matcher matcher = pattern.matcher(result.toString());
            if (matcher.find()) {
                return matcher.group(1);
            }
        } catch (Exception e) {
            System.out.println("请求SHA出现异常！");
        } finally {
            try {
                in.close();
            } catch (Exception ignored) {
            }
        }
        return null;
    }

    /**
     * GitHub 请求数据类
     * <p>
     * 用于封装向 GitHub 发起请求时所需的数据信息，包括提交信息、分支名称、内容和提交哈希值等字段。
     *
     * @author 未知
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
        /** 内容字段，用于存储主要文本或数据内容 */
        private String content;
        /** SHA 值 */
        private String sha;
    }

}
