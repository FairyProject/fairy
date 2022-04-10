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

import io.fairyproject.container.*;
import io.fairyproject.config.GlobalStorageConfiguration;
import io.fairyproject.config.StorageConfiguration;
import io.fairyproject.jackson.JacksonService;
import io.fairyproject.library.Library;
import io.fairyproject.util.exceptionally.ThrowingRunnable;

import javax.annotation.Nullable;
import java.io.Serializable;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@ServiceDependency({GlobalStorageConfiguration.class, SerializerFactory.class, JacksonService.class})
public class StorageService {

    @Autowired
    private GlobalStorageConfiguration globalStorageConfiguration;

    private final Map<String, RepositoryProvider> repositoryProviders = new ConcurrentHashMap<>();

    @PreInitialize
    public void onPreInitialize() {
        Fairy.getLibraryHandler().downloadLibraries(false, Collections.singletonList(
                Library.H2_DRIVER
        ));
        ComponentRegistry.registerComponentHolder(ComponentHolder.builder()
                .type(RepositoryProvider.class)
                .onEnable(obj -> {
                    RepositoryProvider repositoryProvider = (RepositoryProvider) obj;

                    registerRepositoryProvider(repositoryProvider);
                    repositoryProvider.build();
                })
                .onDisable(obj -> {
                    RepositoryProvider repositoryProvider = (RepositoryProvider) obj;

                    unregisterRepositoryProvider(repositoryProvider);
                })
                .build());
    }

    @PostDestroy
    public void onPostDestroy() {
        for (RepositoryProvider repositoryProvider : this.repositoryProviders.values()) {
            this.unregisterRepositoryProvider(repositoryProvider);
        }
    }

    public void registerRepositoryProvider(RepositoryProvider repositoryProvider) {
        this.repositoryProviders.put(repositoryProvider.id(), repositoryProvider);
    }

    public void unregisterRepositoryProvider(RepositoryProvider repositoryProvider) {
        ThrowingRunnable.sneaky(repositoryProvider::close).run();
        this.repositoryProviders.remove(repositoryProvider.id());
    }

    @Nullable
    public RepositoryProvider getRepositoryProvider(String providerId) {
        return this.repositoryProviders.get(providerId);
    }

    public void loadStorageConfiguration(Path parentFolder, StorageConfiguration configuration) {
        RepositoryProvider repositoryProvider = this.getRepositoryProvider(configuration.getId());
        if (repositoryProvider == null) {
            repositoryProvider = RepositoryProvider.create(configuration.getRepositoryType(), parentFolder, configuration.getId());
            this.registerRepositoryProvider(repositoryProvider);
        }

        repositoryProvider.verify(configuration.getConfig());
        repositoryProvider.registerOptions(configuration.getConfig());
        repositoryProvider.build();
    }

    public <E, ID extends Serializable> Repository<E, ID> createRepository(String providerId, String repoId, Class<E> entityType) {
        final RepositoryProvider repositoryProvider = this.getRepositoryProvider(providerId);
        if (repositoryProvider == null) {
            throw new NullPointerException("Repository does not exists.");
        }

        return repositoryProvider.buildRepository(entityType, repoId);
    }

    public <E, ID extends Serializable> Repository<E, ID> createRepository(String repoId, Class<E> entityType) {
        return this.createRepository(this.globalStorageConfiguration.getStorageProviderFor(repoId), repoId, entityType);
    }
}
