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

package org.fairy;

import org.fairy.library.Library;
import org.fairy.plugin.PluginClassLoader;
import org.fairy.task.ITaskScheduler;

import javax.annotation.Nullable;
import java.io.File;
import java.util.Set;

public interface FairyPlatform {

    /**
     * get Plugin Class Loader
     *
     * @return Plugin Class Loader
     */
    PluginClassLoader getClassloader();

    /**
     * get Fairy Folder
     *
     * @return Fairy Folder
     */
    File getDataFolder();

    /**
     * get Dependencies based on platforms
     *
     * @return Dependencies Set
     */
    Set<Library> getDependencies();

    /**
     * on Post Services Initial
     *
     */
    default void onPostServicesInitial() {

    }

    /**
     * get Class Loader Name
     *
     * @param classLoader Class Loader
     * @return Name
     * @throws Exception Any error during the process
     */
    @Nullable
    default String getClassLoaderName(ClassLoader classLoader) throws Exception {
        return null;
    }

    /**
     * Save Resource File to Fairy Folder
     *
     * @param name the Name
     * @param replace should Replace File
     */
    void saveResource(String name, boolean replace);

    /**
     * Shutdown Fairy
     *
     */
    void shutdown();

    /**
     * is Fairy Running
     *
     * @return is Running
     */
    boolean isRunning();

    /**
     * is Main Thread
     *
     * @return is Main Thread
     */
    boolean isMainThread();

    /**
     * Create Task Scheduler for Fairy Core to schedule tasks with built-in APIs from each platforms
     *
     * @return the Task Scheduler
     */
    ITaskScheduler createTaskScheduler();

}
