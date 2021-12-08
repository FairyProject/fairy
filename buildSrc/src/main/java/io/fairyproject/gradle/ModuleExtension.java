package io.fairyproject.gradle;

import lombok.Getter;
import org.gradle.api.Project;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.MapProperty;
import org.gradle.api.provider.Property;

import javax.inject.Inject;
import java.util.Collections;

@Getter
public class ModuleExtension {

    private final Property<String> name;
    private final Property<String> classPath;
    private final Property<Boolean> abstraction;
    private final ListProperty<String> depends;
    private final ListProperty<String> subDepends;
    private final ListProperty<String> platforms;
    private final ListProperty<Lib> libraries;
    // <module, package>
    private final MapProperty<String, String> exclusives;

    @Inject
    public ModuleExtension(ObjectFactory objectFactory, Project project) {
        this.name = objectFactory.property(String.class).convention(project.getName());
        this.classPath = objectFactory.property(String.class).convention("io.fairyproject");
        this.abstraction = objectFactory.property(Boolean.class).convention(false);
        this.depends = objectFactory.listProperty(String.class).convention(Collections.emptyList());
        this.subDepends = objectFactory.listProperty(String.class).convention(Collections.emptyList());
        this.platforms = objectFactory.listProperty(String.class).convention(Collections.emptyList());
        this.libraries = objectFactory.listProperty(Lib.class).convention(Collections.emptyList());
        this.exclusives = objectFactory.mapProperty(String.class, String.class).convention(Collections.emptyMap());
    }

    public void depend(String name) {
        this.depends.add(name);
    }

    public void subDepend(String name) {
        this.subDepends.add(name);
    }

    public void platform(String name) {
        this.platforms.add(name);
    }

    public void library(String gradleDependency) {
        this.libraries.add(new Lib(gradleDependency, null));
    }

    public void library(String gradleDependency, String repository) {
        this.libraries.add(new Lib(gradleDependency, repository));
    }

    public void exclude(String module, String curPackage) {
        this.exclusives.put(curPackage, module);
    }

}
