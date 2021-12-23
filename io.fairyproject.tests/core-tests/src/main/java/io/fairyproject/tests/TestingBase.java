package io.fairyproject.tests;

import io.fairyproject.Debug;
import io.fairyproject.FairyPlatform;
import io.fairyproject.library.Library;
import io.fairyproject.plugin.Plugin;
import io.fairyproject.plugin.PluginAction;
import io.fairyproject.plugin.PluginDescription;
import io.fairyproject.plugin.PluginManager;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ScanResult;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.BeforeClass;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;
import java.util.List;

public abstract class TestingBase {

    private static boolean INITIALIZED = false;

    @BeforeClass
    public static void init() throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        setup();
    }

    public static void setup() throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        if (INITIALIZED) {
            return;
        }
        INITIALIZED = true;

        Class<?> testingHandleClass;
        try (ScanResult scanResult = new ClassGraph().enableAllInfo().addClassLoader(TestingBase.class.getClassLoader()).scan()) {
            final List<Class<?>> classes = scanResult.getClassesImplementing(TestingHandle.class.getName()).loadClasses();
            if (classes.isEmpty()) {
                throw new IllegalStateException("Couldn't find class with TestingHandle.");
            }
            testingHandleClass = classes.get(0);
        }

        final TestingHandle testingHandle = (TestingHandle) testingHandleClass.getDeclaredConstructor().newInstance();
        final Plugin plugin = testingHandle.plugin();

        Debug.UNIT_TEST = true;
        FairyPlatform fairyPlatform = testingHandle.platform();
        FairyPlatform.INSTANCE = fairyPlatform;

        fairyPlatform.load();
        if (testingHandle.shouldInitialize()) {
            PluginDescription description = PluginDescription.builder()
                    .name("unitTesting")
                    .shadedPackage(testingHandle.scanPath())
                    .build();
            plugin.initializePlugin(description, new PluginAction() {
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
            }, plugin.getClass().getClassLoader());
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
