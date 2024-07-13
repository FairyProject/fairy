package io.fairyproject.data.impl.collection;

import io.fairyproject.data.MetaStorage;
import io.fairyproject.util.TypeLiteral;

import java.util.ArrayList;
import java.util.List;

public class MetaListKey<K> extends MetaCollectionKey<K, List<K>> {

    public MetaListKey(String name, TypeLiteral<List<K>> type) {
        super(name, type);
    }

    @Override
    public List<K> createCollection() {
        return new ArrayList<>();
    }

    public K get(MetaStorage storage, int index) {
        return getCollection(storage).get(index);
    }

    public void set(MetaStorage storage, int index, K value) {
        getCollection(storage).set(index, value);
    }

    public void add(MetaStorage storage, int index, K value) {
        getCollection(storage).add(index, value);
    }

    public K remove(MetaStorage storage, int index) {
        return getCollection(storage).remove(index);
    }

    public int indexOf(MetaStorage storage, K value) {
        return getCollection(storage).indexOf(value);
    }

    public int lastIndexOf(MetaStorage storage, K value) {
        return getCollection(storage).lastIndexOf(value);
    }

    public List<K> subList(MetaStorage storage, int fromIndex, int toIndex) {
        return getCollection(storage).subList(fromIndex, toIndex);
    }

    public void addAll(MetaStorage storage, int index, List<K> values) {
        getCollection(storage).addAll(index, values);
    }

}
