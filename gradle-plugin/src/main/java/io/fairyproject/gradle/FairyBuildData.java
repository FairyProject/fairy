package io.fairyproject.gradle;

import com.google.common.collect.ImmutableList;
import lombok.Builder;
import lombok.Data;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Data
@Builder
public class FairyBuildData implements Serializable {

    private static final long serialVersionUID = 3L;

    public static FairyBuildData create(FairyExtension fairyExtension, Set<Lib> libraries) {
        return FairyBuildData.builder()
                .fairyVersion(fairyExtension.getFairyVersion().getOrElse(""))
                .fairyPlatforms(fairyExtension.getFairyPlatforms().get())
                .classifier(fairyExtension.getClassifier().get())
                .mainPackage(fairyExtension.getMainPackage().get())
                .name(fairyExtension.getName().get())
                .description(fairyExtension.getDescription().getOrNull())
                .libraryMode(fairyExtension.getLibraryMode().get())
                .authors(fairyExtension.getAuthors().get())
                .fairyIde(fairyExtension.getFairyIde().getOrElse(false))
                .localRepo(fairyExtension.getLocalRepo().get())
                .nodes(fairyExtension.getNodes())
                .fairyModules(fairyExtension.getFairyModules())
                .libraries(ImmutableList.copyOf(libraries))
                .build();
    }

    private String fairyVersion;
    private List<PlatformType> fairyPlatforms;

    // Libraries
    private String aspectJVersion;

    // Plugin
    private String classifier;
    private String mainPackage;
    private String name;
    private String description;
    private boolean libraryMode;
    private List<String> authors;
    private List<Lib> libraries;

    // Specify for debug
    private boolean fairyIde;
    private boolean localRepo;

    private Map<PlatformType, Map<String, String>> nodes;
    private List<String> fairyModules;

    public Map<String, String> properties(PlatformType type) {
        return this.nodes.computeIfAbsent(type, t -> new HashMap<>(1));
    }

    @SuppressWarnings("unused")
    private static void writeObject(ObjectOutputStream s) throws IOException {
        s.defaultWriteObject();
    }

    // Gradle only needs to serialize objects, so this isn't strictly needed
    @SuppressWarnings("unused")
    private static void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException {
        s.defaultReadObject();
    }

}
