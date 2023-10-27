package io.fairyproject.bootstrap.app;

import io.fairyproject.Fairy;
import io.fairyproject.app.Application;
import io.fairyproject.app.FairyAppPlatform;
import io.fairyproject.bootstrap.instance.AbstractPluginInstance;
import io.fairyproject.plugin.PluginAction;
import io.fairyproject.plugin.initializer.PluginClassInitializer;

public class ApplicationInstance extends AbstractPluginInstance {

    public ApplicationInstance(PluginClassInitializer initializer) {
        super(initializer);
    }

    @Override
    protected ClassLoader getClassLoader() {
        return ClassLoader.getSystemClassLoader();
    }

    @Override
    protected PluginAction getPluginAction() {
        return new ApplicationAction(this);
    }

    @Override
    public void onLoad() {
        ((FairyAppPlatform) Fairy.getPlatform()).setMainApplication((Application) this.plugin);
        super.onLoad();
    }
}
