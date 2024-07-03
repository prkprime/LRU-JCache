# Java LRU Cache
Simple implementation of javax.cache.Cache with fixed size and LRU (Least Recently Used) eviction policy.

## Compile and Install locally
```sh
mvn clean install -U
```

## Usage
Add dependency after locally installing the jar
```xml
<dependency>
  <groupId>dev.prkprime</groupId>
  <artifactId>LRU-JCache</artifactId>
  <version>1.0-SNAPSHOT</version>
</dependency>
```
Create a new instance of LRUCache with a name and a maximum size.
Note: this lacks full implementation of javax.cache.Cache and only basic methods are implemented.
CacheManager and CacheProvider are not implemented.
```java
LRUCache<String, String> cache = new LRUCache<>("test", 50);
```
