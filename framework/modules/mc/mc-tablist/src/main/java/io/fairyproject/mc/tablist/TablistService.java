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

import io.fairyproject.Fairy;
import io.fairyproject.container.*;
import io.fairyproject.container.collection.ContainerObjCollector;
import io.fairyproject.event.Subscribe;
import io.fairyproject.mc.MCPlayer;
import io.fairyproject.mc.event.MCPlayerJoinEvent;
import io.fairyproject.mc.event.MCPlayerQuitEvent;
import io.fairyproject.mc.registry.player.MCPlayerRegistry;
import io.fairyproject.mc.scheduler.MCSchedulerProvider;
import io.fairyproject.mc.tablist.util.TabSlot;
import io.fairyproject.metadata.MetadataKey;
import io.fairyproject.util.Stacktrace;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;

@Getter
@InjectableComponent
@RequiredArgsConstructor
public class TablistService {

    private static final MetadataKey<Tablist> TABLIST_KEY = MetadataKey.create(Fairy.METADATA_PREFIX + "TabList", Tablist.class);
    public static TablistService INSTANCE;

    private final TablistSender tablistSender = new TablistSender();
    private final List<TablistAdapter> adapters = new ArrayList<>();
    private final ContainerContext containerContext;
    private final MCPlayerRegistry mcPlayerRegistry;
    private final MCSchedulerProvider mcSchedulerProvider;

    private ScheduledExecutorService thread;

    //Tablist Ticks
    @Setter
    private long ticks = 20;

    @PreInitialize
    public void onPreInitialize() {
        INSTANCE = this;

        this.containerContext.objectCollectorRegistry().add(ContainerObjCollector.create()
                .withFilter(ContainerObjCollector.inherits(TablistAdapter.class))
                .withAddHandler(ContainerObjCollector.warpInstance(TablistAdapter.class, this::registerAdapter))
                .withRemoveHandler(ContainerObjCollector.warpInstance(TablistAdapter.class, this::unregisterAdapter))
        );
    }

    @PostInitialize
    public void onPostInitialize() {
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

    public void registerPlayerTablist(MCPlayer player) {
        Tablist tablist = new Tablist(player, this, this.tablistSender);

        player.metadata().put(TABLIST_KEY, tablist);
    }

    public void removePlayerTablist(MCPlayer player) {
        player.metadata().remove(TABLIST_KEY);
    }

    private void setup() {
        // Start Thread
        this.mcSchedulerProvider.getAsyncScheduler().scheduleAtFixedRate(() -> {
            if (this.adapters.isEmpty())
                return;

            for (MCPlayer player : this.mcPlayerRegistry.getAllPlayers()) {
                player.metadata().ifPresent(TABLIST_KEY, tablist -> {
                    try {
                        tablist.update();
                    } catch (Throwable throwable) {
                        Stacktrace.print(throwable);
                    }
                });
            }
        }, this.ticks, this.ticks);
    }

    public void stop() {
        if (this.thread != null) {
            this.thread.shutdown();
            this.thread = null;
        }
    }
}
