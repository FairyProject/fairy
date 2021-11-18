package io.fairyproject.bukkit.mc;

import io.fairyproject.bukkit.protocol.BukkitNettyInjector;
import io.fairyproject.bukkit.util.Players;
import io.fairyproject.mc.*;
import io.fairyproject.mc.protocol.MCProtocol;
import io.fairyproject.mc.protocol.mapping.MCProtocolMapping1_8;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.UUID;
import java.util.stream.Collectors;

public class BukkitMCInitializer implements Runnable {
    @Override
    public void run() {
        MCProtocol.initialize(new BukkitNettyInjector(), new MCProtocolMapping1_8()); // TODO
        MCServer.Companion.CURRENT = new BukkitMCServer();
        MCEntity.Companion.BRIDGE = new MCEntity.Bridge() {
            @Override
            public MCEntity from(Object entity) {
                if (!(entity instanceof org.bukkit.entity.Entity)) {
                    throw new UnsupportedOperationException();
                }
                return new BukkitMCEntity((org.bukkit.entity.Entity) entity);
            }
        };
        MCWorld.Companion.BRIDGE = new MCWorld.Bridge() {
            @Override
            public MCWorld from(Object world) {
                if (!(world instanceof org.bukkit.World)) {
                    throw new UnsupportedOperationException();
                }
                return new BukkitMCWorld((org.bukkit.World) world);
            }
        };
        MCPlayer.Companion.BRIDGE = new MCPlayer.Bridge() {
            @Override
            public UUID from(Object obj) {
                return Players.tryGetUniqueId(obj);
            }

            @Override
            public MCPlayer find(UUID uuid) {
                final Player player = Bukkit.getPlayer(uuid);
                if (player != null) {
                    return MCPlayer.from(player);
                }
                return null;
            }

            @Override
            public MCPlayer create(Object obj) {
                if (!(obj instanceof Player)) {
                    throw new IllegalArgumentException();
                }
                return new BukkitMCPlayer((Player) obj);
            }

            @Override
            public Collection<MCPlayer> all() {
                return Bukkit.getOnlinePlayers().stream()
                        .map(MCPlayer::from)
                        .collect(Collectors.toList());
            }
        };
        MCGameProfile.Companion.BRIDGE = new MCGameProfile.Bridge() {
            @Override
            public MCGameProfile create(String name, UUID uuid) {
                return new BukkitMCGameProfile(name, uuid);
            }

            @Override
            public MCGameProfile from(Object object) {
                return BukkitMCGameProfile.CONVERTER.getSpecific(object);
            }
        };
    }
}
