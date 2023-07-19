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

import com.github.retrooper.packetevents.util.Vector3d;
import io.fairyproject.mc.hologram.HologramImpl;
import io.fairyproject.mc.hologram.entity.HologramEntity;
import io.fairyproject.mc.hologram.line.HologramLine;
import io.fairyproject.mc.util.Position;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@RequiredArgsConstructor
public abstract class AbstractHologramEntity implements HologramEntity {

    protected final HologramImpl hologram;
    protected HologramLine line;
    protected double y;
    protected int entityId;
    protected UUID entityUuid;

    protected Vector3d packetPosition() {
        Position pos = hologram.getPosition();

        return new Vector3d(pos.getX(), pos.getY() + y, pos.getZ());
    }

    @Override
    public int getEntityId() {
        return entityId;
    }

    public void setLine(HologramLine line) {
        this.line = line;
    }

    public void setY(double y) {
        this.y = y;
    }

    public void setEntityId(int entityId) {
        this.entityId = entityId;
    }

    public void setEntityUuid(UUID entityUuid) {
        this.entityUuid = entityUuid;
    }
}
