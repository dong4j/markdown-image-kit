package info.dong4j.idea.plugin.weibo.http;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

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
public interface WbpHttpRequest {
    /**
     * Gets header.
     *
     * @return the header
     * @since 0.0.1
     */
    Map<String, String> getHeader();

    /**
     * Sets header.
     *
     * @param header the header
     * @since 0.0.1
     */
    void setHeader(Map<String, String> header);

    /**
     * Do get wbp http response.
     *
     * @param url    the url
     * @param header the header
     * @param params the params
     * @return the wbp http response
     * @throws IOException the io exception
     * @since 0.0.1
     */
    WbpHttpResponse doGet(String url, Map<String, String> header, Map<String, String> params) throws IOException;

    /**
     * Do get wbp http response.
     *
     * @param url    the url
     * @param params the params
     * @return the wbp http response
     * @throws IOException the io exception
     * @since 0.0.1
     */
    WbpHttpResponse doGet(String url, Map<String, String> params) throws IOException;

    /**
     * Do get wbp http response.
     *
     * @param url the url
     * @return the wbp http response
     * @throws IOException the io exception
     * @since 0.0.1
     */
    WbpHttpResponse doGet(String url) throws IOException;

    /**
     * Do post wbp http response.
     *
     * @param url    the url
     * @param header the header
     * @param params the params
     * @return the wbp http response
     * @throws IOException the io exception
     * @since 0.0.1
     */
    WbpHttpResponse doPost(String url, Map<String, String> header, Map<String, String> params) throws IOException;

    /**
     * Do post wbp http response.
     *
     * @param url    the url
     * @param params the params
     * @return the wbp http response
     * @throws IOException the io exception
     * @since 0.0.1
     */
    WbpHttpResponse doPost(String url, Map<String, String> params) throws IOException;

    /**
     * Do post wbp http response.
     *
     * @param url the url
     * @return the wbp http response
     * @throws IOException the io exception
     * @since 0.0.1
     */
    WbpHttpResponse doPost(String url) throws IOException;

    /**
     * Do post multi part wbp http response.
     *
     * @param url     the url
     * @param header  the header
     * @param content the content
     * @return the wbp http response
     * @throws IOException the io exception
     * @since 0.0.1
     */
    WbpHttpResponse doPostMultiPart(String url, Map<String, String> header, String content) throws IOException;

    /**
     * Convert params string.
     *
     * @param params the params
     * @return the string
     * @since 0.0.1
     */
    default String convertParams(Map<String, String> params) {
        Set<Map.Entry<String, String>> entries = params.entrySet();
        StringBuilder sb = new StringBuilder();
        entries.forEach(e -> {
            sb.append(e.getKey());
            sb.append("=");
            sb.append(e.getValue());
            sb.append("&");
        });
        String s = sb.toString();
        return s.substring(0, s.length() - 1);
    }
}
