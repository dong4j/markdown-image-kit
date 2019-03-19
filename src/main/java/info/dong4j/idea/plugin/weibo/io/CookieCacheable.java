package info.dong4j.idea.plugin.weibo.io;

import java.io.*;

/**
 * <p>Description: </p>
 *
 * @author echisan
 * @update dong4j
 * @date 2018 -06-14 22:31
 */
public interface CookieCacheable {

    /**
     * Save cookie.
     *
     * @param cookie the cookie
     * @throws IOException the io exception
     */
    void saveCookie(String cookie) throws IOException;

    /**
     * Read cookie string.
     *
     * @return the string
     * @throws IOException the io exception
     */
    String readCookie() throws IOException;

    /**
     * Delete cookie.
     */
    void deleteCookie();

}
