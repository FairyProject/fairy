package io.fairyproject.gradle;

import lombok.Builder;
import lombok.Data;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.MapProperty;
import org.gradle.api.provider.Property;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Data
@Builder
public class ModuleExtensionSerializable implements Serializable {

    private static final long serialVersionUID = 1L;

    public static ModuleExtensionSerializable create(ModuleExtension extension, Map<Lib, Boolean> libraries) {
        return ModuleExtensionSerializable.builder()
                .name(extension.getName().get())
                .classPath(extension.getClassPath().get())
                .abstraction(extension.getAbstraction().get())
                .depends(extension.getDepends().get())
                .subDepends(extension.getSubDepends().get())
                .platforms(extension.getPlatforms().get())
                .libraries(libraries.entrySet().stream()
                        .filter(entry -> !entry.getValue())
                        .map(Map.Entry::getKey)
                        .collect(Collectors.toList()))
                .exclusives(extension.getExclusives().get())
                .build();
    }

    private String name;
    private String classPath;
    private boolean abstraction;
    private List<String> depends;
    private List<String> subDepends;
    private List<String> platforms;
    private List<Lib> libraries;
    private Map<String, String> exclusives;

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
