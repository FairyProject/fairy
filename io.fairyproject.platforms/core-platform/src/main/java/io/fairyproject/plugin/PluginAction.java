package io.fairyproject.plugin;

public interface PluginAction {

    void close() throws Exception;

    boolean isClosed();

}
