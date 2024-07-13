package io.fairyproject.data.impl;

import io.fairyproject.data.MetaKey;
import io.fairyproject.util.TypeLiteral;
import lombok.Getter;

import java.util.Objects;

@Getter
public class MetaKeyImpl<T> implements MetaKey<T> {

    private final int id;
    private final String name;
    private final TypeLiteral<T> type;

    public MetaKeyImpl(String name, TypeLiteral<T> type) {
        this.id = ID_COUNTER.getAndIncrement();
        this.name = name;
        this.type = type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MetaKeyImpl<?> metaKey = (MetaKeyImpl<?>) o;
        return id == metaKey.id;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
