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

package io.fairyproject.bukkit.reflection.wrapper;

import lombok.Getter;
import io.fairyproject.bukkit.reflection.resolver.FieldResolver;
import io.fairyproject.bukkit.reflection.resolver.MethodResolver;
import io.fairyproject.bukkit.reflection.resolver.ResolverQuery;

import java.util.Arrays;
import java.util.stream.Stream;

public class ObjectWrapper extends WrapperAbstract {

    @Getter
    private final Object object;
    private final FieldResolver fieldResolver;
    private final MethodResolver methodResolver;

    public ObjectWrapper(Object object) {
        this.object = object;

        Class<?> type = object.getClass();
        this.fieldResolver = new FieldResolver(type);
        this.methodResolver = new MethodResolver(type);
    }

    public void setField(String field, Object value) {
        FieldWrapper fieldWrapper = this.getFieldWrapper(field);
        fieldWrapper.set(this.object, value);
    }

    private FieldWrapper getFieldWrapper(String field) {
        FieldWrapper fieldWrapper = this.fieldResolver.resolveWrapper(field);
        Class<?> type = object.getClass();

        while (!fieldWrapper.exists()) {
            type = type.getSuperclass();

            if (type == Object.class) {
                break;
            }

            fieldWrapper = new FieldResolver(type).resolveWrapper(field);
        }

        if (!fieldWrapper.exists()) {
            throw new IllegalStateException("Cannot found the field with name " + field);
        }

        return fieldWrapper;
    }

    public <T> FieldWrapper<T> getFieldWrapperByIndex(Class<T> type, int index) {
        return this.fieldResolver.resolve(type, index);
    }

    public <T> T getFieldByIndex(Class<T> type, int index) {
        return this.getFieldWrapperByIndex(type, index).get(this.object);
    }

    public <T> T getField(String field) {
        return (T) this.getFieldWrapper(field).get(this.object);
    }

    public <T> T getFieldByFirstType(Class<T> type) {
        try {
            return (T) this.fieldResolver.resolveByFirstTypeDynamic(type).get(this.object);
        } catch (Throwable throwable) {
            throw new RuntimeException(throwable);
        }
    }

    public <T> T getFieldByLastType(Class<T> type) {
        try {
            return (T) this.fieldResolver.resolveByLastTypeWrapper(type).get(this.object);
        } catch (Throwable throwable) {
            throw new RuntimeException(throwable);
        }
    }

    public <T> T invoke(String method, Object... parameters) {
        return (T) this.getMethod(method, Stream.of(parameters)
                .map(Object::getClass)
                .toArray(Class[]::new)).invoke(this.object, parameters);
    }

    public MethodWrapper getMethod(String method, Class... parametersType) {
        ResolverQuery query = new ResolverQuery(method, parametersType);
        MethodWrapper methodWrapper = this.methodResolver.resolveWrapper(query);
        Class<?> type = object.getClass();

        while (!methodWrapper.exists()) {
            type = type.getSuperclass();

            if (type == Object.class) {
                break;
            }

            methodWrapper = new MethodResolver(type).resolveWrapper(query);
        }

        if (!methodWrapper.exists()) {
            throw new IllegalStateException("Cannot found the method with name " + method + " with parameters " + Arrays.toString(parametersType));
        }

        return methodWrapper;
    }

    @Override
    public boolean exists() {
        return this.object != null;
    }
}
