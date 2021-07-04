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

package org.fairy.config;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.Getter;
import org.fairy.Fairy;
import org.fairy.StorageService;
import org.fairy.bean.Autowired;
import org.fairy.bean.PostInitialize;
import org.fairy.bean.Service;
import org.fairy.config.annotation.ElementType;
import org.fairy.config.yaml.YamlConfiguration;

import java.io.File;
import java.util.List;
import java.util.Map;

@Service(name = "storageConfiguration")
@Getter
public class GlobalStorageConfiguration extends YamlConfiguration {

    private Map<String, String> storages = Maps.newHashMap(ImmutableMap.of(
            "locale", "default"
    ));

    private String defaultProvider = "default";
    @ElementType(StorageConfiguration.class)
    private List<StorageConfiguration> storageProviders = Lists.newArrayList(new StorageConfiguration());

    @Autowired
    private transient StorageService storageService;

    protected GlobalStorageConfiguration() {
        super(new File(Fairy.getPlatform().getDataFolder(), "storage.yml").toPath());
    }

    @PostInitialize
    public void onPostInitialize() {
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
