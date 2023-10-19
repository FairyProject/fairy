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

import io.fairyproject.devtools.watcher.ClasspathFileChangedEvent;
import io.fairyproject.event.Subscribe;
import io.fairyproject.plugin.Plugin;
import io.fairyproject.task.ITaskScheduler;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ReloaderListener {

    private final Reloader reloader;
    private final AgentDetector agentDetector;
    private final ITaskScheduler taskScheduler;

    @Subscribe
    public void onClasspathFileChanged(ClasspathFileChangedEvent event) {
        Plugin plugin = event.getPlugin();

        if (this.agentDetector.isActive())
            // We use agent reloader, so we don't need to reload the plugin
            return;

        taskScheduler.runSync(() -> this.reloader.reload(plugin));
    }

}
