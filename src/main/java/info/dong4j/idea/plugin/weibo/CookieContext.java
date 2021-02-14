/*
 * MIT License
 *
 * Copyright (c) 2021 dong4j <dong4j@gmail.com>
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

import info.dong4j.idea.plugin.settings.MikPersistenComponent;
import info.dong4j.idea.plugin.weibo.io.CookieCacheable;

import org.apache.commons.lang.StringUtils;

import lombok.extern.slf4j.Slf4j;

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
@Slf4j
public class CookieContext implements CookieCacheable {
    /** LOCK */
    private static final Object LOCK = new Object();
    /** context */
    private static volatile CookieContext context = null;
    /** Cookie */
    private volatile String cookie = null;

    /**
     * Cookie context
     *
     * @since 0.0.1
     */
    private CookieContext() {
    }

    /**
     * Gets instance.
     *
     * @return the instance
     * @since 0.0.1
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
     * @since 0.0.1
     */
    synchronized String getCOOKIE() {
        if (StringUtils.isNotBlank(context.cookie)) {
            return context.cookie;
        }
        String cookie = context.readCookie();
        if (StringUtils.isBlank(cookie)) {
            return null;
        }
        return cookie;
    }

    /**
     * Sets cookie.
     *
     * @param cookie the cookie
     * @since 0.0.1
     */
    synchronized void setCOOKIE(String cookie) {
        context.cookie = cookie;
        this.saveCookie(cookie);
    }

    /**
     * Save cookie
     *
     * @param cookie cookie
     * @since 0.0.1
     */
    @Override
    public void saveCookie(String cookie) {
        MikPersistenComponent.getInstance().getState().getWeiboOssState().setCookies(cookie);
    }

    /**
     * Read cookie
     *
     * @return the string
     * @since 0.0.1
     */
    @Override
    public String readCookie() {
        return MikPersistenComponent.getInstance().getState().getWeiboOssState().getCookies();
    }

    /**
     * Delete cookie
     *
     * @since 0.0.1
     */
    @Override
    public void deleteCookie() {
        context.cookie = null;
        this.saveCookie("");
    }
}
