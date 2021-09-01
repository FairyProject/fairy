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

package org.fairy.bukkit.util;

import lombok.experimental.UtilityClass;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffect;
import org.fairy.bukkit.events.player.PlayerClearEvent;
import org.fairy.task.Task;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@UtilityClass
public class Players {

    public UUID tryGetUniqueId(Object player) {
        if (!(player instanceof Player)) {
            throw new ClassCastException(player.getClass().getName() + " is not a Player");
        }

        return ((Player) player).getUniqueId();
    }

    public List<Player> transformUuids(Collection<UUID> uuids) {
        return transformUuids(uuids.stream());
    }

    public List<Player> transformUuids(UUID... uuids) {
        return transformUuids(Stream.of(uuids));
    }

    public List<Player> transformUuids(Stream<UUID> uuids) {
        return streamUuids(uuids).collect(Collectors.toList());
    }

    public Stream<Player> streamUuids(Collection<UUID> uuids) {
        return streamUuids(uuids.stream());
    }

    public Stream<Player> streamUuids(UUID... uuids) {
        return streamUuids(Stream.of(uuids));
    }

    public Stream<Player> streamUuids(Stream<UUID> uuids) {
        return uuids
                .map(Bukkit::getPlayer)
                .filter(Objects::nonNull);
    }

    public void clear(final Player player) {
        player.setHealth(player.getMaxHealth());
        player.setFoodLevel(20);
        player.setExp(0.0f);
        player.setTotalExperience(0);
        player.setLevel(0);
        player.setFireTicks(0);
        player.setFallDistance(0);
        player.getInventory().clear();
        player.getInventory().setArmorContents(null);
        player.updateInventory();
        player.setGameMode(GameMode.SURVIVAL);
        player.getActivePotionEffects().stream().map(PotionEffect::getType).forEach(player::removePotionEffect);

        new PlayerClearEvent(player).call();
    }

    public CompletableFuture<PlayerInventory> updateInventoryLater(Player player) {
        return CompletableFuture.supplyAsync(() -> {
            player.updateInventory();
            return player.getInventory();
        }, Task.mainLater(1));
    }

}
