package info.dong4j.idea.plugin.weibo;

import org.junit.Test;

import java.io.File;
import java.io.IOException;

/**
 * <p>Description: ${description}</p>
 *
 * @author dong4j
 * @version 1.0.0
 * @email "mailto:dong4j@gmail.com"
 * @date 2019.03.17 22:42
 * @since 1.1.0
 */
public class WbpUploadRequestTest {

    /**
     * Build
     *
     * @throws IOException io exception
     * @since 1.1.0
     */
    @Test
    public void build() throws Exception {
        WbpUploadRequest request = new UploadRequestBuilder()
            .setAcount(System.getProperty("username"), System.getProperty("password"))
            .build();
        UploadResponse response = request.upload(new File("/Users/dong4j/Downloads/NewInstance.png"));
        System.out.println(response.getResult());
        System.out.println(response.getMessage());
        System.out.println(response.getImageInfo());
        System.out.println(response.getImageInfo().getLarge());
    }
}
