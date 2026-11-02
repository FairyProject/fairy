package io.fairyproject.tests.bukkit;

import io.fairyproject.bukkit.FairyBukkitPlatform;
import io.fairyproject.mc.protocol.PacketEventsBuilder;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.lang.reflect.Field;

public class FairyBukkitTestingPlatform extends FairyBukkitPlatform {

    public FairyBukkitTestingPlatform() {
        super(new File("build/tmp/fairy"));
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

    @Override
    protected void loadProtocol() {
        // Do nothing
    }

    @Override
    public PacketEventsBuilder providePacketEventBuilder() {
        throw new IllegalStateException("providePacketEventBuilder() should not be called in tests");
    }
}
