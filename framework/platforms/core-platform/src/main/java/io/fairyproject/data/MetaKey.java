package io.fairyproject.data;

import io.fairyproject.data.impl.MetaKeyImpl;
import io.fairyproject.data.impl.collection.MetaListKey;
import io.fairyproject.data.impl.collection.MetaMapKey;
import io.fairyproject.data.impl.collection.MetaSetKey;
import io.fairyproject.util.TypeLiteral;
import org.jetbrains.annotations.ApiStatus;

import java.lang.ref.Reference;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A key for metadata
 *
 * @param <T> the type of the key
 */
public interface MetaKey<T> {

    @ApiStatus.Internal
    AtomicInteger ID_COUNTER = new AtomicInteger(0);

    /**
     * Get the unique identifier of this key
     *
     * @return the unique identifier
     */
    int getId();

    /**
     * Get the name of this key
     *
     * @return the name
     */
    String getName();

    /**
     * Get the type of this key
     *
     * @return the type
     */
    TypeLiteral<T> getType();

    /**
     * Cast the value to the type of this key
     * This method will return null if the value is null
     * This method will return null if the value is a reference and the reference is null
     *
     * @param value the value to cast
     * @return the casted value
     */
    @SuppressWarnings("unchecked")
    default T cast(Object value) {
        if (value == null)
            return null;

        if (value instanceof Reference) {
            Object obj = ((Reference<?>) value).get();
            if (obj == null)
                return null;

            return (T) obj;
        }

        return (T) value;
    }

    /**
     * Get the current capacity of the keys
     *
     * @return the current capacity
     */
    static int getCurrentCapacity() {
        return ID_COUNTER.get() + 1;
    }

    /**
     * Create a new meta key
     *
     * @param name the name of the key
     * @param type the type of the key
     * @param <T>  the type of the key
     * @return the new meta key
     */
    static <T> MetaKey<T> create(String name, Class<T> type) {
        return new MetaKeyImpl<>(name, new TypeLiteral<>(type));
    }

    /**
     * Create a new meta key with boolean type
     *
     * @param name the name of the key
     * @return the new meta key
     */
    static MetaKey<Boolean> createBoolean(String name) {
        return create(name, Boolean.class);
    }

    /**
     * Create a new meta key with integer type
     *
     * @param name the name of the key
     * @return the new meta key
     */
    static MetaKey<Integer> createInt(String name) {
        return create(name, Integer.class);
    }

    /**
     * Create a new meta key with long type
     *
     * @param name the name of the key
     * @return the new meta key
     */
    static MetaKey<Long> createLong(String name) {
        return create(name, Long.class);
    }

    /**
     * Create a new meta key with float type
     *
     * @param name the name of the key
     * @return the new meta key
     */
    static MetaKey<Float> createFloat(String name) {
        return create(name, Float.class);
    }

    /**
     * Create a new meta key with double type
     *
     * @param name the name of the key
     * @return the new meta key
     */
    static MetaKey<Double> createDouble(String name) {
        return create(name, Double.class);
    }

    /**
     * Create a new meta key with string type
     *
     * @param name the name of the key
     * @return the new meta key
     */
    static MetaKey<String> createString(String name) {
        return create(name, String.class);
    }

    /**
     * Create a new meta key with byte type
     *
     * @param name the name of the key
     * @return the new meta key
     */
    static MetaKey<Byte> createByte(String name) {
        return create(name, Byte.class);
    }

    /**
     * Create a new meta key with short type
     *
     * @param name the name of the key
     * @return the new meta key
     */
    static MetaKey<Short> createShort(String name) {
        return create(name, Short.class);
    }

    /**
     * Create a new meta key
     *
     * @param name the name of the key
     * @param type the type of the key
     * @param <T>  the type of the key
     * @return the new meta key
     */
    static <T> MetaKey<T> create(String name, TypeLiteral<T> type) {
        return new MetaKeyImpl<>(name, type);
    }

    /**
     * Create a list key
     *
     * @param name the name of the key
     * @param type the type of values in the list of the key
     */
    @SuppressWarnings("unused")
    static <T> MetaListKey<T> createList(String name, Class<T> type) {
        return new MetaListKey<>(name, new TypeLiteral<List<T>>() {
        });
    }

    /**
     * Create a list key
     *
     * @param name the name of the key
     * @param type the type of values in the list of the key
     */
    @SuppressWarnings("unused")
    static <T> MetaListKey<T> createList(String name, TypeLiteral<List<T>> type) {
        return new MetaListKey<>(name, type);
    }

    /**
     * Create a set key
     *
     * @param name the name of the key
     * @param type the type of values in the set of the key
     */
    @SuppressWarnings("unused")
    static <T> MetaSetKey<T> createSet(String name, Class<T> type) {
        return new MetaSetKey<>(name, new TypeLiteral<Set<T>>() {
        });
    }

    /**
     * Create a set key
     *
     * @param name the name of the key
     * @param type the type of values in the set of the key
     */
    @SuppressWarnings("unused")
    static <T> MetaSetKey<T> createSet(String name, TypeLiteral<Set<T>> type) {
        return new MetaSetKey<>(name, type);
    }

    /**
     * Create a map key
     *
     * @param name      the name of the key
     * @param keyType   the type of keys in the map of the key
     * @param valueType the type of values in the map of the key
     */
    @SuppressWarnings("unused")
    static <K, V> MetaMapKey<K, V> createMap(String name, Class<K> keyType, Class<V> valueType) {
        return new MetaMapKey<>(name, new TypeLiteral<Map<K, V>>() {
        });
    }

    /**
     * Create a map key
     *
     * @param name    the name of the key
     * @param mapType the type of the map of the key
     */
    @SuppressWarnings("unused")
    static <K, V> MetaMapKey<K, V> createMap(String name, TypeLiteral<Map<K, V>> mapType) {
        return new MetaMapKey<>(name, mapType);
    }

}
