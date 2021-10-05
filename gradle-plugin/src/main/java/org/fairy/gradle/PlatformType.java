package org.fairy.gradle;

import org.fairy.gradle.file.FileGenerator;
import org.fairy.gradle.file.FileGeneratorBukkit;

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
    INDEPENDENT;

    public String getDependencyName() {
        return this.name().toLowerCase();
    }

    public FileGenerator createFileGenerator() {
        throw new UnsupportedOperationException("Platform " + this.name() + " hasn't been supported.");
    }

}
