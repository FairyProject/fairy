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

package io.fairyproject.sidebar;

import io.fairyproject.Fairy;
import io.fairyproject.container.ContainerContext;
import io.fairyproject.container.InjectableComponent;
import io.fairyproject.container.PostInitialize;
import io.fairyproject.container.PreInitialize;
import io.fairyproject.container.collection.ContainerObjCollector;
import io.fairyproject.event.Subscribe;
import io.fairyproject.mc.MCPlayer;
import io.fairyproject.mc.event.MCPlayerJoinEvent;
import io.fairyproject.mc.event.MCPlayerQuitEvent;
import io.fairyproject.mc.registry.player.MCPlayerRegistry;
import io.fairyproject.mc.scheduler.MCSchedulerProvider;
import io.fairyproject.scheduler.response.TaskResponse;
import io.fairyproject.util.Stacktrace;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

@InjectableComponent
@RequiredArgsConstructor
public class SidebarService {

    private final List<SidebarAdapter> adapters = new ArrayList<>();
    private final Queue<Runnable> runnableQueue = new ConcurrentLinkedQueue<>();
    private final AtomicBoolean activated = new AtomicBoolean(false);
    private final ContainerContext containerContext;
    private final MCPlayerRegistry mcPlayerRegistry;
    private final MCSchedulerProvider mcSchedulerProvider;

    @PreInitialize
    public void onPreInitialize() {
        this.containerContext.objectCollectorRegistry().add(ContainerObjCollector.create()
                .withFilter(ContainerObjCollector.inherits(SidebarAdapter.class))
                .withAddHandler(ContainerObjCollector.warpInstance(SidebarAdapter.class, this::addAdapter))
                .withRemoveHandler(ContainerObjCollector.warpInstance(SidebarAdapter.class, this::removeAdapter))
        );
    }

    @PostInitialize
    public void onPostInitialize() {
        this.activate();
    }

    @Subscribe
    public void onPlayerJoin(MCPlayerJoinEvent event) {
        this.getOrCreateScoreboard(event.getPlayer());
    }

    @Subscribe
    public void onPlayerQuit(MCPlayerQuitEvent event) {
        this.remove(event.getPlayer());
    }

    public void addAdapter(SidebarAdapter adapter) {
        this.adapters.add(adapter);
        this.adapters.sort(Collections.reverseOrder(Comparator.comparingInt(SidebarAdapter::priority)));
        this.activate();
    }

    public void removeAdapter(SidebarAdapter adapter) {
        this.adapters.remove(adapter);
    }

    private void activate() {
        if (activated.compareAndSet(false, true)) {
            mcSchedulerProvider.getAsyncScheduler().scheduleAtFixedRate(this::onTick, this.getUpdateTick(), this.getUpdateTick());
        }
    }

    private int getUpdateTick() {
        int tick = 2;
        for (SidebarAdapter adapter : this.adapters) {
            int adapterTick = adapter.tick();
            if (adapterTick != -1) {
                tick = adapterTick;
                break;
            }
        }

        return tick;
    }

    public TaskResponse<Void> onTick() {
        try {
            this.tick();

            this.runQueue();
            if (this.adapters.isEmpty()) {
                this.activated.set(false);
                return TaskResponse.success(null);
            }
        } catch (Exception ex) {
            Stacktrace.print(ex);
        }

        return TaskResponse.continueTask();
    }

    public void runQueue() {
        Runnable runnable;
        while ((runnable = this.runnableQueue.poll()) != null) {
            runnable.run();
        }
    }

    private void tick() {
        for (MCPlayer player : this.mcPlayerRegistry.getAllPlayers()) {
            if (!Fairy.isRunning()) {
                break;
            }

            Sidebar board = this.get(player);
            if (board == null) {
                continue;
            }

            SidebarInfo entry = this.findAdapter(player);
            if (entry == null) {
                board.remove();
                continue;
            }

            board.setTitle(entry.getTitle());

            if (entry.getLines() == null || entry.getLines().isEmpty()) {
                board.remove();
            } else {
                board.setLines(entry.getLines());
            }
        }
    }

    private SidebarInfo findAdapter(MCPlayer player) {
        SidebarInfo entry = null;

        for (SidebarAdapter adapter : this.adapters) {
            Component title = adapter.getTitle(player);
            List<Component> list = adapter.getLines(player);
            if (title != null && list != null && !list.isEmpty()) {
                entry = new SidebarInfo(title, list);
                break;
            }
        }

        return entry;
    }

    public void remove(MCPlayer player) {
        Sidebar board = this.get(player);

        if (board != null) {
            board.remove();
            player.metadata().remove(Sidebar.METADATA_TAG);
        }
    }

    public Sidebar get(MCPlayer player) {
        return player.metadata().getOrNull(Sidebar.METADATA_TAG);
    }

    public Sidebar getOrCreateScoreboard(MCPlayer player) {
        return player.metadata().getOrPut(Sidebar.METADATA_TAG, () -> {
            Sidebar board = new Sidebar(player);
            for (SidebarAdapter adapter : this.adapters) {
                adapter.onBoardCreate(player, board);
            }
            return board;
        });
    }

    @RequiredArgsConstructor
    @Getter
    private static class SidebarInfo {
        private final Component title;
        private final List<Component> lines;
    }

}
