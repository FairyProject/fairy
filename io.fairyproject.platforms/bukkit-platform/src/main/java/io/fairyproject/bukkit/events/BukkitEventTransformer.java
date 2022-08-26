package io.fairyproject.bukkit.events;

import io.fairyproject.bukkit.FairyBukkitPlatform;
import io.fairyproject.bukkit.util.BukkitPos;
import io.fairyproject.container.PreInitialize;
import io.fairyproject.container.Service;
import io.fairyproject.event.GlobalEventNode;
import io.fairyproject.mc.MCPlayer;
import io.fairyproject.mc.MCWorld;
import io.fairyproject.mc.event.*;
import io.fairyproject.mc.event.world.MCWorldUnloadEvent;
import io.fairyproject.mc.util.Pos;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.*;
import org.bukkit.event.world.WorldUnloadEvent;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.Function;

@Service
public class BukkitEventTransformer {

    public static final EventPriority PRIORITY_REGISTRATION = EventPriority.MONITOR;
    private static final Listener DUMMY_LISTENER = new Listener() {
    };

    private Map<Class<? extends Event>, Class<?>> bukkitToMC;

    @PreInitialize
    public void onPreInitialize() {
        this.bukkitToMC = new ConcurrentHashMap<>();
        this.register(PlayerJoinEvent.class, MCPlayerJoinEvent.class, event -> new MCPlayerJoinEvent(MCPlayer.from(event.getPlayer())));
        this.register(PlayerQuitEvent.class, MCPlayerQuitEvent.class, event -> new MCPlayerQuitEvent(MCPlayer.from(event.getPlayer())));
        this.register(PlayerMoveEvent.class, MCPlayerMoveEvent.class, event -> {
            Pos fromPos = BukkitPos.toMCPos(event.getFrom());
            Pos toPos = BukkitPos.toMCPos(event.getTo());

            return new MCPlayerMoveEvent(MCPlayer.from(event.getPlayer()), fromPos, toPos);
        }, (event, mcEvent) -> {
            if (mcEvent.isCancelled()) {
                event.setCancelled(true);
                return;
            }

            if (mcEvent.changed()) {
                event.setTo(BukkitPos.toBukkitLocation(mcEvent.toPos()));
            }
        });
        this.register(PlayerTeleportEvent.class, MCPlayerTeleportEvent.class, event -> {
            Pos fromPos = BukkitPos.toMCPos(event.getFrom());
            Pos toPos = BukkitPos.toMCPos(event.getTo());

            return new MCPlayerTeleportEvent(MCPlayer.from(event.getPlayer()), fromPos, toPos);
        }, (event, mcEvent) -> {
            if (mcEvent.isCancelled()) {
                event.setCancelled(true);
                return;
            }

            if (mcEvent.changed()) {
                event.setTo(BukkitPos.toBukkitLocation(mcEvent.toPos()));
            }
        });
        this.register(WorldUnloadEvent.class, MCWorldUnloadEvent.class,
                event -> new MCWorldUnloadEvent(MCWorld.from(event.getWorld())),
                (event, mcEvent) -> {
                    if (mcEvent.isCancelled()) {
                        event.setCancelled(true);
                    }
                });
        this.register(PlayerChangedWorldEvent.class, MCPlayerChangedWorldEvent.class,
                event -> new MCPlayerChangedWorldEvent(MCPlayer.from(event.getPlayer()), MCWorld.from(event.getFrom()), MCWorld.from(event.getPlayer().getWorld()))
        );
    }

    @Nullable
    public Class<?> getMC(Class<? extends Event> bukkitEvent) {
        return this.bukkitToMC.get(bukkitEvent);
    }

    private <B extends Event, M extends io.fairyproject.event.Event> void register(Class<B> bukkitClass, Class<M> mcClass, Function<B, M> transformer) {
        this.register(bukkitClass, mcClass, transformer, null);
    }

    private <B extends Event, M extends io.fairyproject.event.Event> void register(Class<B> bukkitClass, Class<M> mcClass, Function<B, M> transformer, BiConsumer<B, M> postProcessing) {
        this.bukkitToMC.put(bukkitClass, mcClass);
        Bukkit.getPluginManager().registerEvent(bukkitClass, DUMMY_LISTENER, PRIORITY_REGISTRATION, (listener, event) -> {
            if (!bukkitClass.isInstance(event)) {
                return;
            }
            final M mcEvent = transformer.apply(bukkitClass.cast(event));
            GlobalEventNode.get().call(mcEvent);
            if (postProcessing != null) {
                postProcessing.accept(bukkitClass.cast(event), mcEvent);
            }
        }, FairyBukkitPlatform.PLUGIN);
    }

}
