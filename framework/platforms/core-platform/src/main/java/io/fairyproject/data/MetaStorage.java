package io.fairyproject.data;

import io.fairyproject.data.impl.MetaStorageImpl;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.Reference;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * A storage for metadata
 */
public interface MetaStorage {

    /**
     * Check if the storage contains a value for the given key
     *
     * @param key the key
     * @return true if the storage contains a value for the key, false otherwise
     */
    default boolean contains(@NotNull MetaKey<?> key) {
        return getOrNull(key) != null;
    }

    /**
     * Get the value for the given key
     *
     * @param key the key
     * @return the value or null if no value is found
     * @param <T> the type of the value
     */
    @Nullable
    <T> T getOrNull(@NotNull MetaKey<T> key);

    /**
     * Get the value for the given key or the default value if no value is found
     *
     * @param key the key
     * @param def the default value
     * @return the value or the default value if no value is found
     * @param <T> the type of the value
     */
    @NotNull
    default <T> T getOrDefault(@NotNull MetaKey<T> key, @NotNull T def) {
        T value = getOrNull(key);
        return value != null ? value : def;
    }

    /**
     * Get the value for the given key or throw a NullPointerException if no value is found
     *
     * @param key the key
     * @return the value
     * @param <T> the type of the value
     */
    @NotNull
    default <T> T getOrThrow(@NotNull MetaKey<T> key) {
        T value = getOrNull(key);
        if (value == null) {
            throw new NullPointerException("No value found for key: " + key.getName());
        }
        return value;
    }

    /**
     * Put the value for the given key
     *
     * @param key the key
     * @param value the value
     * @param <T> the type of the value
     */
    <T> void put(@NotNull MetaKey<T> key, @NotNull T value);

    /**
     * Put the reference for the given key
     *
     * @param key the key
     * @param reference the reference
     * @param <T> the type of the value
     */
    <T> void putRef(@NotNull MetaKey<T> key, @NotNull Reference<T> reference);

    /**
     * Compute the value for the given key if no value is found
     *
     * @param key the key
     * @param value the value supplier
     * @return the value from supplier or the value found
     * @param <T> the type of the value
     */
    <T> T computeIfAbsent(@NotNull MetaKey<T> key, @NotNull Supplier<T> value);

    /**
     * Compute the reference for the given key if no value is found
     *
     * @param key the key
     * @param reference the reference supplier
     * @param <T> the type of the value
     */
    <T> void computeIfAbsentRef(@NotNull MetaKey<T> key, @NotNull Supplier<Reference<T>> reference);

    /**
     * If the value for the given key is present, apply the consumer
     *
     * @param key the key
     * @param consumer the consumer
     * @param <T> the type of the value
     */
    default <T> void ifPresent(@NotNull MetaKey<T> key, @NotNull Consumer<T> consumer) {
        T value = getOrNull(key);
        if (value != null) {
            consumer.accept(value);
        }
    }

    /**
     * Remove the value for the given key
     *
     * @param key the key
     * @return true if the value was removed, false otherwise
     * @param <T> the type of the value
     */
    <T> boolean remove(@NotNull MetaKey<T> key);

    /**
     * Clear the storage
     */
    void clear();

    /**
     * Create a new MetaStorage
     *
     * @return the new MetaStorage
     */
    static MetaStorage create() {
        return new MetaStorageImpl();
    }

}
