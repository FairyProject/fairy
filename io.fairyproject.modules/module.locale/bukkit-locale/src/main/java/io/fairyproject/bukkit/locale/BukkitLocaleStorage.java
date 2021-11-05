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

package io.fairyproject.bukkit.locale;

import io.fairyproject.Repository;
import io.fairyproject.Storage;
import io.fairyproject.bean.*;
import io.fairyproject.bukkit.listener.events.Events;
import io.fairyproject.bukkit.storage.ThreadedPlayerStorage;
import io.fairyproject.bukkit.storage.ThreadedPlayerStorageConfiguration;
import io.fairyproject.bukkit.storage.ThreadedPlayerStorageConfigurationRepository;
import io.fairyproject.locale.LocaleData;
import io.fairyproject.locale.LocaleService;
import org.bukkit.entity.Player;

import java.util.UUID;

@Service(name = "bukkit:locale-storage")
@ServiceDependency(dependencies = "locale", type = @DependencyType(ServiceDependencyType.SUB_DISABLE))
public class BukkitLocaleStorage extends ThreadedPlayerStorage<LocaleData> {

    private Repository<LocaleData, UUID> localeRepository;

    @Autowired
    private LocaleService localeService;

    @PostInitialize
    public void onPostInitialize() {
        this.localeRepository = Storage.createRepository("locale", LocaleData.class);
        this.localeService.setLocaleStorage(this);
    }

    @Override
    public boolean isDebugging() {
        return true;
    }

    @Override
    protected ThreadedPlayerStorageConfiguration<LocaleData> buildStorageConfiguration() {
        return new ThreadedPlayerStorageConfigurationRepository<LocaleData>() {
            @Override
            public LocaleData create(UUID uuid, String name) {
                return new LocaleData(uuid);
            }

            @Override
            public Repository<LocaleData, UUID> getRepository() {
                return BukkitLocaleStorage.this.localeRepository;
            }

            @Override
            public boolean shouldUnloadOnQuit(Player player) {
                return true;
            }
        };
    }

    @Override
    protected void onLoadedMain(Player player, LocaleData localeData) {
        Events.call(new PlayerLocaleLoadedEvent(player, localeData));
    }
}
