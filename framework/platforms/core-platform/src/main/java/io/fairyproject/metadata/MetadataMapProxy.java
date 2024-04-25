package io.fairyproject.metadata;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

public interface MetadataMapProxy extends MetadataMap {

    MetadataMap getMetadataMap();

    default <T> void put(@NotNull MetadataKey<T> key, @NotNull T value) {
        this.getMetadataMap().put(key, value);
    }

    default <T> void put(@NotNull MetadataKey<T> key, @NotNull TransientValue<T> value) {
        this.getMetadataMap().put(key, value);
    }

    default <T> void forcePut(@NotNull MetadataKey<T> key, @NotNull T value) {
        this.getMetadataMap().forcePut(key, value);
    }

    default <T> void forcePut(@NotNull MetadataKey<T> key, @NotNull TransientValue<T> value) {
        this.getMetadataMap().forcePut(key, value);
    }

    default <T> boolean putIfAbsent(@NotNull MetadataKey<T> key, @NotNull T value) {
        return this.getMetadataMap().putIfAbsent(key, value);
    }

    default <T> boolean putIfAbsent(@NotNull MetadataKey<T> key, @NotNull TransientValue<T> value) {
        return this.getMetadataMap().putIfAbsent(key, value);
    }

    @NotNull
    default <T> Optional<T> get(@NotNull MetadataKey<T> key) {
        return this.getMetadataMap().get(key);
    }

    default <T> boolean ifPresent(@NotNull MetadataKey<T> key, @NotNull Consumer<? super T> action) {
        return this.getMetadataMap().ifPresent(key, action);
    }

    @Nullable
    default <T> T getOrNull(@NotNull MetadataKey<T> key) {
        return this.getMetadataMap().getOrNull(key);
    }

    @NotNull
    default <T> T getOrDefault(@NotNull MetadataKey<T> key, @Nullable T def) {
        return this.getMetadataMap().getOrDefault(key, def);
    }

    @NotNull
    default <T> T getOrPut(@NotNull MetadataKey<T> key, @NotNull Supplier<? extends T> def) {
        return this.getMetadataMap().getOrPut(key, def);
    }

    @Override
    default <T> T getOrThrow(@NotNull MetadataKey<T> key) {
        return this.getMetadataMap().getOrThrow(key);
    }

    @NotNull
    default <T> T getOrPutExpiring(@NotNull MetadataKey<T> key, @NotNull Supplier<? extends TransientValue<T>> def) {
        return this.getMetadataMap().getOrPutExpiring(key, def);
    }

    default boolean has(@NotNull MetadataKey<?> key) {
        return this.getMetadataMap().has(key);
    }

    default boolean remove(@NotNull MetadataKey<?> key) {
        return this.getMetadataMap().remove(key);
    }

    default void clear() {
        this.getMetadataMap().clear();
    }

    @NotNull
    default Map<MetadataKey<?>, Object> asMap() {
        return this.getMetadataMap().asMap();
    }

    default boolean isEmpty() {
        return this.getMetadataMap().isEmpty();
    }

    default void cleanup() {
        this.getMetadataMap().cleanup();
    }

}
