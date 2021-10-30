package io.fairyproject.bootstrap.app;

import io.fairyproject.FairyPlatform;
import io.fairyproject.app.FairyAppPlatform;
import io.fairyproject.bootstrap.BasePlatformBridge;

public class AppPlatformBridge extends BasePlatformBridge {
    @Override
    public FairyPlatform createPlatform() {
        return new FairyAppPlatform();
    }
}
