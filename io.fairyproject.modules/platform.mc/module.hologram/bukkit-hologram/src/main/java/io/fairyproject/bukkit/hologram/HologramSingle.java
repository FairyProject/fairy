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
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import io.fairyproject.bukkit.Imanity;
import io.fairyproject.bukkit.hologram.api.ViewHandler;
import io.fairyproject.bukkit.reflection.MinecraftReflection;
import io.fairyproject.bukkit.reflection.ProtocolLibHelper;
import io.fairyproject.bukkit.util.SpigotUtil;
import io.fairyproject.mc.MCAdventure;
import io.fairyproject.mc.MCPlayer;
import io.fairyproject.mc.protocol.MCProtocol;
import io.fairyproject.mc.protocol.MCVersion;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.Collection;

@Getter
@Setter
public class HologramSingle {

    private Hologram hologram;

    private final int armorStandId;
    private final int horseId;
    private int index;
    private float y;

    private ViewHandler viewHandler;

    public HologramSingle(Hologram hologram, ViewHandler viewHandler, float y, int index) {
        this.hologram = hologram;
        this.y = y;
        this.viewHandler = viewHandler;
        this.index = index;

        this.armorStandId = MinecraftReflection.getNewEntityId();
        this.horseId = MinecraftReflection.getNewEntityId();
    }

    public int getArmorStandId() {
        return this.armorStandId;
    }

    public Location getLocation() {
        return hologram.getLocation().clone().add(0, this.y, 0);
    }

    public void send(Collection<? extends Player> players) {
        if (!players.isEmpty()) {
            this.sendSpawnPacket(players);
            this.sendTeleportPacket(players);
            this.sendNamePackets(players);
            this.sendAttachPacket(players);
        }

    }

    public void sendRemove(Collection<? extends Player> players) {
        if (!players.isEmpty()) {
            this.sendDestroyPacket(players);
        }

    }

    protected void sendSpawnPacket(Collection<? extends Player> players) {
        players.forEach(player -> {
            if (MCProtocol.INSTANCE.version().isOrAbove(MCVersion.V1_8) || SpigotUtil.getProtocolVersion(player) > 5) {
                PacketContainer packetContainer = new PacketContainer(PacketType.Play.Server.SPAWN_ENTITY_LIVING);

                packetContainer.getIntegers()
                        .write(0, this.armorStandId)
                        .write(1, 30)
                        .write(2, (int) Math.floor(this.getLocation().getX() * 32.0))
                        .write(3, (int) Math.floor((this.getLocation().getY() - 2.25) * 32.0))
                        .write(4, (int) Math.floor(this.getLocation().getZ() * 32.0));

                packetContainer.getBytes()
                        .write(0, (byte)((int)(this.getLocation().getYaw() * 256.0F / 360.0F)))
                        .write(1, (byte)((int)(this.getLocation().getPitch() * 256.0F / 360.0F)))
                        .write(2, (byte) 0);

                packetContainer.getDataWatcherModifier().write(0, this.buildDataWatcher(player));

                ProtocolLibHelper.send(player, packetContainer);
            }
        });
    }

    protected void sendTeleportPacket(Collection<? extends Player> players) {
        players.forEach(player -> {
            if (MCProtocol.INSTANCE.version().isOrAbove(MCVersion.V1_8) || SpigotUtil.getProtocolVersion(player) > 5) {
                PacketContainer packetContainer = new PacketContainer(PacketType.Play.Server.ENTITY_TELEPORT);

                packetContainer.getIntegers()
                        .write(0, this.armorStandId)
                        .write(1, (int) Math.floor(this.getLocation().getX() * 32.0))
                        .write(2, (int) Math.floor((this.getLocation().getY() - 2.25) * 32.0))
                        .write(3, (int) Math.floor(this.getLocation().getZ() * 32.0));

                packetContainer.getBytes()
                        .write(0, (byte)((int)(this.getLocation().getYaw() * 256.0F / 360.0F)))
                        .write(1, (byte)((int)(this.getLocation().getPitch() * 256.0F / 360.0F)));

                ProtocolLibHelper.send(player, packetContainer);
            } else {
                Imanity.IMPLEMENTATION.sendEntityTeleport(player, this.getLocation().add(0, 54.56D, 0), this.armorStandId);
                Imanity.IMPLEMENTATION.sendEntityTeleport(player, this.getLocation().add(0, 54.56D, 0), this.horseId);
            }

        });
    }

    protected void sendNamePackets(Collection<? extends Player> players) {
        players.forEach(player -> {
            PacketContainer packetContainer = new PacketContainer(PacketType.Play.Server.ENTITY_METADATA);
            packetContainer.getWatchableCollectionModifier()
                    .write(0, this.buildDataWatcher(player).getWatchableObjects());

            packetContainer.getIntegers()
                    .write(0, this.armorStandId);

            ProtocolLibHelper.send(player, packetContainer);
        });
    }

    protected WrappedDataWatcher buildDataWatcher(Player player) {
        MCPlayer mcPlayer = MCPlayer.from(player);

        WrappedDataWatcher dataWatcher = new WrappedDataWatcher();
        dataWatcher.setObject(0, (byte) 32);
        dataWatcher.setObject(1, (short) 300);
        dataWatcher.setObject(2, MCAdventure.asLegacyString(this.getViewHandler().view(player), mcPlayer.getLocale()));
        dataWatcher.setObject(3, (byte) 1);
        dataWatcher.setObject(4, (byte) 1);
        dataWatcher.setObject(6, 20.0f);
        dataWatcher.setObject(7, 0);
        dataWatcher.setObject(8, (byte) 0);
        dataWatcher.setObject(9, (byte) 0);
        dataWatcher.setObject(10, (byte) 1);
        return dataWatcher;
    }

    protected void sendDestroyPacket(Collection<? extends Player> players) {
        players.forEach(player -> {
            if (MCProtocol.INSTANCE.version().isOrAbove(MCVersion.V1_8) || SpigotUtil.getProtocolVersion(player) > 5) {
                PacketContainer packetContainer = new PacketContainer(PacketType.Play.Server.ENTITY_DESTROY);

                packetContainer.getIntegerArrays()
                        .write(0, new int[] { this.armorStandId });

                ProtocolLibHelper.send(player, packetContainer);
            }
        });
    }

    protected void sendAttachPacket(Collection<? extends Player> players) {
        players.forEach(player -> {
            if (this.hologram.isAttached()) {
                Imanity.IMPLEMENTATION.sendEntityAttach(player, 0, this.armorStandId, this.hologram.getAttachedTo().getEntityId());
            } else {
                Imanity.IMPLEMENTATION.sendEntityAttach(player, 0, this.armorStandId, -1);
            }
        });
    }

}