package io.fairyproject.gradle;

import io.fairyproject.gradle.file.FileGenerator;
import io.fairyproject.gradle.file.FileGeneratorBukkit;
import io.fairyproject.gradle.util.MavenUtil;
import org.gradle.api.Project;

import java.io.IOException;

public enum PlatformType {

    BUKKIT {
        @Override
        public FileGenerator createFileGenerator() {
            return new FileGeneratorBukkit();
        }

        @Override
        public PlatformType[] inherited() {
            return new PlatformType[] { MC };
        }

        @Override
        public void applyDependencies(Project project, FairyExtension extension) throws IOException {
            String groupId = "dev.imanity.mockbukkit";
            String version = extension.properties(PlatformType.BUKKIT).getOrDefault("api-version", "1.16");
            String artifactId = "MockBukkit" + version;

            project.getDependencies().add("testImplementation",
                    groupId + ":" + artifactId + ":" + MavenUtil.getLatest(groupId, artifactId)
            );
        }
    },
    BUNGEE,
    MC,
    VELOCITY,
    NUKKIT,
    APP {
        @Override
        public FileGenerator createFileGenerator() {
            return null;
        }
    };

    public String getDependencyName() {
        return this.name().toLowerCase();
    }

    public FileGenerator createFileGenerator() {
        throw new UnsupportedOperationException("Platform " + this.name() + " hasn't been supported.");
    }

    public void applyDependencies(Project project, FairyExtension extension) throws IOException {
    }

    // name in parameter shouldn't contains platform prefix or "module." for example module.tablist should be tablist in input, bukkit-xseries should be xseries
    public String searchModuleName(String name) throws IOException {
        String main = this.name().toLowerCase() + "-" + name;
        if (MavenUtil.isExistingModule(main)) {
            return main;
        }
        for (PlatformType platformType : this.inherited()) {
            main = platformType.name().toLowerCase() + "-" + name;
            if (MavenUtil.isExistingModule(main)) {
                return main;
            }
        }
        main = "core-" + name;
        if (MavenUtil.isExistingModule(main)) {
            return main;
        }

        main = "module." + name;
        if (MavenUtil.isExistingModule(main)) {
            return main;
        }
        return null;
    }

    public PlatformType[] inherited() {
        return new PlatformType[0];
    }

}
