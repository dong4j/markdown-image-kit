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
public class DefaultWbpHttpResponse implements WbpHttpResponse {
    /** Status code */
    private final int statusCode;
    /** Header */
    private final Map<String, String> header;
    /** Body */
    private final String body;

    /**
     * Instantiates a new Default wbp http response.
     *
     * @param statusCode the status code
     * @param header     the header
     * @param body       the body
     * @since 0.0.1
     */
    DefaultWbpHttpResponse(int statusCode, Map<String, String> header, String body) {
        this.statusCode = statusCode;
        this.header = header;
        this.body = body;
    }

    /**
     * Gets status code *
     *
     * @return the status code
     * @since 0.0.1
     */
    @Override
    public int getStatusCode() {
        return this.statusCode;
    }

    /**
     * Gets header *
     *
     * @return the header
     * @since 0.0.1
     */
    @Override
    public Map<String, String> getHeader() {
        return this.header;
    }

    /**
     * Gets body *
     *
     * @return the body
     * @since 0.0.1
     */
    @Override
    public String getBody() {
        return this.body;
    }
}
