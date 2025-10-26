package info.dong4j.idea.plugin.util;

import com.intellij.openapi.util.io.FileUtil;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;

/**
 * OpenAPI 接口
 * <p>
 * 提供与开放 API 相关的通用操作方法，主要用于创建资源。该接口定义了创建资源的核心流程，包括建立 HTTP 连接、发送请求数据、处理响应结果等。同时包含构建请求内容和获取 HTTP 连接的方法，支持通过 Base64 编码上传文件流。
 * <p>
 * 实现该接口的类需要提供具体的网络请求实现，包括连接配置、数据传输和异常处理逻辑。
 *
 * @author dong4j
 * @version 1.0.0
 * @date 2021.02.17
 * @since 1.4.0
 */
public interface OpenAPI {
    /**
     * 创建资源
     * <p>
     * 向指定URL发送创建请求，上传文件流，并根据响应码判断操作是否成功。
     *
     * @param url        请求的URL地址
     * @param fileStream 要上传的文件流
     * @param token      认证令牌
     * @param branch     分支名称
     * @return 操作是否成功
     * @throws Exception 发生异常时抛出
     * @since 1.4.0
     */
    default boolean create(String url,
                           InputStream fileStream,
                           String token,
                           String branch) throws Exception {
        HttpURLConnection connection = this.getHttpURLConnection(url, token);
        connection.setDoOutput(true);
        connection.setUseCaches(false);
        connection.connect();

        try (DataOutputStream dos = new DataOutputStream(connection.getOutputStream())) {
            String content = Base64Utils.encodeToString(FileUtil.adaptiveLoadBytes(fileStream));

            dos.writeBytes(this.buildRequest(branch, content, token));

            int responseCode = connection.getResponseCode();
            // 读取响应
            if (responseCode == 200 || responseCode == 201) {
                return true;
            } else if (responseCode == 422 || responseCode == 400) {
                // 已存在相同文件, 这里直接返回
                return true;
            } else if (responseCode == 404) {
                throw new RuntimeException(responseCode + " " + connection.getResponseMessage()
                                           + " :The branch (" + branch + ") may not be created");
            } else {
                throw new RuntimeException(responseCode + " " + connection.getResponseMessage());
            }
        } finally {
            // 断开连接
            connection.disconnect();
        }
    }

    /**
     * 构建请求字符串
     * <p>
     * 根据提供的分支、内容和令牌信息，构建相应的请求字符串。
     *
     * @param branch  分支名称
     * @param content 内容信息
     * @param token   认证令牌
     * @return 构建完成的请求字符串
     * @since 1.4.0
     */
    String buildRequest(String branch, String content, String token);

    /**
     * 获取HTTP URL连接
     * <p>
     * 根据指定的URL和Token创建并返回一个HTTP URL连接对象
     *
     * @param url   要访问的URL地址
     * @param token 认证使用的Token
     * @return HTTP URL连接对象
     * @throws IOException 如果连接过程中发生IO异常
     * @since 1.4.0
     */
    HttpURLConnection getHttpURLConnection(String url, String token) throws IOException;
}
