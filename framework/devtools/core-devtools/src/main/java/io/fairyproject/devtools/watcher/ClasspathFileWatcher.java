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

package io.fairyproject.devtools.watcher;

import io.fairyproject.container.PostDestroy;
import io.fairyproject.container.PostInitialize;
import lombok.Getter;
import org.apache.commons.io.monitor.FileAlterationListener;
import org.apache.commons.io.monitor.FileAlterationMonitor;
import org.apache.commons.io.monitor.FileAlterationObserver;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class ClasspathFileWatcher {

    @Getter
    private final FileAlterationMonitor monitor;
    private final Map<String, FileAlterationObserver> observers;
    private boolean running = false;

    public ClasspathFileWatcher(long interval) {
        this.monitor = new FileAlterationMonitor(interval);
        this.observers = new HashMap<>();
    }

    public void addURL(URL url, FileAlterationListener listener) throws URISyntaxException {
        File file = new File(url.toURI());
        if (!file.exists())
            return;

        if (!file.isDirectory())
            return;

        FileAlterationObserver observer = new FileAlterationObserver(file);
        observer.addListener(listener);
        this.monitor.addObserver(observer);
        this.observers.put(url.toString(), observer);
    }

    public boolean isStarted() {
        return this.running;
    }

    public Iterable<FileAlterationObserver> getObservers() {
        return this.monitor.getObservers();
    }

    @PostInitialize
    public void start() throws Exception {
        this.monitor.start();
        this.running = true;
    }

    @PostDestroy
    public void stop() throws Exception {
        this.monitor.stop();
        this.running = false;
    }

    public void removeURL(URL url) {
        FileAlterationObserver observer = this.observers.remove(url.toString());
        if (observer == null)
            return;

        this.monitor.removeObserver(observer);
    }
}
