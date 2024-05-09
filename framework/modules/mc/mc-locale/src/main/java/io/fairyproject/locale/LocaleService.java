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

import io.fairyproject.Debug;
import io.fairyproject.container.*;
import io.fairyproject.locale.util.YamlResourceBundle;
import io.fairyproject.mc.MCPlayer;
import io.fairyproject.storage.DataClosable;
import io.fairyproject.storage.PlayerStorage;
import io.fairyproject.util.ConditionUtils;
import io.fairyproject.util.entry.Entry;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import net.kyori.adventure.translation.Translator;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;
import java.util.UUID;

@Service
public class LocaleService {

    @Setter
    private PlayerStorage<LocaleData> localeStorage;
    @Getter
    private LocalizationConfiguration localizationConfiguration;

    @PreInitialize
    public void onPreInitialize() {
    }

    @PostInitialize
    public void onPostInitialize() {
        this.localizationConfiguration = new LocalizationConfiguration();
        if (Debug.UNIT_TEST) return;
        this.localizationConfiguration.loadAndSave();
    }

    public boolean isTranslationFile(Path path) {
        final String fileName = path.getFileName().toString();
        try {
            this.recognizeFileType(getFileExtension(fileName));
            return true;
        } catch (IllegalArgumentException ex) {
            return false;
        }
    }

    public Entry<Locale, ResourceBundle> loadTranslationFile(Path path) {
        try {
            final String fileName = path.getFileName().toString();
            LocaleFileType fileType = this.recognizeFileType(getFileExtension(fileName));
            final String localeName = getFileName(fileName);
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
                        resourceBundle = new YamlResourceBundle(reader);
                        break;
                    default:
                        throw new IllegalArgumentException("Cannot recognize the locale type ." + fileType);
                }
            }

            return new Entry<>(locale, resourceBundle);
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
        ConditionUtils.notNull(locale, "Couldn't find locale with name " + localeName);

        this.setLocale(uuid, locale);
    }

    public <Player> void setLocale(Player player, @NonNull String localeName) {
        this.setLocale(MCPlayer.from(player).getUUID(), localeName);
    }

    public static @Nullable Locale parseLocale(@Nullable String locale) {
        return locale == null ? null : Translator.parseLocale(locale);
    }

    private static String getFileExtension(String fileName) {
        if (fileName == null) {
            return null;
        }
        int extensionPos = fileName.lastIndexOf('.');
        int lastUnixPos = fileName.lastIndexOf('/');
        int lastWindowsPos = fileName.lastIndexOf('\\');
        int lastSeparator = Math.max(lastUnixPos, lastWindowsPos);
        int index = lastSeparator > extensionPos ? -1 : extensionPos;
        if (index == -1) {
            return "";
        } else {
            return fileName.substring(index + 1);
        }
    }

    private static String getFileName(String fileName) {
        if (fileName == null) {
            return null;
        }
        int extensionPos = fileName.lastIndexOf('.');
        int lastUnixPos = fileName.lastIndexOf('/');
        int lastWindowsPos = fileName.lastIndexOf('\\');
        int lastSeparator = Math.max(lastUnixPos, lastWindowsPos);
        int index = lastSeparator > extensionPos ? -1 : extensionPos;
        if (index == -1) {
            return fileName;
        } else {
            return fileName.substring(0, index);
        }
    }

}
