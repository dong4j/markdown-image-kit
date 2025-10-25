package info.dong4j.idea.plugin.weibo.http;

import java.util.Map;

/**
 * <p>Description: </p>
 *
 * @author echisan
 * @version 0.0.1
 * @email "mailto:dong4j@gmail.com"
 * @date 2018.06.14 22:31
 * @update dong4j
 * @since 0.0.1
 */
public interface WbpHttpResponse {
    /**
     * Gets status code.
     *
     * @return the status code
     * @since 0.0.1
     */
    int getStatusCode();

    /**
     * Gets header.
     *
     * @return the header
     * @since 0.0.1
     */
    Map<String, String> getHeader();

    /**
     * Gets body.
     *
     * @return the body
     * @since 0.0.1
     */
    String getBody();
}
