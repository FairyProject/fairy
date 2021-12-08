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

    public void onAppEnable() {
    }

    public void onAppDisable() {
    }
}
