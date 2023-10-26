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

package io.fairyproject.devtools.reload;

import io.fairyproject.plugin.Plugin;
import io.fairyproject.task.ITaskScheduler;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

@Setter
@RequiredArgsConstructor
public class Reloader {

    private final ITaskScheduler taskScheduler;
    private final long quietPeriod;
    private ReloadShutdownHandler reloadShutdownHandler;
    private ReloadStartupHandler reloadStartupHandler;
    @Getter
    private boolean reloadQueued;

    /**
     * Reload the plugin
     *
     * @param plugin The plugin to reload
     * @return true if a plugin reload is queued
     */
    public boolean reload(@NotNull Plugin plugin) {
        if (plugin == null)
            throw new IllegalArgumentException("Plugin must not be null");

        synchronized (this) {
            if (this.reloadQueued)
                return false;
            this.reloadQueued = true;
        }

        taskScheduler.runScheduled(() -> this.doReload(plugin), this.quietPeriod / 50);
        return true;
    }

    private void doReload(Plugin plugin) {
        this.reloadShutdownHandler.shutdown(plugin);
        this.reloadStartupHandler.start(plugin);
    }

}
