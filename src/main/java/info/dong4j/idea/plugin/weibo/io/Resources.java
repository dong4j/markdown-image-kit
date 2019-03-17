package info.dong4j.idea.plugin.weibo.io;

import java.io.*;
import java.net.*;

/**
 * <p>Description: </p>
 *
 * @author echisan
 * @update dong4j
 * @date 2018 -06-14 22:31
 */
public class Resources {
    private static ClassLoaderWrapper classLoaderWrapper = new ClassLoaderWrapper();

    /**
     * Instantiates a new Resources.
     */
    Resources() {
    }

    /**
     * Gets default class loader.
     *
     * @return the default class loader
     */
    public static ClassLoader getDefaultClassLoader() {
        return classLoaderWrapper.defaultClassLoader;
    }

    /**
     * Sets default class loader.
     *
     * @param classLoader the class loader
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
     */
    public static URL getResourceURL(ClassLoader classLoader, String resource) throws IOException {
        URL url = classLoaderWrapper.getResourceAsURL(resource, classLoader);
        if (url == null) {
            throw new IOException("Could not find resource" + resource);
        }
        return url;
    }
}
