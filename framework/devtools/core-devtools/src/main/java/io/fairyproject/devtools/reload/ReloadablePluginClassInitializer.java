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

package io.fairyproject.devtools.reload;

import io.fairyproject.devtools.DevToolProperties;
import io.fairyproject.devtools.reload.classloader.ReloadableClassLoader;
import io.fairyproject.plugin.Plugin;
import io.fairyproject.plugin.initializer.DefaultPluginClassInitializer;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.URL;
import java.util.List;

@RequiredArgsConstructor
@Getter
public class ReloadablePluginClassInitializer extends DefaultPluginClassInitializer {

    private final ClasspathCollection classpathCollection;

    public ReloadablePluginClassInitializer() {
        this.classpathCollection = DevToolProperties.getClasspathCollection();
    }

    @Override
    public @NotNull ClassLoader initializeClassLoader(@NotNull String name, @NotNull ClassLoader classLoader) {
        List<URL> urls = classpathCollection.getURLsByName(name);
        if (urls == null)
            return classLoader;

        try {
            return new ReloadableClassLoader(urls.toArray(new URL[0]), classLoader);
        } catch (Throwable throwable) {
            throw new IllegalStateException(throwable);
        }
    }

    @Override
    public Plugin create(String mainClassPath, ClassLoader classLoader) {
        Plugin plugin = super.create(mainClassPath, classLoader);

        if (classLoader instanceof ReloadableClassLoader) {
            ((ReloadableClassLoader) classLoader).setPlugin(plugin);
        }

        return plugin;
    }

    @Override
    public void onPluginLoad(Plugin plugin) {
        List<URL> urls = classpathCollection.getURLsByName(plugin.getName());
        if (urls == null)
            return;

        for (URL url : urls) {
            plugin.getClassLoaderRegistry().addUrl(url);
        }
    }

    @Override
    public void onPluginUnload(Plugin plugin) {
        ReloadableClassLoader classLoader = (ReloadableClassLoader) plugin.getPluginClassLoader();
        try {
            classLoader.close();
        } catch (IOException e) {
            throw new IllegalStateException("Failed to close class loader", e);
        }
    }
}
