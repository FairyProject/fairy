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

import java.lang.annotation.*;

/**
 * Marks a method for asynchronous execution.
 *
 * <p>Add this annotation to the method you want to execute asynchronously.
 * Methods of return type {@code void} and {@link java.util.concurrent.Future}
 * are supported. In the latter case, an actual asynchronous Future will be
 * returned, but the target method should return a temporary {@code Future}
 * that passes the value through as the return type needs to be the same.
 *
 * <p>Usage with other return types may cause unexpected behavior (because
 * {@code NULL} will always be returned).
 *
 * <p>Keep in mind that there is a limited number of threads working with
 * methods annotated with <code>@Async</code>. Thus, if one of your
 * methods keep a thread busy for a long time, others will wait. Try to
 * make all methods fast, when you annotate them with <code>@Async</code>.
 *
 * @author Carlos Miranda (miranda.cma@gmail.com)
 * @version $Id: fdd7dbd67ba6c4777048aaf7c2cdcecdd17768dc $
 * @see <a href="http://aspects.jcabi.com">http://aspects.jcabi.com/</a>
 * @since 0.16
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Async {
}
