package io.fairytest;

import io.fairyproject.library.Library;
import io.fairyproject.plugin.Plugin;
import io.fairyproject.plugin.PluginAction;
import io.fairyproject.plugin.PluginDescription;

import java.io.File;
import java.nio.file.Path;

public class PluginMock extends Plugin {

    public PluginMock() {
        PluginDescription description = PluginDescription.builder()
                .name("test")
                .mainClass("io.fairytest.plugin.PluginMock")
                .shadedPackage("io.fairytest.plugin")
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
