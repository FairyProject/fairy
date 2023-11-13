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

package io.fairyproject.scheduler.response;

import io.fairyproject.scheduler.TaskState;
import org.jetbrains.annotations.Nullable;

public interface TaskResponse<R> {

    static <R> TaskResponse<R> success(R result) {
        return new SuccessTaskResponse<>(result);
    }

    static <R> TaskResponse<R> failure(Throwable throwable) {
        return new FailureTaskResponse<>(throwable, throwable.getMessage());
    }

    static <R> TaskResponse<R> failure(String errorMessage) {
        return new FailureTaskResponse<>(null, errorMessage);
    }

    static <R> TaskResponse<R> failure(Throwable throwable, String errorMessage) {
        return new FailureTaskResponse<>(throwable, errorMessage);
    }

    @SuppressWarnings("unchecked")
    static <R> TaskResponse<R> continueTask() {
        return (TaskResponse<R>) ContinueTaskResponse.INSTANCE;
    }

    TaskState getState();

    @Nullable Throwable getThrowable();

    @Nullable String getErrorMessage();

    @Nullable R getResult();

}
