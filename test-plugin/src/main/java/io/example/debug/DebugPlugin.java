package io.example.debug;

import io.fairyproject.Debug;
import io.fairyproject.Fairy;
import io.fairyproject.container.Autowired;
import io.fairyproject.devtools.bukkit.BukkitPluginReloader;
import io.fairyproject.devtools.reload.Reloader;
import io.fairyproject.plugin.Plugin;

public class DebugPlugin extends Plugin {

    @Autowired
    private BukkitPluginReloader reloader;

    @Override
    public void onInitial() {
        Debug.IN_FAIRY_IDE = true;
        System.out.println("Saved the day " + this.getClass().getClassLoader());
    }

    @Override
    public void onPluginEnable() {
        Fairy.getTaskScheduler().runScheduled(() -> reloader.reload(this), 20L);
    }
}
