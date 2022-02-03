package io.fairyproject.debug;

import io.fairyproject.Debug;
import io.fairyproject.bootstrap.bukkit.BukkitPlugin;
import io.fairyproject.container.ContainerContext;
import io.fairyproject.plugin.Plugin;

public class DebugPlugin extends Plugin {

    @Override
    public void onInitial() {
        Debug.IN_FAIRY_IDE = true;
        ContainerContext.SHOW_LOGS = true;
        System.out.println("Fairy debug IDE mode enabled.");
        System.out.println(BukkitPlugin.INSTANCE);
    }

}
