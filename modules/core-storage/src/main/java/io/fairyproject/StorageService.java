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

import io.fairyproject.bean.*;
import io.fairyproject.config.GlobalStorageConfiguration;
import io.fairyproject.config.StorageConfiguration;
import io.fairyproject.library.Library;
import org.fairy.bean.*;
import io.fairyproject.module.Modular;
import io.fairyproject.util.Utility;

import javax.annotation.Nullable;
import java.io.Serializable;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Modular(
        value = "core-storage",
        abstraction = false
)
@Service(name = "storage")
@ServiceDependency(dependencies = {"storageConfiguration", "serializer", "jackson"})
public class StorageService {

    @Autowired
    private GlobalStorageConfiguration globalStorageConfiguration;

    private final Map<String, RepositoryProvider> repositoryProviders = new ConcurrentHashMap<>();

    @PreInitialize
    public void onPreInitialize() {
        Fairy.getLibraryHandler().downloadLibraries(true, Library.BYTE_BUDDY);
        ComponentRegistry.registerComponentHolder(new ComponentHolder() {
            @Override
            public Class<?>[] type() {
                return new Class[] {RepositoryProvider.class};
            }

            @Override
            public void onEnable(Object instance) {
                RepositoryProvider repositoryProvider = (RepositoryProvider) instance;

                registerRepositoryProvider(repositoryProvider);
                repositoryProvider.build();
            }

            @Override
            public void onDisable(Object instance) {
                RepositoryProvider repositoryProvider = (RepositoryProvider) instance;

                unregisterRepositoryProvider(repositoryProvider);
            }
        });
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
        Utility.sneaky(repositoryProvider::close);

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
