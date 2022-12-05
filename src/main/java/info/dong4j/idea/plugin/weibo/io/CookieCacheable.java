package info.dong4j.idea.plugin.weibo.io;

import java.io.IOException;

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
public interface CookieCacheable {

    /**
     * Save cookie.
     *
     * @param cookie the cookie
     * @throws IOException the io exception
     * @since 0.0.1
     */
    void saveCookie(String cookie) throws IOException;

    /**
     * Read cookie string.
     *
     * @return the string
     * @throws IOException the io exception
     * @since 0.0.1
     */
    String readCookie() throws IOException;

    /**
     * Delete cookie.
     *
     * @since 0.0.1
     */
    void deleteCookie();

}
