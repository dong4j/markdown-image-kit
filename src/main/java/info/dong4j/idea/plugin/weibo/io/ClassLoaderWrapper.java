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
class ClassLoaderWrapper {
    /**
     * The System class loader.
     */
    private ClassLoader systemClassLoader;
    /**
     * The Default class loader.
     */
    ClassLoader defaultClassLoader;

    /**
     * Instantiates a new Class loader wrapper.
     */
    ClassLoaderWrapper() {
        try {
            systemClassLoader = ClassLoader.getSystemClassLoader();
            defaultClassLoader = getClass().getClassLoader();
        } catch (SecurityException ignored) {

        }
    }

    /**
     * Gets resource as url.
     *
     * @param resource the resource
     * @return the resource as url
     */
    URL getResourceAsURL(String resource) {
        return getResourceAsURL(resource, getClassLoaders(null));
    }

    /**
     * Gets resource as url.
     *
     * @param resource    the resource
     * @param classLoader the class loader
     * @return the resource as url
     */
    URL getResourceAsURL(String resource, ClassLoader classLoader) {
        return getResourceAsURL(resource, getClassLoaders(classLoader));
    }

    /**
     * Gets resource as url.
     *
     * @param resource    the resource
     * @param classLoader the class loader
     * @return the resource as url
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
     * Gets resource as stream.
     *
     * @param resource the resource
     * @return the resource as stream
     */
    InputStream getResourceAsStream(String resource) {
        return getResourceAsStream(resource, getClassLoaders(null));
    }

    /**
     * Gets resource as stream.
     *
     * @param resource    the resource
     * @param classLoader the class loader
     * @return the resource as stream
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

    /**
     * Get class loaders class loader [ ].
     *
     * @param classLoader the class loader
     * @return the class loader [ ]
     */
    private ClassLoader[] getClassLoaders(ClassLoader classLoader) {
        return new ClassLoader[]{
                classLoader,
                defaultClassLoader,
                Thread.currentThread().getContextClassLoader(),
                getClass().getClassLoader(),
                systemClassLoader
        };
    }
}
