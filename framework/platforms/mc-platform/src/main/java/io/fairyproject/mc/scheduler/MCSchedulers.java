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

package io.fairyproject.mc.scheduler;

import io.fairyproject.container.Autowired;
import io.fairyproject.mc.MCWorld;
import io.fairyproject.mc.util.Position;
import lombok.experimental.UtilityClass;

/**
 * Avoid using this class as much as you can.
 * This provides static methods to get the scheduler.
 * But it is recommended to have them injected.
 */
@UtilityClass
public class MCSchedulers {

    @Autowired
    private MCSchedulerProvider mcSchedulerProvider;

    public MCScheduler getGlobalScheduler() {
        return mcSchedulerProvider.getGlobalScheduler();
    }

    public MCScheduler getAsyncScheduler() {
        return mcSchedulerProvider.getAsyncScheduler();
    }

    public MCScheduler getEntityScheduler(Object entity) {
        return mcSchedulerProvider.getEntityScheduler(entity);
    }

    public MCScheduler getLocationScheduler(Position position) {
        return mcSchedulerProvider.getLocationScheduler(position);
    }

    public MCScheduler getChunkScheduler(MCWorld world, int chunkX, int chunkZ) {
        return mcSchedulerProvider.getChunkScheduler(world, chunkX, chunkZ);
    }

}
