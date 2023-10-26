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

import io.fairyproject.mock.MockPlugin;
import io.fairyproject.plugin.Plugin;
import io.fairyproject.task.ITaskScheduler;
import io.fairyproject.task.async.AsyncTaskScheduler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;

class ReloaderTest {

    private Reloader reloader;
    private Plugin plugin;
    private ReloadShutdownHandler reloadShutdownHandler;
    private ReloadStartupHandler reloadStartupHandler;

    @BeforeEach
    void setUp() {
        ITaskScheduler taskScheduler = new AsyncTaskScheduler();

        reloader = new Reloader(taskScheduler, 100);
        plugin = new MockPlugin();
        reloadShutdownHandler = Mockito.mock(ReloadShutdownHandler.class);
        reloadStartupHandler = Mockito.mock(ReloadStartupHandler.class);

        reloader.setReloadShutdownHandler(reloadShutdownHandler);
        reloader.setReloadStartupHandler(reloadStartupHandler);
    }

    @Nested
    class Reload {

        @Test
        void reloadPluginMustNotBeNull() {
            assertThrows(IllegalArgumentException.class, () -> reloader.reload(null));
        }

        @Test
        void reloadShouldCallShutdownAndStartupHandler() throws InterruptedException {
            boolean queued = reloader.reload(plugin);

            assertTrue(queued);
            assertTrue(reloader.isReloadQueued());

            Mockito.verify(reloadShutdownHandler, Mockito.never()).shutdown(plugin);
            Mockito.verify(reloadStartupHandler, Mockito.never()).start(plugin);

            Thread.sleep(200);

            Mockito.verify(reloadShutdownHandler, Mockito.times(1)).shutdown(plugin);
            Mockito.verify(reloadStartupHandler, Mockito.times(1)).start(plugin);
        }

        @Test
        void reloadShouldIgnoreNewRequests() throws InterruptedException {
            boolean aQueued = reloader.reload(plugin);
            boolean bQueued = reloader.reload(plugin);

            assertTrue(aQueued);
            assertFalse(bQueued);
            assertTrue(reloader.isReloadQueued());

            Thread.sleep(200);

            Mockito.verify(reloadShutdownHandler, Mockito.times(1)).shutdown(plugin);
            Mockito.verify(reloadStartupHandler, Mockito.times(1)).start(plugin);
        }

    }

}