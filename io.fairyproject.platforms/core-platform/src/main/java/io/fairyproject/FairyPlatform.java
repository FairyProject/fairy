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

import io.fairyproject.container.ContainerContext;
import io.fairyproject.container.object.ContainerObj;
import io.fairyproject.event.GlobalEventNode;
import io.fairyproject.internal.Process;
import io.fairyproject.internal.ProcessManager;
import io.fairyproject.jackson.JacksonService;
import io.fairyproject.library.LibraryHandler;
import io.fairyproject.plugin.Plugin;
import io.fairyproject.plugin.PluginHandler;
import io.fairyproject.plugin.PluginManager;
import io.fairyproject.task.ITaskScheduler;
import io.fairyproject.util.IOUtil;
import io.fairyproject.util.URLClassLoaderAccess;
import io.fairyproject.util.terminable.TerminableConsumer;
import io.fairyproject.util.terminable.composite.CompositeClosingException;
import io.fairyproject.util.terminable.composite.CompositeTerminable;
import io.github.classgraph.ClassGraph;
import io.github.toolfactory.narcissus.Narcissus;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.io.File;
import java.io.InputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;

@Getter
public abstract class FairyPlatform implements TerminableConsumer {

    public static FairyPlatform INSTANCE;
    private final AtomicBoolean loadedDependencies = new AtomicBoolean();
    private Plugin mainPlugin;

    private ITaskScheduler taskScheduler;
    private CompositeTerminable compositeTerminable;

    private PluginManager pluginManager;
    private LibraryHandler libraryHandler;
    private ContainerContext containerContext;
    private JacksonService jacksonService;
    private GlobalEventNode eventNode;

    public FairyPlatform() {
        if (Narcissus.libraryLoaded) {
            ClassGraph.CIRCUMVENT_ENCAPSULATION = ClassGraph.CircumventEncapsulationMethod.NARCISSUS;
        }
    }

    public void preload() {
        this.containerContext = ProcessManager.get().register(new ContainerContext());
        this.eventNode = ProcessManager.get().register(new GlobalEventNode());
        this.pluginManager = ProcessManager.get().register(new PluginManager(this.createPluginHandler()));
        this.jacksonService = ProcessManager.get().register(new JacksonService());
        this.taskScheduler = ProcessManager.get().register(this.createTaskScheduler());
        this.libraryHandler = ProcessManager.get().register(new LibraryHandler());
        this.loadProcesses();

        ProcessManager.get().preload();
    }

    public void load(Plugin mainPlugin) {
        this.mainPlugin = mainPlugin;



        ProcessManager.get().load();
        this.compositeTerminable = CompositeTerminable.create();
    }

    public void enable() {
        ProcessManager.get().enable();
        // register all the processes to container node

    }

    protected void loadProcesses() {
        // Load processes
    }

    public void disable() {
        try {
            this.compositeTerminable.close();
        } catch (CompositeClosingException ex) {
            ex.printStackTrace();
        }

        ProcessManager.get().destroy();
        Fairy.getPluginManager().callFrameworkFullyDisable();
    }

    @Override
    public <T extends AutoCloseable> @NotNull T bind(T t) {
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
    public abstract URLClassLoaderAccess getClassloader();

    /**
     * get Fairy Folder
     *
     * @return Fairy Folder
     */
    public abstract File getDataFolder();

    /**
     * on Post Services Initial
     *
     */
    public void onPostServicesInitial() {
        // to be overwritten
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
        IOUtil.saveResource(this, name, replace);
    }

    public InputStream getResource(String filename) {
        return IOUtil.getResource(this, filename);
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

    /**
     * Create a plugin handler for the platform
     *
     * @return the plugin handler
     */
    public abstract PluginHandler createPluginHandler();

    /**
     * Get the platform type of current platform
     *
     * @return the Platform type
     */
    public abstract PlatformType getPlatformType();

}
