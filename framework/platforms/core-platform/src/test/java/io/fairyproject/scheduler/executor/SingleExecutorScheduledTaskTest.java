/*
 * MIT License
 *
 * Copyright (c) 2022 Fairy Project
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

package io.fairyproject.scheduler.executor;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.concurrent.Callable;
import java.util.concurrent.ScheduledFuture;

class SingleExecutorScheduledTaskTest {

    private ScheduledFuture<?> scheduledFuture;
    private Callable<String> callable;
    private SingleExecutorScheduledTask<String> singleExecutorScheduledTask;

    @BeforeEach
    void setUp() {
        scheduledFuture = Mockito.mock(ScheduledFuture.class);
        callable = Mockito.mock(Callable.class);
        singleExecutorScheduledTask = new SingleExecutorScheduledTask<>(callable);
        singleExecutorScheduledTask.setScheduledFuture(scheduledFuture);
    }

    @Test
    void testRun() throws Exception {
        Mockito.when(callable.call()).thenReturn("Hello World");

        singleExecutorScheduledTask.run();

        Mockito.verify(callable).call();
        Mockito.verify(scheduledFuture, Mockito.never()).cancel(Mockito.anyBoolean());
        Assertions.assertTrue(singleExecutorScheduledTask.getFuture().isDone());
        Assertions.assertEquals("Hello World", singleExecutorScheduledTask.getFuture().get());
    }

    @Test
    void runOnCancelledShouldNotCallCallable() throws Exception {
        Mockito.when(callable.call()).thenReturn("Hello World");
        singleExecutorScheduledTask.cancel();

        singleExecutorScheduledTask.run();

        Mockito.verify(callable, Mockito.never()).call();
    }

    @Test
    void cancel() {
        singleExecutorScheduledTask.cancel();

        Mockito.verify(scheduledFuture).cancel(false);
        Assertions.assertTrue(singleExecutorScheduledTask.getFuture().isCancelled());
    }

    @Test
    void cancelIfDoneShouldNotCancel() {
        Mockito.when(singleExecutorScheduledTask.getScheduledFuture().isDone()).thenReturn(true);

        singleExecutorScheduledTask.cancel();

        Mockito.verify(scheduledFuture, Mockito.never()).cancel(Mockito.anyBoolean());
    }
}