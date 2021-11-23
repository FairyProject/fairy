package io.fairytest.plugin;

import io.fairyproject.library.Library;
import io.fairyproject.plugin.Plugin;
import io.fairyproject.plugin.PluginAction;
import io.fairyproject.plugin.PluginDescription;
import org.apache.commons.lang3.tuple.Pair;

import java.io.File;
import java.nio.file.Path;

public class PluginMock extends Plugin {

    public PluginMock() {
        PluginDescription description = PluginDescription.builder()
                .name("test")
                .mainClass("io.fairytest.plugin.PluginMock")
                .shadedPackage("io.fairytest.plugin")
                .module(Pair.of("module.command", "0.0.1b1"))
                .library(Library.builder()
                        .gradle("com.google.guava:guava:31.0.1-jre")
                        .build())
                .build();
        this.initializePlugin(description, new PluginAction() {
            @Override
            public void close() {
                System.exit(-1);
            }

            @Override
            public boolean isClosed() {
                return false;
            }

            @Override
            public Path getDataFolder() {
                return new File(".").toPath();
            }
        }, this.getClass().getClassLoader());
    }

}
