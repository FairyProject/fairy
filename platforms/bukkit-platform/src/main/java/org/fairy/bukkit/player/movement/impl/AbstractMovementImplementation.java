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

package org.fairy.bukkit.player.movement.impl;

import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.fairy.bukkit.player.movement.MovementListener;

@Getter
public abstract class AbstractMovementImplementation {

    private MovementListener movementListener;
    private boolean ignoreSameBlock;
    private boolean ignoreSameY;

    public AbstractMovementImplementation(MovementListener movementListener) {
        this.movementListener = movementListener;
    }

    public AbstractMovementImplementation ignoreSameBlock() {
        this.ignoreSameBlock = true;
        return this;
    }

    public AbstractMovementImplementation ignoreSameBlockAndY() {
        this.ignoreSameBlock = true;
        this.ignoreSameY = true;
        return this;
    }

    public void register(Plugin plugin) {

    }

    public void unregister() {

    }

    public void updateLocation(Player player, Location from, Location to) {
        if (from.getX() != to.getX()
                || from.getY() != to.getY()
                || from.getZ() != to.getZ()) {
            boolean cancelled = false;

            if (this.isIgnoreSameBlock() && from.getBlockX() == to.getBlockX() && from.getBlockZ() == to.getBlockZ()) {
                if (this.isIgnoreSameY() || from.getBlockY() == to.getBlockY()) {
                    cancelled = true;
                }
            }

            if (!cancelled) {
                this.movementListener.handleUpdateLocation(player, from, to);
            }
        }
    }

    public void updateRotation(Player player, Location from, Location to) {
        if (from.getYaw() != to.getYaw()
                || from.getPitch() != to.getPitch()) {
            this.movementListener.handleUpdateRotation(player, from, to);
        }
    }

}
