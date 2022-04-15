package io.fairyproject.tests.bukkit;

import io.fairyproject.bukkit.FairyBukkitPlatform;
import io.fairyproject.bukkit.reflection.minecraft.OBCVersion;
import io.fairyproject.mc.MCInitializer;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.lang.reflect.Field;

public abstract class FairyBukkitTestingPlatform extends FairyBukkitPlatform {

    public FairyBukkitTestingPlatform() {
        super(new File("build/tmp/fairy"));
        OBCVersion.forceSet(this.version());
//        MCProtocol.OVERWRITTEN_VERSION = this.version().toMCVersion(); // TODO
    }

    @Override
    public MCInitializer createMCInitializer() {
        return new BukkitTestingMCInitializer();
    }

    public abstract OBCVersion version();

    public static void patchBukkitPlugin(JavaPlugin plugin) throws ClassNotFoundException, NoSuchFieldException, IllegalAccessException {
        final Class<?> type = Class.forName("io.fairyproject.bootstrap.bukkit.BukkitPlugin");

        final Field field = type.getDeclaredField("INSTANCE");
        field.setAccessible(true);
        field.set(null, plugin);
    }
}
