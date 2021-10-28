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

package io.fairyproject.bukkit.tablist;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import io.fairyproject.bukkit.packet.PacketDto;
import io.fairyproject.bukkit.packet.type.PacketTypeClasses;
import io.fairyproject.bukkit.packet.wrapper.server.WrappedPacketOutLogin;
import io.fairyproject.bukkit.reflection.MinecraftReflection;
import io.fairyproject.bukkit.tablist.util.IImanityTabImpl;
import io.fairyproject.bukkit.tablist.util.impl.ProtocolLibTabImpl;
import io.fairyproject.bukkit.tablist.util.impl.v1_8.NMS1_8TabImpl;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import io.fairyproject.Fairy;
import io.fairyproject.bukkit.FairyBukkitPlatform;
import io.fairyproject.bukkit.listener.events.Events;
import io.fairyproject.bukkit.packet.PacketListener;
import io.fairyproject.bukkit.packet.PacketService;
import io.fairyproject.bean.Beans;
import io.fairyproject.bukkit.metadata.Metadata;
import io.fairyproject.metadata.MetadataKey;
import lombok.Getter;
import lombok.Setter;
import io.fairyproject.bean.Autowired;
import io.fairyproject.util.Stacktrace;

import java.util.concurrent.*;

@Getter
public class ImanityTabHandler {

    // TODO
//    public static void registerTablistHandler(ImanityTabAdapter adapter) {
//        Imanity.TAB_HANDLER = new ImanityTabHandler(adapter);
//    }

    //Instance
    @Getter
    private static ImanityTabHandler instance;

    private static final MetadataKey<ImanityTablist> TABLIST_KEY = MetadataKey.create(Fairy.METADATA_PREFIX + "TabList", ImanityTablist.class);

    private final ImanityTabAdapter adapter;
    private ScheduledExecutorService thread;
    private IImanityTabImpl implementation;

    @Autowired
    private PacketService packetService;

    //Tablist Ticks
    @Setter private long ticks = 20;

    public ImanityTabHandler(ImanityTabAdapter adapter) {
        if (instance != null) {
            throw new RuntimeException("ImanityTab has already been instantiated!");
        }

        instance = this;

        this.adapter = adapter;

        Beans.inject(this);

        this.registerImplementation();
        this.setup();

        Events.subscribe(PlayerJoinEvent.class).listen(event -> {
            this.registerPlayerTablist(event.getPlayer());
        }).build(FairyBukkitPlatform.PLUGIN);
        Events.subscribe(PlayerQuitEvent.class).listen(event -> {
            this.removePlayerTablist(event.getPlayer());
        }).build(FairyBukkitPlatform.PLUGIN);
    }

    private void registerImplementation() {
        if (Bukkit.getPluginManager().getPlugin("ProtocolLib") != null) {
            this.implementation = new ProtocolLibTabImpl();
            FairyBukkitPlatform.PLUGIN.getLogger().info("Registered Tablist Implementation with ProtocolLib");
            return;
        }

        if (MinecraftReflection.VERSION == MinecraftReflection.Version.v1_8_R3) {

            this.implementation = new NMS1_8TabImpl();
            return;
        }
        FairyBukkitPlatform.PLUGIN.getLogger().info("Unable to register ImanityTablist with a proper implementation");
    }

    public void registerPlayerTablist(Player player) {
        ImanityTablist tablist = new ImanityTablist(player);

        Metadata
                .provideForPlayer(player)
                .put(TABLIST_KEY, tablist);
    }

    public void removePlayerTablist(Player player) {
        Metadata
                .provideForPlayer(player)
                .remove(TABLIST_KEY);
    }

    private void setup() {

        //Ensure that the thread has stopped running
        if (this.thread != null) {
            this.thread.shutdown();
            this.thread = null;
        }

        // To ensure client will display 60 slots on 1.7
        if (Bukkit.getMaxPlayers() < 60) {
//            this.implementation.registerLoginListener();
            packetService.registerPacketListener(new PacketListener() {
                @Override
                public Class<?>[] type() {
                    return new Class[] { PacketTypeClasses.Server.LOGIN };
                }

                @Override
                public boolean write(Player player, PacketDto dto) {
                    WrappedPacketOutLogin packet = dto.wrap(WrappedPacketOutLogin.class);
                    packet.setMaxPlayers(60);

                    dto.refresh();
                    return true;
                }
            });
        }

        //Start Thread
        this.thread = Executors.newSingleThreadScheduledExecutor(new ThreadFactoryBuilder()
                .setNameFormat("Imanity-Tablist-Thread")
                .setDaemon(true)
                .setUncaughtExceptionHandler((thread1, throwable) -> Stacktrace.print(throwable))
            .build());

        this.thread.scheduleAtFixedRate(() -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                ImanityTablist tablist = Metadata
                        .provideForPlayer(player)
                        .getOrNull(TABLIST_KEY);

                if (tablist != null) {
                    try {
                        tablist.update();
                    } catch (Throwable throwable) {
                        Stacktrace.print(throwable);
                    }
                }
            }
        }, 50L, 50L, TimeUnit.MILLISECONDS);
    }

    public void stop() {
        if (this.thread != null) {
            this.thread.shutdown();
            this.thread = null;
        }
    }
}
