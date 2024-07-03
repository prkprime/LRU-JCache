# Java LRU Cache
Simple implementation of javax.cache.Cache with fixed size and LRU (Least Recently Used) eviction policy.

## Usage
Create a new instance of LRUCache with a name and a maximum size.
Note: this lacks full implementation of javax.cache.Cache and only basic methods are implemented.
CacheManager and CacheProvider are not implemented.
```java
LRUCache<String, String> cache = new LRUCache<>("test", 50);
```

## Compile
```sh
mvn clean install -U
```