package io.fairyproject.gradle;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.fairyproject.gradle.util.MavenUtil;
import lombok.Getter;
import org.apache.commons.lang3.tuple.Pair;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.artifacts.ModuleDependency;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

@Getter
public class ModuleDependencyTreeLoader {

    private final Project project;
    private final Configuration fairyConfiguration;
    private final Configuration downloaderConfiguration;
    private final Set<String> allLoadedModules;
    private final Multimap<String, String> exclusives;

    private ModuleDependencyTreeLoader(Project project, Configuration fairyConfiguration, Configuration downloaderConfiguration) {
        this.project = project;
        this.fairyConfiguration = fairyConfiguration;
        this.downloaderConfiguration = downloaderConfiguration;
        this.allLoadedModules = new HashSet<>();
        this.exclusives = HashMultimap.create();
    }

    public void load(String moduleName, Dependency dependency) throws IOException {
        this.load(moduleName, dependency, new ArrayList<>());
    }

    private void load(String moduleName, Dependency dependency, List<String> moduleTree) throws IOException {
        boolean contains = moduleTree.contains(moduleName);
        moduleTree.add(moduleName);
        if (contains) {
            this.throwCircularDependency(moduleName, moduleTree);
        }

        if (allLoadedModules.contains(moduleName)) {
            return;
        }
        allLoadedModules.add(moduleName);

        fairyConfiguration.getDependencies().add(dependency);
        project.getDependencies().add("testCompileOnly", dependency);

        this.loadModuleJar(dependency, moduleTree);
    }

    private void loadModuleJar(Dependency dependency, List<String> moduleTree) throws IOException {
        // copy for retrieve files
        final Configuration downloader = downloaderConfiguration.copy();
        downloader.getDependencies().add(dependency);
        for (File file : downloader.files(dependency)) {
            JarFile jarFile = new JarFile(file);
            final ZipEntry entry = jarFile.getEntry("module.json");
            if (entry != null) {
                final JsonObject jsonObject = FairyPlugin.GSON.fromJson(new InputStreamReader(jarFile.getInputStream(entry)), JsonObject.class);

                if (jsonObject.has("libraries")) {
                    this.loadModuleLibrary(jsonObject.getAsJsonArray("libraries"));
                }

                if (jsonObject.has("exclusive")) {
                    final JsonObject exclusive = jsonObject.getAsJsonObject("exclusive");

                    for (Map.Entry<String, JsonElement> e : exclusive.entrySet()) {
                        this.exclusives.put(e.getValue().getAsString(), e.getKey());
                    }
                }

                for (Pair<String, Dependency> pair : this.readDependencyList(jsonObject)) {
                    final List<String> tree = new ArrayList<>(moduleTree);
                    this.load(pair.getKey(), pair.getValue(), tree);
                }
            }
        }
    }

    private void loadModuleLibrary(JsonArray jsonArray) {
        for (JsonElement element : jsonArray) {
            final JsonObject library = element.getAsJsonObject();
            final String tag = library.get("dependency").getAsString();
            if (library.has("repository")) {
                final String repository = library.get("repository").getAsString();
                this.project.getRepositories().maven(mavenArtifactRepository -> mavenArtifactRepository.setUrl(repository));
            }
            this.fairyConfiguration.getDependencies().add(this.project.getDependencies().create(tag));
        }
    }

    private void throwCircularDependency(String moduleName, List<String> moduleTree) {
        StringJoiner stringBuilder = new StringJoiner(" -> ");
        for (String name : moduleTree) {
            if (name.equals(moduleName)) {
                stringBuilder.add("*" + name + "*");
            } else {
                stringBuilder.add(name);
            }
        }
        throw new IllegalStateException("Circular dependency: " + stringBuilder);
    }

    private List<Pair<String, Dependency>> readDependencyList(JsonObject jsonObject) throws IOException {
        List<Pair<String, Dependency>> dependencyList = new ArrayList<>();
        if (jsonObject.has("depends")) {
            for (JsonElement element : jsonObject.getAsJsonArray("depends")) {
                this.readDependencyString(element.getAsString(), dependencyList);
            }
        }
        return dependencyList;
    }

    private void readDependencyString(String dependencyTag, List<Pair<String, Dependency>> dependencyList) throws IOException {
        String[] split = dependencyTag.split(":");
        String name = split[0];
        String version = split[1];
        if (version == null) {
            version = MavenUtil.getLatest(name);
        }
        if (FairyPlugin.IS_IN_IDE) {
            Project dependProject = this.project.project(IDEDependencyLookup.getIdentityPath(name));
            ModuleDependency dependency = (ModuleDependency) this.project.getDependencies().create(dependProject);
            dependency.setTargetConfiguration("shadow");

            dependencyList.add(Pair.of(name, dependency));
        } else {
            String formatDependencyTag = String.format(FairyPlugin.DEPENDENCY_FORMAT, name, version);
            dependencyList.add(Pair.of(name, this.project.getDependencies().create(formatDependencyTag)));
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private Project project;
        private Configuration fairyConfiguration;
        private Configuration downloaderConfiguration;

        public Builder project(Project project) {
            this.project = project;
            return this;
        }

        public Builder configuration(Configuration configuration) {
            this.fairyConfiguration = configuration;
            return this;
        }

        public Builder downloaderConfiguration(Configuration downloaderConfiguration) {
            this.downloaderConfiguration = downloaderConfiguration;
            return this;
        }

        public ModuleDependencyTreeLoader build() {
            return new ModuleDependencyTreeLoader(this.project, this.fairyConfiguration, this.downloaderConfiguration);
        }

    }

}
