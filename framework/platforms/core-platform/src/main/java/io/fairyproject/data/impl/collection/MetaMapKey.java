package io.fairyproject.data.impl.collection;

import io.fairyproject.data.MetaStorage;
import io.fairyproject.data.impl.MetaKeyImpl;
import io.fairyproject.util.TypeLiteral;

import java.util.HashMap;
import java.util.Map;

public class MetaMapKey<K, V> extends MetaKeyImpl<Map<K, V>> {

    public MetaMapKey(String name, TypeLiteral<Map<K, V>> type) {
        super(name, type);
    }

    public Map<K, V> createCollection() {
        return this.cast(new HashMap<>());
    }

    public Map<K, V> getCollection(MetaStorage storage) {
        return this.cast(storage.computeIfAbsent(this, this::createCollection));
    }

    public V get(MetaStorage storage, K key) {
        return getCollection(storage).get(key);
    }

    public V put(MetaStorage storage, K key, V value) {
        return getCollection(storage).put(key, value);
    }

    public V remove(MetaStorage storage, K key) {
        return getCollection(storage).remove(key);
    }

    public boolean containsKey(MetaStorage storage, K key) {
        return getCollection(storage).containsKey(key);
    }

    public boolean containsValue(MetaStorage storage, V value) {
        return getCollection(storage).containsValue(value);
    }

    public void clear(MetaStorage storage) {
        getCollection(storage).clear();
    }

    public int size(MetaStorage storage) {
        return getCollection(storage).size();
    }

    public boolean isEmpty(MetaStorage storage) {
        return getCollection(storage).isEmpty();
    }

}
