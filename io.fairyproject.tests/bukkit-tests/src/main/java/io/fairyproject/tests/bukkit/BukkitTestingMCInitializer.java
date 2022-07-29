package io.fairyproject.tests.bukkit;

import io.fairyproject.bukkit.mc.BukkitMCInitializer;
import io.fairyproject.mc.MCPlayer;
import io.fairyproject.mc.protocol.MCVersion;
import io.fairyproject.tests.mc.MCPlayerMock;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.UUID;

public class BukkitTestingMCInitializer extends BukkitMCInitializer {

    @Override
    public MCPlayer.Bridge createPlayerBridge() {
        final MCPlayer.Bridge playerBridge = super.createPlayerBridge();
        return new MCPlayer.Bridge() {
            @Override
            public UUID from(@NotNull Object obj) {
                return playerBridge.from(obj);
            }

            @Override
            public MCPlayer find(UUID uuid) {
                return playerBridge.find(uuid);
            }

            @Override
            public MCPlayer create(Object obj) {
                final Player player = (Player) obj;
                return new MCPlayerMock(player.getUniqueId(), player.getName(), MCVersion.V1_8, player); // version customize?
            }

            @Override
            public Collection<MCPlayer> all() {
                return playerBridge.all();
            }
        };
    }
}
