package io.fairyproject.tests.bukkit;

import io.fairyproject.bukkit.FairyBukkitPlatform;
import io.fairyproject.mc.MCInitializer;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.lang.reflect.Field;

public class FairyBukkitTestingPlatform extends FairyBukkitPlatform {

    public FairyBukkitTestingPlatform() {
        super(new File("build/tmp/fairy"));
    }

    @Override
    public MCInitializer createMCInitializer() {
        return new BukkitTestingMCInitializer();
    }

    public static void patchBukkitPlugin(JavaPlugin plugin) throws NoSuchFieldException, IllegalAccessException {
        final Class<?> type;
        try {
            type = Class.forName("io.fairyproject.bootstrap.bukkit.BukkitPlugin");
        } catch (ClassNotFoundException ex) {
            return;
        }

        final Field field = type.getDeclaredField("INSTANCE");
        field.setAccessible(true);
        field.set(null, plugin);
    }
}
