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

package org.fairy.bukkit.scoreboard;

import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.fairy.Fairy;
import org.fairy.bean.*;
import org.fairy.bukkit.FairyBukkitPlatform;
import org.fairy.bukkit.Imanity;
import org.fairy.bukkit.listener.events.Events;
import org.fairy.bukkit.metadata.Metadata;
import org.fairy.task.Task;
import org.fairy.task.TaskRunnable;
import org.fairy.util.CC;
import org.fairy.util.Stacktrace;
import org.fairy.util.entry.Entry;
import org.fairy.util.terminable.Terminable;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

@Service(name = "sidebar")
public class SidebarService implements TaskRunnable {

    private List<SidebarAdapter> adapters;
    private Queue<Runnable> runnableQueue;
    private AtomicBoolean activated;

    @PreInitialize
    public void preInit() {
        this.adapters = new ArrayList<>();
        this.runnableQueue = new ConcurrentLinkedQueue<>();
        this.activated = new AtomicBoolean(true);
        ComponentRegistry.registerComponentHolder(new ComponentHolder() {
            @Override
            public Class<?>[] type() {
                return new Class[] { SidebarAdapter.class };
            }

            @Override
            public void onEnable(Object instance) {
                addAdapter((SidebarAdapter) instance);
            }
        });
    }

    @PostInitialize
    public void postInit() {
        Task.mainRepeated(this, this.getUpdateTick());
        Events.subscribe(PlayerJoinEvent.class).listen((subscription, event) -> getOrCreateScoreboard(event.getPlayer())).build(FairyBukkitPlatform.PLUGIN);
        Events.subscribe(PlayerQuitEvent.class).listen((subscription, event) -> remove(event.getPlayer())).build(FairyBukkitPlatform.PLUGIN);
    }

    public void addAdapter(SidebarAdapter adapter) {
        this.adapters.add(adapter);
        this.adapters.sort(Collections.reverseOrder(Comparator.comparingInt(SidebarAdapter::priority)));
        this.activate();
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
        for (Player player : Imanity.getPlayers()) {

            if (!Fairy.isRunning()) {
                break;
            }

            Sidebar board = this.get(player);
            if (board == null) {
                continue;
            }

            Entry<String, List<String>> entry = this.findAdapter(player);
            if (entry == null) {
                board.remove();
                continue;
            }
            String title = CC.translate(entry.getKey());

            board.setTitle(title);

            List<String> newLines = entry.getValue();

            if (newLines == null || newLines.isEmpty()) {
                board.remove();
            } else {

                board.setLines(newLines);

            }
        }
    }

    private Entry<String, List<String>> findAdapter(Player player) {
        Entry<String, List<String>> entry = null;

        for (SidebarAdapter adapter : this.adapters) {
            String title = adapter.getTitle(player);
            List<String> list = adapter.getLines(player);
            if (title != null && !title.isEmpty() &&
                    list != null && !list.isEmpty()) {
                entry = new Entry<>(title, list);
                break;
            }
        }

        return entry;
    }

    public void remove(Player player) {
        Sidebar board = this.get(player);

        if (board != null) {
            board.remove();
            Metadata.provideForPlayer(player).remove(Sidebar.METADATA_TAG);
        }
    }

    public Sidebar get(Player player) {
        return Metadata.provideForPlayer(player).getOrNull(Sidebar.METADATA_TAG);
    }

    public Sidebar getOrCreateScoreboard(Player player) {
        return Metadata.provideForPlayer(player).getOrPut(Sidebar.METADATA_TAG, () -> {
            Sidebar board = new Sidebar(player);
            for (SidebarAdapter adapter : this.adapters) {
                adapter.onBoardCreate(player, board);
            }
            return board;
        });
    }


}
