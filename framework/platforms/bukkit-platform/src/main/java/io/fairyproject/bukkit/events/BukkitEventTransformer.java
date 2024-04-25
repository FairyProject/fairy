package io.fairyproject.bukkit.events;

import io.fairyproject.bukkit.FairyBukkitPlatform;
import io.fairyproject.bukkit.util.BukkitPos;
import io.fairyproject.container.InjectableComponent;
import io.fairyproject.container.PreInitialize;
import io.fairyproject.event.GlobalEventNode;
import io.fairyproject.mc.MCPlayer;
import io.fairyproject.mc.MCWorld;
import io.fairyproject.mc.event.*;
import io.fairyproject.mc.event.world.MCWorldUnloadEvent;
import io.fairyproject.mc.registry.player.MCPlayerRegistry;
import io.fairyproject.mc.util.Position;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
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

@InjectableComponent
@RequiredArgsConstructor
public class BukkitEventTransformer {

    public static final EventPriority PRIORITY_REGISTRATION = EventPriority.MONITOR;
    private static final Listener DUMMY_LISTENER = new Listener() {
    };

    private final MCPlayerRegistry playerRegistry;
    private Map<Class<? extends Event>, Class<?>> bukkitToMC;

    @PreInitialize
    public void onPreInitialize() {
        this.bukkitToMC = new ConcurrentHashMap<>();
        this.register(AsyncPlayerPreLoginEvent.class, AsyncLoginEvent.class, this::transformAsyncLoginEvent);
        this.register(PlayerJoinEvent.class, NativePlayerLoginEvent.class, EventPriority.LOWEST, this::transformNativeLoginEvent);
        this.register(PlayerJoinEvent.class, MCPlayerJoinEvent.class, event -> new MCPlayerJoinEvent(playerRegistry.getByPlatform(event.getPlayer())));
        this.register(PlayerQuitEvent.class, MCPlayerQuitEvent.class, event -> {
            MCPlayer player = playerRegistry.findByPlatform(event.getPlayer());
            if (player == null) {
                return null;
            }
            return new MCPlayerQuitEvent(player);
        });
        this.register(PlayerMoveEvent.class, MCPlayerMoveEvent.class, event -> {
            Player player = event.getPlayer();
            Position fromPos = BukkitPos.toMCPos(event.getFrom());
            Position toPos = BukkitPos.toMCPos(event.getTo());

            return new MCPlayerMoveEvent(playerRegistry.getByPlatform(player), fromPos, toPos);
        }, (event, mcEvent) -> {
            if (mcEvent.isCancelled()) {
                event.setCancelled(true);
                return;
            }

            if (mcEvent.isChanged()) {
                event.setTo(BukkitPos.toBukkitLocation(mcEvent.getToPos()));
            }
        });
        this.register(PlayerTeleportEvent.class, MCPlayerTeleportEvent.class, event -> {
            Position fromPos = BukkitPos.toMCPos(event.getFrom());
            Position toPos = BukkitPos.toMCPos(event.getTo());

            return new MCPlayerTeleportEvent(playerRegistry.getByPlatform(event.getPlayer()), fromPos, toPos);
        }, (event, mcEvent) -> {
            if (mcEvent.isCancelled()) {
                event.setCancelled(true);
                return;
            }

            if (mcEvent.isChanged()) {
                event.setTo(BukkitPos.toBukkitLocation(mcEvent.getToPos()));
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
                event -> new MCPlayerChangedWorldEvent(playerRegistry.getByPlatform(event.getPlayer()), MCWorld.from(event.getFrom()), MCWorld.from(event.getPlayer().getWorld()))
        );
    }

    private NativePlayerLoginEvent transformNativeLoginEvent(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        return new NativePlayerLoginEvent(player, player.getUniqueId(), player.getName(), player.getAddress().getAddress());
    }

    private AsyncLoginEvent transformAsyncLoginEvent(AsyncPlayerPreLoginEvent event) {
        AsyncLoginEvent asyncLoginEvent = new AsyncLoginEvent(event.getName(), event.getUniqueId(), event.getAddress());
        asyncLoginEvent.setCancelled(event.getLoginResult() != AsyncPlayerPreLoginEvent.Result.ALLOWED);

        return asyncLoginEvent;
    }

    @Nullable
    public Class<?> getMC(Class<? extends Event> bukkitEvent) {
        return this.bukkitToMC.get(bukkitEvent);
    }

    private <B extends Event, M extends io.fairyproject.event.Event> void register(Class<B> bukkitClass, Class<M> mcClass, Function<B, M> transformer) {
        this.register(bukkitClass, mcClass, transformer, null);
    }

    private <B extends Event, M extends io.fairyproject.event.Event> void register(Class<B> bukkitClass, Class<M> mcClass, EventPriority priority, Function<B, M> transformer) {
        this.register(bukkitClass, mcClass, priority, transformer, null);
    }

    private <B extends Event, M extends io.fairyproject.event.Event> void register(Class<B> bukkitClass, Class<M> mcClass, Function<B, M> transformer, BiConsumer<B, M> postProcessing) {
        this.register(bukkitClass, mcClass, PRIORITY_REGISTRATION, transformer, postProcessing);
    }


    private <B extends Event, M extends io.fairyproject.event.Event> void register(Class<B> bukkitClass, Class<M> mcClass, EventPriority priority, Function<B, M> transformer, BiConsumer<B, M> postProcessing) {
        this.bukkitToMC.put(bukkitClass, mcClass);
        Bukkit.getPluginManager().registerEvent(bukkitClass, DUMMY_LISTENER, priority, (listener, event) -> {
            if (!bukkitClass.isInstance(event)) {
                return;
            }
            M mcEvent = transformer.apply(bukkitClass.cast(event));
            if (mcEvent == null) {
                return;
            }
            GlobalEventNode.get().call(mcEvent);
            if (postProcessing != null) {
                postProcessing.accept(bukkitClass.cast(event), mcEvent);
            }
        }, FairyBukkitPlatform.PLUGIN);
    }

}
