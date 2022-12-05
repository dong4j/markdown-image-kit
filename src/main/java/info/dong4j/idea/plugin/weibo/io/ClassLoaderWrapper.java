package info.dong4j.idea.plugin.weibo.io;

import java.io.InputStream;
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
class ClassLoaderWrapper {
    /**
     * The Default class loader.
     */
    ClassLoader defaultClassLoader;
    /**
     * The System class loader.
     */
    private ClassLoader systemClassLoader;

    /**
     * Instantiates a new Class loader wrapper.
     *
     * @since 0.0.1
     */
    ClassLoaderWrapper() {
        try {
            this.systemClassLoader = ClassLoader.getSystemClassLoader();
            this.defaultClassLoader = this.getClass().getClassLoader();
        } catch (SecurityException ignored) {

        }
    }

    /**
     * Gets resource as url.
     *
     * @param resource the resource
     * @return the resource as url
     * @since 0.0.1
     */
    URL getResourceAsURL(String resource) {
        return this.getResourceAsURL(resource, this.getClassLoaders(null));
    }

    /**
     * Gets resource as url.
     *
     * @param resource    the resource
     * @param classLoader the class loader
     * @return the resource as url
     * @since 0.0.1
     */
    private URL getResourceAsURL(String resource, ClassLoader[] classLoader) {
        URL url;
        for (ClassLoader cl : classLoader) {

            if (null != cl) {
                // look for the resource as passed in...
                url = cl.getResource(resource);
                // ...but some class loaders want this leading "/", so we'll add it
                // and try again if we didn't find the resource
                if (null == url) {
                    url = cl.getResource("/" + resource);
                }
                // "It's always in the last place I look for it!"
                // ... because only an idiot would keep looking for it after finding it, so stop looking already.
                if (null != url) {
                    return url;
                }
            }
        }
        return null;
    }

    /**
     * Get class loaders class loader [ ].
     *
     * @param classLoader the class loader
     * @return the class loader [ ]
     * @since 0.0.1
     */
    private ClassLoader[] getClassLoaders(ClassLoader classLoader) {
        return new ClassLoader[] {
            classLoader,
            this.defaultClassLoader,
            Thread.currentThread().getContextClassLoader(),
            this.getClass().getClassLoader(),
            this.systemClassLoader
        };
    }

    /**
     * Gets resource as url.
     *
     * @param resource    the resource
     * @param classLoader the class loader
     * @return the resource as url
     * @since 0.0.1
     */
    URL getResourceAsURL(String resource, ClassLoader classLoader) {
        return this.getResourceAsURL(resource, this.getClassLoaders(classLoader));
    }

    /**
     * Gets resource as stream.
     *
     * @param resource the resource
     * @return the resource as stream
     * @since 0.0.1
     */
    InputStream getResourceAsStream(String resource) {
        return this.getResourceAsStream(resource, this.getClassLoaders(null));
    }

    /**
     * Gets resource as stream.
     *
     * @param resource    the resource
     * @param classLoader the class loader
     * @return the resource as stream
     * @since 0.0.1
     */
    private InputStream getResourceAsStream(String resource, ClassLoader[] classLoader) {
        for (ClassLoader cl : classLoader) {
            if (null != cl) {
                InputStream returnValue = cl.getResourceAsStream(resource);
                if (null == returnValue) {
                    returnValue = cl.getResourceAsStream("/" + resource);
                }
                if (null != returnValue) {
                    return returnValue;
                }
            }
        }
        return null;
    }
}
