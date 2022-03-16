package io.fairyproject.tests;

import io.fairyproject.ExtendedClassLoader;
import io.fairyproject.FairyPlatform;
import io.fairyproject.library.Library;
import io.fairyproject.plugin.PluginManager;
import io.fairyproject.task.ITaskScheduler;
import io.fairyproject.task.async.AsyncTaskScheduler;

import java.io.File;
import java.util.Collection;
import java.util.Collections;

public class FairyTestingPlatform extends FairyPlatform {

    private final Thread thread;
    public FairyTestingPlatform() {
        this.thread = Thread.currentThread();
        if (!PluginManager.isInitialized()) {
            PluginManager.initialize(type -> {
                if (type.getName().startsWith("io.fairytest")) {
                    return "test";
                }
                return null;
            });
        }
    }

    @Override
    public void load() {
        super.load();
    }

    @Override
    public void enable() {
        super.enable();
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
