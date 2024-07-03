package dev.prkprime.lrucache;

import lombok.Getter;
import lombok.ToString;

import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.configuration.CacheEntryListenerConfiguration;
import javax.cache.configuration.Configuration;
import javax.cache.integration.CacheLoader;
import javax.cache.integration.CompletionListener;
import javax.cache.processor.EntryProcessor;
import javax.cache.processor.EntryProcessorException;
import javax.cache.processor.EntryProcessorResult;
import java.io.Closeable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

/**
 * for LRU, we will create a hashmap, with key as an actual key and value as a node of doubly linked list
 *
 * @param <K>
 *            key type
 * @param <V>
 *            value type
 */
@ToString(onlyExplicitlyIncluded = true)
public class LRUCache<K, V> implements Cache<K, V>, Closeable {
    private CacheLoader<K, V> cacheLoader;
    private final long size;
    private final String name;

    AtomicBoolean closed = new AtomicBoolean(false);

    private Map<K, LRULinkedListNode<K, V>> lruCacheMap = new HashMap<>();

    @ToString.Include
    @Getter
    private LRULinkedList<K, V> lruLinkedList = new LRULinkedList<>();

    public <C extends Configuration<K, V>> LRUCache(String cacheName, long cacheSize) {
        this.name = cacheName;
        this.size = cacheSize;
    }

    @Override
    public V get(K key) {
        if (lruCacheMap.containsKey(key)) {
            LRULinkedListNode<K, V> node = lruCacheMap.get(key);
            lruLinkedList.promoteToFirst(node);
            return node.getValue();
        }
        return null;
    }

    public int getCurrentSize() {
        return lruCacheMap.size();
    }

    @Override
    public Map<K, V> getAll(Set<? extends K> keys) {
        return keys.stream().collect(Collectors.toMap(key -> key, this::get));
    }

    @Override
    public boolean containsKey(K key) {
        return lruCacheMap.containsKey(key);
    }

    @Override
    public void loadAll(Set<? extends K> keys, boolean replaceExistingValues, CompletionListener completionListener) {
        // not implemented
    }

    @Override
    public void put(K key, V value) {
        if (lruCacheMap.containsKey(key)) { // if map already has key
            putIfKeyFound(key, value);
        } else { // if key is not present in map
            putIfKeyNotFound(key, value);
        }
    }

    private V putIfKeyFound(K key, V value) {
        LRULinkedListNode<K, V> node = lruCacheMap.get(key); // retrieve node from map
        V oldValue = node.getValue();
        node.setValue(value);// set new value
        lruLinkedList.promoteToFirst(node);// promote node to first
        return oldValue;
    }

    @Override
    public V getAndPut(K key, V value) {
        V oldValue;
        if (lruCacheMap.containsKey(key)) {
            LRULinkedListNode<K, V> node = lruCacheMap.get(key);
            lruLinkedList.promoteToFirst(node);
            oldValue = node.getValue();
            node.setValue(value); // set new value
            lruLinkedList.promoteToFirst(node); // promote node to first
            return oldValue;
        } else {
            putIfKeyNotFound(key, value);
            return null;
        }
    }

    private void putIfKeyNotFound(K key, V value) {
        if (lruCacheMap.size() >= size) { // if map size is equal (or greater in unlikely scenario) than permitted cache
            // size
            K evictedKey = lruLinkedList.evictLast(); // evict last node of linked list
            lruCacheMap.remove(evictedKey); // remove evicted key from map
        }
        LRULinkedListNode<K, V> newNode = new LRULinkedListNode<>(key, value);// create new node
        lruLinkedList.addFirst(newNode);
        lruCacheMap.put(key, newNode);
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> map) {
        map.forEach(this::put);
    }

    @Override
    public boolean putIfAbsent(K key, V value) {
        if (!lruCacheMap.containsKey(key)) {
            putIfKeyNotFound(key, value);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean remove(K key) {
        if (lruCacheMap.containsKey(key)) {
            removeAndReturnNode(key);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean remove(K key, V oldValue) {
        if (lruCacheMap.containsKey(key) && lruCacheMap.get(key).getValue().equals(oldValue)) {
            removeAndReturnNode(key);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public V getAndRemove(K key) {
        if (lruCacheMap.containsKey(key)) {
            LRULinkedListNode<K, V> deletedNode = removeAndReturnNode(key);
            return deletedNode.getValue();
        } else {
            return null;
        }
    }

    private LRULinkedListNode<K, V> removeAndReturnNode(K key) {
        LRULinkedListNode<K, V> nodeTobeDeleted = lruCacheMap.get(key);
        LRULinkedListNode<K, V> deletedNode = lruLinkedList.deleteNode(nodeTobeDeleted);
        lruCacheMap.remove(key);
        return deletedNode;
    }

    @Override
    public boolean replace(K key, V oldValue, V newValue) {
        if (lruCacheMap.containsKey(key) && get(key).equals(oldValue)) {
            putIfKeyFound(key, newValue);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean replace(K key, V value) {
        if (lruCacheMap.containsKey(key)) {
            putIfKeyFound(key, value);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public V getAndReplace(K key, V value) {
        if (lruCacheMap.containsKey(key)) {
            return putIfKeyFound(key, value);
        } else {
            return null;
        }
    }

    @Override
    public void removeAll(Set<? extends K> keys) {
        keys.forEach(this::remove);
    }

    @Override
    public void removeAll() {
        lruLinkedList.clear();
        lruCacheMap.clear();
    }

    @Override
    public void clear() {
        removeAll();
        lruLinkedList = null;
        lruCacheMap = null;
    }

    @Override
    public <C extends Configuration<K, V>> C getConfiguration(Class<C> clazz) {
        return null;
    }

    @Override
    public <T> T invoke(K key, EntryProcessor<K, V, T> entryProcessor, Object... arguments)
            throws EntryProcessorException {
        return null;
    }

    @Override
    public <T> Map<K, EntryProcessorResult<T>> invokeAll(Set<? extends K> keys, EntryProcessor<K, V, T> entryProcessor,
            Object... arguments) {
        return Map.of();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public CacheManager getCacheManager() {
        return null;
    }

    @Override
    public void close() {
        clear();
        closed.set(true);
    }

    @Override
    public boolean isClosed() {
        return closed.get();
    }

    @Override
    public <T> T unwrap(Class<T> clazz) {
        return null;
    }

    @Override
    public void registerCacheEntryListener(CacheEntryListenerConfiguration<K, V> cacheEntryListenerConfiguration) {

    }

    @Override
    public void deregisterCacheEntryListener(CacheEntryListenerConfiguration<K, V> cacheEntryListenerConfiguration) {

    }

    @Override
    public Iterator<Entry<K, V>> iterator() {
        return null;
    }
}
