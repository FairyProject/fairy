package io.fairyproject.bukkit.timings;

import lombok.Getter;
import org.bukkit.plugin.Plugin;

@Getter
public class UnitTestingTiming extends MCTiming {

    private final Plugin plugin;
    private final String command;
    private final MCTiming parent;

    private long startTime;
    private long lastTime;

    public UnitTestingTiming(Plugin plugin, String command, MCTiming parent) {
        this.plugin = plugin;
        this.command = command;
        this.parent = parent;
    }

    @Override
    public MCTiming startTiming() {
        if (this.startTime != -1) {
            this.stopTiming();
        }

        this.startTime = System.currentTimeMillis();
        return this;
    }

    @Override
    public void stopTiming() {
        if (this.startTime == -1) {
            return;
        }

        this.lastTime = System.currentTimeMillis() - this.startTime;
        this.startTime = -1;
    }
}
