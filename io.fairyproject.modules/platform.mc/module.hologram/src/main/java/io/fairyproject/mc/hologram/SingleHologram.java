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

import com.github.retrooper.packetevents.protocol.entity.data.EntityData;
import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes;
import com.github.retrooper.packetevents.protocol.entity.type.EntityTypes;
import com.github.retrooper.packetevents.util.Vector3d;
import com.github.retrooper.packetevents.wrapper.play.server.*;
import com.google.common.collect.ImmutableList;
import io.fairyproject.mc.MCServer;
import io.fairyproject.mc.hologram.api.ViewHandler;
import io.fairyproject.mc.MCAdventure;
import io.fairyproject.mc.MCEntity;
import io.fairyproject.mc.MCPlayer;
import io.fairyproject.mc.protocol.MCProtocol;
import io.fairyproject.mc.protocol.MCVersion;
import io.fairyproject.mc.util.Pos;
import lombok.Getter;
import lombok.Setter;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class SingleHologram {

    private Hologram hologram;

    private final int armorStandId;
    private final int horseId;

    private final UUID armorStandUuid;
    private final UUID horseUuid;

    private int index;
    private float y;

    private ViewHandler viewHandler;

    public SingleHologram(Hologram hologram, ViewHandler viewHandler, float y, int index) {
        this.hologram = hologram;
        this.y = y;
        this.viewHandler = viewHandler;
        this.index = index;

        this.armorStandId = MCEntity.Companion.BRIDGE.newEntityId();
        this.horseId = MCEntity.Companion.BRIDGE.newEntityId();

        // Should avoid uuid duplication??
        this.armorStandUuid = UUID.randomUUID();
        this.horseUuid = UUID.randomUUID();
    }

    public int getArmorStandId() {
        return this.armorStandId;
    }

    public Pos getLocation() {
        return hologram.getPosition().clone().add(0, this.y, 0);
    }

    public void send(Collection<? extends MCPlayer> players) {
        if (!players.isEmpty()) {
            this.sendSpawnPacket(players);
            this.sendTeleportPacket(players);
            this.sendNamePackets(players);
            this.sendAttachPacket(players);
        }
    }

    public void sendRemove(Collection<? extends MCPlayer> players) {
        if (!players.isEmpty()) {
            this.sendDestroyPacket(players);
        }
    }

    protected void sendSpawnPacket(Collection<? extends MCPlayer> players) {
        players.forEach(player -> {
            WrapperPlayServerSpawnLivingEntity packet = new WrapperPlayServerSpawnLivingEntity(
                    this.armorStandId,
                    this.armorStandUuid,
                    EntityTypes.ARMOR_STAND,
                    new Vector3d(
                            this.getLocation().getX(),
                            this.getLocation().getY(),
                            this.getLocation().getZ()
                    ),
                    this.getLocation().getYaw(),
                    this.getLocation().getPitch(),
                    0,
                    new Vector3d(),
                    this.buildDataWatcher(player)
            );

            MCProtocol.sendPacket(player, packet);
        });
    }

    protected void sendTeleportPacket(Collection<? extends MCPlayer> players) {
        players.forEach(player -> {
            WrapperPlayServerEntityTeleport packet = new WrapperPlayServerEntityTeleport(
                    this.armorStandId,
                    new Vector3d(
                            this.getLocation().getX(),
                            this.getLocation().getY(),
                            this.getLocation().getZ()
                    ),
                    this.getLocation().getYaw(),
                    this.getLocation().getPitch(),
                    false
            );
            MCProtocol.sendPacket(player, packet);
        });
    }

    protected void sendNamePackets(Collection<? extends MCPlayer> players) {
        players.forEach(player -> {
            WrapperPlayServerEntityMetadata packet = new WrapperPlayServerEntityMetadata(
                    this.armorStandId,
                    this.buildDataWatcher(player)
            );
            MCProtocol.sendPacket(player, packet);
        });
    }

    protected List<EntityData> buildDataWatcher(MCPlayer player) {
        EntityData displayName;
        if (MCServer.current().getVersion().isOrAbove(MCVersion.V1_13)) {
            displayName = new EntityData(2, EntityDataTypes.COMPONENT, this.getViewHandler().view(player));
        } else {
            displayName = new EntityData(2, EntityDataTypes.STRING, MCAdventure.asLegacyString(this.getViewHandler().view(player), player.getLocale()));
        }

        return ImmutableList.of(
                new EntityData(0, EntityDataTypes.BYTE, 32),
                new EntityData(1, EntityDataTypes.SHORT, 300),
                displayName,
                new EntityData(3, EntityDataTypes.BYTE, 1),
                new EntityData(4, EntityDataTypes.BYTE, 1),
                new EntityData(6, EntityDataTypes.FLOAT, 20.0f),
                new EntityData(7, EntityDataTypes.INT, 0),
                new EntityData(8, EntityDataTypes.BYTE, 0),
                new EntityData(9, EntityDataTypes.BYTE, 0),
                new EntityData(10, EntityDataTypes.BYTE, 1)
        );
    }

    protected void sendDestroyPacket(Collection<? extends MCPlayer> players) {
        players.forEach(player -> {
            WrapperPlayServerDestroyEntities packet = new WrapperPlayServerDestroyEntities(this.armorStandId);
            MCProtocol.sendPacket(player, packet);
        });
    }

    protected void sendAttachPacket(Collection<? extends MCPlayer> players) {
        players.forEach(player -> {
            if (this.hologram.isAttached()) {
                WrapperPlayServerAttachEntity packet = new WrapperPlayServerAttachEntity(
                        this.armorStandId,
                        this.hologram.getAttachedTo().getId(),
                        false
                );
                MCProtocol.sendPacket(player, packet);
            } else {
                WrapperPlayServerAttachEntity packet = new WrapperPlayServerAttachEntity(
                        this.armorStandId,
                        -1,
                        false
                );
                MCProtocol.sendPacket(player, packet);
            }
        });
    }

}