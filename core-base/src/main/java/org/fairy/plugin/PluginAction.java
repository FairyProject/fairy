package org.fairy.plugin;

public interface PluginAction {

    void close() throws Exception;

    boolean isClosed();

}
