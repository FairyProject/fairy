package io.fairyproject.locale;

import com.google.common.collect.Sets;
import io.fairyproject.container.Autowired;
import io.fairyproject.container.PostInitialize;
import io.fairyproject.container.PreDestroy;
import io.fairyproject.container.ServiceDependency;
import io.fairyproject.locale.util.YamlResourceBundle;
import io.fairyproject.util.FileUtil;
import lombok.Getter;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.translation.GlobalTranslator;
import net.kyori.adventure.translation.TranslationRegistry;
import net.kyori.adventure.util.UTF8ResourceBundleControl;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@ServiceDependency(
        value = LocaleService.class
)
public abstract class TranslationManager {

    @Autowired
    protected static LocaleService LOCALE_SERVICE;
    protected static final Logger LOGGER = LogManager.getLogger(TranslationManager.class);

    protected final Set<Locale> installed = Sets.newConcurrentHashSet();
    @Getter
    protected TranslationRegistry translationRegistry;
    protected boolean loaded;

    @PostInitialize
    protected void onInitialize() throws IOException {
        if (this.loaded) {
            return;
        }
        this.load();
    }

    @PreDestroy
    protected void onDestroy() {
        this.unload();
    }

    public void load() throws IOException {
        this.unload(); // unload to ensure it's newly loading
        this.loaded = true;

        this.translationRegistry = TranslationRegistry.create(this.key());
        this.translationRegistry.defaultLocale(this.defaultLocale());

        final Path directory = this.getTranslationsDirectory();
        if (!Files.exists(directory)) {
            Files.createDirectories(directory);
            final String resourceDirectory = this.getResourceDirectory();
            if (resourceDirectory != null) {
                FileUtil.forEachDirectoryInResources(this.getClass(), resourceDirectory, (name, inputStream) -> {
                    final File file = new File(directory.toFile(), name);
                    FileUtil.writeInputStreamToFile(inputStream, file);
                });
            }
        }

        List<Path> translationFiles;
        try (Stream<Path> stream = Files.list(directory)) {
            translationFiles = stream.filter(LOCALE_SERVICE::isTranslationFile).collect(Collectors.toList());
        } catch (IOException e) {
            translationFiles = Collections.emptyList();
        }

        if (translationFiles.isEmpty()) {
            return;
        }

        Map<Locale, ResourceBundle> loaded = new HashMap<>();
        for (Path translationFile : translationFiles) {
            try {
                Map.Entry<Locale, ResourceBundle> result = LOCALE_SERVICE.loadTranslationFile(translationFile);
                this.translationRegistry.registerAll(result.getKey(), result.getValue(), false);
                this.installed.add(result.getKey());

                loaded.put(result.getKey(), result.getValue());
            } catch (Exception e) {
                LOGGER.warn("Error loading locale file: " + translationFile.getFileName(), e);
            }
        }

        // try registering the locale without a country code - if we don't already have a registration for that
        loaded.forEach((locale, bundle) -> {
            Locale localeWithoutCountry = new Locale(locale.getLanguage());
            if (!locale.equals(localeWithoutCountry) && !localeWithoutCountry.equals(this.defaultLocale()) && this.installed.add(localeWithoutCountry)) {
                try {
                    this.translationRegistry.registerAll(localeWithoutCountry, bundle, false);
                } catch (IllegalArgumentException e) {
                    // ignore
                }
            }
        });

//        ResourceBundle bundle = null;
//        switch (this.defaultLocaleFileType()) {
//            case PROPERTIES:
//                bundle = ResourceBundle.getBundle(this.defaultBundleKey(), this.defaultLocale(), this.getClass().getClassLoader(), UTF8ResourceBundleControl.get());
//                break;
//            case YAML:
//                bundle = ResourceBundle.getBundle(this.defaultBundleKey(), this.defaultLocale(), this.getClass().getClassLoader(), YamlResourceBundle.Control.INSTANCE);
//                break;
//        }
//        try {
//            this.translationRegistry.registerAll(this.defaultLocale(), bundle, false);
//        } catch (IllegalArgumentException e) {
//            if (!isAdventureDuplicatesException(e)) {
//                LOGGER.warn("Error loading default locale file", e);
//            }
//        }
    }

    public void unload() {
        if (!this.loaded) {
            return;
        }
        this.loaded = false;

        if (this.translationRegistry != null) {
            GlobalTranslator.get().removeSource(this.translationRegistry);
            this.installed.clear();
        }
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private static boolean isAdventureDuplicatesException(Exception e) {
        return e instanceof IllegalArgumentException && (e.getMessage().startsWith("Invalid key") || e.getMessage().startsWith("Translation already exists"));
    }

    public abstract String getResourceDirectory();

    public abstract Path getTranslationsDirectory();

    public abstract String defaultBundleKey();

    public abstract LocaleFileType defaultLocaleFileType();

    public abstract Locale defaultLocale();

    public abstract Key key();

}
