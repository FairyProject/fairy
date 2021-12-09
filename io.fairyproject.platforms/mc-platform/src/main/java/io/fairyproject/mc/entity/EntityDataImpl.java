package io.fairyproject.mc.entity;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.Nullable;

import java.util.Set;
import java.util.stream.Collectors;

public class EntityDataImpl implements EntityData {

    private final Int2ObjectOpenHashMap<EntityData.Item<?>> items;

    public EntityDataImpl() {
        this.items = new Int2ObjectOpenHashMap<>();
    }

    @Override
    public boolean add(Item<?> item) {
        return this.items.put(item.index(), item) != item;
    }

    @Override
    public Item<?> get(int index) {
        return this.items.get(index);
    }

    @Override
    public Item<?> remove(int index) {
        return this.items.remove(index);
    }

    @Override
    public boolean isEmpty() {
        return this.items.isEmpty();
    }

    @Override
    public Set<Pair<Integer, Item>> all() {
        return this.items.int2ObjectEntrySet()
                .stream()
                .map(entry -> Pair.of(entry.getIntKey(), (Item)entry.getValue()))
                .collect(Collectors.toSet());
    }

    @Override
    public void clear() {
        this.items.clear();
    }

    @Override
    public <T> Item<T> define(int index, EntityDataSerializer<T> dataSerializer) {
        return new ItemImpl<>(index, dataSerializer);
    }

    @RequiredArgsConstructor
    public static class ItemImpl<T> implements EntityData.Item<T> {

        private final int index;
        private final EntityDataSerializer<T> serializer;
        @Nullable
        private T object;

        @Override
        public int index() {
            return this.index;
        }

        @Override
        public EntityDataSerializer<T> serializer() {
            return this.serializer;
        }

        @Override
        @Nullable
        public T getObject() {
            return this.object;
        }

        @Override
        public void setObject(@Nullable T t) {
            this.object = t;
        }
    }
}
