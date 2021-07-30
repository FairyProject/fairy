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

package org.fairy.task;

import lombok.experimental.UtilityClass;
import org.fairy.Fairy;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Supplier;

@UtilityClass
public class Task {

    private Thread MAIN;

    public Executor main() {
        return main(false);
    }

    public Executor main(boolean forceMain) {
        return run -> {
            if (Thread.currentThread() != MAIN || forceMain) {
                Fairy.getTaskScheduler().runSync(() -> {
                    MAIN = Thread.currentThread();
                    run.run();
                });
            } else {
                run.run();
            }
        };
    }

    public <T> CompletableFuture<T> supplyMain(Supplier<T> supplier) {
        return CompletableFuture.supplyAsync(supplier, main());
    }

    public CompletableFuture<Void> runMain(Runnable runnable) {
        return CompletableFuture.runAsync(runnable, main());
    }

    public Executor async() {
        return Fairy.getTaskScheduler()::runAsync;
    }

    public Executor mainLater(long ticks) {
        return run -> Fairy.getTaskScheduler().runScheduled(() -> {
            MAIN = Thread.currentThread();
            run.run();
        }, ticks);
    }

    public Executor asyncLater(long ticks) {
        return run -> Fairy.getTaskScheduler().runAsyncScheduled(run, ticks);
    }

    public <T> CompletableFuture<T> supplyAsync(Supplier<T> supplier) {
        return CompletableFuture.supplyAsync(supplier, async());
    }

    public CompletableFuture<Void> runAsync(Runnable runnable) {
        return CompletableFuture.runAsync(runnable, async());
    }

}
