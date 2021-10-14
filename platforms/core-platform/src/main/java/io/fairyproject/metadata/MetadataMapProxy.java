package io.fairyproject.metadata;

import com.google.common.collect.ImmutableMap;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

public interface MetadataMapProxy extends MetadataMap {

    MetadataMap getMetadataMap();

    default <T> void put(@Nonnull MetadataKey<T> key, @Nonnull T value) {
        this.getMetadataMap().put(key, value);
    }

    default <T> void put(@Nonnull MetadataKey<T> key, @Nonnull TransientValue<T> value) {
        this.getMetadataMap().put(key, value);
    }

    default <T> void forcePut(@Nonnull MetadataKey<T> key, @Nonnull T value) {
        this.getMetadataMap().forcePut(key, value);
    }

    default <T> void forcePut(@Nonnull MetadataKey<T> key, @Nonnull TransientValue<T> value) {
        this.getMetadataMap().forcePut(key, value);
    }

    default <T> boolean putIfAbsent(@Nonnull MetadataKey<T> key, @Nonnull T value) {
        return this.getMetadataMap().putIfAbsent(key, value);
    }

    default <T> boolean putIfAbsent(@Nonnull MetadataKey<T> key, @Nonnull TransientValue<T> value) {
        return this.getMetadataMap().putIfAbsent(key, value);
    }

    @Nonnull
    default <T> Optional<T> get(@Nonnull MetadataKey<T> key) {
        return this.getMetadataMap().get(key);
    }

    default <T> boolean ifPresent(@Nonnull MetadataKey<T> key, @Nonnull Consumer<? super T> action) {
        return this.getMetadataMap().ifPresent(key, action);
    }

    @Nullable
    default <T> T getOrNull(@Nonnull MetadataKey<T> key) {
        return this.getMetadataMap().getOrNull(key);
    }

    @Nonnull
    default <T> T getOrDefault(@Nonnull MetadataKey<T> key, @Nullable T def) {
        return this.getMetadataMap().getOrDefault(key, def);
    }

    @Nonnull
    default <T> T getOrPut(@Nonnull MetadataKey<T> key, @Nonnull Supplier<? extends T> def) {
        return this.getMetadataMap().getOrPut(key, def);
    }

    @Override
    default <T> T getOrThrow(@NotNull MetadataKey<T> key) {
        return this.getMetadataMap().getOrThrow(key);
    }

    @Nonnull
    default <T> T getOrPutExpiring(@Nonnull MetadataKey<T> key, @Nonnull Supplier<? extends TransientValue<T>> def) {
        return this.getMetadataMap().getOrPutExpiring(key, def);
    }

    default boolean has(@Nonnull MetadataKey<?> key) {
        return this.getMetadataMap().has(key);
    }

    default boolean remove(@Nonnull MetadataKey<?> key) {
        return this.getMetadataMap().remove(key);
    }

    default void clear() {
        this.getMetadataMap().clear();
    }

    @Nonnull
    default ImmutableMap<MetadataKey<?>, Object> asMap() {
        return this.getMetadataMap().asMap();
    }

    default boolean isEmpty() {
        return this.getMetadataMap().isEmpty();
    }

    default void cleanup() {
        this.getMetadataMap().cleanup();
    }

}
