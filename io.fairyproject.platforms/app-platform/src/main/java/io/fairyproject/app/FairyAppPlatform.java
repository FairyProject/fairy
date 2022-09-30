package io.fairyproject.app;

import io.fairyproject.Debug;
import io.fairyproject.FairyPlatform;
import io.fairyproject.PlatformType;
import io.fairyproject.app.logger.TinyLogger;
import io.fairyproject.log.Log;
import io.fairyproject.plugin.Plugin;
import io.fairyproject.plugin.PluginHandler;
import io.fairyproject.plugin.PluginManager;
import io.fairyproject.task.ITaskScheduler;
import io.fairyproject.task.async.AsyncTaskScheduler;
import io.fairyproject.util.URLClassLoaderAccess;
import io.fairyproject.util.exceptionally.ThrowingRunnable;
import lombok.Getter;
import org.tinylog.provider.ProviderRegistry;

import java.io.File;
import java.net.JarURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarFile;

public class FairyAppPlatform extends FairyPlatform {

    private final List<Class<?>> appClasses = new ArrayList<>();
    private final URLClassLoaderAccess classLoader;
    private final Thread mainThread;
    private boolean running;

    @Getter
    private Application mainApplication;

    private final Object shutdownLock = new Object();
    private boolean shuttingDown;

    public FairyAppPlatform() {
        FairyPlatform.INSTANCE = this;
        this.classLoader = URLClassLoaderAccess.create((URLClassLoader) this.getClass().getClassLoader());
        this.mainThread = Thread.currentThread();
        this.running = true;

        if (!Debug.UNIT_TEST)
            Log.set(new TinyLogger());
    }

    @Override
    public void enable() {
        super.enable();
    }

    public void setMainApplication(Application mainApplication) {
        this.mainApplication = mainApplication;
        ThrowingRunnable.sneaky(() -> {
            final URL url = mainApplication.getClass().getProtectionDomain().getCodeSource().getLocation();
            File file;
            try {
                file = new File(url.toURI());
            } catch (IllegalArgumentException ex) {
                file = new File(((JarURLConnection) url.openConnection()).getJarFileURL().toURI());
            } catch (URISyntaxException ex) {
                file = new File(url.getPath());
            }

            new JarFile(file).stream()
                    .filter(it -> it.getName().endsWith(".class"))
                    .forEach(it -> {
                        try {
                            this.appClasses.add(Class.forName(it.getName().replace("/", ".").substring(0, it.getName().length() - 6), false, mainApplication.getClassLoader()));
                        } catch (Throwable ignored) {

                        }
                    });
        }).run();
    }

    public boolean isAppClass(Class<?> type) {
        return this.appClasses.contains(type);
    }

    @Override
    public URLClassLoaderAccess getClassloader() {
        return this.classLoader;
    }

    @Override
    public File getDataFolder() {
        return new File("fairy");
    }

    @Override
    public void shutdown() {
        synchronized (this.shutdownLock) {
            if (this.shuttingDown) {
                return;
            }
            this.shuttingDown = true;
        }
        Log.info("Shutting down...");

        if (this.mainApplication != null) {
            try {
                this.mainApplication.close();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    ProviderRegistry.getLoggingProvider().shutdown();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        this.running = false;
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

    @Override
    public PluginHandler createPluginHandler() {
        return new AppPluginHandler();
    }

    @Override
    public PlatformType getPlatformType() {
        return PlatformType.APP;
    }
}
