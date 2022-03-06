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

import io.fairyproject.mc.MCPlayer;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class HologramRenderer {

    private String worldName;
    private final List<Integer> holograms = new ArrayList<>();

    public HologramRenderer(MCPlayer player) {
        this.worldName = player.getWorld().name();
    }

    public void removeFarHolograms(MCPlayer player, HologramFactory hologramFactory) {
        String newWorldName = player.getWorld().name();
        if (!this.worldName.equals(newWorldName)) {
            this.reset(player, hologramFactory);
            this.worldName = newWorldName;
            return;
        }

        this.holograms.removeIf(id -> {
            Hologram hologram = hologramFactory.get(id);

            if (hologram == null) {
                return true;
            }

            if (hologram.distanceTo(player) > HologramFactory.DISTANCE_TO_RENDER) {
                hologram.removePlayer(player);
                return true;
            }

            return false;
        });
    }

    public void removeHologram(MCPlayer player, Hologram hologram) {
        hologram.removePlayer(player);
        this.holograms.remove((Integer) hologram.getId());
    }

    public void reset(MCPlayer player, HologramFactory hologramFactory) {
        this.holograms.forEach(id -> {
            Hologram hologram = hologramFactory.get(id);
            if (hologram == null) {
                return;
            }

            hologram.removePlayer(player);
        });
        this.holograms.clear();
    }

    public void addNearHolograms(MCPlayer player, HologramFactory hologramFactory) {
        hologramFactory.all()
                .stream()
                .filter(hologram -> !this.holograms.contains(hologram.getId()))
                .filter(hologram -> hologram.distanceTo(player) <= HologramFactory.DISTANCE_TO_RENDER)
                .forEach(hologram -> {
                    hologram.spawnPlayer(player);
                    this.holograms.add(hologram.getId());
                });
    }

}
