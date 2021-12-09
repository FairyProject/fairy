package io.fairyproject.mc.entity;

import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

public interface EntityData {

    static EntityData createImpl() {
        return new EntityDataImpl();
    }

    boolean add(Item<?> item);

    Item<?> get(int index);

    Item<?> remove(int index);

    boolean isEmpty();

    Set<Pair<Integer, Item>> all();

    void clear();

    <T> Item<T> define(int index, EntityDataSerializer<T> dataSerializer);

    interface Item<T> {

        int index();

        EntityDataSerializer<T> serializer();

        @Nullable
        T getObject();

        void setObject(@Nullable T t);

    }

}
