package info.dong4j.idea.plugin.weibo;

import info.dong4j.idea.plugin.weibo.http.DefaultWbpHttpRequest;
import info.dong4j.idea.plugin.weibo.http.WbpHttpRequest;

import org.apache.commons.lang.StringUtils;

/**
 * <p>Description: </p>
 *
 * @author echisan
 * @update dong4j
 * @date 2018 -06-14 22:31
 */
public class UploadRequestBuilder {
    private String username;
    private String password;
    private long tryLoginTime = 0;

    /**
     * Sets acount.
     *
     * @param username the username
     * @param password the password
     * @return the acount
     */
    public UploadRequestBuilder setAcount(String username, String password) {
        this.username = username;
        this.password = password;
        return this;
    }

    /**
     * Sets try login time.
     *
     * @param time the time
     * @return the try login time
     */
    public UploadRequestBuilder setTryLoginTime(long time) {
        this.tryLoginTime = time;
        return this;
    }

    /**
     * Build wbp upload request.
     *
     * @return the wbp upload request
     */
    public WbpUploadRequest build() {
        WbpHttpRequest request = new DefaultWbpHttpRequest();
        if (StringUtils.isBlank(username)) {
            throw new IllegalArgumentException("用户名不能为空!");
        }
        if (StringUtils.isBlank(password)) {
            throw new IllegalArgumentException("密码不能为空!");
        }
        return new WbpUploadRequest(request, this.username, this.password);
    }
}
