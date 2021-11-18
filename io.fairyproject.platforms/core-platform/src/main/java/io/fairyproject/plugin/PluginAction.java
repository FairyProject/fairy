package io.fairyproject.plugin;

import java.nio.file.Path;

public interface PluginAction {

    void close() throws Exception;

    boolean isClosed();

    Path getDataFolder();

}
