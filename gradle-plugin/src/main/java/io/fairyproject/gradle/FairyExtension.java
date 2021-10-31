package io.fairyproject.gradle;

import io.fairyproject.gradle.util.VersionRetrieveUtil;
import lombok.Getter;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;

import javax.inject.Inject;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@Getter
public class FairyExtension {

    private static String LATEST = "0.5b1";

    static {
        try {
            LATEST = VersionRetrieveUtil.getLatest();
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }

    // Fairy
    private final Property<String> fairyVersion;
    private final ListProperty<PlatformType> fairyPlatforms;
    private final ListProperty<String> fairyModules;
    private final ListProperty<String> fairyExtensions;

    // Libraries
    private final Property<String> aspectJVersion;

    // Plugin
    private final Property<String> classifier;
    private final Property<String> mainPackage;
    private final Property<String> name;
    private final Property<String> description;
    private final Property<String> version;
    private final ListProperty<String> authors;

    private final Map<PlatformType, Map<String, String>> nodes;

    @Inject
    public FairyExtension(ObjectFactory objectFactory) {
        // Fairy
        this.fairyVersion = objectFactory.property(String.class).convention(LATEST);
        this.fairyPlatforms = objectFactory.listProperty(PlatformType.class).convention(Collections.singleton(PlatformType.BUKKIT));
        this.fairyModules = objectFactory.listProperty(String.class).convention(Collections.emptyList());
        this.fairyExtensions = objectFactory.listProperty(String.class).convention(Collections.emptyList());

        // Libraries
        this.aspectJVersion = objectFactory.property(String.class).convention("1.9.7");

        // Plugin
        this.classifier = objectFactory.property(String.class).convention("all");
        this.mainPackage = objectFactory.property(String.class);
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

    public void module(String name) {
        this.fairyModules.add(name);
    }

    public void platform(String platformName) {
        this.fairyPlatforms.add(PlatformType.valueOf(platformName.toUpperCase()));
    }

    public Map<String, String> properties(PlatformType type) {
        return this.nodes.computeIfAbsent(type, t -> new HashMap<>(1));
    }

}
