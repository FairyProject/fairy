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
import io.fairyproject.container.PostInitialize;
import io.fairyproject.container.PreInitialize;
import io.fairyproject.container.Service;
import io.fairyproject.container.collection.ContainerObjCollector;
import io.fairyproject.event.Subscribe;
import io.fairyproject.mc.MCPlayer;
import io.fairyproject.mc.event.MCPlayerJoinEvent;
import io.fairyproject.mc.event.MCPlayerQuitEvent;
import io.fairyproject.task.Task;
import io.fairyproject.task.TaskRunnable;
import io.fairyproject.util.Stacktrace;
import io.fairyproject.util.terminable.Terminable;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
public class SidebarService implements TaskRunnable {

    private List<SidebarAdapter> adapters;
    private Queue<Runnable> runnableQueue;
    private AtomicBoolean activated;

    @PreInitialize
    public void onPreInitialize() {
        this.adapters = new ArrayList<>();
        this.runnableQueue = new ConcurrentLinkedQueue<>();
        this.activated = new AtomicBoolean(true);
        ContainerContext.get().objectCollectorRegistry().add(ContainerObjCollector.create()
                .withFilter(ContainerObjCollector.inherits(SidebarAdapter.class))
                .withAddHandler(ContainerObjCollector.warpInstance(SidebarAdapter.class, this::addAdapter))
                .withRemoveHandler(ContainerObjCollector.warpInstance(SidebarAdapter.class, this::removeAdapter))
        );
    }

    @PostInitialize
    public void onPostInitialize() {
        Task.mainRepeated(this, this.getUpdateTick());
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
            Task.mainRepeated(this, this.getUpdateTick());
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

    @Override
    public void run(Terminable terminable) {
        try {
            this.tick();

            this.runQueue();
            if (this.adapters.isEmpty()) {
                terminable.close();
                this.activated.set(false);
            }
        } catch (Exception ex) {
            Stacktrace.print(ex);
        }
    }

    public void runQueue() {
        Runnable runnable;
        while ((runnable = this.runnableQueue.poll()) != null) {
            runnable.run();
        }
    }

    private void tick() {
        for (MCPlayer player : MCPlayer.all()) {
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
