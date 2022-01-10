package io.fairyproject.tests.bukkit;

import io.fairyproject.bukkit.FairyBukkitPlatform;
import io.fairyproject.bukkit.reflection.minecraft.MinecraftVersion;
import io.fairyproject.mc.MCInitializer;

import java.io.File;

public abstract class FairyBukkitTestingPlatform extends FairyBukkitPlatform {

    public FairyBukkitTestingPlatform() {
        super(new File("build/tmp/fairy"));
        MinecraftVersion.forceSet(this.version());
    }

    @Override
    public MCInitializer createMCInitializer() {
        return new BukkitTestingMCInitializer();
    }

    public abstract MinecraftVersion version();
}
