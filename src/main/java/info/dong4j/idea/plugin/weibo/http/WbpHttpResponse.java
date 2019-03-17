package info.dong4j.idea.plugin.weibo.http;

import java.util.Map;

/**
 * <p>Description: </p>
 *
 * @author echisan
 * @update dong4j
 * @date 2018 -06-14 22:31
 */
public interface WbpHttpResponse {
    /**
     * Gets status code.
     *
     * @return the status code
     */
    int getStatusCode();

    /**
     * Gets header.
     *
     * @return the header
     */
    Map<String, String> getHeader();

    /**
     * Gets body.
     *
     * @return the body
     */
    String getBody();
}
