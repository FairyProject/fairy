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
import io.fairyproject.mock.MockPlugin;
import io.fairyproject.plugin.Plugin;
import io.fairyproject.task.ITaskScheduler;
import io.fairyproject.util.terminable.Terminable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.File;

class ReloaderListenerTest {

    private Plugin plugin;
    private Reloader reloader;
    private AgentDetector agentDetector;
    private ReloaderListener reloaderListener;
    private ITaskScheduler taskScheduler;

    @BeforeEach
    void setUp() {
        plugin = new MockPlugin();
        reloader = Mockito.mock(Reloader.class);
        agentDetector = Mockito.mock(AgentDetector.class);
        taskScheduler = Mockito.mock(ITaskScheduler.class);
        Mockito.when(taskScheduler.runSync(Mockito.any())).then(invocation -> {
            Runnable runnable = invocation.getArgument(0);
            runnable.run();
            return Mockito.mock(Terminable.class);
        });

        reloaderListener = new ReloaderListener(reloader, agentDetector, taskScheduler);
    }

    @Test
    void onClasspathFileChangedMustCallReload() {
        reloaderListener.onClasspathFileChanged(new ClasspathFileChangedEvent(plugin, new File("test")));

        Mockito.verify(reloader).reload(plugin);
    }

    @Test
    void onClasspathFileChangedMustNotCallReloadWhenAgentReloaderIsPresent() {
        Mockito.when(agentDetector.isActive()).thenReturn(true);

        reloaderListener.onClasspathFileChanged(new ClasspathFileChangedEvent(plugin, new File("test")));

        Mockito.verify(reloader, Mockito.never()).reload(plugin);
    }

    @Test
    void onClasspathFileChangedMustMakeSureTheEventIsCalledOnTheMainThread() {
        reloaderListener.onClasspathFileChanged(new ClasspathFileChangedEvent(plugin, new File("test")));

        Mockito.verify(taskScheduler).runSync(Mockito.any());
    }

}