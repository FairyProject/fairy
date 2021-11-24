package io.fairyproject.bukkit.events;

import io.fairyproject.container.PreInitialize;
import io.fairyproject.container.Service;
import io.fairyproject.bukkit.FairyBukkitPlatform;
import io.fairyproject.event.EventBus;
import io.fairyproject.mc.MCPlayer;
import io.fairyproject.mc.event.MCPlayerJoinEvent;
import io.fairyproject.mc.event.MCPlayerQuitEvent;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

@Service(name = "bukkit:event-transformer")
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
    }

    @Nullable
    public Class<?> getMC(Class<? extends Event> bukkitEvent) {
        return this.bukkitToMC.get(bukkitEvent);
    }

    private <B extends Event, M> void register(Class<B> bukkitClass, Class<M> mcClass, Function<B, M> transformer) {
        this.bukkitToMC.put(bukkitClass, mcClass);
        Bukkit.getPluginManager().registerEvent(bukkitClass, DUMMY_LISTENER, PRIORITY_REGISTRATION, (listener, event) -> {
            if (!bukkitClass.isInstance(event)) {
                return;
            }
            final M mcEvent = transformer.apply(bukkitClass.cast(event));
            EventBus.call(mcEvent);
        }, FairyBukkitPlatform.PLUGIN);
    }

}
