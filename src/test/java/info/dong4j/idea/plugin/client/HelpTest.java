package info.dong4j.idea.plugin.client;

import info.dong4j.idea.plugin.util.IOUtils;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.junit.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import lombok.extern.slf4j.Slf4j;

/**
 * 帮助测试类
 * <p>
 * 该类主要用于测试与帮助相关的 HTTP 请求功能，提供了一个帮助方法用于发送 HTTP POST 请求，并包含一个测试方法用于验证功能。
 * <p>
 * 主要功能包括构建 HTTP 客户端、发送请求、处理响应以及关闭资源。
 *
 * @author dong4j
 * @version 1.0.0
 * @date 2021.02.14
 * @since 1.0.0
 */
@Slf4j
public class HelpTest {
    /**
     * 根据指定路径发送帮助请求并返回 HTTP 客户端
     * <p>
     * 该方法创建并配置一个 HTTP 客户端，发送 POST 请求到指定路径，处理响应并关闭客户端。
     *
     * @param where 请求的路径参数
     * @return 配置好的 HTTP 客户端
     * @throws Exception 发生异常时抛出
     * @since 1.1.0
     */
    private CloseableHttpClient help(String where) throws Exception {

        HttpClientBuilder builder = HttpClients.custom();
        // 必须设置 UA, 不然会报 403
        builder.setUserAgent("Mozilla/5.0 (Windows; U; Windows NT 6.1; zh-CN; rv:1.9.2.6)");
        CloseableHttpClient client = builder.build();

        HttpPost httpPost = new HttpPost("http://127.0.0.1:8080/rest/help/" + where);

        try {
            HttpResponse response = client.execute(httpPost);

            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                byte[] res = EntityUtils.toByteArray(response.getEntity());
                String result = IOUtils.toString(res, StandardCharsets.UTF_8.name());
                log.debug("{}", result);
            }

        } catch (IOException e) {
            log.debug("", e);
        } finally {
            try {
                client.close();
            } catch (IOException ignored) {
            }
        }
        return client;
    }

    /**
     * 测试 help 方法在设置模式下的异常处理逻辑
     * <p>
     * 测试场景：调用 help 方法并传入 "setting" 参数
     * 预期结果：方法应捕获并打印异常堆栈信息
     * <p>
     * 注意：该测试需要确保 help 方法内部会抛出异常以验证异常处理逻辑
     */
    @Test
    public void test() {
        try {
            this.help("setting");
        } catch (Exception ignored) {
        }
    }
}
