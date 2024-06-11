package io.fairyproject.bukkit.listener.impl;

import io.fairyproject.bukkit.listener.RegisterAsListener;
import io.fairyproject.container.InjectableComponent;
import io.fairyproject.mc.MCPlayer;
import io.fairyproject.mc.registry.player.MCPlayerRegistry;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

@InjectableComponent
@RegisterAsListener
@RequiredArgsConstructor
public class FairyPlayerListener implements Listener {

    private final MCPlayerRegistry registry;

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        MCPlayerRegistry.JOIN_QUIT_LOCK.lock();
        try {
            this.registry.removePlayer(player.getUniqueId());
        } finally {
            MCPlayerRegistry.JOIN_QUIT_LOCK.unlock();
        }
    }

}
