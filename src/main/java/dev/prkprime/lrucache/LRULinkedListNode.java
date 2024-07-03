package dev.prkprime.lrucache;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import javax.cache.Cache.Entry;

@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class LRULinkedListNode<K, V> implements Entry<K, V> {
    @EqualsAndHashCode.Include
    private K key;
    @EqualsAndHashCode.Include
    private V value;
    private LRULinkedListNode<K, V> prev;
    private LRULinkedListNode<K, V> next;

    LRULinkedListNode(K key, V value) {
        this.key = key;
        this.value = value;
    }

    private static String getKeyString(LRULinkedListNode<?, ?> node) {
        return node != null ? node.key.toString() : null;
    }

    @Override
    public String toString() {
        return "LinkedListNode{" + "key=" + key + ", " + "prevKey=" + getKeyString(prev) + ", " + "nextKey="
                + getKeyString(next) + '}';
    }

    @Override
    public Object unwrap(Class clazz) {
        return null;
    }
}
