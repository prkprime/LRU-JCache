package dev.prkprime.lrucache;

import javax.cache.CacheException;
import javax.cache.CacheManager;
import javax.cache.configuration.OptionalFeature;
import javax.cache.spi.CachingProvider;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Objects;
import java.util.Properties;
import java.util.WeakHashMap;

public class LRUCacheProvider implements CachingProvider {

    private WeakHashMap<ClassLoader, HashMap<URI, CacheManager>> cacheManagersByClassLoader;

    public LRUCacheProvider() {
        this.cacheManagersByClassLoader = new WeakHashMap<>();
    }

    @Override
    public synchronized CacheManager getCacheManager(URI uri, ClassLoader classLoader, Properties properties) {
        URI managerURI = uri == null ? getDefaultURI() : uri;
        ClassLoader managerClassLoader = classLoader == null ? getDefaultClassLoader() : classLoader;
        Properties managerProperties = properties == null ? new Properties() : properties;
        HashMap<URI, CacheManager> cacheManagersByURI = cacheManagersByClassLoader.get(managerClassLoader);
        if (cacheManagersByURI == null) {
            cacheManagersByURI = new HashMap<>();
        }
        CacheManager cacheManager = cacheManagersByURI.computeIfAbsent(managerURI,
                y -> new LRUCacheManager(this, y, managerClassLoader, managerProperties));
        if (!cacheManagersByClassLoader.containsKey(managerClassLoader)) {
            cacheManagersByClassLoader.put(managerClassLoader, cacheManagersByURI);
        }
        return cacheManager;
    }

    @Override
    public ClassLoader getDefaultClassLoader() {
        return getClass().getClassLoader();
    }

    @Override
    public URI getDefaultURI() {
        try {
            return new URI(this.getClass().getName());
        } catch (URISyntaxException e) {
            throw new CacheException("Failed to create the default URI for the javax.cache LRUCache implementation", e);
        }
    }

    @Override
    public Properties getDefaultProperties() {
        return null;
    }

    @Override
    public CacheManager getCacheManager(URI uri, ClassLoader classLoader) {
        return getCacheManager(uri, classLoader, getDefaultProperties());
    }

    @Override
    public CacheManager getCacheManager() {
        return getCacheManager(getDefaultURI(), getDefaultClassLoader(), null);
    }

    @Override
    public void close() {
        WeakHashMap<ClassLoader, HashMap<URI, CacheManager>> managersByClassLoader = this.cacheManagersByClassLoader;
        this.cacheManagersByClassLoader = new WeakHashMap<>();
        for (ClassLoader classLoader : managersByClassLoader.keySet()) {
            for (CacheManager cacheManager : managersByClassLoader.get(classLoader).values()) {
                cacheManager.close();
            }
        }
    }

    @Override
    public void close(ClassLoader classLoader) {
        ClassLoader managerClassLoader = classLoader == null ? getDefaultClassLoader() : classLoader;
        HashMap<URI, CacheManager> cacheManagersByURI = cacheManagersByClassLoader.remove(managerClassLoader);
        if (cacheManagersByURI != null) {
            for (CacheManager cacheManager : cacheManagersByURI.values()) {
                cacheManager.close();
            }
        }
    }

    @Override
    public void close(URI uri, ClassLoader classLoader) {
        URI managerURI = uri == null ? getDefaultURI() : uri;
        ClassLoader managerClassLoader = classLoader == null ? getDefaultClassLoader() : classLoader;
        HashMap<URI, CacheManager> cacheManagersByURI = cacheManagersByClassLoader.get(managerClassLoader);
        if (cacheManagersByURI != null) {
            CacheManager cacheManager = cacheManagersByURI.remove(managerURI);
            if (cacheManager != null) {
                cacheManager.close();
            }
            if (cacheManagersByURI.isEmpty()) {
                cacheManagersByClassLoader.remove(managerClassLoader);
            }
        }
    }

    /**
     * Releases the CacheManager with the specified URI and ClassLoader from this CachingProvider. This does not close
     * the CacheManager. It simply releases it from being tracked by the CachingProvider.
     * <p>
     * This method does nothing if a CacheManager matching the specified parameters is not being tracked.
     * </p>
     *
     * @param uri
     *            the URI of the CacheManager
     * @param classLoader
     *            the ClassLoader of the CacheManager
     */
    public synchronized void releaseCacheManager(URI uri, ClassLoader classLoader) {
        URI managerURI = uri == null ? getDefaultURI() : uri;
        ClassLoader managerClassLoader = classLoader == null ? getDefaultClassLoader() : classLoader;

        HashMap<URI, CacheManager> cacheManagersByURI = cacheManagersByClassLoader.get(managerClassLoader);
        if (cacheManagersByURI != null) {
            cacheManagersByURI.remove(managerURI);
            if (cacheManagersByURI.isEmpty()) {
                cacheManagersByClassLoader.remove(managerClassLoader);
            }
        }
    }

    @Override
    public boolean isSupported(OptionalFeature optionalFeature) {
        if (Objects.requireNonNull(optionalFeature) == OptionalFeature.STORE_BY_REFERENCE) {
            return true;
        }
        return false;
    }
}
