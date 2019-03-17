package info.dong4j.idea.plugin.weibo.http;

import java.io.*;
import java.util.Map;
import java.util.Set;

/**
 * <p>Description: </p>
 *
 * @author echisan
 * @update dong4j
 * @date 2018 -06-14 22:31
 */
public interface WbpHttpRequest {
    /**
     * Sets header.
     *
     * @param header the header
     */
    void setHeader(Map<String, String> header);

    /**
     * Gets header.
     *
     * @return the header
     */
    Map<String, String> getHeader();

    /**
     * Do get wbp http response.
     *
     * @param url    the url
     * @param header the header
     * @param params the params
     * @return the wbp http response
     * @throws IOException the io exception
     */
    WbpHttpResponse doGet(String url, Map<String, String> header, Map<String, String> params) throws IOException;

    /**
     * Do get wbp http response.
     *
     * @param url    the url
     * @param params the params
     * @return the wbp http response
     * @throws IOException the io exception
     */
    WbpHttpResponse doGet(String url, Map<String, String> params) throws IOException;

    /**
     * Do get wbp http response.
     *
     * @param url the url
     * @return the wbp http response
     * @throws IOException the io exception
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
     */
    WbpHttpResponse doPost(String url, Map<String, String> header, Map<String, String> params) throws IOException;

    /**
     * Do post wbp http response.
     *
     * @param url    the url
     * @param params the params
     * @return the wbp http response
     * @throws IOException the io exception
     */
    WbpHttpResponse doPost(String url, Map<String, String> params) throws IOException;

    /**
     * Do post wbp http response.
     *
     * @param url the url
     * @return the wbp http response
     * @throws IOException the io exception
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
     */
    WbpHttpResponse doPostMultiPart(String url, Map<String, String> header, String content) throws IOException;

    /**
     * Convert params string.
     *
     * @param params the params
     * @return the string
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
