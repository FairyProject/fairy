package io.fairyproject.tests;

import io.fairyproject.Debug;
import io.fairyproject.FairyPlatform;
import io.fairyproject.log.Log;
import io.fairyproject.plugin.Plugin;
import io.fairyproject.plugin.PluginAction;
import io.fairyproject.plugin.PluginDescription;
import io.fairyproject.plugin.PluginManager;
import io.fairyproject.tests.logger.DebugLogger;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ScanResult;
import lombok.Getter;
import lombok.SneakyThrows;

import java.io.File;
import java.lang.reflect.Modifier;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

public class TestingContext {

    private static TestingContext INSTANCE;

    private final AtomicBoolean initialized = new AtomicBoolean(false);
    private final Object lock = new Object();

    private TestingHandle testingHandle;
    @Getter
    private boolean clean;
    private Plugin plugin;

    public void initialize() {
        if (this.initialized.get()) {
            return;
        }

        this.initialize(this.findTestingHandle());
        this.clean = true;
    }

    public void initialize(TestingHandle testingHandle) {
        synchronized (this.lock) {
            if (!this.initialized.compareAndSet(false, true)) {
                return;
            }
            Log.set(new DebugLogger());
            testingHandle.onPreInitialization();

            Debug.UNIT_TEST = true;
            this.plugin = testingHandle.plugin();
            FairyPlatform fairyPlatform = testingHandle.platform();
            FairyPlatform.INSTANCE = fairyPlatform;

            PluginDescription description = PluginDescription.builder()
                    .name("unitTesting")
                    .shadedPackage(testingHandle.scanPath())
                    .build();
            PluginAction pluginAction = new PluginAction() {
                @Override
                public void close() {
                    shutdown();
                }

                @Override
                public boolean isClosed() {
                    return !initialized.get();
                }

                @Override
                public Path getDataFolder() {
                    return new File("build/tmp/fairy").toPath();
                }
            };

            fairyPlatform.preload();

            CompletableFuture<Plugin> completableFuture = new CompletableFuture<>();
            PluginManager.INSTANCE.onPluginPreLoaded(testingHandle.getClass().getClassLoader(), description, pluginAction, completableFuture);
            completableFuture.complete(plugin);

            fairyPlatform.load(plugin);

            if (testingHandle.shouldInitialize()) {
                plugin.initializePlugin(description, pluginAction, plugin.getClass().getClassLoader());

                // add test classpath to the plugin classloader
                plugin.getClassLoaderRegistry().addUrl(this.getClass().getProtectionDomain().getCodeSource().getLocation());
            }
            PluginManager.INSTANCE.addPlugin(plugin);
            PluginManager.INSTANCE.onPluginInitial(plugin);
            plugin.onInitial();

            fairyPlatform.enable();
            plugin.onPreEnable();
            PluginManager.INSTANCE.onPluginEnable(plugin);
            plugin.onPluginEnable();
        }
    }

    public void setDirty() {
        this.clean = false;
    }

    public void shutdown() {
        synchronized (this.lock) {
            if (!this.initialized.compareAndSet(true, false)) {
                return;
            }

            this.plugin.onPluginDisable();
            PluginManager.INSTANCE.onPluginDisable(this.plugin);
            if (FairyPlatform.INSTANCE != null)
                FairyPlatform.INSTANCE.disable();
            PluginManager.INSTANCE.unload();
            this.plugin.onFrameworkFullyDisable();
        }
    }

    @SneakyThrows
    public TestingHandle findTestingHandle() {
        if (this.testingHandle == null) {
            Class<?> testingHandleClass = null;
            try (ScanResult scanResult = new ClassGraph().enableAllInfo().addClassLoader(TestingContext.class.getClassLoader()).scan()) {
                final List<Class<?>> classes = scanResult.getClassesImplementing(TestingHandle.class.getName()).loadClasses();

                for (Class<?> aClass : classes) {
                    if (!aClass.isInterface() && !Modifier.isAbstract(aClass.getModifiers())) {
                        testingHandleClass = aClass;
                        break;
                    }
                }
            }
            if (testingHandleClass == null) {
                throw new IllegalStateException("Couldn't find class with TestingHandle.");
            }

            this.testingHandle = (TestingHandle) testingHandleClass.getDeclaredConstructor().newInstance();
        }
        return this.testingHandle;
    }

    public synchronized boolean isInitialized() {
        return this.initialized.get();
    }

    public static TestingContext get() {
        if (INSTANCE == null) {
            INSTANCE = new TestingContext();
        }
        return INSTANCE;
    }
}
