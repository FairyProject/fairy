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
package io.fairytest.aspect;

import org.hamcrest.CoreMatchers;
import org.hamcrest.MatcherAssert;
import io.fairyproject.Async;
import org.junit.Test;

import java.util.concurrent.*;

public final class AsyncTest {

    @Test
    @SuppressWarnings("PMD.DoNotUseThreads")
    public void executesAsynchronously() throws Exception {
        final BlockingQueue<String> queue = new LinkedBlockingQueue<String>();
        final Runnable runnable = new Runnable() {
            @Async
            @Override
            public void run() {
                queue.offer(Thread.currentThread().getName());
            }
        };
        runnable.run();
        MatcherAssert.assertThat(
            queue.poll(30, TimeUnit.SECONDS),
            CoreMatchers.allOf(
                    CoreMatchers.not(Thread.currentThread().getName()),
                    CoreMatchers.startsWith("imanity-async")
            )
        );
    }

    @Test
    public void executesCompletableFutureValue() throws Exception {
        MatcherAssert.assertThat(
                new Foo().asyncMethodWithCompletableFuture()
                        .get(5, TimeUnit.MINUTES),
                CoreMatchers.allOf(
                        CoreMatchers.not(Thread.currentThread().getName()),
                        CoreMatchers.startsWith("imanity-async")
                )
        );
    }

    @Test
    public void returnsFutureValue() throws Exception {
        MatcherAssert.assertThat(
            new Foo().asyncMethodWithReturnValue()
                .get(5, TimeUnit.MINUTES),
                CoreMatchers.allOf(
                        CoreMatchers.not(Thread.currentThread().getName()),
                        CoreMatchers.startsWith("imanity-async")
            )
        );
    }

    @Test(expected = IllegalStateException.class)
    public void throwsWhenMethodDoesNotReturnVoidOrFuture() {
        new Foo().asyncMethodThatReturnsInt();
    }

    private static final class Foo {

        @Async
        public Future<String> asyncMethodWithReturnValue() {
            return new Future<String>() {
                @Override
                public boolean cancel(final boolean interruptible) {
                    return false;
                }
                @Override
                public boolean isCancelled() {
                    return false;
                }
                @Override
                public boolean isDone() {
                    return true;
                }
                @Override
                public String get() {
                    return Thread.currentThread().getName();
                }
                @Override
                public String get(final long timeout, final TimeUnit unit) {
                    return Thread.currentThread().getName();
                }
            };
        }

        @Async
        public CompletableFuture<String> asyncMethodWithCompletableFuture() {
            return CompletableFuture.completedFuture(Thread.currentThread().getName());
        }

        @Async
        public int asyncMethodThatReturnsInt() {
            return 0;
        }
    }

}
