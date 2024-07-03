package dev.prkprime.lrucache;

import lombok.extern.slf4j.Slf4j;

import javax.cache.Cache;
import javax.cache.CacheException;
import javax.cache.CacheManager;
import javax.cache.configuration.Configuration;
import javax.cache.spi.CachingProvider;
import java.lang.ref.WeakReference;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

@Slf4j
public class LRUCacheManager implements CacheManager {

    private final HashMap<String, LRUCache<?, ?>> caches = new HashMap<>();

    private final LRUCacheProvider cachingProvider;
    private final URI uri;
    private final WeakReference<ClassLoader> classLoaderReference;
    private final Properties properties;
    private volatile boolean isClosed;

    private static final long defaultCacheSize = 1024;

    public LRUCacheManager(LRUCacheProvider lruCacheProvider, URI managerURI, ClassLoader managerClassLoader,
            Properties managerProperties) {
        this.cachingProvider = lruCacheProvider;
        if (managerURI == null) {
            throw new NullPointerException("No CacheManager URI specified");
        }
        this.uri = managerURI;
        if (managerClassLoader == null) {
            throw new NullPointerException("No ClassLoader specified");
        }
        this.classLoaderReference = new WeakReference<ClassLoader>(managerClassLoader);
        this.properties = new Properties();
        for (Object key : managerProperties.keySet()) {
            this.properties.put(key, managerProperties.get(key));
        }
        // this.properties = properties == null ? new Properties() : new Properties(properties);
        isClosed = false;
    }

    @Override
    public CachingProvider getCachingProvider() {
        return cachingProvider;
    }

    @Override
    public URI getURI() {
        return uri;
    }

    @Override
    public ClassLoader getClassLoader() {
        return classLoaderReference.get();
    }

    @Override
    public Properties getProperties() {
        return properties;
    }

    @Override
    public <K, V, C extends Configuration<K, V>> Cache<K, V> createCache(String cacheName, C configuration)
            throws IllegalArgumentException {
        return createCacheCustomSize(cacheName, configuration, defaultCacheSize);
    }

    public <K, V, C extends Configuration<K, V>> Cache<K, V> createCacheCustomSize(String cacheName, C configuration,
            long cacheSize) {
        if (isClosed()) {
            throw new IllegalStateException();
        }

        if (cacheName == null) {
            throw new NullPointerException("cacheName must not be null");
        }

        if (configuration == null) {
            throw new NullPointerException("configuration must not be null");
        }

        synchronized (caches) {
            LRUCache<?, ?> cache = caches.get(cacheName);

            if (cache == null) {
                // cache = new LRUCache<>(this, cacheName, getClassLoader(), configuration, cacheSize);
                caches.put(cache.getName(), cache);

                return (Cache<K, V>) cache;
            } else {
                throw new CacheException("A cache named " + cacheName + " already exists.");
            }
        }
    }

    @Override
    public <K, V> Cache<K, V> getCache(String cacheName, Class<K> keyType, Class<V> valueType) {
        return null;
    }

    @Override
    public <K, V> Cache<K, V> getCache(String cacheName) {
        return null;
    }

    @Override
    public Iterable<String> getCacheNames() {
        return null;
    }

    @Override
    public void destroyCache(String cacheName) {

    }

    @Override
    public void enableManagement(String cacheName, boolean enabled) {

    }

    @Override
    public void enableStatistics(String cacheName, boolean enabled) {

    }

    @Override
    public synchronized void close() {
        if (!isClosed()) {
            // first releaseCacheManager the CacheManager from the CacheProvider so that
            // future requests for this CacheManager won't return this one
            cachingProvider.releaseCacheManager(getURI(), getClassLoader());
            isClosed = true;
            List<LRUCache<?, ?>> cacheList;
            synchronized (caches) {
                cacheList = caches.values().stream().toList();
                caches.clear();
            }
            for (Cache<?, ?> cache : cacheList) {
                try {
                    cache.close();
                } catch (Exception e) {
                    log.warn("Error stopping cache: {}", cache, e);
                }
            }
        }
    }

    @Override
    public boolean isClosed() {
        return isClosed;
    }

    @Override
    public <T> T unwrap(Class<T> clazz) {
        return null;
    }
}
