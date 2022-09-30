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

package io.fairyproject.task;

import io.fairyproject.internal.Process;
import io.fairyproject.util.terminable.Terminable;

public interface ITaskScheduler extends Process {

    Terminable runAsync(Runnable runnable);

    Terminable runAsyncScheduled(Runnable runnable, long time);

    Terminable runAsyncRepeated(TaskRunnable runnable, long time);

    Terminable runAsyncRepeated(TaskRunnable runnable, long delay, long time);

    default Terminable runAsyncRepeated(Runnable runnable, long time) {
        return this.runAsyncRepeated(t -> runnable.run(), time);
    }

    default Terminable runAsyncRepeated(Runnable runnable, long delay, long time) {
        return this.runAsyncRepeated(t -> runnable.run(), delay, time);
    }

    Terminable runSync(Runnable runnable);

    Terminable runScheduled(Runnable runnable, long time);

    Terminable runRepeated(TaskRunnable runnable, long time);

    Terminable runRepeated(TaskRunnable runnable, long delay, long time);

    default Terminable runRepeated(Runnable runnable, long time) {
        return this.runRepeated(t -> runnable.run(), time);
    }

    default Terminable runRepeated(Runnable runnable, long delay, long time) {
        return this.runRepeated(t -> runnable.run(), delay, time);
    }

    default Terminable runSync(Runnable runnable, Object identifier) {
        return this.runSync(runnable);
    }

    default Terminable runScheduled(Runnable runnable, Object identifier, long time) {
        return this.runScheduled(runnable, time);
    }

    default Terminable runRepeated(TaskRunnable runnable, Object identifier, long time) {
        return this.runRepeated(runnable, time);
    }

    default Terminable runRepeated(TaskRunnable runnable, Object identifier, long delay, long time) {
        return this.runRepeated(runnable, delay, time);
    }

}
