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

package io.fairyproject.bukkit.reflection.accessor;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import io.fairyproject.bukkit.reflection.resolver.ResolverQuery;
import io.fairyproject.util.AccessUtil;
import io.fairyproject.util.Utility;
import io.fairyproject.util.exceptionally.ThrowingSupplier;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class ClassAccessorCache {

    private static final LoadingCache<Class<?>, ClassAccessorCache> CLASS_ACCESSORS = CacheBuilder.newBuilder()
            .expireAfterAccess(5, TimeUnit.MINUTES)
            .build(new CacheLoader<Class<?>, ClassAccessorCache>() {
                @Override
                public ClassAccessorCache load(Class<?> key) throws Exception {
                    return new ClassAccessorCache(key);
                }
            });

    public static ClassAccessorCache get(Class<?> parentClass) {
        return ThrowingSupplier.unchecked(() -> CLASS_ACCESSORS.get(parentClass)).get();
    }

    private final Class<?> parentClass;
    private final Map<ResolverQuery, Method> methodCache;
    private final Map<ResolverQuery, Field> fieldCache;

    public ClassAccessorCache(Class<?> parentClass) {
        this.parentClass = parentClass;

        this.methodCache = new ConcurrentHashMap<>();
        this.fieldCache = new ConcurrentHashMap<>();
    }

    public Method resolveMethod(ResolverQuery query) throws ReflectiveOperationException {
        if (methodCache.containsKey(query)) {
            return methodCache.get(query);
        }

        int currentIndex = 0;
        Method result = null;
        for (Method method : this.parentClass.getDeclaredMethods()) {
            if ((query.getReturnType() == null || Utility.wrapPrimitive(query.getReturnType()).equals(Utility.wrapPrimitive(method.getReturnType())))
                    && (query.getName() == null || method.getName().equals(query.getName()))
                    && (query.getModifierOptions() == null || query.getModifierOptions().matches(method.getModifiers()))) {
                if (query.getTypes() != null && query.getTypes().length > 0) {
                    if (!Utility.isParametersEquals(query.getTypes(), method.getParameterTypes())) {
                        continue;
                    }
                }

                if (query.getIndex() == -2) {
                    result = method;
                    continue;
                }

                if (query.getIndex() < 0 || query.getIndex() == currentIndex++) {
                    return this.cache(query, method);
                }
            }
        }
        if (result != null) {
            return this.cache(query, result);
        }

        throw new NoSuchMethodException();
    }

    public Field resolveField(ResolverQuery query) throws ReflectiveOperationException {
        if (fieldCache.containsKey(query)) {
            return fieldCache.get(query);
        }

        int currentIndex = 0;
        Field result = null;
        for (Field field : this.parentClass.getDeclaredFields()) {
            if ((query.getName() == null || field.getName().equals(query.getName()))
                    && (query.getReturnType() == null || Utility.wrapPrimitive(query.getReturnType()).equals(Utility.wrapPrimitive(field.getType())))
                    && (query.getModifierOptions() == null || query.getModifierOptions().matches(field.getModifiers()))) {
                if (query.getTypes() != null && query.getTypes().length > 0) {
                    Type[] genericTypes = Utility.getGenericTypes(field);
                    if (genericTypes == null) {
                        continue;
                    }

                    if (!Utility.isParametersEquals(genericTypes, query.getTypes())) {
                        continue;
                    }
                }

                if (query.getIndex() == -2) {
                    result = field;
                    continue;
                }

                if (query.getIndex() < 0 || query.getIndex() == currentIndex++) {
                    return this.cache(query, field);
                }
            }
        }

        if (result != null) {
            return this.cache(query, result);
        }
        throw new NoSuchFieldException();
    }

    private Method cache(ResolverQuery query, Method method) throws ReflectiveOperationException {
        AccessUtil.setAccessible(method);
        this.methodCache.put(query, method);
        return method;
    }

    private Field cache(ResolverQuery query, Field field) throws ReflectiveOperationException {
        AccessUtil.setAccessible(field);
        this.fieldCache.put(query, field);
        return field;
    }
}
