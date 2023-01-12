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

package io.fairyproject.config.filter;

import io.fairyproject.config.annotation.NestedConfig;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.util.*;
import java.util.function.Predicate;

import static java.util.stream.Collectors.toList;

@FunctionalInterface
public interface FieldFilter extends Predicate<Field> {

    @Override
    default FieldFilter and(@NotNull Predicate<? super Field> other) {
        Objects.requireNonNull(other);
        return (t) -> test(t) && other.test(t);
    }

    default List<? extends Field> filterDeclaredFieldsOf(Class<?> cls) {
        // Allow checking for nested classes
        final NestedConfig annotation = cls.getAnnotation(NestedConfig.class);
        final List<Class<?>> accepted;
        if (annotation == null) {
            accepted = Collections.emptyList();
        } else {
            accepted = Arrays.asList(annotation.value());
        }

        List<Field> fields = new ArrayList<>();
        Class<?> current = cls;
        do {
            if (current == cls || accepted.contains(current)) {
                fields.addAll(Arrays.asList(current.getDeclaredFields()));
            }
            current = current.getSuperclass();
        } while (current != Object.class && current != null);

        return fields.stream()
                .filter(this)
                .collect(toList());
    }
}
