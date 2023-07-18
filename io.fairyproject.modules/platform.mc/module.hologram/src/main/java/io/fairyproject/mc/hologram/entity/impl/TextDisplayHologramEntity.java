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
import io.fairyproject.mc.hologram.HologramImpl;
import io.fairyproject.mc.protocol.MCProtocol;
import io.fairyproject.mc.util.Position;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TextDisplayHologramEntity extends AbstractHologramEntity {
    public TextDisplayHologramEntity(HologramImpl hologram) {
        super(hologram);
    }

    @Override
    public void show(MCPlayer player) {
        Position pos = hologram.getPosition();

        WrapperPlayServerSpawnEntity packet = new WrapperPlayServerSpawnEntity(
                this.entityId,
                Optional.of(this.entityUuid),
                EntityTypes.TEXT_DISPLAY,
                this.packetPosition(),
                pos.getPitch(),
                pos.getYaw(),
                pos.getYaw(),
                0,
                Optional.of(new Vector3d())
        );

        MCProtocol.sendPacket(player, packet);
        this.update(player);
    }

    @Override
    public void update(MCPlayer player) {
        MCEntity attached = hologram.getAttached();

        WrapperPlayServerEntityTeleport packet = new WrapperPlayServerEntityTeleport(
                this.entityId,
                this.packetPosition(),
                this.hologram.getPosition().getPitch(),
                this.hologram.getPosition().getYaw(),
                false
        );
        WrapperPlayServerEntityMetadata metadataPacket = new WrapperPlayServerEntityMetadata(
                this.entityId,
                this.createEntityData(player)
        );
        WrapperPlayServerAttachEntity attachEntityPacket = new WrapperPlayServerAttachEntity(
                this.entityId,
                attached != null ? attached.getId() : -1,
                false
        );

        MCProtocol.sendPacket(player, packet);
        MCProtocol.sendPacket(player, metadataPacket);
        MCProtocol.sendPacket(player, attachEntityPacket);
    }

    @Override
    public void hide(MCPlayer player) {
        WrapperPlayServerDestroyEntities packet = new WrapperPlayServerDestroyEntities(this.entityId);

        MCProtocol.sendPacket(player, packet);
    }

    private List<EntityData> createEntityData(MCPlayer mcPlayer) {
        List<EntityData> entityDataList = new ArrayList<>();

        entityDataList.add(new EntityData(22, EntityDataTypes.COMPONENT, MCAdventure.asItemString(line.render(mcPlayer), mcPlayer.getLocale()))); // text
        entityDataList.add(new EntityData(23, EntityDataTypes.CAT_VARIANT, 200)); // line width
        entityDataList.add(new EntityData(24, EntityDataTypes.CAT_VARIANT, 0x40000000)); // background color
        entityDataList.add(new EntityData(25, EntityDataTypes.CAT_VARIANT, -1)); // text opacity
        /**
         * bit mask
         * 0x01 = has shadow
         * 0x02 = is see through
         * 0x04 = use default background color
         * 0x08 = alignment (0 = center, 1, 3 = left, 2 = right)
         */
        boolean hasShadow = true;
        boolean isSeeThrough = false;
        boolean useDefaultBackgroundColor = true;
        int alignment = 0;
        entityDataList.add(new EntityData(26, EntityDataTypes.CAT_VARIANT, (hasShadow ? 0x01 : 0) | (isSeeThrough ? 0x02 : 0) | (useDefaultBackgroundColor ? 0x04 : 0) | alignment));

        return entityDataList;
    }
}
