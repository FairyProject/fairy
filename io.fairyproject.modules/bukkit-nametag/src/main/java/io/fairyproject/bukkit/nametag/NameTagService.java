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

package io.fairyproject.bukkit.nametag;

import com.google.common.primitives.Ints;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import io.fairyproject.Fairy;
import io.fairyproject.bean.*;
import io.fairyproject.bukkit.nametag.impl.DefaultNameTagAdapter;
import io.fairyproject.module.Modular;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import io.fairyproject.bukkit.Imanity;
import io.fairyproject.bukkit.metadata.Metadata;
import io.fairyproject.metadata.MetadataKey;
import io.fairyproject.util.Stacktrace;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Modular(
        value = "bukkit-nametag"
)
@Service(name = "nametag")
public class NameTagService {

    protected static MetadataKey<NameTagList> TEAM_INFO_KEY = MetadataKey.create(Fairy.METADATA_PREFIX + "TeamInfo", NameTagList.class);
    private static final int UPDATE_INTERVAL = 2;

    private Map<String, NameTagInfo> registeredTeams;
    private List<NameTagAdapter> adapters;
    private Queue<NameTagUpdate> pendingUpdates;
    private ScheduledExecutorService executorService;

    @PreInitialize
    public void preInit() {
        ComponentRegistry.registerComponentHolder(new ComponentHolder() {
            @Override
            public Class<?>[] type() {
                return new Class[] { NameTagAdapter.class };
            }

            @Override
            public void onEnable(Object instance) {
                register((NameTagAdapter) instance);
            }
        });
    }

    @PostInitialize
    public void init() {

        this.adapters = new LinkedList<>();
        this.registeredTeams = new HashMap<>();

        this.pendingUpdates = new ConcurrentLinkedQueue<>();

        this.executorService = Executors.newSingleThreadScheduledExecutor(new ThreadFactoryBuilder()
            .setDaemon(true)
            .setNameFormat("Imanity - Name Tag")
            .setUncaughtExceptionHandler((thread, throwable) -> Stacktrace.print(throwable))
            .build());
        this.executorService.scheduleWithFixedDelay(this::update, 50 * UPDATE_INTERVAL, 50 * UPDATE_INTERVAL, TimeUnit.MILLISECONDS);

        this.register(new DefaultNameTagAdapter());
    }

    @PostDestroy
    public void stop() {
        this.executorService.shutdown();
    }

    private void update() {

        NameTagUpdate update;
        while ((update = this.pendingUpdates.poll()) != null) {
            this.applyUpdate(update);
        }

    }

    public void disconnect(Player player) {
        this.executorService.submit(() -> this.applyDisconnect(player.getName()));
    }

    protected void applyDisconnect(String name) {

        for (Player other : Imanity.getPlayers()) {
            if (other.getName().equals(name) || !other.isOnline()) {
                continue;
            }

            NameTagList list = Metadata
                    .provideForPlayer(other)
                    .getOrNull(TEAM_INFO_KEY);

            if (list != null) {

                NameTagInfo tagInfo = list.getTeamFor(name);
                if (tagInfo != null) {
                    tagInfo.removeName(name);
                    list.removeTeamFor(name);

                    Imanity.IMPLEMENTATION.sendMember(other, tagInfo.getName(), Collections.singleton(name), 4);
                }

            }
        }

    }

    public void register(NameTagAdapter adapter) {
        this.adapters.add(adapter);
        this.adapters.sort((o1, o2) -> Ints.compare(o2.getWeight(), o1.getWeight()));
    }

    public void updateFromThirdSide(Player toRefresh) {
        NameTagUpdate update = new NameTagUpdate(toRefresh);

        this.pendingUpdates.add(update);
    }

    public void updateFromFirstSide(Player refreshFor) {
        Imanity.getPlayers().forEach(toRefresh -> {
            if (refreshFor == toRefresh) {
                return;
            }

            this.updateFor(toRefresh, refreshFor);
        });
    }

    public void updateFor(Player toRefresh, Player refreshFor) {
        NameTagUpdate update = new NameTagUpdate(toRefresh, refreshFor);

        this.pendingUpdates.add(update);
    }

    public void updateAll() {
        Imanity.getPlayers().forEach(this::updateFromThirdSide);
    }

    protected void applyUpdate(NameTagUpdate update) {
        Player toRefresh = Bukkit.getPlayer(update.getToRefresh());
        if (toRefresh == null) {
            return;
        }

        if (update.getRefreshFor() == null) {
            Imanity.getPlayers().forEach(refreshFor -> this.updateForInternal(toRefresh, refreshFor));
        } else {
            Player refreshFor = Bukkit.getPlayer(update.getRefreshFor());
            if (refreshFor != null) {
                this.updateForInternal(toRefresh, refreshFor);
            }
        }
    }

    private void updateForInternal(Player toRefresh, Player refreshFor) {
        NameTagInfo info = null;

        for (NameTagAdapter adapter : this.adapters) {
            info = adapter.fetch(refreshFor, toRefresh);
            if (info != null) {
                break;
            }
        }

        if (info == null) {
            return;
        }

        NameTagList list = Metadata
                .provideForPlayer(refreshFor)
                .getOrPut(TEAM_INFO_KEY, NameTagList::new);

        list.putTeamFor(toRefresh.getName(), info);
        Imanity.IMPLEMENTATION.sendMember(refreshFor, info.getName(), Collections.singleton(toRefresh.getName()), 3);
    }

    @Nullable
    protected NameTagInfo getTeam(String prefix, String suffix) {
        return this.registeredTeams.getOrDefault(this.toKey(prefix, suffix), null);
    }

    protected void initialPlayer(Player player) {
        for (NameTagInfo tagInfo : this.registeredTeams.values()) {
            this.sendTeam(player, tagInfo, 0);
        }
    }

    protected NameTagInfo getOrCreate(String prefix, String suffix) {
        NameTagInfo info = this.getTeam(prefix, suffix);

        if (info != null) {
            return info;
        }

        NameTagInfo newTeam = new NameTagInfo(prefix, suffix);
        this.registeredTeams.put(this.toKey(prefix, suffix), newTeam);
        for (Player player : Imanity.getPlayers()) {
            this.sendTeam(player, newTeam, 0);
        }
        return newTeam;
    }

    private void sendTeam(Player player, NameTagInfo info, int type) {
        Imanity.IMPLEMENTATION.sendTeam(player, info.getName(), info.getPrefix(), info.getSuffix(), info.getNameSet(), type);
    }

    private String toKey(String prefix, String suffix) {
        return prefix + ":" + suffix;
    }
}
