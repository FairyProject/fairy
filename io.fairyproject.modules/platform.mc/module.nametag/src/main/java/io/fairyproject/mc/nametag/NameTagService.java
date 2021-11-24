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

package io.fairyproject.mc.nametag;

import com.google.common.primitives.Ints;
import io.fairyproject.Fairy;
import io.fairyproject.container.ComponentHolder;
import io.fairyproject.container.ComponentRegistry;
import io.fairyproject.container.PreInitialize;
import io.fairyproject.container.Service;
import io.fairyproject.event.Subscribe;
import io.fairyproject.mc.MCPlayer;
import io.fairyproject.mc.event.MCPlayerJoinEvent;
import io.fairyproject.mc.event.MCPlayerQuitEvent;
import io.fairyproject.mc.protocol.item.TeamAction;
import io.fairyproject.mc.protocol.packet.PacketPlay;
import io.fairyproject.metadata.MetadataKey;
import io.fairyproject.task.Task;
import io.fairyproject.util.Utility;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

@Service(name = "nametag")
public class NameTagService {

    protected static MetadataKey<NameTagList> TEAM_INFO_KEY = MetadataKey.create(Fairy.METADATA_PREFIX + "NameTag", NameTagList.class);

    private Map<String, NameTag> nametags;
    private List<NameTagAdapter> adapters;

    @PreInitialize
    public void onPreInitialize() {
        ComponentRegistry.registerComponentHolder(ComponentHolder.builder()
                .type(NameTagAdapter.class)
                .onEnable(obj -> this.register((NameTagAdapter) obj))
                .onDisable(obj -> this.unregister((NameTagAdapter) obj))
                .build());

        this.adapters = new LinkedList<>();
        this.nametags = new ConcurrentHashMap<>();
    }

    @Subscribe
    public void onPlayerJoin(MCPlayerJoinEvent event) {
        for (NameTag tagInfo : this.nametags.values()) {
            this.sendPacket(event.getPlayer(), tagInfo);
        }
    }

    @Subscribe
    public void onPlayerQuit(MCPlayerQuitEvent event) {
        final String name = event.getPlayer().getName();
        Task.runAsync(() -> MCPlayer.all().forEach(other -> {
            if (other.getName().equals(name) || !other.isOnline()) {
                return;
            }
            other.metadata().ifPresent(TEAM_INFO_KEY, list -> {
                NameTag nameTag = list.getNameTag(name);
                if (nameTag != null) {
                    nameTag.removeName(name);
                    list.removeNameTag(name);
                    other.sendPacket(PacketPlay.Out.ScoreboardTeam.builder()
                            .player(nameTag.getName())
                            .players(Collections.singleton(name))
                            .teamAction(TeamAction.LEAVE)
                            .build());
                }
            });
        }));
    }

    public void register(NameTagAdapter adapter) {
        this.adapters.add(adapter);
        this.adapters.sort((o1, o2) -> Ints.compare(o2.getWeight(), o1.getWeight()));
    }

    public void unregister(NameTagAdapter adapter) {
        this.adapters.remove(adapter);
    }

    public CompletableFuture<?> updateFromThirdSide(MCPlayer target) {
        NameTagUpdate update = NameTagUpdate.createTarget(target);
        return Task.runAsync(() -> this.applyUpdate(update));
    }

    public CompletableFuture<?> updateFromFirstSide(MCPlayer player) {
        NameTagUpdate update = NameTagUpdate.createTarget(player);
        return Task.runAsync(() -> this.applyUpdate(update));
    }

    public CompletableFuture<?> update(MCPlayer target, MCPlayer player) {
        NameTagUpdate update = NameTagUpdate.create(target, player);
        return Task.runAsync(() -> this.applyUpdate(update));
    }

    public CompletableFuture<?> updateAll() {
        return Task.runAsync(() -> this.applyUpdate(NameTagUpdate.all()));
    }

    protected void applyUpdate(NameTagUpdate update) {
        final UUID playerUuid = update.getPlayer();
        final UUID targetUuid = update.getTarget();
        if (playerUuid == null && targetUuid == null) {
            Utility.twice(MCPlayer.all(), this::updateForInternal);
        } else if (playerUuid != null && targetUuid == null) {
            MCPlayer player = MCPlayer.find(playerUuid);
            if (player != null) {
                MCPlayer.all().forEach(target -> this.updateForInternal(player, target));
            }
        } else if (playerUuid == null) {
            MCPlayer target = MCPlayer.find(targetUuid);
            if (target != null) {
                MCPlayer.all().forEach(player -> this.updateForInternal(player, target));
            }
        }
        MCPlayer player = MCPlayer.find(playerUuid);
        MCPlayer target = MCPlayer.find(targetUuid);
        if (player != null && target != null) {
            this.updateForInternal(player, target);
        }
    }

    private void updateForInternal(MCPlayer player, MCPlayer target) {
        for (NameTagAdapter adapter : this.adapters) {
            NameTag nametag = adapter.fetch(player, target);
            if (nametag != null) {
                NameTagList list = player.metadata().getOrPut(TEAM_INFO_KEY, NameTagList::new);

                list.addNameTag(target.getName(), nametag);
                player.sendPacket(PacketPlay.Out.ScoreboardTeam.builder()
                        .player(nametag.getName())
                        .players(Collections.singleton(target.getName()))
                        .teamAction(TeamAction.JOIN)
                        .build());
                break;
            }
        }
    }

    @Nullable
    protected NameTag getNameTag(Component prefix, Component suffix) {
        return this.nametags.getOrDefault(this.toKey(prefix, suffix), null);
    }

    protected NameTag getOrCreate(Component prefix, Component suffix) {
        NameTag info = this.getNameTag(prefix, suffix);

        if (info != null) {
            return info;
        }

        NameTag newTeam = new NameTag(prefix, suffix);
        this.nametags.put(this.toKey(prefix, suffix), newTeam);
        for (MCPlayer player : MCPlayer.all()) {
            this.sendPacket(player, newTeam);
        }
        return newTeam;
    }

    private void sendPacket(MCPlayer mcPlayer, NameTag info) {
        mcPlayer.sendPacket(PacketPlay.Out.ScoreboardTeam.builder()
                .player(info.getName())
                .teamAction(TeamAction.ADD)
                .parameters(Optional.of(PacketPlay.Out.ScoreboardTeam.Parameters.builder()
                        .playerPrefix(info.getPrefix())
                        .playerSuffix(info.getSuffix())
                        .build()))
                .build());
    }

    private String toKey(Component prefix, Component suffix) {
        return prefix + ":" + suffix;
    }
}
