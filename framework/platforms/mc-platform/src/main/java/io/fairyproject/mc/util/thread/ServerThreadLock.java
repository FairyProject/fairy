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

package io.fairyproject.mc.util.thread;

import io.fairyproject.container.Containers;
import io.fairyproject.mc.scheduler.MCScheduler;
import io.fairyproject.mc.scheduler.MCSchedulerProvider;

/**
 * A tool to synchronize code with the main server thread
 *
 * <p>It is highly recommended to use this interface with try-with-resource blocks.</p>
 */
public interface ServerThreadLock extends AutoCloseable {

    /**
     * Blocks the current thread until a {@link ServerThreadLock} can be obtained.
     *
     * <p>Will attempt to return immediately if the calling thread is the main thread itself.</p>
     *
     * @return a lock
     */
    static ServerThreadLock obtain() {
        MCSchedulerProvider mcSchedulerProvider = Containers.get(MCSchedulerProvider.class);
        MCScheduler globalScheduler = mcSchedulerProvider.getGlobalScheduler();

        return new ServerThreadLockImpl(globalScheduler);
    }

    /**
     * Closes the lock, and allows the main thread to continue
     */
    @Override
    void close();

}
