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

import com.github.retrooper.packetevents.manager.server.ServerVersion;
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
import io.fairyproject.mc.protocol.MCProtocol;
import io.fairyproject.mc.registry.player.MCPlayerRegistry;
import io.fairyproject.mc.scheduler.MCSchedulerProvider;
import io.fairyproject.scheduler.response.TaskResponse;
import io.fairyproject.sidebar.handler.SidebarHandler;
import io.fairyproject.sidebar.handler.legacy.LegacySidebarHandler;
import io.fairyproject.sidebar.handler.legacy.V13LegacySidebarHandler;
import io.fairyproject.sidebar.handler.modern.ModernSidebarHandler;
import io.fairyproject.sidebar.handler.modern.LunarFixModernSidebarHandler;
import io.fairyproject.util.Stacktrace;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

@InjectableComponent
@RequiredArgsConstructor
public class SidebarService {

    private static final boolean LUNAR_CLIENT_FIX = System.getProperty("fairy.sidebar.lunar-client-fix", "true").equalsIgnoreCase("true");
    private final List<SidebarProvider> providers = new ArrayList<>();
    private final AtomicBoolean taskState = new AtomicBoolean(false);
    private final ContainerContext containerContext;
    private final MCProtocol mcProtocol;
    private final MCPlayerRegistry mcPlayerRegistry;
    private final MCSchedulerProvider mcSchedulerProvider;
    private SidebarHandler sidebarHandler;

    @PreInitialize
    @SuppressWarnings("deprecation")
    public void onPreInitialize() {
        ServerVersion version = mcProtocol.getPacketEvents().getServerManager().getVersion();
        if (version.isNewerThanOrEquals(ServerVersion.V_1_20_3)) {
            // 1.20.3+
            if (LUNAR_CLIENT_FIX) {
                this.sidebarHandler = new LunarFixModernSidebarHandler();
            } else {
                this.sidebarHandler = new ModernSidebarHandler();
            }
        } else if (version.isNewerThanOrEquals(ServerVersion.V_1_13)) {
            // 1.13+
            this.sidebarHandler = new V13LegacySidebarHandler();
        } else {
            // 1.8 - 1.12
            this.sidebarHandler = new LegacySidebarHandler();
        }

        this.containerContext.objectCollectorRegistry().add(ContainerObjCollector.create()
                .withFilter(ContainerObjCollector.inherits(SidebarAdapter.class))
                .withAddHandler(ContainerObjCollector.warpInstance(SidebarAdapter.class, adapter -> this.addProvider(SidebarAdapter.asProvider(adapter))))
                .withRemoveHandler(ContainerObjCollector.warpInstance(SidebarAdapter.class, adapter -> this.removeProvider(SidebarAdapter.asProvider(adapter))))
        );

        this.containerContext.objectCollectorRegistry().add(ContainerObjCollector.create()
                .withFilter(ContainerObjCollector.inherits(SidebarProvider.class))
                .withAddHandler(ContainerObjCollector.warpInstance(SidebarProvider.class, this::addProvider))
                .withRemoveHandler(ContainerObjCollector.warpInstance(SidebarProvider.class, this::removeProvider))
        );
    }

    @PostInitialize
    public void onPostInitialize() {
        this.scheduleTask();
    }

    @Subscribe
    public void onPlayerJoin(MCPlayerJoinEvent event) {
        this.getOrCreate(event.getPlayer());
    }

    @Subscribe
    public void onPlayerQuit(MCPlayerQuitEvent event) {
        this.remove(event.getPlayer());
    }

    public void addProvider(SidebarProvider provider) {
        this.providers.add(provider);
        this.providers.sort(Collections.reverseOrder(Comparator.comparingInt(SidebarProvider::getPriority)));

        this.scheduleTask();
    }

    public void removeProvider(SidebarProvider provider) {
        this.providers.remove(provider);
    }

    private void scheduleTask() {
        if (taskState.compareAndSet(false, true))
            return;

        mcSchedulerProvider.getAsyncScheduler().scheduleAtFixedRate(this::onTick, 2L, 2L);
    }

    public TaskResponse<Void> onTick() {
        try {
            this.tick();
        } catch (Exception ex) {
            Stacktrace.print(ex);
        }

        if (this.providers.isEmpty()) {
            this.taskState.set(false);
            return TaskResponse.success(null);
        }

        return TaskResponse.continueTask();
    }

    private void tick() {
        if (!Fairy.isRunning())
            return;

        for (MCPlayer player : this.mcPlayerRegistry.getAllPlayers()) {
            Sidebar sidebar = this.get(player);
            if (sidebar == null)
                continue;

            sidebar.setTicks(sidebar.getTicks() + 1);
            if (sidebar.getTicks() < 2)
                continue;

            SidebarData data = this.writeProviderToData(player);
            if (data == null) {
                if (sidebar.getProvider() != null)
                    // Sidebar is hidden
                    sidebar.getProvider().onSidebarHidden(player, sidebar);

                sidebar.remove();
                continue;
            }

            SidebarProvider provider = data.getProvider();
            if (!sidebar.isAvailable())
                // Sidebar is shown
                provider.onSidebarShown(player, sidebar);

            sidebar.setProvider(provider);
            sidebar.setTitle(data.getTitle());
            sidebar.setLines(data.getLines());
        }
    }

    private SidebarData writeProviderToData(MCPlayer player) {
        for (SidebarProvider provider : this.providers) {
            Component title = provider.getTitle(player);
            List<SidebarLine> lines = provider.getLines(player);
            if (title != null && lines != null && !lines.isEmpty()) {
                return new SidebarData(provider, title, lines);
            }
        }

        return null;
    }

    public void remove(MCPlayer player) {
        Sidebar sidebar = this.get(player);
        if (sidebar == null)
            return;

        sidebar.remove();
        player.metadata().remove(Sidebar.METADATA_TAG);
    }

    public Sidebar get(MCPlayer player) {
        return player.metadata().getOrNull(Sidebar.METADATA_TAG);
    }

    public Sidebar getOrCreate(MCPlayer player) {
        return player.metadata().getOrPut(Sidebar.METADATA_TAG, () -> new Sidebar(player, this.sidebarHandler));
    }

    @RequiredArgsConstructor
    @Getter
    private static class SidebarData {
        private final SidebarProvider provider;
        private final Component title;
        private final List<SidebarLine> lines;
    }

}
