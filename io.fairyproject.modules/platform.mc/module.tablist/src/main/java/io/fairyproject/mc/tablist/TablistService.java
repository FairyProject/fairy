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

package io.fairyproject.mc.tablist;

import io.fairyproject.container.*;
import io.fairyproject.mc.tablist.util.TabSlot;
import io.fairyproject.mc.tablist.util.TablistImpl;
import io.fairyproject.mc.tablist.util.impl.MainTablistImpl;
import io.fairyproject.event.Subscribe;
import io.fairyproject.mc.MCPlayer;
import io.fairyproject.mc.event.MCPlayerJoinEvent;
import io.fairyproject.mc.event.MCPlayerQuitEvent;
import io.fairyproject.task.Task;
import io.fairyproject.Fairy;
import io.fairyproject.metadata.MetadataKey;
import lombok.Getter;
import lombok.Setter;
import io.fairyproject.util.Stacktrace;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.*;

@Getter
@Service
public class TablistService {

    public static TablistService INSTANCE;

    private static final MetadataKey<Tablist> TABLIST_KEY = MetadataKey.create(Fairy.METADATA_PREFIX + "TabList", Tablist.class);

    private List<TablistAdapter> adapters;
    private ScheduledExecutorService thread;
    private TablistImpl implementation;

    //Tablist Ticks
    @Setter
    private long ticks = 20;

    @PreInitialize
    public void onPreInitialize() {
        INSTANCE = this;

        this.adapters = new ArrayList<>();
        ComponentRegistry.registerComponentHolder(ComponentHolder.builder()
                .type(TablistAdapter.class)
                .onEnable(obj -> this.registerAdapter((TablistAdapter) obj))
                .onDisable(obj -> this.unregisterAdapter((TablistAdapter) obj))
                .build());
    }

    @PostInitialize
    public void onPostInitialize() {
        this.registerImplementation();
        this.setup();
    }

    public void registerAdapter(TablistAdapter adapter) {
        this.adapters.add(adapter);
        this.adapters.sort((a, b) -> b.priority() - a.priority());
    }

    public void unregisterAdapter(TablistAdapter adapter) {
        this.adapters.remove(adapter);
    }

    @Nullable
    public Set<TabSlot> getSlots(MCPlayer mcPlayer) {
        for (TablistAdapter adapter : this.adapters) {
            final Set<TabSlot> slots = adapter.getSlots(mcPlayer);
            if (slots != null) {
                return slots;
            }
        }
        return null;
    }

    @Nullable
    public Component getHeader(MCPlayer mcPlayer) {
        for (TablistAdapter adapter : this.adapters) {
            final Component header = adapter.getHeader(mcPlayer);
            if (header != null) {
                return header;
            }
        }
        return null;
    }

    @Nullable
    public Component getFooter(MCPlayer mcPlayer) {
        for (TablistAdapter adapter : this.adapters) {
            final Component footer = adapter.getFooter(mcPlayer);
            if (footer != null) {
                return footer;
            }
        }
        return null;
    }

    @Subscribe
    public void onPlayerJoin(MCPlayerJoinEvent event) {
        this.registerPlayerTablist(event.getPlayer());
    }

    @Subscribe
    public void onPlayerQuit(MCPlayerQuitEvent event) {
        this.removePlayerTablist(event.getPlayer());
    }

    private void registerImplementation() {
        this.implementation = new MainTablistImpl();
    }

    public void registerPlayerTablist(MCPlayer player) {
        Tablist tablist = new Tablist(player);

        player.metadata().put(TABLIST_KEY, tablist);
    }

    public void removePlayerTablist(MCPlayer player) {
        player.metadata().remove(TABLIST_KEY);
    }

    private void setup() {
        // To ensure client will display 60 slots on 1.7
        // TODO
//        if (Bukkit.getMaxPlayers() < 60) {
//            this.implementation.registerLoginListener();
//            packetService.registerPacketListener(new PacketListener() {
//                @Override
//                public Class<?>[] type() {
//                    return new Class[] { PacketTypeClasses.Server.LOGIN };
//                }
//
//                @Override
//                public boolean write(Player player, PacketDto dto) {
//                    WrappedPacketOutLogin packet = dto.wrap(WrappedPacketOutLogin.class);
//                    packet.setMaxPlayers(60);
//
//                    dto.refresh();
//                    return true;
//                }
//            });
//        }

        //Start Thread
        Task.asyncRepeated(t -> {
            if (this.adapters.isEmpty()) {
                return;
            }
            for (MCPlayer player : MCPlayer.all()) {
                Tablist tablist = player.metadata().getOrNull(TABLIST_KEY);

                if (tablist != null) {
                    try {
                        tablist.update();
                    } catch (Throwable throwable) {
                        Stacktrace.print(throwable);
                    }
                }
            }
        }, this.ticks);
    }

    public void stop() {
        if (this.thread != null) {
            this.thread.shutdown();
            this.thread = null;
        }
    }
}
