package io.fairyproject.bootstrap.app;

import io.fairyproject.FairyPlatform;
import io.fairyproject.app.FairyAppPlatform;
import io.fairyproject.bootstrap.platform.AbstractPlatformBootstrap;
import io.fairyproject.bootstrap.type.PlatformType;
import io.fairyproject.log.Log;
import org.jetbrains.annotations.Nullable;

public class AppBootstrap extends AbstractPlatformBootstrap {

    @Override
    protected void onFailure(@Nullable Throwable throwable) {
        if (throwable != null) {
            Log.error("An exception was thrown", throwable);
        }
        System.exit(-1);
    }

    @Override
    protected PlatformType getPlatformType() {
        return PlatformType.APP;
    }

    @Override
    protected FairyPlatform createPlatform() {
        return new FairyAppPlatform();
    }
}
