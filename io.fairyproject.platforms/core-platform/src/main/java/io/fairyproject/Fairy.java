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

import io.fairyproject.event.GlobalEventNode;
import io.fairyproject.library.LibraryHandler;
import io.fairyproject.plugin.Plugin;
import io.fairyproject.plugin.PluginManager;
import lombok.experimental.UtilityClass;
import io.fairyproject.task.ITaskScheduler;
import io.fairyproject.util.FastRandom;

/**
 * Static extension of FairyBootstrap
 */
@UtilityClass
public class Fairy {

    public final String METADATA_PREFIX = "Imanity_";
    public final String PACKAGE_NAME = "fairy";
    private final FastRandom RANDOM = new FastRandom();

    public FastRandom random() {
        return RANDOM;
    }

    public boolean isRunning() {
        return FairyPlatform.INSTANCE.isRunning();
    }

    public Plugin getMainPlugin() {
        return FairyPlatform.INSTANCE.getMainPlugin();
    }

    public String getFairyPackage() {
        if (Debug.UNIT_TEST) {
            return "io.fairyproject";
        }
        return Fairy.getMainPlugin().getDescription().getShadedPackage() + "." + PACKAGE_NAME;
    }

    public GlobalEventNode getGlobalEventNode() {
        return FairyPlatform.INSTANCE.getEventNode();
    }

    public ITaskScheduler getTaskScheduler() {
        return FairyPlatform.INSTANCE.getTaskScheduler();
    }

    public LibraryHandler getLibraryHandler() {
        return FairyPlatform.INSTANCE.getLibraryHandler();
    }

    public PluginManager getPluginManager() {
        return FairyPlatform.INSTANCE.getPluginManager();
    }

    public FairyPlatform getPlatform() {
        return FairyPlatform.INSTANCE;
    }

}
