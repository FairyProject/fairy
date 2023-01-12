package io.fairyproject.mc.protocol;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.PacketEventsAPI;
import com.github.retrooper.packetevents.event.*;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import io.fairyproject.Debug;
import io.fairyproject.Fairy;
import io.fairyproject.container.*;
import io.fairyproject.container.collection.ContainerObjCollector;
import io.fairyproject.event.GlobalEventNode;
import io.fairyproject.log.Log;
import io.fairyproject.mc.MCPlayer;
import io.fairyproject.mc.MCServer;
import io.fairyproject.mc.protocol.event.MCPlayerPacketReceiveEvent;
import io.fairyproject.mc.protocol.event.MCPlayerPacketSendEvent;
import io.fairyproject.mc.protocol.impl.BukkitPacketEventsBuilder;
import io.fairyproject.mc.protocol.impl.mock.MockPacketEventsBuilder;
import io.fairyproject.mc.protocol.packet.PacketSender;
import io.fairyproject.mc.protocol.packet.impl.PacketSenderImpl;
import io.fairyproject.mc.protocol.packet.impl.PacketSenderMock;
import io.fairyproject.mc.version.MCVersionMappingRegistry;
import io.fairyproject.util.terminable.Terminable;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Getter
@Service
@RequiredArgsConstructor
public class MCProtocol {
    public static MCProtocol INSTANCE;

    private final MCVersionMappingRegistry mappingRegistry;

    private PacketSender packetSender;
    private PacketEventsAPI<?> packetEvents;

    private final Map<Class<?>, PacketListenerCommon> listenerCommonMap = new ConcurrentHashMap<>();

    @PreInitialize
    public void onPreInitialize() {
        INSTANCE = this;
        PacketEventsBuilder packetEventsBuilder;
        PacketSender packetSender;

        if (Debug.UNIT_TEST) {
            packetEventsBuilder = new MockPacketEventsBuilder(MCServer.current(), this.mappingRegistry);
            packetSender = PacketSenderMock.get();
        } else {
            packetSender = new PacketSenderImpl();
            switch (Fairy.getPlatform().getPlatformType()) {
                case BUKKIT:
                    packetEventsBuilder = new BukkitPacketEventsBuilder();
                    break;
                default:
                    throw new UnsupportedOperationException("The current platform aren't supported for protocol module.");
            }
        }

        this.packetEvents = packetEventsBuilder.build();
        PacketEvents.setAPI(this.packetEvents);
        this.packetEvents.load();
        this.packetSender = packetSender;

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
