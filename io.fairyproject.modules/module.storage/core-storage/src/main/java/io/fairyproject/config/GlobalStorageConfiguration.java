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

package io.fairyproject.config;

import io.fairyproject.Debug;
import io.fairyproject.Fairy;
import io.fairyproject.StorageService;
import io.fairyproject.config.annotation.ElementType;
import io.fairyproject.config.yaml.YamlConfiguration;
import io.fairyproject.container.Autowired;
import io.fairyproject.container.InjectableComponent;
import io.fairyproject.container.PostInitialize;
import io.fairyproject.util.collection.MapBuilder;
import lombok.Getter;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@InjectableComponent
@Getter
@SuppressWarnings("FieldMayBeFinal")
public class GlobalStorageConfiguration extends YamlConfiguration {

    private Map<String, String> storages = MapBuilder.<String, String>create()
            .put("locale", "default")
            .build();

    private String defaultProvider = "default";
    @ElementType(StorageConfiguration.class)
    private List<StorageConfiguration> storageProviders = Collections.singletonList(new StorageConfiguration());

    @Autowired
    private transient StorageService storageService;

    protected GlobalStorageConfiguration() {
        super(new File(Fairy.getPlatform().getDataFolder(), "storage.yml").toPath());
    }

    @PostInitialize
    public void onPostInitialize() {
        if (Debug.UNIT_TEST) return;
        this.loadAndSave();
    }

    public String getStorageProviderFor(String repositoryId) {
        return this.storages.getOrDefault(repositoryId, this.defaultProvider);
    }

    @Override
    protected void postLoad() {
        if (storageProviders.isEmpty()) {
            storageProviders.add(new StorageConfiguration());
        }
        for (StorageConfiguration configuration : this.storageProviders) {
            this.storageService.loadStorageConfiguration(Fairy.getPlatform().getDataFolder().toPath().toAbsolutePath(), configuration);
        }
    }
}
