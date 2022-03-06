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

package io.fairyproject.mc.hologram;

import com.github.retrooper.packetevents.event.PacketListener;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.util.Vector3f;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientInteractEntity;
import io.fairyproject.container.Component;
import io.fairyproject.container.ContainerConstruct;
import io.fairyproject.event.Subscribe;
import io.fairyproject.mc.MCPlayer;
import io.fairyproject.mc.event.MCPlayerJoinEvent;
import io.fairyproject.mc.event.MCPlayerMoveEvent;
import io.fairyproject.mc.event.MCPlayerQuitEvent;
import io.fairyproject.mc.event.world.MCWorldUnloadEvent;
import io.fairyproject.mc.util.Pos;
import io.fairyproject.ScheduledAtFixedRate;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

@Component
public class HologramListener implements PacketListener {

    private final Set<MCPlayer> toUpdate = new HashSet<>();

    @ContainerConstruct
    public HologramListener() {
        this.runScheduler();
    }

    @ScheduledAtFixedRate(delay = 100, ticks = 20, async = false)
    public void runScheduler() {
        final Iterator<MCPlayer> iterator = toUpdate.iterator();
        while (iterator.hasNext()) {
            MCPlayer player = iterator.next();
            iterator.remove();

            this.update(player);
        }
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        MCPlayer player = MCPlayer.from(event.getPlayer());
        HologramFactory hologramFactory = Holograms.find(player.getWorld());
        if (hologramFactory == null) {
            return;
        }

        WrapperPlayClientInteractEntity packet = new WrapperPlayClientInteractEntity(event);
        int entityId = packet.getEntityId();
        Hologram hologram = hologramFactory.getByEntityId(entityId);
        if (hologram == null || hologram.getInteractListener() == null) {
            return;
        }

        WrapperPlayClientInteractEntity.InteractAction action = packet.getAction();
        switch (action) {
            case ATTACK:
                hologram.getInteractListener().attack(player);
                break;
            case INTERACT:
                hologram.getInteractListener().interact(player);
                break;
            case INTERACT_AT:
                final Vector3f vector = packet.getTarget().orElse(null);
                hologram.getInteractListener().interactAt(player, vector);
                break;
        }
    }

    @Subscribe
    public void onMove(MCPlayerMoveEvent event) {
        final Pos from = event.getFromPos();
        final Pos to = event.getToPos();

        if (from.getBlockX() >> 4 == to.getBlockX() >> 4 && from.getBlockZ() >> 4 == to.getBlockZ() >> 4) {
            return;
        }

        this.toUpdate.add(event.getPlayer());
    }

    @Subscribe
    public void onPlayerJoin(MCPlayerJoinEvent event) {
        MCPlayer player = event.getPlayer();

        this.update(player);
    }

    @Subscribe
    public void onPlayerQuit(MCPlayerQuitEvent event) {
        MCPlayer player = event.getPlayer();

        this.toUpdate.remove(player);
        Holograms.factory(player.getWorld()).reset(player);
    }

    @Subscribe
    public void onWorldUnload(MCWorldUnloadEvent event) {
        event.getWorld().metadata().remove(HologramFactory.WORLD_METADATA);
    }

    private void update(MCPlayer player) {
        Holograms.factory(player.getWorld()).update(player);
    }

}
