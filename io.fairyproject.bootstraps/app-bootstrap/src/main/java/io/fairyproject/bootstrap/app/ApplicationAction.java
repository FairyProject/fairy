package io.fairyproject.bootstrap.app;

import io.fairyproject.plugin.PluginAction;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ApplicationAction implements PluginAction {

    private final ApplicationHolder applicationHolder;
    private boolean closed = false;

    @Override
    public void close() {
        this.closed = true;
        this.applicationHolder.onDisable();

        System.exit(-1);
    }

    @Override
    public boolean isClosed() {
        return this.closed;
    }
}
