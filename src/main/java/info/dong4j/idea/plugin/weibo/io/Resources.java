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
