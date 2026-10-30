package com.grimidk.formicempire.classes.infrasctructure.assets;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;

final class LruCache<K, V> {

    private final int maxEntries;
    private final Map<K, V> map;

    LruCache(int maxEntries) {
        this.maxEntries = Math.max(1, maxEntries);
        this.map = new LinkedHashMap<>(16, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
                return size() > LruCache.this.maxEntries;
            }
        };
    }

    int size() {
        synchronized (map) {
            return map.size();
        }
    }

    V get(K key, Function<K, V> loader) {
        synchronized (map) {
            V existing = map.get(key);
            if (existing != null) {
                return existing;
            }
        }
        V created = loader.apply(key);
        if (created == null) {
            return null;
        }
        synchronized (map) {
            V raced = map.get(key);
            if (raced != null) {
                return raced;
            }
            map.put(key, created);
            return created;
        }
    }
}
