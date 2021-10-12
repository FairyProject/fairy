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

package org.fairy.locale;

import com.google.common.base.Preconditions;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.apache.commons.io.FilenameUtils;
import org.fairy.bean.*;
import org.fairy.config.yaml.YamlConfiguration;
import org.fairy.module.Depend;
import org.fairy.module.Modular;
import org.fairy.storage.DataClosable;
import org.fairy.util.CC;
import org.fairy.util.FileUtil;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Modular(
        value = "core-locale",
        abstraction = true,
        depends = @Depend("core-storage")
)
@Service(name = "locale")
public class LocaleService {

    private Map<String, Locale> locales;
    @Getter
    private Locale defaultLocale;
    private Yaml yaml;

    @Setter
    private PlayerLocaleStorage localeStorage;
    private LocalizationConfiguration localizationConfiguration;

    @PreInitialize
    public void onPreInitialize() {
        ComponentRegistry.registerComponentHolder(new ComponentHolder() {
            @Override
            public Class<?>[] type() {
                return new Class[] {LocaleDirectory.class};
            }

            @Override
            public void onEnable(Object instance) {
                loadLocaleDirectory((LocaleDirectory) instance);
            }
        });
    }

    @PostInitialize
    public void onPostInitialize() {
        this.localizationConfiguration = new LocalizationConfiguration();
        this.localizationConfiguration.loadAndSave();

        this.locales = new ConcurrentHashMap<>();
        this.defaultLocale = this.getOrRegister(this.localizationConfiguration.getDefaultLocale());

        this.yaml = new Yaml();
    }

    public void loadLocaleDirectory(LocaleDirectory localeDirectory) {
        final File directory = localeDirectory.directory();
        if (!directory.exists()) {
            directory.mkdirs();
            final String resourceDirectory = localeDirectory.resourceDirectory();
            if (resourceDirectory != null) {
                FileUtil.forEachDirectoryInResources(localeDirectory.getClass(), resourceDirectory, (name, inputStream) -> {
                    final File file = new File(directory, name);

                    FileUtil.writeInputStreamToFile(inputStream, file);
                });
            }
        }

        for (File file : directory.listFiles()) {
            String name = FilenameUtils.removeExtension(file.getName());
            this.registerFromYaml(name, localeDirectory.config(file));
        }

        final String defaultLocale = localeDirectory.defaultLocale();
        File defaultLocaleFile = new File(directory, defaultLocale + ".yml");
        if (!defaultLocaleFile.exists()) {
            this.registerFromYaml(defaultLocale, localeDirectory.config(defaultLocaleFile));
        }
    }

    public Locale getOrRegister(String name) {
        Locale locale = this.locales.get(name);

        if (locale == null) {
            locale = this.registerLocale(name);
        }

        return locale;
    }

    public Locale registerLocale(String name) {
        final Locale locale = new Locale(name);
        this.locales.put(name, locale);

        return locale;
    }

    public Locale unregisterLocale(String name) {
        return this.locales.remove(name);
    }

    public Locale registerFromYml(File file) {
        try {
            return this.registerFromYml(new FileInputStream(file));
        } catch (Throwable throwable) {
            throw new RuntimeException("Something wrong while loading file for locale", throwable);
        }
    }

    public Locale registerFromYml(InputStream inputStream) {
        Map<String, Object> map = this.yaml.load(inputStream);
        String name = map.get("locale").toString();

        Locale locale = this.getOrRegister(name);
        locale.registerEntries("", map);

        return locale;
    }

    public Locale registerFromYaml(String name, YamlConfiguration yamlConfiguration) {
        try {
            final Map<String, Object> entries = yamlConfiguration.loadEntries();

            Locale locale = this.getOrRegister(name);
            locale.registerEntries("", entries);

            return locale;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Locale getLocaleByName(String name) {
        if (this.locales.containsKey(name)) {
            return this.locales.get(name);
        }

        return null;
    }

    public Locale getLocale(UUID uuid) {
        return this.localeStorage.find(uuid).getLocale();
    }

    public <Player> Locale getLocale(Player player) {
        return this.localeStorage.find(this.localeStorage.getUuidByPlayer(player)).getLocale();
    }

    public void setLocale(UUID uuid, @NonNull Locale locale) {
        try (DataClosable<LocaleData> data = this.localeStorage.findAndSave(uuid)) {
            data.val().setLocale(locale);
        }
    }

    public <Player> void setLocale(Player player, @NonNull Locale locale) {
        this.setLocale(this.localeStorage.getUuidByPlayer(player), locale);
    }

    public void setLocale(UUID uuid, @NonNull String localeName) {
        final Locale locale = this.getLocaleByName(localeName);
        Preconditions.checkNotNull(locale, "Couldn't find locale with name " + localeName);

        this.setLocale(uuid, locale);
    }

    public <Player> void setLocale(Player player, @NonNull String localeName) {
        this.setLocale(this.localeStorage.getUuidByPlayer(player), localeName);
    }

    public String translate(UUID uuid, String key) {
        return CC.translate(this.getLocale(uuid).get(key));
    }

    public <Player> String translate(Player player, String key) {
        return this.translate(this.localeStorage.getUuidByPlayer(player), key);
    }

}
