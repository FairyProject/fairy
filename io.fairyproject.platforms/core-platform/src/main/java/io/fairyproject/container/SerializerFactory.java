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
import io.fairyproject.jackson.JacksonService;
import io.fairyproject.serializer.AvoidDuplicate;
import io.fairyproject.serializer.SerializerData;
import lombok.Getter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The serializer factory that stores every {@link io.fairyproject.ObjectSerializer} instances.
 *
 * This service is widely used in modules such as storage and config.
 * It's like a wildcard service, so you only need to register one serializer and use everywhere.
 */
@Service
@ServiceDependency(JacksonService.class)
@Getter
public class SerializerFactory {

    private static final Logger LOGGER = LogManager.getLogger(SerializerFactory.class);

    private Map<Class<?>, SerializerData> serializers;

    @Autowired
    private JacksonService jacksonService;

    @PreInitialize
    public void onPreInitialize() {
        this.serializers = new ConcurrentHashMap<>();

        ComponentRegistry.registerComponentHolder(ComponentHolder.builder()
                        .type(ObjectSerializer.class)
                        .onEnable(obj -> {
                            ObjectSerializer<?, ?> serializer = (ObjectSerializer<?, ?>) obj;
                            this.registerSerializer(serializer);
                        })
                        .onDisable(obj -> {
                            ObjectSerializer<?, ?> serializer = (ObjectSerializer<?, ?>) obj;
                            this.unregisterSerializer(serializer);
                        })
                .build());
    }

    /**
     * Register a serializer and store it.
     * @param serializer The serializer instance
     */
    public void registerSerializer(@NotNull ObjectSerializer<?, ?> serializer) {
        boolean avoidDuplication = serializer.getClass().isAnnotationPresent(AvoidDuplicate.class);
        if (serializers.containsKey(serializer.inputClass())) {
            if (avoidDuplication) {
                throw new IllegalArgumentException("The Serializer for " + serializer.inputClass().getName() + " already exists!");
            } else {
                LOGGER.warn("Serializer with key type " + serializer.inputClass().getName() + " already exists, it is recommended to avoid duplication.");
            }
            return;
        }

        SerializerData serializerData = new SerializerData(serializer, avoidDuplication);

        this.serializers.put(serializer.inputClass(), serializerData);
        this.jacksonService.registerJacksonConfigure(new SerializerJacksonConfigure(serializer));
    }

    /**
     * Unregister an existing serializer.
     * @param serializer The serializer instance
     * @return true if unregister success
     */
    public boolean unregisterSerializer(@NotNull ObjectSerializer<?, ?> serializer) {
        return this.serializers.remove(serializer.inputClass()) != null;
    }

    /**
     * Search for the serializer instance by key type.
     * @param type the type of the serializer you are looking for
     * @return the serializer instance, null if not found
     */
    @Nullable
    public ObjectSerializer<?, ?> findSerializer(@NotNull Class<?> type) {
        final SerializerData serializerData = this.serializers.getOrDefault(type, null);
        return serializerData != null ? serializerData.getSerializer() : null;
    }

}
