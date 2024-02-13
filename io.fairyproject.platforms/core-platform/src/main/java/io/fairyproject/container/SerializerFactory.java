/*
 * MIT License
 *
 * Copyright (c) 2021 Imanity
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package io.fairyproject.container;

import io.fairyproject.ObjectSerializer;
import io.fairyproject.container.collection.ContainerObjCollector;
import io.fairyproject.log.Log;
import io.fairyproject.serializer.AvoidDuplicate;
import io.fairyproject.serializer.SerializerData;
import io.fairyproject.util.ConditionUtils;
import io.fairyproject.util.exceptionally.ThrowingSupplier;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The serializer factory that stores every {@link io.fairyproject.ObjectSerializer} instances.
 *
 * This service is widely used in modules such as storage and config.
 * It's like a wildcard service, so you only need to register one serializer and use everywhere.
 */
@InjectableComponent
@RequiredArgsConstructor
@Getter
public class SerializerFactory {

    private final ContainerContext context;
    private Map<Class<?>, SerializerData> serializerByValueType;
    private Map<Class<?>, SerializerData> serializerBySerializerType;

    @PreInitialize
    public void onPreInitialize() {
        this.serializerByValueType = new ConcurrentHashMap<>();
        this.serializerBySerializerType = new ConcurrentHashMap<>();

        this.context.objectCollectorRegistry().add(ContainerObjCollector.create()
                .withFilter(ContainerObjCollector.inherits(ObjectSerializer.class))
                .withAddHandler(ContainerObjCollector.warpInstance(ObjectSerializer.class, this::registerSerializer))
                .withRemoveHandler(ContainerObjCollector.warpInstance(ObjectSerializer.class, this::unregisterSerializer))
        );
    }

    /**
     * Register a serializer and store it.
     * @param serializer The serializer instance
     */
    public void registerSerializer(@NotNull ObjectSerializer<?, ?> serializer) {
        boolean avoidDuplication = serializer.getClass().isAnnotationPresent(AvoidDuplicate.class);
        if (serializerByValueType.containsKey(serializer.inputClass())) {
            if (avoidDuplication) {
                throw new IllegalArgumentException("The Serializer for " + serializer.inputClass().getName() + " already exists!");
            } else {
                Log.warn("Serializer with key type " + serializer.inputClass().getName() + " already exists, it is recommended to avoid duplication.");
            }
            return;
        }

        SerializerData serializerData = new SerializerData(serializer, avoidDuplication);

        this.serializerByValueType.put(serializer.inputClass(), serializerData);
        this.serializerBySerializerType.put(serializer.getClass(), serializerData);
    }

    /**
     * Unregister an existing serializer.
     * @param serializer The serializer instance
     * @return true if unregister success
     */
    public boolean unregisterSerializer(@NotNull ObjectSerializer<?, ?> serializer) {
        this.serializerBySerializerType.remove(serializer.getClass());
        return this.serializerByValueType.remove(serializer.inputClass()) != null;
    }

    /**
     * Unregister an existing serializer.
     * @param type The serializer key type
     * @return true if unregister success
     */
    public boolean unregisterSerializer(@NotNull Class<?> type) {
        final SerializerData serializerData = this.serializerByValueType.remove(type);
        if (serializerData != null) {
            this.serializerBySerializerType.remove(serializerData.getSerializer().getClass());
            return true;
        }
        return false;
    }

    /**
     * Search for the serializer instance by key type.
     * @param type the type of the serializer you are looking for
     * @return the serializer instance, null if not found
     */
    @Nullable
    public ObjectSerializer<?, ?> findSerializer(@NotNull Class<?> type) {
        final SerializerData serializerData = this.serializerByValueType.get(type);
        if (serializerData == null) {
            // Search for the serializer by the assignable type
            for (Map.Entry<Class<?>, SerializerData> entry : this.serializerByValueType.entrySet()) {
                Class<?> key = entry.getKey();
                SerializerData value = entry.getValue();

                if (key.isAssignableFrom(type)) {
                    this.serializerByValueType.put(type, value);

                    return value.getSerializer();
                }
            }
        }
        return serializerData != null ? serializerData.getSerializer() : null;
    }

    /**
     * Search or new instance and cache the serializer by serializer type.
     * @param serializerClass the type class of the serializer you are looking for
     * @return the serializer instance
     */
    @NotNull
    public ObjectSerializer<?, ?> findOrCacheSerializer(@NotNull Class<?> serializerClass) {
        ConditionUtils.is(ObjectSerializer.class.isAssignableFrom(serializerClass), "Cannot findOrCacheSerializer() by a non-serializer class.");
        final SerializerData serializerData = this.serializerBySerializerType.get(serializerClass);
        if (serializerData == null) {
            ObjectSerializer<?, ?> serializer = ThrowingSupplier
                    .sneaky(() -> (ObjectSerializer<?, ?>) serializerClass.getDeclaredConstructor().newInstance())
                    .get();
            this.registerSerializer(serializer);
            return serializer;
        }
        return serializerData.getSerializer();
    }

}
