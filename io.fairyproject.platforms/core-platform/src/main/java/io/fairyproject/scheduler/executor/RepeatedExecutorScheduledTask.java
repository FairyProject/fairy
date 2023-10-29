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

import java.util.concurrent.Callable;

public class RepeatedExecutorScheduledTask<R> extends ExecutorScheduledTask<R> {

    private final Callable<TaskResponse<R>> callable;

    public RepeatedExecutorScheduledTask(Callable<TaskResponse<R>> callable) {
        this.callable = callable;
    }

    @Override
    public void run() {
        if (cancelled.get())
            return;

        try {
            TaskResponse<R> result = callable.call();

            switch (result.getState()) {
                case SUCCESS:
                    future.complete(result.getResult());
                    scheduledFuture.cancel(false);
                    break;
                case FAILURE:
                    if (result.getThrowable() != null) {
                        future.completeExceptionally(result.getThrowable());
                    } else {
                        future.completeExceptionally(new IllegalStateException(result.getErrorMessage()));
                    }
                    scheduledFuture.cancel(false);
                    break;
                case CONTINUE:
                    break;
            }
        } catch (Exception e) {
            future.completeExceptionally(e);
        }
    }

}
