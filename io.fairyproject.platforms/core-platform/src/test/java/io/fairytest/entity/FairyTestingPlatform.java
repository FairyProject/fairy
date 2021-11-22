package io.fairytest.entity;

import io.fairyproject.ExtendedClassLoader;
import io.fairyproject.FairyPlatform;
import io.fairyproject.library.Library;
import io.fairyproject.task.ITaskScheduler;
import io.fairyproject.task.async.AsyncTaskScheduler;

import java.io.File;
import java.util.Collections;
import java.util.Set;

public class FairyTestingPlatform extends FairyPlatform {

    private final Thread thread;
    public FairyTestingPlatform() {
        this.thread = Thread.currentThread();
    }

    @Override
    public void loadDependencies() {
        // We do not need dependencies here
    }

    @Override
    public ExtendedClassLoader getClassloader() {
        return new ExtendedClassLoader(this.getClass().getClassLoader());
    }

    @Override
    public File getDataFolder() {
        return new File(".");
    }

    @Override
    public Set<Library> getDependencies() {
        return Collections.emptySet();
    }

    @Override
    public void saveResource(String name, boolean replace) {

    }

    @Override
    public void shutdown() {

    }

    @Override
    public boolean isRunning() {
        return true;
    }

    @Override
    public boolean isMainThread() {
        return Thread.currentThread() == this.thread;
    }

    @Override
    public ITaskScheduler createTaskScheduler() {
        return new AsyncTaskScheduler();
    }
}
