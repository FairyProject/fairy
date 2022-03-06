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

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.PacketListener;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.util.Vector3f;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientInteractEntity;
import io.fairyproject.Fairy;
import io.fairyproject.mc.MCPlayer;
import io.fairyproject.mc.MCWorld;
import io.fairyproject.mc.util.Pos;
import io.fairyproject.metadata.MetadataKey;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class HologramFactory {

    public static final int DISTANCE_TO_RENDER = 60;
    public static final MetadataKey<HologramFactory> WORLD_METADATA = MetadataKey.create(Fairy.METADATA_PREFIX + "WorldHolograms", HologramFactory.class);
    public static final MetadataKey<HologramRenderer> HOLOGRAM_METADATA = MetadataKey.create(Fairy.METADATA_PREFIX + "Holograms", HologramRenderer.class);
    private final Map<Integer, Hologram> holograms = new HashMap<>();
    // Thread Safe
    private final Map<Integer, Hologram> entityIdToHolograms = new ConcurrentHashMap<>();

    public HologramFactory(MCWorld world) {
        PacketEvents.getAPI().getEventManager().registerListener(new PacketListener() {
            @Override
            public void onPacketReceive(PacketReceiveEvent event) {
                MCPlayer player = MCPlayer.from(event.getPlayer());

                if (player.getWorld() != world) {
                    return;
                }

                WrapperPlayClientInteractEntity packet = new WrapperPlayClientInteractEntity(event);
                int entityId = packet.getEntityId();
                Hologram hologram = entityIdToHolograms.get(entityId);
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
        }, PacketListenerPriority.MONITOR, false, true);
    }

    public static HologramFactory from(MCWorld world) {
        return world.metadata().getOrPut(HologramFactory.WORLD_METADATA, () -> new HologramFactory(world));
    }

    protected Hologram getByEntityId(int entityId) {
        return this.entityIdToHolograms.get(entityId);
    }

    protected void registerEntityId(int entityId, Hologram hologram) {
        this.entityIdToHolograms.put(entityId, hologram);
    }

    protected void unregisterEntityId(int entityId) {
        this.entityIdToHolograms.remove(entityId);
    }

    public Hologram add(Pos pos, String... texts) {
        Hologram hologram = new Hologram(pos, this);
        for (String text : texts) {
            hologram.addText(text);
        }
        this.add(hologram);
        hologram.spawn();
        return hologram;
    }

    public void add(Hologram hologram) {
        this.holograms.put(hologram.getId(), hologram);
    }

    public void update(MCPlayer player) {
        HologramRenderer holograms = this.renderer(player);
        holograms.removeFarHolograms(player, this);
        holograms.addNearHolograms(player, this);
    }

    public void reset(MCPlayer player) {
        HologramRenderer holograms = this.renderer(player);
        holograms.reset(player, this);
        player.metadata().remove(HOLOGRAM_METADATA);
    }

    public void remove(Hologram hologram) {
        this.holograms.remove(hologram.getId());
    }

    public Hologram get(int id) {
        return this.holograms.get(id);
    }

    public Collection<Hologram> all() {
        return this.holograms.values();
    }

    public HologramRenderer renderer(MCPlayer player) {
        return player.metadata().getOrPut(HOLOGRAM_METADATA, () -> new HologramRenderer(player));
    }
}
