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

import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerTeams;
import io.fairyproject.Fairy;
import io.fairyproject.container.ContainerContext;
import io.fairyproject.container.PreInitialize;
import io.fairyproject.container.Service;
import io.fairyproject.container.collection.ContainerObjCollector;
import io.fairyproject.event.GlobalEventNode;
import io.fairyproject.event.Subscribe;
import io.fairyproject.log.Log;
import io.fairyproject.mc.MCPlayer;
import io.fairyproject.mc.event.MCPlayerJoinEvent;
import io.fairyproject.mc.event.MCPlayerQuitEvent;
import io.fairyproject.mc.nametag.update.DuoPlayerNameTagUpdate;
import io.fairyproject.mc.nametag.update.NameTagUpdate;
import io.fairyproject.mc.nametag.update.SinglePlayerNameTagUpdate;
import io.fairyproject.mc.protocol.MCProtocol;
import io.fairyproject.metadata.MetadataKey;
import io.fairyproject.task.Task;
import io.fairyproject.util.Utility;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class NameTagService {

    protected static MetadataKey<NameTagList> TEAM_INFO_KEY = MetadataKey.create(Fairy.METADATA_PREFIX + "name-tag", NameTagList.class);

    private final AtomicInteger teamId = new AtomicInteger(0);
    private final Map<NameTag, NameTagData> nameTagData = new ConcurrentHashMap<>();
    private final List<NameTagAdapter> nameTagAdapters = new LinkedList<>();

    @PreInitialize
    public void onPreInitialize() {
        ContainerContext.get().objectCollectorRegistry().add(ContainerObjCollector.create()
                .withFilter(ContainerObjCollector.inherits(NameTagAdapter.class))
                .withAddHandler(ContainerObjCollector.warpInstance(NameTagAdapter.class, this::register))
                .withRemoveHandler(ContainerObjCollector.warpInstance(NameTagAdapter.class, this::unregister))
        );
    }

    private CompletableFuture<?> runAsync(Runnable runnable) {
        return Task.runAsync(() -> {
            try {
                runnable.run();
            } catch (Throwable throwable) {
                Log.error("An error occurred while running async task", throwable);
            }
        });
    }

    @Subscribe
    public void onPlayerJoin(MCPlayerJoinEvent event) {
        for (NameTagData data : this.nameTagData.values()) {
            this.sendCreatePacket(event.getPlayer(), data);
        }
    }

    @Subscribe
    public void onPlayerQuit(MCPlayerQuitEvent event) {
        final String name = event.getPlayer().getName();
        runAsync(() -> removeNameFromAll(name));
    }

    private void removeNameFromAll(String name) {
        for (MCPlayer player : MCPlayer.all()) {
            if (player.getName().equals(name))
                return;

            if (!player.isOnline())
                return;

            player.metadata().ifPresent(TEAM_INFO_KEY, list -> removeNameFromList(name, player, list));
        }
    }

    private void removeNameFromList(String name, MCPlayer player, NameTagList list) {
        NameTagData data = list.get(name);
        if (data == null)
            return;

        list.remove(name);

        WrapperPlayServerTeams packet = new WrapperPlayServerTeams(
                data.getName(),
                WrapperPlayServerTeams.TeamMode.REMOVE_ENTITIES,
                Optional.empty(),
                name
        );
        MCProtocol.sendPacket(player, packet);
    }

    public void register(NameTagAdapter adapter) {
        this.nameTagAdapters.add(adapter);
        this.nameTagAdapters.sort((o1, o2) -> Integer.compare(o2.getWeight(), o1.getWeight()));
    }

    public void unregister(NameTagAdapter adapter) {
        this.nameTagAdapters.remove(adapter);
    }

    public Collection<NameTagAdapter> getNameTagAdapters() {
        return Collections.unmodifiableCollection(this.nameTagAdapters);
    }

    public CompletableFuture<?> updateFromThirdSide(MCPlayer target) {
        NameTagUpdate update = NameTagUpdate.createAllToPlayer(target);
        return runAsync(() -> this.applyUpdate(update));
    }

    public CompletableFuture<?> updateFromFirstSide(MCPlayer player) {
        NameTagUpdate update = NameTagUpdate.createPlayerToAll(player);
        return runAsync(() -> this.applyUpdate(update));
    }

    public CompletableFuture<?> update(MCPlayer player) {
        NameTagUpdate firstSide = NameTagUpdate.createPlayerToAll(player);
        NameTagUpdate thirdSide = NameTagUpdate.createAllToPlayer(player);
        return runAsync(() -> {
            this.applyUpdate(firstSide);
            this.applyUpdate(thirdSide);
        });
    }

    public CompletableFuture<?> update(MCPlayer target, MCPlayer player) {
        NameTagUpdate update = NameTagUpdate.create(target, player);
        return runAsync(() -> this.applyUpdate(update));
    }

    public CompletableFuture<?> updateAll() {
        return runAsync(() -> this.applyUpdate(NameTagUpdate.all()));
    }

    protected void applyUpdate(@NotNull NameTagUpdate update) {
        switch (update.getType()) {
            case ALL:
                this.applyUpdateAll();
                break;
            case ALL_TO_PLAYER:
                this.applyUpdateAllToPlayer((SinglePlayerNameTagUpdate) update);
                break;
            case PLAYER_TO_ALL:
                this.applyUpdatePlayerToAll((SinglePlayerNameTagUpdate) update);
                break;
            case PLAYER_TO_PLAYER:
                this.applyUpdatePlayerToPlayer((DuoPlayerNameTagUpdate) update);
                break;
            default:
                throw new IllegalArgumentException("Unknown update type: " + update.getType());
        }
    }

    private void applyUpdatePlayerToPlayer(DuoPlayerNameTagUpdate update) {
        MCPlayer player = MCPlayer.find(update.getPlayer());
        MCPlayer target = MCPlayer.find(update.getTarget());
        if (player != null && target != null)
            this.updateForInternal(player, target);
    }

    private void applyUpdatePlayerToAll(SinglePlayerNameTagUpdate update) {
        MCPlayer target = MCPlayer.find(update.getPlayer());
        if (target != null)
            MCPlayer.all().forEach(player -> this.updateForInternal(target, player));
    }

    private void applyUpdateAllToPlayer(SinglePlayerNameTagUpdate update) {
        MCPlayer target = MCPlayer.find(update.getPlayer());
        if (target != null)
            MCPlayer.all().forEach(player -> this.updateForInternal(player, target));
    }

    private void applyUpdateAll() {
        Utility.twice(MCPlayer.all(), this::updateForInternal);
    }

    @Nullable
    public NameTag findNameTag(MCPlayer player, MCPlayer target) {
        for (NameTagAdapter adapter : this.nameTagAdapters) {
            NameTag nametag = adapter.fetch(player, target);
            if (nametag != null)
                return nametag;
        }
        return null;
    }

    private void updateForInternal(MCPlayer player, MCPlayer target) {
        NameTag nameTag = this.findNameTag(player, target);
        if (nameTag == null)
            return;

        NameTagUpdateEvent event = new NameTagUpdateEvent(player, target, nameTag);
        GlobalEventNode.get().call(event);
        if (event.isCancelled())
            return;

        nameTag = event.getNameTag();

        NameTagList list = player.metadata().getOrPut(TEAM_INFO_KEY, NameTagList::new);
        NameTagData nameTagData = this.getOrCreateData(nameTag);
        list.add(target.getName(), nameTagData);

        WrapperPlayServerTeams packet = new WrapperPlayServerTeams(
                nameTagData.getName(),
                WrapperPlayServerTeams.TeamMode.ADD_ENTITIES,
                Optional.empty(),
                target.getName()
        );
        MCProtocol.sendPacket(player, packet);
    }

    @Nullable
    protected NameTagData getData(NameTag nameTag) {
        return this.nameTagData.getOrDefault(nameTag, null);
    }

    protected NameTagData getOrCreateData(NameTag nameTag) {
        NameTagData data = this.getData(nameTag);
        if (data != null)
            return data;

        NameTagData newData = new NameTagData(this.generateTeamName(), nameTag);
        this.nameTagData.put(nameTag, newData);
        this.sendDataToAll(newData);

        return newData;
    }

    private void sendDataToAll(NameTagData newData) {
        for (MCPlayer player : MCPlayer.all())
            this.sendCreatePacket(player, newData);
    }

    private String generateTeamName() {
        return "team-" + this.teamId.getAndIncrement();
    }

    private void sendCreatePacket(MCPlayer mcPlayer, NameTagData data) {
        NameTag nameTag = data.getNameTag();

        Component prefix = Component.empty();
        if (nameTag.getPrefix() != null)
            prefix = nameTag.getPrefix();

        Component suffix = Component.empty();
        if (nameTag.getSuffix() != null)
            suffix = nameTag.getSuffix();

        WrapperPlayServerTeams.NameTagVisibility packetVisibility = nameTag.getNameTagVisibility();
        if (packetVisibility == null)
            packetVisibility = WrapperPlayServerTeams.NameTagVisibility.ALWAYS;

        TextColor color = nameTag.getColor();
        if (color == null) {
            color = prefix.color();
            if (color == null)
                color = NamedTextColor.WHITE;
        }

        WrapperPlayServerTeams packet = new WrapperPlayServerTeams(
                data.getName(),
                WrapperPlayServerTeams.TeamMode.CREATE,
                Optional.of(new WrapperPlayServerTeams.ScoreBoardTeamInfo(
                        Component.empty(),
                        prefix,
                        suffix,
                        packetVisibility,
                        WrapperPlayServerTeams.CollisionRule.ALWAYS,
                        NamedTextColor.nearestTo(color),
                        WrapperPlayServerTeams.OptionData.NONE
                ))
        );
        MCProtocol.sendPacket(mcPlayer, packet);
    }
}
