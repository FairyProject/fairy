package io.fairyproject.data.impl.collection;

import io.fairyproject.util.TypeLiteral;

import java.util.HashSet;
import java.util.Set;

public class MetaSetKey<K> extends MetaCollectionKey<K, Set<K>> {

    public MetaSetKey(String name, TypeLiteral<Set<K>> type) {
        super(name, type);
    }

    @Override
    public Set<K> createCollection() {
        return new HashSet<>();
    }

}
