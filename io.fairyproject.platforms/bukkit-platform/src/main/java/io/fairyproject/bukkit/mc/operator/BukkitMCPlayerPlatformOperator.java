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

package io.fairyproject.bukkit.mc.operator;

import io.fairyproject.bukkit.mc.BukkitMCPlayer;
import io.fairyproject.bukkit.mc.entity.BukkitDataWatcherConverter;
import io.fairyproject.bukkit.util.Players;
import io.fairyproject.mc.MCPlayer;
import io.fairyproject.mc.MCServer;
import io.fairyproject.mc.registry.player.MCPlayerPlatformOperator;
import io.fairyproject.mc.version.MCVersionMappingRegistry;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.net.InetAddress;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class BukkitMCPlayerPlatformOperator implements MCPlayerPlatformOperator {

    private final MCServer mcServer;
    private final BukkitAudiences bukkitAudiences;
    private final BukkitDataWatcherConverter dataWatcherConverter;
    private final BukkitMCPlayerOperator playerOperator;
    protected final MCVersionMappingRegistry versionMappingRegistry;

    @Override
    public UUID getUniqueId(@NotNull Object platformPlayer) {
        return Players.tryGetUniqueId(platformPlayer);
    }

    @Override
    public String getName(@NotNull Object platformPlayer) {
        if (platformPlayer instanceof String)
            return (String) platformPlayer;

        if (platformPlayer instanceof Player)
            return ((Player) platformPlayer).getName();

        throw new IllegalArgumentException(platformPlayer.getClass().getName());
    }

    @Override
    public List<MCPlayer> loadOnlinePlayers() {
        return Bukkit.getOnlinePlayers().stream()
                .map(player -> {
                    MCPlayer mcPlayer = create(player.getName(), player.getUniqueId(), Objects.requireNonNull(player.getAddress()).getAddress());
                    mcPlayer.setNative(player);

                    return mcPlayer;
                })
                .collect(Collectors.toList());
    }

    @Override
    public MCPlayer create(
            @NotNull String name,
            @NotNull UUID uuid,
            @NotNull InetAddress address) {
        return new BukkitMCPlayer(uuid, name, address, mcServer, bukkitAudiences, dataWatcherConverter, playerOperator, versionMappingRegistry);
    }

}
