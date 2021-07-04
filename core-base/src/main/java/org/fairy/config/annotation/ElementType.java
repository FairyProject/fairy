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

package org.fairy.config.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates the type of elements a {@code Collection} or {@code Map} contains.
 * <p>
 * This annotation must only be used if a {@code Collection} or {@code Map} contains
 * elements whose type is not simple. Note that {@code Map} keys can only be of some
 * simple type.
 * <p>
 * If collections are nested, the {@code nestingLevel} must be set. Examples:
 * <ul>
 * <li>nestingLevel 1: {@code List<List<T>>}</li>
 * <li>nestingLevel 1: {@code List<Set<T>>}</li>
 * <li>nestingLevel 1: {@code List<Map<String, T>>}</li>
 * <li>nestingLevel 2: {@code List<List<List<T>>>}</li>
 * <li>nestingLevel 2: {@code List<Set<List<T>>>}</li>
 * <li>nestingLevel 2: {@code List<List<Map<String, T>>>}</li>
 * </ul>
 */
@Target(java.lang.annotation.ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ElementType {
    /**
     * Returns the type of elements a {@code Collection} or {@code Map} contains.
     *
     * @return type of elements.
     */
    Class<?> value();

    /**
     * Returns the nesting level
     *
     * @return nesting level
     */
    int nestingLevel() default 0;
}
