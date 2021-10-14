package io.fairyproject.bootstrap;

import org.fairy.FairyPlatform;

public abstract class BasePlatformBridge {

    private FairyPlatform platform;

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
