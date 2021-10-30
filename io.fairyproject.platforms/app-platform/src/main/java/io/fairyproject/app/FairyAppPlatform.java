package io.fairyproject.app;

import io.fairyproject.ExtendedClassLoader;
import io.fairyproject.FairyPlatform;
import io.fairyproject.app.task.AsyncTaskScheduler;
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
    public void saveResource(String name, boolean replace) {
        if (name != null && !name.equals("")) {
            name = name.replace('\\', '/');
            InputStream in = this.getResource(name);
            if (in == null) {
                throw new IllegalArgumentException("The embedded resource '" + name + "' cannot be found");
            } else {
                File outFile = new File(this.getDataFolder(), name);
                int lastIndex = name.lastIndexOf(47);
                File outDir = new File(this.getDataFolder(), name.substring(0, Math.max(lastIndex, 0)));
                if (!outDir.exists()) {
                    outDir.mkdirs();
                }

                try {
                    if (outFile.exists() && !replace) {
                        LOGGER.warn("Could not save " + outFile.getName() + " to " + outFile + " because " + outFile.getName() + " already exists.");
                    } else {
                        OutputStream out = new FileOutputStream(outFile);
                        byte[] buf = new byte[1024];

                        int len;
                        while((len = in.read(buf)) > 0) {
                            out.write(buf, 0, len);
                        }

                        out.close();
                        in.close();
                    }
                } catch (IOException var10) {
                    LOGGER.info("Could not save " + outFile.getName() + " to " + outFile, var10);
                }

            }
        } else {
            throw new IllegalArgumentException("ResourcePath cannot be null or empty");
        }
    }

    public InputStream getResource(String filename) {
        if (filename == null) {
            throw new IllegalArgumentException("Filename cannot be null");
        } else {
            try {
                URL url = this.getClass().getClassLoader().getResource(filename);
                if (url == null) {
                    return null;
                } else {
                    URLConnection connection = url.openConnection();
                    connection.setUseCaches(false);
                    return connection.getInputStream();
                }
            } catch (IOException var4) {
                return null;
            }
        }
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
