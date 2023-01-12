package io.fairyproject.tests.bukkit;

import io.fairyproject.bukkit.mc.BukkitMCInitializer;
import io.fairyproject.bukkit.mc.operator.BukkitMCPlayerOperator;
import io.fairyproject.bukkit.reflection.BukkitNMSManager;
import io.fairyproject.mc.MCPlayer;
import io.fairyproject.mc.version.MCVersion;
import io.fairyproject.mc.version.MCVersionMappingRegistry;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.UUID;

public class BukkitTestingMCInitializer extends BukkitMCInitializer {

    @Override
    public MCPlayer.Bridge createPlayerBridge(MCVersionMappingRegistry versionMappingRegistry) {
        final MCPlayer.Bridge playerBridge = super.createPlayerBridge(versionMappingRegistry);
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
                return new BukkitMCPlayerMock(player.getUniqueId(), player.getName(), MCVersion.of(8), player, versionMappingRegistry); // version customize?
            }

            @Override
            public Collection<MCPlayer> all() {
                return playerBridge.all();
            }
        };
    }
}
