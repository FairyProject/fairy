package io.fairyproject.data.impl.collection;

import io.fairyproject.data.MetaStorage;
import io.fairyproject.data.impl.MetaKeyImpl;
import io.fairyproject.util.TypeLiteral;

import java.util.Collection;

public abstract class MetaCollectionKey<K, C extends Collection<K>> extends MetaKeyImpl<C> {

    public MetaCollectionKey(String name, TypeLiteral<C> type) {
        super(name, type);

    }

    public abstract C createCollection();

    public C getCollection(MetaStorage storage) {
        return this.cast(storage.computeIfAbsent(this, this::createCollection));
    }

    public boolean add(MetaStorage storage, K value) {
        return getCollection(storage).add(value);
    }

    public boolean remove(MetaStorage storage, K value) {
        return getCollection(storage).remove(value);
    }

    public boolean contains(MetaStorage storage, K value) {
        return getCollection(storage).contains(value);
    }

    public void clear(MetaStorage storage) {
        storage.remove(this);
    }

    public int size(MetaStorage storage) {
        return getCollection(storage).size();
    }

    public boolean isEmpty(MetaStorage storage) {
        return !storage.contains(this) || getCollection(storage).isEmpty();
    }

    public boolean containsAll(MetaStorage storage, Collection<K> values) {
        return storage.contains(this) && getCollection(storage).containsAll(values);
    }

    public void addAll(MetaStorage storage, Collection<K> values) {
        getCollection(storage).addAll(values);
    }

    public void removeAll(MetaStorage storage, Collection<K> values) {
        getCollection(storage).removeAll(values);
    }

    public void retainAll(MetaStorage storage, Collection<K> values) {
        getCollection(storage).retainAll(values);
    }

    public Collection<K> getValues(MetaStorage storage) {
        return getCollection(storage);
    }

    public void setValues(MetaStorage storage, Collection<K> values) {
        getCollection(storage).clear();
        getCollection(storage).addAll(values);
    }

}
