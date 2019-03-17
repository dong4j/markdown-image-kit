package info.dong4j.idea.plugin.weibo.http;

import java.util.Map;

/**
 * <p>Description: </p>
 *
 * @author echisan
 * @update dong4j
 * @date 2018 -06-14 22:31
 */
public class DefaultWbpHttpResponse implements WbpHttpResponse {
    private int statusCode;
    private Map<String, String> header;
    private String body;

    /**
     * Instantiates a new Default wbp http response.
     *
     * @param statusCode the status code
     * @param header     the header
     * @param body       the body
     */
    DefaultWbpHttpResponse(int statusCode, Map<String, String> header, String body) {
        this.statusCode = statusCode;
        this.header = header;
        this.body = body;
    }

    @Override
    public int getStatusCode() {
        return this.statusCode;
    }

    @Override
    public Map<String, String> getHeader() {
        return this.header;
    }

    @Override
    public String getBody() {
        return this.body;
    }
}
