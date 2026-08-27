package com.grimidk.formicempire.classes.infrasctructure.assets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

class LruCacheTest {

    @Test
    void evictsOldestEntryWhenOverCapacity() {
        LruCache<Integer, String> cache = new LruCache<>(2);
        cache.get(1, k -> "one");
        cache.get(2, k -> "two");
        cache.get(1, k -> "unexpected");
        cache.get(3, k -> "three");

        assertEquals(2, cache.size());
        assertNotNull(cache.get(1, k -> "unexpected"));
        assertNotNull(cache.get(3, k -> "unexpected"));
    }
}
