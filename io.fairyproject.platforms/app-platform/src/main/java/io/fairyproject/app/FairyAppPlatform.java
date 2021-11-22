package io.fairyproject.app;

import io.fairyproject.ExtendedClassLoader;
import io.fairyproject.FairyPlatform;
import io.fairyproject.task.async.AsyncTaskScheduler;
import io.fairyproject.library.Library;
import io.fairyproject.task.ITaskScheduler;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.Set;

public class FairyAppPlatform extends FairyPlatform {

    private final ExtendedClassLoader classLoader;
    private final Thread mainThread;
    private boolean running;

    private Application mainApplication;

    public FairyAppPlatform() {
        this.classLoader = new ExtendedClassLoader(this.getClass().getClassLoader());
        this.mainThread = Thread.currentThread();
        this.running = true;

        Runtime.getRuntime().addShutdownHook(new Thread(this::shutdown));
    }

    public void setMainApplication(Application mainApplication) {
        this.mainApplication = mainApplication;
    }

    @Override
    public ExtendedClassLoader getClassloader() {
        return this.classLoader;
    }

    @Override
    public File getDataFolder() {
        return new File(".");
    }

    @Override
    public Set<Library> getDependencies() {
        return null;
    }

    @Override
    public void shutdown() {
        this.running = false;

        if (this.mainApplication != null) {
            try {
                this.mainApplication.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        System.exit(-1);
    }

    @Override
    public boolean isRunning() {
        return this.running;
    }

    @Override
    public boolean isMainThread() {
        return this.mainThread == Thread.currentThread();
    }

    @Override
    public ITaskScheduler createTaskScheduler() {
        return new AsyncTaskScheduler();
    }
}
