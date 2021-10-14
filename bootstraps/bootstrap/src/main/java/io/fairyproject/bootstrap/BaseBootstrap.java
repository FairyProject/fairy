package io.fairyproject.bootstrap;

import com.google.common.base.Preconditions;
import io.fairyproject.bootstrap.type.PlatformType;
import io.fairyproject.bootstrap.util.DownloadUtil;
import io.fairyproject.bootstrap.util.FairyClassLoader;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public abstract class BaseBootstrap {

    public static ClassLoader CLASS_LOADER;
    public static void join(@NotNull Runnable runnable) {
        Preconditions.checkNotNull(CLASS_LOADER);

        final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(CLASS_LOADER);
        try {
            runnable.run();
        } finally {
            Thread.currentThread().setContextClassLoader(classLoader);
        }
    }

    private BasePlatformBridge platformBridge;

    public final void load() {
        if (!this.trySearchBootstraps()) {
            return;
        }

        try {
            final Path directory = this.getBootstrapDirectory();
            if (!Files.exists(directory)) {
                Files.createDirectories(directory);
            }

            Path jarPath = this.getOrDownloadCore();
            CLASS_LOADER = new FairyClassLoader(jarPath);

            BaseBootstrap.join(() -> {
                this.platformBridge = this.createPlatformBridge();
                this.platformBridge.load();
            });
        } catch (Throwable throwable) {
            this.onFailure(throwable);
        }
    }

    public final void enable() {
        if (this.platformBridge == null) {
            return;
        }
        BaseBootstrap.join(() -> this.platformBridge.enable());
    }

    public final void disable() {
        if (this.platformBridge == null) {
            return;
        }
        BaseBootstrap.join(() -> this.platformBridge.disable());
    }

    @NotNull
    private Path getOrDownloadCore() throws IOException {
        Path directory = this.getBootstrapDirectory();
        Path file = directory.resolve("fairy.jar");
        if (Files.exists(file)) {
            return file;
        }

        return DownloadUtil.download(file, this.getPlatformType().name().toLowerCase());
    }

    protected abstract void onFailure(@Nullable Throwable throwable);

    protected abstract PlatformType getPlatformType();

    protected abstract BasePlatformBridge createPlatformBridge();

    protected abstract Path getBootstrapDirectory();

    protected boolean trySearchBootstraps() {
        try {
            final Class<?> fairyClass = Class.forName("org.fairy.Fairy");
            BaseBootstrap.CLASS_LOADER = fairyClass.getClassLoader();

            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

}
