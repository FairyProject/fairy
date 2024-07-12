package io.fairyproject.data.impl;

import io.fairyproject.data.MetaRegistry;
import io.fairyproject.data.MetaStorage;

import java.io.Serializable;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MetaRegistryImpl<K extends Serializable> implements MetaRegistry<K> {

    private final Map<K, MetaStorage> cache = new ConcurrentHashMap<>();

    @Override
    public MetaStorage provide(K id) {
        return cache.computeIfAbsent(id, k -> new MetaStorageImpl());
    }

    @Override
    public MetaStorage get(K id) {
        return cache.get(id);
    }

    @Override
    public void remove(K id) {
        cache.remove(id);
    }

    @Override
    public void destroy() {
        cache.clear();
    }

    @Override
    public Map<K, MetaStorage> cache() {
        return Collections.unmodifiableMap(cache);
    }
}
