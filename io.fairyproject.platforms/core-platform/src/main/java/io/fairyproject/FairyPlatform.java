/*
 * MIT License
 *
 * Copyright (c) 2021 Imanity
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

package io.fairyproject;

import io.fairyproject.aspect.AsyncAspect;
import io.fairyproject.cache.CacheableAspect;
import io.fairyproject.container.ContainerContext;
import io.fairyproject.container.object.SimpleContainerObject;
import io.fairyproject.library.Library;
import io.fairyproject.library.LibraryHandler;
import io.fairyproject.plugin.PluginManager;
import io.fairyproject.task.ITaskScheduler;
import io.fairyproject.util.terminable.composite.CompositeClosingException;
import io.fairyproject.util.terminable.composite.CompositeTerminable;
import lombok.Getter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;

@Getter
public abstract class FairyPlatform {

    public static final Logger LOGGER = LogManager.getLogger(FairyPlatform.class);

    public static FairyPlatform INSTANCE;
    private final AtomicBoolean loadedDependencies = new AtomicBoolean();

    private ITaskScheduler taskScheduler;
    private CompositeTerminable compositeTerminable;

    private LibraryHandler libraryHandler;
    private ContainerContext containerContext;

    public void load() {
        this.loadDependencies();

        this.taskScheduler = this.createTaskScheduler();
        this.compositeTerminable = CompositeTerminable.create();
    }

    public void enable() {
        this.loadBindable();

        this.containerContext = new ContainerContext();
        this.containerContext.registerObject(new SimpleContainerObject(this, this.getClass()));
        this.containerContext.init();
    }

    public void disable() {
        try {
            this.compositeTerminable.close();
        } catch (CompositeClosingException ex) {
            ex.printStackTrace();
        }

        this.containerContext.stop();
        PluginManager.INSTANCE.callFrameworkFullyDisable();
    }

    private void loadBindable() {
        this.bind(AsyncAspect.EXECUTOR);
//        this.bind(CacheableAspect.CLEANER_SERVICE);
//        this.bind(CacheableAspect.UPDATER_SERVICE);
    }

    public void loadDependencies() {
        if (!this.loadedDependencies.compareAndSet(false, true)) {
            return;
        }

        LOGGER.info("Loading Fairy Dependencies...");
        this.libraryHandler = new LibraryHandler();

        List<Library> dependencies = new ArrayList<>(this.getDependencies());
        dependencies.add(Library.CAFFEINE);
        dependencies.add(Library.SPRING_CORE);
        dependencies.add(Library.SPRING_EL);

        this.libraryHandler.downloadLibraries(true, dependencies);

    }

    public <T extends AutoCloseable> T bind(T t) {
        return this.compositeTerminable.bind(t);
    }

    public AutoCloseable bind(ExecutorService executorService) {
        return this.bind(executorService::shutdown);
    }

    /**
     * get Plugin Class Loader
     *
     * @return Plugin Class Loader
     */
    public abstract ExtendedClassLoader getClassloader();

    /**
     * get Fairy Folder
     *
     * @return Fairy Folder
     */
    public abstract File getDataFolder();

    /**
     * get Dependencies based on platforms
     *
     * @return Dependencies Set
     */
    public abstract Collection<Library> getDependencies();

    /**
     * on Post Services Initial
     *
     */
    public void onPostServicesInitial() {

    }

    /**
     * get Class Loader Name
     *
     * @param classLoader Class Loader
     * @return Name
     * @throws Exception Any error during the process
     */
    @Nullable
    public String getClassLoaderName(ClassLoader classLoader) throws Exception {
        return null;
    }

    /**
     * Save Resource File to Fairy Folder
     *
     * @param name the Name
     * @param replace should Replace File
     */
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

    /**
     * Shutdown Fairy
     *
     */
    public abstract void shutdown();

    /**
     * is Fairy Running
     *
     * @return is Running
     */
    public abstract boolean isRunning();

    /**
     * is Main Thread
     *
     * @return is Main Thread
     */
    public abstract boolean isMainThread();

    /**
     * Create Task Scheduler for Fairy Core to schedule tasks with built-in APIs from each platforms
     *
     * @return the Task Scheduler
     */
    public abstract ITaskScheduler createTaskScheduler();

}
