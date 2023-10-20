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

import io.fairyproject.Fairy;
import io.fairyproject.FairyPlatform;
import io.fairyproject.container.ContainerContext;
import io.fairyproject.container.InjectableComponent;
import io.fairyproject.container.configuration.Configuration;
import io.fairyproject.devtools.DevToolProperties;
import io.fairyproject.devtools.DevToolSettings;
import io.fairyproject.devtools.reload.impl.DefaultReloadShutdownHandler;
import io.fairyproject.devtools.reload.impl.DefaultReloadStartupHandler;
import io.fairyproject.devtools.watcher.ClasspathFileAlterationListener;
import io.fairyproject.devtools.watcher.ClasspathFileWatcher;

@Configuration
public class ReloaderConfiguration {

    @InjectableComponent
    public Reloader reloader(ContainerContext context) {
        Reloader reloader = new Reloader();
        reloader.setReloadStartupHandler(new DefaultReloadStartupHandler(context));
        reloader.setReloadShutdownHandler(new DefaultReloadShutdownHandler(context.nodeDestroyer()));

        return reloader;
    }

    @InjectableComponent
    public ClasspathFileWatcher classpathFileWatcher(DevToolSettings settings) {
        return new ClasspathFileWatcher(settings.getRestart().getClasspathScanInterval().toMillis());
    }

    @InjectableComponent
    public ReloaderListener reloaderListener(Reloader reloader, AgentDetector agentDetector) {
        return new ReloaderListener(reloader, agentDetector, Fairy.getTaskScheduler());
    }

    @InjectableComponent
    public ReloaderPluginListener reloaderPluginListener(ClasspathFileWatcher classpathFileWatcher) {
        return new ReloaderPluginListener(classpathFileWatcher, DevToolProperties.getClasspathCollection());
    }

}
