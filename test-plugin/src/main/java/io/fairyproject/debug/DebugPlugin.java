package io.fairyproject.debug;

import io.fairyproject.Debug;
import io.fairyproject.plugin.Plugin;

public class DebugPlugin extends Plugin {

    @Override
    public void onInitial() {
        Debug.IN_FAIRY_IDE = true;
        Debug.BREAKPOINT = () -> {
            // break point!
        };
        System.out.println("Fairy debug IDE mode enabled.");
    }

    @Override
    public void onPluginEnable() {

    }

}
