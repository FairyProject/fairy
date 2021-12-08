package io.fairyproject.bootstrap.app;

import io.fairyproject.bootstrap.BaseBootstrap;
import io.fairyproject.bootstrap.BasePlatformBridge;
import io.fairyproject.bootstrap.type.PlatformType;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.nio.file.Path;

public class AppBootstrap extends BaseBootstrap {

    public static AppBootstrap INSTANCE;
    public static boolean FAIRY_READY = false;

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
    protected BasePlatformBridge createPlatformBridge() {
        return new AppPlatformBridge();
    }

    @Override
    protected Path getBootstrapDirectory() {
        return new File("fairy").toPath();
    }
}
