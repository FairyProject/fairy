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

package io.fairyproject.bukkit.scheduler.folia;

import io.fairyproject.bukkit.scheduler.folia.wrapper.WrapperScheduledTask;
import io.fairyproject.log.Log;
import io.fairyproject.scheduler.ScheduledTask;
import io.fairyproject.scheduler.repeat.RepeatPredicate;
import io.fairyproject.scheduler.response.TaskResponse;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

@RequiredArgsConstructor
public class FoliaRepeatedScheduledTask<R> implements ScheduledTask<R>, Consumer<io.papermc.paper.threadedregions.scheduler.ScheduledTask> {

    private final CompletableFuture<R> future = new CompletableFuture<>();
    private final Callable<TaskResponse<R>> callable;
    private final RepeatPredicate<R> repeatPredicate;

    @Setter
    private WrapperScheduledTask scheduledTask;

    @Override
    public CompletableFuture<R> getFuture() {
        return future;
    }

    @Override
    public void accept(io.papermc.paper.threadedregions.scheduler.ScheduledTask scheduledTask) {
        WrapperScheduledTask wrapperScheduledTask = WrapperScheduledTask.of(scheduledTask);

        try {
            TaskResponse<R> response = callable.call();

            switch (response.getState()) {
                case SUCCESS:
                    future.complete(response.getResult());
                    wrapperScheduledTask.cancel();
                    break;
                case FAILURE:
                    Throwable throwable = response.getThrowable();
                    String errorMessage = response.getErrorMessage();
                    if (throwable != null) {
                        future.completeExceptionally(throwable);
                    } else {
                        future.completeExceptionally(new IllegalStateException(errorMessage));
                    }

                    wrapperScheduledTask.cancel();
                    break;
                case CONTINUE:
                    if (!repeatPredicate.shouldContinue(this)) {
                        future.complete(repeatPredicate.getDefaultValue());
                        wrapperScheduledTask.cancel();
                    }
                    break;
                default:
                    throw new IllegalStateException("Unexpected value: " + response.getState());
            }
        } catch (Exception e) {
            Log.error("An error occurred while executing a scheduled task", e);
            future.completeExceptionally(e);

            wrapperScheduledTask.cancel();
        }
    }

    @Override
    public void cancel() {
        future.cancel(false);
        scheduledTask.cancel();
    }

}
