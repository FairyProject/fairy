package io.fairyproject.gradle;

import io.fairyproject.gradle.util.SneakyThrow;
import io.fairyproject.gradle.util.MavenUtil;
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

    // Libraries
    private final Property<String> aspectJVersion;

    // Plugin
    private final Property<String> classifier;
    private final Property<String> mainPackage;
    private final Property<String> name;
    private final Property<String> description;
    private final Property<String> version;
    private final Property<Boolean> libraryMode;
    private final ListProperty<String> authors;

    // Specify for debug
    private final Property<Boolean> fairyIde;
    private final Property<Boolean> localRepo;

    private final Map<PlatformType, Map<String, String>> nodes;
    private final List<String> fairyModules;

    @Inject
    public FairyExtension(ObjectFactory objectFactory) {
        // Fairy
        this.fairyVersion = objectFactory.property(String.class);
        this.fairyPlatforms = objectFactory.listProperty(PlatformType.class).convention(Collections.singleton(PlatformType.BUKKIT));

        // Libraries
        this.aspectJVersion = objectFactory.property(String.class).convention("1.9.7");

        // Plugin
        this.classifier = objectFactory.property(String.class).convention("all");
        this.mainPackage = objectFactory.property(String.class);
        this.name = objectFactory.property(String.class);
        this.description = objectFactory.property(String.class);
        this.version = objectFactory.property(String.class);
        this.fairyIde = objectFactory.property(Boolean.class);
        this.localRepo = objectFactory.property(Boolean.class).convention(false);
        this.libraryMode = objectFactory.property(Boolean.class).convention(false);
        this.authors = objectFactory.listProperty(String.class).convention(Collections.emptyList());

        this.nodes = new HashMap<>();
        this.fairyModules = new ArrayList<>();
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
        FairyPlugin.QUEUE.add(() -> {
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
        });
    }

    public void platform(String platformName) {
        this.fairyPlatforms.add(PlatformType.valueOf(platformName.toUpperCase()));
    }

    public Map<String, String> properties(PlatformType type) {
        return this.nodes.computeIfAbsent(type, t -> new HashMap<>(1));
    }

}
