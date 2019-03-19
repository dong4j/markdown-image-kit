package info.dong4j.idea.plugin.weibo;

import info.dong4j.idea.plugin.settings.ImageManagerPersistenComponent;
import info.dong4j.idea.plugin.weibo.io.CookieCacheable;

import org.apache.commons.lang.StringUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * <p>Description: </p>
 *
 * @author echisan
 * @update dong4j
 * @date 2018 -06-14 22:31
 */
@Slf4j
public class CookieContext implements CookieCacheable {
    private static volatile CookieContext context = null;
    private static final Object LOCK = new Object();
    private volatile String cookie = null;

    private CookieContext() {
    }

    /**
     * Gets instance.
     *
     * @return the instance
     */
    public static CookieContext getInstance() {
        if (context == null) {
            synchronized (LOCK) {
                if (context == null) {
                    context = new CookieContext();
                } else {
                    return context;
                }
            }
        }
        return context;
    }

    /**
     * Gets cookie.
     *
     * @return the cookie
     */
    synchronized String getCOOKIE() {
        if (context.cookie != null) {
            return context.cookie;
        }
        String cookie = context.readCookie();
        if(StringUtils.isBlank(cookie)){
            return null;
        }
        return cookie;
    }

    /**
     * Sets cookie.
     *
     * @param cookie the cookie
     */
    synchronized void setCOOKIE(String cookie) {
        context.cookie = cookie;
    }

    @Override
    public void saveCookie(String cookie) {
        ImageManagerPersistenComponent.getInstance().getState().getWeiboOssState().setCookies(cookie);
    }

    @Override
    public String readCookie() {
        return ImageManagerPersistenComponent.getInstance().getState().getWeiboOssState().getCookies();
    }
}
