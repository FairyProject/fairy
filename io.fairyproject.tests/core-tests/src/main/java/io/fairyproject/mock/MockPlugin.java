package io.fairyproject.mock;

import io.fairyproject.library.Library;
import io.fairyproject.plugin.Plugin;
import io.fairyproject.plugin.PluginAction;
import io.fairyproject.plugin.PluginDescription;
import lombok.Getter;

import java.io.File;
import java.nio.file.Path;

@Getter
public class MockPlugin extends Plugin {

    private boolean onInitialCalled = false;
    private boolean onPreEnableCalled = false;
    private boolean onPostEnableCalled = false;
    private boolean onPreDisableCalled = false;
    private boolean onPostDisableCalled = false;

    public MockPlugin() {
        this("test");
    }

    public MockPlugin(String name) {
        PluginDescription description = PluginDescription.builder()
                .name(name)
                .mainClass("io.fairytest.plugin.PluginMock")
                .shadedPackage("io.fairytest.plugin")
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

    @Override
    public void onInitial() {
        this.onInitialCalled = true;
    }

    @Override
    public void onPreEnable() {
        this.onPreEnableCalled = true;
    }

    @Override
    public void onPluginEnable() {
        this.onPostEnableCalled = true;
    }

    @Override
    public void onPluginDisable() {
        this.onPreDisableCalled = true;
    }

    @Override
    public void onFrameworkFullyDisable() {
        this.onPostDisableCalled = true;
    }
}
