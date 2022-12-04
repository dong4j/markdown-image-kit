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

package info.dong4j.idea.plugin.weibo;

import info.dong4j.idea.plugin.util.StringUtils;
import info.dong4j.idea.plugin.weibo.http.DefaultWbpHttpRequest;
import info.dong4j.idea.plugin.weibo.http.WbpHttpRequest;

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
public class UploadRequestBuilder {
    /** Username */
    private String username;
    /** Password */
    private String password;

    /**
     * Sets acount.
     *
     * @param username the username
     * @param password the password
     * @return the acount
     * @since 0.0.1
     */
    public UploadRequestBuilder setAcount(String username, String password) {
        this.username = username;
        this.password = password;
        return this;
    }

    /**
     * Build wbp upload request.
     *
     * @return the wbp upload request
     * @since 0.0.1
     */
    public WbpUploadRequest build() throws Exception {
        WbpHttpRequest request = new DefaultWbpHttpRequest();
        if (StringUtils.isBlank(this.username)) {
            throw new IllegalArgumentException("用户名不能为空!");
        }
        if (StringUtils.isBlank(this.password)) {
            throw new IllegalArgumentException("密码不能为空!");
        }
        return new WbpUploadRequest(request, this.username, this.password);
    }
}
