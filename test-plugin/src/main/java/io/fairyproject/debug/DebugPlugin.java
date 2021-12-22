package io.fairyproject.debug;

import io.fairyproject.Debug;
import io.fairyproject.plugin.Plugin;

public class DebugPlugin extends Plugin {

    @Override
    public void onInitial() {
        Debug.IN_FAIRY_IDE = true;
        System.out.println("Fairy debug IDE mode enabled.");
    }

}
