/*
 * MIT License
 *
 * Copyright (c) 2022 Fairy Project
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

package io.fairyproject.mc.hologram.entity.impl;

import com.github.retrooper.packetevents.protocol.entity.data.EntityData;
import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes;
import com.github.retrooper.packetevents.protocol.entity.type.EntityTypes;
import com.github.retrooper.packetevents.util.Vector3d;
import com.github.retrooper.packetevents.wrapper.play.server.*;
import io.fairyproject.mc.MCAdventure;
import io.fairyproject.mc.MCEntity;
import io.fairyproject.mc.MCPlayer;
import io.fairyproject.mc.MCServer;
import io.fairyproject.mc.hologram.HologramImpl;
import io.fairyproject.mc.protocol.MCProtocol;
import io.fairyproject.mc.util.Position;
import io.fairyproject.mc.version.MCVersion;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ArmorStandHologramEntity extends AbstractHologramEntity {

    public ArmorStandHologramEntity(HologramImpl hologram) {
        super(hologram);
    }

    public void show(@NotNull MCPlayer player) {
        Position pos = hologram.getPosition();

        // spawn packet
        if (MCServer.current().getVersion().isHigherOrEqual(MCVersion.of(1, 19))) {
            WrapperPlayServerSpawnEntity packet = new WrapperPlayServerSpawnEntity(
                    this.entityId,
                    Optional.of(this.entityUuid),
                    EntityTypes.ARMOR_STAND,
                    this.packetPosition(),
                    pos.getPitch(),
                    pos.getYaw(),
                    pos.getYaw(),
                    0,
                    Optional.of(new Vector3d())
            );

            MCProtocol.sendPacket(player, packet);
        } else {
            WrapperPlayServerSpawnLivingEntity packet = new WrapperPlayServerSpawnLivingEntity(
                    this.entityId,
                    this.entityUuid,
                    EntityTypes.ARMOR_STAND,
                    this.packetPosition(),
                    pos.getYaw(),
                    pos.getPitch(),
                    0,
                    new Vector3d(),
                    this.createEntityData(player)
            );

            MCProtocol.sendPacket(player, packet);
        }

        this.update(player);
    }

    public void update(@NotNull MCPlayer player) {
        Position pos = hologram.getPosition();
        MCEntity attached = hologram.getAttached();

        // metadata packet
        WrapperPlayServerEntityMetadata metadataPacket = new WrapperPlayServerEntityMetadata(
                this.entityId,
                this.createEntityData(player)
        );

        // teleport packet
        WrapperPlayServerEntityTeleport teleportPacket = new WrapperPlayServerEntityTeleport(
                this.entityId,
                this.packetPosition(),
                pos.getYaw(),
                pos.getPitch(),
                false
        );

        // attach entity packet
        WrapperPlayServerAttachEntity attachEntityPacket = new WrapperPlayServerAttachEntity(
                this.entityId,
                attached != null ? attached.getId() : -1,
                false
        );

        MCProtocol.sendPacket(player, teleportPacket);
        MCProtocol.sendPacket(player, metadataPacket);
        MCProtocol.sendPacket(player, attachEntityPacket);
    }

    public void hide(@NotNull MCPlayer player) {
        // remove entity packet
        WrapperPlayServerDestroyEntities packet = new WrapperPlayServerDestroyEntities(this.entityId);
        MCProtocol.sendPacket(player, packet);
    }

    private List<EntityData> createEntityData(MCPlayer player) {
        MCServer server = hologram.getServer();
        List<EntityData> entityDataList = new ArrayList<>();

        // entity data bit mask
        entityDataList.add(new EntityData(0, EntityDataTypes.BYTE, (byte) 0x20));

        // custom name
        MCVersion version = server.getVersion();
        if (version.isHigherOrEqual(MCVersion.of(13))) {
            entityDataList.add(new EntityData(
                    2,
                    EntityDataTypes.OPTIONAL_COMPONENT,
                    Optional.ofNullable(this.line.render(player))
                            .map(e -> MCAdventure.asItemString(e, player.getLocale()))
            ));
        } else {
            entityDataList.add(new EntityData(2, EntityDataTypes.STRING, MCAdventure.asLegacyString(this.line.render(player), player.getLocale())));
        }

        // always show name tag
        if (version.isHigherOrEqual(MCVersion.of(9))) {
            entityDataList.add(new EntityData(3, EntityDataTypes.BOOLEAN, true));
        } else {
            entityDataList.add(new EntityData(3, EntityDataTypes.BYTE, (byte) 1));
        }

        // armorstand status bit mask
        if (version.isHigherOrEqual(MCVersion.of(17)))
            entityDataList.add(new EntityData(15, EntityDataTypes.BYTE, (byte) 0x11));
        else if (version.isHigherOrEqual(MCVersion.of(15)))
            entityDataList.add(new EntityData(14, EntityDataTypes.BYTE, (byte) 0x11));
        else if (version.isHigherOrEqual(MCVersion.of(14)))
            entityDataList.add(new EntityData(13, EntityDataTypes.BYTE, (byte) 0x11));
        else if (version.isHigherOrEqual(MCVersion.of(10)))
            entityDataList.add(new EntityData(11, EntityDataTypes.BYTE, (byte) 0x11));
        else
            entityDataList.add(new EntityData(10, EntityDataTypes.BYTE, (byte) 0x11));

        return entityDataList;
    }

}
