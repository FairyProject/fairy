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

import io.fairyproject.FairyPlatform;
import io.fairyproject.library.classloader.IsolatedClassLoader;
import io.fairyproject.library.relocate.Relocation;
import io.fairyproject.library.relocate.RelocationHandler;
import io.fairyproject.library.relocate.RelocationHandlerImpl;
import io.fairyproject.plugin.Plugin;
import io.fairyproject.plugin.PluginManager;
import io.fairyproject.util.URLClassLoaderAccess;
import lombok.SneakyThrows;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class LibraryHandlerImpl implements LibraryHandler {

    private final FairyPlatform platform;
    private final Path directory;
    private final MessageDigest messageDigest;

    private final Map<Library, Path> loadedLibrary;
    private final Map<Plugin, URLClassLoaderAccess> pluginClassLoaders;
    private final Map<Set<Library>, IsolatedClassLoader> loaders;
    private final RelocationHandler relocationHandler;

    public LibraryHandlerImpl(FairyPlatform platform) {
        this.platform = platform;
        this.directory = platform.getDataFolder().toPath().resolve("libs");
        this.loadedLibrary = new ConcurrentHashMap<>();
        this.pluginClassLoaders = new ConcurrentHashMap<>();
        this.loaders = new ConcurrentHashMap<>();

        try {
            if (!Files.exists(this.directory))
                Files.createDirectories(this.directory);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to create directory for libraries.", e);
        }

        this.relocationHandler = new RelocationHandlerImpl(this);

        try {
            this.messageDigest = MessageDigest.getInstance("SHA-256");
        } catch (Exception e) {
            throw new IllegalStateException("Failed to initialize message digest.", e);
        }

        if (PluginManager.isInitialized())
            PluginManager.INSTANCE.registerListener(new LibraryHandlerPluginListener(this));
    }

    public Map<Plugin, URLClassLoaderAccess> getPluginClassLoaders() {
        return pluginClassLoaders;
    }

    protected void addClassLoader(Plugin plugin, URLClassLoaderAccess classLoader) {
        this.pluginClassLoaders.put(plugin, classLoader);
        this.loadedLibrary.forEach((library, path) -> {
            try {
                classLoader.addURL(path.toUri().toURL());
            } catch (MalformedURLException e) {
                throw new IllegalStateException("Failed to add URL to classloader.", e);
            }
        });
    }

    @Override
    public IsolatedClassLoader obtainClassLoaderWith(Collection<Library> libraries) {
        return this.obtainClassLoaderWith(libraries.toArray(new Library[0]));
    }

    @Override
    public IsolatedClassLoader obtainClassLoaderWith(Library... libraries) {
        Set<Library> set = new HashSet<>(Arrays.asList(libraries));

        for (Library dependency : libraries) {
            if (!this.loadedLibrary.containsKey(dependency)) {
                throw new IllegalStateException("Dependency " + dependency + " is not loaded.");
            }
        }

        IsolatedClassLoader classLoader = this.loaders.get(set);
        if (classLoader != null) {
            return classLoader;
        }

        List<URL> urls = new ArrayList<>();
        for (Library library : set) {
            Path path = this.loadedLibrary.get(library);
            try {
                urls.add(path.toUri().toURL());
            } catch (MalformedURLException e) {
                throw new IllegalStateException("Failed to isolate classloader.", e);
            }
        }

        classLoader = new IsolatedClassLoader(urls.toArray(new URL[0]));
        this.loaders.put(set, classLoader);
        return classLoader;
    }

    @Override
    public void loadLibrary(Library library, boolean addToUCP, Relocation... relocations) {
        if (loadedLibrary.containsKey(library))
            return;

        Path path;
        try {
            path = this.remapLibrary(this.downloadLibrary(library), library, relocations);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load library " + library + ".", e);
        }

        this.loadedLibrary.put(library, path);
        if (addToUCP) {
            this.platform.getClassloader().addPath(path);
            for (URLClassLoaderAccess classLoader : this.pluginClassLoaders.values()) {
                classLoader.addPath(path);
            }
        }
    }

    protected Path downloadLibrary(Library library) throws IOException {
        Path path = this.directory.resolve(library.getFileName() + ".jar");
        if (Files.exists(path) && this.isChecksumValid(path, library.getChecksum()))
            return path;

        HttpURLConnection connection = (HttpURLConnection) library.getUrl(library.getRepository()).openConnection();
        connection.setDoInput(true);
        connection.addRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; Win64; x64; rv:56.0) Gecko/20100101 Firefox/56.0");
        connection.setRequestMethod("GET");

        try (InputStream is = connection.getInputStream()) {
            Files.copy(is, path, StandardCopyOption.REPLACE_EXISTING);
        }

        return this.downloadLibrary(library);
    }

    @SneakyThrows
    protected boolean isChecksumValid(Path path, byte[] checksum) {
        if (checksum == null)
            return true;

        byte[] raw = Files.readAllBytes(path);
        byte[] bytes = this.messageDigest.digest(raw);

        return Arrays.equals(bytes, checksum);
    }

    protected Path remapLibrary(Path rawPath, Library library, Relocation... relocations) {
        Path relocatedPath = this.directory.resolve(library.getFileName() + "-relocated.jar");
        if (Files.exists(relocatedPath))
            return relocatedPath;

        if (relocations.length == 0)
            return rawPath;

        try {
            this.relocationHandler.remap(rawPath, relocatedPath, Arrays.asList(relocations));
        } catch (Exception e) {
            throw new IllegalStateException("Failed to relocate library " + library.getFileName() + ".", e);
        }

        return relocatedPath;
    }
}
