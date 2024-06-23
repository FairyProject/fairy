package io.example.debug;

import io.fairyproject.Debug;
import io.fairyproject.mc.scheduler.MCSchedulers;
import io.fairyproject.plugin.Plugin;
import io.fairyproject.scheduler.repeat.RepeatPredicate;

public class DebugPlugin extends Plugin {

    @Override
    public void onInitial() {
        Debug.IN_FAIRY_IDE = true;
    }

    @Override
    public void onPluginEnable() {
        MCSchedulers.getGlobalScheduler().scheduleAtFixedRate(() -> {
            System.out.println("Hello, world!");
        }, 20, 20, RepeatPredicate.cycled(5));
    }
}
