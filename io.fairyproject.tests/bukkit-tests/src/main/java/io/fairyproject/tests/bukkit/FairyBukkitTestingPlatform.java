package io.fairyproject.tests.bukkit;

import io.fairyproject.bukkit.FairyBukkitPlatform;
import io.fairyproject.mc.MCInitializer;

import java.io.File;

public class FairyBukkitTestingPlatform extends FairyBukkitPlatform {

    public FairyBukkitTestingPlatform() {
        super(new File("build/tmp/fairy"));
    }

    @Override
    public MCInitializer createMCInitializer() {
        return new BukkitTestingMCInitializer();
    }
}
