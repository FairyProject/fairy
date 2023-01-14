/*
 * MIT License
 *
 * Copyright (c) 2022 Fairy Project
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

package io.fairyproject.mc.protocol;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.PacketEventsAPI;
import com.github.retrooper.packetevents.event.*;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import io.fairyproject.container.ContainerContext;
import io.fairyproject.container.PostDestroy;
import io.fairyproject.container.PostInitialize;
import io.fairyproject.container.PreInitialize;
import io.fairyproject.container.collection.ContainerObjCollector;
import io.fairyproject.event.GlobalEventNode;
import io.fairyproject.log.Log;
import io.fairyproject.mc.MCPlayer;
import io.fairyproject.mc.protocol.event.MCPlayerPacketReceiveEvent;
import io.fairyproject.mc.protocol.event.MCPlayerPacketSendEvent;
import io.fairyproject.mc.protocol.packet.PacketSender;
import io.fairyproject.mc.version.MCVersionMappingRegistry;
import io.fairyproject.util.terminable.Terminable;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Getter
@RequiredArgsConstructor
public class MCProtocol {
    public static MCProtocol INSTANCE;

    private final MCVersionMappingRegistry mappingRegistry;
    private final PacketEventsBuilder packetEventsBuilder;
    private final PacketSender packetSender;

    private PacketEventsAPI<?> packetEvents;

    private final Map<Class<?>, PacketListenerCommon> listenerCommonMap = new ConcurrentHashMap<>();

    @PreInitialize
    public void onPreInitialize() {
        INSTANCE = this;

        this.packetEvents = this.packetEventsBuilder.build();
        PacketEvents.setAPI(this.packetEvents);
        this.packetEvents.load();

        // automatically register PacketListener that are obj
        this.registerPacketListenerObjectCollector();
        this.registerMCEventTransformer();

        Log.info("Loaded MCProtocol with PacketEvents version %s on minecraft version %s", packetEvents.getVersion(), packetEvents.getServerManager().getVersion());
    }

    private void registerMCEventTransformer() {
        this.packetEvents.getEventManager().registerListener(new PacketListener() {

            @Override
            public void onPacketReceive(PacketReceiveEvent event) {
                Object player = event.getPlayer();
                if (player == null)
                    return;

                MCPlayer mcPlayer = MCPlayer.from(player);
                GlobalEventNode.get().call(new MCPlayerPacketReceiveEvent(mcPlayer, event));
            }

            @Override
            public void onPacketSend(PacketSendEvent event) {
                Object player = event.getPlayer();
                if (player == null)
                    return;

                MCPlayer mcPlayer = MCPlayer.from(player);
                GlobalEventNode.get().call(new MCPlayerPacketSendEvent(mcPlayer, event));
            }

        }, PacketListenerPriority.LOWEST);
    }

    private void registerPacketListenerObjectCollector() {
        ContainerContext.get().objectCollectorRegistry().add(ContainerObjCollector.create()
                .withFilter(ContainerObjCollector.inherits(PacketListener.class))
                .withAddHandler(ContainerObjCollector.warpInstance(PacketListener.class, obj -> {
                    if (obj instanceof PacketListenerCommon) {
                        PacketEvents.getAPI().getEventManager().registerListener((PacketListenerCommon) obj);
                    } else {
                        // TODO: made it configurable?
                        final PacketListenerAbstract listener = obj.asAbstract(PacketListenerPriority.NORMAL);
                        PacketEvents.getAPI().getEventManager().registerListener(listener);

                        this.listenerCommonMap.put(obj.getClass(), listener);
                    }
                }))
                .withRemoveHandler(ContainerObjCollector.warpInstance(PacketListener.class, obj -> {
                    if (obj instanceof PacketListenerCommon) {
                        PacketEvents.getAPI().getEventManager().unregisterListener((PacketListenerCommon) obj);
                    } else {
                        final PacketListenerCommon listenerCommon = this.listenerCommonMap.remove(obj.getClass());
                        if (listenerCommon != null) {
                            PacketEvents.getAPI().getEventManager().unregisterListener(listenerCommon);
                        }
                    }
                }))
        );
    }

    @PostInitialize
    public void onPostInitialize() {
        this.packetEvents.getSettings()
                .debug(false)
                .bStats(false)
                .checkForUpdates(false);
        this.packetEvents.init();
    }

    @PostDestroy
    public void onPostDestroy() {
        this.packetEvents.terminate();
    }

    public static void sendPacket(MCPlayer mcPlayer, PacketWrapper<?> packetWrapper) {
        MCProtocol.INSTANCE.getPacketSender().sendPacket(mcPlayer, packetWrapper);
    }

    public static Terminable listen(PacketListenerCommon packetListener) {
        MCProtocol.INSTANCE.getPacketEvents().getEventManager().registerListener(packetListener);
        return () -> MCProtocol.INSTANCE.getPacketEvents().getEventManager().unregisterListener(packetListener);
    }

}
