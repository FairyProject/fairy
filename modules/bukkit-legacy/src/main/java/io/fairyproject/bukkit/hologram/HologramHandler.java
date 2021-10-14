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

package io.fairyproject.bukkit.hologram;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.EnumWrappers;
import io.fairyproject.Fairy;
import io.fairyproject.bean.Autowired;
import io.fairyproject.bukkit.FairyBukkitPlatform;
import io.fairyproject.bukkit.hologram.player.RenderedHolograms;
import io.fairyproject.bukkit.packet.wrapper.other.Vector3D;
import io.fairyproject.bukkit.reflection.ProtocolLibService;
import io.fairyproject.metadata.MetadataKey;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import io.fairyproject.bukkit.metadata.Metadata;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class HologramHandler {

    @Autowired
    private static ProtocolLibService PROTOCOL_LIB_SERVICE;

    public static HologramHandler getHologramHandler(World world) {
        return Metadata.provideForWorld(world).getOrPut(HologramHandler.WORLD_METADATA, () -> new HologramHandler(world));
    }

    public static final int DISTANCE_TO_RENDER = 60;
    public static final MetadataKey<HologramHandler> WORLD_METADATA = MetadataKey.create(Fairy.METADATA_PREFIX + "WorldHolograms", HologramHandler.class);
    public static final MetadataKey<RenderedHolograms> HOLOGRAM_METADATA = MetadataKey.create(Fairy.METADATA_PREFIX + "Holograms", RenderedHolograms.class);
    private final World world;
    private final Map<Integer, Hologram> holograms = new HashMap<>();
    // Thread Safe
    private final Map<Integer, Hologram> entityIdToHolograms = new ConcurrentHashMap<>();

    public HologramHandler(World world) {
        this.world = world;
        PROTOCOL_LIB_SERVICE.validEnabled();

        PROTOCOL_LIB_SERVICE.manager().addPacketListener(new PacketAdapter(FairyBukkitPlatform.PLUGIN, PacketType.Play.Client.USE_ENTITY) {
            @Override
            public void onPacketReceiving(PacketEvent event) {
                final Player player = event.getPlayer();

                if (player.getWorld() != world) {
                    return;
                }

                final PacketContainer packet = event.getPacket();
                final Integer entityId = packet.getIntegers().read(0);
                final Hologram hologram = entityIdToHolograms.get(entityId);
                if (hologram == null || hologram.getInteractListener() == null) {
                    return;
                }

                final EnumWrappers.EntityUseAction action = packet.getEntityUseActions().read(0);
                switch (action) {
                    case ATTACK:
                        hologram.getInteractListener().attack(player);
                        break;
                    case INTERACT:
                        hologram.getInteractListener().interact(player);
                        break;
                    case INTERACT_AT:
                        final Vector vector = packet.getVectors().read(0);
                        hologram.getInteractListener().interactAt(player, new Vector3D(vector.getX(), vector.getY(), vector.getZ()));
                        break;
                }
            }
        });
    }

    protected void registerEntityId(int entityId, Hologram hologram) {
        this.entityIdToHolograms.put(entityId, hologram);
    }

    protected void unregisterEntityId(int entityId) {
        this.entityIdToHolograms.remove(entityId);
    }

    public Hologram addHologram(Location location, String... texts) {
        Hologram hologram = new Hologram(location, this);
        for (String text : texts) {
            hologram.addText(text);
        }
        this.addHologram(hologram);
        hologram.spawn();
        return hologram;
    }

    public void addHologram(Hologram hologram) {
        this.holograms.put(hologram.getId(), hologram);
    }

    public void update(Player player) {

        RenderedHolograms holograms = this.getRenderedHolograms(player);
        holograms.removeFarHolograms(player, this);
        holograms.addNearHolograms(player, this);

    }

    public void reset(Player player) {

        RenderedHolograms holograms = this.getRenderedHolograms(player);
        holograms.reset(player, this);
        Metadata.provideForPlayer(player).remove(HOLOGRAM_METADATA);

    }

    public Hologram getHologram(int id) {
        return this.holograms.get(id);
    }

    public Collection<Hologram> getHolograms() {
        return this.holograms.values();
    }

    public RenderedHolograms getRenderedHolograms(Player player) {
        return Metadata.provideForPlayer(player)
                .getOrPut(HOLOGRAM_METADATA, () -> new RenderedHolograms(player));
    }

    public void removeHologram(Hologram hologram) {
        this.holograms.remove(hologram.getId());
    }
}
