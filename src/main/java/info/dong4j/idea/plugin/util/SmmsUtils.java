package info.dong4j.idea.plugin.util;

import com.google.gson.Gson;

import info.dong4j.idea.plugin.entity.SmmsResult;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import lombok.extern.slf4j.Slf4j;

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
@Slf4j
public class SmmsUtils {

    /**
     * 将对象上传到 GitHub 的 OSS 存储中
     * <p>
     * 该方法用于将指定的输入流内容以指定的 key 存储到 GitHub 的指定仓库和分支下。
     *
     * @param key     要存储的对象的键（Key）
     * @param content 要存储的对象内容，以 InputStream 形式提供
     * @param token   GitHub 认证的 token
     * @throws Exception 如果上传过程中发生异常
     * @since 1.3.0
     */
    public static String putObject(String api,
                                   String token,
                                   String key,
                                   InputStream content) throws Exception {


        HttpClientBuilder builder = HttpClients.custom();
        // 必须设置 UA, 不然会报 403
        builder.setUserAgent("Mozilla/5.0 (Windows; U; Windows NT 6.1; zh-CN; rv:1.9.2.6)");

        try (CloseableHttpClient client = builder.build()) {
            HttpEntity reqEntity = MultipartEntityBuilder.create()
                .addBinaryBody("smfile", content, ContentType.DEFAULT_BINARY, key)
                .build();

            HttpPost post = new HttpPost(api);
            post.setEntity(reqEntity);
            post.setHeader("Authorization", token);

            HttpResponse response = client.execute(post);

            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                byte[] res = EntityUtils.toByteArray(response.getEntity());
                String result = IOUtils.toString(res, StandardCharsets.UTF_8.name());
                SmmsResult smmsResult = new Gson().fromJson(result, SmmsResult.class);
                log.trace("{}", smmsResult);
                if (smmsResult.getCode().equals("image_repeated")) {
                    return smmsResult.getImages();
                }
                if (smmsResult.getData() == null) {
                    throw new RuntimeException(smmsResult.getCode() + " " + smmsResult.getMessage());
                }
                return smmsResult.getData().getUrl();
            } else {
                throw new RuntimeException(response.getStatusLine().getStatusCode() + " " + response.getStatusLine().getReasonPhrase());
            }
        }
    }

}
