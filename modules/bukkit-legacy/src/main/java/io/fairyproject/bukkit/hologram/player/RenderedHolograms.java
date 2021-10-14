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

package io.fairyproject.bukkit.hologram.player;

import lombok.Getter;
import org.bukkit.entity.Player;
import io.fairyproject.bukkit.hologram.Hologram;
import io.fairyproject.bukkit.hologram.HologramHandler;

import java.util.ArrayList;
import java.util.List;

@Getter
public class RenderedHolograms {

    private String worldName;
    private final List<Integer> holograms = new ArrayList<>();

    public RenderedHolograms(Player player) {
        this.worldName = player.getWorld().getName();
    }

    public void removeFarHolograms(Player player, HologramHandler hologramHandler) {

        String newWorldName = player.getWorld().getName();
        if (!this.worldName.equals(newWorldName)) {

            this.reset(player, hologramHandler);
            this.worldName = newWorldName;

            return;
        }

        this.holograms.removeIf(id -> {
            Hologram hologram = hologramHandler.getHologram(id);

            if (hologram == null) {
                return true;
            }

            if (hologram.distaneTo(player) > HologramHandler.DISTANCE_TO_RENDER) {
                hologram.removePlayer(player);
                return true;
            }

            return false;
        });
    }

    public void removeHologram(Player player, Hologram hologram) {
        hologram.removePlayer(player);
        this.holograms.remove((Integer) hologram.getId());
    }

    public void reset(Player player, HologramHandler hologramHandler) {
        this.holograms.forEach(id -> {
            Hologram hologram = hologramHandler.getHologram(id);
            if (hologram == null) {
                return;
            }

            hologram.removePlayer(player);
        });
        this.holograms.clear();
    }

    public void addNearHolograms(Player player, HologramHandler hologramHandler) {
        hologramHandler.getHolograms()
                .stream()
                .filter(hologram -> !this.holograms.contains(hologram.getId()))
                .filter(hologram -> hologram.distaneTo(player) <= HologramHandler.DISTANCE_TO_RENDER)
                .forEach(hologram -> {
                    hologram.spawnPlayer(player);
                    this.holograms.add(hologram.getId());
                });
    }

}
