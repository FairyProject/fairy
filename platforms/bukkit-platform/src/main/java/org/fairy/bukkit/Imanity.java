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
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.fairy.bukkit.impl.server.ServerImplementation;
import org.fairy.bukkit.listener.events.Events;
import org.fairy.bukkit.player.movement.MovementListener;
import org.fairy.bukkit.player.movement.impl.AbstractMovementImplementation;

import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Deprecated // TODO: completely remove this class
public final class Imanity {

    public static final Logger LOGGER = LogManager.getLogger(Imanity.class);

    public static ServerImplementation IMPLEMENTATION;

    public static AbstractMovementImplementation registerMovementListener(MovementListener movementListener) {
        Plugin plugin = null;

        try {
            plugin = JavaPlugin.getProvidingPlugin(movementListener.getClass());
        } catch (Throwable ignored) {}

        if (plugin == null) {
            plugin = FairyBukkitPlatform.PLUGIN;
        }

        AbstractMovementImplementation implementation = Imanity.IMPLEMENTATION.movement(movementListener);

        implementation.register(plugin);
        return implementation;
    }

    public static List<? extends Player> getPlayers() {
        return ImmutableList.copyOf(FairyBukkitPlatform.PLUGIN.getServer().getOnlinePlayers());
    }

    @Deprecated
    public static void callEvent(Event event) {
        FairyBukkitPlatform.PLUGIN.getServer().getPluginManager().callEvent(event);
    }

}
