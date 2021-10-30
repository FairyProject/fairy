package io.fairyproject.gradle;

import io.fairyproject.gradle.file.FileGenerator;
import io.fairyproject.gradle.file.FileGeneratorBukkit;

public enum PlatformType {

    BUKKIT {
        @Override
        public FileGenerator createFileGenerator() {
            return new FileGeneratorBukkit();
        }
    },
    BUNGEE,
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

}
