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

package org.fairy.bukkit.util;

import lombok.experimental.UtilityClass;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.util.BlockIterator;
import org.fairy.bukkit.reflection.resolver.MethodResolver;
import org.fairy.reflect.Reflect;
import org.fairy.util.CC;

import javax.annotation.Nullable;
import java.io.File;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

// TODO - completely remove this class
@UtilityClass
public class BukkitUtil {

    @Deprecated
    public List<Player> getPlayersFromUuids(Collection<UUID> uuids) {
        return Players.transformUuids(uuids);
    }

    @Deprecated
    public List<Player> getPlayersFromUuids(UUID... uuids) {
        return Players.transformUuids(uuids);
    }

    // TODO - better replacement
    @Nullable
    public Player getDamager(EntityDamageEvent event) {
        if (event instanceof EntityDamageByEntityEvent) {
            EntityDamageByEntityEvent damageByEntityEvent = (EntityDamageByEntityEvent) event;
            Entity damager = damageByEntityEvent.getDamager();

            if (damager instanceof Player) {
                return (Player) damageByEntityEvent.getDamager();
            }

            if (damager instanceof Projectile) {
                ProjectileSource shooter = ((Projectile) damager).getShooter();
                if (shooter instanceof Player) {
                    return (Player) shooter;
                }
            }
        }
        return null;
    }

    // TODO - move to core-misc
    public String getProgressBar(int current, int max, int totalBars, String symbol, String completedColor, String notCompletedColor){
        float percent = (float) current / max;

        int progressBars = (int) ((int) totalBars * percent);

        int leftOver = (totalBars - progressBars);

        StringBuilder sb = new StringBuilder();
        sb.append(ChatColor.translateAlternateColorCodes('&', completedColor));
        for (int i = 0; i < progressBars; i++) {
            sb.append(symbol);
        }
        sb.append(ChatColor.translateAlternateColorCodes('&', notCompletedColor));
        for (int i = 0; i < leftOver; i++) {
            sb.append(symbol);
        }
        return sb.toString();
    }

    @Deprecated
    public void clear(final Player player) {
        Players.clear(player);
    }

    @Deprecated
    public void sendMessages(Player player, Iterable<String> iterable) {
        for (String message : iterable) {
            player.sendMessage(message);
        }
    }

    public File getPluginJar(JavaPlugin plugin) {

        MethodResolver resolver = new MethodResolver(JavaPlugin.class);
        return (File) resolver.resolveWrapper("getFile").invoke(plugin);

    }

    @Deprecated
    public void delayedUpdateInventory(Player player) {
        Players.updateInventoryLater(player);
    }

    // TODO - move to Players
    public boolean isPlayerEvent(Class<?> event) {
        if (PlayerEvent.class.isAssignableFrom(event)) {
            return true;
        }

        MethodResolver resolver = new MethodResolver(event);
        return resolver.resolveWrapper("getPlayer").exists();
    }

    // TODO - move to Players
    public Block getBlockLookingAt(final Player player, final int distance) {
        final Location location = player.getEyeLocation();
        final BlockIterator blocksToAdd = new BlockIterator(location, 0.0D, distance);
        Block block = null;
        while (blocksToAdd.hasNext()) {
            block = blocksToAdd.next();
        }
        return block;
    }

    public File getDataFolder(int depth) {
        return getCurrentPlugin(depth + 1).getDataFolder();
    }

    public Plugin getCurrentPlugin(int depth) {
        Class<?> caller = Reflect.getCallerClass(depth).orElse(null);

        if (caller != null) {
            Plugin plugin = null;

            try {
                plugin = JavaPlugin.getProvidingPlugin(caller);
            } catch (Throwable ignored) {}

            if (plugin != null) {
                return plugin;
            } else {
                throw new IllegalArgumentException("Caller class from depth " + depth + " is not plugin class.");
            }
        } else {
            throw new IllegalArgumentException("Caller class from depth " + depth + " does not exists.");
        }
    }

    public File getDataFolder() {
        return getDataFolder(4);
    }

    public File getFile(String fileName) {
        return new File(getDataFolder(4), fileName);
    }

    public boolean isNPC(Player player) {
        if (player.hasMetadata("ImanityBot")) {
            return true;
        }

        if (player.hasMetadata("NPC")) {
            return true;
        }

        return false;
    }

}
