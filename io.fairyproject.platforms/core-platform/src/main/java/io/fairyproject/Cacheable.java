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

package io.fairyproject;

import org.intellij.lang.annotations.Language;

import java.lang.annotation.*;
import java.util.concurrent.TimeUnit;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Cacheable {

    /**
     * Lifetime of an object in cache, in time units.
     */
    int lifetime() default 1;

    /**
     * Time units of object lifetime.
     *
     * <p>The minimum unit you can use is a second. We simply can't cache for
     * less than a second, because cache is being cleaned every second.
     */
    TimeUnit unit() default TimeUnit.MINUTES;

    /**
     * Keep in cache forever.
     */
    boolean forever() default false;

    /**
     * Storing the key of the cacheable
     *
     */
    @Language("SpEL") String key() default "";

    /**
     * Don't store if condition is false
     *
     */
    @Language("SpEL") String condition() default "";

    /**
     * Prevent to store value if one of argument were null
     *
     */
    boolean preventArgumentNull() default true;

    /**
     * Identifies a method that should flush all cached entities of
     * this class/object, before being executed.
     * @since 0.7.18
     */
    @Documented
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    @interface ClearBefore {
    }

    /**
     * Identifies a method that should flush all cached entities of
     * this class/object, after being executed.
     * @since 0.7.18
     */
    @Documented
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    @interface ClearAfter {
    }

}
