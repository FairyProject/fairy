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

import org.fairy.config.format.FieldNameFormatter;
import org.fairy.config.format.FieldNameFormatters;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that a specific {@code FieldNameFormatter} is used. If a
 * {@link #formatterClass()} is specified, the {@code FieldNameFormatters}
 * returned by {@link #value()} is ignored. The {@code formatterClass} must
 * be instantiable and must have a no-args constructor.
 * <p>
 * This annotation takes precedence over the value set in properties object.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Format {
    /**
     * Returns the {@code FieldNameFormatters} instance to be for formatting.
     * The return value of this method is ignored if a {@code formatterClass()}
     * is set.
     *
     * @return {@code FieldNameFormatters} instance
     */
    FieldNameFormatters value() default FieldNameFormatters.IDENTITY;

    /**
     * Returns the class of a {@code FieldNameFormatter} implementation.
     * The class must be instantiable and must have a no-args constructor.
     *
     * @return class of {@code FieldNameFormatter} implementation
     */
    Class<? extends FieldNameFormatter> formatterClass() default FieldNameFormatter.class;
}
