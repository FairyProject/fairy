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

package io.fairyproject.mc.registry.player;

import io.fairyproject.container.PostInitialize;
import io.fairyproject.mc.MCPlayer;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@RequiredArgsConstructor
public class MCPlayerRegistryImpl implements MCPlayerRegistry {

    private final MCPlayerPlatformOperator playerPlatformOperator;
    protected final Map<UUID, MCPlayer> players = new ConcurrentHashMap<>();

    @PostInitialize
    public void onPostInitialize() {
        for (MCPlayer mcPlayer : playerPlatformOperator.loadOnlinePlayers()) {
            this.addPlayer(mcPlayer);
        }
    }

    @Override
    public @NotNull MCPlayer findPlayerByUuid(@NotNull UUID uuid) {
        MCPlayer mcPlayer = players.get(uuid);
        if (mcPlayer == null)
            throw new IllegalArgumentException("Player with UUID " + uuid + " does not exist");

        return mcPlayer;
    }

    @Override
    public @NotNull MCPlayer findPlayerByName(@NotNull String name) {
        // TODO: optimize this to use a map of names to MCPlayers
        MCPlayer mcPlayer = players.values().stream()
                .filter(player -> player.getName().equals(name))
                .findFirst()
                .orElse(null);
        if (mcPlayer == null)
            throw new IllegalArgumentException("Player with name " + name + " does not exist");

        return mcPlayer;
    }

    @Override
    public @NotNull MCPlayer findPlayerByPlatformPlayer(@NotNull Object platformPlayer) {
        UUID uuid = this.playerPlatformOperator.getUniqueId(platformPlayer);
        MCPlayer mcPlayer = this.players.get(uuid);
        if (mcPlayer == null)
            throw new IllegalArgumentException("Player with UUID " + uuid + " does not exist");

        return mcPlayer;
    }

    @Override
    public void addPlayer(@NotNull MCPlayer player) {
        if (players.containsKey(player.getUUID()))
            throw new IllegalArgumentException("Player with UUID " + player.getUUID() + " already exists");

        players.put(player.getUUID(), player);
    }

    @Override
    public @Nullable MCPlayer removePlayer(@NotNull UUID uuid) {
        MCPlayer retVal = this.players.remove(uuid);
        if (retVal == null)
            throw new IllegalArgumentException("Player with UUID " + uuid + " does not exist");

        return retVal;
    }

    public void removePlayer(@NotNull MCPlayer player) {
        removePlayer(player.getUUID());
    }

    @Override
    public Collection<MCPlayer> getAllPlayers() {
        return players.values();
    }
}
