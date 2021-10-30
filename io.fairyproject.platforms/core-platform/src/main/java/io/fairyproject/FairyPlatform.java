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

import com.google.common.collect.ImmutableSet;
import io.fairyproject.aspect.AsyncAspect;
import io.fairyproject.bean.BeanContext;
import io.fairyproject.bean.details.SimpleBeanDetails;
import io.fairyproject.cache.CacheableAspect;
import io.fairyproject.config.BaseConfiguration;
import io.fairyproject.library.Library;
import io.fairyproject.library.LibraryHandler;
import io.fairyproject.plugin.PluginManager;
import io.fairyproject.util.terminable.composite.CompositeClosingException;
import io.fairyproject.util.terminable.composite.CompositeTerminable;
import lombok.Getter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import io.fairyproject.task.ITaskScheduler;

import javax.annotation.Nullable;
import java.io.File;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;

@Getter
public abstract class FairyPlatform {

    public static final Logger LOGGER = LogManager.getLogger(FairyPlatform.class);

    public static FairyPlatform INSTANCE;

    private ITaskScheduler taskScheduler;
    private CompositeTerminable compositeTerminable;
    private AtomicBoolean loadedDependencies;

    private BaseConfiguration baseConfiguration;
    private LibraryHandler libraryHandler;
    private BeanContext beanContext;

    public void load() {
        this.taskScheduler = this.createTaskScheduler();

        this.compositeTerminable = CompositeTerminable.create();
        this.loadedDependencies = new AtomicBoolean();
    }

    public void enable() {
        this.loadDependencies();
        this.loadBindable();

        this.baseConfiguration = new BaseConfiguration();
        this.baseConfiguration.loadAndSave();

        this.beanContext = new BeanContext();
        this.beanContext.registerBean(new SimpleBeanDetails(this, "fairyBootstrap", this.getClass()));
        this.beanContext.init();
    }

    public void disable() {
        try {
            this.compositeTerminable.close();
        } catch (CompositeClosingException ex) {
            ex.printStackTrace();
        }

        this.beanContext.stop();
        PluginManager.INSTANCE.callFrameworkFullyDisable();
    }

    private void loadBindable() {
        this.bind(AsyncAspect.EXECUTOR);
        this.bind(CacheableAspect.CLEANER_SERVICE);
        this.bind(CacheableAspect.UPDATER_SERVICE);
    }

    public void loadDependencies() {
        if (!this.loadedDependencies.compareAndSet(false, true)) {
            return;
        }

        LOGGER.info("Loading Dependencies...");
        this.libraryHandler = new LibraryHandler();

        Set<Library> main = ImmutableSet.of(
                // SQL
                Library.MARIADB_DRIVER,
                Library.HIKARI,
                Library.MYSQL_DRIVER,
                Library.POSTGRESQL_DRIVER,

                // MONGO
                Library.MONGO_DB_SYNC,
                Library.MONGO_DB_CORE,
                Library.MONGOJACK,
                Library.BSON,

                // Caffeine
                Library.CAFFEINE,

                // Spring
                Library.SPRING_CORE,
                Library.SPRING_EL
        );
        this.libraryHandler.downloadLibraries(true, main);

        Set<Library> isolated = ImmutableSet.of(
                Library.H2_DRIVER
        );
        this.libraryHandler.downloadLibraries(false, isolated);

        final Set<Library> platformDependencies = this.getDependencies();
        if (!platformDependencies.isEmpty()) {
            this.libraryHandler.downloadLibraries(true, platformDependencies);
        }

        this.libraryHandler.downloadLibraries(true, Library.REDISSON);
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
    public abstract Set<Library> getDependencies();

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
    public abstract void saveResource(String name, boolean replace);

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
