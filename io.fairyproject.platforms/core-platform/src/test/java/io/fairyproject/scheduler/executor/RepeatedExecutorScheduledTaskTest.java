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

import io.fairyproject.scheduler.response.TaskResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class RepeatedExecutorScheduledTaskTest {

    private ScheduledFuture<String> scheduledFuture;
    private Callable<TaskResponse<String>> callable;
    private RepeatedExecutorScheduledTask<String> repeatedExecutorScheduledTask;

    @BeforeEach
    void setUp() {
        scheduledFuture = Mockito.mock(ScheduledFuture.class);
        Callable<TaskResponse<String>> callable = () -> this.callable.call();
        repeatedExecutorScheduledTask = new RepeatedExecutorScheduledTask<>(callable);
        repeatedExecutorScheduledTask.setScheduledFuture(scheduledFuture);
    }

    @Test
    void testRunSuccess() throws Exception {
        callable = () -> TaskResponse.success("Hello World");

        repeatedExecutorScheduledTask.run();

        Mockito.verify(scheduledFuture).cancel(Mockito.anyBoolean());
        assertTrue(repeatedExecutorScheduledTask.getFuture().isDone());
        assertEquals("Hello World", repeatedExecutorScheduledTask.getFuture().get());
    }

    @Test
    void testRunFailure() {
        callable = () -> TaskResponse.failure(new Exception("Hello World"));

        repeatedExecutorScheduledTask.run();

        Mockito.verify(scheduledFuture).cancel(Mockito.anyBoolean());
        assertTrue(repeatedExecutorScheduledTask.getFuture().isCompletedExceptionally());
        assertThrows(Exception.class, () -> repeatedExecutorScheduledTask.getFuture().get());
    }

    @Test
    void testRunFailureWithoutException() {
        callable = () -> TaskResponse.failure("Hello World");

        repeatedExecutorScheduledTask.run();

        Mockito.verify(scheduledFuture).cancel(Mockito.anyBoolean());
        assertTrue(repeatedExecutorScheduledTask.getFuture().isCompletedExceptionally());
        assertThrows(ExecutionException.class, () -> repeatedExecutorScheduledTask.getFuture().get());
    }

    @Test
    void testRunContinue() {
        callable = TaskResponse::continueTask;

        repeatedExecutorScheduledTask.run();

        Mockito.verify(scheduledFuture, Mockito.never()).cancel(Mockito.anyBoolean());
        assertFalse(repeatedExecutorScheduledTask.getFuture().isDone());
    }

    @Test
    void testFullRun() throws ExecutionException, InterruptedException {
        AtomicInteger counter = new AtomicInteger(0);
        callable = () -> {
            if (counter.incrementAndGet() < 5) {
                return TaskResponse.continueTask();
            } else {
                return TaskResponse.success("Hello World");
            }
        };

        while (counter.get() < 5) {
            Mockito.verify(scheduledFuture, Mockito.never()).cancel(Mockito.anyBoolean());
            assertFalse(repeatedExecutorScheduledTask.getFuture().isDone());

            repeatedExecutorScheduledTask.run();
        }

        Mockito.verify(scheduledFuture).cancel(false);
        assertTrue(repeatedExecutorScheduledTask.getFuture().isDone());
        assertEquals("Hello World", repeatedExecutorScheduledTask.getFuture().get());
    }

}