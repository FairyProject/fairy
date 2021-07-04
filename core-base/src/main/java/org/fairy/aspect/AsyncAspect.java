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

package org.fairy.aspect;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.fairy.util.Stacktrace;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@Aspect
public final class AsyncAspect {

    public static final ExecutorService EXECUTOR =
            Executors.newCachedThreadPool(
                    new ThreadFactoryBuilder()
                        .setDaemon(true)
                        .setNameFormat("imanity-async-%d")
                        .setUncaughtExceptionHandler((thread, throwable) -> Stacktrace.print(throwable))
                    .build()
            );

    @Around("execution(@org.fairy.Async * * (..))")
    public Object wrap(final ProceedingJoinPoint point) {
        final Class<?> returned = ((MethodSignature) point.getSignature()).getMethod().getReturnType();

        if (!Future.class.isAssignableFrom(returned) && !returned.equals(Void.TYPE)) {
            throw new IllegalStateException(
                    String.format(
                            "%s: Return type is %s, not void or Future, cannot use @Async",
                            point.toShortString(),
                            returned.getCanonicalName()
                    )
            );
        }

        final Future<?> future = EXECUTOR.submit(() -> {
                    Object returned1 = null;
                    try {
                        final Object result1 = point.proceed();
                        if (result1 instanceof Future) {
                            returned1 = ((Future<?>) result1).get();
                        }
                    } catch (final Throwable ex) {
                        throw new IllegalStateException(
                                String.format("%s: Exception thrown", point.toShortString()),
                                ex
                        );
                    }
                    return returned1;
                }
        );
        Object resultObject = null;
        if (Future.class.isAssignableFrom(returned)) {
            resultObject = future;
        }
        return resultObject;
    }

}
