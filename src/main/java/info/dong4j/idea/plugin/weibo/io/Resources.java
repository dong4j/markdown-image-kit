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

package info.dong4j.idea.plugin.weibo.io;

import java.io.IOException;
import java.net.URL;

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
public class Resources {
    /** classLoaderWrapper */
    private static final ClassLoaderWrapper classLoaderWrapper = new ClassLoaderWrapper();

    /**
     * Instantiates a new Resources.
     *
     * @since 0.0.1
     */
    Resources() {
    }

    /**
     * Gets default class loader.
     *
     * @return the default class loader
     * @since 0.0.1
     */
    public static ClassLoader getDefaultClassLoader() {
        return classLoaderWrapper.defaultClassLoader;
    }

    /**
     * Sets default class loader.
     *
     * @param classLoader the class loader
     * @since 0.0.1
     */
    public static void setDefaultClassLoader(ClassLoader classLoader) {
        classLoaderWrapper.defaultClassLoader = classLoader;
    }

    /**
     * Gets resource url.
     *
     * @param classLoader the class loader
     * @param resource    the resource
     * @return the resource url
     * @throws IOException the io exception
     * @since 0.0.1
     */
    public static URL getResourceURL(ClassLoader classLoader, String resource) throws IOException {
        URL url = classLoaderWrapper.getResourceAsURL(resource, classLoader);
        if (url == null) {
            throw new IOException("Could not find resource" + resource);
        }
        return url;
    }
}
