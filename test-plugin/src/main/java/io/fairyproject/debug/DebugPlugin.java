package io.fairyproject.debug;

import io.fairyproject.Debug;
import io.fairyproject.app.Application;
import io.fairyproject.plugin.Plugin;

public class DebugPlugin extends Application {

    @Override
    public void onInitial() {
        Debug.IN_FAIRY_IDE = true;
        Debug.BREAKPOINT = () -> {
            // break point!
        };
        System.out.println("Fairy debug IDE mode enabled.");
    }

    @Override
    public void onAppEnable() {

    }

}
