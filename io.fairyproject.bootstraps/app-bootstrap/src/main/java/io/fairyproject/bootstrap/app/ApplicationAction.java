package io.fairyproject.bootstrap.app;

import io.fairyproject.plugin.PluginAction;
import lombok.RequiredArgsConstructor;

import java.io.File;
import java.nio.file.Path;

@RequiredArgsConstructor
public class ApplicationAction implements PluginAction {

    private final ApplicationInstance applicationHolder;
    private boolean closed = false;

    @Override
    public void close() {
        this.closed = true;
        this.applicationHolder.onDisable();
        AppBootstrap.INSTANCE.disable();

        System.exit(-1);
    }

    @Override
    public boolean isClosed() {
        return this.closed;
    }

    @Override
    public Path getDataFolder() {
        return new File(".").toPath();
    }
}
