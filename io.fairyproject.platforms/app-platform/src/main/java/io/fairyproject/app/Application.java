package io.fairyproject.app;

import io.fairyproject.plugin.Plugin;

public abstract class Application extends Plugin {

    @Override
    public final void onPluginEnable() {
        this.onAppEnable();
    }

    @Override
    public final void onPluginDisable() {
        this.onAppDisable();
    }

    /**
     * The method that will be called on application starting
     */
    public void onAppEnable() {
    }

    /**
     * The method that will be called on application shutting down
     */
    public void onAppDisable() {
    }
}
