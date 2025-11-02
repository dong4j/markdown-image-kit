package info.dong4j.idea.plugin.client;

import com.google.gson.Gson;

import info.dong4j.idea.plugin.entity.SmmsResult;
import info.dong4j.idea.plugin.util.IOUtils;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import lombok.extern.slf4j.Slf4j;

/**
 * Smms 客户端测试类
 * <p>
 * 用于测试与 Smms 图床服务的交互功能，包括文件上传和图片上传接口的调用。
 * 提供了两种测试方法：一种通过文件路径直接上传，另一种通过输入流上传。
 * 该类主要用于验证 Smms 服务的可用性和接口行为。
 *
 * @author dong4j
 * @version 0.0.1
 * @date 2019.04.02
 * @since 1.1.0
 */
@Slf4j
public class SmmsClientTest {
    /**
     * 测试上传图片功能
     * <p>
     * 测试场景：模拟上传图片到指定接口
     * 预期结果：日志中应记录上传请求的详细信息
     * <p>
     * 注意：测试需要本地存在指定路径的图片文件
     */
    @Test
    public void test1() throws IOException {
        log.info("{}", this.start("https://sm.ms/api/v2/upload", "/Users/dong4j/Downloads/mik.webp"));
    }

    /**
     * 发起 HTTP POST 请求，上传文件并返回响应结果
     * <p>
     * 该方法创建 HTTP POST 请求，将指定路径的文件作为 multipart/form-data 格式上传到指定 URL，
     * 并处理响应结果。若请求成功，解析返回的 JSON 数据并记录日志。
     *
     * @param url      请求的目标 URL
     * @param filePath 要上传的文件的本地路径
     * @return 空字符串（当前实现始终返回空字符串，需根据实际需求调整）
     * @throws IOException 如果请求过程中发生 I/O 错误
     * @since 1.1.0
     */
    @NotNull
    private String start(String url, String filePath) throws IOException {
        HttpPost post = new HttpPost(url);
        File imageFile = new File(filePath);
        FileBody imageFileBody = new FileBody(imageFile);

        HttpEntity reqEntity = MultipartEntityBuilder.create()
            .addPart("smfile", imageFileBody)
            .build();

        post.setEntity(reqEntity);
        // 添加 Authorization header
        post.setHeader("Authorization", "xxx");

        HttpClientBuilder builder = HttpClients.custom();
        // 必须设置 UA, 不然会报 403
        builder.setUserAgent("Mozilla/5.0 (Windows; U; Windows NT 6.1; zh-CN; rv:1.9.2.6)");
        CloseableHttpClient httpClient = builder.build();

        HttpResponse response = httpClient.execute(post);
        byte[] res;
        if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
            res = EntityUtils.toByteArray(response.getEntity());
            String result = IOUtils.toString(res, StandardCharsets.UTF_8.name());
            SmmsResult smmsResult = new Gson().fromJson(result, SmmsResult.class);
            log.info("{}", smmsResult);
        }

        return "";
    }


    @Test
    public void test_2() throws FileNotFoundException {
        log.info("{}", this.upload(new FileInputStream("/Users/dong4j/Downloads/mik.webp"),
                                   "mik.webp"));
    }

    /**
     * 上传文件到 sm.ms 图床服务并返回图片地址
     * <p>
     * 该方法通过 HTTP POST 请求将文件上传至 sm.ms 的 API 接口，若上传成功则返回图片的 URL，否则返回错误信息。
     *
     * @param inputStream 文件输入流，用于读取要上传的文件内容
     * @param filename    文件名，用于标识上传的文件
     * @return 如果上传成功，返回图片的 URL；如果失败，返回错误信息
     * @since 1.1.0
     */
    public String upload(InputStream inputStream, String filename) {
        try {
            HttpClientBuilder builder = HttpClients.custom();
            // 必须设置 UA, 不然会报 403
            builder.setUserAgent("Mozilla/5.0 (Windows; U; Windows NT 6.1; zh-CN; rv:1.9.2.6)");
            CloseableHttpClient client = builder.build();


            HttpEntity reqEntity = MultipartEntityBuilder.create()
                .addBinaryBody("smfile", inputStream, ContentType.DEFAULT_BINARY, filename)
                .build();

            HttpPost post = new HttpPost("https://sm.ms/api/v2/upload");
            post.setEntity(reqEntity);
            // 添加 Authorization header
            post.setHeader("Authorization", "xxx");

            HttpResponse response = client.execute(post);

            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                byte[] res = EntityUtils.toByteArray(response.getEntity());
                String result = IOUtils.toString(res, StandardCharsets.UTF_8.name());
                SmmsResult smmsResult = new Gson().fromJson(result, SmmsResult.class);
                log.trace("{}", smmsResult);
                if (smmsResult.getData() == null) {
                    return smmsResult.getMessage();
                }
                return smmsResult.getData().getUrl();
            }
        } catch (Exception e) {
            log.trace("", e);
        }
        return "";
    }
}
