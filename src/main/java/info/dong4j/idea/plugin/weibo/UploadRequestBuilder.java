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
