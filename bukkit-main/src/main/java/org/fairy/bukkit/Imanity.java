/*
 * MIT License
 *
 * Copyright (c) 2021 Imanity
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package org.fairy.bukkit;

import com.google.common.collect.ImmutableList;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.fairy.bean.Autowired;
import org.fairy.bukkit.bossbar.BossBarAdapter;
import org.fairy.bukkit.bossbar.BossBarHandler;
import org.fairy.bukkit.chunk.block.CacheBlockSetHandler;
import org.fairy.bukkit.hologram.HologramHandler;
import org.fairy.bukkit.impl.server.ServerImplementation;
import org.fairy.bukkit.listener.events.Events;
import org.fairy.bukkit.metadata.Metadata;
import org.fairy.bukkit.player.movement.MovementListener;
import org.fairy.bukkit.player.movement.impl.AbstractMovementImplementation;
import org.fairy.bukkit.tablist.ImanityTabAdapter;
import org.fairy.bukkit.tablist.ImanityTabHandler;
import org.fairy.bukkit.timer.TimerHandler;
import org.fairy.bukkit.visual.VisualBlockHandler;
import org.fairy.plugin.PluginClassLoader;
import org.fairy.util.FastRandom;

import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Imanity {

    public static final Logger LOGGER = LogManager.getLogger(Imanity.class);

    public static ImanityTabHandler TAB_HANDLER;
    public static BossBarHandler BOSS_BAR_HANDLER;

    @Autowired
    public static TimerHandler TIMER_HANDLER;

    @Deprecated
    public static Plugin PLUGIN;

    public static ServerImplementation IMPLEMENTATION;
    private static VisualBlockHandler VISUAL_BLOCK_HANDLER;

    public static VisualBlockHandler getVisualBlockHandler() {
        if (VISUAL_BLOCK_HANDLER == null) {
            VISUAL_BLOCK_HANDLER = new VisualBlockHandler();
        }

        return VISUAL_BLOCK_HANDLER;
    }

    public static CacheBlockSetHandler getBlockSetHandler(World world) {
        return Metadata.provideForWorld(world).getOrPut(CacheBlockSetHandler.METADATA, () -> new CacheBlockSetHandler(world));
    }

    public static HologramHandler getHologramHandler(World world) {
        return Metadata.provideForWorld(world).getOrPut(HologramHandler.WORLD_METADATA, () -> new HologramHandler(world));
    }

    public static AbstractMovementImplementation registerMovementListener(MovementListener movementListener) {
        Plugin plugin = null;

        try {
            plugin = JavaPlugin.getProvidingPlugin(movementListener.getClass());
        } catch (Throwable ignored) {}

        if (plugin == null) {
            plugin = Imanity.PLUGIN;
        }

        AbstractMovementImplementation implementation = Imanity.IMPLEMENTATION.movement(movementListener);

        implementation.register(plugin);
        return implementation;
    }

    @Deprecated
    public static void registerEvents(Listener... listeners) {
        Events.subscribe(listeners);
    }

    public static void unregisterEvents(Listener... listeners) {
        for (Listener listener : listeners) {
            HandlerList.unregisterAll(listener);
        }
    }

    public static List<? extends Player> getPlayers() {
        return ImmutableList.copyOf(Imanity.PLUGIN.getServer().getOnlinePlayers());
    }

    public static void callEvent(Event event) {
        PLUGIN.getServer().getPluginManager().callEvent(event);
    }

    public static void registerTablistHandler(ImanityTabAdapter adapter) {
        Imanity.TAB_HANDLER = new ImanityTabHandler(adapter);
    }

    public static void registerBossBarHandler(BossBarAdapter adapter) {
        Imanity.BOSS_BAR_HANDLER = new BossBarHandler(adapter);
    }

}
