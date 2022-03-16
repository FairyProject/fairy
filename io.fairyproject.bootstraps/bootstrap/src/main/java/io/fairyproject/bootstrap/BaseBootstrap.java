package io.fairyproject.bootstrap;

import io.fairyproject.FairyPlatform;
import io.fairyproject.bootstrap.type.PlatformType;
import io.fairyproject.plugin.Plugin;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Files;
import java.nio.file.Path;

public abstract class BaseBootstrap {

    protected final Plugin plugin;
    private FairyPlatform fairyPlatform;

    public BaseBootstrap(Plugin plugin) {
        this.plugin = plugin;
    }

    public final boolean load() {
        if (FairyPlatform.class.getClassLoader() != this.getClass().getClassLoader()) {
            return true;
        }
        if (FairyPlatform.INSTANCE != null) {
            return true;
        }

        try {
            final Path directory = this.getBootstrapDirectory();
            if (!Files.exists(directory)) {
                Files.createDirectories(directory);
            }

            this.fairyPlatform = this.createPlatform();
            this.fairyPlatform.load();
        } catch (Throwable throwable) {
            this.onFailure(throwable);
            return false;
        }
        return true;
    }

    public final void enable() {
        if (this.fairyPlatform == null) {
            return;
        }
        this.fairyPlatform.enable();
    }

    public final void disable() {
        if (this.fairyPlatform == null) {
            return;
        }
        this.fairyPlatform.disable();
    }

    protected abstract void onFailure(@Nullable Throwable throwable);

    protected abstract PlatformType getPlatformType();

    protected abstract FairyPlatform createPlatform();

    protected abstract Path getBootstrapDirectory();

}
