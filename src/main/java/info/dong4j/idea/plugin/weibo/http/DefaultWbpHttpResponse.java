/*
 * MIT License
 *
 * Copyright (c) 2022 dong4j <dong4j@gmail.com>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

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
