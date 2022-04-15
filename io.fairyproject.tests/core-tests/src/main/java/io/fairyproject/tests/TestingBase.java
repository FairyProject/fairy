package io.fairyproject.tests;

import io.fairyproject.Debug;
import io.fairyproject.FairyPlatform;
import io.fairyproject.container.controller.AutowiredContainerController;
import io.fairyproject.plugin.Plugin;
import io.fairyproject.plugin.PluginAction;
import io.fairyproject.plugin.PluginDescription;
import io.fairyproject.plugin.PluginManager;
import io.fairyproject.util.exceptionally.SneakyThrowUtil;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ScanResult;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public abstract class TestingBase {

    private static boolean INITIALIZED = false;

    public static boolean isInitialized() {
        return INITIALIZED;
    }

    @BeforeAll
    public static void init() throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        setup();
    }

    @BeforeEach
    public void beforeEach() {
        try {
            AutowiredContainerController.INSTANCE.applyObject(this);
        } catch (ReflectiveOperationException e) {
            SneakyThrowUtil.sneakyThrow(e);
        }
    }

    public static void setup() throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        if (INITIALIZED) {
            return;
        }

        setup(findTestingHandle());
    }

    public static void setup(TestingHandle testingHandle) {
        if (INITIALIZED) {
            return;
        }
        INITIALIZED = true;

        Debug.UNIT_TEST = true;
        final Plugin plugin = testingHandle.plugin();
        FairyPlatform fairyPlatform = testingHandle.platform();
        FairyPlatform.INSTANCE = fairyPlatform;

        fairyPlatform.load(plugin);

        PluginDescription description = PluginDescription.builder()
                .name("unitTesting")
                .shadedPackage(testingHandle.scanPath())
                .build();
        PluginAction pluginAction = new PluginAction() {
            @Override
            public void close() {
                throw new IllegalStateException("close() shouldn't be called in unit testing.");
            }

            @Override
            public boolean isClosed() {
                return false;
            }

            @Override
            public Path getDataFolder() {
                return new File(".").toPath();
            }
        };

        CompletableFuture<Plugin> completableFuture = new CompletableFuture<>();
        PluginManager.INSTANCE.onPluginPreLoaded(testingHandle.getClass().getClassLoader(), description, pluginAction, completableFuture);
        completableFuture.complete(plugin);

        if (testingHandle.shouldInitialize()) {
            plugin.initializePlugin(description, pluginAction, plugin.getClass().getClassLoader());
        }
        PluginManager.INSTANCE.addPlugin(plugin);
        PluginManager.INSTANCE.onPluginInitial(plugin);
        plugin.onInitial();

        fairyPlatform.enable();
        plugin.onPreEnable();
        PluginManager.INSTANCE.onPluginEnable(plugin);
        plugin.onPluginEnable();
    }

    public static TestingHandle findTestingHandle() throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        Class<?> testingHandleClass = null;
        try (ScanResult scanResult = new ClassGraph().enableAllInfo().addClassLoader(TestingBase.class.getClassLoader()).scan()) {
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

        return (TestingHandle) testingHandleClass.getDeclaredConstructor().newInstance();
    }

}
