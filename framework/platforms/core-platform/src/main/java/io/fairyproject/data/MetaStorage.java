package io.fairyproject.data;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.Reference;
import java.util.function.Supplier;

public interface MetaStorage {

    default boolean contains(@NotNull MetaKey<?> key) {
        return getOrNull(key) != null;
    }

    @Nullable
    <T> T getOrNull(@NotNull MetaKey<T> key);

    @NotNull
    default <T> T getOrDefault(@NotNull MetaKey<T> key, @NotNull T def) {
        T value = getOrNull(key);
        return value != null ? value : def;
    }

    @NotNull
    default <T> T getOrThrow(@NotNull MetaKey<T> key) {
        T value = getOrNull(key);
        if (value == null) {
            throw new NullPointerException("No value found for key: " + key.getName());
        }
        return value;
    }

    <T> void put(@NotNull MetaKey<T> key, @NotNull T value);

    <T> void putRef(@NotNull MetaKey<T> key, @NotNull Reference<T> reference);

    <T> T computeIfAbsent(@NotNull MetaKey<T> key, @NotNull Supplier<T> value);

    <T> void computeIfAbsentRef(@NotNull MetaKey<T> key, @NotNull Supplier<Reference<T>> reference);

    <T> boolean remove(@NotNull MetaKey<T> key);

    void clear();


}
