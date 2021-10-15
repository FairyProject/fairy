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

package io.fairyproject.locale;

import com.google.common.base.Preconditions;
import io.fairyproject.bean.*;
import io.fairyproject.mc.MCPlayer;
import io.fairyproject.module.Depend;
import io.fairyproject.module.Modular;
import io.fairyproject.storage.DataClosable;
import io.fairyproject.storage.PlayerStorage;
import lombok.NonNull;
import lombok.Setter;
import net.kyori.adventure.translation.Translator;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;
import java.util.UUID;

@Modular(
        value = "mc-locale",
        abstraction = true,
        depends = @Depend("core-storage")
)
@Service(name = "locale")
public class LocaleService {

    public static Locale DEFAULT = Locale.ENGLISH;

    @Setter
    private PlayerStorage<LocaleData> localeStorage;
    private LocalizationConfiguration localizationConfiguration;

    @PreInitialize
    public void onPreInitialize() {
        ComponentRegistry.registerComponentHolder(new ComponentHolder() {
            @Override
            public Class<?>[] type() {
                return new Class[] {TranslationManager.class};
            }
        });
    }

    @PostInitialize
    public void onPostInitialize() {
        this.localizationConfiguration = new LocalizationConfiguration();
        this.localizationConfiguration.loadAndSave();
    }

    public boolean isTranslationFile(Path path) {
        final String fileName = path.getFileName().toString();
        try {
            this.recognizeFileType(FilenameUtils.getExtension(fileName));
            return true;
        } catch (IllegalArgumentException ex) {
            return false;
        }
    }

    public Pair<Locale, ResourceBundle> loadTranslationFile(Path path) {
        try {
            final String fileName = path.getFileName().toString();
            LocaleFileType fileType = this.recognizeFileType(FilenameUtils.getExtension(fileName));
            final String localeName = FilenameUtils.getName(fileName);
            Locale locale = parseLocale(localeName);

            if (locale == null) {
                throw new IllegalStateException("Unknown locale '" + localeName + "', unable to register.");
            }

            ResourceBundle resourceBundle;
            try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
                switch (fileType) {
                    case PROPERTIES:
                        resourceBundle = new PropertyResourceBundle(reader);
                        break;
                    case YAML:
                        resourceBundle = new dev.akkinoc.util.YamlResourceBundle(reader);
                        break;
                    default:
                        throw new IllegalArgumentException("Cannot recognize the locale type ." + fileType);
                }
            }

            return Pair.of(locale, resourceBundle);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private LocaleFileType recognizeFileType(String extension) {
        switch (extension.toLowerCase()) {
            case "properties":
                return LocaleFileType.PROPERTIES;
            case "yaml":
            case "yml":
                return LocaleFileType.YAML;
        }
        throw new IllegalArgumentException("Cannot recognize the locale type ." + extension);
    }

    public Locale getLocale(UUID uuid) {
        return this.localeStorage.find(uuid).getLocale();
    }

    public <Player> Locale getLocale(Player player) {
        return this.localeStorage.find(MCPlayer.from(player).getUUID()).getLocale();
    }

    public void setLocale(UUID uuid, @NonNull Locale locale) {
        try (DataClosable<LocaleData> data = this.localeStorage.findAndSave(uuid)) {
            data.val().setLocale(locale);
        }
    }

    public <Player> void setLocale(Player player, @NonNull Locale locale) {
        this.setLocale(MCPlayer.from(player).getUUID(), locale);
    }

    public void setLocale(UUID uuid, @NonNull String localeName) {
        final Locale locale = parseLocale(localeName);
        Preconditions.checkNotNull(locale, "Couldn't find locale with name " + localeName);

        this.setLocale(uuid, locale);
    }

    public <Player> void setLocale(Player player, @NonNull String localeName) {
        this.setLocale(MCPlayer.from(player).getUUID(), localeName);
    }

    public static @Nullable Locale parseLocale(@Nullable String locale) {
        return locale == null ? null : Translator.parseLocale(locale);
    }

}
