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

import io.fairyproject.Fairy;
import io.fairyproject.library.classloader.IsolatedClassLoader;
import io.fairyproject.plugin.Plugin;
import io.fairyproject.plugin.PluginListenerAdapter;
import io.fairyproject.plugin.PluginManager;
import io.fairyproject.util.FairyThreadFactory;
import io.fairyproject.util.Stacktrace;
import io.fairyproject.util.URLClassLoaderAccess;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Stream;

public class LibraryHandler {

    private final Path libFolder;

    private final Map<Library, List<Path>> loaded = new ConcurrentHashMap<>();
    private final Map<Plugin, URLClassLoaderAccess> pluginClassLoaders = new ConcurrentHashMap<>();
    private final Map<Set<Library>, IsolatedClassLoader> loaders = new HashMap<>();

    private final Logger LOGGER = LogManager.getLogger(LibraryHandler.class);
    private final ExecutorService EXECUTOR = Executors.newCachedThreadPool(FairyThreadFactory.builder()
            .daemon(true)
            .name("Library Downloader - <id>")
            .uncaughtExceptionHandler((thread, throwable) -> Stacktrace.print(throwable))
            .priority(Thread.NORM_PRIORITY - 1)
            .build());

    public LibraryHandler() {
        File file = new File(Fairy.getPlatform().getDataFolder(), "libs");
        if (!file.exists()) {
            file.mkdirs();
        }
        this.libFolder = file.toPath();

        if (PluginManager.isInitialized()) {
            PluginManager.INSTANCE.registerListener(new PluginListenerAdapter() {
                @Override
                public void onPluginInitial(Plugin plugin) {
                    final URLClassLoaderAccess classLoader = URLClassLoaderAccess.create((URLClassLoader) plugin.getPluginClassLoader());
                    pluginClassLoaders.put(plugin, classLoader);
                    for (List<Path> paths : loaded.values()) {
                        for (Path path : paths) {
                            classLoader.addPath(path);
                        }
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
        Set<Library> set = new HashSet<>(Arrays.asList(libraries));

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
                    .flatMap(files -> {
                        URL[] retVal = new URL[files.size()];
                        int i = 0;
                        for (Path file : files) {
                            try {
                                retVal[i++] = file.toUri().toURL();
                            } catch (MalformedURLException e) {
                                throw new RuntimeException(e);
                            }
                        }
                        return Stream.of(retVal);
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
        this.downloadLibraryAsynchronously(autoLoad, libraries).join();
    }

    public CompletableFuture<?> downloadLibraryAsynchronously(boolean autoLoad, Library... libraries) {
        CompletableFuture<?>[] futures = new CompletableFuture[libraries.length];

        for (int i = 0; i < libraries.length; i++) {
            CompletableFuture<?> future = futures[i] = new CompletableFuture<>();
            final Library library = libraries[i];

            EXECUTOR.submit(() -> {
                try {
                    loadLibrary(library, autoLoad);
                    LOGGER.info("Loaded Library " + library.name() + " v" + library.getVersion());
                } catch (Throwable throwable) {
                    LOGGER.warn("Unable to load library " + library.getFileName() + ".", throwable);
                } finally {
                    future.complete(null);
                }
            });
        }

        return CompletableFuture.allOf(futures);
    }

    private List<Path> loadLibrary(Library library, boolean addToUCP) {
        if (loaded.containsKey(library)) {
            return loaded.get(library);
        }

        List<Path> files = this.downloadLibrary(library);
        this.loaded.put(library, files);
        if (addToUCP) {
            for (Path path : files) {
                Fairy.getPlatform().getClassloader().addJarToClasspath(path);
                for (URLClassLoaderAccess classLoader : this.pluginClassLoaders.values()) {
                    classLoader.addPath(path);
                }
            }
        }
        return files;
    }

    protected List<Path> downloadLibrary(Library library) {
        Path file = this.libFolder.resolve(library.getFileName() + ".jar");

        if (Files.exists(file)) {
            return Collections.singletonList(file);
        }

        try (InputStream is = library.getUrl(library.getRepository()).openStream()) {
            Files.copy(is, file);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }

        return Collections.singletonList(file);
    }
}
