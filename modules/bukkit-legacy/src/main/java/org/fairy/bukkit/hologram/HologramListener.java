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

package org.fairy.bukkit.hologram;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.world.WorldUnloadEvent;
import org.fairy.bukkit.FairyBukkitPlatform;
import org.fairy.bukkit.timings.MCTiming;
import org.fairy.bukkit.timings.TimingService;
import org.fairy.bean.BeanConstructor;
import org.fairy.ScheduledAtFixedRate;
import org.fairy.bukkit.Imanity;
import org.fairy.bukkit.metadata.Metadata;
import org.fairy.bukkit.player.movement.MovementListener;
import org.fairy.bean.Component;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

@Component
public class HologramListener implements Listener {

    private final Set<Player> toUpdate = new HashSet<>();
    private final MCTiming timing;

    @BeanConstructor
    public HologramListener(TimingService timingService) {
        Imanity.registerMovementListener(new MovementListener() {
            @Override
            public void handleUpdateLocation(Player player, Location from, Location to) {
                toUpdate.add(player);
            }

            @Override
            public void handleUpdateRotation(Player player, Location from, Location to) {

            }
        }).ignoreSameBlockAndY();
        this.timing = timingService.of(FairyBukkitPlatform.PLUGIN, "Holograms Update");
        this.runScheduler();
    }

    @ScheduledAtFixedRate(delay = 100, ticks = 20, async = false)
    public void runScheduler() {
        try (MCTiming ignored = this.timing.startTiming()) {
            final Iterator<Player> iterator = toUpdate.iterator();
            while (iterator.hasNext()) {
                final Player player = iterator.next();
                iterator.remove();

                this.update(player);
            }
        }
    }

    @EventHandler
    public void onTeleport(PlayerTeleportEvent event) {
        Player player = event.getPlayer();

        Location from = event.getFrom(), to = event.getTo();

        if (from.getBlockX() == to.getBlockX() && from.getBlockZ() == to.getBlockZ()) {
            return;
        }
        this.update(player);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        this.update(player);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        this.toUpdate.remove(player);
        Imanity.getHologramHandler(player.getWorld()).reset(player);
    }

    @EventHandler
    public void onWorldUnload(WorldUnloadEvent event) {
        Metadata.provideForWorld(event.getWorld()).remove(HologramHandler.WORLD_METADATA);
    }

    private void update(Player player) {
        Imanity.getHologramHandler(player.getWorld()).update(player);
    }

}
