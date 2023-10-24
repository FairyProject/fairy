/*
 * MIT License
 *
 * Copyright (c) 2022 Fairy Project
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

package io.fairyproject.devtools.reload.classloader;

import io.fairyproject.plugin.Plugin;
import io.fairyproject.plugin.PluginClassLoader;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import java.net.URL;
import java.net.URLClassLoader;

/**
 * A classloader that can reload classes from a plugin using specific classpath
 *
 * @author LeeGod
 * @since 0.7
 */
@Getter
@Setter
public class ReloadableClassLoader extends URLClassLoader implements PluginClassLoader {

    static {
        ClassLoader.registerAsParallelCapable();
    }

    private Plugin plugin;

    public ReloadableClassLoader(URL[] urls, ClassLoader parent) {
        super(urls, parent);
    }

    @Override
    public void addURL(@NotNull URL url) {
        super.addURL(url);
    }

    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        // We don't want to reload classes from FairyProject
        if (name.contains("io.fairyproject"))
            return super.loadClass(name, resolve);

        // We don't want to reload classes that wasn't from the plugin
        if (plugin != null && !name.contains(plugin.getDescription().getShadedPackage())) {
            return super.loadClass(name, resolve);
        }

        synchronized (getClassLoadingLock(name)) {
            Class<?> loadedClass = findLoadedClass(name);
            if (loadedClass == null) {
                try {
                    loadedClass = findClass(name);
                } catch (ClassNotFoundException ex) {
                    loadedClass = Class.forName(name, false, getParent());
                }
            }

            if (resolve) {
                resolveClass(loadedClass);
            }

            return loadedClass;
        }
    }
}
