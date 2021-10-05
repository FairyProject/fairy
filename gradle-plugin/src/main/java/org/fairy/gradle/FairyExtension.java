package org.fairy.gradle;

import lombok.Getter;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.MapProperty;
import org.gradle.api.provider.Property;

import javax.inject.Inject;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Getter
public class FairyExtension {

    // Fairy
    private final Property<String> fairyVersion;
    private final ListProperty<PlatformType> fairyPlatforms;
    private final MapProperty<String, String> fairyModules;
    private final ListProperty<String> fairyExtensions;

    // Libraries
    private final Property<String> aspectJVersion;

    // Plugin
    private final Property<String> classifier;
    private final Property<String> shadedPackage;
    private final Property<String> name;
    private final Property<String> description;
    private final Property<String> version;
    private final ListProperty<String> authors;

    private final Map<PlatformType, Map<String, String>> nodes;

    @Inject
    public FairyExtension(ObjectFactory objectFactory) {
        // Fairy
        this.fairyVersion = objectFactory.property(String.class).convention("0.5b1");
        this.fairyPlatforms = objectFactory.listProperty(PlatformType.class).convention(Collections.singleton(PlatformType.BUKKIT));
        this.fairyModules = objectFactory.mapProperty(String.class, String.class).convention(Collections.emptyMap());
        this.fairyExtensions = objectFactory.listProperty(String.class).convention(Collections.emptyList());

        // Libraries
        this.aspectJVersion = objectFactory.property(String.class).convention("1.9.7");

        // Plugin
        this.classifier = objectFactory.property(String.class).convention("all");
        this.shadedPackage = objectFactory.property(String.class);
        this.name = objectFactory.property(String.class);
        this.description = objectFactory.property(String.class);
        this.version = objectFactory.property(String.class);
        this.authors = objectFactory.listProperty(String.class).convention(Collections.emptyList());

        this.nodes = new HashMap<>();
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

    public Map<String, String> properties(PlatformType type) {
        return this.nodes.computeIfAbsent(type, t -> new HashMap<>(1));
    }

}
