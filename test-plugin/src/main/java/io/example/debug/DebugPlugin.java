package io.example.debug;

import io.fairyproject.Debug;
import io.fairyproject.Fairy;
import io.fairyproject.container.Autowired;
import io.fairyproject.devtools.bukkit.BukkitPluginReloaderSetup;
import io.fairyproject.plugin.Plugin;

public class DebugPlugin extends Plugin {

    @Override
    public void onInitial() {
        Debug.IN_FAIRY_IDE = true;
    }

}
