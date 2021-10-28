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

package io.fairyproject.reflect;

import lombok.Getter;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

@Getter
public class ReflectObject {

    private final ReflectCache accessorCache;
    private final Class<?> type;

    private Object instance;

    public ReflectObject(Object instance) {
        this(instance.getClass());
        this.instance(instance);
    }

    public ReflectObject(Class<?> type) {
        this.type = type;
        this.accessorCache = ReflectCache.get(type);
    }

    public ReflectObject instance(Object instance) {
        this.instance = instance;
        return this;
    }

    public <T> T get(Class<T> type, int index) {
        Field field = this.accessorCache.resolveField(new ReflectQuery(type, index));

        Object obj = Reflect.getField(this.instance, field);
        return obj != null ? type.cast(obj) : null;
    }

    public <T> T get(String name) {
        Field field = this.accessorCache.resolveField(new ReflectQuery(name));

        Object obj = Reflect.getField(this.instance, field);
        return obj != null ? (T) obj : null;
    }

    public void set(Class<?> type, int index, Object value) {
        Field field = this.accessorCache.resolveField(new ReflectQuery(type, index));

        Reflect.setField(this.instance, field, value);
    }

    public void set(String name, Object value) {
        Field field = this.accessorCache.resolveField(new ReflectQuery(name));

        Reflect.setField(this.instance, field, value);
    }

    public <T> T invoke(String name, Object... parameters) {
        Class[] types = new Class[parameters.length];
        for (int i = 0; i < parameters.length; i++) {
            types[i] = parameters[i].getClass();
        }

        Method method = this.accessorCache.resolveMethod(new ReflectQuery(name, types));
        try {
            Object result = method.invoke(this.instance, parameters);
            return result != null ? (T) result : null;
        } catch (ReflectiveOperationException ex) {
            throw new ImanityReflectException(ex);
        }
    }

}
