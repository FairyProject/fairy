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

package io.fairyproject.bukkit.util;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class DirectionUtil
{
    public static Direction getDirection(final Player player, final Location location) {
        final Vector direction = player.getEyeLocation().getDirection().setY(0);
        final Vector target = location.toVector().subtract(player.getLocation().toVector()).normalize().setY(0);
        double n;
        for (n = Math.toDegrees(direction.angle(target)); n < 0.0; n += 360.0) {}
        if (n <= 45.0) {
            return Direction.UP;
        }
        if (n > 45.0 && n <= 135.0) {
            if (target.crossProduct(direction).getY() > 0.0) {
                return Direction.RIGHT;
            }
            return Direction.LEFT;
        }
        else {
            if (n > 135.0) {
                return Direction.DOWN;
            }
            return null;
        }
    }

    @RequiredArgsConstructor
    @Getter
    public enum Direction
    {
        DOWN("\u2193", "⇩"),
        UP("\u2191", "⇧"),
        RIGHT("\u2192", "⇨"),
        LEFT("\u2190", "⇦");

        private final String symbolA;
        private final String symbolB;
    }
}
