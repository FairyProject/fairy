package io.fairyproject.bootstrap;

import io.fairyproject.FairyPlatform;

public abstract class BasePlatformBridge {

    protected FairyPlatform platform;

    public abstract FairyPlatform createPlatform();

    public void load() {
        this.platform = this.createPlatform();
        this.platform.load();
    }

    public void enable() {
        this.platform.enable();
    }

    public void disable() {
        this.platform.disable();
    }

}
