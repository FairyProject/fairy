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

package io.fairyproject.library;

import com.google.common.collect.ImmutableSet;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import io.fairyproject.library.classloader.IsolatedClassLoader;
import io.fairyproject.library.relocate.RelocateHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import io.fairyproject.Fairy;
import io.fairyproject.plugin.Plugin;
import io.fairyproject.ExtendedClassLoader;
import io.fairyproject.plugin.PluginListenerAdapter;
import io.fairyproject.plugin.PluginManager;
import io.fairyproject.util.Stacktrace;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LibraryHandler {

    private final Path libFolder;
    private final RelocateHandler relocateHandler;

    private final Map<Library, Path> loaded = new ConcurrentHashMap<>();
    private final Map<Plugin, ExtendedClassLoader> pluginClassLoaders = new ConcurrentHashMap<>();
    private final Map<ImmutableSet<Library>, IsolatedClassLoader> loaders = new HashMap<>();

    private final Logger LOGGER = LogManager.getLogger(LibraryHandler.class);
    private final ExecutorService EXECUTOR = Executors.newCachedThreadPool(new ThreadFactoryBuilder()
        .setDaemon(true)
        .setNameFormat("Library Downloader - %d")
        .setUncaughtExceptionHandler((thread, throwable) -> Stacktrace.print(throwable))
        .build());

    public LibraryHandler() {
        File file = new File(Fairy.getPlatform().getDataFolder(), "libs");
        if (!file.exists()) {
            file.mkdirs();
        }
        this.libFolder = file.toPath();

        // ASM
        this.downloadLibraries(true,
                Library.ASM,
                Library.ASM_COMMONS
        );

        this.relocateHandler = new RelocateHandler(this);
        if (PluginManager.isInitialized()) {
            PluginManager.INSTANCE.registerListener(new PluginListenerAdapter() {
                @Override
                public void onPluginInitial(Plugin plugin) {
                    ExtendedClassLoader classLoader = new ExtendedClassLoader(plugin.getPluginClassLoader());

                    pluginClassLoaders.put(plugin, classLoader);
                    for (Path path : loaded.values()) {
                        classLoader.addJarToClasspath(path);
                    }
                }

                @Override
                public void onPluginDisable(Plugin plugin) {
                    pluginClassLoaders.remove(plugin);
                }
            });
        }
    }

    public IsolatedClassLoader obtainClassLoaderWith(Collection<Library> libraries) {
        return this.obtainClassLoaderWith(libraries.toArray(new Library[0]));
    }

    public IsolatedClassLoader obtainClassLoaderWith(Library... libraries) {
        ImmutableSet<Library> set = ImmutableSet.copyOf(libraries);

        for (Library dependency : libraries) {
            if (!this.loaded.containsKey(dependency)) {
                throw new IllegalStateException("Dependency " + dependency + " is not loaded.");
            }
        }

        synchronized (this.loaders) {
            IsolatedClassLoader classLoader = this.loaders.get(set);
            if (classLoader != null) {
                return classLoader;
            }

            URL[] urls = set.stream()
                    .map(this.loaded::get)
                    .map(file -> {
                        try {
                            return file.toUri().toURL();
                        } catch (MalformedURLException e) {
                            throw new RuntimeException(e);
                        }
                    })
                    .toArray(URL[]::new);

            classLoader = new IsolatedClassLoader(urls);
            this.loaders.put(set, classLoader);
            return classLoader;
        }
    }

    public void downloadLibraries(boolean autoLoad, Collection<Library> libraries) {
        this.downloadLibraries(autoLoad, libraries.toArray(new Library[0]));
    }

    public void downloadLibraries(boolean autoLoad, Library... libraries) {

        CountDownLatch latch = new CountDownLatch(libraries.length);

        for (Library library : libraries) {
            EXECUTOR.execute(() -> {
                try {
                    loadLibrary(library, autoLoad);
                    LOGGER.info("Loaded Library " + library.name() + " v" + library.getVersion());
                } catch (Throwable throwable) {
                    LOGGER.warn("Unable to load library " + library.getFileName() + ".", throwable);
                } finally {
                    latch.countDown();
                }
            });

        }

        try {
            latch.await();
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }

    }

    private Path loadLibrary(Library library, boolean addToUCP) throws Exception {
        if (loaded.containsKey(library)) {
            return loaded.get(library);
        }

        Path file = this.remapLibrary(library, this.downloadLibrary(library));
        this.loaded.put(library, file);
        if (addToUCP) {
//            this.classLoader.addJarToClasspath(file);
            Fairy.getPlatform().getClassloader().addJarToClasspath(file);
            for (ExtendedClassLoader classLoader : this.pluginClassLoaders.values()) {
                classLoader.addJarToClasspath(file);
            }
        }
        return file;
    }

    private Path remapLibrary(Library library, Path normalFile) {
        if (library.getRelocations().isEmpty()) {
            return normalFile;
        }

        Path remappedFile = this.libFolder.resolve(library.getFileName() + "-remapped.jar");
        if (Files.exists(remappedFile)) {
            return remappedFile;
        }

        try {
            System.out.println("Remapping library " + library.getName() + "...");

            this.relocateHandler.relocate(normalFile, remappedFile, library.getRelocations());
        } catch (Throwable throwable) {
            throw new RuntimeException("Something wrong while relocating library...", throwable);
        }
        return remappedFile;
    }

    protected Path downloadLibrary(Library library) throws LibraryDownloadException {
        Path file = this.libFolder.resolve(library.getFileName() + ".jar");

        if (Files.exists(file)) {
            return file;
        }

        try {
            library.getRepository().download(library, file);
            return file;
        } catch (LibraryDownloadException ex) {
            throw ex;
        }
    }
}
