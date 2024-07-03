package dev.prkprime.lrucache;

import org.junit.jupiter.api.Test;

public class LRUCacheTest {
    @Test
    public void testCacheCreateAndPutAndEviction() {
        try (LRUCache<String, String> stringCache = new LRUCache<>("test", 50)) {
            for (int i = 10; i > 0; i--) {
                stringCache.put("key-" + i, "value-" + i);
            }
            assert stringCache.getCurrentSize() == 10;
            for (int i = 100; i > 10; i--) {
                stringCache.put("key-" + i, "value-" + i);
            }
            assert stringCache.getCurrentSize() == 50;
            assert stringCache.get("key-11").equals(stringCache.getLruLinkedList().getByIndex(0).getValue());
            String ignored = stringCache.get("key-25");
            assert stringCache.get("key-25").equals(stringCache.getLruLinkedList().getByIndex(0).getValue());
        }
    }
}
