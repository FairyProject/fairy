package io.fairyproject.gradle;

import io.fairyproject.gradle.util.SneakyThrow;
import lombok.Getter;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;

import javax.inject.Inject;
import java.util.*;

@Getter
public class FairyExtension {

    // Fairy
    private final Property<String> fairyVersion;
    private final ListProperty<PlatformType> fairyPlatforms;

    // Plugin
    private final Property<String> classifier;
    private final Property<String> mainPackage;
    private final Property<String> name;
    private final Property<String> description;
    private final Property<Boolean> libraryMode;
    private final ListProperty<String> authors;

    // Specify for debug
    private final Property<Boolean> fairyIde;
    private final Property<Boolean> localRepo;

    private final Map<PlatformType, Map<String, String>> nodes;
    private final List<String> fairyModules;
    private final List<Lib> libraries;

    @Inject
    public FairyExtension(ObjectFactory objectFactory) {
        // Fairy
        this.fairyVersion = objectFactory.property(String.class);
        this.fairyPlatforms = objectFactory.listProperty(PlatformType.class).convention(Collections.singleton(PlatformType.BUKKIT));

        // Plugin
        this.classifier = objectFactory.property(String.class).convention("all");
        this.mainPackage = objectFactory.property(String.class);
        this.name = objectFactory.property(String.class);
        this.description = objectFactory.property(String.class);
        this.fairyIde = objectFactory.property(Boolean.class);
        this.localRepo = objectFactory.property(Boolean.class).convention(false);
        this.libraryMode = objectFactory.property(Boolean.class).convention(false);
        this.authors = objectFactory.listProperty(String.class).convention(Collections.emptyList());

        this.nodes = new HashMap<>();
        this.fairyModules = new ArrayList<>();
        this.libraries = new ArrayList<>();
    }

    public void bukkitApi(String api) {
        this.properties(PlatformType.BUKKIT).put("api-version", api);
    }

    public void load(String load) {
        this.properties(PlatformType.BUKKIT).put("load", load);
    }

    public void prefix(String prefix) {
        this.properties(PlatformType.BUKKIT).put("prefix", prefix);
    }

    public void module(String name) {
        FairyPlugin.INSTANCE.checkIdeIdentityState();
        try {
            String module = null;
            for (PlatformType platformType : this.fairyPlatforms.get()) {
                module = platformType.searchModuleName(name, this.fairyVersion.get());
                if (module != null) {
                    break;
                }
            }
            if (this.localRepo.get() && module == null) {
                module = name;
            }
            if (module == null) {
                throw new IllegalArgumentException("Couldn't find module " + name);
            }
            this.getFairyModules().add(module);
        } catch (Exception ex) {
            SneakyThrow.sneaky(ex);
        }
    }

    public void platform(String platformName) {
        this.fairyPlatforms.add(PlatformType.valueOf(platformName.toUpperCase()));
    }

    public void library(String library) {
        this.libraries.add(new Lib(library, null));
    }

    public void library(String library, String repository) {
        this.libraries.add(new Lib(library, repository));
    }

    public Map<String, String> properties(PlatformType type) {
        return this.nodes.computeIfAbsent(type, t -> new HashMap<>(1));
    }

}
