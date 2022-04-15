package io.fairyproject.bootstrap.app;

import io.fairyproject.FairyPlatform;
import io.fairyproject.app.FairyAppPlatform;
import io.fairyproject.bootstrap.BaseBootstrap;
import io.fairyproject.bootstrap.type.PlatformType;
import io.fairyproject.plugin.Plugin;
import org.jetbrains.annotations.Nullable;

public class AppBootstrap extends BaseBootstrap {

    public static AppBootstrap INSTANCE;
    public static boolean FAIRY_READY = false;

    public AppBootstrap() {
        super();
    }

    @Override
    protected void onFailure(@Nullable Throwable throwable) {
        if (throwable != null) {
            throwable.printStackTrace();
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
