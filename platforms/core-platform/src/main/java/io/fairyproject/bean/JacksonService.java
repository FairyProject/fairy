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

package io.fairyproject.bean;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.fairyproject.jackson.JacksonConfigure;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

@Service(name = "jackson")
public class JacksonService {

    public static JacksonService INSTANCE;

    @Getter
    private ObjectMapper mainMapper;
    private Map<String, ObjectMapper> registeredMappers;
    private List<JacksonConfigure> jacksonConfigures;

    @PreInitialize
    public void preInit() {
        INSTANCE = this;

        this.registeredMappers = new ConcurrentHashMap<>();
        this.jacksonConfigures = new ArrayList<>();
        this.mainMapper = this.getOrCreateJacksonMapper("main");
    }

    @SafeVarargs
    public final ObjectMapper getOrCreateJacksonMapper(String name, Consumer<ObjectMapper>... onFirstInitial) {
        if (this.registeredMappers.containsKey(name)) {
            return this.registeredMappers.get(name);
        }

        ObjectMapper objectMapper = new ObjectMapper();
        this.configureJacksonMapper(name, objectMapper);
        for (Consumer<ObjectMapper> consumer : onFirstInitial) {
            consumer.accept(objectMapper);
        }

        return objectMapper;
    }

    public void configureJacksonMapper(String name, ObjectMapper objectMapper) {
        objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.NONE);
        objectMapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
        objectMapper.configure(MapperFeature.PROPAGATE_TRANSIENT_MARKER, true);

        this.registeredMappers.put(name, objectMapper);
        for (JacksonConfigure configure : this.jacksonConfigures) {
            configure.configure(objectMapper);
        }
    }

    public void registerJacksonConfigure(JacksonConfigure jacksonConfigure) {
        this.jacksonConfigures.add(jacksonConfigure);
        for (ObjectMapper mapper : this.registeredMappers.values()) {
            jacksonConfigure.configure(mapper);
        }
    }

}
